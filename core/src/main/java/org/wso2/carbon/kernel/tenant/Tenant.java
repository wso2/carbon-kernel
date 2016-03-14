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
package org.wso2.carbon.kernel.tenant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The entity which represent the concept of a tenant in the Carbon runtime.
 *
 * @since 5.1.0
 */
public class Tenant {

    private final String domain;
    private Map<String, Object> properties = new HashMap<>();


    /**
     * The constructor which takes the domain name as the argument to construct the tenant object.
     *
     * @param domain value of the tenant domain for this instance.
     */
    public Tenant(String domain) {
        this.domain = domain;
    }

    /**
     * Returns the domain of the tenant.
     *
     * @return the tenant domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the value of the attribute <code>key</code>.
     *
     * @param key the attribute key
     * @return value of the specified attribute
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Returns attributes of the tenant.
     *
     * @return a map of tenant attributes
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Sets a tenant attribute
     *
     * @param key   the attribute name
     * @param value the attribute value
     */
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    /**
     * Sets a map of the tenant attributes.
     *
     * @param props the maps of tenant attributes
     */
    public void setProperties(Map<String, Object> props) {
        this.properties = props;
    }

}
