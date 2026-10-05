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

package org.wso2.carbon.user.core.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Tests that the error code is carried on the exception, and not only inside its message, when a
 * user store operation fails because the user does not exist.
 */
public class AbstractUserStoreManagerErrorCodeTest {

    private static final String NON_EXISTING_USER_ID = "4f0d5b9c-0a2e-4f9a-9f1d-6f0a2b3c4d5e";

    private UniqueIDJDBCUserStoreManager userStoreManager;

    @BeforeMethod
    public void setUp() throws Exception {

        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_ID_ENABLED))
                .thenReturn("true");
        when(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn("PRIMARY");

        userStoreManager = mock(UniqueIDJDBCUserStoreManager.class, CALLS_REAL_METHODS);
        Field realmConfig = AbstractUserStoreManager.class.getDeclaredField("realmConfig");
        realmConfig.setAccessible(true);
        realmConfig.set(userStoreManager, realmConfiguration);

        UserStore userStore = new UserStore();
        userStore.setUserStoreManager(userStoreManager);
        userStore.setRecurssive(false);
        userStore.setDomainName("PRIMARY");
        doReturn(userStore).when(userStoreManager).getUserStoreWithID(anyString());
        doReturn(false).when(userStoreManager).doCheckExistingUserWithID(anyString());
    }

    @Test
    public void testGetUserClaimValuesWithIDCarriesNonExistingUserErrorCode() {

        try {
            userStoreManager.getUserClaimValuesWithID(NON_EXISTING_USER_ID,
                    new String[] {"http://wso2.org/claims/username"}, null);
            fail("Expected a UserStoreException for a non existing user.");
        } catch (UserStoreException e) {
            assertEquals(e.getErrorCode(),
                    UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode());
        }
    }

    @Test
    public void testGetUserClaimValueWithIDCarriesNonExistingUserErrorCode() {

        try {
            userStoreManager.getUserClaimValueWithID(NON_EXISTING_USER_ID,
                    "http://wso2.org/claims/username", null);
            fail("Expected a UserStoreException for a non existing user.");
        } catch (UserStoreException e) {
            assertEquals(e.getErrorCode(),
                    UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode());
        }
    }
}
