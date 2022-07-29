package com.lauter.applifecycle

import com.android.SdkConstants
import com.android.build.api.transform.*
import java.io.File

inline fun QualifiedContent.getOutputLocation(invocation: TransformInvocation): File {
    val format = when (this) {
        is JarInput -> {
            Format.JAR
        }
        is DirectoryInput -> {
            Format.DIRECTORY
        }
        else -> {
            throw AssertionError("Unsupported qualifiedContent of ${this.javaClass}")
        }
    }
    return invocation.outputProvider.getContentLocation(name, contentTypes, scopes, format)
}

inline fun <T : QualifiedContent> List<T>.forEach(invocation: TransformInvocation, action: (input: T, output: File) -> Unit) {
    for (item in this) {
        if (item is JarInput) {
            action(item, item.getOutputLocation(invocation))
        } else if (item is DirectoryInput) {
            action(item, item.getOutputLocation(invocation))
        }
    }
}

inline fun File.isClass() = name.endsWith(SdkConstants.DOT_CLASS)

inline fun String.isClass() = this.endsWith(SdkConstants.DOT_CLASS)