/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.transports;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.transports.transporter.CustomCarbonTransport;

/**
 * Unit tests class for org.wso2.carbon.kernel.transports.TransportManager.
 *
 * @since 5.0.0
 */
public class TransportManagerTest {

    private TransportManager transportManager;
    private CustomCarbonTransport carbonTransport;
    private CustomCarbonTransport carbonTransport2;

    public TransportManagerTest() {
    }

    @BeforeTest
    public void setup() {
        transportManager = new TransportManager();
        carbonTransport = new CustomCarbonTransport("dummyId");
        carbonTransport2 = new CustomCarbonTransport("dummyId2");
        transportManager.registerTransport(carbonTransport);
        transportManager.registerTransport(carbonTransport2);
    }

    @Test
    public void testUnsuccessfullStartTransport() {
        try {
            transportManager.startTransport("wrongId");
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "wrongId not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }


    @Test(dependsOnMethods = {"testUnsuccessfullStartTransport"})
    public void testSuccessfullStartTransport() {
        try {
            transportManager.startTransport("dummyId");
        } catch (IllegalArgumentException e) {
            Assert.assertFalse(true);
        }
        Assert.assertTrue(true);
    }

    @Test(dependsOnMethods = {"testSuccessfullStartTransport"})
    public void testUnsuccessfullStopTransport() {
        try {
            transportManager.stopTransport("wrongId");
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "wrongId not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfullStopTransport"})
    public void testSuccessfullStopTransport() {
        try {
            transportManager.stopTransport("dummyId");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    @Test(dependsOnMethods = {"testSuccessfullStopTransport"})
    public void testUnregisterTransport() {
        try {
            transportManager.unregisterTransport(carbonTransport);
            transportManager.stopTransport(carbonTransport.getId());
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "dummyId not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnregisterTransport"})
    public void testUnsuccessfulBeginMaintenance() {
        try {
            transportManager.registerTransport(carbonTransport2);
            transportManager.beginMaintenance();
        } catch (IllegalStateException e) {
            String exceptionMessage = "Cannot put transport dummyId2 into maintenance. Current state: UNINITIALIZED";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfulBeginMaintenance"})
    public void testSuccessfulBeginMaintenance() {
        try {
            transportManager.registerTransport(carbonTransport2);
            transportManager.startTransport(carbonTransport2.getId());
            transportManager.beginMaintenance();
        } catch (IllegalStateException e) {
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

}
