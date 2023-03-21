package social.xperience.k1

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FieldDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.load.kotlin.computeJvmDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import kotlin.reflect.typeOf

class InvariantClassBuilder(
    internal val classBuilder: ClassBuilder,
) : DelegatingClassBuilder() {
    override fun getDelegate(): ClassBuilder = classBuilder

    val holdsAnnotation = FqName("social.xperience.Holds")

    val fields: MutableList<String> = mutableListOf()

    override fun defineClass(
        origin: PsiElement?,
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>,
    ) {
        super.defineClass(origin, version, access, name, signature, superName, interfaces)
    }

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        // add support for fields, not target annotated with ${fieldname}\$annotations
        return super.newMethod(origin, access, name, desc, signature, exceptions)
    }

    override fun newField(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        value: Any?,
    ): FieldVisitor {
        val field = origin.descriptor ?: error("Unexpected error occurred")
        val original = super.newField(origin, access, name, desc, signature, value)
        // collect all fields with an annotation
        if (field.annotations.hasAnnotation(holdsAnnotation)) {
            fields.add(desc)
        }
        return original
    }

    override fun done(generateSmapCopyToAnnotation: Boolean) {
        generateToString()
        super.done(generateSmapCopyToAnnotation)
    }


    private fun generateToString() {
        val mv = newMethod(
            JvmDeclarationOrigin.NO_ORIGIN, Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null
        )
        val fields = fields.joinToString(", ") { it }
        mv.visitCode()
        mv.visitTypeInsn(Opcodes.NEW, STRING_BUILDER.internalName)
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, STRING_BUILDER.internalName, "<init>", "()V", false)
        mv.visitLdcInsn(fields)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            STRING_BUILDER.internalName,
            "append",
            "(Ljava/lang/String;)L${STRING_BUILDER.internalName};",
            false
        )
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL, STRING_BUILDER.internalName, "toString", "()Ljava/lang/String;", false
        )
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitEnd()
    }

    companion object {
        val STRING_BUILDER = Type.getType(StringBuilder::class.java)
    }
}