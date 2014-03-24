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

import java.util.Date;
import java.util.Map;

/**
 * Tenant is the entity which represent the concept of a tenant in the Carbon runtime.
 * <p/>
 * Tenant is the entity which represents a virtual partition(isolated area) of a software which is designed
 * according to the multi-tenancy architecture. We use the term tenant in Carbon to refer an isolated partition
 * in the server. Therefore we can define a tenant as the unit of isolation.
 * <p/>
 * Tenant interface extends the TenantContainer interface to support hierarchical tenancy model where a group of
 * tenants are connected to each other with parent-child relationships.
 *
 * @see TenantContainer
 * @since 5.0.0
 */
public interface Tenant extends TenantContainer {

    /**
     * Returns the domain of the tenant.
     *
     * @return the tenant domain
     */
    public String getDomain();

    /**
     * Returns the human readable name of the tenant.
     *
     * @return the tenant name
     */
    public String getName();

    /**
     * Returns the tenant description.
     *
     * @return the tenant description
     */
    public String getDescription();

    /**
     * Returns created date and time of the tenant.
     *
     * @return the tenant created date
     */
    public Date getCreatedDate();

    /**
     * Returns the username of the tenant administrator.
     *
     * @return the username of the tenant administrator
     */
    public String getAdminUsername();

    /**
     * Returns the email address of the tenant administrator.
     *
     * @return the email address of the tenant administrator
     */
    public String getAdminUserEmailAddress();

    /**
     * Returns the value of the attribute <code>key</code>.
     *
     * @param key the attribute key
     * @return value of the specified attribute
     */
    public String getProperty(String key);

    /**
     * Returns attributes of the tenant.
     *
     * @return a map of tenant attributes
     */
    public Map<String, String> getProperties();

    /**
     * Sets the tenant domain.
     *
     * @param domain the tenant domain
     */
    public void setDomain(String domain);

    /**
     * Sets a human readable name of the tenant.
     *
     * @param name the tenant name
     */
    public void setName(String name);

    /**
     * Sets a description of the tenant.
     *
     * @param description the tenant description
     */
    public void setDescription(String description);

    /**
     * Sets the created date and time of the tenant.
     *
     * @param createdDate the tenant created date
     */
    public void setCreatedDate(Date createdDate);

    /**
     * Sets the username of the tenant administrator.
     *
     * @param adminUsername the username of the tenant administrator
     */
    public void setAdminUsername(String adminUsername);

    /**
     * Sets the email address of the tenant administrator.
     *
     * @param emailAddress the email address of the tenant administrator
     */
    public void setAdminUserEmailAddress(String emailAddress);

    /**
     * Sets a tenant attribute
     *
     * @param key   the attribute name
     * @param value the attribute value
     */
    public void setProperty(String key, String value);

    /**
     * Sets a map of the tenant attributes.
     *
     * @param props the maps of tenant attributes
     */
    public void setProperties(Map<String, String> props);
}
