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
package org.apache.axiom.om.impl.builder;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.ds.ByteArrayDataSource;
import org.apache.axiom.om.ds.custombuilder.ByteArrayCustomBuilder;
import org.apache.axiom.om.util.CopyUtils;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Validates CopyUtils utility
 */
/**
 * @author scheu
 *
 */
// TODO: This class seems to be a copy and paste of CopyUtilsTest. Clean this up.
public class CustomBuilderTest extends AbstractTestCase {

    public CustomBuilderTest(String testName) {
        super(testName);
    }
    
    public void testSample1() throws Exception {
        copyAndCheck(createEnvelope(getTestResource(TestConstants.SAMPLE1), false), true);
    }
    
    
    public void testHeaderCustomBuilder() throws Exception{
        XMLStreamReader parser =
                StAXUtils.createXMLStreamReader(getTestResource(TestConstants.SOAP_SOAPMESSAGE));
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser, null);
        builder.registerCustomBuilder(new QName("http://schemas.xmlsoap.org/ws/2004/03/addressing","To"), 3, new
                                      ByteArrayCustomBuilder("utf-8"));
        SOAPEnvelope sourceEnv = (SOAPEnvelope) builder.getDocumentElement();
        SOAPHeader header = sourceEnv.getHeader();
        ArrayList al =
            header.getHeaderBlocksWithNSURI("http://schemas.xmlsoap.org/ws/2004/03/addressing");
        for(int i=0;i<al.size();i++){
            SOAPHeaderBlock shb = (SOAPHeaderBlock)al.get(i);
            if("To".equals(shb.getLocalName())){
                assertNotNull(shb.getDataSource());
            }
        }
    }
    
    /**
     * Test OMSE with SOAPMessage
     * @throws Exception
     */
    public void testSOAPMESSAGE() throws Exception {
        copyAndCheck(createEnvelope(getTestResource(TestConstants.SOAP_SOAPMESSAGE), false), true);
    }
    
    
    /** 
     * Same as testSOAPMessage, except that the a fault check is done too.
     * (The fault check simulates what happens in the engine.)
     * @throws Exception
     */
    public void testSOAPMESSAGE2() throws Exception {
        copyAndCheck(createEnvelope(getTestResource(TestConstants.SOAP_SOAPMESSAGE), true), true);
    }
    
    public void testWHITESPACE_MESSAGE() throws Exception {
        copyAndCheck(createEnvelope(getTestResource(TestConstants.WHITESPACE_MESSAGE), false), true);
    }
    
    public void testREALLY_BIG_MESSAGE() throws Exception {
        // Ignore the serialization comparison
        copyAndCheck(createEnvelope(getTestResource(TestConstants.REALLY_BIG_MESSAGE), false), false);
    }
    public void testOMSE() throws Exception {
        SOAPEnvelope sourceEnv = createEnvelope(getTestResource(TestConstants.EMPTY_BODY_MESSAGE), false);
        SOAPBody body = sourceEnv.getBody();
        
        // Create a payload
        String text = "<tns:payload xmlns:tns=\"urn://test\">Hello World</tns:payload>";
        String encoding = "UTF-8";
        ByteArrayDataSource bads = new ByteArrayDataSource(text.getBytes(encoding), encoding);
        OMNamespace ns = body.getOMFactory().createOMNamespace("urn://test", "tns");
        OMSourcedElement omse =body.getOMFactory().createOMElement(bads, "payload", ns);
        body.addChild(omse);
        copyAndCheck(sourceEnv, true);
    }
    
    public void testOMSE2() throws Exception {
        SOAPEnvelope sourceEnv = createEnvelope(getTestResource(TestConstants.EMPTY_BODY_MESSAGE), false);
        SOAPBody body = sourceEnv.getBody();
        SOAPHeader header = sourceEnv.getHeader();
        String encoding = "UTF-8";
        
        // Create a header OMSE
        String hdrText = "<hdr:myheader xmlns:hdr=\"urn://test\">Hello World</hdr:myheader>";
        ByteArrayDataSource badsHdr = 
            new ByteArrayDataSource(hdrText.getBytes(encoding), encoding);
        OMNamespace hdrNS = header.getOMFactory().createOMNamespace("urn://test", "hdr");
        SOAPFactory sf = (SOAPFactory) header.getOMFactory();
        SOAPHeaderBlock shb = sf.createSOAPHeaderBlock("myheader", hdrNS, badsHdr);
        shb.setProcessed();  // test setting processing flag
        header.addChild(shb);
        
        // Create a payload
        String text = "<tns:payload xmlns:tns=\"urn://test\">Hello World</tns:payload>";
        ByteArrayDataSource bads = new ByteArrayDataSource(text.getBytes(encoding), encoding);
        OMNamespace ns = body.getOMFactory().createOMNamespace("urn://test", "tns");
        OMSourcedElement omse =body.getOMFactory().createOMElement(bads, "payload", ns);
        body.addChild(omse);
        
        copyAndCheck(sourceEnv, true);
        
        // The source SOAPHeaderBlock should not be expanded in the process
        assertTrue(shb.isExpanded() == false);
        
    }
    
    /**
     * Create SOAPEnvelope from the test in the indicated file
     * @param file
     * @return
     * @throws Exception
     */
    protected SOAPEnvelope createEnvelope(InputStream in, boolean doFaultCheck) throws Exception {
        XMLStreamReader parser = StAXUtils.createXMLStreamReader(in);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser, null);
        
        SOAPEnvelope sourceEnv = (SOAPEnvelope) builder.getDocumentElement();
        
        // Do a fault check.  This is normally done in the engine (Axiom) and should
        // not cause inteference with the custom builder processing
        if (doFaultCheck) {
            sourceEnv.getBody().hasFault();
        }
        
        // Do the registration here...this simulates when it could occure in the engine
        // (After the fault check and during phase processing...probably dispatch phase)
        builder.registerCustomBuilderForPayload(new ByteArrayCustomBuilder("utf-8"));
        return sourceEnv;
    }
    
    /**
     * Make a copy of the source envelope and validate the target tree
     * @param sourceEnv
     * @param checkText (if true, check the serialization of the source and target tree)
     * @throws Exception
     */
    protected void copyAndCheck(SOAPEnvelope sourceEnv, boolean checkText) throws Exception {
        SOAPEnvelope targetEnv = CopyUtils.copy(sourceEnv);
        
        identityCheck(sourceEnv, targetEnv, "");
        
        String sourceText = sourceEnv.toString();
        String targetText = targetEnv.toString();
        
        // In some cases the serialization code or internal hashmaps cause
        // attributes or namespaces to be in a different order...accept this for now.
        if (checkText) {
            assertTrue("\nSource=" + sourceText +
                   "\nTarget=" + targetText,
                   sourceText.equals(targetText));
        }
        SOAPBody body = sourceEnv.getBody();
        OMElement payload = body.getFirstElement();
        
        assertTrue("Expected OMSourcedElement payload", payload instanceof OMSourcedElement);
        assertTrue("Expected unexpanded payload", !((OMSourcedElement)payload).isExpanded());
        
    }
    
    /**
     * Check the identity of each object in the tree
     * @param source
     * @param target
     * @param depth
     */
    protected void identityCheck(OMNode source, OMNode target, String depth) {
        // System.out.println(depth + source.getClass().getName());
        if (source instanceof OMElement) {
            
            if (source instanceof OMSourcedElement) {
                assertTrue("Source = " + source.getClass().getName() + 
                           "Target = " + target.getClass().getName(),
                           target instanceof OMSourcedElement);
                assertTrue("Source Expansion = " +((OMSourcedElement)source).isExpanded() +
                           "Target Expansion = " + ((OMSourcedElement)target).isExpanded(),
                           ((OMSourcedElement)source).isExpanded() ==
                               ((OMSourcedElement)target).isExpanded());
                if (((OMSourcedElement)source).isExpanded()) {
                    Iterator i = ((OMElement) source).getChildren();
                    Iterator j = ((OMElement) target).getChildren();
                    while(i.hasNext() && j.hasNext()) {
                        OMNode sourceChild = (OMNode) i.next();
                        OMNode targetChild = (OMNode) j.next();
                        identityCheck(sourceChild, targetChild, depth + "  ");
                    }
                    assertTrue("Source and Target have different number of children",
                               i.hasNext() == j.hasNext());
                }
            } else {
                assertTrue("Source = " + source.getClass().getName() + 
                           "Target = " + target.getClass().getName(),
                           source.getClass().getName().equals(
                           target.getClass().getName()));
                Iterator i = ((OMElement) source).getChildren();
                Iterator j = ((OMElement) target).getChildren();
                while(i.hasNext() && j.hasNext()) {
                    OMNode sourceChild = (OMNode) i.next();
                    OMNode targetChild = (OMNode) j.next();
                    identityCheck(sourceChild, targetChild, depth + "  ");
                }
                assertTrue("Source and Target have different number of children",
                           i.hasNext() == j.hasNext());
            }
        } else {
            assertTrue("Source = " + source.getClass().getName() + 
                   "Target = " + target.getClass().getName(),
                   source.getClass().getName().equals(
                   target.getClass().getName()));
        }
    }
}
