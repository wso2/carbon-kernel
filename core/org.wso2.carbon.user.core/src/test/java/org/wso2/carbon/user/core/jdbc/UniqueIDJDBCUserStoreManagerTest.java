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

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.GIVEN_NAME_PREFIX;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.GROUP_NAME;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.INTERNAL_ROLE_NAME;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.PASSWORD;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.USER_COUNT;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.USER_NAME_PREFIX;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.initUserStoreManager;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.setUpCarbonContext;
import static org.wso2.carbon.user.core.jdbc.JDBCUserStoreTestUtil.tearDownCarbonContext;

/**
 * Unit tests for {@link UniqueIDJDBCUserStoreManager}.
 */
public class UniqueIDJDBCUserStoreManagerTest {

    private static final String USER_MGT_XML = "user-mgt-test-uniqueId.xml";
    private static final String DB_FOLDER = "target/UniqueIDJDBCUserStoreManagerTest";
    private static final String DB_SCRIPTS_HOME = "src/test/resources/dbscripts/group_uuid_disable";

    private AbstractUserStoreManager userStoreManager;
    private final Map<String, String> givenNamesOfUserIDs = new HashMap<>();
    private final Map<String, String[]> rolesOfUserIDs = new HashMap<>();

    /**
     * Create the user store and the test users. Every user gets the group, and every second user the internal role.
     *
     * @throws Exception If the database cannot be created.
     */
    @BeforeClass
    public void setUp() throws Exception {

        setUpCarbonContext();
        userStoreManager = initUserStoreManager(USER_MGT_XML, DB_FOLDER, DB_SCRIPTS_HOME);
        userStoreManager.addRoleWithID(GROUP_NAME, null, null, false);
        userStoreManager.addRole(INTERNAL_ROLE_NAME, null, null);
        String everyOneRoleName = userStoreManager.getRealmConfiguration().getEveryOneRoleName();
        for (int i = 1; i <= USER_COUNT; i++) {
            Map<String, String> claims = new HashMap<>();
            claims.put(ClaimTestUtil.CLAIM_URI1, GIVEN_NAME_PREFIX + i);
            String[] roles = i % 2 == 0 ? new String[]{GROUP_NAME, INTERNAL_ROLE_NAME} : new String[]{GROUP_NAME};
            User user = userStoreManager.addUserWithID(USER_NAME_PREFIX + i, PASSWORD, roles, claims, null);
            givenNamesOfUserIDs.put(user.getUserID(), GIVEN_NAME_PREFIX + i);
            rolesOfUserIDs.put(user.getUserID(), (String[]) ArrayUtils.add(roles, everyOneRoleName));
        }
    }

    /**
     * Release the user store and restore the global state.
     */
    @AfterClass
    public void tearDown() {

        tearDownCarbonContext();
    }

    @Test(description = "Claims of a user list exceeding the IN clause limit must be retrieved for every user.")
    public void testGetUsersClaimValuesWithIDForLargeUserList() throws UserStoreException {

        List<UniqueIDUserClaimSearchEntry> claimSearchEntries = userStoreManager.getUsersClaimValuesWithID(
                new ArrayList<>(givenNamesOfUserIDs.keySet()), Collections.singletonList(ClaimTestUtil.CLAIM_URI1),
                null);
        assertEquals(claimSearchEntries.size(), USER_COUNT, "Claims must be returned for every user in the list.");
        for (UniqueIDUserClaimSearchEntry claimSearchEntry : claimSearchEntries) {
            String userID = claimSearchEntry.getUser().getUserID();
            assertEquals(claimSearchEntry.getClaims().get(ClaimTestUtil.CLAIM_URI1), givenNamesOfUserIDs.get(userID),
                    "Claim value mismatch for user: " + userID);
        }
    }

    @Test(description = "Roles of a user list exceeding the IN clause limit must be retrieved for every user.")
    public void testGetRoleListOfUsersWithIDForLargeUserList() throws UserStoreException {

        List<String> userIDs = new ArrayList<>(givenNamesOfUserIDs.keySet());
        Map<String, List<String>> roleListOfUsers = userStoreManager.getRoleListOfUsersWithID(userIDs);
        assertEquals(roleListOfUsers.size(), USER_COUNT, "Roles must be returned for every user in the list.");
        for (String userID : userIDs) {
            List<String> roles = roleListOfUsers.get(userID);
            assertNotNull(roles, "Roles must be returned for user: " + userID);
            assertEqualsNoOrder(roles.toArray(), rolesOfUserIDs.get(userID), "Unexpected roles for user: " + userID);
        }
    }
}
