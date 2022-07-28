package com.lauter.applifecycle

import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@AutoService(Processor::class)
class AppLifecycleProcessor : AbstractProcessor() {

    // const
    companion object {
        const val callbackName = "com.lauter.applifecycle.AppLifecycleCallback"
    }

    private lateinit var filer: Filer
    private lateinit var elements: Elements
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv?.run {
            this@AppLifecycleProcessor.filer = filer
            this@AppLifecycleProcessor.elements = elementUtils
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_11

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AppLifecycle::class.java.canonicalName)
    }

    override fun process(p0: MutableSet<out TypeElement>?, environment: RoundEnvironment?): Boolean {
        environment?.run {
            getElementsAnnotatedWith(AppLifecycle::class.java)
                .filter { it.kind == ElementKind.CLASS }
                .filter {
                    (it as TypeElement).interfaces.contains(
                        elements.getTypeElement(callbackName).asType()
                    )
                }
                .forEach {
                    AppLifecycleProxyBuilder(it as TypeElement, elements).build().writeTo(filer)
                }
        }
        return true
    }
}