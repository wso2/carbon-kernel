package org.wso2.carbon.base.exception;

/**
 * Class for capturing any type of exception that occurs when initializing configuration
 */
public class ConfigurationInitializationException extends Exception {
    public ConfigurationInitializationException(String message) {
        super(message);
    }

    public ConfigurationInitializationException(String message, Exception e) {
        super(message, e);
    }

    public ConfigurationInitializationException(Exception e) {
        super(e);
    }
}
