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
 * Represents a generic capability provided by an OSGi bundle.
 *
 * @since 5.1.0
 */
public class Capability {
    protected String name;
    protected CapabilityType type;
    protected Bundle bundle;

    /**
     * Describes the type of the Capability.
     *
     * @since 5.1.0
     */
    public enum CapabilityType {
        OSGi_SERVICE,
        MANIFEST_HEADER
    }

    public Capability(String name, CapabilityType type, Bundle bundle) {
        this.name = name;
        this.type = type;
        this.bundle = bundle;
    }

    public String getName() {
        return name;
    }

    public CapabilityType getType() {
        return type;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Capability)) {
            return false;
        }

        Capability other = (Capability) obj;
        return this.name.equals(other.getName()) && this.bundle.equals(other.getBundle());
    }

    public int hashCode() {
        assert false;
        return 50;
    }
}
