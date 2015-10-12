/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment.exception;

/**
 * Represents the Deployment Engine related Exception
 */
public class DeploymentEngineException extends Exception {

    /**
     * This will construct the DeploymentEngineException with the detailed exception message
     *
     * @param message the detailed exception message to be included with DeploymentEngineException
     */
    public DeploymentEngineException(String message) {
        super(message);
    }

    /**
     * This will construct a new DeploymentEngineException with the specified detail message and
     * cause.
     *
     * @param message the exception message to be included with DeploymentEngineException
     * @param cause the cause exception to be included with DeploymentEngineException
     */
    public DeploymentEngineException(String message, Exception cause) {
        super(message, cause);
    }
}
