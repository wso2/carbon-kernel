/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.jdbc;

import org.testng.annotations.Test;
import org.wso2.carbon.user.core.util.JDBCRealmUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers the SQL Server claim update path, where a regression is silent: nothing throws and nothing
 * logs, the statement just stops seeking and starts locking the user's whole attribute set again.
 */
public class ClaimUpdatePredicateSQLTest {

    @Test
    public void testMSSQLStatementCastsOnlyTheVarcharPredicates() {

        String sql = JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_MSSQL_SQL;
        for (String column : new String[] { "UM_ATTR_NAME", "UM_PROFILE_ID", "UM_USER_ID" }) {
            assertTrue(sql.contains(column + "=CAST(? AS VARCHAR"),
                    column + " is a VARCHAR column and must be compared against a VARCHAR parameter, otherwise the "
                            + "conversion lands on the column and the predicate cannot seek: " + sql);
        }
        assertTrue(sql.startsWith("UPDATE UM_USER_ATTRIBUTE SET UM_ATTR_VALUE=? "),
                "UM_ATTR_VALUE is NVARCHAR and is bound as Unicode where the user store asks for it, so casting it "
                        + "would silently mangle non-ASCII claim values: " + sql);
    }

    @Test
    public void testMSSQLStatementKeepsTheGenericParameterOrder() {

        String generic = JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_SQL;
        String mssql = JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_MSSQL_SQL;
        assertEquals(mssql.replaceAll("CAST\\(\\? AS VARCHAR\\(\\d+\\)\\)", "?").replaceAll("\\s+", " ").trim(),
                generic.replaceAll("\\s+", " ").trim(),
                "Both statements are bound by the same code, in a fixed parameter order, so the SQL Server variant "
                        + "must differ from the generic one only by the casts.");
    }

    @Test
    public void testMSSQLStatementIsRegisteredUnderTheKeyTheLookupUses() {

        assertEquals(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_MSSQL,
                JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED + "-mssql",
                "updateProperties looks the statement up as the property name plus the database type, so the two "
                        + "must agree - if they drift, the cast is never read and nothing reports it.");
        assertEquals(JDBCRealmUtil.getSQL(new HashMap<>())
                        .get(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_MSSQL),
                JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_MSSQL_SQL,
                "A user store that overrides nothing must pick up the SQL Server variant.");
        assertFalse(JDBCRealmUtil.getSQL(new HashMap<>(Map.of(
                        JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED, "UPDATE the store's own SQL")))
                        .containsKey(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_MSSQL),
                "The SQL Server variant is looked up first, so it must not be added to a user store that configured "
                        + "its own generic statement - that would override what was configured.");
    }

    @Test
    public void testTheBatchIsOrderedIndependentlyOfTheCallersMap() {

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("http://wso2.org/claims/mobile", "1");
        properties.put("http://wso2.org/claims/emailaddress", "2");
        properties.put("http://wso2.org/claims/country", "3");

        Map<String, String> reversed = new LinkedHashMap<>();
        properties.entrySet().stream().sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .forEach(e -> reversed.put(e.getKey(), e.getValue()));

        assertEquals(claimOrder(properties), claimOrder(reversed),
                "Two callers passing the same claims in different orders must produce the same batch order, "
                        + "otherwise they can take the same row locks in opposite orders and deadlock.");
        assertEquals(claimOrder(properties), Arrays.asList("http://wso2.org/claims/country",
                "http://wso2.org/claims/emailaddress", "http://wso2.org/claims/mobile"));
    }

    private List<String> claimOrder(Map<String, String> properties) {

        return UniqueIDJDBCUserStoreManager.orderClaimsForBatch(properties).stream()
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
