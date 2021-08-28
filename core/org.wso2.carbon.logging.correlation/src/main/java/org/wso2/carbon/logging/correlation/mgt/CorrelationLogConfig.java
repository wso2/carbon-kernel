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
import org.wso2.carbon.logging.correlation.CorrelationLogConfigAttribute;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ReflectionException;

/**
 * Correlatin log dynamic MBean class.
 */
public class CorrelationLogConfig implements DynamicMBean {
    private static Log log = LogFactory.getLog(CorrelationLogConfig.class);

    private String componentName;
    private CorrelationLogConfigAttribute[] attributes;
    private MBeanInfo mBeanInfo;

    public CorrelationLogConfig(String componentName, CorrelationLogConfigAttribute[] attributes) {
        this.componentName = componentName;
        this.attributes = attributes;
        buildDynamicMBean();
    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attribute == null) {
            throw new RuntimeException(new IllegalArgumentException("Attribute name cannot be null."));
        }

        // If the attribute name is valid, return the value from the config map.
        if (isValidAttribute(attribute)) {
            return ConfigMapHolder.getInstance().getConfig(componentName, attribute);
        }
        throw new AttributeNotFoundException("Cannot find " + attribute + " attribute");
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        if (attribute == null) {
            throw new RuntimeException(new IllegalArgumentException("Attribute name cannot be null."));
        }

        // If the attribute name is valid, set the value to the config map.
        if (isValidAttribute(attribute.getName())) {
            ConfigMapHolder.getInstance().setConfig(componentName, attribute.getName(), attribute.getValue());
            return;
        }
        throw new AttributeNotFoundException("Cannot find " + attribute + " attribute");
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return null;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mBeanInfo;
    }

    /**
     * Iterate each
     */
    private void buildDynamicMBean() {
        MBeanAttributeInfo[] attributeInfos = new MBeanAttributeInfo[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            attributeInfos[i] = new MBeanAttributeInfo(
                    attributes[i].getName(),
                    attributes[i].getType(),
                    attributes[i].getDescription(),
                    true,
                    true,
                    attributes[i].getType().equals(Boolean.class.getName()));

            // Add the default value into the config map.
            ConfigMapHolder.getInstance().setConfig(componentName, attributes[i].getName(),
                    attributes[i].getDefaultValue());
        }
        mBeanInfo = new MBeanInfo(this.getClass().getName(), "", attributeInfos, null, null, new MBeanNotificationInfo[0]);
    }

    /**
     * Check whether the provided attributes is valid by validating it against the given attribute list.
     *
     * @param attribute
     * @return
     */
    private boolean isValidAttribute(String attribute) {
        for (CorrelationLogConfigAttribute attr : attributes) {
            if (attr.getName().equals(attribute)) {
                return true;
            }
        }
        log.debug("Invalid attribute name " + attribute + " in the component " + componentName);
        return false;
     }
}
