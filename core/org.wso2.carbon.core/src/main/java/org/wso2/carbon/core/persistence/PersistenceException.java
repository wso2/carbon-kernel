package org.wso2.carbon.core.persistence;

/**
 * this gets thrown when there's exceptions in service metadata persistence
 */
public class PersistenceException extends Exception {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
