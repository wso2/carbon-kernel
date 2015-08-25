/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.base;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ServerConfigurationTest extends BaseTest {

    protected String testDir = "src" + File.separator + "test" + File.separator;
    private static final String CONFIG_FILE = "sample-carbon.xml";

    /**
     * @param testName
     */
    public ServerConfigurationTest(String testName) {
        super(testName);
    }

    @Test
    public void testServerConfigInit1() throws ServerConfigurationException {

        String nameProp = "Name";
        String nameValue = "${product.name}";

        String versionProp = "Version";
        String versionValue = "${product.version}";

        String packageProp = "Package";
        String packageValue = "org.wso2.carbon";

        String offsetProp = "Ports.Offset";
        String offsetValue = "0";

        String rmiRegistryPortProp = "Ports.JMX.RMIRegistryPort";
        String rmiRegistryPortValue = "9999";

        String rmiServerPortProp = "Ports.JMX.RMIServerPort";
        String rmiServerPortValue = "11111";

        File configFile = new File(getTestResourceFile(CONFIG_FILE).getAbsolutePath());
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        serverConfiguration.forceInit(configFile.getAbsolutePath());

        String[] nameProperties = serverConfiguration.getProperties(nameProp);
        String[] versionProperties = serverConfiguration.getProperties(versionProp);
        String[] packageProperties = serverConfiguration.getProperties(packageProp);
        String[] portOffsetProperties = serverConfiguration.getProperties(offsetProp);
        String[] PortJMXRegProperties = serverConfiguration.getProperties(rmiRegistryPortProp);
        String[] portJMXServerProperties = serverConfiguration.getProperties(rmiServerPortProp);

        for (String prop : nameProperties) {
            Assert.assertEquals(nameValue, nameProperties[0]);
        }

        for (String prop : versionProperties) {
            Assert.assertEquals(versionValue, versionProperties[0]);
        }

        for (String prop : packageProperties) {
            Assert.assertEquals(packageValue, packageProperties[0]);
        }

        for (String prop : portOffsetProperties) {
            Assert.assertEquals(offsetValue, portOffsetProperties[0]);
        }

        for (String prop : PortJMXRegProperties) {
            Assert.assertEquals(rmiRegistryPortValue, PortJMXRegProperties[0]);
        }

        for (String prop : portJMXServerProperties) {
            Assert.assertEquals(rmiServerPortValue, portJMXServerProperties[0]);
        }
    }

    @Test
    public void testServerConfigInit2() throws ServerConfigurationException, FileNotFoundException {

        String nameProp = "Name";
        String nameValue = "${product.name}";

        String versionProp = "Version";
        String versionValue = "${product.version}";

        String packageProp = "Package";
        String packageValue = "org.wso2.carbon";

        String offsetProp = "Ports.Offset";
        String offsetValue = "0";

        String rmiRegistryPortProp = "Ports.JMX.RMIRegistryPort";
        String rmiRegistryPortValue = "9999";

        String rmiServerPortProp = "Ports.JMX.RMIServerPort";
        String rmiServerPortValue = "11111";

        File configFile = new File(getTestResourceFile(CONFIG_FILE).getAbsolutePath());
        InputStream inputStream =  new FileInputStream(configFile);
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
        serverConfiguration.forceInit(inputStream);

        String[] nameProperties = serverConfiguration.getProperties(nameProp);
        String[] versionProperties = serverConfiguration.getProperties(versionProp);
        String[] packageProperties = serverConfiguration.getProperties(packageProp);
        String[] portOffsetProperties = serverConfiguration.getProperties(offsetProp);
        String[] PortJMXRegProperties = serverConfiguration.getProperties(rmiRegistryPortProp);
        String[] portJMXServerProperties = serverConfiguration.getProperties(rmiServerPortProp);

        for (String prop : nameProperties) {
            Assert.assertEquals(nameValue, nameProperties[0]);
        }

        for (String prop : versionProperties) {
            Assert.assertEquals(versionValue, versionProperties[0]);
        }

        for (String prop : packageProperties) {
            Assert.assertEquals(packageValue, packageProperties[0]);
        }

        for (String prop : portOffsetProperties) {
            Assert.assertEquals(offsetValue, portOffsetProperties[0]);
        }

        for (String prop : PortJMXRegProperties) {
            Assert.assertEquals(rmiRegistryPortValue, PortJMXRegProperties[0]);
        }

        for (String prop : portJMXServerProperties) {
            Assert.assertEquals(rmiServerPortValue, portJMXServerProperties[0]);
        }
    }
}
