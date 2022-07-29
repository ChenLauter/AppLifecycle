package com.lauter.applifecycle

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class AppLifecycleVisitor(classVisitor: ClassVisitor,
                          private val callbacks: List<String>) : ClassVisitor(Opcodes.ASM9,classVisitor) {


    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var visitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if ("<init>" == name && "()V" == descriptor && access and Opcodes.ACC_PRIVATE != 0) {
            visitor = object : AdviceAdapter(ASM9, visitor, access, name, descriptor) {
                override fun onMethodExit(opcode: Int) {
                    for (item in callbacks) {
                        mv.visitVarInsn(ALOAD, 0)
                        mv.visitLdcInsn(item.replace("/", "."))
                        mv.visitMethodInsn(
                            INVOKESPECIAL,
                            "com/lauter/applifecycle/AppLifecycleManager",
                            "registerAppLifecycleCallback",
                            "(Ljava/lang/String;)V",
                            false
                        )
                    }
                }
            }
        }
        return visitor
    }
}