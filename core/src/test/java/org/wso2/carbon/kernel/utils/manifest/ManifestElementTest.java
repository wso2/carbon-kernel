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
                "RequiredCapabilityListener\";capability-name=\"org.wso2.carbon.sample.transport.mgt.Transport," +
                "org.wso2.carbon.sample.runtime.mgt.Runtime" +
                "\";component-key=carbon-sample-transport-mgt,abc=org.wso2.carbon";

        try {
            List<ManifestElement> manifestElementList = ManifestElement.parseHeader(PROVIDE_CAPABILITY, key, null);
            Assert.assertTrue(manifestElementList.size() > 0);

            ManifestElement firstElement = manifestElementList.get(0);
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

    @Test(expectedExceptions = ManifestElementParserException.class)
    public void testParseHeaderFail() throws ManifestElementParserException {
        //Sample header string to create a list of manifest elements.
        String key = "abc=org.wso2.carbon;something:something,";
        ManifestElement.parseHeader(PROVIDE_CAPABILITY, key, null);
    }

    @Test
    public void testParseHeaderWithDirectives() throws ManifestElementParserException {
        //Sample header string to create a list of manifest elements.
        String key = "wireAdmin: cap=osgi.service;objectClass=org.osgi.service.wireadmin.WireAdmin;uses:=\"" +
                "org.osgi.ser" +
                "vice.wireadmin\";effective:=active";
        List<ManifestElement> elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, key, null);
        ManifestElement firstElement = elements.get(0);
        Assert.assertEquals(firstElement.getDirectives("effective")[0], "active");
    }

    @Test
    public void testParseHeaderEmptyValueTest() {
        try {
            List<ManifestElement> elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, null, null);
            Assert.assertEquals(0, elements.size());
        } catch (ManifestElementParserException e) {
            Assert.assertTrue(false);
        }
    }
}
