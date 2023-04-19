package social.xperience

import javassist.ClassPool
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.Modifier
import javassist.bytecode.LocalVariableAttribute
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass

class RealTransformer(className: String) {
    private val classPool = ClassPool.getDefault()
    private val ctClass = classPool.get(className.replace("/", "."))
    private var classVerifier: KClass<out Verification<*>>? = null

    private val annotatedFields: MutableMap<CtField, KClass<out Verification<*>>> = mutableMapOf()
    private val annotatedFieldsWithSetter: MutableMap<CtField, KClass<out Verification<*>>> = mutableMapOf()

    private val createdVerificationFields: MutableSet<String> = mutableSetOf()

    private var hasChanges = false

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RealTransformer::class.java)
    }

    fun visit() {
        val annotation: Holds? = ctClass.getAnnotation(Commons.annotation.java) as Holds?
        if (annotation != null) {
            logger.info("Found class annotation in class ${ctClass.name}")
            classVerifier = annotation.verifier
            visitAnnotation(annotation)
        }
        // look through fields to find potential checks for constructor
        ctClass.declaredFields.forEach {
            visitProperty(it)
        }
        ctClass.declaredConstructors.forEach {
            visitConstructor(it)
        }
        ctClass.declaredMethods.forEach {
            visitMethod(it)
        }
    }

    fun shouldCompile(): Boolean = classVerifier != null || annotatedFields.isNotEmpty() || hasChanges


    fun compile(): ByteArray {
        ctClass.writeFile("generated")
        return ctClass.toBytecode()
    }

    private fun visitProperty(field: CtField) {
        val annotation: Holds = field.getPropertyAnnotation(Commons.annotation) as Holds? ?: return
        logger.info("Found property annotation in class ${ctClass.name}#${field.name}")
        visitAnnotation(annotation)
        annotatedFields[field] = annotation.verifier
        if (!field.hasSetter()) {
            return
        }
        annotatedFieldsWithSetter[field] = annotation.verifier
    }

    private fun visitConstructor(ctConstructor: CtConstructor) {
        val annotation = ctConstructor.getAnnotation(Commons.annotation.java) as Holds?
        if (annotation != null) {
            logger.info("Found constructor annotation in class ${ctClass.name}#${ctConstructor.name}")
            visitAnnotation(annotation)
        }
        if (classVerifier != null) {
            ctConstructor.insertAfter(
                "${ctClass.name}.${classVerifier!!.simpleName!!.lowercase()}.verify(this);"
            )
        }
        annotatedFields.forEach { (field, verifier) ->
            ctConstructor.insertAfter(
                "${ctClass.name}.${verifier.simpleName!!.lowercase()}.verify(this.${field.name});"
            )
        }
        if (annotation != null) {
            ctConstructor.insertAfter(
                "${ctClass.name}.${annotation.verifier.simpleName!!.lowercase()}.verify(this);"
            )
        }
    }

    private fun visitMethod(ctMethod: CtMethod) {
        // kotlin creates static functions for annotations for property annotation called ${propertyName}$annotations
        if (ctMethod.name.endsWith("\$annotations")) {
            return
        }
        val annotation = ctMethod.getAnnotation(Commons.annotation.java) as Holds? ?: return
        logger.info("Found method annotation in class ${ctClass.name}#${ctMethod.name}")
        visitAnnotation(annotation)
        hasChanges = true
        val paramSize = ctMethod.parameterTypes.size
        val params =
            (ctMethod.methodInfo.codeAttribute.getAttribute(LocalVariableAttribute.tag) as LocalVariableAttribute)

        val functionVerifierParam = mutableListOf<String>()

        repeat(paramSize) {
            functionVerifierParam += params.variableName(it)
        }

        val x =
            "${ctClass.name}.${annotation.verifier.simpleName!!.lowercase()}.verify(new social.xperience.common.FunctionVerifierClass.FunctionVerifier$paramSize(${
                functionVerifierParam.joinToString(
                    ", "
                )
            }));"

        ctMethod.insertBefore(x)
    }


    /**
     * Creates a Verification pool field
     */
    private fun visitAnnotation(annotation: Holds) {
        val name = annotation.verifier.simpleName!!.lowercase()
        if (!createdVerificationFields.contains(name)) {
            val verifierClassName = annotation.verifier.qualifiedName
            val field = CtField(classPool.get(verifierClassName), name, ctClass)
            field.modifiers = Modifier.STATIC
            ctClass.addField(field, CtField.Initializer.byNew(classPool.get(verifierClassName)))
        }
    }

    private fun CtField.hasSetter(): Boolean {
        return declaringClass.declaredMethods.any {
            it.name == "set${name.capitalized}"
        }
    }

    private val String.capitalized: String
        get() = replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }


    private fun CtField.setter(): CtMethod {
        return declaringClass.getDeclaredMethod("set${name.capitalized}}")
    }

    private fun CtField.getPropertyAnnotation(annotation: KClass<*>): Annotation? {
        return declaringClass.declaredBehaviors.firstOrNull { behaviour ->
            behaviour.hasAnnotation(annotation.java) && behaviour.name.startsWith(
                "get${name.capitalized}"
            )
        }?.getAnnotation(annotation.java) as Annotation?
    }
}