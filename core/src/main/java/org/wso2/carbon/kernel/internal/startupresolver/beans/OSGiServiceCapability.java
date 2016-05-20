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

/**
 * Represents an OSGi service capability.
 * For an example all following manifest headers are converted to {@code OSGiServiceCapability} objects.
 * <p>
 * Carbon-Component: osgi.service; objectClass="org.wso2.carbon.kernel.transports.CarbonTransport"
 *
 * @since 5.1.0
 */
public class OSGiServiceCapability extends Capability {

    private String dependentComponentName;

    public OSGiServiceCapability(String capabilityName, CapabilityType type, CapabilityState state, Bundle bundle) {
        super(capabilityName, type, state, bundle);
    }

    public String getDependentComponentName() {
        return dependentComponentName;
    }

    public void setDependentComponentName(String dependentComponentName) {
        this.dependentComponentName = dependentComponentName;
    }

    /**
     * Checks whether the given OSGiServiceCapability is equal to this OSGiServiceCapability.
     * <p>
     * Two OSGiServiceCapability objects are equal if
     * 1) Their names are equal and,
     * 2) Their are from the same bundle and,
     * 3) When one of the OSGiServiceCapability is in the expected state and the other one is in the available
     * state or vice versa.
     *
     * @param obj OSGiServiceCapability to be checked.
     * @return 'true' if the given OSGiServiceCapability is equal to this OSGiServiceCapability.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Capability)) {
            return false;
        }

        Capability other = (Capability) obj;
        return this.name.equals(other.getName()) && this.bundle.equals(other.getBundle()) &&
                (this.getState() == CapabilityState.AVAILABLE && other.getState() == CapabilityState.EXPECTED ||
                        this.getState() == CapabilityState.EXPECTED &&
                                other.getState() == CapabilityState.AVAILABLE);
    }

    public int hashCode() {
        assert false;
        return 30;
    }
}
