package social.xperience.k2

import org.jetbrains.kotlin.backend.common.CompilationException
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.propertyIfAccessor
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class InvariantCallTransformer(
    private val sourceFile: IrFile,
    private val builtIns: IrBuiltIns,
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val annotation: FqName = FqName("social.xperience.Holds"),
) : IrElementTransformerVoidWithContext() {
    private val irFactory: IrFactory = context.irFactory

    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (declaration.classId == SharedReferences.poolClassId) {
            SharedReferences.poolClass = declaration
        }

        if (!declaration.hasAnonymousInitializer()) {
            declaration.declarations.add(
                irFactory.createAnonymousInitializer(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    IrDeclarationOrigin.INSTANCE_RECEIVER,
                    IrAnonymousInitializerSymbolImpl(),
                    false
                ).also {
                    it.parent = declaration
                    // empty body
                    it.body = DeclarationIrBuilder(context, it.symbol).irBlockBody {
                    }
                })
        }

        return super.visitClassNew(declaration)
    }

    private fun IrClass.hasAnonymousInitializer(): Boolean {
        declarations.forEach {
            if (it is IrAnonymousInitializer) {
                return true
            }
        }
        return false
    }

    private fun IrBuilderWithScope.generateVerificationFunctionCall(
        element: IrDeclarationBase,
        argument: IrExpression,
    ): List<IrFunctionAccessExpression> {
        return element.getAnnotations(annotation)
            .map { xy ->
                val classReference = xy.getValueArgument(0)!! as IrClassReference
                generateNewPoolProperty(classReference)

                val classId = (classReference.symbol.owner as IrClass).classId!!
                val function =
                    this@InvariantCallTransformer.context.referenceFunctions(
                        CallableId(
                            classId,
                            Name.identifier("verify")
                        )
                    )
                        .single()
                irCall(function).also {
                    it.dispatchReceiver = irGetField(
                        irGetObject(SharedReferences.poolClass.symbol),
                        SharedReferences.poolClass.properties.find { field -> field.name == classReference.variableIdentifier() }!!.backingField!!
                    )
                    it.putValueArgument(0, argument)
                }
            }
    }

    private fun IrClassReference.variableIdentifier(): Name {
        return Name.identifier((symbol.owner as IrClass).name.asString().lowercase())
    }

    /**
     * Generates a new Verification Pool property to avoid constructor calls, as Verification classes must be stateless
     */
    private fun generateNewPoolProperty(classReference: IrClassReference) {
        val owner = classReference.symbol.owner as IrClass
        if (SharedReferences.generatedPoolFields.contains(owner)) {
            return
        }
        SharedReferences.poolClass.addProperty {
            name = classReference.variableIdentifier()
            modality = Modality.FINAL
        }.also { property ->
            property.backingField = irFactory.buildField {
                isFinal = true
                name = classReference.variableIdentifier()
                type = classReference.classType
            }.also { field ->
                val primaryConstructorSymbol = owner.primaryConstructor!!.symbol
                field.correspondingPropertySymbol = property.symbol
                field.parent = SharedReferences.poolClass
                field.initializer = irFactory.createExpressionBody(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    IrConstructorCallImpl.fromSymbolOwner(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        primaryConstructorSymbol.owner.returnType,
                        primaryConstructorSymbol,
                        0
                    )
                )
            }
        }
        SharedReferences.generatedPoolFields.add(owner)
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        val irFunction = expression.symbol.owner
        if (irFunction.isPropertySetterAccessor && currentFunction != null) {
            val property = irFunction.propertyIfAccessor as IrProperty
            if (property.hasAnnotation(annotation) || property.parentAsClass.hasAnnotation(annotation)) {
                return DeclarationIrBuilder(context, expression.symbol).irBlock {
                    +super.visitFunctionAccess(expression)
                    +generateVerificationFunctionCall(
                        property, irCall(property.getter!!).also {
                            it.dispatchReceiver = expression.dispatchReceiver
                        }
                    )
                }
            }
        }
        return super.visitFunctionAccess(expression)
    }

    val IrFunction.isPropertySetterAccessor: Boolean
        get() = isPropertyAccessor && isSetter


    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        declaration.valueParameters.forEach {
            if (it.hasAnnotation(annotation)) {
                it.isPropertyAccessor
                val property =
                    declaration.parentAsClass.properties.firstOrNull { prop -> prop.name == it.name }
                if (property != null) {
                    (property.annotations as MutableList).addAll(
                        it.getAnnotations(annotation)
                    )
                }
            }
        }
        return super.visitConstructor(declaration)
    }

    private fun IrAnnotationContainer.getAnnotations(annotation: FqName): List<IrConstructorCall> {
        return annotations.filter { it.isAnnotation(annotation) }
    }

    override fun visitAnonymousInitializerNew(declaration: IrAnonymousInitializer): IrStatement {
        val clazz = declaration.parentAsClass
        val statement = DeclarationIrBuilder(context, declaration.symbol).irBlock {
            if (clazz.hasAnnotation(annotation)) {
                +generateVerificationFunctionCall(
                    clazz, irGet(clazz.thisReceiver!!, clazz.defaultType)
                )
            }
            clazz.properties.forEach {
                if (it.hasAnnotation(annotation)) {
                    +generateVerificationFunctionCall(
                        it, irGetField(
                            irGet(clazz.thisReceiver!!),
                            it.backingField!!,
                            it.backingField!!.type
                        )
                    )
                }
            }
        }

        declaration.body
            .statements.add(statement)
        return super.visitAnonymousInitializerNew(declaration)
    }

    /**
     * If the declaration has a `HOLDS` annotation, inject a verification for all function params
     * Injects a function call at the end of the function body
     */
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        // function has function level annotation -> verify function parameter
        if (declaration.hasAnnotation(annotation) && declaration !is IrConstructor) {
            val verificationCall = generateVerificationCallForFunction(declaration)
            (declaration.body!!.statements as MutableList).add(0, verificationCall)
        }

        // We currently don't care about function property accessor, constructor and fake overrides.
        // Fake overrides are functions of parents that we don't explicitly override
        if (declaration.isPropertyAccessor || declaration.isFakeOverride) {
            return super.visitFunctionNew(declaration)
        }

        if (declaration !is IrConstructor) {
            val body = declaration.body ?: return super.visitFunctionNew(declaration)

            val valueParameterVerifications = DeclarationIrBuilder(context, declaration.symbol).irBlock {
                declaration.valueParameters.forEach {
                    if (it.hasAnnotation(annotation)) {
                        +generateVerificationFunctionCall(it, irGet(it))
                    }
                }
            }
            (body.statements as MutableList).add(0, valueParameterVerifications)
        }

        return super.visitFunctionNew(declaration)
    }

    private fun generateVerificationCallForFunction(declaration: IrFunction): IrExpression {
        val numberOfValueParams = declaration.valueParameters.size
        if (numberOfValueParams < 1 || numberOfValueParams > 15) {
            throw CompilationException(
                "@Holds annotation for functions with $numberOfValueParams is not supported",
                currentFile,
                declaration
            )
        }
        // use "." for children of sealed classes, that are !directly! nested
        // could be optimized further by introducing storing in a fixed size list
        val classToUse =
            context.referenceClass(ClassId.fromString("social/xperience/common/FunctionVerifierClass.FunctionVerifier$numberOfValueParams"))!!

        return DeclarationIrBuilder(context, declaration.symbol).irBlock {
            +generateVerificationFunctionCall(declaration,
                irCallConstructor(classToUse.owner.primaryConstructor!!.symbol,
                    declaration.valueParameters.map { it.type }).also {
                    it.putValueArgument(0, irGet(declaration.valueParameters.first()))
                })
        }
    }
}