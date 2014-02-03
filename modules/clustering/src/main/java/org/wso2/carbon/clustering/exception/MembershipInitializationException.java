package org.wso2.carbon.clustering.exception;

public class MembershipInitializationException extends Exception {
    public MembershipInitializationException(String message) {
        super(message);
    }

    public MembershipInitializationException(String message, Exception e) {
        super(message, e);
    }

    public MembershipInitializationException(Exception e) {
        super(e);
    }
}
