package org.apache.axis2.jaxws.jaxb.string;

import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.TestLogger;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

public class JAXBStringUTF8Tests extends AbstractTestCase {
    String axisEndpoint = "http://localhost:6060/axis2/services/JAXBStringService.JAXBStringPortTypeImplPort";

    public static Test suite() {
        return getTestSetup(new TestSuite(JAXBStringUTF8Tests.class));
    }

    private void runTest(String value) {
        runTestWithUTF8(value, value);
    }

    private void runTest(String value, String value1) {
        runTestWithUTF8(value, value1);
    }

    public void testSimpleString() throws Exception {
        runTest("a simple string");
    }

    public void testSimpleStringSwitchEncoding() throws Exception {
        String input = "a simple string";
        String output = "a simple string";
        
        // Run with different encodings to verify proper processing.
        runTestWithEncoding(input, output, null);  // no encoding means to use default, UTF-8
        runTestWithEncoding(input, output, "UTF-16");  // Make a call with UTF-16
        runTestWithEncoding(input, output, null);  // now try again...using default, UTF-8
    }
    
    public void testStringWithApostrophes() throws Exception {
        runTest("this isn't a simple string");
    }

    public void testStringWithEntities() throws Exception {
        runTest("&amp;&lt;&gt;&apos;&quot;", "&amp;&lt;&gt;&apos;&quot;");
    }

    public void testStringWithRawEntities() throws Exception {
        runTest("&<>'\"", "&<>'\"");
    }

    public void testStringWithLeadingAndTrailingSpaces() throws Exception {
        runTest("          centered          ");
    }

    public void testWhitespace() throws Exception {
        runTest(" \n \t "); // note: \r fails
    }

    public void testFrenchAccents() throws Exception {
        runTest("\u00e0\u00e2\u00e4\u00e7\u00e8\u00e9\u00ea\u00eb\u00ee\u00ef\u00f4\u00f6\u00f9\u00fb\u00fc");
    }

    public void testGermanUmlauts() throws Exception {
        runTest(" Some text \u00df with \u00fc special \u00f6 chars \u00e4.");
    }

    public void testWelcomeUnicode1() throws Exception {
        // welcome in several languages
        runTest(
                "Chinese (trad.) : \u6b61\u8fce  ");
    }

    public void testWelcomeUnicode2() throws Exception {
        // welcome in several languages
        runTest(
                "Greek : \u03ba\u03b1\u03bb\u03ce\u03c2 \u03bf\u03c1\u03af\u03c3\u03b1\u03c4\u03b5");
    }

    public void testWelcomeUnicode3() throws Exception {
        // welcome in several languages
        runTest(
                "Japanese : \u3088\u3046\u3053\u305d");
    }

    private void runTestWithUTF8(String input, String output) {
        runTestWithEncoding(input, output, null);  // no encoding means to use default, UTF-8
    }

    private void runTestWithEncoding(String input, String output, String encoding) {
        TestLogger.logger.debug("Test : " + getName());
        try {
            JAXBStringPortType myPort = (new JAXBStringService()).getJAXBStringPort();
            BindingProvider p = (BindingProvider) myPort;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

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
