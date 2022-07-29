package com.lauter.applifecycle

import org.apache.commons.compress.utils.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class JarHandler(private val inputJar: File, private val outputJar: File){

    private var mFilters:MutableList<((jarFile: JarFile, jarEntity: JarEntry) -> Boolean)> = mutableListOf()
    private var mComplete:(()->Unit)? = null

    fun filter(predicate: (jarFile: JarFile, jarEntity: JarEntry) -> Boolean) : JarHandler {
        mFilters.add(predicate)
        return this
    }

    fun doComplete(action:() -> Unit) : JarHandler {
        mComplete = action
        return this
    }

    fun map(transform:(jarEntry: JarEntry, inputStream: InputStream) -> ByteArray) : JarHandler {
        JarFile(inputJar).use { jarFile ->
            JarOutputStream(FileOutputStream(outputJar)).use { outputStream ->
                jarFile.stream().forEach { jarEntry: JarEntry? ->
                    val entryName = jarEntry?.name
                    val inputStream: InputStream = jarFile.getInputStream(jarEntry)
                    outputStream.putNextEntry(ZipEntry(entryName))
                    var result = false
                    mFilters.forEach { predicate ->
                        if(entryName != null) {
                            result = result xor predicate(jarFile,jarEntry)
                        }
                    }
                    if(result && jarEntry != null){
                        outputStream.write(transform(jarEntry,inputStream))
                    } else {
                        outputStream.write(IOUtils.toByteArray(inputStream))
                    }
                    outputStream.closeEntry()
                }
            }
            mComplete?.invoke()
        }
        return this
    }

    companion object {

        fun create(inputJar: File, outputJar: File) = JarHandler(inputJar,outputJar)
    }
}