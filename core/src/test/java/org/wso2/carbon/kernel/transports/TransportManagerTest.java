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
    private CustomCarbonTransport carbonTransport3;

    public TransportManagerTest() {
    }

    @BeforeTest
    public void setup() {
        transportManager = new TransportManager();
        carbonTransport = new CustomCarbonTransport("dummyId");
        carbonTransport2 = new CustomCarbonTransport("dummyId2");
        carbonTransport3 = new CustomCarbonTransport("dummyId3");
        transportManager.registerTransport(carbonTransport);
        transportManager.registerTransport(carbonTransport2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "wrongId not found")
    public void testUnsuccessfullStartTransport() {
        transportManager.startTransport("wrongId");
    }

    @Test(dependsOnMethods = {"testUnsuccessfullStartTransport"})
    public void testSuccessfullStartTransport() {
        try {
            transportManager.startTransport("dummyId");
        } catch (IllegalArgumentException e) {
            Assert.assertFalse(false, "");
        }
    }

    @Test(dependsOnMethods = {"testSuccessfullStartTransport"}, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "wrongId not found")
    public void testUnsuccessfullStopTransport() {
        transportManager.stopTransport("wrongId");
    }

    @Test(dependsOnMethods = {"testUnsuccessfullStopTransport"})
    public void testSuccessfullStopTransport() {
        try {
            transportManager.stopTransport("dummyId");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(false, "failed to stop the transport.");
        }
    }

    @Test(dependsOnMethods = {"testSuccessfullStopTransport"}, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "dummyId not found")
    public void testUnregisterTransportFail() {
        transportManager.unregisterTransport(carbonTransport);
        transportManager.stopTransport(carbonTransport.getId());
    }

    @Test(dependsOnMethods = {"testUnregisterTransportFail"})
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
        transportManager.startTransport(carbonTransport2.getId());
        transportManager.beginMaintenance();
    }

    @Test(dependsOnMethods = {"testSuccessfulBeginMaintenance"})
    public void testSuccessfulEndMaintenance() {
        try {
            transportManager.endMaintenance();
            transportManager.unregisterTransport(carbonTransport2);
        } catch (IllegalStateException e) {
            Assert.assertTrue(false, "attempting to unregister transport in an illegal state");
        }
    }

    @Test(dependsOnMethods = {"testSuccessfulEndMaintenance"}, expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Cannot start transport dummyId3. Current state: STARTED")
    public void testUnSuccessfulStartOfAlreadyStartedTransports() {
        transportManager.registerTransport(carbonTransport3);
        transportManager.startTransport(carbonTransport3.getId());
        //startTransports will try to start a transport which is already started. Thus this
        //will throw an IllegalStateException.
        transportManager.startTransports();
    }

    @Test(dependsOnMethods = {"testUnSuccessfulStartOfAlreadyStartedTransports"},
            expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Cannot stop transport dummyId3. Current state: STOPPED")
    public void testUnSuccessfulStoppingAlreadyStoppedTransport() {
        transportManager.stopTransport(carbonTransport3.getId());
        transportManager.stopTransports();
    }

    @Test(dependsOnMethods = {"testUnSuccessfulStoppingAlreadyStoppedTransport"})
    public void testSuccessfulStartTransports() {
        try {
            transportManager.startTransports();
        } catch (IllegalStateException e) {
            Assert.assertTrue(false, "Illegal state of transport when trying to start the transport.");
        }
    }
}
