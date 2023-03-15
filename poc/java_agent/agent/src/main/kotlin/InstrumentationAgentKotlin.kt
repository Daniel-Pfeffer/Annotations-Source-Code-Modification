import java.lang.instrument.Instrumentation

object InstrumentationAgentKotlin {
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
            if (this.isAnnotation) {
                this as Class<out Annotation>
            } else {
                throw IllegalArgumentException()
            }
        }
        println(instrumentation.allLoadedClasses.filter { it.name.contains("xperience") }.map { it.name })
        // this would only transform newly loaded classes, if used with premain that shouldn't be a problem,
        // as long as no System classes will be transformed
        // If we want to transform already loaded class we would have
        // to do that explicitly with ```instrumentation.retransformClasses()```
        instrumentation.addTransformer(FunctionTraceClassTransformerKotlin(target), true)
        // re-transforms loaded classes if re-transform is supported
        if (instrumentation.isRetransformClassesSupported) {
            instrumentation.allLoadedClasses.forEach {
                if (instrumentation.isModifiableClass(it)) {
                    instrumentation.retransformClasses(it)
                }
            }
        }
        println("[Agent] finished potentially transforming all loaded classes")
    }
}