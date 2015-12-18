package org.test.component;

import java.util.logging.Logger;

/**
 * This class implements the Greeter interface.
 *
 * @since 1.0.0
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
    public void hello() {
        logger.info("Hello" + name);
    }

    /**
     * Outputs an info log saying bye.
     */
    public void bye() {
        logger.info("Bye " + name);
    }
}
