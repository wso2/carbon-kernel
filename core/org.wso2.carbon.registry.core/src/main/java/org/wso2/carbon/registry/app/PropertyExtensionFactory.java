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

import org.apache.abdera.util.AbstractExtensionFactory;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.xml.namespace.QName;

/**
 * Implementation for an extension factory for properties. For simplicity, the tags are treated as
 * property extension elements as well.
 */
public class PropertyExtensionFactory extends AbstractExtensionFactory {

    /**
     * Qualified name for a property name element.
     */
    public static final QName PROPERTY_NAME =
            new QName(RegistryConstants.REGISTRY_NAMESPACE, "name", "ns");

    /**
     * Qualified name for a property value element.
     */
    public static final QName PROPERTY_VALUE =
            new QName(RegistryConstants.REGISTRY_NAMESPACE, "value", "ns");

    /**
     * Qualified name for a properties element.
     */
    public static final QName PROPERTIES =
            new QName(RegistryConstants.REGISTRY_NAMESPACE, "properties", "ns");

    /**
     * Qualified name for a property element.
     */
    public static final QName PROPERTY =
            new QName(RegistryConstants.REGISTRY_NAMESPACE, "property", "ns");

    /**
     * Qualified name for a tags element. This is represented by the properties class.
     */
    public static final QName TAGS = new QName(RegistryConstants.REGISTRY_NAMESPACE, "tags", "ns");

    /**
     * Qualified name for a tag element. This is represented by the property class.
     */
    public static final QName TAG = new QName(RegistryConstants.REGISTRY_NAMESPACE, "tag", "ns");

    /**
     * Default constructor.
     */
    public PropertyExtensionFactory() {
        super(RegistryConstants.REGISTRY_NAMESPACE);
        addImpl(PROPERTY_NAME, PropertyName.class);
        addImpl(PROPERTY_VALUE, PropertyValue.class);
        addImpl(PROPERTIES, Properties.class);
        addImpl(TAGS, Properties.class);
        addImpl(PROPERTY, Property.class);
        addImpl(TAG, Property.class);
    }

}
