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
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.interpreter.getAnnotation
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
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

    private lateinit var poolClass: IrClass


    override fun visitClassNew(declaration: IrClass): IrStatement {
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
                if (property.hasAnnotation(annotation)) {
                    +generateVerificationFunctionCall(
                        property, irGetField(
                            irGet(currentFunction.dispatchReceiverParameter!!),
                            property.backingField!!,
                            property.backingField!!.type
                        )
                    )
                }
            }
            // we can use any property as they are grouped by functions, thus cannot have different parents
            val property = setProperties.firstOrNull { it.parentAsClass.hasAnnotation(annotation) }
            if (property != null) {
                val parent = property.parentAsClass
                +generateVerificationFunctionCall(
                    parent, irGet(currentFunction.dispatchReceiverParameter!!, parent.defaultType)
                )
            }
            +super.visitReturn(expression)
        }
    }

    private fun IrBuilderWithScope.generateVerificationFunctionCall(
        element: IrDeclarationBase,
        argument: IrExpression,
    ): IrFunctionAccessExpression {
        val classReference = element.getAnnotation(annotation).valueArguments.first() as IrClassReference
        if (!SharedReferences.generatedPoolFields.contains(classReference)) {
            generateNewPoolProperty(classReference)
        }
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

    /**
     * Generates a new Verification Pool property to avoid constructor calls, as Verification classes must be stateless
     */
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
                field.initializer = context.irFactory.createExpressionBody(
                    -1, -1,
                    IrConstructorCallImpl.fromSymbolOwner(
                        -1,
                        -1,
                        (classReference.symbol.owner as IrClass).primaryConstructor!!.symbol.owner.returnType,
                        (classReference.symbol.owner as IrClass).primaryConstructor!!.symbol,
                        0
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

        if (declaration is IrConstructor) {
            declaration.parentAsClass.declarations.add(generateVerificationCallForAnonymousInitializer(declaration))
        } else {
            val body = declaration.body ?: return super.visitFunctionNew(declaration)

            // an additional irCall is needed at the end, as ()V functions have no explicit IrReturn
            if (body.statements.last() !is IrReturn) {
                // why does something like this work?
                // deliberately, cast body.statement list to mutable, to add irReturnUnit statement
                (body.statements as MutableList).add(DeclarationIrBuilder(context, declaration.symbol).irReturnUnit())
            }
        }

        return super.visitFunctionNew(declaration)
    }

    private fun generateVerificationCallForAnonymousInitializer(declaration: IrConstructor): IrAnonymousInitializer {
        val clazz = declaration.parentAsClass
        val propertiesWithAnnotation = clazz.properties.filter { it.hasAnnotation(annotation) }
        return context.irFactory.createAnonymousInitializer(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            IrDeclarationOrigin.INSTANCE_RECEIVER,
            IrAnonymousInitializerSymbolImpl(),
            false
        ).apply {
            parent = clazz
            body = DeclarationIrBuilder(context, declaration.symbol).irBlockBody {
                if (clazz.hasAnnotation(annotation)) {
                    +generateVerificationFunctionCall(
                        clazz, irGet(clazz.thisReceiver!!, clazz.defaultType)
                    )
                }
                propertiesWithAnnotation.forEach {
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
    }

    private fun generateVerificationCallForFunction(declaration: IrFunction): IrExpression {
        val numberOfValueParams = declaration.valueParameters.size
        if (numberOfValueParams < 1 || numberOfValueParams > 15) {
            throw CompilationException(
                "@Holds annotation for functions with $numberOfValueParams is not supported", currentFile, declaration
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