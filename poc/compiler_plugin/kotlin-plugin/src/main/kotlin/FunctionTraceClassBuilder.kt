import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.commons.LocalVariablesSorter

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

        // we have to override the LocalVariableSorter with the protected constructor,
        // otherwise the init throws an IllegalStateException
        return object : LocalVariablesSorter(Opcodes.API_VERSION, access, desc, original) {
            var index = 0
            override fun visitCode() {
                super.visitCode()
                index = newLocalMapping(Type.LONG_TYPE)
                InstructionAdapter(this).onFunctionVisit(function, index)
            }

            override fun visitInsn(opcode: Int) {
                when (opcode) {
                    Opcodes.RETURN, Opcodes.ARETURN, Opcodes.DRETURN, Opcodes.FRETURN, Opcodes.IRETURN, Opcodes.LRETURN -> InstructionAdapter(
                        this
                    ).onFunctionReturn(function, index)
                }
                super.visitInsn(opcode)
            }
        }
    }
}

private fun InstructionAdapter.onFunctionVisit(function: FunctionDescriptor, index: Int) {
    val params = function.valueParameters.joinToString(", ") { it.name.toString() }
    printStatic("Enter ${function.name}($params)")
    invokestatic("java/lang/System", "currentTimeMillis", "()J", false)
    // pops value from stack and stores it at fp+index in random access stack ("variable stack")
    store(index, Type.LONG_TYPE)
}

val stringBuilderType = Type.getType(StringBuilder::class.java)

private fun InstructionAdapter.createPrintable() {
    getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
    anew(stringBuilderType)
    dup()
    invokespecial(stringBuilderType.internalName, "<init>", "()V", false)
}

private fun InstructionAdapter.appendPrintable(toPrint: Any) {
    visitLdcInsn(toPrint)
    invokevirtual(
        stringBuilderType.internalName,
        "append",
        "(Ljava/lang/String;)L${stringBuilderType.internalName};",
        false
    )
}

private fun InstructionAdapter.print() {
    invokevirtual(stringBuilderType.internalName, "toString", "()Ljava/lang/String;", false)
    invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
}

private fun InstructionAdapter.printStatic(toPrint: String) {
    createPrintable()
    appendPrintable(toPrint)
    print()
}


private fun InstructionAdapter.onFunctionReturn(function: FunctionDescriptor, index: Int) {
    createPrintable()
    appendPrintable("Exit ${function.name}() after ")
    // get current time in milliseconds
    invokestatic("java/lang/System", "currentTimeMillis", "()J", false)
    // load old value in ms
    load(index, Type.LONG_TYPE)
    sub(Type.LONG_TYPE)
    invokevirtual(stringBuilderType.internalName, "append", "(J)L${stringBuilderType.internalName};", false)
    appendPrintable("ms")
    print()
}