/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.tenant;

/**
 * An event used with the TenantListener for tenant lifecycle event notification.
 *
 * @since 1.0.0
 */
public class TenantEvent {

    public static final int ADDED = 0x00000001;

    public static final int REMOVED = 0x00000002;

    public static final int LOADED = 0x00000003;

    public static final int UNLOADED = 0x00000004;

    private final int type;

    private final String tenantDomain;


    /**
     * Constructs a tenant event object using the given event type and domain value.
     *
     * @param type event type to use with this instance.
     * @param tenantDomain name of the tenant domain to use with this instance.
     */
    public TenantEvent(int type, String tenantDomain) {
        this.type = type;
        this.tenantDomain = tenantDomain;
    }

    /**
     * Returns the tenant lifecycle event type of this event
     *
     * @return the integer value of the event type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the tenant domain associated with this event.
     *
     * @return tenantDomain
     */
    public String getTenantDomain() {
        return tenantDomain;
    }
}
