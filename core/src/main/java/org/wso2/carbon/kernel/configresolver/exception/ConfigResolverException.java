package org.wso2.carbon.kernel.configresolver.exception;

/**
 * Created by jayanga on 8/16/16.
 */
public class ConfigResolverException extends Exception {
    public ConfigResolverException(String message) {
        super(message);
    }

    public ConfigResolverException(String message, Throwable cause) {
        super(message, cause);
    }
}
