package org.wso2.carbon.clustering.exception;

public class ClusterInitializationException extends Exception {
    public ClusterInitializationException(String message) {
        super (message);
    }

    public ClusterInitializationException(String message, Exception e) {
        super (message, e);
    }

    public ClusterInitializationException(Exception e) {
        super (e);
    }
}
