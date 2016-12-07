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

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;
import org.wso2.carbon.kernel.utils.manifest.ManifestElementParserException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * OSGi tests class to test org.wso2.carbon.kernel.utils.manifest.ManifestElement as OSGi service registration.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class ManifestElementOSGiTest {
    private static final String CARBON_COMPONENT = "Carbon-Component";

    @Inject
    BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[] { copyOSGiLibBundle(
                maven().artifactId("org.wso2.carbon.sample.deployer.mgt").groupId("org.wso2.carbon")
                        .versionAsInProject()), copyOSGiLibBundle(
                maven().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                        .versionAsInProject()) };
    }

    @Test
    public void testParseHeader() {
        Bundle carbonCoreBundle = Arrays.stream(bundleContext.getBundles())
                .filter(b -> b.getSymbolicName().equals("org.wso2.carbon.sample.deployer.mgt"))
                .findFirst()
                .get();

        String headerValue = carbonCoreBundle.getHeaders().get(CARBON_COMPONENT);
        try {
            List<ManifestElement> elements = ManifestElement
                    .parseHeader(CARBON_COMPONENT, headerValue, bundleContext.getBundle());
            Assert.assertTrue(elements.size() > 0);

            ManifestElement firstElement = elements.get(0);
            String value = firstElement.getValue();
            String strRepresentation = firstElement.toString();
            String[] elementsInManifest = strRepresentation.split(";");
            List<String> keys = Collections.list(firstElement.getKeys());

            Assert.assertEquals("startup.listener", value);
            Assert.assertEquals(elementsInManifest.length, 3);
            Assert.assertEquals(keys.size(), 2);
        } catch (ManifestElementParserException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testParseHeaderEmptyValueTest() {
        try {
            List<ManifestElement> elements = ManifestElement
                    .parseHeader(CARBON_COMPONENT, null, bundleContext.getBundle());
            Assert.assertEquals(0, elements.size());
        } catch (ManifestElementParserException e) {
            Assert.assertTrue(false);
        }
    }
}
