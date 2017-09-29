/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.base;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by kasun on 9/29/17.
 */
public class CarbonContextHolderBaseTest {
    private static CarbonContextHolderBase carbonContextHolderBase;
    private static DiscoveryService discoveryService;

    @BeforeClass
    public static void createAnInstance() {
        carbonContextHolderBase = CarbonContextHolderBase.getCurrentCarbonContextHolderBase();
        discoveryService = CarbonContextHolderBase.getDiscoveryServiceProvider();
    }

    @Test
    public void testSetDiscoveryServiceProvider() throws Exception {
        DiscoveryServiceImp expectedDiscoveryServiceImp = new DiscoveryServiceImp();
        CarbonContextHolderBase.setDiscoveryServiceProvider(expectedDiscoveryServiceImp);
        DiscoveryService actualDiscoveryService = CarbonContextHolderBase.getDiscoveryServiceProvider();
        assertEquals(expectedDiscoveryServiceImp, actualDiscoveryService);
    }

    @Test
    public void test1RegisterUnloadTenantTask() throws Exception {
        UnloadTenantTaskImpl unloadTenantTask = new UnloadTenantTaskImpl();
        CarbonContextHolderBase.registerUnloadTenantTask(unloadTenantTask);
        Field unloadTenantTaskslistField = CarbonContextHolderBase.class.getDeclaredField("unloadTenantTasks");
        unloadTenantTaskslistField.setAccessible(true);
        List<UnloadTenantTask> unloadTenantTaskslist = (List<UnloadTenantTask>) unloadTenantTaskslistField.get
                (carbonContextHolderBase);
        assertTrue(unloadTenantTaskslist.contains(unloadTenantTask));
    }


    @Test
    public void testStartTenantFlow() throws Exception {
        carbonContextHolderBase.startTenantFlow();
        Field propertiesField = CarbonContextHolderBase.class.getDeclaredField("properties");

        propertiesField.setAccessible(true);

        int actualTenantId = carbonContextHolderBase.getTenantId();
        String actualUsername = carbonContextHolderBase.getUsername();
        String actualTenantDomain = carbonContextHolderBase.getTenantDomain();
        Map<String, Object> actualPropertied = (Map < String, Object>)propertiesField.get(carbonContextHolderBase);

        int expectedTenantId = -1;
        String expectedUsername = null;
        String expectedTenantDomain = null;
        assertEquals(expectedTenantId, actualTenantId);
        assertEquals(expectedUsername, actualUsername);
        assertEquals(expectedTenantDomain, actualTenantDomain);
        assertTrue(actualPropertied.isEmpty());
    }

    @Test
    public void destroyCurrentCarbonContextHolder() throws Exception {
    }

}