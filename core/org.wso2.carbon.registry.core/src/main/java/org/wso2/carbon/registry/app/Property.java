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

package org.wso2.carbon.registry.app;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElementWrapper;

import javax.xml.namespace.QName;

/**
 * Implementation of an {@link org.apache.abdera.model.ExtensibleElement} for a property.
 */
public class Property extends ExtensibleElementWrapper {

    /**
     * Creates a property extensible element using the given internal element.
     *
     * @param internal the internal element.
     */
    public Property(Element internal) {
        super(internal);
    }

    /**
     * Creates a property extensible element using the given factory and the qualified name.
     *
     * @param factory the factory
     * @param qName   the qualified name.
     */
    public Property(Factory factory, QName qName) {
        super(factory, qName);
    }

    /**
     * Method to set a property name as an extension.
     *
     * @param propertyName the property name to set.
     *
     * @see PropertyName
     */
    public void addName(PropertyName propertyName) {
        addExtension(propertyName);
    }

    /**
     * Method to set a property value as an extension.
     *
     * @param propertyValue the property value to set.
     *
     * @see PropertyValue
     */
    public void addValue(PropertyValue propertyValue) {
        addExtension(propertyValue);
    }
}
