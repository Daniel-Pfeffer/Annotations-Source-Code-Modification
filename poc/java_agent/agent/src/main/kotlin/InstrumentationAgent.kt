import java.lang.instrument.Instrumentation
import java.lang.reflect.Method

object InstrumentationAgent {
    val annotation = "social.xperience.FunctionTrace.Trace"

    @JvmStatic
    fun premain(agentArgs: String?, inst: Instrumentation) {
        println("[Agent] In premain")
        transformClass(annotation, inst)
    }

    @JvmStatic
    fun agentmain(agentArgs: String?, inst: Instrumentation) {
        println("[Agent] In agentmain")
        transformClass(annotation, inst)
    }

    @JvmStatic
    fun transformClass(annotationName: String?, instrumentation: Instrumentation) {
        val target = with(Class.forName(annotationName)) {
            if (isAnnotation) {
                this as Class<out Annotation>
            } else {
                throw IllegalArgumentException()
            }
        }
        // Class has to be manually loaded.
        // Don't know how to avoid that yet
        // TODO: research how to access dynamically
        val onlyClass = Class.forName("social.xperience.sample.MainKt")
        instrumentation.allLoadedClasses.forEach { clazz ->
            with(clazz.declaredMethods.filter { it.isAnnotationPresent(target) }) {
                if (isNotEmpty()) {
                    transform(
                        map { it.name },
                        clazz,
                        clazz.classLoader,
                        instrumentation
                    )
                }
            }
        }
    }

    @JvmStatic
    fun transform(methods: List<String>, clazz: Class<*>, classLoader: ClassLoader, instrumentation: Instrumentation) {
        val transformer = FunctionTraceClassTransformer(clazz.name, methods, classLoader)
        instrumentation.addTransformer(transformer, true)
        instrumentation.retransformClasses(clazz)
    }
}