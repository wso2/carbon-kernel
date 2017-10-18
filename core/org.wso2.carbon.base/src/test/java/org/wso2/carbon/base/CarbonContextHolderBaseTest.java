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

import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test class for CarbonContextHolderBase related methods.
 */
public class CarbonContextHolderBaseTest {

    @Test(groups = {"org.wso2.carbon.base"})
    public void testSetDiscoveryServiceProvider() throws Exception {
        DiscoveryService expectedDiscoveryServiceImpl = mock(DiscoveryService.class);
        CarbonContextHolderBase.setDiscoveryServiceProvider(expectedDiscoveryServiceImpl);
        DiscoveryService actualDiscoveryService = CarbonContextHolderBase.getDiscoveryServiceProvider();
        assertEquals(actualDiscoveryService, expectedDiscoveryServiceImpl);
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testRegisterUnloadTenantTask() throws Exception {
        UnloadTenantTask unloadTenantTask = mock(UnloadTenantTask.class);
        CarbonContextHolderBase.registerUnloadTenantTask(unloadTenantTask);
        Field unloadTenantTaskslistField = CarbonContextHolderBase.class.getDeclaredField("unloadTenantTasks");
        unloadTenantTaskslistField.setAccessible(true);
        List<UnloadTenantTask> unloadTenantTaskslist = (List<UnloadTenantTask>) unloadTenantTaskslistField.get
                (CarbonContextHolderBase.getCurrentCarbonContextHolderBase());
        assertTrue(unloadTenantTaskslist.contains(unloadTenantTask));
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testStartTenantFlow() throws Exception {
        CarbonContextHolderBase.getCurrentCarbonContextHolderBase().startTenantFlow();
        Field propertiesField = CarbonContextHolderBase.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        int actualTenantId = CarbonContextHolderBase.getCurrentCarbonContextHolderBase().getTenantId();
        String actualUsername = CarbonContextHolderBase.getCurrentCarbonContextHolderBase().getUsername();
        String actualTenantDomain = CarbonContextHolderBase.getCurrentCarbonContextHolderBase().getTenantDomain();
        Map<String, Object> actualProperties = (Map<String, Object>) propertiesField.get(CarbonContextHolderBase
                .getCurrentCarbonContextHolderBase());

        int expectedTenantId = -1;
        String expectedUsername = null;
        String expectedTenantDomain = null;
        assertEquals(expectedTenantId, actualTenantId);
        assertEquals(expectedUsername, actualUsername);
        assertEquals(expectedTenantDomain, actualTenantDomain);
        assertTrue(actualProperties.isEmpty());
    }
}
