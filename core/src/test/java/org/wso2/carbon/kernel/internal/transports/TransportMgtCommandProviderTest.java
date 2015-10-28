package org.wso2.carbon.kernel.internal.transports;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.transports.TransportManager;

public class TransportMgtCommandProviderTest {
    private DummyTransport dummyTransportOne;
    private TransportManager transportManager;
    private TransportMgtCommandProvider transportMgtCommandProvider;
    private DummyInterpreter commandInterpreter;
    private DummyTransport[] dummyTransports;

    @BeforeClass
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

        String[] transportIdList = new String[randomInt + 1];
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

    @Test
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

    @Test
    public void test_startTransport() throws Exception {
        transportMgtCommandProvider._startTransport(commandInterpreter);
        Assert.assertEquals(dummyTransportOne.getStarted(), Boolean.TRUE);
    }

    @Test(dependsOnMethods = "test_startTransport")
    public void test_stopTransport() throws Exception {
        commandInterpreter.resetCounter();
        transportMgtCommandProvider._stopTransport(commandInterpreter);
        Assert.assertEquals(dummyTransportOne.getStopped(), Boolean.TRUE);
    }

    @Test(dependsOnMethods = "test_stopTransport")
    public void test_startTransports() throws Exception {
        transportMgtCommandProvider._startTransports(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getStarted());
        }
    }

    @Test(dependsOnMethods = "test_startTransports")
    public void test_stopTransports() throws Exception {
        commandInterpreter.resetCounter();

        transportMgtCommandProvider._stopTransports(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getStopped());
        }
    }

    @Test (dependsOnMethods = "test_stopTransports")
    public void test_beginMaintenance() throws Exception {
        commandInterpreter.resetCounter();

        transportMgtCommandProvider._beginMaintenance(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getBeganMaintenance());
        }

    }

    @Test (dependsOnMethods = "test_beginMaintenance")
    public void test_endMaintenance() throws Exception {
        commandInterpreter.resetCounter();

        transportMgtCommandProvider._endMaintenance(commandInterpreter);

        for (int i = 0; i < dummyTransports.length; i++) {
            Assert.assertTrue(dummyTransports[i].getEndedMaintenance());
        }

    }

    @Test (expectedExceptions = UnsupportedOperationException.class)
    public void test_listTransports() throws Exception {
        commandInterpreter.resetCounter();
        transportMgtCommandProvider._listTransports(commandInterpreter);
    }
}
