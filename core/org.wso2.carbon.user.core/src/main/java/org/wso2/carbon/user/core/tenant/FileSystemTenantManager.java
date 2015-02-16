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
package org.wso2.carbon.user.core.tenant;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.utils.CarbonUtils;

import javax.sql.DataSource;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Store the realm configuration of tenant to file system and rest in database
 */
public class FileSystemTenantManager extends CommonHybridLDAPTenantManager {
    DataSource dataSource;
    private static Log log = LogFactory.getLog(TenantManager.class);
    protected BundleContext bundleContext;
    private final String CarbonHome = CarbonUtils.getCarbonHome();
    private String filePath = CarbonHome + "/repository/tenants/";
    private final String userMgtXml = "user-mgt.xml";

    /**
     * Map which maps tenant domains to tenant IDs
     * <p/>
     * Key - tenant domain, value - tenantId
     */
    private Map tenantDomainIdMap = new ConcurrentHashMap<String, Integer>();

    /**
     * This is the reverse of the tenantDomainIdMap. Key - tenantId, value - tenant domain
     */
    private Map tenantIdDomainMap = new ConcurrentHashMap<Integer, String>();

    protected TenantCache tenantCacheManager = TenantCache.getInstance();
    private LDAPConnectionContext ldapConnectionSource;
    private TenantMgtConfiguration tenantMgtConfig = null;
    private RealmConfiguration realmConfig = null;


    public FileSystemTenantManager(OMElement omElement, Map<String, Object> properties) throws Exception {
        super(omElement, properties);
    }

    public FileSystemTenantManager(DataSource dataSource, String superTenantDomain) {
        super(dataSource, superTenantDomain);
    }

    /**
     * Adds a tenant. RealmConfiguration is saved to file system and rest of the details kept in database
     *
     * @param tenant
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public int addTenant(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {
        int tenantID = super.addTenant(tenant);

        RealmConfiguration tenantRealmConfig = tenant.getRealmConfig();
        tenantRealmConfig.setSecondaryRealmConfig(null);
        String realmConfigString = RealmConfigXMLProcessor.serialize(
                tenantRealmConfig).toString();
        saveConfigToFileSystem(tenantID, realmConfigString);

        return tenantID;
    }

    private void saveConfigToFileSystem(int id, String realmConfigString) {
        File tenantFolder = new File(filePath + id + File.separator + "user-mgt.xml");
        if (!tenantFolder.exists()) {
            new File(filePath + id).mkdir();
        }
        try {
            FileUtils.writeStringToFile(tenantFolder, realmConfigString);
        } catch (IOException e) {
            log.error("Error in saving realm configuration of tenant:" + id + ".");
        }
    }


    /**
     * Get the tenant details.Realm configuration read from file system and rest of the details retrieved from database
     *
     * @param tenantId
     * @return
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public Tenant getTenant(int tenantId) throws UserStoreException {
        Tenant tenant = super.getTenant(tenantId);

        InputStream inStream;
        try {

            inStream = new FileInputStream(filePath + tenantId + File.separator + userMgtXml);

            if (inStream == null) {
                String message = "Configuration file could not be read in.";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
            }
            inStream = CarbonUtils.replaceSystemVariablesInXml(inStream);
            RealmConfigXMLProcessor processor = new RealmConfigXMLProcessor();
            RealmConfiguration realmConfig = processor.buildRealmConfiguration(inStream);

            //Set tenant id for all user stores
            realmConfig.setTenantId(tenantId);
            RealmConfiguration secondary = realmConfig.getSecondaryRealmConfig();
            while(secondary!=null){
                secondary.setTenantId(tenantId);
                secondary = secondary.getSecondaryRealmConfig();
            }

            tenant.setRealmConfig(realmConfig);
            tenant.setAdminName(realmConfig.getAdminUserName());
            tenantCacheManager.addToCache(new TenantIdKey(tenantId), new TenantCacheEntry<Tenant>(tenant));

        } catch (CarbonException e) {
            String errorMessage = "Error occurred while replacing System variables in "+userMgtXml;
            if(log.isDebugEnabled()){
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (FileNotFoundException e) {
            log.error("Realm configuration file 'user-mgt.xml',does not exist for tenant:" + tenantId);
        }

        return tenant;
    }


//    public void deleteTenant(int tenantId) throws UserStoreException {
//
//        // Remove tenant information from the cache.
//        tenantIdDomainMap.remove(tenantId);
//        tenantCacheManager.clearCacheEntry(new TenantIdKey(tenantId));
//
//        Connection dbConnection = null;
//        PreparedStatement prepStmt = null;
//        try {
//            dbConnection = getDBConnection();
//            String sqlStmt = TenantConstants.DELETE_TENANT_SQL;
//            prepStmt = dbConnection.prepareStatement(sqlStmt);
//            prepStmt.setInt(1, tenantId);
//
//            prepStmt.executeUpdate();
//            dbConnection.commit();
//        } catch (SQLException e) {
//            DatabaseUtil.rollBack(dbConnection);
//            String msg = "Error in deleting the tenant with " + "tenant id: "
//                    + tenantId + ".";
//            log.error(msg, e);
//            throw new UserStoreException(msg, e);
//        } finally {
//            DatabaseUtil.closeAllConnections(dbConnection,prepStmt);
//        }
//    }
//
//
//    private Connection getDBConnection() throws SQLException {
//        Connection dbConnection = DatabaseUtil.getDBConnection(this.dataSource);
//        dbConnection.setAutoCommit(false);
//        return dbConnection;
//    }

}
