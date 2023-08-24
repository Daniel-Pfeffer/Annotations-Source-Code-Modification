package social.xperience.k2

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.backend.Fir2IrConverter
import org.jetbrains.kotlin.ir.builders.declarations.IrValueParameterBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeBuilder
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.name.Name

class InvariantIrGenerationExtension(
    private val messageCollector: MessageCollector,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // we add a custom class to the first file we read, at the first opportunity
        // problematic is what happens with multiple modules?
        moduleFragment.files.first().also {
            it.declarations.add(0, createVerificationPool(pluginContext, it))
        }
        moduleFragment.files.forEach {
            InvariantCallTransformer(it, pluginContext.irBuiltIns, pluginContext, messageCollector).visitFile(it)
            messageCollector.report(CompilerMessageSeverity.INFO, it.dump())
        }
    }

    /**
     * Here be dragons.
     * This creates a custom verification pool objects to minimize constructor calls for VerificationObjects
     */
    private fun createVerificationPool(context: IrPluginContext, file: IrFile): IrClass {
        return context.irFactory.buildClass {
            // creates a new final object with name VerificationPool, parent inherits package name
            kind = ClassKind.OBJECT
            name = Name.identifier("VerificationPool")
            modality = Modality.FINAL
        }.also { irClass ->
            irClass.superTypes = listOf(context.irBuiltIns.anyType)
            // add a special valueparameter as the thisReceiver
            irClass.thisReceiver =
                context.irFactory.buildValueParameter(
                    IrValueParameterBuilder().also { param ->
                        param.name = Name.special("<this>")
                        // type has to be the class Type cannot be `irClass.defaultType` as this uses the receiver type
                        param.type = IrSimpleTypeBuilder()
                            .also { it.classifier = irClass.symbol }
                            .buildSimpleType()
                        // set origin as instance_receiver just to be sure...
                        param.origin = IrDeclarationOrigin.INSTANCE_RECEIVER
                    }, irClass
                )
            // add the primary constructor
            irClass.declarations.add(
                context.irFactory.buildConstructor {
                    isPrimary = true
                    returnType = irClass.defaultType
                    // object have private constructor
                    visibility = DescriptorVisibilities.PRIVATE
                }.also { constructor ->
                    constructor.parent = irClass
                    val anyConstructor = context.irBuiltIns.anyClass.constructors.first().owner
                    // constructor have to call a delegate constructor to parent and instance the object
                    constructor.body = context.irFactory.createBlockBody(
                        -1, -1, listOf(
                            IrDelegatingConstructorCallImpl(
                                -1,
                                -1,
                                context.irBuiltIns.unitType,
                                anyConstructor.symbol,
                                0,
                                0
                            ),
                            IrInstanceInitializerCallImpl(-1, -1, irClass.symbol, context.irBuiltIns.unitType)
                        )
                    )
                })
            irClass.parent = file
            // set sharedReference class to complete classid consisting of package and name
            //throw CompilationException("Uwu sum error occurred ${irClass.classId}", file, null, null)
            SharedReferences.poolClassId = irClass.classId!!
        }
    }
}