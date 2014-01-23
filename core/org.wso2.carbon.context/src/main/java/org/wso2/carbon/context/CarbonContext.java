/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.context;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.queuing.CarbonQueue;
import org.wso2.carbon.queuing.CarbonQueueManager;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URI;
import java.util.Hashtable;

/**
 * This provides the API for sub-tenant programming around
 * <a href="http://wso2.com/products/carbon">WSO2 Carbon</a> and
 * <a href="http://wso2.com/cloud/stratos">WSO2 Stratos</a>. Each CarbonContext will utilize an
 * underlying {@link CarbonContextHolder} instance, which will store the actual data.
 */
@SuppressWarnings("unused")
public class CarbonContext {

    // The reason to why we decided to have a CarbonContext and a CarbonContextHolder is to address
    // the potential build issues due to cyclic dependencies. Therefore, any bundle that can access
    // the CarbonContext can also access the CarbonContext holder. But, there are some low-level
    // bundles that can only access the CarbonContext holder. The CarbonContext provides a much
    // cleaner and easy to use API around the CarbonContext holder.

    private CarbonContextHolder carbonContextHolder = null;

    /**
     * Creates a CarbonContext using the given CarbonContext holder as its backing instance.
     *
     * @param carbonContextHolder the CarbonContext holder that backs this CarbonContext object.
     *
     * @see CarbonContextHolder
     */
    protected CarbonContext(CarbonContextHolder carbonContextHolder) {
        this.carbonContextHolder = carbonContextHolder;
    }

    /**
     * Utility method to obtain the current CarbonContext holder after an instance of a
     * CarbonContext has been created.
     *
     * @return the current CarbonContext holder
     */
    protected CarbonContextHolder getCarbonContextHolder() {
        if (carbonContextHolder == null) {
            return CarbonContextHolder.getCurrentCarbonContextHolder();
        }
        return carbonContextHolder;
    }

    /**
     * Obtains the CarbonContext instance stored on the CarbonContext holder.
     *
     * @return the CarbonContext instance.
     */
    public static CarbonContext getCurrentContext() {
        return new CarbonContext(null);
    }

    /**
     * Method to obtain the tenant id on this CarbonContext instance.
     *
     * @return the tenant id.
     */
    public int getTenantId() {
        CarbonBaseUtils.checkSecurity();
        return getCarbonContextHolder().getTenantId();
    }

    /**
     * Method to obtain the username on this CarbonContext instance.
     *
     * @return the username.
     */
    public String getUsername() {
        return getCarbonContextHolder().getUsername();
    }

    /**
     * Method to obtain the tenant domain on this CarbonContext instance.
     *
     * @return the tenant domain.
     */
    public String getTenantDomain() {
        return getCarbonContextHolder().getTenantDomain();
    }

    /**
     * Method to obtain an instance of a registry on this CarbonContext instance.
     *
     * @param type the type of registry required.
     *
     * @return the requested registry instance.
     */
    public Registry getRegistry(RegistryType type) {
        switch (type) {
            case USER_CONFIGURATION:
                return (Registry) getCarbonContextHolder().getProperty(
                        CarbonContextHolder.CONFIG_USER_REGISTRY_INSTANCE);

            case SYSTEM_CONFIGURATION:
                return (Registry) getCarbonContextHolder().getProperty(
                        CarbonContextHolder.CONFIG_SYSTEM_REGISTRY_INSTANCE);

            case USER_GOVERNANCE:
                return (Registry) getCarbonContextHolder().getProperty(
                        CarbonContextHolder.GOVERNANCE_USER_REGISTRY_INSTANCE);

            case SYSTEM_GOVERNANCE:
                return (Registry) getCarbonContextHolder().getProperty(
                        CarbonContextHolder.GOVERNANCE_SYSTEM_REGISTRY_INSTANCE);

            case LOCAL_REPOSITORY:
                return (Registry) getCarbonContextHolder().getProperty(
                        CarbonContextHolder.LOCAL_REPOSITORY_INSTANCE);

            default:
                return null;
        }
    }

    /**
     * Method to obtain the user realm on this CarbonContext instance.
     *
     * @return the user realm instance.
     */
    public UserRealm getUserRealm() {
        return (UserRealm) getCarbonContextHolder().getProperty(CarbonContextHolder.USER_REALM);
    }

    /**
     * Method to obtain the default cache instance.
     *
     * @return the cache instance.
     */
    public Cache getCache(String cacheName) {
        return CacheManager.getInstance().getCache(cacheName);
    }

    /**
     * Method to obtain a named queue instance.
     *
     * @param name the name of the queue instance.
     *
     * @return the queue instance.
     */
    public CarbonQueue<?> getQueue(String name) {
        return CarbonQueueManager.getInstance().getQueue(name);
    }

    /**
     * Method to obtain a JNDI-context with the given initialization properties.
     *
     * @param properties the properties required to create the JNDI-contNDext instance.
     *
     * @return the JNDI-context.
     * @throws NamingException if the operation failed.
     */
    public Context getJNDIContext(Hashtable properties) throws NamingException {
        return new InitialContext(properties);
    }

    /**
     * Method to obtain a JNDI-context.
     *
     * @return the JNDI-context.
     * @throws NamingException if the operation failed.
     */
    public Context getJNDIContext() throws NamingException {
        return new InitialContext();
    }

    /**
     * Method to discover a set of service endpoints belonging the defined scopes..
     *
     * @param scopes the scopes in which to look-up for the service.
     *
     * @return a list of service endpoints.
     */
    public String[] discover(URI[] scopes) {
        try {
            return CarbonContextHolder.getDiscoveryServiceProvider().probe(null, scopes, null,
                    getCarbonContextHolder().getTenantId());
        } catch (Exception ignored) {
            // If an exception occurs, simply return no endpoints. The discovery component will
            // be responsible of reporting any errors.
            return new String[0];
        }
    }
}
