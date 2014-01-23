/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.saaj.integration;

import junit.framework.Assert;
import org.apache.axiom.attachments.Attachments;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.saaj.SAAJTestRunner;
import org.apache.axis2.saaj.TestUtils;
import org.apache.axis2.saaj.Validated;
import org.apache.axis2.util.Utils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@RunWith(SAAJTestRunner.class)
public class IntegrationTest extends Assert {

    static int port;
    public static final QName SERVICE_NAME = new QName("Echo");
    public static final QName OPERATION_NAME = new QName("echo");

    public static final String SAAJ_REPO =
            System.getProperty("basedir", ".") + "/" + "target/test-classes/saaj-repo";

    private String lastSoapAction; // Stores the last SOAP action received by the server
    
    // Set this to true to let the Echo service trigger a SOAPFault
    private boolean triggerFault;

    protected static String getAddress() {
        return "http://127.0.0.1:" +
                port +
                "/axis2/services/Echo";
    }
    
    @BeforeClass
    public static void initUtilServer() throws Exception {
        port = UtilServer.start(SAAJ_REPO);
        AxisConfiguration axisCfg =
                UtilServer.getConfigurationContext().getAxisConfiguration();
        axisCfg.addParameter(new Parameter("enableMTOM", "optional"));
        axisCfg.addParameter(new Parameter("enableSwA", "optional"));
    }

    @AfterClass
    public static void shutDownUtilServer() throws Exception {
        UtilServer.stop();
    }

    @Before
    public void setUp() throws Exception {
        MessageReceiver mr = new AbstractInOutMessageReceiver() {
            @Override
            public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage)
                    throws AxisFault {
                
                lastSoapAction = inMessage.getSoapAction();
                if (triggerFault) {
                    throw new AxisFault("Triggered SOAP fault as requested");
                } else {
                    outMessage.setEnvelope(inMessage.getEnvelope());
                    Attachments inAttachments = inMessage.getAttachmentMap();
                    Attachments outAttachments = outMessage.getAttachmentMap();
                    for (String contentId : inAttachments.getAllContentIDs()) {
                        if (!contentId.equals(inAttachments.getSOAPPartContentID())) {
                            outAttachments.addDataHandler(contentId,
                                    inAttachments.getDataHandler(contentId));
                        }
                    }
                    outMessage.setDoingSwA(inMessage.isDoingSwA());
                    outMessage.setDoingMTOM(inMessage.isDoingMTOM());
                }
            }
        };
        UtilServer.deployService(
                Utils.createSimpleService(SERVICE_NAME, mr, null, OPERATION_NAME));
    }

    @After
    public void tearDown() throws Exception {
        UtilServer.unDeployService(SERVICE_NAME);
        UtilServer.unDeployClientService();
    }

    @Validated @Test
    public void testSendReceiveMessageWithEmptyNSPrefix() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage request = mf.createMessage();

        SOAPPart sPart = request.getSOAPPart();
        SOAPEnvelope env = sPart.getEnvelope();
        SOAPBody body = env.getBody();

        //Namespace prefix is empty
        body.addBodyElement(new QName("http://fakeNamespace2.org","echo"))
        							.addTextNode("This is some text");

        SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
        SOAPMessage response = sCon.call(request, getAddress());
        assertFalse(response.getAttachments().hasNext());
        assertEquals(0, response.countAttachments());

        String requestStr = printSOAPMessage(request);
        String responseStr = printSOAPMessage(response);
        assertTrue(responseStr.indexOf("echo") > -1);
        sCon.close();
    }
    
    @Validated @Test
    public void testSendReceiveSimpleSOAPMessage() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage request = mf.createMessage();

        createSimpleSOAPPart(request);

        SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
        SOAPMessage response = sCon.call(request, getAddress());
        assertFalse(response.getAttachments().hasNext());
        assertEquals(0, response.countAttachments());

        String requestStr = printSOAPMessage(request);
        String responseStr = printSOAPMessage(response);
        assertTrue(responseStr.indexOf("echo") != -1);
        sCon.close();
    }

    // TODO: it is not clear how this method can give predictable results,
    //       given that ByteArrayOutputStream#toString uses the platform default charset
    //       encoding while SOAPMessage#writeTo may use another encoding!!!
    private String printSOAPMessage(final SOAPMessage msg) throws SOAPException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);
        String responseStr = baos.toString();

        assertTrue(responseStr.indexOf("This is some text") != -1);
        return responseStr;
    }

    @Validated @Test
    public void testSendReceiveMessageWithAttachment() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage request = mf.createMessage();

        //create the SOAPPart
        createSOAPPart(request);

        //Attach a text/plain object with the SOAP request
        String sampleMessage = "Sample Message: Hello World!";
        AttachmentPart textAttach = request.createAttachmentPart(sampleMessage, "text/plain");
        textAttach.addMimeHeader("Content-Transfer-Encoding", "binary");
        textAttach.setContentId("submitSampleText@apache.org");
        request.addAttachmentPart(textAttach);

        //Attach a java.awt.Image object to the SOAP request
        DataHandler imageDH = new DataHandler(TestUtils.getTestFileAsDataSource("axis2.jpg"));
        AttachmentPart jpegAttach = request.createAttachmentPart(imageDH);
        jpegAttach.addMimeHeader("Content-Transfer-Encoding", "binary");
        jpegAttach.setContentId("submitSampleImage@apache.org");
        jpegAttach.setContentType("image/jpg");
        request.addAttachmentPart(jpegAttach);

        SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
        SOAPMessage response = sCon.call(request, getAddress());

        assertEquals(2, response.countAttachments());

        Iterator attachIter = response.getAttachments();

        int i = 0;
        while (attachIter.hasNext()) {
            AttachmentPart attachment = (AttachmentPart)attachIter.next();
            final Object content = attachment.getDataHandler().getContent();
            if (content instanceof String) {
                assertEquals(sampleMessage, (String)content);
            } else if (content instanceof ByteArrayInputStream) {
                ByteArrayInputStream bais = (ByteArrayInputStream)content;
                byte[] b = new byte[15000];
                final int lengthRead = bais.read(b);
                FileOutputStream fos =
                        new FileOutputStream(new File(System.getProperty("basedir", ".") + "/" +
                                "target/test-resources/result" + (i++) + ".jpg"));
                fos.write(b, 0, lengthRead);
                fos.flush();
                fos.close();

                assertTrue(attachment.getContentType().equals("image/jpeg")
                        || attachment.getContentType().equals("text/plain"));
            }
        }

        sCon.close();

    }

    private void createSOAPPart(SOAPMessage message) throws SOAPException {
        SOAPPart sPart = message.getSOAPPart();
        SOAPEnvelope env = sPart.getEnvelope();
        SOAPBody body = env.getBody();

        final SOAPHeader soapHeader = env.getHeader();
        soapHeader
                .addHeaderElement(env.createName("TestHeader1", "swa", "http://fakeNamespace.org"));
        soapHeader
                .addHeaderElement(env.createName("TestHeader2", "swa", "http://fakeNamespace.org"));
        final SOAPHeaderElement headerEle3 =
                soapHeader.addHeaderElement(
                        env.createName("TestHeader3", "swa", "http://fakeNamespace.org"));
        final SOAPElement ch1 = headerEle3.addChildElement("he3", "swa");
        ch1.addTextNode("Im Header Element of header3");

        Name ns = env.createName("echo", "swa", "http://fakeNamespace.org");
        SOAPBodyElement bodyElement = body.addBodyElement(ns);

        Name nameMain = env.createName("internal");
        SOAPElement mainChildEle = bodyElement.addChildElement(nameMain);

        Name ns2 = env.createName("text");
        SOAPElement textReference = mainChildEle.addChildElement(ns2);
        Name hrefAttr = env.createName("href");
        textReference.addAttribute(hrefAttr, "cid:submitSampleText@apache.org");

        Name ns3 = env.createName("image");
        SOAPElement imageReference = mainChildEle.addChildElement(ns3);
        Name ns31 = env.createName("inner");
        final SOAPElement img = imageReference.addChildElement(ns31);
        img.addAttribute(hrefAttr, "cid:submitSampleImage@apache.org");

        Name ns4 = env.createName("plaintxt");
        SOAPElement plainTxt = mainChildEle.addChildElement(ns4);
        plainTxt.addTextNode("This is simple plain text");

        Name ns5 = env.createName("nested");
        SOAPElement nested = mainChildEle.addChildElement(ns5);
        nested.addTextNode("Nested1 Plain Text");
        Name ns6 = env.createName("nested2");
        SOAPElement nested2 = nested.addChildElement(ns6);
        nested2.addTextNode("Nested2 Plain Text");
    }

    private void createSimpleSOAPPart(SOAPMessage message) throws SOAPException {
        SOAPPart sPart = message.getSOAPPart();
        SOAPEnvelope env = sPart.getEnvelope();
        SOAPBody body = env.getBody();
        SOAPHeader header = env.getHeader();
        header.addHeaderElement(env.createName("Header1",
                                               "pref",
                                               "http://test.apach.org/test"))
                .addTextNode("This is header1");

        Name ns = env.createName("echo", "swa2", "http://fakeNamespace2.org");
        final SOAPBodyElement bodyElement = body.addBodyElement(ns);
        Name ns2 = env.createName("something");
        final SOAPElement ele1 = bodyElement.addChildElement(ns2);
        ele1.addTextNode("This is some text");

        Name ns3 = env.createName("ping", "swa3", "http://fakeNamespace3.org");
        final SOAPBodyElement bodyElement2 = body.addBodyElement(ns3);
        Name ns4 = env.createName("another");
        final SOAPElement ele2 = bodyElement2.addChildElement(ns4);
        ele2.addTextNode("This is another text");
    }
    
    @Validated @Test
    public void testSendReceive_ISO88591_EncodedSOAPMessage() throws Exception {
    	MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml; charset=iso-8859-1");
        
        InputStream inputStream = TestUtils.getTestFile("soap-part-iso-8859-1.xml");
        SOAPMessage requestMessage = MessageFactory.newInstance().createMessage(mimeHeaders, inputStream);
        

        SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
        SOAPMessage response = sCon.call(requestMessage, getAddress());
        assertFalse(response.getAttachments().hasNext());
        assertEquals(0, response.countAttachments());

        printSOAPMessage(requestMessage);
        String responseStr = printSOAPMessage(response);
        assertEquals("This is some text.Here are some special chars : \u00F6\u00C6\u00DA\u00AE\u00A4",
                     response.getSOAPBody().getElementsByTagName("something").item(0).getTextContent());
        assertTrue(responseStr.indexOf("echo") != -1);
        sCon.close();
    }    

    @Validated @Test
    public void testCallWithSOAPAction() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage request = mf.createMessage();

        String soapAction = "urn:test:echo";
        
        request.getSOAPPart().getEnvelope().getBody().addBodyElement(new QName("urn:test", "echo"));
        request.getMimeHeaders().addHeader("SOAPAction", soapAction);

        SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
        sCon.call(request, getAddress());
        sCon.close();
        
        assertEquals(soapAction, lastSoapAction);
    }
    
    @Validated @Test
    public void testCallMTOM() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", TestUtils.MTOM_TEST_MESSAGE_CONTENT_TYPE);
        InputStream in = TestUtils.getTestFile(TestUtils.MTOM_TEST_MESSAGE_FILE);
        SOAPMessage request = mf.createMessage(headers, in);
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
        
        // Remove the headers since they have mustunderstand=1 
        envelope.getHeader().removeContents();
        // Change the name of the body content so that the request is routed to the echo service
        ((SOAPElement)envelope.getBody().getChildElements().next()).setElementQName(new QName("echo"));
        
        SOAPConnection sCon = SOAPConnectionFactory.newInstance().createConnection();
        SOAPMessage response = sCon.call(request, getAddress());
        sCon.close();
        
        SOAPPart soapPart = response.getSOAPPart();
        SOAPElement textElement =
                (SOAPElement)soapPart.getEnvelope().getElementsByTagName("text").item(0);
        AttachmentPart ap = response.getAttachment((SOAPElement)textElement.getChildNodes().item(0));
        assertNotNull(ap);
    }
    
    /**
     * Checks that {@link org.apache.axis2.saaj.SOAPConnectionImpl} correctly performs transport
     * cleanup. If there is a problem with transport cleanup, then this test will fail with an error
     * caused by a timeout waiting for an available connection. This is a regression test for
     * AXIS2-4752.
     * 
     * @throws Exception
     */
    @Test
    public void testConnectionCleanup() throws Exception {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage request = mf.createMessage();
        SOAPBodyElement bodyElement = request.getSOAPBody().addBodyElement(new QName("urn:test", "echo"));
        for (int i=0; i<1000; i++) {
            bodyElement.addChildElement(new QName("test")).addTextNode("some text");
        }
        SOAPConnection conn = SOAPConnectionFactory.newInstance().createConnection();
        for (int i=0; i<100; i++) {
            // Let the Echo service trigger a SOAP fault on every second call. This allows us to check
            // that the connection cleanup is done correctly also if the response is a SOAP fault.
            triggerFault = i % 2 == 0;
            if (triggerFault) {
                try {
                    conn.call(request, getAddress());
                    fail("Expected SOAPException");
                } catch (SOAPException ex) {
                    // Expected
                }
            } else {
                SOAPMessage response = conn.call(request, getAddress());
                assertEquals(1000, ((Element)response.getSOAPBody().getFirstChild()).getChildNodes().getLength());
            }
        }
        conn.close();
    }
}
