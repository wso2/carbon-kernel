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
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * CarbonTomcatServiceComponentTest includes test scenarios for
 * [1] functions, setServerConfigurationService (), unsetServerConfigurationService (),
 * setCarbonTomcatService () and unsetCarbonTomcatService () of CarbonTomcatServiceComponent.
 * @since 4.4.19
 */
public class CarbonTomcatServiceComponentTest {

    private static final Logger log = Logger.getLogger("CarbonTomcatServiceComponentTest");

    /**
     * Checks setServerConfigurationService () and unsetServerConfigurationService () functionality.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testServerConfigurationService () {
        // mocking inputs
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        // calling setServerConfigurationService () functionality
        CarbonTomcatServiceComponent carbonTomcatServiceComponent = new CarbonTomcatServiceComponent();
        carbonTomcatServiceComponent.setServerConfigurationService(serverConfigurationService);
        log.info("Testing if mocked server configuration service instance " +
                "was set to Carbon tomcat service holder");
        Assert.assertEquals(CarbonTomcatServiceHolder.getServerConfigurationService(), serverConfigurationService,
                "Carbon tomcat service holder does not carry the correct server configuration service instance " +
                        "set by Carbon tomcat service component");
        // calling unsetServerConfigurationService () functionality
        carbonTomcatServiceComponent.unsetServerConfigurationService(serverConfigurationService);
        log.info("Testing if mocked server configuration service instance " +
                "was unset from Carbon tomcat service holder");
        Assert.assertEquals(CarbonTomcatServiceHolder.getServerConfigurationService(), null,
                "Carbon tomcat service holder does not correctly unset the server configuration service instance " +
                        "set by Carbon tomcat service component");
    }

    /**
     * Checks setCarbonTomcatService () and unsetCarbonTomcatService () functionality.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"})
    public void testCarbonTomcatService () {
        // mocking inputs
        CarbonTomcatService carbonTomcatService = mock(CarbonTomcatService.class);
        // calling setCarbonTomcatService () functionality
        CarbonTomcatServiceComponent carbonTomcatServiceComponent = new CarbonTomcatServiceComponent();
        carbonTomcatServiceComponent.setCarbonTomcatService(carbonTomcatService);
        log.info("Testing if mocked Carbon tomcat service instance " +
                "was set to Carbon tomcat service holder");
        Assert.assertEquals(CarbonTomcatServiceHolder.getCarbonTomcatService(), carbonTomcatService,
                "Carbon tomcat service holder does not carry the correct Carbon tomcat service instance " +
                        "set by Carbon tomcat service component");
        // calling unsetCarbonTomcatService () functionality
        carbonTomcatServiceComponent.unsetCarbonTomcatService(carbonTomcatService);
        log.info("Testing if mocked Carbon tomcat service instance " +
                "was unset from Carbon tomcat service holder");
        Assert.assertEquals(CarbonTomcatServiceHolder.getCarbonTomcatService(), null,
                "Carbon tomcat service holder does not correctly unset the Carbon tomcat service instance " +
                        "set by Carbon tomcat service component");
    }
}
