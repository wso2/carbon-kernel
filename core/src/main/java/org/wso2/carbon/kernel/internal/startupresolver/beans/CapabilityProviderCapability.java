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
                                        String providedCapabilityName,
                                        Bundle bundle) {

        super(capabilityName, type, bundle);
        this.providedCapabilityName = providedCapabilityName;
    }

    public String getProvidedCapabilityName() {
        return providedCapabilityName;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CapabilityProviderCapability)) {
            return false;
        }

        CapabilityProviderCapability other = (CapabilityProviderCapability) obj;
        return this.providedCapabilityName.equals(other.getProvidedCapabilityName()) &&
                this.bundle.equals(other.getBundle());
    }

    public int hashCode() {
        assert false;
        return 40;
    }
}
