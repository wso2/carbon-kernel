/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.jdbc.realm;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.DefaultRealmService;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.JDBCTenantManager;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to represent an in memory realm service.
 */
public class InMemoryRealmService implements RealmService {

    private Map<Integer, UserRealm> userRealmMap = new HashMap<Integer, UserRealm>();
    private RealmConfiguration bootstrapRealmConfig = null;
    private UserRealm bootstrapRealm = null;
    private static final Log log = LogFactory.getLog(InMemoryRealmService.class);
    private TenantManager tenantManager;
    private BasicDataSource dataSource;
    private DefaultRealmService realmService;

    /**
     * Construct a in memory realm service.
     *
     * @throws RegistryException throws if the operation failed.
     */
    public InMemoryRealmService() throws RegistryException {
        setup();
        try {
            realmService = new DefaultRealmService(bootstrapRealmConfig, tenantManager);
            bootstrapRealm = initializeRealm(bootstrapRealmConfig, dataSource, MultitenantConstants.SUPER_TENANT_ID);
            
        } catch (Exception e) {
            String msg = "Error in init bootstrap realm";
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Method to return a user realm for the given realm configuration.
     *
     * @param tenantRealmConfig the realm configuration.
     *
     * @return the user realm
     * @throws UserStoreException throws if the operation failed.
     */
    public UserRealm getUserRealm(RealmConfiguration tenantRealmConfig) throws UserStoreException {
        int tenantId = tenantRealmConfig.getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return bootstrapRealm;
        }
        UserRealm userRealm = userRealmMap.get(tenantId);
        if (userRealm == null) {
            userRealm = initializeRealm(tenantRealmConfig, dataSource, tenantId);
            userRealmMap.put(tenantId, userRealm);
        } else {
            long existingRealmPersistedTime = -1L;
            long newRealmConfigPersistedTime = -1L;
            if (userRealm.getRealmConfiguration().getPersistedTimestamp() != null) {
                existingRealmPersistedTime = userRealm.getRealmConfiguration()
                        .getPersistedTimestamp().getTime();
            }
            if (tenantRealmConfig.getPersistedTimestamp() != null) {
                newRealmConfigPersistedTime = tenantRealmConfig.getPersistedTimestamp().getTime();
            }

            if (existingRealmPersistedTime != newRealmConfigPersistedTime) {
                // this is an update
                userRealm = initializeRealm(tenantRealmConfig, dataSource, tenantId);
                userRealmMap.put(tenantId, userRealm);
            }
        }
        return userRealm;
    }

    /**
     * Method to set a user realm instance.
     *
     * @param tenantId the tenant identifier
     * @param realm    the user realm of the tenant
     *
     * @throws UserStoreException if the operation failed.
     */
    @SuppressWarnings("unused")
    public void setUserRealm(int tenantId, UserRealm realm) throws UserStoreException {
        userRealmMap.put(tenantId, realm);
    }

    /**
     * Get the boot strap configuration (tenant0's configuration).
     *
     * @return the bootstrap realm configuration.
     */
    public RealmConfiguration getBootstrapRealmConfiguration() {
        return bootstrapRealmConfig;
    }

    /**
     * Setup the realm service.
     *
     * @throws RegistryException if the operation failed.
     */
    public void setup() throws RegistryException {

        String derbyDBName = "target/databasetest/CARBON_TEST";
        String dbDirectory = "target/databasetest";
        if ((new File(dbDirectory)).exists()) {
            deleteDBDir(new File(dbDirectory));
        }
        // create an in-memory realm
        try {
            // check whether the driver is loaded.
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            String msg = "Derby database embedded driver is not available in the class path. "
                    + "Could not create the database for the user manager.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        dataSource = new BasicDataSource();
        String connectionUrl = "jdbc:h2:" + derbyDBName;
        dataSource.setUrl(connectionUrl);
        dataSource.setDriverClassName("org.h2.Driver");

        try {
            DatabaseCreator creator = new DatabaseCreator(dataSource);
            creator.createRegistryDatabase();
            RealmConfigXMLProcessor builder = new RealmConfigXMLProcessor();
            InputStream inStream = new FileInputStream(
                    "src/test/resources/user-test/user-mgt-registry-test.xml");
            try {
                bootstrapRealmConfig = builder.buildRealmConfiguration(inStream);
            } finally {
                inStream.close();
            }
        } catch (Exception e) {
            String msg = "Failed to initialize the user manager. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        this.tenantManager = new JDBCTenantManager(dataSource, null);
    }

    /**
     * Get the bootstrap realm.
     *
     * @return the bootstrap realm.
     * @throws UserStoreException throws if the operation failed.
     */
    public UserRealm getBootstrapRealm() throws UserStoreException {
        return bootstrapRealm;
    }

    public void setTenantManager(org.wso2.carbon.user.api.TenantManager tenantManager)
            throws org.wso2.carbon.user.api.UserStoreException {
        setTenantManager((TenantManager) tenantManager);
    }

    /**
     * Delete the temporary database directory.
     *
     * @param dir database directory.
     *
     * @return true if the database directory was deleted
     */
    private static boolean deleteDBDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (!deleteDBDir(new File(dir, child))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Return the tenant manager.
     *
     * @return the tenant manager.
     */
    public TenantManager getTenantManager() {
        return tenantManager;
    }

    public org.wso2.carbon.user.api.UserRealm getTenantUserRealm(int tenantId)
            throws UserStoreException {
        try {
        	if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
        		return this.bootstrapRealm;
        	}
            return realmService.getTenantUserRealm(tenantId);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to initialize the user manager. " + e.getMessage();
            log.error(msg, e);
            throw new UserStoreException(msg, e);
        }
    }

    /**
     * Initialize the realm for a tenant id.
     *
     * @param realmConfig realm configuration.
     * @param dataSource  data source
     * @param tenantId    tenant id
     *
     * @return the user realm.
     * @throws UserStoreException the user realm.
     */
    @SuppressWarnings("unused")
    public UserRealm initializeRealm(RealmConfiguration realmConfig, DataSource dataSource,
                                     int tenantId) throws UserStoreException {
        // the data source is thought to be of use on a later date, so leaving the method as it is.
        UserRealm userRealm = new DefaultRealm();
        userRealm.init(realmConfig, null, null, tenantId);

        return userRealm;
    }
    
    public void setTenantManager(org.wso2.carbon.user.core.tenant.TenantManager t) {
    }

    public MultiTenantRealmConfigBuilder getMultiTenantRealmConfigBuilder()
            throws UserStoreException {
        return null;
    }

    public UserRealm getCachedUserRealm(int tenantId) throws UserStoreException {
        return this.userRealmMap.get(tenantId);
    }

    public void clearCachedUserRealm(int i) throws UserStoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * This method is used in default realm service in tenant-mgt. Hence no implementation for this
     * method.
     */
    public TenantMgtConfiguration getTenantMgtConfiguration(){
        TenantMgtConfiguration tenantMgtConfig = null;
        return tenantMgtConfig;

    }


    public void addCustomUserStore(String realmName, String userStoreClassName,
                                   Map<String, String> properties, int tenantId)
                                                                                throws UserStoreException{
        
    }

	@Override
	public void setBootstrapRealmConfiguration(RealmConfiguration arg0) {
		
	}
    
}
