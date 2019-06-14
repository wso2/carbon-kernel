/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.nextgen.config.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model object for parse values within phases.
 */
public class Context {


    private Map<String, Object> templateData;
    private Map<String, String> resolvedSystemProperties;
    private Map<String, String> resolvedEnvironmentVariables;
    private Map<String, String> secrets;

    public Context() {

        this.templateData = new HashMap<>();
        this.resolvedEnvironmentVariables = new HashMap<>();
        this.resolvedSystemProperties = new HashMap<>();
        this.secrets = new HashMap<>();
    }



    public Map<String, Object> getTemplateData() {

        return templateData;
    }

    public Map<String, String> getResolvedSystemProperties() {

        return resolvedSystemProperties;
    }

    public Map<String, String> getResolvedEnvironmentVariables() {

        return resolvedEnvironmentVariables;
    }

    public Map<String, String> getSecrets() {

        return secrets;
    }
}
