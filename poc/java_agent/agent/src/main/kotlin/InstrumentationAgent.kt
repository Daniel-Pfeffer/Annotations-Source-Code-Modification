import java.lang.instrument.Instrumentation
import java.lang.reflect.Method

object InstrumentationAgent {
    val annotation = "social.xperience.FunctionTrace.Trace"

    @JvmStatic
    fun premain(agentArgs: String, inst: Instrumentation) {
        // TODO: fix issues where annotation premain crashes with core dump, ClassNotFound: kotlin.jvm.internals.Intrinsics
        println("[Agent] In premain")
        transformClass(annotation, inst)
    }

    @JvmStatic
    fun agentmain(agentArgs: String, inst: Instrumentation) {
        println("[Agent] In agentmain")
        transformClass(annotation, inst)
    }

    @JvmStatic
    fun transformClass(annotationName: String, instrumentation: Instrumentation) {
        val target = with(Class.forName(annotationName)) {
            if (isAnnotation) {
                this as Class<out Annotation>
            } else {
                throw IllegalArgumentException()
            }
        }
        instrumentation.allLoadedClasses.forEach {

            it.declaredMethods.forEach { method ->
                if (method.isAnnotationPresent(target)) {
                    transform(method, it, it.classLoader, instrumentation)
                }
            }
        }
    }

    @JvmStatic
    fun transform(method: Method, clazz: Class<*>, classLoader: ClassLoader, instrumentation: Instrumentation) {
        val transformer = FunctionTraceClassTransformer(clazz.name, method.name, classLoader)
        instrumentation.addTransformer(transformer, true)
        instrumentation.retransformClasses(clazz)
    }
}