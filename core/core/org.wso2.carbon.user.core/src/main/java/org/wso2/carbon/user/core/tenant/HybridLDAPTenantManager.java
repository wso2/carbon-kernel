/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.core.tenant;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.util.Map;

/**
 * this class is used to store the tenant details in ldap server as well
 */

public class HybridLDAPTenantManager extends JDBCTenantManager {

    private static final Log log = LogFactory.getLog(HybridLDAPTenantManager.class);

    public HybridLDAPTenantManager(OMElement omElement, Map<String, Object> properties)
            throws Exception {
        super(omElement, properties);
    }

    public HybridLDAPTenantManager(DataSource dataSource, String superTenantDomain) {
        super(dataSource, superTenantDomain);
    }

    /**
     * @inheritDoc When this tenant manager is used, need to make sure that tenant partitions corresponding to
     * tenants which are currently existing, are initialized in the LDAP side.
     */
    @Override
    public void initializeExistingPartitions() {
        try {
            Tenant[] existingTenants = getAllTenants();
            LDAPTenantManager ldapTenantManager = getLDAPTenantManager();
            for (Tenant tenant : existingTenants) {
                if (!tenant.getDomain().equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    ldapTenantManager.addPartitionToTenant(tenant);
                }
            }
        } catch (UserStoreException e) {
            log.error("Cannot get the existing tenants", e);
        }
    }

    @Override
    public int addTenant(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {
        int tenantID = super.addTenant(tenant);
        tenant.setId(tenantID);
        LDAPTenantManager ldapTenantManager = getLDAPTenantManager();
        ldapTenantManager.addTenant((Tenant) tenant);
        return tenantID;
    }

    @Override
    public void updateTenant(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {
        super.updateTenant(tenant);
        LDAPTenantManager ldapTenantManager = getLDAPTenantManager();
        ldapTenantManager.updateTenant((Tenant) tenant);
    }

    @Override
    public void deleteTenant(int tenantId) throws UserStoreException {
        super.deleteTenant(tenantId);
        LDAPTenantManager ldapTenantManager = getLDAPTenantManager();
        ldapTenantManager.deleteTenant(tenantId);
    }

    public void setBundleContext(BundleContext bundleContext) {
        super.setBundleContext(bundleContext);

    }

    private LDAPTenantManager getLDAPTenantManager() {
        ServiceReference serviceReference = this.bundleContext.getServiceReference(
                LDAPTenantManager.class.getName());
        return (LDAPTenantManager) this.bundleContext.getService(serviceReference);
    }
}
