/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.utils.component.xml.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Test class for DeployerConfigBuilder and its methods.
 */
public class DeployerConfigBuilderTest extends BaseTest {
    private final static String componentXmlFileName = "component_deployer_config.xml";
    private final static String deployerConfigKey = "deployers";

    @Test(groups = "org.wso2.carbon.utils.component.xml.builder")
    public void testBuild() throws Exception {
        InputStream xmlStream = new FileInputStream(Paths.get(testDir, componentXmlFileName).toFile());
        StAXOMBuilder builder = new StAXOMBuilder(xmlStream);
        OMElement document = builder.getDocumentElement();

        for (Iterator itr = document.getChildElements(); itr.hasNext(); ) {
            OMElement configElement = (OMElement) itr.next();
            String configKey = configElement.getLocalName();

            Assert.assertEquals(configKey, deployerConfigKey);
            ComponentConfigBuilder configBuilder = new DeployerConfigBuilder();

            DeployerConfig[] deployerConfigs = (DeployerConfig[]) configBuilder.build(configElement);
            for (DeployerConfig deployerConfig : deployerConfigs) {
                switch (deployerConfig.getDirectory()) {
                    case "carbonapps":
                        Assert.assertEquals(deployerConfig.getClassStr(),
                                "org.wso2.carbon.application.deployer.CappAxis2Deployer");
                        Assert.assertEquals(deployerConfig.getExtension(),
                                "car");
                        break;
                    case "webapps":
                        Assert.assertEquals(deployerConfig.getClassStr(),
                                "org.wso2.carbon.application.deployer.WebappDeployer");
                        Assert.assertEquals(deployerConfig.getExtension(),
                                "war");
                        break;
                    default:
                        Assert.fail();
                }
            }
        }
    }
}
