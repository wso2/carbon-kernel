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
import org.apache.abdera.model.ElementWrapper;

import javax.xml.namespace.QName;

/**
 * Implementation of an {@link org.apache.abdera.model.ExtensibleElement} for a property name.
 */
@SuppressWarnings("unused")
public class PropertyName extends ElementWrapper {

    /**
     * Creates a property name extensible element using the given internal element.
     *
     * @param internal the internal element.
     */
    public PropertyName(Element internal) {
        super(internal);
    }

    /**
     * Creates a property name extensible element using the given factory and the qualified name.
     *
     * @param factory the factory
     * @param qName   the qualified name.
     */
    public PropertyName(Factory factory, QName qName) {
        super(factory, qName);
    }

    /**
     * Method to obtain the property name.
     *
     * @return the property name.
     */
    public String getPropertyName() {
        return getText();
    }

    /**
     * Method to set the property name.
     *
     * @param propertyName the property name.
     */
    public void setPropertyName(String propertyName) {
        setText(propertyName);
    }

}
