package org.wso2.carbon.user.core.multiplecredentials;/*
 *   Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import org.wso2.carbon.user.core.UserStoreException;

public class MultipleCredentialsException extends UserStoreException {

    public MultipleCredentialsException() {
    }

    public MultipleCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleCredentialsException(String message, boolean convertMessage) {
        super(message, convertMessage);
    }

    public MultipleCredentialsException(String message) {
        super(message);
    }

    public MultipleCredentialsException(Throwable cause) {
        super(cause);
    }
}
