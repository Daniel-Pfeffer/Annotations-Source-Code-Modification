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
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.interpreter.getAnnotation
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.collections.MutableList
import kotlin.collections.MutableSet
import kotlin.collections.emptyList
import kotlin.collections.first
import kotlin.collections.last
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.collections.set
import kotlin.collections.single

class InvariantCallTransformer(
    private val sourceFile: IrFile,
    private val builtIns: IrBuiltIns,
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val annotation: FqName = FqName("social.xperience.Holds"),
) : IrElementTransformerVoidWithContext() {

    private var hasPropertyLevelAnnotation: Boolean = false

    private lateinit var poolClass: IrClass

    /**
     * We only care about property annotations, as in Kotlin we can't create a Field directly,
     * thus a field annotation wouldn't be intelligent
     */
    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        // check if a property has an annotation
        hasPropertyLevelAnnotation = hasPropertyLevelAnnotation || declaration.hasAnnotation(annotation)
        return super.visitPropertyNew(declaration)
    }

    override fun visitClassNew(declaration: IrClass): IrStatement {
        // check if the class has a class level annotation
        hasPropertyLevelAnnotation = hasPropertyLevelAnnotation || declaration.hasAnnotation(annotation)
        if (declaration.classId == SharedReferences.poolClassId) {
            poolClass = declaration
        }
        return super.visitClassNew(declaration)
    }

    private val functionSetAccessors = mutableMapOf<IrFunction, MutableSet<IrProperty>>()

    override fun visitReturn(expression: IrReturn): IrExpression {
        val currentFunction = this.currentFunction!!.irElement as IrFunction

        val setProperties = functionSetAccessors[currentFunction] ?: return super.visitReturn(expression)

        return DeclarationIrBuilder(context, currentFunction.symbol).irBlock {
            for (property in setProperties) {
                val classReference = property.getAnnotation(annotation).valueArguments.first() as IrClassReference
                if (!SharedReferences.generatedPoolFields.contains(classReference)) {
                    generateNewPoolProperty(classReference)
                }
                +generateVerificationFunctionCall(
                    classReference, irGetField(
                        irGet(currentFunction.dispatchReceiverParameter!!),
                        property.backingField!!,
                        property.backingField!!.type
                    )
                )
            }
            +super.visitReturn(expression)
        }
    }

    private fun IrBuilderWithScope.generateVerificationFunctionCall(
        classReference: IrClassReference,
        argument: IrExpression,
    ): IrFunctionAccessExpression {
        val classId = (classReference.symbol.owner as IrClass).classId!!
        val function =
            this@InvariantCallTransformer.context.referenceFunctions(CallableId(classId, Name.identifier("verify")))
                .single()
        return irCall(function).also {
            it.dispatchReceiver = irGetField(
                irGetObject(poolClass.symbol),
                poolClass.properties.find { field -> field.name == classReference.variableIdentifier() }!!.backingField!!
            )
            it.putValueArgument(0, argument)
        }
    }

    private fun IrClassReference.variableIdentifier(): Name {
        return Name.identifier((symbol.owner as IrClass).name.asString().lowercase())
    }

    private fun generateNewPoolProperty(classReference: IrClassReference) {
        poolClass.addProperty {
            name = classReference.variableIdentifier()
            modality = Modality.FINAL
        }.also {
            it.backingField = context.irFactory.buildField {
                isFinal = true
                name = classReference.variableIdentifier()
                type = classReference.classType
            }.also { field ->
                field.correspondingPropertySymbol = it.symbol
                field.parent = poolClass
                field.initializer = DeclarationIrBuilder(context, field.symbol).irExprBody(
                    DeclarationIrBuilder(
                        context,
                        field.symbol
                    ).irCallConstructor(
                        (classReference.symbol.owner as IrClass).primaryConstructor!!.symbol,
                        emptyList()
                    )
                )
            }
        }
        SharedReferences.generatedPoolFields.add(classReference)
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        val irFunction = expression.symbol.owner
        if (irFunction.isPropertyAccessor && irFunction.isSetter && currentFunction != null) {
            val property = irFunction.propertyIfAccessor as IrProperty
            if (property.hasAnnotation(annotation) || property.parentAsClass.hasAnnotation(annotation)) {
                val currentFunction: IrFunction = currentFunction!!.irElement as IrFunction
                if (functionSetAccessors[currentFunction] == null) {
                    functionSetAccessors[currentFunction] = mutableSetOf()
                }
                functionSetAccessors[currentFunction]!!.add(property)
            }
        }
        return super.visitFunctionAccess(expression)
    }

    /**
     * Injects a function call at the end of the function body
     */
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        // function has function level annotation -> verify function parameter
        if (declaration.hasAnnotation(annotation)) {
            val numberOfValueParams = declaration.valueParameters.size
            if (numberOfValueParams < 1 || numberOfValueParams > 15) {
                throw CompilationException(
                    "@Holds annotation for functions with $numberOfValueParams is not supported",
                    currentFile,
                    declaration
                )
            }
            // use "." for children of sealed classes, that are !directly! nested
            val classToUse =
                context.referenceClass(ClassId.fromString("social/xperience/common/FunctionVerifierClass.FunctionVerifier$numberOfValueParams"))!!
            val classReference = declaration.getAnnotation(annotation).valueArguments.first() as IrClassReference
            if (!SharedReferences.generatedPoolFields.contains(classReference)) {
                generateNewPoolProperty(classReference)
            }
            (declaration.body!!.statements as MutableList).add(
                0,
                DeclarationIrBuilder(context, declaration.symbol).irBlock {
                    +generateVerificationFunctionCall(
                        classReference,
                        irCallConstructor(
                            classToUse.owner.primaryConstructor!!.symbol,
                            declaration.valueParameters.map { it.type }
                        ).also {
                            it.putValueArgument(0, irGet(declaration.valueParameters.first()))
                        }
                    )
                })
        }
        // if absolutely no property or class level annotation were found, we can simply exit for now
        // when introducing function and local variable annotations that will become obsolete
        if (!hasPropertyLevelAnnotation) {
            return super.visitFunctionNew(declaration)
        }
        // We currently don't care about function property accessor, constructor and fake overrides.
        // Fake overrides are functions of parents that we don't explicitly override
        if (declaration is IrConstructor || declaration.isPropertyAccessor || declaration.isFakeOverride) {
            return super.visitFunctionNew(declaration)
        }

        val body = declaration.body ?: return super.visitFunctionNew(declaration)

        // an additional irCall is needed at the end, as ()V functions have no explicit IrReturn
        if (body.statements.last() !is IrReturn) {
            // why does something like this work?
            // deliberately, cast body.statement list to mutable, to add irReturnUnit statement
            (body.statements as MutableList).add(DeclarationIrBuilder(context, declaration.symbol).irReturnUnit())
        }
        return super.visitFunctionNew(declaration)
    }
}