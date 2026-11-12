/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.jdbc;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.UserIdResolverCache;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME;

/**
 * Utilities for the JDBC user store manager tests.
 */
public class JDBCUserStoreTestUtil {

    public static final String GROUP_NAME = "largeUserListGroup";
    public static final String INTERNAL_ROLE_NAME = "Internal/largeUserListRole";
    public static final String USER_NAME_PREFIX = "largeUserListUser";
    public static final String GIVEN_NAME_PREFIX = "givenName";
    public static final String PASSWORD = "pass1";
    // Exceeds the IN clause limit of InClauseLimitedDriver.
    public static final int USER_COUNT = 1500;
    private static final String CARBON_HOME = "src/test/resources/dbscripts/group_uuid_disable";

    private static String previousCarbonHome;
    private static Boolean previousLegacyAuthzRuntime;

    /**
     * Set up the carbon context of the super tenant.
     */
    public static void setUpCarbonContext() {

        previousCarbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        previousLegacyAuthzRuntime = CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME;
        setCarbonHome(new File(CARBON_HOME).getAbsolutePath());
        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = true;
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        clearUserIdResolverCache();
    }

    /**
     * Restore the state changed by {@link #setUpCarbonContext()}.
     */
    public static void tearDownCarbonContext() {

        DatabaseUtil.closeDatabasePoolConnection();
        clearUserIdResolverCache();
        setCarbonHome(previousCarbonHome);
        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = previousLegacyAuthzRuntime;
    }

    /**
     * Create a user store manager on a new H2 database.
     *
     * @param userMgtXml    Realm configuration file.
     * @param dbFolder      Folder of the database.
     * @param dbScriptsHome Folder that contains the database scripts.
     * @return User store manager.
     * @throws Exception If the database cannot be created; thrown by {@link DatabaseCreator}.
     */
    public static AbstractUserStoreManager initUserStoreManager(String userMgtXml, String dbFolder,
                                                                String dbScriptsHome) throws Exception {

        deleteDir(new File(dbFolder));
        String dbUrl = "jdbc:h2:./" + dbFolder + "/CARBON_TEST";
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(dbUrl);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        // DatabaseCreator reads the scripts from the carbon home.
        setCarbonHome(new File(dbScriptsHome).getAbsolutePath());
        try {
            new DatabaseCreator(ds).createRegistryDatabase();
        } finally {
            setCarbonHome(carbonHome);
            ds.close();
        }

        DatabaseUtil.closeDatabasePoolConnection();
        RealmConfiguration realmConfig;
        try (InputStream inStream = JDBCUserStoreTestUtil.class.getClassLoader().getResourceAsStream(userMgtXml)) {
            realmConfig = TestRealmConfigBuilder.buildRealmConfigWithJDBCConnectionUrl(inStream, dbUrl);
        }
        realmConfig.getRealmProperties().put(JDBCRealmConstants.DRIVER_NAME, InClauseLimitedDriver.class.getName());
        UserRealm realm = new DefaultRealm();
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil.getProfileTestData(),
                MultitenantConstants.SUPER_TENANT_ID);
        // Let the next realm create its own data source.
        DatabaseUtil.closeDatabasePoolConnection();
        return (AbstractUserStoreManager) realm.getUserStoreManager();
    }

    private static void setCarbonHome(String carbonHome) {

        if (carbonHome == null) {
            System.clearProperty(ServerConstants.CARBON_HOME);
        } else {
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
        }
    }

    private static void deleteDir(File dir) {

        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteDir(child);
            }
        }
        dir.delete();
    }

    private static void clearUserIdResolverCache() {

        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
        UserIdResolverCache.getInstance()
                .clear(RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME, MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * H2 driver that rejects an IN clause with more than 1000 values, as Oracle does.
     */
    public static class InClauseLimitedDriver extends org.h2.Driver {

        private static final int MAX_IN_CLAUSE_PARAMETER_COUNT = 1000;
        private static final Pattern IN_CLAUSE_PATTERN = Pattern.compile("\\bIN\\s*\\(", Pattern.CASE_INSENSITIVE);

        @Override
        public Connection connect(String url, Properties info) throws SQLException {

            Connection connection = super.connect(url, info);
            if (connection == null) {
                return null;
            }
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class[]{Connection.class}, (proxy, method, args) -> {
                        if ("prepareStatement".equals(method.getName())) {
                            validateInClauseParameterCount((String) args[0]);
                        }
                        try {
                            return method.invoke(connection, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }

        private static void validateInClauseParameterCount(String sql) throws SQLException {

            Matcher matcher = IN_CLAUSE_PATTERN.matcher(sql);
            while (matcher.find()) {
                int parameterCount = 0;
                int depth = 1;
                for (int i = matcher.end(); i < sql.length() && depth > 0; i++) {
                    char character = sql.charAt(i);
                    if (character == '(') {
                        depth++;
                    } else if (character == ')') {
                        depth--;
                    } else if (character == '?') {
                        parameterCount++;
                    }
                }
                if (parameterCount > MAX_IN_CLAUSE_PARAMETER_COUNT) {
                    throw new SQLException("ORA-01795: maximum number of expressions in a list is "
                            + MAX_IN_CLAUSE_PARAMETER_COUNT);
                }
            }
        }
    }
}
