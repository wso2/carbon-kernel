package org.wso2.ei.config;

/**
 * Exception to be thrown in case of a validation failure.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {

        super(message);
    }

    public ValidationException(String message, Throwable cause) {

        super(message, cause);
    }
}
