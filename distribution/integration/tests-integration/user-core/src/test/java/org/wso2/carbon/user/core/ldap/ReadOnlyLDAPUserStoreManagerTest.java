/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Integration test for ReadOnlyLDAPUserStoreManager.
 * Uses embedded LDAP to check the functionality of the class.
 *
 */
public class ReadOnlyLDAPUserStoreManagerTest {

    private static final Log log = LogFactory.getLog(ReadOnlyLDAPUserStoreManagerTest.class);

    private static final String MOBILE_CLAIM = "www.wso2.org/mobile";
    private static final String NAME_CLAIM = "www.wso2.org/name";
    private static final String SNAME_CLAIM = "www.wso2.org/surname";
    private static final String U_NAME_CLAIM = "www.wso2.org/uName";
    private static final String MOBILE_ATTR = "mobile";
    private static final String CN_ATTR = "cn";
    private static final String SNAME_ATTR = "sn";
    private static final String U_NAME_ATTR = "uName";
    private static final String USER_NAME_1 = "LName1";
    private static final String USER_NAME_2 = "LName2";
    private static final String USER_UPDATED_LAST_NAME = "LNameX";
    private static final String SEARCH_BASE = "ou=Users,dc=WSO2,dc=ORG";
    private static final int LDAP_SERVER_PORT = 12389;
    private static final int TEST_TENANT_1 = 10001;
    private static final String TEST_TENANT_DOMAIN_1 = "" + TEST_TENANT_1;

    private JdbcDataSource dataSource;
    private LDAPConnectionContext ldapConnectionContext;
    private ClaimManager claimManager;
    private Map<String, String> claimMap = new HashMap<String, String>();
    private TestDirectoryServer testDirectoryServer;

    @AfterSuite
    public void tearDown() throws UserStoreException {
        try {
            clearTestUsers();
        } finally {
            testDirectoryServer.stopLdapService();
        }
    }

    @BeforeSuite
    public void setUp() throws Exception {
        System.setProperty("carbon.home", ReadOnlyLDAPUserStoreManagerTest.class.getResource("/").getFile());
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        dataSource = createDatasource();
        ldapConnectionContext = new LDAPConnectionContext(getRealmConfiguration());
        claimManager = Mockito.mock(ClaimManager.class);

        claimMap.put(MOBILE_CLAIM, MOBILE_ATTR);
        claimMap.put(NAME_CLAIM, CN_ATTR);
        claimMap.put(SNAME_CLAIM, SNAME_ATTR);
        claimMap.put(U_NAME_CLAIM, U_NAME_ATTR);

        Mockito.doAnswer(new Answer() {

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return claimMap.get(invocationOnMock.getArguments()[0]);
            }
        }).when(claimManager).getAttributeName(Matchers.anyString());
        Mockito.doAnswer(new Answer() {

            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ClaimMapping result = new ClaimMapping();
                result.setMappedAttribute(claimMap.get(invocationOnMock.getArguments()[0]));
                return result;
            }
        }).when(claimManager).getClaimMapping(Matchers.anyString());

        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.doReturn(TEST_TENANT_DOMAIN_1).when(tenantManager).getDomain(Matchers.anyInt());

        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.doReturn(tenantManager).when(realmService).getTenantManager();

        Method setRealmServiceMethod = UserStoreMgtDSComponent.class
                .getDeclaredMethod("setRealmService", RealmService.class);
        setRealmServiceMethod.setAccessible(true);
        setRealmServiceMethod.invoke(new UserStoreMgtDSComponent(), realmService);

        testDirectoryServer = new TestDirectoryServer();
        testDirectoryServer.startLdapServer(LDAP_SERVER_PORT);

        clearTestUsers();
    }

    @Test
    public void testGetUserPropertyValues_Caching()
            throws UserStoreException, NamingException, IOException, MalformedObjectNameException,
            InstanceNotFoundException {
        ReadOnlyLDAPUserStoreManager readOnlyLDAPUserStoreManager1 = getReadOnlyLDAPUserStoreManager();
        ReadWriteLDAPUserStoreManager readWriteLDAPUserStoreManager1 = getReadWriteDAPUserStoreManager();

        Map<String, String> claims = new HashMap<String, String>();
        claims.put(SNAME_CLAIM, USER_UPDATED_LAST_NAME);
        claims.put(U_NAME_CLAIM, USER_NAME_1);

        startTenantFlow(TEST_TENANT_1, TEST_TENANT_DOMAIN_1);
        if (!readWriteLDAPUserStoreManager1.isExistingUser(USER_NAME_1)) {
            readWriteLDAPUserStoreManager1.doAddUser(USER_NAME_1, "test1", new String[0], claims, "defult");
        }

        boolean isAuthenticated = false;
        try {
            rename(USER_NAME_1, USER_NAME_2);
        } catch (Exception e) {
            log.error("Error in renaming LDAP user " + USER_NAME_1, e);
            //Ignore any exception.
        }

        // DoAuthenticate should cause the user to be cached.
        isAuthenticated = readOnlyLDAPUserStoreManager1.doAuthenticate(USER_UPDATED_LAST_NAME, "test1");
        Assert.assertTrue(isAuthenticated);

        Map<String, String> userProps = readOnlyLDAPUserStoreManager1
                .getUserPropertyValues(USER_NAME_1, new String[] { CN_ATTR }, "default");
        Assert.assertNotNull(userProps);

        // Rename the user back
        rename(USER_NAME_2, USER_NAME_1);

        try {
            readOnlyLDAPUserStoreManager1.getUserClaimValue(USER_UPDATED_LAST_NAME, SNAME_CLAIM, "default");
            Assert.fail("The user search should fail, due to cache holding previous DN for the user, "
                    + "and a UserStoreException should have been thrown.");
        } catch (UserStoreException expectedException) {
            //This exception expected due to cache.
        }

        clearCache(readOnlyLDAPUserStoreManager1);

        String lastName = readOnlyLDAPUserStoreManager1
                .getUserClaimValue(USER_UPDATED_LAST_NAME, SNAME_CLAIM, "default");
        Assert.assertNotNull(lastName);
        PrivilegedCarbonContext.endTenantFlow();
    }

    private void clearCache(ReadOnlyLDAPUserStoreManager readOnlyLDAPUserStoreManager1) {
        final String cacheManagerName = "UserCacheManager";
        final String userCacheNamePrefix = CachingConstants.LOCAL_CACHE_PREFIX + "UserCache-";
        try {
            startTenantFlow(TEST_TENANT_1, TEST_TENANT_DOMAIN_1);
            CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(cacheManagerName);
            Cache userCache = cacheManager.getCache(userCacheNamePrefix + readOnlyLDAPUserStoreManager1.hashCode());
            userCache.removeAll();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    private void rename(String fromUid, String toUid) throws UserStoreException, NamingException {
        final String searchBase = SEARCH_BASE;
        DirContext mainDirContext = this.ldapConnectionContext.getContext();
        mainDirContext.rename("uid=" + fromUid + "," + SEARCH_BASE, "uid=" + toUid + "," + SEARCH_BASE);
        DirContext subDirContext = (DirContext) mainDirContext.lookup(searchBase);
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(null);
        NamingEnumeration<SearchResult> returnedResultList = mainDirContext
                .search(searchBase, "(&(objectClass=person)(uid=" + toUid + "))", searchControls);

        if (returnedResultList.hasMore()) {
            SearchResult result = returnedResultList.next();
            Attributes updatedAttributes = new BasicAttributes(true);
            Attribute currentUpdatedAttribute = new BasicAttribute("cn");
            currentUpdatedAttribute.add(toUid);
            updatedAttributes.put(currentUpdatedAttribute);
            subDirContext.modifyAttributes(result.getName(), DirContext.REPLACE_ATTRIBUTE, updatedAttributes);
        }
    }

    private ReadWriteLDAPUserStoreManager getReadWriteDAPUserStoreManager() throws UserStoreException {
        RealmConfiguration realmConfig = getRealmConfiguration();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);
        properties.put(UserCoreConstants.FIRST_STARTUP_CHECK, false);

        return new ReadWriteLDAPUserStoreManager(realmConfig, properties, claimManager, null, null, TEST_TENANT_1);
    }

    private ReadOnlyLDAPUserStoreManager getReadOnlyLDAPUserStoreManager() throws UserStoreException {
        RealmConfiguration realmConfig = getRealmConfiguration();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(UserCoreConstants.DATA_SOURCE, dataSource);

        return new ReadOnlyLDAPUserStoreManager(realmConfig, properties, claimManager, null, null, TEST_TENANT_1);
    }

    private RealmConfiguration getRealmConfiguration() {
        RealmConfiguration realmConfig = new RealmConfiguration();
        realmConfig.getUserStoreProperties().put(LDAPConstants.CONNECTION_URL, "ldap://localhost:" + LDAP_SERVER_PORT);
        realmConfig.getUserStoreProperties().put(LDAPConstants.DNS_URL, null);
        realmConfig.getUserStoreProperties().put("AnonymousBind", "false");
        realmConfig.getUserStoreProperties().put(LDAPConstants.CONNECTION_NAME, "uid=admin,ou=system");
        realmConfig.getUserStoreProperties().put(LDAPConstants.CONNECTION_PASSWORD, "secret");
        realmConfig.getUserStoreProperties().put(LDAPConstants.USER_SEARCH_BASE, SEARCH_BASE);
        realmConfig.getUserStoreProperties().put(LDAPConstants.USER_NAME_LIST_FILTER, "(objectClass=person)");
        realmConfig.getUserStoreProperties()
                .put(LDAPConstants.USER_NAME_SEARCH_FILTER, "(&(objectClass=person)(sn=?))");
        realmConfig.getUserStoreProperties().put(LDAPConstants.USER_NAME_ATTRIBUTE, "uid");
        realmConfig.getUserStoreProperties().put(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED, "true");
        realmConfig.getUserStoreProperties().put(LDAPConstants.GROUP_SEARCH_BASE, "ou=Groups,dc=wso2,dc=org");
        realmConfig.getUserStoreProperties().put(LDAPConstants.GROUP_NAME_LIST_FILTER, "ou=Groups,dc=wso2,dc=org");
        realmConfig.getUserStoreProperties().put(LDAPConstants.ROLE_NAME_FILTER, "(&(objectClass=groupOfNames)(cn=?))");
        realmConfig.getUserStoreProperties().put(LDAPConstants.GROUP_NAME_ATTRIBUTE, "cn");
        realmConfig.getUserStoreProperties().put(LDAPConstants.MEMBERSHIP_ATTRIBUTE, "member");
        realmConfig.getUserStoreProperties().put(LDAPConstants.USER_ENTRY_OBJECT_CLASS, "identityPerson");
        realmConfig.getUserStoreProperties().put(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS, "groupOfNames");
        return realmConfig;
    }

    private JdbcDataSource createDatasource() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");

        String dbScript = this.getClass().getResource("/dbscripts/h2-um.sql").getFile();
        loadData(dataSource, dbScript);
        return dataSource;
    }

    private void loadData(JdbcDataSource ds, String location) throws Exception {
        File file = new File(location);

        final String LOAD_DATA_QUERY = "RUNSCRIPT FROM '" + file.getCanonicalPath() + "'";

        Connection connection = null;
        connection = ds.getConnection();
        Statement statement = connection.createStatement();
        statement.execute(LOAD_DATA_QUERY);
        connection.close();
    }

    private void clearTestUsers() throws UserStoreException {
        deleteUser(USER_NAME_1);
        deleteUser(USER_NAME_2);
    }

    private void deleteUser(String userId) throws UserStoreException {
        DirContext mainDirContext = this.ldapConnectionContext.getContext();
        try {
            mainDirContext.destroySubcontext("uid=" + userId + "," + SEARCH_BASE);
        } catch (NamingException e) {
            log.error("Error in deleting user : " + userId, e);
        }
    }

    /**
     * Common utility method to start the Super tenant flow.
     */
    private void startTenantFlow(int tenantId, String tenantDomain) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomain);
    }
}