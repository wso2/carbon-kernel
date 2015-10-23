/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.deployment.exception;

/**
 * The exception class for all deployer registration, un-registration related the exceptions that
 * can be thrown from CarbonDeployementEngine.
 */

public class DeployerRegistrationException extends Exception {
    /**
     * This will construct the DeployerRegistrationException with the detailed exception message.
     *
     * @param message the detailed exception message to be included with DeployerRegistrationException
     */
    public DeployerRegistrationException(String message) {
        super(message);
    }

    /**
     * This will construct a new DeployerRegistrationException with the specified detail message and
     * cause.
     *
     * @param message the exception message to be included with DeployerRegistrationException
     * @param cause the cause exception to be included with DeployerRegistrationException
     */
    public DeployerRegistrationException(String message, Exception cause) {
        super(message, cause);
    }
}
