package com.lauter.applifecycle

import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

class AppLifecycleProxyBuilder(private val typeElement: TypeElement, elements: Elements) {

    private val packageName = elements.getPackageOf(typeElement).qualifiedName.toString()
    private val fileName = "${typeElement.simpleName}_Proxy"
    private val contextType = elements.getTypeElement("android.content.Context").asClassName()
    private val superInterface = elements.getTypeElement(AppLifecycleProcessor.callbackName).asClassName()

    fun build(): FileSpec {
        println("AppLifecycle: Build Proxy for ${typeElement.qualifiedName}")
        return FileSpec.builder(packageName, fileName)
            .addType(getTypeSpec())
            .build()
    }

    private fun getTypeSpec(): TypeSpec {
        return TypeSpec.classBuilder(fileName)
            .addProperty(getProperty())
            .addSuperinterface(superInterface)
            .addFunction(getOnCreate())
            .addFunction(getPriority())
            .build()
    }

    private fun getProperty(): PropertySpec {
        // 对应注解类实例
        return PropertySpec.builder("callback", typeElement.asClassName(), KModifier.PRIVATE)
            .initializer("%T()", typeElement.asType())
            .build()
    }

    private fun getOnCreate(): FunSpec {
        // onCreate(context: Context)
        return FunSpec.builder("onCreate")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("context", contextType)
            .addStatement("callback.onCreate(context)")
            .build()
    }

    private fun getPriority(): FunSpec {
        // getPriority(): Int
        return FunSpec.builder("getPriority")
            .addModifiers(KModifier.OVERRIDE)
            .returns(Int::class)
            .addStatement("return callback.getPriority()")
            .build()
    }
}