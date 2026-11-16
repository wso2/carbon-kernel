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

package org.wso2.carbon.user.core.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.user.core.UserCoreConstants.MSSQL_TYPE;
import static org.wso2.carbon.user.core.UserCoreConstants.ORACLE_TYPE;

/**
 * Unit tests for {@link DatabaseUtil#getBatchesForInClause(List, String)}.
 */
public class DatabaseUtilTest {

    private static final int ORACLE_BATCH_SIZE = 1000;
    private static final int MSSQL_BATCH_SIZE = 2000;
    private static final String VALUE_PREFIX = "value";

    @Test(description = "An empty value list must not produce any batch.")
    public void testGetBatchesForInClauseWithEmptyList() {

        assertTrue(DatabaseUtil.getBatchesForInClause(Collections.emptyList(), ORACLE_TYPE).isEmpty(),
                "No batch must be returned for an empty value list.");
    }

    @Test(description = "A value list within the limit must be returned as a single batch.")
    public void testGetBatchesForInClauseWithinLimit() {

        List<String> values = createValues(ORACLE_BATCH_SIZE);

        List<List<String>> batches = DatabaseUtil.getBatchesForInClause(values, ORACLE_TYPE);
        assertEquals(batches.size(), 1, "A value list within the limit must not be split.");
        assertEquals(batches.get(0), values, "The batch must hold all the values in their original order.");
    }

    @DataProvider(name = "valueCountsExceedingLimit")
    public Object[][] valueCountsExceedingLimit() {

        return new Object[][]{
                {ORACLE_TYPE, ORACLE_BATCH_SIZE + 1, new int[]{ORACLE_BATCH_SIZE, 1}},
                {ORACLE_TYPE, 2500, new int[]{ORACLE_BATCH_SIZE, ORACLE_BATCH_SIZE, 500}},
                {MSSQL_TYPE, MSSQL_BATCH_SIZE + 1, new int[]{MSSQL_BATCH_SIZE, 1}},
                {MSSQL_TYPE, 4500, new int[]{MSSQL_BATCH_SIZE, MSSQL_BATCH_SIZE, 500}}
        };
    }

    @Test(dataProvider = "valueCountsExceedingLimit",
            description = "A value list exceeding the limit must be split into ordered batches within the limit.")
    public void testGetBatchesForInClauseExceedingLimit(String databaseType, int valueCount,
                                                        int[] expectedBatchSizes) {

        List<String> values = createValues(valueCount);

        List<List<String>> batches = DatabaseUtil.getBatchesForInClause(values, databaseType);
        assertEquals(batches.size(), expectedBatchSizes.length, "Unexpected number of batches.");
        List<String> mergedValues = new ArrayList<>();
        for (int i = 0; i < batches.size(); i++) {
            assertEquals(batches.get(i).size(), expectedBatchSizes[i], "Unexpected size of batch: " + i);
            mergedValues.addAll(batches.get(i));
        }
        assertEquals(mergedValues, values, "The batches must hold every value once, in the original order.");
    }

    @Test(description = "Duplicate values must be removed before batching, keeping their first occurrence.")
    public void testGetBatchesForInClauseRemovesDuplicates() {

        List<String> values = createValues(ORACLE_BATCH_SIZE);
        List<String> valuesWithDuplicates = new ArrayList<>(values);
        valuesWithDuplicates.add(values.get(0));
        valuesWithDuplicates.add(values.get(ORACLE_BATCH_SIZE - 1));

        List<List<String>> batches = DatabaseUtil.getBatchesForInClause(valuesWithDuplicates, ORACLE_TYPE);
        assertEquals(batches.size(), 1, "Duplicates must not count towards the limit.");
        assertEquals(batches.get(0), values, "Each value must appear once, at its first occurrence.");
    }

    @Test(description = "Values differing only by case must be kept, as they can be different users.")
    public void testGetBatchesForInClauseKeepsCaseVariants() {

        List<String> values = Arrays.asList("john", "John");

        List<List<String>> batches = DatabaseUtil.getBatchesForInClause(values, ORACLE_TYPE);
        assertEquals(batches.size(), 1, "Two values must be returned as a single batch.");
        assertEquals(batches.get(0), values, "Values differing only by case must not be removed as duplicates.");
    }

    @DataProvider(name = "databaseTypesWithoutLimit")
    public Object[][] databaseTypesWithoutLimit() {

        return new Object[][]{{"mysql"}, {"postgresql"}, {"db2"}, {"h2"}, {null}};
    }

    @Test(dataProvider = "databaseTypesWithoutLimit",
            description = "Database types other than Oracle and MSSQL must get the value list unchanged.")
    public void testGetBatchesForInClauseWithoutLimit(String databaseType) {

        List<String> values = createValues(2500);
        values.add(values.get(0));

        List<List<String>> batches = DatabaseUtil.getBatchesForInClause(values, databaseType);
        assertEquals(batches.size(), 1, "The value list must not be split.");
        assertEquals(batches.get(0), values, "The value list must be returned unchanged.");
    }

    private static List<String> createValues(int count) {

        List<String> values = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            values.add(VALUE_PREFIX + i);
        }
        return values;
    }
}
