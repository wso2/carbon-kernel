package org.apache.axis2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;

import org.apache.axis2.namespace.Constants;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;


public class MessageContextBuilderTest extends AbstractTestCase {
    private OMNamespaceImpl nsp = new OMNamespaceImpl(Constants.URI_SOAP11_ENV, "soapenv");

    public MessageContextBuilderTest(String testName) {
        super(testName);
    }

    
    public void testSwitchNamespacePrefixes()
            throws Exception {

        // Incoming envelope has a "soapenv" prefix
        assertEquals("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH,
                MessageContextBuilder.switchNamespacePrefix("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH, nsp));

        // Incoming envelope has a "s" prefix 
        assertEquals("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH,
                MessageContextBuilder.switchNamespacePrefix("s:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH, nsp));

        // Incoming envelope uses default namespace and no prefixes 
        assertEquals("soapenv:" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH,
                MessageContextBuilder.switchNamespacePrefix(":" + SOAPConstants.FAULT_CODE_VERSION_MISMATCH, nsp));
    }
    
    public void testElementNamespaces() throws Exception {
        File file = getTestResourceFile("soapmessage.xml");
        SOAPBuilder soapBuilder = new SOAPBuilder();
        FileInputStream fis = new FileInputStream(file);
        MessageContext mc = new MessageContext();
        
        //Set up a mock envelope
        try {   
            OMElement envelope = soapBuilder.processDocument(fis, SOAP11Constants.SOAP_11_CONTENT_TYPE, mc);   
        } catch (Exception e) {
        }

        SOAPProcessingException e =  new SOAPProcessingException(
            "Transport level information does not match with SOAP" +
                    " Message namespace URI", "S:"  + ":" +
                        SOAPConstants.FAULT_CODE_VERSION_MISMATCH);

        AxisFault axisFault = AxisFault.makeFault(e);
        ConfigurationContext configContext = new ConfigurationContext(new AxisConfiguration());
        mc.setConfigurationContext(configContext);
        MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(mc, axisFault);

        SOAPFault fault = faultContext.getEnvelope().getBody().getFault();
        String se = faultContext.getEnvelope().getNamespace().getPrefix();
        
        assertEquals (fault.getCode().getTextAsQName().getPrefix(), se);
    }

    
    
}