package org.wso2.carbon.base.exception;

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
