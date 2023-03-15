import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.security.ProtectionDomain

class FunctionTraceClassTransformerKotlin(
    private val annotation: Class<out Annotation>,
) : ClassFileTransformer {

    @Throws(IllegalClassFormatException::class)
    override fun transform(
        module: Module,
        loader: ClassLoader,
        className: String,
        classBeingRedefined: Class<*>,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray,
    ): ByteArray {
        return transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
    }

    @Throws(IllegalClassFormatException::class)
    override fun transform(
        loader: ClassLoader,
        className: String,
        classBeingRedefined: Class<*>,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray,
    ): ByteArray {
        println("[Agent] Called transform for $className")
        var byteCode = classfileBuffer
        var counter = 0
        val cp = ClassPool.getDefault()
        val cc = cp.get(className.replace("/", "."))

        cc.declaredMethods.forEach {
            if (it.hasAnnotation(annotation)) {
                counter++
                try {
                    transformMethod(it)
                } catch (e: Throwable) {
                    println(e)
                }
            }
        }
        // check if any transformation occurred, if not, we don't have to compile new, thus saving start-up runtime
        if (counter > 0) {
            byteCode = cc.toBytecode()
        }
        cc.detach()

        return byteCode
    }

    private fun transformMethod(method: CtMethod) {
        method.addLocalVariable("starttime", CtClass.longType)
        val stringBuilder = StringBuilder()
        stringBuilder.append("System.out.println(\"Enter ${method.name}()\");")
        stringBuilder.append("starttime = System.currentTimeMillis();")
        method.insertBefore(stringBuilder.toString())

        method.addLocalVariable("endtime", CtClass.longType)
        val str2 = StringBuilder()
        str2.append("endtime = System.currentTimeMillis();")
        str2.append("System.out.println(\"Exit ${method.name}() after \"+ (endtime - starttime) +\"ms.\");")
        method.insertAfter(str2.toString())
    }
}