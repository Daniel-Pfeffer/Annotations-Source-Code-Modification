package social.xperience.k2

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nameForIrSerialization
import org.jetbrains.kotlin.name.FqName

class PowerAssertCallTransformer(
    private val sourceFile: IrFile,
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val annotation: FqName = FqName("social.xperience.Holds"),
) : IrElementTransformerVoidWithContext() {
    private val annotatedFields = mutableListOf<IrField>()

    /**
     * Field annotation, with specifying annotation target
     */
    override fun visitFieldNew(declaration: IrField): IrStatement {
        if (declaration.annotations.hasAnnotation(annotation)) {
            annotatedFields.add(declaration)
        }
        return super.visitFieldNew(declaration)
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        return super.visitFunctionNew(declaration)
    }

    /**
     * Field annotation, without specifying the @field annotation target
     */
    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        return super.visitPropertyNew(declaration)
    }

    fun IrDeclaration.stringify(): String {
        return this.parent.kotlinFqName.asString() + "#" + this.nameForIrSerialization
    }


    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        return super.visitDeclaration(declaration)
    }

    override fun visitSetField(expression: IrSetField): IrExpression {
        return super.visitSetField(expression)
    }
}