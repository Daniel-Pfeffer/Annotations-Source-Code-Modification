package social.xperience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class InstrumentationAgent {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentationAgent.class);

    public static void premain(String args, Instrumentation instrumentation) {
        logger.info("[AGENT] In premain");
        registerTransformer(instrumentation);
    }

    private static void registerTransformer(Instrumentation instrumentation) {
        instrumentation.addTransformer(new InvariantsClassTransformer());
    }
}
