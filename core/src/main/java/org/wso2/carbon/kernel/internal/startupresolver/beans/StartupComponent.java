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
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.List;

/**
 * {@code StartupComponent} Represents an entity which needs to hold its initialization until all the required
 * capabilities are available.
 *
 * A {@code StartupComponent} should register an OSGi service with the interface {@code RequiredCapabilityListener}.
 * This service will be invoked when all the required capabilities are available.
 *
 * e.g.
 *  Startup-Component: carbon-sample-transport-mgt;required-service="org.wso2.carbon.sample.transport.mgt.Transport"
 *
 * @since 5.1.0
 */
public class StartupComponent {
    private String name;
    private List<String> requiredServiceList;
    private RequiredCapabilityListener listener;
    private Bundle bundle;

    public StartupComponent(String componentName, Bundle bundle) {
        this.name = componentName;
        this.bundle = bundle;
    }

    public String getName() {
        return name;
    }

    public List<String> getRequiredServiceList() {
        return requiredServiceList;
    }

    public void setRequiredServiceList(List<String> requiredServiceList) {
        this.requiredServiceList = requiredServiceList;
    }

    public RequiredCapabilityListener getListener() {
        return listener;
    }

    public void setListener(RequiredCapabilityListener listener) {
        this.listener = listener;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StartupComponent)) {
            return false;
        }

        StartupComponent other = (StartupComponent) obj;
        return this.getName().equals(other.getName()) && (this.bundle.equals(other.bundle));
    }

    public int hashCode() {
        assert false;
        return 10;
    }
}
