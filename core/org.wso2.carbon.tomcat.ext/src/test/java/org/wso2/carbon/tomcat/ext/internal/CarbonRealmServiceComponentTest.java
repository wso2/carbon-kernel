/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.internal;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * CarbonRealmServiceComponentTest includes test scenarios for
 * [1] functions, setRealmService (), unsetRealmService (), setRegistryService () and
 * unsetRegistryService () of CarbonRealmServiceComponent.
 * @since 4.4.19
 */
public class CarbonRealmServiceComponentTest {

    private static final Logger log = Logger.getLogger("UtilsTest");

    /**
     * Checks setRealmService () and unsetRealmService () functionality.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testRealmService () {
        // mocking inputs
        RealmService realmService = mock(RealmService.class);
        // calling setRealmService () functionality
        CarbonRealmServiceComponent carbonRealmServiceComponent = new CarbonRealmServiceComponent();
        carbonRealmServiceComponent.setRealmService(realmService);
        log.info("Testing if mocked realm service instance was set to Carbon realm service holder");
        Assert.assertEquals(CarbonRealmServiceHolder.getRealmService(), realmService,
                "Carbon realm service holder does not carry the correct realm service instance set by " +
                        "Carbon realm service component");
        // calling unsetRealmService () functionality
        carbonRealmServiceComponent.unsetRealmService(realmService);
        log.info("Testing if mocked realm service instance was unset from Carbon realm service holder");
        Assert.assertEquals(CarbonRealmServiceHolder.getRealmService(), null,
                "Carbon realm service holder does not correctly unset the realm service instance set by " +
                        "Carbon realm service component");
    }

    /**
     * Checks setRegistryService () and unsetRegistryService () functionality.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testRegistryService () {
        // mocking inputs
        RegistryService registryService = mock(RegistryService.class);
        // calling setRegistryService () functionality
        CarbonRealmServiceComponent carbonRealmServiceComponent = new CarbonRealmServiceComponent();
        carbonRealmServiceComponent.setRegistryService(registryService);
        log.info("Testing if mocked registry service instance was set to Carbon realm service holder");
        Assert.assertEquals(CarbonRealmServiceHolder.getRegistryService(), registryService,
                "Carbon realm service holder does not carry the correct registry service instance set by " +
                        "Carbon realm service component");
        // calling unsetRegistryService () functionality
        carbonRealmServiceComponent.unsetRegistryService(registryService);
        log.info("Testing if mocked registry service instance was unset from Carbon realm service holder");
        Assert.assertEquals(CarbonRealmServiceHolder.getRegistryService(), null,
                "Carbon realm service holder does not correctly unset the registry service instance set by " +
                        "Carbon realm service component");
    }
}
