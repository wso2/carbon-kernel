/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.jdbc;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JDBCRealmSecondaryUserStoreTest extends BaseTestCase {

    public static final String JDBC_TEST_USERMGT_XML = "user-mgt-test.xml";

    private static String TEST_URL = "jdbc:h2:./target/JDBCRealmSecondaryUserStoreTest/CARBON_TEST";
    private AbstractUserStoreManager admin = null;
    private static String userId1;
    private static String userId2;

    public void setUp() throws Exception {

        super.setUp();
        DatabaseUtil.closeDatabasePoolConnection();
        initRealmStuff(TEST_URL);
        DatabaseUtil.closeDatabasePoolConnection();
    }

    public void initRealmStuff(String dbUrl) throws Exception {

        String dbFolder = "target/JDBCRealmSecondaryUserStoreTest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(dbUrl);
        DatabaseCreator creator = new DatabaseCreator(ds);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String resourcesPath = new File("src/test/resources").getAbsolutePath();
        System.setProperty(ServerConstants.CARBON_HOME, resourcesPath);
        creator.createRegistryDatabase();
        System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
        UserRealm realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader().getResource(JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), MultitenantConstants.SUPER_TENANT_ID);
        admin = (AbstractUserStoreManager) realm.getUserStoreManager();
        addSecondaryUserStoreManager(realmConfig, admin, realm);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUserRealm(realm);
        ds.close();
    }

    public void test100AdRole() throws UserStoreException {

        admin.addRole("SECONDARYJDBC/role1", null, null);
        admin.addRole("SECONDARYJDBC/role3", null, null);
        admin.addRole("SECONDARYJDBC/role4", null, null);
        admin.addRole("role5", null, null);
        //admin, Internal/everyone, role1, role3, role4
        Assert.assertEquals(6, admin.getRoleNames().length);
        assertTrue(ArrayUtils.contains(admin.getRoleNames(), "SECONDARYJDBC/role1"));
    }

    public void test101AddUser() throws UserStoreException {

        admin.addUser("SECONDARYJDBC/user2", "pass2", null, null,
                null, false);
        admin.addUser("SECONDARYJDBC/user3", "pass3", null, null,
                null, false);
        admin.addUser("user4", "pass4", null, null, null,
                false);
        //  user2,user3,user4 + admin
        Assert.assertEquals(4, admin.listUsers("*", 100).length);
        assertTrue(ArrayUtils.contains(admin.listUsers("*", 100), "SECONDARYJDBC/user2"));
    }

    public void test102AdRoleWithUser() throws UserStoreException {

        admin.addRole("SECONDARYJDBC/role2", new String[]{"SECONDARYJDBC/user2"}, null);
        //admin, Internal/everyone, role1, role2, role3, role4
        Assert.assertEquals(7, admin.getRoleNames().length);
    }

    public void test103AddUserWithRole() throws UserStoreException {

        admin.addUser("SECONDARYJDBC/user1", "pass1", new String[]{"role1"}, null,
                null, false);
        //  user1,user2,user3,user4 + admin
        Assert.assertEquals(5, admin.listUsers("*", 100).length);
    }

    public void test104Authenticate() throws UserStoreException {
        //Add a primary user with the same name.
        admin.addUser("user1", "primaryuser1", null, null,
                null, false);
        //Test authenticate with SECONDARYJDBC user store credentials.
        assertTrue(admin.authenticate("user1", "pass1"));
        assertTrue(admin.authenticate("SECONDARYJDBC/user1", "pass1"));

        //Test authenticate with primary user store credentials.
        assertTrue(admin.authenticate("user1", "primaryuser1"));
        assertFalse(admin.authenticate("SECONDARYJDBC/user1", "primaryuser1"));
    }

    public void test105UpdateCredential() throws UserStoreException {

        admin.updateCredential("SECONDARYJDBC/user1", "pass11", "pass1");
        assertFalse(admin.authenticate("user1", "pass1"));
        assertTrue(admin.authenticate("user1", "pass11"));
    }

    public void test106UpdateCredentialByAdmin() throws UserStoreException {

        admin.updateCredentialByAdmin("SECONDARYJDBC/user2", "pass22");
        assertFalse(admin.authenticate("user2", "pass2"));
        assertTrue(admin.authenticate("user2", "pass22"));
    }

    public void test107SetUserClaimValueInDefaultProfile() throws UserStoreException {

        admin.setUserClaimValue("SECONDARYJDBC/user1", ClaimTestUtil.CLAIM_URI1,
                "usergivenname1", null);
        assertEquals("usergivenname1", admin.getUserClaimValue("SECONDARYJDBC/user1",
                ClaimTestUtil.CLAIM_URI1, null));
    }

    public void test108SetUserClaimValuesInDefaultProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in default profile
        Map<String, String> map = new HashMap<>();
        map.put(ClaimTestUtil.CLAIM_URI1, "usergivenname2");
        map.put(ClaimTestUtil.CLAIM_URI3, "usergivenname3");

        admin.setUserClaimValues("SECONDARYJDBC/user2", map, null);

        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};

        Map<String, String> obtained = admin.getUserClaimValues("SECONDARYJDBC/user2", allClaims, null);
        assertEquals("usergivenname2", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname3", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));
    }

    public void test109SetUserClaimValueInCustomProfile() throws UserStoreException {

        //Test Set/Get User Claim Values in home profile
        admin.setUserClaimValue("SECONDARYJDBC/user1", ClaimTestUtil.CLAIM_URI1, "usergivenname1_home",
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("usergivenname1_home", admin.getUserClaimValue("SECONDARYJDBC/user1",
                ClaimTestUtil.CLAIM_URI1,
                ClaimTestUtil.HOME_PROFILE_NAME));
        assertEquals("usergivenname1", admin.getUserClaimValue("SECONDARYJDBC/user1",
                ClaimTestUtil.CLAIM_URI1, null));
    }

    public void test110SetUserClaimValuesInCustomProfile() throws UserStoreException {

        Map<String, String> map = new HashMap<>();
        map.put(ClaimTestUtil.CLAIM_URI1, "usergivenname2_home");
        map.put(ClaimTestUtil.CLAIM_URI3, "usergivenname3_home");

        admin.setUserClaimValues("SECONDARYJDBC/user2", map, ClaimTestUtil.HOME_PROFILE_NAME);
        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};
        Map<String, String> obtained = admin.getUserClaimValues("SECONDARYJDBC/user2", allClaims,
                ClaimTestUtil.HOME_PROFILE_NAME);
        assertEquals("usergivenname2_home", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname3_home", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));

        obtained = admin.getUserClaimValues("SECONDARYJDBC/user2", allClaims, null);
        assertEquals("usergivenname2", obtained.get(ClaimTestUtil.CLAIM_URI1));
        assertEquals("usergivenname3", obtained.get(ClaimTestUtil.CLAIM_URI3));
        assertNull(obtained.get(ClaimTestUtil.CLAIM_URI2));

        // With the username claim in default profile.
        assertEquals(3, admin.getUserClaimValues("SECONDARYJDBC/user2", null).length);
        assertEquals(2, admin.getUserClaimValues("SECONDARYJDBC/user2", ClaimTestUtil.HOME_PROFILE_NAME).length);
    }

    public void test112GetUserListInDefaultProfile() throws UserStoreException {

        assertEquals(1, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "usergivenname2",
                null).length);
    }

    public void test113GetUserListInCustomProfile() throws UserStoreException {

        assertEquals(1, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "usergivenname2_home",
                ClaimTestUtil.HOME_PROFILE_NAME).length);
    }

    public void test114GetUserListWithPagination() throws UserStoreException {

        assertEquals(1, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "SECONDARYJDBC/usergivenname2",
                null, 10, 1).length);
        assertEquals(0, admin.getUserList(ClaimTestUtil.CLAIM_URI1, "SECONDARYJDBC/usergivenname2",
                null, 10, 2).length);
    }

    public void test115GetUserListWithCondition() throws UserStoreException {

        ExpressionCondition expressionCondition = new ExpressionCondition(ExpressionOperation.EQ.toString(),
                ExpressionAttribute.USERNAME.toString(), "user2");
        assertEquals(1, admin.getUserList(expressionCondition, "SECONDARYJDBC", null,
                10, 0, null, null).length);
        assertEquals(0, admin.getUserList(expressionCondition, "SECONDARYJDBC", null,
                10, 2, null, null).length);
    }

    public void test116GetRoleListOfUser() throws UserStoreException {

        assertEquals(2, admin.getRoleListOfUser("SECONDARYJDBC/user1").length);
    }

    public void test117UpdateRoleListOfUser() throws UserStoreException {

        admin.updateRoleListOfUser("SECONDARYJDBC/user1", null, new String[]{"role2"});
        admin.updateRoleListOfUser("SECONDARYJDBC/user1", new String[]{"role2"}, new String[]{"role4",
                "role3"});

        String[] rolesOfUser = admin.getRoleListOfUser("SECONDARYJDBC/user1");
        assertEquals(4, rolesOfUser.length);

        admin.updateRoleListOfUser("SECONDARYJDBC/user1", new String[]{"role3"}, null);

        String[] rolesOfUserNew = admin.getRoleListOfUser("SECONDARYJDBC/user1");
        assertEquals(3, rolesOfUserNew.length);

        //negative
        try {
            admin.updateRoleListOfUser("SECONDARYJDBC/user1", new String[]{"role2"}, new String[]{"role4",
                    "no_role1"});
        } catch (UserStoreException e) {
            // Expected
            assertEquals("The role: no_role1 does not exist.", e.getMessage());
        }
    }

    public void test118UpdateUserListOfRole() throws UserStoreException {

        admin.updateUserListOfRole("SECONDARYJDBC/role2", new String[]{"user1"}, null);
        admin.updateUserListOfRole("SECONDARYJDBC/role3", null, new String[]{"user1", "user2"});
        //negative
        try {
            admin.updateUserListOfRole("SECONDARYJDBC/role3", null, new String[]{"nouser1", "nouser2"});
        } catch (UserStoreException e) {
            // Expected
            // TODO: 1/8/20 Need to fix
//            assertEquals("User nouser1 does not exit in the system.", e.getMessage());
        }
        String[] users = admin.getUserListOfRole("SECONDARYJDBC/role3");
        assertEquals(2, users.length);

        admin.updateUserListOfRole("SECONDARYJDBC/role3", new String[]{"user1"}, null);

        String[] usersNew = admin.getUserListOfRole("SECONDARYJDBC/role3");
        assertEquals(1, usersNew.length);
    }

    public void test119IsUserInRole() throws UserStoreException {

        assertFalse(admin.isUserInRole("SECONDARYJDBC/user1", "role3"));
    }

    public void test120IsUserExists() throws UserStoreException {

        assertTrue(admin.isExistingUser("SECONDARYJDBC/user2"));
        assertFalse(admin.isExistingUser("SECONDARYJDBC/no-user2"));
    }

    public void test121IsRoleExists() throws UserStoreException {

        assertTrue(admin.isExistingRole("SECONDARYJDBC/role3"));
        assertFalse(admin.isExistingRole("SECONDARYJDBC/no-role3"));
    }

    public void test122GetUsersClaimValues() throws UserStoreException {

        String[] allClaims = {ClaimTestUtil.CLAIM_URI1, ClaimTestUtil.CLAIM_URI2,
                ClaimTestUtil.CLAIM_URI3};

        UserClaimSearchEntry[] obtained = admin.getUsersClaimValues(new String[]{"SECONDARYJDBC/user1", "SECONDARYJDBC/user2"},
                allClaims, null);

        assertEquals(2, obtained.length);
        assertNotNull(obtained[0].getClaims().get("http://wso2.org/givenname"));
        assertNotNull(obtained[1].getClaims().get("http://wso2.org/givenname"));
    }

    public void test123UpdateRoleName() throws UserStoreException {

        String[] usersBefore = admin.getUserListOfRole("SECONDARYJDBC/role3");
        admin.updateRoleName("SECONDARYJDBC/role3", "SECONDARYJDBC/newrole3");
        String[] usersAfter = admin.getUserListOfRole("SECONDARYJDBC/newrole3");
        assertEquals(usersBefore.length, usersAfter.length);
    }

    public void test124DeleteUserClaimValue() throws UserStoreException {

        assertNotNull(admin.getUserClaimValue("SECONDARYJDBC/user1",
                "http://wso2.org/givenname", null));
        admin.deleteUserClaimValue("SECONDARYJDBC/user1",
                "http://wso2.org/givenname", null);
        assertNull(admin.getUserClaimValue("SECONDARYJDBC/user1",
                "http://wso2.org/givenname", null));
    }

    public void test125DeleteUserClaimValues() throws UserStoreException {

        assertNotNull(admin.getUserClaimValue("SECONDARYJDBC/user2", "http://wso2.org/givenname3", null));
        admin.deleteUserClaimValues("SECONDARYJDBC/user2", new String[]{"http://wso2.org/givenname3"}, null);
        assertNull(admin.getUserClaimValue("SECONDARYJDBC/user2", "http://wso2.org/givenname3", null));
    }

    public void test126DeleteRole() throws UserStoreException {

        String[] users = admin.getUserListOfRole("SECONDARYJDBC/role1");
        for (String user : users) {
            assertTrue(ArrayUtils.contains(admin.getRoleListOfUser(user), "SECONDARYJDBC/role1"));
        }
        admin.deleteRole("SECONDARYJDBC/role1");
        assertFalse(ArrayUtils.contains(admin.getRoleNames(), "SECONDARYJDBC/role1"));

        for (String user : users) {
            assertFalse(ArrayUtils.contains(admin.getRoleListOfUser(user), "SECONDARYJDBC/role1"));
        }
    }

    public void test176DeleteUser() throws UserStoreException {

        String[] roles = admin.getRoleListOfUser("SECONDARYJDBC/user1");
        for (String role : roles) {
            assertTrue(ArrayUtils.contains(admin.getUserListOfRole(role), "SECONDARYJDBC/user1"));
        }
        admin.deleteUser("SECONDARYJDBC/user1");
        assertFalse(ArrayUtils.contains(admin.listUsers("*", 100), "SECONDARYJDBC/role1"));

        for (String role : roles) {
            assertFalse(ArrayUtils.contains(admin.getUserListOfRole(role), "SECONDARYJDBC/user1"));
        }
    }

    public void test177AdRoleWithID() throws UserStoreException {

        admin.addRoleWithID("SECONDARYJDBC/role1WithID", null, null, false);
        Assert.assertEquals(7, admin.getRoleNames().length);
    }

    public void test178AddUserWithID() throws UserStoreException {

        User user2WithID = admin.addUserWithID("SECONDARYJDBC/user2WithID", "pass2",
                null, null, null);
        assertNotNull(user2WithID);
        userId2 = user2WithID.getUserID();
        assertNotNull(admin.addUserWithID("SECONDARYJDBC/user3WithID", "pass3", null,
                null, null));
        assertNotNull(admin.addUserWithID("SECONDARYJDBC/user4WithID", "pass4", null,
                null, null));
        //  user2,user3,user4 + admin + user2WithID,user3WithID,user4WithID
        Assert.assertEquals(8, admin.listUsersWithID("*", 100).size());
    }

    public void test180AddUserWithRoleWithID() throws UserStoreException {

        User user1WithID = admin.addUserWithID("SECONDARYJDBC/user1WithID", "pass1", new String[]{"role1WithID"},
                null, null);
        assertNotNull(user1WithID);
        userId1 = user1WithID.getUserID();
        //  user2,user3,user4 + admin + user2WithID,user3WithID,user4WithID,user1WithID
        Assert.assertEquals(9, admin.listUsersWithID("*", 100).size());
    }

    public void test183AuthenticateWithID() throws UserStoreException {

        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId1,
                "pass1").getAuthenticationStatus());
        assertEquals(AuthenticationResult.AuthenticationStatus.SUCCESS, admin.authenticateWithID(userId2,
                "pass2").getAuthenticationStatus());
    }

    private void addSecondaryUserStoreManager(RealmConfiguration primaryRealm,
                                              AbstractUserStoreManager userStoreManager,
                                              UserRealm userRealm) throws Exception {

        String dbUrl = "jdbc:h2:./target/BasicJDBCDatabaseTestSecondary/CARBON_TEST";
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(dbUrl);
        DatabaseCreator creator = new DatabaseCreator(ds);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String resourcesPath = new File("src/test/resources").getAbsolutePath();
        System.setProperty(ServerConstants.CARBON_HOME, resourcesPath);
        creator.createRegistryDatabase();
        System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
        InputStream inStream = this.getClass().getClassLoader().getResource("SECONDARYJDBC.xml").openStream();
        RealmConfiguration realmConfig = getRealmConfiguration(primaryRealm,"src/test/resources/SECONDARYJDBC.xml",
                inStream);
        userStoreManager.addSecondaryUserStoreManager(realmConfig, userRealm);
        ds.close();
    }

    private RealmConfiguration getRealmConfiguration(RealmConfiguration primaryRealm, String identifier,
                                                     InputStream scriptBinaryStream)
            throws UserStoreException, XMLStreamException {

        return buildUserStoreConfiguration(primaryRealm, getRealmElement(scriptBinaryStream), identifier);
    }


    private OMElement getRealmElement(InputStream inputStream) throws XMLStreamException,
            org.wso2.carbon.user.core.UserStoreException {

        try {
            inputStream = CarbonUtils.replaceSystemVariablesInXml(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(inputStream);
            return builder.getDocumentElement();
        } catch (CarbonException e) {
            throw new org.wso2.carbon.user.core.UserStoreException(e.getMessage(), e);
        }
    }

    private RealmConfiguration buildUserStoreConfiguration(RealmConfiguration primaryRealm,
                                 OMElement userStoreElement,
                                 String filePath) throws UserStoreException {
        RealmConfiguration realmConfig;
        String userStoreClass;
        Map<String, String> userStoreProperties;
        boolean passwordsExternallyManaged = false;

        realmConfig = new RealmConfiguration();
        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] fileNames = filePath.split(pattern);
        String fileName = fileNames[fileNames.length - 1].replace(".xml", "").replace("_", ".");
        userStoreClass = userStoreElement.getAttributeValue(new QName(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS));
        userStoreProperties = getChildPropertyElements(userStoreElement);

        if (!userStoreProperties.get(UserStoreConfigConstants.DOMAIN_NAME).equalsIgnoreCase(fileName)) {
            throw new org.wso2.carbon.user.core.UserStoreException(
                    "File name is required to be the user store domain name(eg.: wso2.com-->wso2_com.xml).");
        }

        String sIsPasswordExternallyManaged = userStoreProperties
                .get(UserCoreConstants.RealmConfig.LOCAL_PASSWORDS_EXTERNALLY_MANAGED);

        if (null != sIsPasswordExternallyManaged
                && !sIsPasswordExternallyManaged.trim().equals("")) {
            passwordsExternallyManaged = Boolean.parseBoolean(sIsPasswordExternallyManaged);
        }

        realmConfig.setUserStoreClass(userStoreClass);
        realmConfig.setAuthorizationManagerClass(primaryRealm.getAuthorizationManagerClass());
        realmConfig.setEveryOneRoleName(UserCoreUtil.addDomainToName(primaryRealm.getEveryOneRoleName(),
                UserCoreConstants.INTERNAL_DOMAIN));
        realmConfig.setUserStoreProperties(userStoreProperties);
        realmConfig.setPasswordsExternallyManaged(passwordsExternallyManaged);
        realmConfig.setAuthzProperties(primaryRealm.getAuthzProperties());
        realmConfig.setRealmProperties(primaryRealm.getRealmProperties());
        realmConfig.setPasswordsExternallyManaged(primaryRealm.isPasswordsExternallyManaged());

        if (realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST) == null) {
            realmConfig.getUserStoreProperties().put(
                    UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST,
                    UserCoreConstants.RealmConfig.PROPERTY_VALUE_DEFAULT_MAX_COUNT);
        }

        if (realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY) == null) {
            realmConfig.getUserStoreProperties().put(
                    UserCoreConstants.RealmConfig.PROPERTY_READ_ONLY,
                    UserCoreConstants.RealmConfig.PROPERTY_VALUE_DEFAULT_READ_ONLY);
        }
        return realmConfig;
    }

    private Map<String, String> getChildPropertyElements(OMElement omElement) {

        try {
            AXIOMXPath xPath = new AXIOMXPath(UserCoreConstants.RealmConfig.DOMAIN_NAME_XPATH);
            OMElement val = (OMElement) xPath.selectSingleNode(omElement);
        } catch (Exception ignored) {
        }

        Map<String, String> map = new HashMap<String, String>();
        Iterator<?> ite = omElement.getChildrenWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_PROPERTY));
        while (ite.hasNext()) {
            OMElement propElem = (OMElement) ite.next();
            String propName = propElem.getAttributeValue(new QName(
                    UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME));
            String propValue = propElem.getText();
            if (propName != null && propValue != null) {
                map.put(propName.trim(), propValue.trim());
            }
        }
        return map;
    }

}
