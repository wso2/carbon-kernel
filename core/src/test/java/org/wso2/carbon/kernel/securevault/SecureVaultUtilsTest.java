package org.wso2.carbon.kernel.securevault;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.EnvironmentUtils;

/**
 * Unit tests class for org.wso2.carbon.kernel.securevault.SecureVaultUtils.
 *
 * @since 5.2.0
 */
public class SecureVaultUtilsTest {
    @Test
    public void testEnvironmentVariable() {
        String alias = "${env:my.environment.variable}";
        String value = "ABC123";
        EnvironmentUtils.setEnv("my.environment.variable", value);
        String updatedValue = SecureVaultUtils.readUpdatedValue(alias);
        Assert.assertEquals(updatedValue, value);
    }

    @Test
    public void testSystemVariable() {
        String alias = "${sys:my.system.property}";
        String value = "ABC123";
        System.setProperty("my.system.property", value);
        String updatedValue = SecureVaultUtils.readUpdatedValue(alias);
        Assert.assertEquals(updatedValue, value);
    }

    @Test
    public void testNullAlias() {
        String updatedValue = SecureVaultUtils.readUpdatedValue(null);
        Assert.assertNull(updatedValue);
    }

    @Test
    public void testInvalidPlaceholder() {
        String alias = "${invalid:my.system.property}";
        String updatedValue = SecureVaultUtils.readUpdatedValue(alias);
        Assert.assertEquals(updatedValue, alias);
    }
}
