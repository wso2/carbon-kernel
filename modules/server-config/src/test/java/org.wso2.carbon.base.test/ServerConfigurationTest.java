/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.base.test;



    import org.testng.Assert;
    import org.testng.annotations.BeforeSuite;
    import org.testng.annotations.Test;
    import org.wso2.carbon.base.ServerConfiguration;
    import org.wso2.carbon.base.ServerConfigurationException;

    import java.io.File;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

public class ServerConfigurationTest extends BaseTest{

    protected String testDir = "src" + File.separator + "test" + File.separator;
    private final String CONFIG_FILE = "sample-carbon.xml";

    /**
     * @param testName
     */
    public ServerConfigurationTest(String testName) {
        super(testName);
    }

    @BeforeSuite
        public void doBeforeEachTest() throws ServerConfigurationException {

    }

        @Test
        public void testServerConfigInit() throws ServerConfigurationException {

            String nameProp = "Name";
            String nameValue = "${product.name}";

            String versionProp = "Version";
            String versionValue = "${product.version}";

            String packageProp = "Package";
            String packageValue = "org.wso2.carbon";

            String offsetProp = "Ports.Offset";
            String offsetValue = "0";

            String RMIRegistryPortProp = "Ports.JMX.RMIRegistryPort";
            String RMIRegistryPortValue = "9999";

            String RMIServerPortProp = "Ports.JMX.RMIServerPort";
            String RMIServerPortValue = "11111";

            File configFile = new File(getTestResourceFile(CONFIG_FILE).getAbsolutePath());
            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            serverConfiguration.init(configFile.getAbsolutePath());

            String[] nameProperties = serverConfiguration.getProperties(nameProp);
            String[] versionProperties = serverConfiguration.getProperties(versionProp);
            String[] packageProperties = serverConfiguration.getProperties(packageProp);
            String[] portOffsetProperties = serverConfiguration.getProperties(offsetProp);
            String[] PortJMXRegProperties = serverConfiguration.getProperties(RMIRegistryPortProp);
            String[] portJMXServerProperties = serverConfiguration.getProperties(RMIServerPortProp);

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
                Assert.assertEquals(RMIRegistryPortValue, PortJMXRegProperties[0]);
            }

            for (String prop : portJMXServerProperties) {
                Assert.assertEquals(RMIServerPortValue, portJMXServerProperties[0]);
            }
        }

}
