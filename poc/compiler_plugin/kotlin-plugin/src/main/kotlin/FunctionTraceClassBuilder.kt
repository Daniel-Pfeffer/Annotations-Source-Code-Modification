import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
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
                InstructionAdapter(this).visitCode()
            }
        }
    }
}