package org.wso2.carbon.clustering.exception;


public class MessageFailedException extends Exception {

    public MessageFailedException(String message) {
        super (message);
    }

    public MessageFailedException(String message, Exception e) {
        super (message, e);
    }

    public MessageFailedException(Exception e) {
        super (e);
    }
}
