/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.utils.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Test cases for TenantAwarePatternLayout related logging layout class.
 */
public class TenantAwarePatternLayoutTest extends BaseTest {
    private static final String pattern = "TID: [%T] [%S] [%U] [%A] [%D] [%I] [%H] [%P] [%K] [%U%@%D] [%d] %P%5p " +
            "{%c} - %x %m%n";

    @Test(groups = {"org.wso2.carbon.utils.logging"})
    public void testCreateTenantAwarePatternLayout1() throws Exception {
        TenantAwarePatternLayout tenantAwarePatternLayout = new TenantAwarePatternLayout(pattern);
        Assert.assertEquals(tenantAwarePatternLayout.getConversionPattern(), pattern);
    }

    @Test(groups = {"org.wso2.carbon.utils.logging"}, dependsOnMethods = "testCreateTenantAwarePatternLayout1")
    public void testCreateTenantAwarePatternLayout2() throws Exception {
        Field field = TenantAwarePatternLayout.class.getDeclaredField("tenantPattern");
        field.setAccessible(true);
        TenantAwarePatternLayout tenantAwarePatternLayout = new TenantAwarePatternLayout();
        tenantAwarePatternLayout.setTenantPattern(pattern);
        Assert.assertEquals(field.get(tenantAwarePatternLayout), pattern);
    }

    @Test(groups = {"org.wso2.carbon.utils.logging"}, dependsOnMethods = "testCreateTenantAwarePatternLayout1")
    public void testCreateTenantAwarePatternParser() throws Exception {
        TenantAwarePatternLayout tenantAwarePatternLayout = new TenantAwarePatternLayout();
        Assert.assertNotNull(tenantAwarePatternLayout.createPatternParser(pattern));
    }

    @Test(groups = {"org.wso2.carbon.utils.logging"}, dependsOnMethods = "testCreateTenantAwarePatternParser")
    public void testSetLogUUIDUpdateInterval() throws Exception {
        Field field = TenantAwarePatternLayout.class.getDeclaredField("logUUID");
        field.setAccessible(true);
        TenantAwarePatternLayout tenantAwarePatternLayout = new TenantAwarePatternLayout(pattern);
        tenantAwarePatternLayout.setLogUUIDUpdateInterval("1");
        Assert.assertNotNull(field.get(tenantAwarePatternLayout));
    }

    @Test(groups = {"org.wso2.carbon.utils.logging"})
    public void testLoggingEventFormat() throws Exception {
        String tenantDomain = "test.domain";
        String applicationName = "testApp";
        String userName = "testUser";
        String instanceId = "testInstance";
        String serverKey = "testServer";
        Level logLevel = Level.INFO;
        String fqcn = "org.example.Test";
        String message = "Hello";
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        LoggingEvent loggingEvent = new LoggingEvent(fqcn, Logger.getLogger(fqcn), logLevel, message,
                new RuntimeException());
        try {
            PrivilegedCarbonContext.destroyCurrentContext();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setUsername(userName);
            carbonContext.setApplicationName(applicationName);
            System.setProperty("carbon.instance.name", instanceId);
            System.setProperty("product.key", serverKey);
            initTestServerConfiguration();
            TenantAwarePatternLayout tenantAwarePatternLayout = new TenantAwarePatternLayout(pattern);
            Field field = TenantAwarePatternLayout.class.getDeclaredField("logUUID");
            field.setAccessible(true);
            UUID uuid = (UUID) field.get(tenantAwarePatternLayout);
            String actual = tenantAwarePatternLayout.format(loggingEvent);
            String expected = String.format("TID\\: \\[\\] \\[%s\\] \\[%s\\] \\[%s\\] \\[%s\\] \\[%s\\] \\[%s\\] " +
                            "\\[\\] \\[%s\\] \\[%s\\] \\[\\d{4}\\-\\d{2}\\-\\d{2} " +
                            "\\d{2}\\:\\d{2}\\:\\d{2}\\,\\d{3}\\]  %s \\{%s\\} \\-\\  %s%n",
                    serverKey, userName, applicationName, tenantDomain, instanceId, hostAddress, uuid.toString(),
                    userName + "@" + tenantDomain, logLevel.toString(), fqcn, message);

            Assert.assertTrue(actual.matches(expected));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @AfterTest
    public void cleanup() {
        PrivilegedCarbonContext.destroyCurrentContext();
    }
}
