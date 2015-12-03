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
        Bundle carbonCoreBundle = bundleContext.getBundles()[14];
        String key = carbonCoreBundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY);
        try {
            ManifestElement[] elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, key);
            Assert.assertEquals(3, elements.length);
            ManifestElement sampleElement = elements[1];
            Assert.assertEquals(sampleElement.getValue(), "osgi.service");
            String attribute = sampleElement.getAttribute("component-key");
            String[] attributesList = sampleElement.getAttributes("component-key");
            Assert.assertEquals(attribute, "carbon-runtime-mgt");
            Assert.assertEquals(attributesList.length, 1);

            String stringRepresentation = elements[0].toString();
            Assert.assertEquals("osgi.service;objectClass=\"org.eclipse.osgi.framework.console." +
                    "CommandProvider\";effective:=\"active\"", stringRepresentation);

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
