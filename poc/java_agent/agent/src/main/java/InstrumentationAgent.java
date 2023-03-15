import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class InstrumentationAgent {
    private static String annotation = "social.xperience.FunctionTrace.Trace";

    public static void premain(String args, Instrumentation inst) {
        System.out.println("[Agent] In premain");
        try {
            transformClass(annotation, inst);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }
    }

    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("[Agent] In agentmain");
        try {
            transformClass(annotation, inst);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }
    }

    private static void transformClass(String annotationName, Instrumentation instrumentation) throws ClassNotFoundException, UnmodifiableClassException {
        Class<Annotation> target;
        Class<?> annotation = Class.forName(annotationName);
        if (annotation.isAnnotation()) {
            target = (Class<Annotation>) annotation;
        } else {
            throw new IllegalArgumentException("Fuck off");
        }
        // this would only transform newly loaded classes, if used with premain that shouldn't be a problem,
        // as long as no System classes will be transformed
        // If we want to transform already loaded class we would have
        // to do that explicitly with ```instrumentation.retransformClasses()```
        instrumentation.addTransformer(new FunctionTraceClassTransformer(target), true);
        // re-transforms loaded classes if re-transform is supported
        if (instrumentation.isRetransformClassesSupported()) {
            for (Class loadedClass : instrumentation.getAllLoadedClasses()) {
                if (instrumentation.isModifiableClass(loadedClass)) {
                    instrumentation.retransformClasses(loadedClass);
                }
            }
        }
        System.out.println("[Agent] finished potentially transforming all loaded classes");
    }
}
