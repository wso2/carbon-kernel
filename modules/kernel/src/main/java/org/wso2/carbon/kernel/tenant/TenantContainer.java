/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.kernel.tenant;

import java.util.Map;

/**
 * TenantContainer is the entity which implements the hierarchical tenancy model.
 * <p/>
 * Tenant hierarchy is a tenant group with parent-child relationships. We can name the root nodes of each tenant
 * hierarchy as top-level tenants or primary tenants. The parent of the top-level tenant can be null or can be
 * the “Server”.
 *
 * @see Tenant
 * @since 5.0.0
 */
public interface TenantContainer {

    /**
     * Returns the tenant identifier.
     *
     * @return the tenant id
     */
    public String getID();

    /**
     * Returns the parent tenant of this tenant
     *
     * @return the parent tenant
     */
    public TenantContainer getParent();

    /**
     * Returns child tenants of this tenant.
     *
     * @return a map of child tenants
     */
    public Map<String, TenantContainer> getChildren();

    /**
     * Returns the allowed depth of the tenant hierarchy.
     *
     * @return the allowed depth of the tenant hierarchy
     */
    public int getDepthOfHierarchy();

    /**
     * Sets the tenant ID.
     *
     * @param id the tenant id
     */
    public void setID(String id);

    /**
     * Sets the parent tenant.
     *
     * @param parentTenant the parent tenant
     */
    public void setParent(TenantContainer parentTenant);

    /**
     * Add a new child tenant
     *
     * @param childTenant the child tenant
     */
    public void addChild(TenantContainer childTenant);

    /**
     * Remove the parent tenant
     *
     * @param parentTenant the parent tenant.
     */
    public void unsetParent(TenantContainer parentTenant);

    /**
     * @param childTenant the tenant to be removed as a child.
     *
     * @return the removed child tenant
     */
    public TenantContainer removeChild(TenantContainer childTenant);

    /**
     * Sets the allowed depth of tenant hierarchy.
     *
     * @param depthOfHierarchy the depth of the tenant hierarchy
     */
    public void setDepthOfHierarchy(int depthOfHierarchy);
}
