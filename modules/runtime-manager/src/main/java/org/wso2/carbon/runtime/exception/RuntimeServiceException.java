package org.wso2.carbon.runtime.exception;


/**
 * The super class for all the exception that can be thrown from RuntimeConfiguration
 *
 */

    public class RuntimeServiceException extends Exception {
        public RuntimeServiceException(String message) {
            super(message);
        }

        public RuntimeServiceException(String message, Exception e) {
            super(message, e);
        }

    }
