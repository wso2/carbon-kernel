package ${package};

import java.util.logging.Logger;

/**
 * This class implements the Greeter interface.
 */
public class GreeterImpl implements Greeter {
    Logger logger = Logger.getLogger(GreeterImpl.class.getName());

    private String name;

    public GreeterImpl(String name) {
        this.name = name;
    }

    /**
     * Output an info log saying Hello.
     */
    public void sayHello() {
        logger.info("Hello" + name);
    }

    /**
     * Outputs an info log saying Goodbye
     */
    public void sayGoodBye() {
        logger.info("GoodBye " + name);
    }
}
