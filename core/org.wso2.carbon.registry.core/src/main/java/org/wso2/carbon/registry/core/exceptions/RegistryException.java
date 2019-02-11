/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Base Class for capturing any type of exception that occurs when using the Registry APIs.
 */
public class RegistryException extends org.wso2.carbon.registry.api.RegistryException {

    /**
     * {@inheritDoc}
     */
    public RegistryException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
