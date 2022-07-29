package com.lauter.applifecycle

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.android.utils.FileUtils
import org.apache.commons.compress.utils.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.File
import java.util.concurrent.Callable

class AppLifecyclePlugin : Transform(), Plugin<Project> {

    private val proxySuffix = "_Proxy"
    private val managerClassFile = "com/lauter/applifecycle/AppLifecycleManager.class"
    private val callbackInfo = "com/lauter/applifecycle/AppLifecycleCallback"

    override fun getName(): String = "AppLifecyclePlugin"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean = true

    override fun apply(project: Project) {
        project.extensions.findByType(AppExtension::class.java)?.run {
            println("AppLifecycle: app plugin apply transform")
            registerTransform(this@AppLifecyclePlugin)
        }
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        transformInvocation?.run {
            val executor = WaitableExecutor.useGlobalSharedThreadPool()
            val dirInput = mutableListOf<DirectoryInput>()
            val jarInput = mutableListOf<JarInput>()
            inputs.forEach {
                dirInput.addAll(it.directoryInputs)
                jarInput.addAll(it.jarInputs)
            }
            dirInput.forEach(this) { input, output ->
                val inputDir = input.file.absolutePath
                val outputDir = output.absolutePath
                if (isIncremental) {
                    input.changedFiles.filter { it.key.isClass() }.forEach { (file, status) ->
                        if (status in arrayOf(Status.CHANGED, Status.ADDED)) {
                            executor.execute {
                                val outputFile = File(file.absolutePath.replace(inputDir, outputDir))
                                FileUtils.deleteIfExists(outputFile)
                                file.copyTo(outputFile)
                                transformFile(outputFile)
                            }
                        }
                    }
                } else {
                    output.deleteRecursively()
                    input.file.copyRecursively(output)
                    output.walk().filter { it.isClass() }
                        .forEach {file ->
                            executor.execute {
                                transformFile(file)
                            }
                        }
                }
            }
            jarInput.forEach(this) { input, output ->
                if (isIncremental) {
                    when(input.status){
                        Status.REMOVED -> {
                            FileUtils.deleteIfExists(input.file)
                        }
                        in arrayOf(Status.CHANGED, Status.ADDED) -> {
                            executor.execute(TransformJarTask(input.file,output))
                        }
                    }
                } else {
                    executor.execute(TransformJarTask(input.file,output))
                }
            }
            executor.waitForTasksWithQuickFail<Any>(true)
            onTransformComplete()
        }
    }

    private val appLifecycleClassNames = mutableListOf<String>()
    private var appLifecyclesJar:File? = null

    private fun transformFile(file: File) {
        val classReader = ClassReader(file.readBytes())
        val name = classReader.className
        if (name.endsWith(proxySuffix) &&
            classReader.interfaces.contains(callbackInfo)
        ) {
            appLifecycleClassNames.add(name)
        }
    }

    private fun transformJar(inputJar: File, outputJar: File) {
        JarHandler.create(inputJar, outputJar).filter { _, jarEntity ->
            if(jarEntity.name == managerClassFile){
                appLifecyclesJar = outputJar
            }
            jarEntity.name.isClass() && jarEntity.name.endsWith("$proxySuffix.class")
        }.map { _, inputStream ->
            val byteArray = IOUtils.toByteArray(inputStream)
            val classReader = ClassReader(byteArray)
            if (classReader.className.endsWith(proxySuffix) && classReader.interfaces.contains(
                    callbackInfo
                )
            ) {
                appLifecycleClassNames.add(classReader.className)
            }
            byteArray
        }
    }

    private fun onTransformComplete() {
        if(appLifecyclesJar == null){
            println("AppLifecycle: no jar contain $managerClassFile")
            return
        }
        println("AppLifecycle: find jar contain $managerClassFile $appLifecycleClassNames")
        val targetJar = appLifecyclesJar!!
        // 创建临时jar
        val file = File(targetJar.parent,targetJar.nameWithoutExtension+"_temp.jar")
        if(file.exists()){
            file.delete()
        }
        targetJar.copyTo(file,true)
        JarHandler(file,targetJar).filter { _, jarEntity ->
            jarEntity.name == managerClassFile
        }.doComplete {
            file.delete()
        }.map { _, inputStream ->
            val classReader = ClassReader(
                IOUtils.toByteArray(inputStream)
            )
            val classWriter = ClassWriter(
                classReader,
                ClassWriter.COMPUTE_MAXS
            )
            val cv: ClassVisitor = AppLifecycleVisitor(classWriter,appLifecycleClassNames)
            classReader.accept(cv, ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        }
    }

    private inner class TransformJarTask(private val inputJar:File, private val outputJar:File) :
        Callable<Any?> {
        override fun call(): Any? {
            FileUtils.deleteIfExists(outputJar)
            transformJar(inputJar, outputJar)
            return null
        }
    }
}