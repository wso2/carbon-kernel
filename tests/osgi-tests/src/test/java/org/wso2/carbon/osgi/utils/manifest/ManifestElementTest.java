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
package org.wso2.carbon.osgi.utils.manifest;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;
import org.wso2.carbon.kernel.utils.manifest.ManifestElementParserException;

import java.util.Arrays;
import javax.inject.Inject;

/**
 * OSGi tests class to test org.wso2.carbon.kernel.utils.manifest.ManifestElement as OSGi service registration.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ManifestElementTest {
    private static final String PROVIDE_CAPABILITY = "Provide-Capability";

    @Inject
    BundleContext bundleContext;

    @Test
    public void testParseHeader() {
        Bundle carbonCoreBundle = Arrays.asList(bundleContext.getBundles())
                .stream()
                .filter(b -> b.getSymbolicName().equals("org.wso2.carbon.core"))
                .findFirst()
                .get();

        String key = carbonCoreBundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY);
        try {
            ManifestElement[] elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, key);
            Assert.assertTrue(elements.length > 0);

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
