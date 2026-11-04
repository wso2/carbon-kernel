/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.common;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.config.TestRealmConfigBuilder;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.core.internal.UMListenerServiceComponent;
import org.wso2.carbon.user.core.jdbc.JDBCRealmTest;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests which role arrays {@link UserOperationEventListener#doPostUpdateRoleListOfUser} receives from
 * {@link AbstractUserStoreManager#updateRoleListOfUser(String, String[], String[])}.
 * <p>
 * Listeners whose class name ends with {@link UserCoreErrorConstants#AUDIT_LOGGER_CLASS_NAME} must receive the
 * user store roles together with the domain-qualified Internal/Application roles, so that the audit log lists every
 * updated role. All other listeners must keep receiving only the user store roles. A post-listener that returns
 * false must still trigger the update-role-list-of-user failure handling and stop the listener chain.
 */
public class UpdateRoleListOfUserAuditListenerTest extends BaseTestCase {

    private static final String TEST_URL = "jdbc:h2:./target/auditlistenertest/CARBON_TEST";
    private static final String DB_FOLDER = "target/auditlistenertest";

    private static final String USER = "user1";
    private static final String STORE_ROLE_1 = "TestRole1";
    private static final String STORE_ROLE_2 = "TestRole2";
    private static final String INTERNAL_ROLE = "Internal/creator";
    private static final String APPLICATION_ROLE = "Application/TestApp";

    // High execution order ids so that these stubs do not replace listeners registered by other tests.
    private static final int AUDIT_LISTENER_ORDER = 9101;
    private static final int OTHER_LISTENER_ORDER = 9102;
    private static final int ERROR_LISTENER_ORDER = 9103;

    private UserRealm realm;
    private UserStoreManager userStoreManager;
    private final List<UserOperationEventListener> registeredListeners = new ArrayList<>();
    private final List<UserManagementErrorEventListener> registeredErrorListeners = new ArrayList<>();

    @Override
    public void setUp() throws Exception {

        super.setUp();
        DatabaseUtil.closeDatabasePoolConnection();
        if (new File(DB_FOLDER).exists()) {
            deleteDir(new File(DB_FOLDER));
        }
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl(TEST_URL);
        DatabaseCreator creator = new DatabaseCreator(ds);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        System.setProperty(ServerConstants.CARBON_HOME, new File("src/test/resources").getAbsolutePath());
        creator.createRegistryDatabase();
        if (carbonHome != null) {
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
        }

        realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader()
                .getResource(JDBCRealmTest.JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder.buildRealmConfigWithJDBCConnectionUrl(inStream,
                TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil.getProfileTestData(),
                MultitenantConstants.SUPER_TENANT_ID);
        ds.close();

        userStoreManager = realm.getUserStoreManager();
        userStoreManager.addUser(USER, "dummy-secret1", null, null, null, false);
        userStoreManager.addRole(STORE_ROLE_1, null, null);
        userStoreManager.addRole(STORE_ROLE_2, null, null);
        userStoreManager.addRole(INTERNAL_ROLE, null, null);
        userStoreManager.addRole(APPLICATION_ROLE, null, null);
    }

    @Override
    public void tearDown() throws Exception {

        for (UserOperationEventListener listener : registeredListeners) {
            invokeListenerComponent("unsetUserOperationEventListenerService", UserOperationEventListener.class,
                    listener);
        }
        registeredListeners.clear();
        for (UserManagementErrorEventListener listener : registeredErrorListeners) {
            invokeListenerComponent("unsetUserManagementErrorEventListenerService",
                    UserManagementErrorEventListener.class, listener);
        }
        registeredErrorListeners.clear();
        DatabaseUtil.closeDatabasePoolConnection();
        super.tearDown();
    }

    /**
     * Only Internal/Application roles change: the audit logger must see them, other listeners see empty arrays.
     */
    public void testInternalOnlyRoleChangeIsPassedToAuditLoggerOnly() throws Exception {

        RecordingAuditLogger auditLogger = new RecordingAuditLogger(AUDIT_LISTENER_ORDER, true);
        RecordingListener otherListener = new RecordingListener(OTHER_LISTENER_ORDER, true);
        registerListener(auditLogger);
        registerListener(otherListener);

        userStoreManager.updateRoleListOfUser(USER, null, new String[]{INTERNAL_ROLE, APPLICATION_ROLE});

        assertEquals(1, auditLogger.calls.size());
        assertRoles(auditLogger.calls.get(0).deletedRoles);
        assertRoles(auditLogger.calls.get(0).newRoles, INTERNAL_ROLE, APPLICATION_ROLE);
        assertEquals(1, otherListener.calls.size());
        assertRoles(otherListener.calls.get(0).deletedRoles);
        assertRoles(otherListener.calls.get(0).newRoles);

        // Removal of the same roles.
        userStoreManager.updateRoleListOfUser(USER, new String[]{INTERNAL_ROLE, APPLICATION_ROLE}, null);

        assertEquals(2, auditLogger.calls.size());
        assertRoles(auditLogger.calls.get(1).deletedRoles, INTERNAL_ROLE, APPLICATION_ROLE);
        assertRoles(auditLogger.calls.get(1).newRoles);
        assertEquals(2, otherListener.calls.size());
        assertRoles(otherListener.calls.get(1).deletedRoles);
        assertRoles(otherListener.calls.get(1).newRoles);

        // The internal-role post listener is unchanged and still receives only the internal roles.
        assertEquals(2, otherListener.internalCalls.size());
        assertRoles(otherListener.internalCalls.get(0).newRoles, INTERNAL_ROLE, APPLICATION_ROLE);
        assertRoles(otherListener.internalCalls.get(1).deletedRoles, INTERNAL_ROLE, APPLICATION_ROLE);
    }

    /**
     * User store and Internal/Application roles change together: the audit logger sees the combined arrays,
     * other listeners see only the user store roles.
     */
    public void testMixedRoleChangeIsCombinedForAuditLoggerOnly() throws Exception {

        RecordingAuditLogger auditLogger = new RecordingAuditLogger(AUDIT_LISTENER_ORDER, true);
        RecordingListener otherListener = new RecordingListener(OTHER_LISTENER_ORDER, true);
        registerListener(auditLogger);
        registerListener(otherListener);

        userStoreManager.updateRoleListOfUser(USER, null,
                new String[]{STORE_ROLE_1, INTERNAL_ROLE, STORE_ROLE_2, APPLICATION_ROLE});

        assertEquals(1, auditLogger.calls.size());
        assertRoles(auditLogger.calls.get(0).deletedRoles);
        assertRoles(auditLogger.calls.get(0).newRoles, STORE_ROLE_1, STORE_ROLE_2, INTERNAL_ROLE, APPLICATION_ROLE);
        assertEquals(1, otherListener.calls.size());
        assertRoles(otherListener.calls.get(0).deletedRoles);
        assertRoles(otherListener.calls.get(0).newRoles, STORE_ROLE_1, STORE_ROLE_2);

        // Delete one of each kind and add nothing.
        userStoreManager.updateRoleListOfUser(USER, new String[]{STORE_ROLE_2, INTERNAL_ROLE}, null);

        assertEquals(2, auditLogger.calls.size());
        assertRoles(auditLogger.calls.get(1).deletedRoles, STORE_ROLE_2, INTERNAL_ROLE);
        assertRoles(auditLogger.calls.get(1).newRoles);
        assertRoles(otherListener.calls.get(1).deletedRoles, STORE_ROLE_2);
        assertRoles(otherListener.calls.get(1).newRoles);

        // Add and delete in the same call.
        userStoreManager.updateRoleListOfUser(USER, new String[]{STORE_ROLE_1, APPLICATION_ROLE},
                new String[]{STORE_ROLE_2, INTERNAL_ROLE});

        assertEquals(3, auditLogger.calls.size());
        assertRoles(auditLogger.calls.get(2).deletedRoles, STORE_ROLE_1, APPLICATION_ROLE);
        assertRoles(auditLogger.calls.get(2).newRoles, STORE_ROLE_2, INTERNAL_ROLE);
        assertRoles(otherListener.calls.get(2).deletedRoles, STORE_ROLE_1);
        assertRoles(otherListener.calls.get(2).newRoles, STORE_ROLE_2);

        // The role assignments themselves are applied.
        Set<String> userRoles = new HashSet<>(Arrays.asList(userStoreManager.getRoleListOfUser(USER)));
        assertTrue(userRoles.contains(STORE_ROLE_2));
        assertTrue(userRoles.contains(INTERNAL_ROLE));
        assertFalse(userRoles.contains(STORE_ROLE_1));
        assertFalse(userRoles.contains(APPLICATION_ROLE));
    }

    /**
     * Only user store roles change: both listeners see the same, unchanged user store arrays.
     */
    public void testUserStoreOnlyRoleChangeIsUnchangedForAllListeners() throws Exception {

        RecordingAuditLogger auditLogger = new RecordingAuditLogger(AUDIT_LISTENER_ORDER, true);
        RecordingListener otherListener = new RecordingListener(OTHER_LISTENER_ORDER, true);
        registerListener(auditLogger);
        registerListener(otherListener);

        userStoreManager.updateRoleListOfUser(USER, null, new String[]{STORE_ROLE_1});
        userStoreManager.updateRoleListOfUser(USER, new String[]{STORE_ROLE_1}, new String[]{STORE_ROLE_2});

        assertEquals(2, auditLogger.calls.size());
        assertEquals(2, otherListener.calls.size());
        assertRoles(auditLogger.calls.get(0).deletedRoles);
        assertRoles(auditLogger.calls.get(0).newRoles, STORE_ROLE_1);
        assertRoles(otherListener.calls.get(0).deletedRoles);
        assertRoles(otherListener.calls.get(0).newRoles, STORE_ROLE_1);
        assertRoles(auditLogger.calls.get(1).deletedRoles, STORE_ROLE_1);
        assertRoles(auditLogger.calls.get(1).newRoles, STORE_ROLE_2);
        assertRoles(otherListener.calls.get(1).deletedRoles, STORE_ROLE_1);
        assertRoles(otherListener.calls.get(1).newRoles, STORE_ROLE_2);
    }

    /**
     * A listener whose name merely contains "AuditLogger" (but does not end with it) is not treated as an audit
     * logger.
     */
    public void testListenerNotEndingWithAuditLoggerGetsUserStoreRolesOnly() throws Exception {

        AuditLoggerLikeListener listener = new AuditLoggerLikeListener(OTHER_LISTENER_ORDER, true);
        registerListener(listener);

        userStoreManager.updateRoleListOfUser(USER, null, new String[]{STORE_ROLE_1, INTERNAL_ROLE});

        assertEquals(1, listener.calls.size());
        assertRoles(listener.calls.get(0).newRoles, STORE_ROLE_1);
    }

    /**
     * An audit logger post-listener that returns false must still trigger the failure handling with the user store
     * arrays, and later listeners must not be called.
     */
    public void testFailingAuditLoggerTriggersFailureHandlingAndStopsListenerChain() throws Exception {

        RecordingErrorListener errorListener = new RecordingErrorListener(ERROR_LISTENER_ORDER);
        registerErrorListener(errorListener);
        RecordingAuditLogger failingAuditLogger = new RecordingAuditLogger(AUDIT_LISTENER_ORDER, false);
        RecordingListener laterListener = new RecordingListener(OTHER_LISTENER_ORDER, true);
        registerListener(failingAuditLogger);
        registerListener(laterListener);

        // updateRoleListOfUser returns normally after the failure handling, as before the fix.
        userStoreManager.updateRoleListOfUser(USER, null, new String[]{STORE_ROLE_1, INTERNAL_ROLE});

        assertEquals("The failing audit logger should be called once", 1, failingAuditLogger.calls.size());
        assertRoles(failingAuditLogger.calls.get(0).newRoles, STORE_ROLE_1, INTERNAL_ROLE);

        assertEquals("Failure handling should run once", 1, errorListener.failures.size());
        RoleCall failure = errorListener.failures.get(0);
        assertEquals(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_DURING_POST_UPDATE_ROLE_OF_USER.getCode(),
                failure.errorCode);
        assertEquals(USER, failure.userName);
        // The failure handler keeps receiving the user store arrays, unchanged by the fix.
        assertRoles(failure.deletedRoles);
        assertRoles(failure.newRoles, STORE_ROLE_1);

        assertTrue("Later post-listeners must not be called", laterListener.calls.isEmpty());
        assertTrue("The internal-role post-listener loop must not run", laterListener.internalCalls.isEmpty());
        assertTrue(failingAuditLogger.internalCalls.isEmpty());
    }

    /**
     * A non-audit post-listener that returns false behaves exactly as before.
     */
    public void testFailingNonAuditListenerTriggersFailureHandlingAndStopsListenerChain() throws Exception {

        RecordingErrorListener errorListener = new RecordingErrorListener(ERROR_LISTENER_ORDER);
        registerErrorListener(errorListener);
        RecordingListener failingListener = new RecordingListener(AUDIT_LISTENER_ORDER, false);
        RecordingAuditLogger laterAuditLogger = new RecordingAuditLogger(OTHER_LISTENER_ORDER, true);
        registerListener(failingListener);
        registerListener(laterAuditLogger);

        userStoreManager.updateRoleListOfUser(USER, null, new String[]{STORE_ROLE_1, APPLICATION_ROLE});

        assertEquals(1, failingListener.calls.size());
        assertRoles(failingListener.calls.get(0).newRoles, STORE_ROLE_1);
        assertEquals(1, errorListener.failures.size());
        assertRoles(errorListener.failures.get(0).newRoles, STORE_ROLE_1);
        assertTrue(laterAuditLogger.calls.isEmpty());
        assertTrue(laterAuditLogger.internalCalls.isEmpty());
    }

    private static void assertRoles(String[] actual, String... expected) {

        assertNotNull("Role array passed to the listener should not be null", actual);
        assertEquals("Unexpected roles: " + Arrays.toString(actual), expected.length, actual.length);
        assertEquals("Unexpected roles: " + Arrays.toString(actual), new HashSet<>(Arrays.asList(expected)),
                new HashSet<>(Arrays.asList(actual)));
    }

    private void registerListener(UserOperationEventListener listener) throws Exception {

        invokeListenerComponent("setUserOperationEventListenerService", UserOperationEventListener.class, listener);
        registeredListeners.add(listener);
    }

    private void registerErrorListener(UserManagementErrorEventListener listener) throws Exception {

        invokeListenerComponent("setUserManagementErrorEventListenerService", UserManagementErrorEventListener.class,
                listener);
        registeredErrorListeners.add(listener);
    }

    private static <T> void invokeListenerComponent(String methodName, Class<T> type, T listener) throws Exception {

        Method method = UMListenerServiceComponent.class.getDeclaredMethod(methodName, type);
        method.setAccessible(true);
        method.invoke(new UMListenerServiceComponent(), listener);
    }

    /**
     * Arguments captured from a listener call.
     */
    private static class RoleCall {

        private final String errorCode;
        private final String userName;
        private final String[] deletedRoles;
        private final String[] newRoles;

        RoleCall(String errorCode, String userName, String[] deletedRoles, String[] newRoles) {

            this.errorCode = errorCode;
            this.userName = userName;
            this.deletedRoles = deletedRoles == null ? null : deletedRoles.clone();
            this.newRoles = newRoles == null ? null : newRoles.clone();
        }
    }

    /**
     * Records the role arrays passed to the post update-role-list listeners.
     */
    private static class RecordingListener extends AbstractUserOperationEventListener {

        private final int orderId;
        private final boolean result;
        final List<RoleCall> calls = new ArrayList<>();
        final List<RoleCall> internalCalls = new ArrayList<>();

        RecordingListener(int orderId, boolean result) {

            this.orderId = orderId;
            this.result = result;
        }

        @Override
        public int getExecutionOrderId() {

            return orderId;
        }

        @Override
        public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

            calls.add(new RoleCall(null, userName, deletedRoles, newRoles));
            return result;
        }

        @Override
        public boolean doPostUpdateInternalRoleListOfUser(String userName, String[] deletedInternalRoles,
                                                          String[] addedInternalRoles,
                                                          UserStoreManager userStoreManager)
                throws UserStoreException {

            internalCalls.add(new RoleCall(null, userName, deletedInternalRoles, addedInternalRoles));
            return true;
        }
    }

    /**
     * Class name ends with "AuditLogger", so the user store manager treats it as an audit logger.
     */
    private static class RecordingAuditLogger extends RecordingListener {

        RecordingAuditLogger(int orderId, boolean result) {

            super(orderId, result);
        }
    }

    /**
     * Class name contains, but does not end with, "AuditLogger".
     */
    private static class AuditLoggerLikeListener extends RecordingListener {

        AuditLoggerLikeListener(int orderId, boolean result) {

            super(orderId, result);
        }
    }

    /**
     * Records calls to the update-role-list-of-user failure handler.
     */
    private static class RecordingErrorListener extends AbstractUserManagementErrorListener {

        private final int orderId;
        final List<RoleCall> failures = new ArrayList<>();

        RecordingErrorListener(int orderId) {

            this.orderId = orderId;
        }

        @Override
        public int getExecutionOrderId() {

            return orderId;
        }

        @Override
        public boolean isEnable() {

            return true;
        }

        @Override
        public boolean onUpdateRoleListOfUserFailure(String errorCode, String errorMessage, String userName,
                                                     String[] deletedRoles, String[] newRoles,
                                                     UserStoreManager userStoreManager) throws UserStoreException {

            failures.add(new RoleCall(errorCode, userName, deletedRoles, newRoles));
            return true;
        }
    }
}
