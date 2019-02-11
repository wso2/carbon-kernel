package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ValueInferrerTest {

    private static final String INFER_TOML = "infer.json";

    @Test(dataProvider = "contextProvider")
    public void testParse(Map<String, Object> context, String key, Object expectedValue) {
        String inferConfiguration =
                FileUtils.getFile("src", "test", "resources", INFER_TOML).getAbsolutePath();

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
        return new Object[][]{
                {jdbcContext, "user_store.class", "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager"},
                {jdbcContext, "user_store.properties.ReadOnly", false},
                {jdbcContext, "user_store.properties.dummyArray", Arrays.asList("foo", "bar")},
                {readOnlyLdapContext, "user_store.class", "org.wso2.carbon.user.core.ldap" +
                        ".ReadOnlyLDAPUserStoreManager"},
                {readOnlyLdapContext, "user_store.properties.LDAPConnectionTimeout", 5000},
                {invalidContext, "user_store.class", null},
                {variableContext, "datasource.apim.driver", "com.mysql.jdbc.Driver"}
        };
    }

    @Test
    public void testInfer() {

    }
}
