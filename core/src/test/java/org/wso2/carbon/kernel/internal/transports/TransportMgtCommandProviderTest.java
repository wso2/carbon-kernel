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
package org.wso2.carbon.kernel.internal.transports;

import org.testng.Assert;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.kernel.transports.TransportManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * This class test the functionality of org.wso2.carbon.kernel.internal.transports.TransportMgtCommandProvider.
 *
 * @since 5.0.0
 */
public class TransportMgtCommandProviderTest {
    private DummyTransport dummyTransportOne;
    private TransportManager transportManager;
    private TransportMgtCommandProvider transportMgtCommandProvider;
    private DummyInterpreter commandInterpreter;
    private DummyTransport[] dummyTransports;
    private String[] transportIdList;

    //    @BeforeClass
    public void init() {
        commandInterpreter = new DummyInterpreter();
        setupDummyTransports();
        transportMgtCommandProvider = new TransportMgtCommandProvider(transportManager);
    }

    private void setupDummyTransports() {
        dummyTransportOne = new DummyTransport("dummy-transport");
        transportManager = new TransportManager();
        transportManager.registerTransport(dummyTransportOne);

        Double randomNumber = Math.random() * 10000 % 100;
        int randomInt = randomNumber.intValue() + 100;

        dummyTransports = new DummyTransport[randomInt];

        transportIdList = new String[randomInt + 1];
        transportIdList[0] = "dummy-transport";

        String dummyTransportId;
        for (int i = 0; i < randomInt; i++) {
            dummyTransportId = "dummy-transport-" + i;
            dummyTransports[i] = new DummyTransport(dummyTransportId);
            transportManager.registerTransport(dummyTransports[i]);
            transportIdList[i + 1] = dummyTransportId;
        }

        commandInterpreter.setTransportIdList(transportIdList);
    }

    //    @Test
    public void testGetHelp() throws Exception {
        Assert.assertEquals(transportMgtCommandProvider.getHelp(), "---Transport Management---\n" +
                "\tstartTransport <transportName> - Start the specified transport with <transportName>.\n" +
                "\tstopTransport <transportName> - Stop the specified transport with <transportName>\n" +
                "\tstartTransports - Start all transports\n" +
                "\tstopTransports - Stop all transports\n" +
                "\tbeginMaintenance - Activate maintenance mode of all transports\n" +
                "\tendMaintenance - Deactivate maintenance mode of all transports\n" +
                "\tlistTransports - List all the available transports\n");
    }

    //    @Test
    public void test_startTransport() throws Exception {
        transportMgtCommandProvider._startTransport(commandInterpreter);
        Assert.assertEquals(dummyTransportOne.getStarted(), Boolean.TRUE);
    }

    //    @Test(dependsOnMethods = "test_startTransport")
    public void test_stopTransport() throws Exception {
        commandInterpreter.resetCounter();
        transportMgtCommandProvider._stopTransport(commandInterpreter);
        Assert.assertEquals(dummyTransportOne.getStopped(), Boolean.TRUE);
    }

    //    @Test(dependsOnMethods = "test_stopTransport")
    public void test_startTransports() throws Exception {
        transportMgtCommandProvider._startTransports(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getStarted());
        }
    }

    //    @Test(dependsOnMethods = "test_startTransports")
    public void test_stopTransports() throws Exception {
        commandInterpreter.resetCounter();

        transportMgtCommandProvider._stopTransports(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getStopped());
        }
    }

    //    @Test (dependsOnMethods = "test_stopTransports")
    public void test_beginMaintenance() throws Exception {
        commandInterpreter.resetCounter();

        transportMgtCommandProvider._beginMaintenance(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getBeganMaintenance());
        }

    }

    //    @Test (dependsOnMethods = "test_beginMaintenance")
    public void test_endMaintenance() throws Exception {
        commandInterpreter.resetCounter();

        transportMgtCommandProvider._endMaintenance(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getEndedMaintenance());
        }

    }

    //    @Test (dependsOnMethods = "test_endMaintenance")
    public void test_listTransports() throws Exception {
        String expectedOutputString = "";

        Map<String, CarbonTransport> map = transportManager.getTransports();
        for (String key : map.keySet()) {
            expectedOutputString += "Transport Name: " + map.get(key).getId() + "\t" + " State: STOPPED\n";
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);
        transportMgtCommandProvider._listTransports(commandInterpreter);
        System.out.flush();
        System.setOut(old);
        Assert.assertEquals(baos.toString(), expectedOutputString);
    }

    //    @Test (dependsOnMethods = "test_listTransports", expectedExceptions = IllegalArgumentException.class)
    public void test_startTransportForNullTransportValue() {
        commandInterpreter.resetCounter();
        commandInterpreter.setTransportIdListValuesToNull();
        transportMgtCommandProvider._startTransport(commandInterpreter);
    }

    //    @Test(dependsOnMethods = "test_startTransportForNullTransportValue",
    //            expectedExceptions = IllegalArgumentException.class)
    public void test_stopTransportForNullTransportValue() {
        commandInterpreter.resetCounter();
        transportMgtCommandProvider._stopTransport(commandInterpreter);
    }

    //    @Test (dependsOnMethods = "test_stopTransportForNullTransportValue",
    //            expectedExceptions = IllegalArgumentException.class)
    public void test_startTransportForEmptyStringTransportValue() {
        commandInterpreter.resetCounter();
        commandInterpreter.setTransportIdListValuesToEmptyString();
        transportMgtCommandProvider._startTransport(commandInterpreter);
    }

    //    @Test(dependsOnMethods = "test_startTransportForEmptyStringTransportValue",
    //            expectedExceptions = IllegalArgumentException.class)
    public void test_stopTransportForEmptyStringTransportValue() {
        commandInterpreter.resetCounter();
        transportMgtCommandProvider._stopTransport(commandInterpreter);
    }
}