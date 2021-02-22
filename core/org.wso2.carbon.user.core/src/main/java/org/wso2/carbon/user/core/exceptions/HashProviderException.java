/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.exceptions;

/**
 * The exception is thrown when there is a problem with HashProvider service.
 */
public class HashProviderException extends Exception {

    private static final long serialVersionUID = -6057036683816666355L;
    private String errorCode;

    public HashProviderException() {

        super();
    }

    public HashProviderException(String message, Throwable cause) {

        super(message, cause);
    }

    public HashProviderException(String message, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public HashProviderException(String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
    }

}
