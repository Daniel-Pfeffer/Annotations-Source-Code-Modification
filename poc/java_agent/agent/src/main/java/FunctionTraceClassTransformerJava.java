import javassist.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class FunctionTraceClassTransformerJava implements ClassFileTransformer {
    private Class<Annotation> annotationClass;

    public FunctionTraceClassTransformerJava(Class<Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("[Agent] Called transform for " + className);
        byte[] byteCode = classfileBuffer;
        int counter = 0;
        ClassPool cp = ClassPool.getDefault();
        // can only read stuff
        try {
            CtClass cc = cp.get(className.replace("/", "."));
            for (CtMethod declaredMethod : cc.getDeclaredMethods()) {
                if (declaredMethod.hasAnnotation(annotationClass)) {
                    counter++;
                    transformMethod(declaredMethod);
                }
            }
            if (counter > 0) {
                byteCode = cc.toBytecode();
                cc.detach();
            }
        } catch (NotFoundException | IOException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return byteCode;
    }

    private void transformMethod(CtMethod declaredMethod) throws CannotCompileException {
        declaredMethod.addLocalVariable("starttime", CtClass.longType);
        StringBuilder sb = new StringBuilder();
        sb.append("System.out.println(\"Enter ").append(declaredMethod.getName()).append("()\");");
        sb.append("starttime = System.currentTimeMillis();");
        declaredMethod.insertBefore(sb.toString());

        declaredMethod.addLocalVariable("endtime", CtClass.longType);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("endtime = System.currentTimeMillis();");
        sb2.append("System.out.println(\"Exit ").append(declaredMethod.getName()).append("() after \"+ (endtime - starttime) +\"ms.\");");
        declaredMethod.insertAfter(sb2.toString());
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }
}
