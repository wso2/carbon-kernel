/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core;

/**
 * The exception to throw when the Circuit Breaker triggers for user stores.
 */
public class CircuitBreakerException extends UserStoreException {

    public CircuitBreakerException(String message, Throwable cause) {

        super(message, cause);
    }

    public CircuitBreakerException(String message, String errorCode, Throwable cause) {

        super(message, errorCode, cause);
    }

    public CircuitBreakerException(String message, String errorCode) {

        super(message, errorCode);
    }

    public CircuitBreakerException(String message) {

        super(message);
    }

    public CircuitBreakerException(Throwable cause) {

        super(cause);
    }
}
