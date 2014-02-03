package org.wso2.carbon.clustering.exception;

public class MembershipFailedException extends Exception {
    public MembershipFailedException(String message) {
        super(message);
    }

    public MembershipFailedException(String message, Exception e) {
        super(message, e);
    }

    public MembershipFailedException(Exception e) {
        super(e);
    }
}
