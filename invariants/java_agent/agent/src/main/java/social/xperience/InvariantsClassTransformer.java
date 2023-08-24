package social.xperience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class InvariantsClassTransformer implements ClassFileTransformer {
    private static final Logger logger = LoggerFactory.getLogger(InvariantsClassTransformer.class);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        long start = System.currentTimeMillis();
        long end;
        RealTransformer transformer = new RealTransformer(className);
        try {
            transformer.visit();
        } catch (Throwable t) {
            logger.error(t.getMessage());
        }
        if (transformer.shouldCompile()) {
            byte[] compiled = transformer.toBytecode();
            end = System.currentTimeMillis();
            logger.info("############## Time difference {} in class {}", end - start, className);
            return compiled;
        }
        return ClassFileTransformer.super.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }
}
