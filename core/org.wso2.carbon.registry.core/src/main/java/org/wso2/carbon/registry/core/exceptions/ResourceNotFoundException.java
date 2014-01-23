/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.core.exceptions;

/**
 * This is thrown when a requested resource cannot be located in the Registry.
 */
public class ResourceNotFoundException extends RegistryException {

    /**
     * Constructs a new exception for a resource not found in the given path.
     *
     * @param path the give path at which the resource was not found.
     */
    public ResourceNotFoundException(String path) {
        super("Resource does not exist at path " + path);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param path  the give path at which the resource was not found.
     * @param cause the cause of this exception.
     */
    public ResourceNotFoundException(String path, Throwable cause) {
        super("Resource does not exist at path " + path, cause);
    }
}
