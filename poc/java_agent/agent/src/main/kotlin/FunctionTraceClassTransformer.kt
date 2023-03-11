import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.slf4j.LoggerFactory
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

class FunctionTraceClassTransformer(
    private val targetClassName: String,
    private val targetMethodName: List<String>,
    private val targetClassLoader: ClassLoader,
) : ClassFileTransformer {
    override fun transform(
        loader: ClassLoader,
        className: String,
        classBeingRedefined: Class<*>,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray,
    ): ByteArray {
        val finalTargetClassName = targetClassName.replace(".", "/")
        var byteCode = classfileBuffer
        if (className != finalTargetClassName) {
            return byteCode
        }

        if (loader == targetClassLoader) {
            val cp = ClassPool.getDefault()
            val cc = cp[targetClassName]
            try {
                targetMethodName.forEach {
                    val method = cc.getDeclaredMethod(it)
                    transformMethod(method)
                }
                byteCode = cc.toBytecode()
                cc.detach()
            } catch (t: Throwable) {
                println(t)
            }
        }

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