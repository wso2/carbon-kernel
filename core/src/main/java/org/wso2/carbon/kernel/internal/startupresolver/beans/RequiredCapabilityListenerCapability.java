/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.kernel.internal.startupresolver.beans;

import org.osgi.framework.Bundle;

import java.util.List;

/**
 * {@code RequiredCapabilityListenerCapability} Represents a {@code RequiredCapabilityListener}.
 *
 * This is to support backward compatibility with the previous implementation of the startup order resolver.
 *
 * @since 5.1.0
 */
public class RequiredCapabilityListenerCapability extends OSGiServiceCapability {

    private String componentName;
    private List<String> requiredServiceList;

    public RequiredCapabilityListenerCapability(String capabilityName,
                                                CapabilityType type,
                                                String componentName,
                                                List<String> requiredCapabilityList,
                                                Bundle bundle) {
        super(capabilityName, type, bundle);
        this.componentName = componentName;
        this.requiredServiceList = requiredCapabilityList;
    }

    public String getComponentName() {
        return componentName;
    }

    public List<String> getRequiredServiceList() {
        return requiredServiceList;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof RequiredCapabilityListenerCapability)) {
            return false;
        }

        RequiredCapabilityListenerCapability other = (RequiredCapabilityListenerCapability) obj;
        return this.componentName.equals(other.getName()) &&
                this.bundle.equals(other.getBundle());
    }

    public int hashCode() {
        assert false;
        return 20;
    }
}
