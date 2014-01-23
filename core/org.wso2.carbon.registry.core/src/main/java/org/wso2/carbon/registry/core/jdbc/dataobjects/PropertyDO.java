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

package org.wso2.carbon.registry.core.jdbc.dataobjects;

/**
 * The data object maps with a property
 */
@Deprecated
public class PropertyDO {

    private String name;
    private String value;

    /**
     * Method to get the property name.
     *
     * @return the property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the property name.
     *
     * @param name the property name to be set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to get the property value
     *
     * @return the property value
     */
    public String getValue() {
        return value;
    }

    /**
     * Method to set the property value
     *
     * @param value the property value to be set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
