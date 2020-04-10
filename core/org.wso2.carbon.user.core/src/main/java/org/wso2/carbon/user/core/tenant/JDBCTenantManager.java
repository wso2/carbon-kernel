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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.caching.impl.CacheManagerFactoryImpl;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.RealmCache;
import org.wso2.carbon.user.core.common.UserStoreDeploymentManager;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.constants.UserCoreClaimConstants;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.cache.Caching;
import javax.sql.DataSource;

public class JDBCTenantManager implements TenantManager {

    public static final String COLUMN_NAME_UM_ID = "UM_ID";
    public static final String COLUMN_NAME_UM_DOMAIN_NAME = "UM_DOMAIN_NAME";
    public static final String COLUMN_NAME_UM_EMAIL = "UM_EMAIL";
    public static final String COLUMN_NAME_UM_ACTIVE = "UM_ACTIVE";
    public static final String COLUMN_NAME_UM_CREATED_DATE = "UM_CREATED_DATE";
    public static final String COLUMN_NAME_UM_USER_CONFIG = "UM_USER_CONFIG";
    public static final String COLUMN_NAME_UM_TENANT_UUID = "UM_TENANT_UUID";
    private static Log log = LogFactory.getLog(TenantManager.class);
    protected BundleContext bundleContext;
    protected TenantCache tenantCacheManager = TenantCache.getInstance();
    DataSource dataSource;
    /**
     * Map which maps tenant domains to tenant IDs
     * <p/>
     * Key - tenant domain, value - tenantId
     */
    private TenantIdCache tenantIdCache = TenantIdCache.getInstance();

    /**
     * Map which maps tenant uniqueIds to tenants.
     * Key - tenant uuid, value - tenant.
     */
    private TenantUniqueIdCache tenantUniqueIdCache = TenantUniqueIdCache.getInstance();

    /**
     * This is the reverse of the tenantDomainIdMap. Key - tenantId, value - tenant domain
     */
    private TenantDomainCache tenantDomainCache = TenantDomainCache.getInstance();

    public JDBCTenantManager(OMElement omElement, Map<String, Object> properties) throws Exception {
        this.dataSource = (DataSource) properties.get(UserCoreConstants.DATA_SOURCE);
        if (dataSource == null) {
            throw new Exception("Data Source is null");
        }
        this.tenantCacheManager.clear();
        this.tenantIdCache.clear();
        this.tenantDomainCache.clear();
        this.tenantUniqueIdCache.clear();
    }

    //TODO : Remove the unused variable
    public JDBCTenantManager(DataSource dataSource, String superTenantDomain) {
        this.dataSource = dataSource;
    }

    public int addTenant(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {
        // If tenant id present in tenant bean, we create the tenant with that tenant id.
        if (tenant.getId() > 0) {
            return addTenantWithGivenId(tenant);
        }

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        int id = 0;
        String tenantDomain = tenant.getDomain().toLowerCase();
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.ADD_TENANT_SQL;
            String tenantUniqueID = tenant.getTenantUniqueID();
            if (tenantUniqueID != null) {
                sqlStmt = TenantConstants.ADD_TENANT_SQL_WITH_UUID;
            }

            String dbProductName = dbConnection.getMetaData().getDatabaseProductName();
            prepStmt = dbConnection.prepareStatement(sqlStmt, new String[]{DBUtils
                    .getConvertedAutoGeneratedColumnName(dbProductName, COLUMN_NAME_UM_ID)});

            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, tenant.getEmail());
            Date createdTime = tenant.getCreatedDate();
            long createdTimeMs;
            if (createdTime == null) {
                createdTimeMs = System.currentTimeMillis();
            } else {
                createdTimeMs = createdTime.getTime();
            }
            prepStmt.setTimestamp(3, new Timestamp(createdTimeMs));
            String realmConfigString = RealmConfigXMLProcessor.serialize(
                    (RealmConfiguration) tenant.getRealmConfig()).toString();
            InputStream is = new ByteArrayInputStream(realmConfigString.getBytes());
            prepStmt.setBinaryStream(4, is, is.available());

            if (tenantUniqueID != null) {
                prepStmt.setString(5, tenantUniqueID);
            }
            prepStmt.executeUpdate();

            result = prepStmt.getGeneratedKeys();
            if (result.next()) {
                id = result.getInt(1);
            }
            dbConnection.commit();
            if (log.isDebugEnabled()) {
                log.debug("Successfully created the tenant, adding tenant domain to cache where tenantDomain: {"
                        + tenantDomain + "}");
            }
            tenant.setDomain(tenantDomain);
            tenant.setCreatedDate(new Date(createdTimeMs));
            tenantDomainNameValidation(tenantDomain);
            tenantDomainCache.addToCache(new TenantIdKey(id), new TenantDomainEntry(tenantDomain));
            tenantIdCache.addToCache(new TenantDomainKey(tenantDomain), new TenantIdEntry(id));
            if (tenantUniqueID != null) {
                tenantUniqueIdCache.addToCache(new TenantUniqueIDKey(tenantUniqueID), new
                        TenantCacheEntry<org.wso2.carbon.user.api.Tenant>(tenant));
            }
        } catch (Exception e) {
            DatabaseUtil.rollBack(dbConnection);
            String msg = "Error in adding tenant with " + "tenant domain: " + tenantDomain + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }
        return id;
    }

    /**
     * This method is introduced when we require to create a tenant with provided tenant Id. In some cases, we need to
     * duplicate tenant in multiple environments with same Id.
     *
     * @param tenant - tenant bean with tenantId set.
     * @return
     * @throws UserStoreException if tenant Id is already taken.
     */
    public int addTenantWithGivenId(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {
        // check if tenant id is available, if not available throw exception.
        if (getTenant(tenant.getId()) != null) {
            String errorMsg = "Tenant with tenantId:" + tenant.getId() + " is already created. Tenant creation is " +
                    "aborted for tenant domain:" + tenant.getDomain();
            log.error(errorMsg);
            throw new UserStoreException(errorMsg);
        }

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        int id = 0;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.ADD_TENANT_WITH_ID_SQL;

            String dbProductName = dbConnection.getMetaData().getDatabaseProductName();
            prepStmt = dbConnection.prepareStatement(sqlStmt, new String[]{DBUtils
                    .getConvertedAutoGeneratedColumnName(dbProductName, COLUMN_NAME_UM_ID)});
            prepStmt.setInt(1, tenant.getId());
            prepStmt.setString(2, tenant.getDomain().toLowerCase());
            prepStmt.setString(3, tenant.getEmail());
            Date createdTime = tenant.getCreatedDate();
            long createdTimeMs;
            if (createdTime == null) {
                createdTimeMs = System.currentTimeMillis();
            } else {
                createdTimeMs = createdTime.getTime();
            }
            prepStmt.setTimestamp(4, new Timestamp(createdTimeMs));
            String realmConfigString = RealmConfigXMLProcessor.serialize(
                    (RealmConfiguration) tenant.getRealmConfig()).toString();
            InputStream is = new ByteArrayInputStream(realmConfigString.getBytes());
            prepStmt.setBinaryStream(5, is, is.available());

            prepStmt.executeUpdate();

            id = tenant.getId();
            dbConnection.commit();
            if (log.isDebugEnabled()) {
                log.debug("Successfully created tenant with the provided id, ID: " + tenant.getId() + ", adding "
                        + "tenant domain to cache where tenantDomain: {" + tenant.getDomain().toLowerCase() + "}");
            }
            tenantDomainNameValidation(tenant.getDomain().toLowerCase());
            tenantDomainCache.addToCache(new TenantIdKey(id), new TenantDomainEntry(tenant.getDomain().toLowerCase()));
            tenantIdCache.addToCache(new TenantDomainKey(tenant.getDomain().toLowerCase()), new TenantIdEntry(id));
        } catch (Exception e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in adding tenant with " + "tenant domain: " + tenant.getDomain().toLowerCase()
                    + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }
        return id;
    }


    public void updateTenant(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {
        clearTenantCache(tenant.getId());
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.UPDATE_TENANT_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, tenant.getDomain().toLowerCase());
            prepStmt.setString(2, tenant.getEmail());
            Date createdTime = tenant.getCreatedDate();
            long createdTimeMs;
            if (createdTime == null) {
                createdTimeMs = System.currentTimeMillis();
            } else {
                createdTimeMs = createdTime.getTime();
            }
            prepStmt.setTimestamp(3, new Timestamp(createdTimeMs));
            prepStmt.setInt(4, tenant.getId());

            prepStmt.executeUpdate();

            dbConnection.commit();
        } catch (SQLException e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in updating tenant with " + "tenant domain: "
                    + tenant.getDomain().toLowerCase() + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void updateTenantRealmConfig(org.wso2.carbon.user.api.Tenant tenant) throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt;
            String realmConfigString = null;

            if (tenant.getRealmConfig() != null) {
                realmConfigString = RealmConfigXMLProcessor.serialize(tenant.getRealmConfig()).toString();
                if (realmConfigString != null && realmConfigString.trim().length() > 0) {
                    sqlStmt = TenantConstants.UPDATE_TENANT_CONFIG_SQL;
                    prepStmt = dbConnection.prepareStatement(sqlStmt);
                    InputStream is = null;
                    try {
                        is = new ByteArrayInputStream(realmConfigString.getBytes());
                        prepStmt.setBinaryStream(1, is, is.available());
                        prepStmt.setInt(2, tenant.getId());
                        prepStmt.executeUpdate();
                        dbConnection.commit();
                        clearTenantCache(tenant.getId());
                        RealmCache.getInstance().clearFromCache(tenant.getId(), "primary");
                    } catch (IOException e) {
                        log.error("Error occurs while reading realm configuration", e);
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                log.error(e);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in updating tenant realm configuration with " + "tenant domain: "
                    + tenant.getDomain().toLowerCase() + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    @SuppressWarnings("unchecked")
    public Tenant getTenant(int tenantId) throws UserStoreException {


        TenantCacheEntry<Tenant> entry = tenantCacheManager.getValueFromCache(new TenantIdKey(tenantId));

        if ((entry != null) && (entry.getTenant() != null)) {
            return entry.getTenant();
        }
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        Tenant tenant = null;
        int id;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.GET_TENANT_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);

            result = prepStmt.executeQuery();

            if (result.next()) {
                id = result.getInt(COLUMN_NAME_UM_ID);
                String domain = result.getString(COLUMN_NAME_UM_DOMAIN_NAME);
                String email = result.getString(COLUMN_NAME_UM_EMAIL);
                boolean active = result.getBoolean(COLUMN_NAME_UM_ACTIVE);
                Date createdDate = new Date(result.getTimestamp(
                        COLUMN_NAME_UM_CREATED_DATE).getTime());
                InputStream is = result.getBinaryStream(COLUMN_NAME_UM_USER_CONFIG);

                RealmConfigXMLProcessor processor = new RealmConfigXMLProcessor();
                RealmConfiguration realmConfig = processor.buildTenantRealmConfiguration(is);
                realmConfig.setTenantId(id);

                tenant = new Tenant();
                tenant.setId(id);
                tenant.setDomain(domain);
                tenant.setEmail(email);
                tenant.setCreatedDate(createdDate);
                tenant.setActive(active);
                tenant.setRealmConfig(realmConfig);
                setSecondaryUserStoreConfig(realmConfig, tenantId);
                tenant.setAdminName(realmConfig.getAdminUserName());

                if (log.isDebugEnabled()) {
                    log.debug("Obtained tenant from database for the given tenant ID: " + tenantId
                            + ", hence adding tenant to cache where tenantDomain: {" + domain + "}");
                }
                tenantDomainNameValidation(domain);
                tenantCacheManager.addToCache(new TenantIdKey(id), new TenantCacheEntry<Tenant>(tenant));
                tenantDomainCache.addToCache(new TenantIdKey(id), new TenantDomainEntry(domain));
                tenantIdCache.addToCache(new TenantDomainKey(domain), new TenantIdEntry(id));
            }
            dbConnection.commit();
        } catch (SQLException e) {
            DatabaseUtil.rollBack(dbConnection);
            String msg = "Error in getting the tenant with " + "tenant id: " + tenantId + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }
        return tenant;
    }

    /**
     * TODO : Introduce DTOs
     */
    public Tenant[] getAllTenants() throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        List<Tenant> tenantList = new ArrayList<Tenant>();
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.GET_ALL_TENANTS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            result = prepStmt.executeQuery();

            while (result.next()) {
                int id = result.getInt(COLUMN_NAME_UM_ID);
                String domain = result.getString(COLUMN_NAME_UM_DOMAIN_NAME);
                String email = result.getString(COLUMN_NAME_UM_EMAIL);
                boolean active = result.getBoolean(COLUMN_NAME_UM_ACTIVE);
                Date createdDate = new Date(result.getTimestamp(
                        COLUMN_NAME_UM_CREATED_DATE).getTime());

                Tenant tenant = new Tenant();
                tenant.setId(id);
                tenant.setDomain(domain);
                tenant.setEmail(email);
                tenant.setActive(active);
                tenant.setCreatedDate(createdDate);
                tenantList.add(tenant);
            }
            dbConnection.commit();
        } catch (SQLException e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in getting the tenants.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }
        return tenantList.toArray(new Tenant[tenantList.size()]);
    }

    public TenantSearchResult listTenants(Integer limit, Integer offset, String sortOrder, String sortBy, String filter)
            throws UserStoreException {

        TenantSearchResult tenantSearchResult = new TenantSearchResult();
        String sortedOrder = sortBy + " " + sortOrder;
        try (Connection dbConnection = getDBConnection();
             ResultSet resultSet = getTenantQueryResultSet(dbConnection, sortedOrder, offset, limit)) {
            List<Tenant> tenantList = populateTenantList(resultSet);
            tenantSearchResult.setTenantList(tenantList);
            tenantSearchResult.setTotalTenantCount(getCountOfTenants());
            tenantSearchResult.setLimit(limit);
            tenantSearchResult.setOffSet(offset);
            tenantSearchResult.setSortBy(sortBy);
            tenantSearchResult.setSortOrder(sortOrder);
            tenantSearchResult.setFilter(filter);
            return tenantSearchResult;
        } catch (SQLException e) {
            throw new UserStoreException("Error occurred while listing the tenants.", e);
        }
    }

    public String getDomain(int tenantId) throws UserStoreException {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        } else if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            return null;
        }

        TenantIdKey tenantIdKey = new TenantIdKey(tenantId);
        TenantDomainEntry tenantDomainEntry = tenantDomainCache.getValueFromCache(tenantIdKey);
        if (tenantDomainEntry != null) {
            if (tenantDomainEntry.getTenantDomainName() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Tenant domain from cache: {" + tenantDomainEntry.getTenantDomainName() + "}");
                }
                return tenantDomainEntry.getTenantDomainName().trim();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Tenant domain from cache: {NULL}");
                }
                return tenantDomainEntry.getTenantDomainName();
            }
        }

        String tenantDomain = null;
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.GET_DOMAIN_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);

            result = prepStmt.executeQuery();

            if (result.next()) {
                tenantDomain = result.getString(COLUMN_NAME_UM_DOMAIN_NAME);
            }
            dbConnection.commit();
        } catch (SQLException e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in getting the tenant with " + "tenant id: "
                    + tenantId + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }

        if (tenantDomain != null && !tenantDomain.isEmpty() &&
                tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            if (log.isDebugEnabled()) {
                log.debug("Obtained tenant domain from database, tenantDomain: {" + tenantDomain + "} for the given "
                        + "tenant ID:" + tenantId + ", hence adding tenant domain and tenant ID to cache.");
            }
            tenantDomainNameValidation(tenantDomain);
            tenantDomainCache.addToCache(tenantIdKey, new TenantDomainEntry(tenantDomain));
            tenantIdCache.addToCache(new TenantDomainKey(tenantDomain), new TenantIdEntry(tenantId));
        }

        if (tenantDomain != null) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain from database : {" + tenantDomain + "}");
            }
            return tenantDomain.trim();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain from database : {NULL}");
            }
            return tenantDomain;
        }
    }

    public Tenant[] getAllTenantsForTenantDomainStr(String tenantDomain) throws UserStoreException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        List<Tenant> tenantList = new ArrayList<Tenant>();
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.GET_MATCHING_TENANT_IDS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, "%" + tenantDomain.toLowerCase() + "%");

            result = prepStmt.executeQuery();

            while (result.next()) {
                int id = result.getInt(COLUMN_NAME_UM_ID);
                String domain = result.getString(COLUMN_NAME_UM_DOMAIN_NAME);
                String email = result.getString(COLUMN_NAME_UM_EMAIL);
                boolean active = result.getBoolean(COLUMN_NAME_UM_ACTIVE);
                Date createdDate = new Date(result.getTimestamp(
                        COLUMN_NAME_UM_CREATED_DATE).getTime());

                Tenant tenant = new Tenant();
                tenant.setId(id);
                tenant.setDomain(domain);
                tenant.setEmail(email);
                tenant.setActive(active);
                tenant.setCreatedDate(createdDate);
                tenantList.add(tenant);
            }
            dbConnection.commit();
        } catch (SQLException e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in getting the tenants.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }
        return tenantList.toArray(new Tenant[tenantList.size()]);
    }

    public int getTenantId(String tenantDomain) throws UserStoreException {
        if (tenantDomain != null) {
            tenantDomain = tenantDomain.toLowerCase();
        }

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return MultitenantConstants.SUPER_TENANT_ID;
        } else if (tenantDomain == null) {
            return MultitenantConstants.INVALID_TENANT_ID;
        }
        TenantDomainKey tenantDomainKey = new TenantDomainKey(tenantDomain);
        TenantIdEntry tenantIdEntry = tenantIdCache.getValueFromCache(tenantDomainKey);
        if (tenantIdEntry != null) {
            return tenantIdEntry.getTenantDomainName();
        }

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.GET_TENANT_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, tenantDomain);

            result = prepStmt.executeQuery();

            if (result.next()) {
                tenantId = result.getInt(COLUMN_NAME_UM_ID);
            }
            dbConnection.commit();
            if (tenantDomain != null && !tenantDomain.isEmpty() &&
                    tenantId != MultitenantConstants.INVALID_TENANT_ID) {

                if (log.isDebugEnabled()) {
                    log.debug("Obtained tenant ID: " + tenantId + " from database for the given tenantDomain: {"
                            + tenantDomain + "}, hence adding tenant domain and tenant ID to cache.");
                }
                tenantDomainNameValidation(tenantDomain);
                tenantIdCache.addToCache(tenantDomainKey, new TenantIdEntry(tenantId));
                tenantDomainCache.addToCache(new TenantIdKey(tenantId), new TenantDomainEntry(tenantDomain));
            }
        } catch (SQLException e) {
            DatabaseUtil.rollBack(dbConnection);
            String msg = "Error in getting the tenant id with tenant domain: " + tenantDomain + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }
        return tenantId;
    }

    public Tenant getTenant(String tenantUniqueID) throws UserStoreException {

        TenantCacheEntry<Tenant> entry = tenantUniqueIdCache.getValueFromCache(new TenantUniqueIDKey(tenantUniqueID));

        if ((entry != null) && (entry.getTenant() != null)) {
            return entry.getTenant();
        }
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet result = null;
        Tenant tenant = null;
        int id;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.GET_TENANT_BY_UUID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, tenantUniqueID);

            result = prepStmt.executeQuery();

            if (result.next()) {
                id = result.getInt(COLUMN_NAME_UM_ID);
                String domain = result.getString(COLUMN_NAME_UM_DOMAIN_NAME);
                String email = result.getString(COLUMN_NAME_UM_EMAIL);
                boolean active = result.getBoolean(COLUMN_NAME_UM_ACTIVE);
                Date createdDate = new Date(result.getTimestamp(
                        COLUMN_NAME_UM_CREATED_DATE).getTime());
                InputStream is = result.getBinaryStream(COLUMN_NAME_UM_USER_CONFIG);

                RealmConfigXMLProcessor processor = new RealmConfigXMLProcessor();
                RealmConfiguration realmConfig = processor.buildTenantRealmConfiguration(is);
                realmConfig.setTenantId(id);
                String uniqueId = result.getString(COLUMN_NAME_UM_TENANT_UUID);

                tenant = new Tenant();
                tenant.setTenantUniqueID(uniqueId);
                tenant.setId(id);
                tenant.setDomain(domain);
                tenant.setEmail(email);
                tenant.setCreatedDate(createdDate);
                tenant.setActive(active);
                tenant.setRealmConfig(realmConfig);
                setSecondaryUserStoreConfig(realmConfig, id);
                tenant.setAdminName(realmConfig.getAdminUserName());
                tenant.setAdminUserId(getUserId(realmConfig.getAdminUserName(), id));

                if (log.isDebugEnabled()) {
                    log.debug("Obtained tenant from database for the given UUID: " + uniqueId
                            + ", hence adding tenant to cache where tenantDomain: {" + domain + "}");
                }
                tenantDomainNameValidation(domain);
                tenantUniqueIdCache.addToCache(new TenantUniqueIDKey(uniqueId), new TenantCacheEntry<Tenant>(tenant));
                tenantCacheManager.addToCache(new TenantIdKey(id), new TenantCacheEntry<Tenant>(tenant));
                tenantDomainCache.addToCache(new TenantIdKey(id), new TenantDomainEntry(domain));
                tenantIdCache.addToCache(new TenantDomainKey(domain), new TenantIdEntry(id));
            }
            dbConnection.commit();
        } catch (SQLException e) {
            DatabaseUtil.rollBack(dbConnection);
            String msg = "Error in getting the tenant with " + "tenant UUID: " + tenantUniqueID + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, result, prepStmt);
        }
        return tenant;
    }

    public void activateTenant(int tenantId) throws UserStoreException {

        clearTenantCache(tenantId);
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.ACTIVATE_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            DatabaseUtil.rollBack(dbConnection);
            String msg = "Error in activating the tenant with " + "tenant id: "
                    + tenantId + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void deactivateTenant(int tenantId) throws UserStoreException {

        // Remove tenant information from the cache.
        clearTenantCache(tenantId);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.DEACTIVATE_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in deactivating the tenant with " + "tenant id: "
                    + tenantId + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void activateTenant(String tenantUniqueID) throws UserStoreException {

        // Remove tenant information from the cache.
        clearTenantCaches(tenantUniqueID);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.ACTIVATE_BY_UUID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, tenantUniqueID);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            DatabaseUtil.rollBack(dbConnection);
            String msg = "Error in activating the tenant with tenant UniqueID: " + tenantUniqueID + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public void deactivateTenant(String tenantUniqueID) throws UserStoreException {

        // Remove tenant information from the cache.
        clearTenantCaches(tenantUniqueID);

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = TenantConstants.DEACTIVATE_BY_UUID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, tenantUniqueID);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {

            DatabaseUtil.rollBack(dbConnection);

            String msg = "Error in deactivating the tenant with tenant UniqueID: " + tenantUniqueID + ".";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
    }

    public boolean isTenantActive(int tenantId) throws UserStoreException {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return true;
        } else {
            Tenant tenant = getTenant(tenantId);
            return tenant.isActive();
        }
    }

    /**
     * Delete Tenant
     *
     * @param tenantId - Tenant Id
     * @throws UserStoreException
     */
    public void deleteTenant(int tenantId) throws UserStoreException {
        try {
            deleteTenant(tenantId, true);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    /**
     * Delete Tenant
     *
     * @param tenantId                    - Tenant Id
     * @param removeFromPersistentStorage - Flag to decide weather delete from persistent storage
     * @throws UserStoreException
     */
    public void deleteTenant(int tenantId, boolean removeFromPersistentStorage)
            throws org.wso2.carbon.user.api.UserStoreException {
        // Remove tenant information from the cache.
        String domain = getDomain(tenantId);
        clearTenantCache(tenantId);
        invalidateCacheManager(domain);
        if (removeFromPersistentStorage) {
            Connection dbConnection = null;
            PreparedStatement prepStmt = null;
            try {
                dbConnection = getDBConnection();
                String sqlStmt = TenantConstants.DELETE_TENANT_SQL;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setInt(1, tenantId);

                prepStmt.executeUpdate();
                dbConnection.commit();
            } catch (SQLException e) {
                DatabaseUtil.rollBack(dbConnection);
                String msg = "Error in deleting the tenant with "
                        + "tenant id: " + tenantId + ".";
                if (log.isDebugEnabled()) {
                    log.debug(msg, e);
                }
                throw new UserStoreException(msg, e);
            } finally {
                DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
            }
        }
    }

    private void clearTenantCache(int tenantId) throws UserStoreException {

        String domain = getDomain(tenantId);
        tenantDomainCache.clearCacheEntry(new TenantIdKey(tenantId));
        tenantIdCache.clearCacheEntry(new TenantDomainKey(domain));
        tenantCacheManager.clearCacheEntry(new TenantIdKey(tenantId));
    }

    private void clearTenantCaches(String tenantUniqueID) throws UserStoreException {

        tenantUniqueIdCache.clearCacheEntry(new TenantUniqueIDKey(tenantUniqueID));
        Tenant tenant = this.getTenant(tenantUniqueID);
        int tenantId = tenant.getId();
        tenantDomainCache.clearCacheEntry(new TenantIdKey(tenantId));
        tenantIdCache.clearCacheEntry(new TenantDomainKey(tenant.getDomain()));
        tenantCacheManager.clearCacheEntry(new TenantIdKey(tenantId));
    }

    private void invalidateCacheManager(String domain) {

        CacheManagerFactoryImpl cacheManagerFactory = (CacheManagerFactoryImpl) Caching.getCacheManagerFactory();
        cacheManagerFactory.removeCacheManagerMap(domain);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * @inheritDoc
     */
    public void initializeExistingPartitions() {
        //this method needs not to be implemented in tenant management with JDBC.
    }

    private Connection getDBConnection() throws SQLException {
        Connection dbConnection = DatabaseUtil.getDBConnection(this.dataSource);
        dbConnection.setAutoCommit(false);
        return dbConnection;
    }

    public String getSuperTenantDomain() throws UserStoreException {
        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    /**
     * Read in the secondary user store configurations if available
     *
     * @param realmConfiguration <code>RealmConfiguration</code>
     * @param tenantId           tenant id
     * @throws UserStoreException throws
     */
    private void setSecondaryUserStoreConfig(RealmConfiguration realmConfiguration, int tenantId)
            throws UserStoreException {
        // Get the last realm configuration
        RealmConfiguration lastRealm = realmConfiguration;
        if (realmConfiguration != null) {
            while (lastRealm.getSecondaryRealmConfig() != null) {
                lastRealm = lastRealm.getSecondaryRealmConfig();
            }

            String configPath = CarbonUtils.getCarbonTenantsDirPath() +
                    File.separator + tenantId + File.separator + "userstores";
            File userStores = new File(configPath);
            UserStoreDeploymentManager userStoreDeploymentManager = new UserStoreDeploymentManager();

            File[] files = userStores.listFiles(new FilenameFilter() {
                public boolean accept(File userStores, String name) {
                    return name.toLowerCase().endsWith(".xml");
                }
            });
            if (files != null) {
                for (File file : files) {
                    RealmConfiguration newRealmConfig = userStoreDeploymentManager.
                            getUserStoreConfiguration(file.getAbsolutePath());
                    if (newRealmConfig != null) {
                        lastRealm.setSecondaryRealmConfig(newRealmConfig);
                        lastRealm = lastRealm.getSecondaryRealmConfig();
                    } else {
                        log.error("Error while creating realm configuration from file " + file.getAbsolutePath());
                    }
                }
            }
        }

    }

    /**
     * Check for tenant domain contains any trailing spaces.
     *
     * @param tenantDomain
     */
    private void tenantDomainNameValidation(String tenantDomain) {

        if (tenantDomain.equals(tenantDomain.trim())) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain doesn't contain any trailing white spaces, tenantDomain: {" + tenantDomain
                        + "}");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain contains trailing white spaces, tenantDomain: {" + tenantDomain + "}, "
                        + "current stack trace: \n" + printCurrentStackTrace().toString());
            }
        }
    }

    /**
     * Print current stack trace.
     */
    private StringBuilder printCurrentStackTrace() {

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StringBuilder currentStackTraceBuilder = new StringBuilder();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            currentStackTraceBuilder.append("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName()
                    + ":" + s.getLineNumber() + ") \n");
        }
        return currentStackTraceBuilder;
    }

    /**
     * Get total number of tenant existing in the system.
     *
     * @return number of tenant count.
     * @throws UserStoreException Error when getting count of tenants.
     */
    private int getCountOfTenants() throws UserStoreException {

        String sqlStmt = TenantConstants.LIST_TENANTS_COUNT_SQL;
        int tenantCount = 0;
        try (Connection dbConnection = getDBConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    tenantCount = Integer.parseInt(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new UserStoreException("Error occurred while retrieving tenant count.", e);
        }
        return tenantCount;
    }

    private ResultSet getTenantQueryResultSet(Connection dbConnection, String sortedOrder, Integer offset,
                                              Integer limit) throws SQLException, UserStoreException {

        String dbType = dbConnection.getMetaData().getDatabaseProductName();
        PreparedStatement prepStmt;
        String sqlQuery;
        String sqlTail;
        sqlQuery = TenantConstants.LIST_TENANTS_PAGINATED_SQL;

        if (dbType.contains("MySQL") || dbType.contains("H2")) {
            sqlTail = String.format(TenantConstants.LIST_TENANTS_MYSQL_TAIL, sortedOrder);
            sqlQuery = sqlQuery + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, offset);
            prepStmt.setInt(2, limit);
        } else if (dbType.contains("oracle")) {
            sqlQuery = TenantConstants.LIST_TENANTS_PAGINATED_ORACLE;
            sqlTail = String.format(TenantConstants.LIST_TENANTS_ORACLE_TAIL, sortedOrder);
            sqlQuery = sqlQuery + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, offset + limit);
            prepStmt.setInt(2, offset);
        } else if (dbType.contains("Microsoft")) {
            sqlTail = String.format(TenantConstants.LIST_TENANTS_MSSQL_TAIL, sortedOrder);
            sqlQuery = sqlQuery + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, offset);
            prepStmt.setInt(2, limit);
        } else if (dbType.contains("db2") || dbType.contains("DB2")) {
            sqlQuery = TenantConstants.LIST_TENANTS_PAGINATED_DB2;
            sqlTail = String.format(TenantConstants.LIST_TENANTS_DB2_TAIL, sortedOrder);
            sqlQuery = sqlQuery + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, offset + 1);
            prepStmt.setInt(2, offset + limit);
        } else if (dbType.contains("PostgreSQL")) {
            sqlTail = String.format(TenantConstants.LIST_TENANTS_POSTGRESQL_TAIL, sortedOrder);
            sqlQuery = sqlQuery + sqlTail;
            prepStmt = dbConnection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, limit);
            prepStmt.setInt(2, offset);
        } else {
            String message = "Error while loading tenant from DB: Database driver could not be identified" +
                    " or not supported.";
            log.error(message);
            throw new UserStoreException(message);
        }
        return prepStmt.executeQuery();
    }

    private List<Tenant> populateTenantList(ResultSet resultSet)
            throws SQLException, UserStoreException {

        List<Tenant> tenantList = new ArrayList<Tenant>();
        while (resultSet.next()) {
            int id = resultSet.getInt(COLUMN_NAME_UM_ID);
            String domain = resultSet.getString(COLUMN_NAME_UM_DOMAIN_NAME);
            String email = resultSet.getString(COLUMN_NAME_UM_EMAIL);
            boolean active = resultSet.getBoolean(COLUMN_NAME_UM_ACTIVE);
            Date createdDate = new Date(resultSet.getTimestamp(
                    COLUMN_NAME_UM_CREATED_DATE).getTime());
            String tenantUniqueId = resultSet.getString(COLUMN_NAME_UM_TENANT_UUID);

            InputStream is = resultSet.getBinaryStream(COLUMN_NAME_UM_USER_CONFIG);

            RealmConfigXMLProcessor processor = new RealmConfigXMLProcessor();
            RealmConfiguration realmConfig = processor.buildTenantRealmConfiguration(is);

            Tenant tenant = new Tenant();
            tenant.setId(id);
            tenant.setDomain(domain);
            tenant.setEmail(email);
            tenant.setActive(active);
            tenant.setTenantUniqueID(tenantUniqueId);
            tenant.setCreatedDate(createdDate);
            String adminUserName = realmConfig.getAdminUserName();
            tenant.setAdminName(adminUserName);
            tenant.setAdminUserId(getUserId(adminUserName, id));
            tenantList.add(tenant);
        }
        return tenantList;
    }

    private String getUserId(String userName, int tenantId) throws UserStoreException {

        String claimValue = null;
        RealmService realmService = UserStoreMgtDSComponent.getRealmService();
        try {
            UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);
            if (tenantUserRealm != null) {
                UserStoreManager userStoreManager = (UserStoreManager) tenantUserRealm.getUserStoreManager();
                if (userStoreManager != null) {
                    claimValue = userStoreManager.getUserClaimValue(userName, UserCoreClaimConstants.USER_ID_CLAIM_URI,
                            UserCoreConstants.DEFAULT_PROFILE);
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException("Error while getting claim value for the claim: " +
                    UserCoreClaimConstants.USER_ID_CLAIM_URI, e);
        }
        return claimValue;
    }
}
