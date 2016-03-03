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
package org.wso2.carbon.multitenancy.exception;

/**
 * The exception class used by TenantStore implementation during any error situation while trying the save, load tenants
 * from the tenant store.
 *
 * @since 1.0.0
 */
public class TenantStoreException extends Exception {
    public TenantStoreException(String message) {
        super(message);
    }

    public TenantStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
