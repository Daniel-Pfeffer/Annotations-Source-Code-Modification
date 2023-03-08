import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

internal class FunctionTraceClassBuilder(
    internal val classBuilder: ClassBuilder,
    annotations: List<String>,
) : DelegatingClassBuilder() {

    val annotations: List<FqName> = annotations.map { FqName(it) }
    override fun getDelegate(): ClassBuilder = classBuilder

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
        val function = origin.descriptor as? FunctionDescriptor ?: error("Unexpected error occurred")
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        if (annotations.none { function.annotations.hasAnnotation(it) }) {
            return original
        }

        return object : MethodVisitor(Opcodes.ASM5, original) {
            override fun visitCode() {
                super.visitCode()
                InstructionAdapter(this).onFunctionVisit(function)
            }

            override fun visitInsn(opcode: Int) {
                when (opcode) {
                    Opcodes.RETURN, Opcodes.ARETURN, Opcodes.DRETURN, Opcodes.FRETURN, Opcodes.IRETURN, Opcodes.LRETURN -> InstructionAdapter(
                        this
                    ).onFunctionReturn(function)
                }
                super.visitInsn(opcode)
            }
        }
    }
}

private fun InstructionAdapter.onFunctionVisit(function: FunctionDescriptor) {
    printStatic("Enter ${function.name}")
}

private fun InstructionAdapter.printStatic(toPrint: String) {
    val stringBuilderType = Type.getType(StringBuilder::class.java)
    getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
    anew(stringBuilderType)
    dup()
    invokespecial(stringBuilderType.internalName, "<init>", "()V", false)
    visitLdcInsn(toPrint)
    invokevirtual(
        stringBuilderType.internalName,
        "append",
        "(Ljava/lang/String;)L${stringBuilderType.internalName};",
        false
    )
    invokevirtual(stringBuilderType.internalName, "toString", "()Ljava/lang/String;", false)
    invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
}


private fun InstructionAdapter.onFunctionReturn(function: FunctionDescriptor) {
    printStatic("Exit ${function.name}")
}