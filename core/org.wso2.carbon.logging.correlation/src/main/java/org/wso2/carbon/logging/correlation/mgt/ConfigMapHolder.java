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

package org.wso2.carbon.logging.correlation.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.correlation.ConfigObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Correlation log configuration holder.
 */
public class ConfigMapHolder {
    private static Log log = LogFactory.getLog(ConfigMapHolder.class);
    private static ConfigMapHolder instance = new ConfigMapHolder();

    private ConfigObserver observer = null;
    private Map<String, Map<String, Object>> componentConfigMap;

    private ConfigMapHolder() {
        componentConfigMap = new HashMap<>();
    }

    /**
     * Get the singleton instance of the ConfigMapHolder class.
     * @return
     */
    public static ConfigMapHolder getInstance() {
        return instance;
    }

    /**
     * Registers an observer to be invoked when the config is changed.
     *
     * @param observer
     */
    public void onConfigUpdated(ConfigObserver observer) {
        this.observer = observer;
    }

    /**
     * Get complete config map for a component.
     *
     * @param component Component name
     * @return
     */
    public Map<String, Object> getConfigMap(String component) {
        return componentConfigMap.get(getComponentName(component));
    }

    /**
     * Get component specific configuration by key.
     *
     * @param component Component name
     * @param key Config name
     * @return
     */
    public Object getConfig(String component, String key) {
        Map<String, Object> map = componentConfigMap.get(getComponentName(component));
        if (map != null) {
            return map.get(key);
        }
        return null;
    }

    /**
     * Set config in the map of map.
     *
     * @param component Component name
     * @param key Config name
     * @param value Value
     */
    public void setConfig(String component, String key, Object value) {
        String componentName = getComponentName(component);
        if (componentConfigMap.get(componentName) == null) {
            componentConfigMap.put(componentName, new HashMap<>());
        }
        componentConfigMap.get(componentName).put(key, value);
        if (observer != null) {
            observer.configUpdated(componentName, key, value);
        }
    }

    /**
     * If the component is null, return it as "root". Else the component name will not be changed.
     *
     * @param componentName
     * @return
     */
    private String getComponentName(String componentName) {
        if (componentName != null) {
            return componentName;
        }
        return "root";
    }
}
