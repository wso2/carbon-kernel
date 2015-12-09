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
package org.wso2.carbon.kernel.utils.manifest;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * Test class for ManifestElement class.
 *
 * @since 5.0.0
 */
public class ManifestElementTest {

    private static final String PROVIDE_CAPABILITY = "Provide-Capability";

    @Test
    public void testParseHeader() {
        //Sample header string to create a list of manifest elements.
        String key = "osgi.service;effective:=active;objectClass=\"org.wso2.carbon.kernel.startupresolver." +
                "RequiredCapabilityListener\";capability-name=\"org.wso2.carbon.sample.transport.mgt.Transport" +
                "\";component-key=carbon-sample-transport-mgt";

        try {
            ManifestElement[] elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, key);
            Assert.assertTrue(elements.length > 0);

            ManifestElement firstElement = elements[0];
            String value = firstElement.getValue();
            String strRepresentation = firstElement.toString();
            String[] elementsInManifest = strRepresentation.split(";");
            List<String> keys = Collections.list(firstElement.getKeys());
            String componentKeyAttribute = firstElement.getAttribute("component-key");

            Assert.assertEquals("osgi.service", value);
            Assert.assertEquals(elementsInManifest.length, 5);
            Assert.assertEquals(keys.size(), 3);
            Assert.assertEquals(componentKeyAttribute, "carbon-sample-transport-mgt");
        } catch (ManifestElementParserException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testParseHeaderEmptyValueTest() {
        try {
            ManifestElement[] elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, null);
            Assert.assertEquals(0, elements.length);
        } catch (ManifestElementParserException e) {
            Assert.assertTrue(false);
        }
    }
}
