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

import org.eclipse.osgi.framework.console.CommandProvider;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.transports.TransportManager;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;

/**
 * OSGi tests class to test org.wso2.carbon.kernel.transports.TransportManager.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TransportManagerOSGiTest {
    private static final String TRANSPORT_ID = "DummyTransport";

    @Inject
    private TransportManager transportManager;

    @Inject
    private CommandProvider transportCommandProvider;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Test
    public void testTransportManagerExistence() {
        Assert.assertNotNull(transportManager, "TransportManager Service is null");
    }

    @Test(dependsOnMethods = {"testTransportManagerExistence"})
    public void testUnsuccessfulStartTransport() {

        //wrong id
        try {
            transportManager.startTransport("wrongId");
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "wrongId not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }

        //start again
        try {
            CustomCarbonTransport carbonTransport = new CustomCarbonTransport(TRANSPORT_ID);
            transportManager.registerTransport(carbonTransport);
            transportManager.startTransport(TRANSPORT_ID);
            transportManager.startTransport(TRANSPORT_ID);
        } catch (IllegalStateException e) {
            String exceptionMessage = "Cannot start transport " + TRANSPORT_ID + ". Current state: STARTED";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfulStartTransport"})
    public void testSuccessfulStartTransport() {
        CustomCarbonTransport carbonTransport = new CustomCarbonTransport(TRANSPORT_ID);
        transportManager.registerTransport(carbonTransport);
        transportManager.startTransport(TRANSPORT_ID);
    }

    @Test(dependsOnMethods = {"testSuccessfulStartTransport"})
    public void testSuccessfulStopTransport() {
        transportManager.stopTransport(TRANSPORT_ID);
    }

    @Test(dependsOnMethods = {"testSuccessfulStopTransport"})
    public void testUnsuccessfulStopTransport() {
        //wrong id
        try {
            transportManager.stopTransport("wrongId");
        } catch (IllegalArgumentException e) {
            String exceptionMessage = "wrongId not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }

        //stop again
        try {
            transportManager.stopTransport(TRANSPORT_ID);
            transportManager.stopTransport(TRANSPORT_ID);
        } catch (IllegalStateException e) {
            String exceptionMessage = "Cannot stop transport " + TRANSPORT_ID + ". Current state: STOPPED";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfulStopTransport"})
    public void testUnregisterTransport() {
        try {
            CustomCarbonTransport carbonTransport = new CustomCarbonTransport(TRANSPORT_ID);
            transportManager.unregisterTransport(carbonTransport);
            transportManager.stopTransport(carbonTransport.getId());
        } catch (IllegalArgumentException e) {
            String exceptionMessage = TRANSPORT_ID + " not found";
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnregisterTransport"})
    public void testUnsuccessfulBeginMaintenance() {
        CustomCarbonTransport carbonTransport = new CustomCarbonTransport(TRANSPORT_ID);
        try {
            transportManager.registerTransport(carbonTransport);
            transportManager.beginMaintenance();
        } catch (IllegalStateException e) {
            String exceptionMessage =
                    "Cannot put transport " + TRANSPORT_ID + " into maintenance. Current state: UNINITIALIZED";
            transportManager.unregisterTransport(carbonTransport);
            Assert.assertEquals(exceptionMessage, e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testUnsuccessfulBeginMaintenance"})
    public void testSuccessfulBeginMaintenance() {
        CustomCarbonTransport carbonTransport = new CustomCarbonTransport(TRANSPORT_ID);
        transportManager.registerTransport(carbonTransport);
        transportManager.startTransport(carbonTransport.getId());
        transportManager.beginMaintenance();
        transportManager.endMaintenance();
    }

    @Test(dependsOnMethods = {"testSuccessfulBeginMaintenance"})
    public void testStartAndStopAllTransports() {
        CustomCarbonTransport carbonTransport = new CustomCarbonTransport(TRANSPORT_ID);
        transportManager.registerTransport(carbonTransport);
        transportManager.startTransports();
        transportManager.stopTransports();
    }

    @Test
    public void testCommandProvider() {
        Assert.assertNotNull(transportCommandProvider, "TransportCommandProvider is null");
        transportCommandProvider.getHelp();
    }
}
