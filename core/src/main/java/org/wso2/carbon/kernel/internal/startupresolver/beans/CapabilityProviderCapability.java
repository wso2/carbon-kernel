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
 * Represents a {@code CapabilityProvider}.
 *
 * @since 5.1.0
 */
public class CapabilityProviderCapability extends OSGiServiceCapability {
    private String providedCapabilityName;

    public CapabilityProviderCapability(String capabilityName,
                                        CapabilityType type,
                                        CapabilityState status,
                                        String providedCapabilityName,
                                        Bundle bundle) {

        super(capabilityName, type, status, bundle);
        this.providedCapabilityName = providedCapabilityName;
    }

    public String getProvidedCapabilityName() {
        return providedCapabilityName;
    }

    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Checks whether the given {@code CapabilityProviderCapability} is equal to
     * this {@code CapabilityProviderCapability} instance.
     * <p>
     * Two {@code CapabilityProviderCapability} instances are equal if all the following conditions are satisfied.
     * <p>
     * 1) Their providedCapabilityName values should be equal,
     * 2) They should be from the same bundle.
     * 3) When one of the {@code CapabilityProviderCapability} instances is in the EXPECTED state, the other one
     * should be in the AVAILABLE state, or vice versa.
     *
     * @param obj OSGiServiceCapability to be checked.
     * @return true if the given object is equal to the this object.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof CapabilityProviderCapability)) {
            return false;
        }

        CapabilityProviderCapability other = (CapabilityProviderCapability) obj;
        return this.providedCapabilityName.equals(other.getProvidedCapabilityName()) &&
                this.bundle.equals(other.getBundle()) &&
                (this.getState() == CapabilityState.AVAILABLE && other.getState() == CapabilityState.EXPECTED ||
                        this.getState() == CapabilityState.EXPECTED &&
                                other.getState() == CapabilityState.AVAILABLE);
    }

    public int hashCode() {
        assert false;
        return 40;
    }
}
