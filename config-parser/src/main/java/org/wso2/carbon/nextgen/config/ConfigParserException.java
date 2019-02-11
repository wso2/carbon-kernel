package org.wso2.carbon.nextgen.config;

/**
 * Exception to be thrown in case of a validation failure.
 */
public class ConfigParserException extends Exception {

    public ConfigParserException(String message) {

        super(message);
    }

    public ConfigParserException(String message, Throwable cause) {

        super(message, cause);
    }
}
