/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ValueInferrerTest {

    private static final String INFER_JSON = "infer.json";

    @Test(dataProvider = "contextProvider")
    public void testParse(Map<String, Object> context, String key, Object expectedValue) throws ConfigParserException {

        String inferConfiguration =
                FileUtils.getFile("src", "test", "resources", INFER_JSON).getAbsolutePath();

        Map<String, Object> inferredValues = ValueInferrer.infer(context, inferConfiguration);
        Object actualValue = inferredValues.get(key);
        Assert.assertEquals(actualValue, expectedValue, "Incorrect inferred value for " + key);
    }

    @DataProvider(name = "contextProvider")
    public Object[][] inferringDataSet() {

        Map<String, Object> jdbcContext = new HashMap<>();
        Map<String, Object> readOnlyLdapContext = new HashMap<>();
        Map<String, Object> invalidContext = new HashMap<>();
        jdbcContext.put("user_store.type", "jdbc");
        readOnlyLdapContext.put("user_store.type", "read_only_ldap");
        invalidContext.put("user_store.type", "invalid_value");
        Map<String, Object> variableContext = new HashMap<>();
        variableContext.put("datasource.apim.type", "mysql");
        variableContext.put("datasource.abc.type", "mysql");
        variableContext.put("datasource.abc.name", "hellodb");
        variableContext.put("datasource.cde.type", "oracle");
        variableContext.put("datasource.carbon.type", "mysql");
        return new Object[][]{
                {jdbcContext, "user_store.class", "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager"},
                {jdbcContext, "user_store.properties.ReadOnly", false},
                {jdbcContext, "user_store.properties.dummyArray", Arrays.asList("foo", "bar")},
                {readOnlyLdapContext, "user_store.class", "org.wso2.carbon.user.core.ldap" +
                        ".ReadOnlyLDAPUserStoreManager"},
                {readOnlyLdapContext, "user_store.properties.LDAPConnectionTimeout", 5000},
                {invalidContext, "user_store.class", null},
                {variableContext, "datasource.apim.driver", "com.mysql.jdbc.Driver"},
                {variableContext, "datasource.abc.driver", "com.mysql.jdbc.Driver"},
                {variableContext, "datasource.abc.url", "jdbc:mysql://localhost:3306/$ref{datasource.abc.name}"},
                {variableContext, "datasource.cde.driver", null},
                {variableContext, "datasource.carbon.driver", "com.oracle.Driver"},
                {jdbcContext, "tenant_mgt.tenant_manager.config_builder", "org.wso2.carbon.user.core.config" +
                        ".multitenancy.SimpleRealmConfigBuilder"}
        };
    }

    @Test
    public void testInfer() {

    }
}
