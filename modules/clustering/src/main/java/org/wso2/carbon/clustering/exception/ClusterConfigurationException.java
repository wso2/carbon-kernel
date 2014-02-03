package org.wso2.carbon.clustering.exception;

public class ClusterConfigurationException extends Exception {
    public ClusterConfigurationException(String message) {
        super (message);
    }

    public ClusterConfigurationException(String message, Exception e) {
        super (message, e);
    }

    public ClusterConfigurationException(Exception e) {
        super (e);
    }
}
