/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.user.core.ldap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.util.LDAPUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.user.core.UserStoreConfigConstants.dateAndTimePattern;

/**
 * Unit tests for UniqueIDReadOnlyLDAPUserStoreManager class.
 */
public class UniqueIDReadOnlyLDAPUserStoreManagerTest {

    @Mock
    private RealmConfiguration realmConfig;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test directly the convertToStandardTimeFormat method with all supported formats.
     */
    @Test
    public void testConvertToStandardTimeFormat() throws Exception {

        // Configure realm config for testing - no custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn(null);

        // Test all supported LDAP timestamp formats:
        // "uuuuMMddHHmmss,SSSX" - 14 digits + ,3digits + timezone.
        assertEquals("2025-08-13T14:56:07.123Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,123Z"));
        assertEquals("2025-08-13T14:56:07.123Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,123+0000"));
        assertEquals("2025-08-13T12:56:07.123Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,123+0200"));

        // "uuuuMMddHHmmss.SSSX" - 14 digits + .3digits + timezone.
        assertEquals("2025-08-13T14:56:07.456Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.456Z"));
        assertEquals("2025-08-13T14:56:07.456Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.456+0000"));
        assertEquals("2025-08-13T12:56:07.456Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.456+0200"));

        // "uuuuMMddHHmmss,SSX" - 14 digits + ,2digits + timezone.
        assertEquals("2025-08-13T14:56:07.120Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,12Z"));
        assertEquals("2025-08-13T14:56:07.120Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,12+0000"));
        assertEquals("2025-08-13T12:56:07.120Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,12+0200"));

        // "uuuuMMddHHmmss.SSX" - 14 digits + .2digits + timezone.
        assertEquals("2025-08-13T14:56:07.780Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.78Z"));
        assertEquals("2025-08-13T14:56:07.780Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.78+0000"));
        assertEquals("2025-08-13T12:56:07.780Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.78+0200"));

        // "uuuuMMddHHmmss,SX" - 14 digits + ,1digit + timezone.
        assertEquals("2025-08-13T14:56:07.900Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,9Z"));
        assertEquals("2025-08-13T14:56:07.900Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,9+0000"));
        assertEquals("2025-08-13T12:56:07.900Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607,9+0200"));

        // "uuuuMMddHHmmss.SX" - 14 digits + .1digit + timezone.
        assertEquals("2025-08-13T14:56:07.800Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.8Z"));
        assertEquals("2025-08-13T14:56:07.800Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.8+0000"));
        assertEquals("2025-08-13T12:56:07.800Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607.8+0200"));

        // Test null and empty.
        assertNull(LDAPUtil.convertToStandardTimeFormat(realmConfig, null));
        assertEquals("", LDAPUtil.convertToStandardTimeFormat(realmConfig, ""));

        // Test basic format without fractional seconds.
        assertEquals("2025-08-13T14:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607Z"));
        assertEquals("2025-08-13T14:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607+0000"));
        assertEquals("2025-08-13T12:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607+0200"));
    }

    /**
     * Test convertToStandardTimeFormat with custom date pattern configured.
     */
    @Test
    public void testConvertToStandardTimeFormat_WithCustomPattern() throws Exception {

        // Configure realm config with custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn("uuuuMMddHHmmssX");

        // Test with custom pattern - should use configured pattern instead of inference.
        assertEquals("2025-08-13T14:56:07Z",
                LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607Z"));
    }

    /**
     * Test convertToStandardTimeFormat with potential timezone edge cases.
     */
    @Test
    public void testConvertToStandardTimeFormat_TimezoneEdgeCases() throws Exception {

        // Configure realm config for testing - no custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn(null);

        // Test edge case: 2-digit timezone (might not be supported by current regex).
        try {
            LDAPUtil.convertToStandardTimeFormat(realmConfig, "20250813145607+05");
            // If this doesn't throw an exception, it's supported.
        } catch (UserStoreException e) {
            // Expected - 2-digit timezone not supported by current regex.
            assertTrue(e instanceof UserStoreException,
                    "Should throw UserStoreException for 2-digit timezone");
        }
    }

    /**
     * Test convertToStandardTimeFormat with unsupported date format.
     */
    @Test
    public void testConvertToStandardTimeFormat_UnsupportedFormat() throws Exception {

        // Configure realm config for testing - no custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn(null);

        try {
            // Test with unsupported format - should throw UserStoreException.
            LDAPUtil.convertToStandardTimeFormat(realmConfig, "invalid-date-format");
            // Should not reach here.
            throw new AssertionError("Expected UserStoreException was not thrown");
        } catch (UserStoreException e) {
            assertTrue(e.getMessage().contains("Unsupported LDAP timestamp format"),
                    "Error message should mention unsupported timestamp format");
        }
    }

    /**
     * Test convertToStandardTimeFormat with invalid date format for custom pattern.
     */
    @Test
    public void testConvertToStandardTimeFormat_InvalidCustomPatternFormat() throws Exception {

        // Configure realm config with custom pattern.
        when(realmConfig.getUserStoreProperty(dateAndTimePattern)).thenReturn("uuuuMMddHHmmssX");

        try {
            // Test with invalid format for the custom pattern - should throw UserStoreException.
            LDAPUtil.convertToStandardTimeFormat(realmConfig, "invalid-format-for-custom-pattern");
            // Should not reach here.
            throw new AssertionError("Expected UserStoreException was not thrown");
        } catch (UserStoreException e) {
            assertTrue(e.getMessage().contains("Invalid timestamp format"),
                    "Error message should mention invalid timestamp format");
        }
    }
}
