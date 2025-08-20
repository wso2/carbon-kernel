/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds the context for validating operation scopes.
 * It contains information about whether validation is required,
 * the list of validated scopes, the normalized resource URI,
 * and a map of operation scopes.
 */
public class OperationScopeValidationContext {

    private boolean validationRequired;
    private List<String> validatedScopes;
    private Map<String, String> operationScopeMap = new ConcurrentHashMap<>();

    public boolean isValidationRequired() {

        return validationRequired;
    }

    public void setValidationRequired(boolean validationRequired) {

        this.validationRequired = validationRequired;
    }

    public List<String> getValidatedScopes() {

        return validatedScopes;
    }

    public void setValidatedScopes(List<String> validatedScopes) {

        this.validatedScopes = validatedScopes;
    }

    public Map<String, String> getOperationScopeMap() {

        return operationScopeMap;
    }

    public void setOperationScopeMap(Map<String, String> operationScopeMap) {

        this.operationScopeMap = operationScopeMap;
    }
}
