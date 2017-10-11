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
import org.wso2.carbon.utils.component.xml.config.HTTPGetRequestProcessorConfig;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Test class for HttpGetProcessorBuilder and its methods.
 */
public class HttpGetProcessorBuilderTest extends BaseTest {
    private final static String componentXmlFileName = "component_http_get_processor_config.xml";
    private final static String httpGetProcessorConfigKey = "httpGetRequestProcessors";

    @Test(groups = "org.wso2.carbon.utils.component.xml.builder")
    public void testBuild() throws Exception {
        InputStream xmlStream = new FileInputStream(Paths.get(testDir, componentXmlFileName).toFile());
        StAXOMBuilder builder = new StAXOMBuilder(xmlStream);
        OMElement document = builder.getDocumentElement();

        for (Iterator itr = document.getChildElements(); itr.hasNext(); ) {
            OMElement configElement = (OMElement) itr.next();
            String configKey = configElement.getLocalName();

            Assert.assertEquals(configKey, httpGetProcessorConfigKey);
            ComponentConfigBuilder configBuilder = new HTTPGetRequestProcessorConfigBuilder();

            HTTPGetRequestProcessorConfig[] httpGetRequestProcessorConfigs =
                    (HTTPGetRequestProcessorConfig[]) configBuilder.build(configElement);
            for (HTTPGetRequestProcessorConfig config : httpGetRequestProcessorConfigs) {
                switch (config.getItem()) {
                    case "stub":
                        Assert.assertEquals(config.getClassName(),
                                "org.wso2.carbon.wsdl2form.StubRequestProcessor");
                        break;
                    case "wsdl2form":
                        Assert.assertEquals(config.getClassName(),
                                "org.wso2.carbon.wsdl2form.WSDL2FormRequestProcessor");
                        break;
                    default:
                        Assert.fail();
                }
            }
        }
    }
}
