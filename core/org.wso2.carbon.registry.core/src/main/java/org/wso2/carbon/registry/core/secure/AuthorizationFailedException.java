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

package org.wso2.carbon.registry.core.secure;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Class for capturing exception due to inadequate authorizations to proceed with various registry
 * operations and related utility methods.
 */
public class AuthorizationFailedException extends RegistryException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public AuthorizationFailedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of this exception.
     */
    @SuppressWarnings("unused")
    public AuthorizationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
