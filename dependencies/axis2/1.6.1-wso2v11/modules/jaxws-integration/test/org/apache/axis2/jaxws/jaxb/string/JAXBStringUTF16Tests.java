package org.apache.axis2.jaxws.jaxb.string;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

public class JAXBStringUTF16Tests extends AbstractTestCase {
    String axisEndpoint = "http://localhost:6060/axis2/services/JAXBStringService.JAXBStringPortTypeImplPort";
    String axis2ProviderEndpoint = "http://localhost:6060/axis2/services/StringMessageProviderService.StringMessageProviderPort";

    public static Test suite() {
        return getTestSetup(new TestSuite(JAXBStringUTF16Tests.class));
    }

    private void runTest16(String value) {
        runTestWithUTF16(value, value);
    }

    private void runTest16(String value, String value1) {
        runTestWithUTF16(value, value1);
    }
    
    public void testSimpleString16BOM() throws Exception {
        // Call the Axis2 StringMessageProvider which has a check to ensure
        // that the BOM for UTF-16 is not written inside the message.
        runTestWithEncoding("a simple string", "a simple string", "UTF-16", axis2ProviderEndpoint);
    }

    public void testSimpleString16() throws Exception {
        runTest16("a simple string");
    }

    public void testStringWithApostrophes16() throws Exception {
        runTest16("this isn't a simple string");
    }

    public void testStringWithEntities16() throws Exception {
        runTest16("&amp;&lt;&gt;&apos;&quot;", "&amp;&lt;&gt;&apos;&quot;");
    }

    public void testStringWithRawEntities16() throws Exception {
        runTest16("&<>'\"", "&<>'\"");
    }

    public void testStringWithLeadingAndTrailingSpaces16() throws Exception {
        runTest16("          centered          ");
    }

    public void testWhitespace16() throws Exception {
        runTest16(" \n \t "); // note: \r fails
    }

    public void testFrenchAccents16() throws Exception {
        runTest16("\u00e0\u00e2\u00e4\u00e7\u00e8\u00e9\u00ea\u00eb\u00ee\u00ef\u00f4\u00f6\u00f9\u00fb\u00fc");
    }

    public void testGermanUmlauts16() throws Exception {
        runTest16(" Some text \u00df with \u00fc special \u00f6 chars \u00e4.");
    }

    public void testWelcomeUnicode1_16() throws Exception {
        // welcome in several languages
        runTest16(
                "Chinese (trad.) : \u6b61\u8fce  ");
    }

    public void testWelcomeUnicode2_16() throws Exception {
        // welcome in several languages
        runTest16(
                "Greek : \u03ba\u03b1\u03bb\u03ce\u03c2 \u03bf\u03c1\u03af\u03c3\u03b1\u03c4\u03b5");
    }

    public void testWelcomeUnicode3_16() throws Exception {
        // welcome in several languages
        runTest16(
                "Japanese : \u3088\u3046\u3053\u305d");
    }

    private void runTestWithUTF16(String input, String output) {
        runTestWithEncoding(input, output, "UTF-16");
    }
    private void runTestWithEncoding(String input, String output, String encoding) {
        runTestWithEncoding(input, output, encoding, axisEndpoint);
    }
    private void runTestWithEncoding(String input, String output, String encoding, String endpoint) {
        TestLogger.logger.debug("Test : " + getName());
        try {
            JAXBStringPortType myPort = (new JAXBStringService()).getJAXBStringPort();
            BindingProvider p = (BindingProvider) myPort;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

            if (encoding != null) {
                p.getRequestContext().put(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, encoding);
            }

            Echo request = new Echo();
            request.setArg(input);
            EchoResponse response = myPort.echoString(request);
            TestLogger.logger.debug(response.getResponse());
            assertEquals(output, response.getResponse());
        } catch (WebServiceException webEx) {
            webEx.printStackTrace();
            fail();
        }
    }
}

