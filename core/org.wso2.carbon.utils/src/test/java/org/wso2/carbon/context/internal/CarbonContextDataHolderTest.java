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
package org.wso2.carbon.context.internal;

import org.apache.naming.java.javaURLContextFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.queuing.CarbonQueue;
import org.wso2.carbon.queuing.CarbonQueueManager;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantCarbonQueueManager;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import java.util.Hashtable;

import static org.mockito.Mockito.when;

/**
 * Test cases for CarbonContextDataHolder.
 */
@Test(dependsOnGroups = {"org.wso2.carbon.context", "org.wso2.carbon.utils.base"},
        description = "CarbonContextDataHolder related test cases")
public class CarbonContextDataHolderTest extends BaseTest {

    @Test
    public void testCarbonContextDataHolder() throws Exception {
        CarbonContextDataHolder carbonContextDataHolder1 = CarbonContextDataHolder.getThreadLocalCarbonContextHolder();
        CarbonContextDataHolder carbonContextDataHolder2 = CarbonContextDataHolder.getCurrentCarbonContextHolderBase();
        Assert.assertEquals(carbonContextDataHolder1, carbonContextDataHolder2);
    }

    @Test
    public void testRegistry() throws Exception {
        CarbonContextDataHolder carbonContextDataHolder = CarbonContextDataHolder.getThreadLocalCarbonContextHolder();
        Registry registry = Mockito.mock(Registry.class);
        carbonContextDataHolder.setConfigSystemRegistry(registry);
        carbonContextDataHolder.setConfigUserRegistry(registry);
        carbonContextDataHolder.setGovernanceSystemRegistry(registry);
        carbonContextDataHolder.setGovernanceUserRegistry(registry);

        Assert.assertEquals(carbonContextDataHolder.getConfigSystemRegistry(), registry);
        Assert.assertEquals(carbonContextDataHolder.getConfigUserRegistry(), registry);
        Assert.assertEquals(carbonContextDataHolder.getGovernanceSystemRegistry(), registry);
        Assert.assertEquals(carbonContextDataHolder.getGovernanceUserRegistry(), registry);
    }

    @Test
    public void testUserRealm() throws Exception {
        CarbonContextDataHolder.destroyCurrentCarbonContextHolder();
        UserRealmService userRealmService = Mockito.mock(UserRealmService.class);
        int tenantId = 1234;
        OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();
        dataHolder.setUserRealmService(userRealmService);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        when(userRealmService.getTenantUserRealm(tenantId)).thenReturn(userRealm);

        CarbonContextDataHolder carbonContextDataHolder = CarbonContextDataHolder.getThreadLocalCarbonContextHolder();
        carbonContextDataHolder.setTenantId(tenantId);

        Assert.assertEquals(carbonContextDataHolder.getUserRealm(), userRealm);
    }

    @Test
    public void testQueueManager() throws Exception {
        int tenantId = -1234;
        String queueName = "queue";
        CarbonContextDataHolder carbonContextDataHolder = CarbonContextDataHolder.getThreadLocalCarbonContextHolder();
        carbonContextDataHolder.setTenantId(tenantId);
        MultitenantCarbonQueueManager queueManager = Mockito.mock(MultitenantCarbonQueueManager.class);
        CarbonQueue queue = Mockito.mock(CarbonQueue.class);
        when(queueManager.getQueue(queueName, tenantId)).thenReturn(queue);
        carbonContextDataHolder.setQueueManager(queueManager);
        Assert.assertEquals(queue, CarbonQueueManager.getInstance().getQueue(queueName));

        carbonContextDataHolder.removeQueueManager();
        Assert.assertNull(CarbonQueueManager.getInstance().getQueue(queueName));
    }

    @Test
    public void testTenantInitialContext() throws Exception {
        try {
            InitialContext initialContext = getInitialContext();
            String tenantDomain = "testTenant12345";
            int tenantID = 9876;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain);
            carbonContext.setTenantId(tenantID);
            String resource = "helloTenant";
            String jndiUrl = "tenantTest";
            initialContext.bind(jndiUrl, resource);
            Assert.assertEquals(initialContext.lookup(jndiUrl), resource);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test
    public void testCarbonJNDI1() throws Exception {
        String resource = "hello";
        String jndiUrl = "java:test";
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiUrl, resource);
        Assert.assertEquals(initialContext.lookup(jndiUrl), resource);
    }

    @Test (expectedExceptions = NameNotFoundException.class,
            expectedExceptionsMessageRegExp = ".* is not bound in this Context.*")
    public void testCarbonJNDI2() throws Exception {
        String resource = "hello";
        String anotherResource = "helloAgain";
        String jndiUrl1 = "test1";
        String subJNDIUrl1 = "subTest1";
        Name subJNDIName1 = new CompositeName(subJNDIUrl1);
        String subJNDIUrl2 = "subTest2";
        Name subJNDIName2 = new CompositeName(subJNDIUrl2);
        Name jndiName1 = new CompositeName(jndiUrl1);
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiName1, resource);
        Assert.assertEquals(initialContext.lookup(jndiName1), resource);

        initialContext.rebind(jndiName1, anotherResource);

        Assert.assertNotEquals(initialContext.lookup(jndiName1), resource);
        Assert.assertEquals(initialContext.lookup(jndiName1), anotherResource);

        initialContext.unbind(jndiName1);
        initialContext.bind(jndiName1, resource);

        Assert.assertEquals(initialContext.lookup(jndiName1), resource);

        initialContext.rebind(jndiUrl1, anotherResource);
        Assert.assertEquals(initialContext.lookup(jndiUrl1), anotherResource);

        initialContext.unbind(jndiName1);
        initialContext.bind(jndiUrl1, resource);
        Assert.assertEquals(initialContext.lookup(jndiUrl1), resource);

        Context subContext1 = initialContext.createSubcontext(subJNDIUrl1);
        Context subContext2 = initialContext.createSubcontext(subJNDIUrl2);

        subContext1.bind(subJNDIUrl1, resource);
        subContext2.bind(subJNDIUrl2, anotherResource);

        Assert.assertEquals(subContext1.lookup(subJNDIUrl1), resource);
        Assert.assertEquals(subContext2.lookup(subJNDIUrl2), anotherResource);

        Assert.assertEquals(initialContext.lookup(subJNDIUrl1), subContext1);

        initialContext.destroySubcontext(subJNDIUrl1);
        initialContext.destroySubcontext(subJNDIUrl2);


        Context subJNDIContext1 = initialContext.createSubcontext(subJNDIName1);
        Context subJNDIContext2 = initialContext.createSubcontext(subJNDIName2);

        subJNDIContext1.bind(subJNDIUrl1, resource);
        subJNDIContext2.bind(subJNDIUrl2, anotherResource);

        Assert.assertEquals(subJNDIContext1.lookup(subJNDIUrl1), resource);
        Assert.assertEquals(subJNDIContext2.lookup(subJNDIUrl2), anotherResource);

        Assert.assertEquals(initialContext.lookup(subJNDIUrl1), subJNDIContext1);

        initialContext.destroySubcontext(subJNDIName1);
        initialContext.destroySubcontext(subJNDIName2);

        initialContext.lookup(subJNDIUrl1);
    }


    @Test (expectedExceptions = NameNotFoundException.class,
            expectedExceptionsMessageRegExp = ".* is not bound in this Context.*")
    public void testCarbonJNDI3() throws Exception {
        String resource = "hello";
        Name jndiOldName = new CompositeName("java:test3");
        Name jndiNewName = new CompositeName("java:test4");
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiOldName, resource);
        Assert.assertEquals(initialContext.lookup(jndiOldName), resource);
        initialContext.rename(jndiOldName, jndiNewName);
        Assert.assertEquals(initialContext.lookup(jndiNewName), resource);
        initialContext.lookup(jndiOldName);
    }

    @Test (expectedExceptions = NameNotFoundException.class,
            expectedExceptionsMessageRegExp = ".* is not bound in this Context.*")
    public void testCarbonJNDI4() throws Exception {
        String resource = "hello";
        String jndiOldName = "java:test5";
        String jndiNewName = "java:test6";
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiOldName, resource);
        Assert.assertEquals(initialContext.lookup(jndiOldName), resource);
        initialContext.rename(jndiOldName, jndiNewName);
        Assert.assertEquals(initialContext.lookup(jndiNewName), resource);
        initialContext.lookup(jndiOldName);
    }

    @Test
    public void testCarbonJNDI5() throws Exception {
        String resource = "hello";
        String jndiName = "test5";
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiName, resource);
        NamingEnumeration contexts  = initialContext.list("");
        boolean found = false;
        while (contexts.hasMore()) {
            NameClassPair nc = (NameClassPair)contexts.next();
            if (jndiName.equals(nc.getName())) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        found = false;

        NamingEnumeration bindings = initialContext.listBindings("");
        while (bindings.hasMore()) {
            NameClassPair nc = (NameClassPair)bindings.next();
            if (jndiName.equals(nc.getName())) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }


    @Test
    public void testCarbonJNDI6() throws Exception {
        String resource = "hello";
        String jndiUrl = "test6";
        Name jndiName = new CompositeName(jndiUrl);
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiUrl, resource);
        Assert.assertEquals(initialContext.lookupLink(jndiUrl), resource);
        Assert.assertEquals(initialContext.lookupLink(jndiName), resource);
    }

    @Test
    public void testCarbonJNDI7() throws Exception {
        String resource = "hello";
        String jndiUrl = "test7";
        Name jndiName = new CompositeName(jndiUrl);
        Name resourceName = new CompositeName(resource);
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiUrl, resource);
        NameParser nameParser1 = initialContext.getNameParser(jndiUrl);
        NameParser nameParser2 = initialContext.getNameParser(jndiName);
        Assert.assertEquals(nameParser1.parse(resource), resourceName);
        Assert.assertEquals(nameParser2.parse(resource), resourceName);
    }

    @Test
    public void testCarbonJNDI8() throws Exception {
        String resource = "hello";
        String jndiUrl = "test8";
        String composeName1 = "testCompose1";
        Name composeName2 = new CompositeName("testCompose2");
        InitialContext initialContext = getInitialContext();
        initialContext.bind(jndiUrl, resource);
        Assert.assertEquals(initialContext.composeName(composeName1, "java"), composeName1);
        Assert.assertEquals(initialContext.composeName(composeName2, new CompositeName("java")), composeName2);
    }

    @Test
    public void testCarbonJNDI9() throws Exception {
        String envKey = "testKey";
        String envVal = "testVal";
        InitialContext initialContext = getInitialContext();
        initialContext.addToEnvironment(envKey, envVal);

        Assert.assertTrue(initialContext.getEnvironment().containsKey(envKey));
        Assert.assertTrue(initialContext.getEnvironment().containsValue(envVal));

        initialContext.removeFromEnvironment(envKey);

        Assert.assertFalse(initialContext.getEnvironment().containsKey(envKey));
        Assert.assertFalse(initialContext.getEnvironment().containsValue(envVal));
    }

    private InitialContext getInitialContext() throws ServerConfigurationException, NamingException {
        initTestServerConfiguration();
        InitialContextFactory initialContextFactory = new javaURLContextFactory();
        Class<?> icfClazz = initialContextFactory.getClass();
        ServerConfiguration.getInstance().overrideConfigurationProperty("JNDI.DefaultInitialContextFactory",
                icfClazz.getName());
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.url.pkgs", "org.wso2.carbon.naming");
        return new InitialContext(env);
    }
}
