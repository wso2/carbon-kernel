package org.wso2.carbon.core.persistence;

/**
 * This is thrown when a requested resource or parent of the request can not be found.
 */
public class PersistenceDataNotFoundException extends PersistenceException {

    /**
     * Constructs a new exception for a resource not found for the given service group.
     *
     * @param message the give path at which the resource was not found.
     */
    public PersistenceDataNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the give path at which the resource was not found.
     * @param cause   the cause of this exception.
     */
    public PersistenceDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }


}
