/*
 *
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

package org.wso2.carbon.logging.correlation.bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration bean class.
 */
public class CorrelationLogConfig {
    private boolean enable;
    private String[] components;
    private String[] deniedThreads;
    private Map<String, CorrelationLogComponentConfig> componentConfigs;

    public CorrelationLogConfig() {
        this(false, null, null);
    }

    public CorrelationLogConfig(boolean enable, String[] components, String[] deniedThreads) {
        this(enable, components, deniedThreads, new HashMap<>());
    }

    public CorrelationLogConfig(boolean enable, String[] components, String[] deniedThreads,
                                Map<String, CorrelationLogComponentConfig> componentConfigs) {
        this.enable = enable;
        this.components = components;
        this.deniedThreads = deniedThreads;
        this.componentConfigs = componentConfigs;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String[] getComponents() {
        return components;
    }

    public void setComponents(String[] components) {
        this.components = components;
    }

    public String[] getDeniedThreads() {
        return deniedThreads;
    }

    public void setDeniedThreads(String[] deniedThreads) {
        this.deniedThreads = deniedThreads;
    }

    public Map<String, CorrelationLogComponentConfig> getComponentConfigs() {
        return componentConfigs;
    }

    public void setComponentConfigs(Map<String, CorrelationLogComponentConfig> componentConfigs) {
        this.componentConfigs = componentConfigs;
    }

    /**
     * Returns a clone of this instance.
     *
     * @return
     */
    public CorrelationLogConfig clone() {
        // Create a copy of the current instance.
        CorrelationLogConfig clone = new CorrelationLogConfig(
                this.enable,
                Arrays.copyOf(this.components, this.components.length),
                Arrays.copyOf(this.deniedThreads, this.deniedThreads.length));

        // Copy component configurations.
        Map<String, CorrelationLogComponentConfig> clonedComponentConfigs = new HashMap<>();
        for (Map.Entry<String, CorrelationLogComponentConfig> entry : this.componentConfigs.entrySet()) {
            clonedComponentConfigs.put(new String(entry.getKey()), entry.getValue().clone());
        }
        clone.setComponentConfigs(clonedComponentConfigs);
        return clone;
    }
}
