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
package org.wso2.carbon.osgi.transport;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.transports.TransportManager;

import javax.inject.Inject;

/**
 * OSGi tests class to test org.wso2.carbon.kernel.transports.TransportManager.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TransportManagerTest {
    private static final Logger logger = LoggerFactory.getLogger(TransportManagerTest.class);

    @Inject
    private TransportManager transportManager;

    @Test
    public void testTransportManagerExistence() {
        Assert.assertNotNull(transportManager, "TransportManager Service is null");
    }

    @Test(dependsOnMethods = {"testTransportManagerExistence"})
    public void testUnsuccessfulStartTransport() {
        try {
            transportManager.startTransport("wrongId");
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "wrongId not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfulStartTransport"})
    public void testSuccessfulStartTransport() {
        CustomCarbonTransport carbonTransport = new CustomCarbonTransport("dummyTransport");
        transportManager.registerTransport(carbonTransport);
        transportManager.startTransport("dummyTransport");
    }

    @Test(dependsOnMethods = {"testSuccessfulStartTransport"})
    public void testSuccessfulStopTransport() {
        transportManager.stopTransport("dummyTransport");
    }

    @Test(dependsOnMethods = {"testSuccessfulStopTransport"})
    public void testUnsuccessfulStopTransport() {
        try {
            transportManager.stopTransport("wrongId");
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "wrongId not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfulStopTransport"})
    public void testUnregisterTransport() {
        try {
            CustomCarbonTransport carbonTransport = new CustomCarbonTransport("dummyTransport");
            transportManager.unregisterTransport(carbonTransport);
            transportManager.stopTransport(carbonTransport.getId());
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "dummyTransport not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnregisterTransport"})
    public void testUnsuccessfulBeginMaintenance() {
        CustomCarbonTransport carbonTransport = new CustomCarbonTransport("dummyTransport");
        try {
            transportManager.registerTransport(carbonTransport);
            transportManager.beginMaintenance();
        } catch (IllegalStateException e) {
            String exceptionMessage =
                    "Cannot put transport dummyTransport into maintenance. Current state: UNINITIALIZED";
            transportManager.unregisterTransport(carbonTransport);
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfulBeginMaintenance"})
    public void testSuccessfulBeginMaintenance() {
        CustomCarbonTransport carbonTransport = new CustomCarbonTransport("dummyTransport");
        transportManager.registerTransport(carbonTransport);
        transportManager.startTransport(carbonTransport.getId());
        transportManager.beginMaintenance();
    }
}
