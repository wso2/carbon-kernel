package org.wso2.carbon.http.client.exception;

/**
 * Base Class for capturing any type of exception that occurs when using the HTTP Client.
 */
public class HttpClientException extends Exception {
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public HttpClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of this exception.
     */
    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
