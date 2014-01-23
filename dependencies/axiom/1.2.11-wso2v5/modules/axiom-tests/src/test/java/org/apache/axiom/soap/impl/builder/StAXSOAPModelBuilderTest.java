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

package org.apache.axiom.soap.impl.builder;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.custommonkey.xmlunit.XMLTestCase;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;

public class StAXSOAPModelBuilderTest extends XMLTestCase {

    public void setUp() {

    }

    public void testStAXSOAPModelBuilder() throws Exception {
        String soap12Message =
                "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                        "   <env:Header>\n" +
                        "       <test:echoOk xmlns:test=\"http://example.org/ts-tests\"\n" +
                        "                    env:role=\"http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver\"\n" +
                        "                    env:mustUnderstand=\"true\">\n" +
                        "                       foo\n" +
                        "       </test:echoOk>\n" +
                        "   </env:Header>\n" +
                        "   <env:Body>\n" +
                        "       <env:Fault>\n" +
                        "           <env:Code>\n" +
                        "               <env:Value>env:Sender</env:Value>\n" +
                        "               <env:Subcode>\n" +
                        "                   <env:Value>m:MessageTimeout</env:Value>\n" +
                        "                   <env:Subcode>\n" +
                        "                       <env:Value>m:MessageTimeout</env:Value>\n" +
                        "                   </env:Subcode>\n" +
                        "               </env:Subcode>\n" +
                        "           </env:Code>\n" +
                        "           <env:Reason>\n" +
                        "               <env:Text>Sender Timeout</env:Text>\n" +
                        "           </env:Reason>\n" +
                        "           <env:Node>\n" +
                        "               http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver\n" +
                        "           </env:Node>\n" +
                        "           <env:Role>\n" +
                        "               ultimateReceiver\n" +
                        "           </env:Role>\n" +
                        "           <env:Detail xmlns:m=\"http:www.sample.org\">\n" +
                        "               Details of error\n" +
                        "               <m:MaxTime m:detail=\"This is only a test\">\n" +
                        "                   P5M\n" +
                        "               </m:MaxTime>\n" +
                        "               <m:AveTime>\n" +
                        "                   <m:Time>\n" +
                        "                       P3M\n" +
                        "                   </m:Time>\n" +
                        "               </m:AveTime>\n" +
                        "           </env:Detail>\n" +
                        "       </env:Fault>\n" +
                        "   </env:Body>\n" +
                        "</env:Envelope>";

        String soap11Message =
                "<?xml version='1.0' ?>" +
                        "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                        "   <env:Header>\n" +
                        "       <test:echoOk xmlns:test=\"http://example.org/ts-tests\"\n" +
                        "                    env:actor=\"http://schemas.xmlsoap.org/soap/actor/next\"\n" +
                        "                    env:mustUnderstand=\"1\"" +
                        "       >\n" +
                        "                       foo\n" +
                        "       </test:echoOk>\n" +
                        "   </env:Header>\n" +
                        "   <env:Body>\n" +
                        "       <env:Fault>\n" +
                        "           <faultcode>\n" +
                        "               env:Sender\n" +
                        "           </faultcode>\n" +
                        "           <faultstring>\n" +
                        "               Sender Timeout\n" +
                        "           </faultstring>\n" +
                        "           <faultactor>\n" +
                        "               http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver\n" +
                        "           </faultactor>\n" +
                        "           <detail xmlns:m=\"http:www.sample.org\">\n" +
                        "               Details of error\n" +
                        "               <m:MaxTime m:detail=\"This is only a test\">\n" +
                        "                   P5M\n" +
                        "               </m:MaxTime>\n" +
                        "               <m:AveTime>\n" +
                        "                   <m:Time>\n" +
                        "                       P3M\n" +
                        "                   </m:Time>\n" +
                        "               </m:AveTime>\n" +
                        "           </detail>\n" +
                        "           <n:Test xmlns:n=\"http:www.Test.org\">\n" +
                        "               <n:TestElement>\n" +
                        "                   This is only a test\n" +
                        "               </n:TestElement>\n" +
                        "           </n:Test>\n" +
                        "       </env:Fault>\n" +
                        "   </env:Body>\n" +
                        "</env:Envelope>";

        XMLStreamReader soap12Parser = StAXUtils.createXMLStreamReader(
                new StringReader(soap12Message));
        OMXMLParserWrapper soap12Builder = new StAXSOAPModelBuilder(soap12Parser, null);
        SOAPEnvelope soap12Envelope = (SOAPEnvelope) soap12Builder.getDocumentElement();

        assertTrue("SOAP 1.2 :- envelope local name mismatch",
                   soap12Envelope.getLocalName().equals(
                           SOAPConstants.SOAPENVELOPE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- envelope namespace uri mismatch",
                   soap12Envelope.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        SOAPHeader header = soap12Envelope.getHeader();
        assertTrue("SOAP 1.2 :- Header local name mismatch",
                   header.getLocalName().equals(
                           SOAPConstants.HEADER_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Header namespace uri mismatch",
                   header.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) header.getFirstElement();
        assertTrue("SOAP 1.2 :- Header block name mismatch",
                   headerBlock.getLocalName().equals("echoOk"));
        assertTrue("SOAP 1.2 :- Header block name space uri mismatch",
                   headerBlock.getNamespace().getNamespaceURI().equals(
                           "http://example.org/ts-tests"));
        assertEquals("SOAP 1.2 :- Header block text mismatch", headerBlock.getText().trim(),
                     "foo");

        // Attribute iteration is not in any guaranteed order.
        // Use QNames to get the OMAttributes.
        QName roleQName = new QName(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                    SOAP12Constants.SOAP_ROLE);
        QName mustUnderstandQName = new QName(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                              SOAP12Constants.ATTR_MUSTUNDERSTAND);

        OMAttribute roleAttribute = headerBlock.getAttribute(roleQName);
        OMAttribute mustUnderstandAttribute = headerBlock.getAttribute(mustUnderstandQName);


        assertTrue("SOAP 1.2 :- Role attribute name not found",
                   roleAttribute != null);


        assertTrue("SOAP 1.2 :- Role value mismatch",
                   roleAttribute.getAttributeValue().trim().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI + "/" +
                                   SOAP12Constants.SOAP_ROLE +
                                   "/" +
                                   "ultimateReceiver"));

        assertTrue("SOAP 1.2 :- Mustunderstand attribute not found",
                   mustUnderstandAttribute != null);

        assertTrue("SOAP 1.2 :- Mustunderstand value mismatch",
                   mustUnderstandAttribute.getAttributeValue().equals(
                           SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE));


        SOAPBody body = soap12Envelope.getBody();
        assertTrue("SOAP 1.2 :- Body local name mismatch",
                   body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Body namespace uri mismatch",
                   body.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        SOAPFault fault = body.getFault();
        assertTrue("SOAP 1.2 :- Fault local name mismatch",
                   fault.getLocalName().equals(
                           SOAPConstants.SOAPFAULT_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault namespace uri mismatch",
                   fault.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        Iterator iteratorInFault = fault.getChildren();

        iteratorInFault.next();
        SOAPFaultCode code = (SOAPFaultCode) iteratorInFault.next();
        assertTrue("SOAP 1.2 :- Fault code local name mismatch",
                   code.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                   code.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        Iterator iteratorInCode = code.getChildren();

        iteratorInCode.next();
        SOAPFaultValue value1 = (SOAPFaultValue) iteratorInCode.next();
        assertTrue("SOAP 1.2 :- Fault code value local name mismatch",
                   value1.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                   value1.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
        assertTrue("SOAP 1.2 :- Value1 text mismatch",
                   value1.getText().equals("env:Sender"));

        QName valueQName = value1.getTextAsQName();
        assertTrue("SOAP 1.2 :- Fault code value's qname local name mismatch",
                   valueQName.getLocalPart().equals("Sender"));

        assertTrue("SOAP 1.2 :- Fault code value's qname namespace uri mismatch",
                   valueQName.getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));


        iteratorInCode.next();
        SOAPFaultSubCode subCode1 = (SOAPFaultSubCode) iteratorInCode.next();
        assertTrue("SOAP 1.2 :- Fault sub code local name mismatch",
                   subCode1.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault subcode namespace uri mismatch",
                   subCode1.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        Iterator iteratorInSubCode1 = subCode1.getChildren();

        iteratorInSubCode1.next();
        SOAPFaultValue value2 = (SOAPFaultValue) iteratorInSubCode1.next();
        assertTrue("SOAP 1.2 :- Fault code value local name mismatch",
                   value2.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                   value2.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
        assertTrue("SOAP 1.2 :- Value2 text mismatch",
                   value2.getText().equals("m:MessageTimeout"));

        iteratorInSubCode1.next();
        SOAPFaultSubCode subCode2 = (SOAPFaultSubCode) iteratorInSubCode1.next();
        assertTrue("SOAP 1.2 :- Fault sub code local name mismatch",
                   subCode2.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault subcode namespace uri mismatch",
                   subCode2.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        Iterator iteratorInSubCode2 = subCode2.getChildren();

        iteratorInSubCode2.next();
        SOAPFaultValue value3 = (SOAPFaultValue) iteratorInSubCode2.next();
        assertTrue("SOAP 1.2 :- Fault code value local name mismatch",
                   value3.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault code namespace uri mismatch",
                   value3.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
        assertTrue("SOAP 1.2 :- Value2 text mismatch",
                   value3.getText().equals("m:MessageTimeout"));

        iteratorInFault.next();
        SOAPFaultReason reason = (SOAPFaultReason) iteratorInFault.next();
        assertTrue("SOAP 1.2 :- Fault reason local name mismatch",
                   reason.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault reason namespace uri mismatch",
                   reason.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        Iterator iteratorInReason = reason.getChildren();

        iteratorInReason.next();
        SOAPFaultText text = (SOAPFaultText) iteratorInReason.next();
        assertTrue("SOAP 1.2 :- Fault text local name mismatch",
                   text.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Text namespace uri mismatch",
                   text.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
        assertTrue("SOAP 1.2 :- Text value mismatch",
                   text.getText().equals("Sender Timeout"));

        iteratorInFault.next();
        SOAPFaultNode node = (SOAPFaultNode) iteratorInFault.next();
        assertTrue("SOAP 1.2 :- Fault node local name mismatch",
                   node.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault node namespace uri mismatch",
                   node.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
        assertTrue("SOAP 1.2 :- Node value mismatch",
                   node.getText().trim().equals(
                           "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));

        iteratorInFault.next();
        SOAPFaultRole role = (SOAPFaultRole) iteratorInFault.next();
        assertTrue("SOAP 1.2 :- Fault role local name mismatch",
                   role.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault role namespace uri mismatch",
                   role.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
        assertTrue("SOAP 1.2 :- Role value mismatch",
                   role.getText().trim().equals("ultimateReceiver"));

        iteratorInFault.next();
        SOAPFaultDetail detail = (SOAPFaultDetail) iteratorInFault.next();
        assertTrue("SOAP 1.2 :- Fault detail local name mismatch",
                   detail.getLocalName().equals(
                           SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Fault detail namespace uri mismatch",
                   detail.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        assertTrue("SOAP 1.2 :- Text in detail mismatch",
                   detail.getText().trim().equals("Details of error"));

        Iterator iteratorInDetail = detail.getChildren();

        iteratorInDetail.next();
        OMElement element1 = (OMElement) iteratorInDetail.next();
        assertTrue("SOAP 1.2 :- MaxTime element mismatch",
                   element1.getLocalName().equals("MaxTime"));
        assertTrue("SOAP 1.2 :- MaxTime element namespace mismatch",
                   element1.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));
        assertTrue("SOAP 1.2 :- Text value in MaxTime element mismatch",
                   element1.getText().trim().equals("P5M"));

        Iterator attributeIterator = element1.getAllAttributes();
        OMAttribute attributeInMaxTime = (OMAttribute) attributeIterator.next();
        assertTrue("SOAP 1.2 :- Attribute local name mismatch",
                   attributeInMaxTime.getLocalName().equals("detail"));
        assertTrue("SOAP 1.2 :- Attribute namespace mismatch",
                   attributeInMaxTime.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));
        assertTrue("SOAP 1.2 :- Attribute value mismatch",
                   attributeInMaxTime.getAttributeValue().trim().equals("This is only a test"));

        iteratorInDetail.next();
        OMElement element2 = (OMElement) iteratorInDetail.next();
        assertTrue("SOAP 1.2 :- AveTime element mismatch",
                   element2.getLocalName().equals("AveTime"));
        assertTrue("SOAP 1.2 :- AveTime element namespace mismatch",
                   element2.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));

        Iterator iteratorInAveTimeElement = element2.getChildren();

        iteratorInAveTimeElement.next();
        OMElement element21 = (OMElement) iteratorInAveTimeElement.next();
        assertTrue("SOAP 1.2 :- Time element mismatch",
                   element21.getLocalName().equals("Time"));
        assertTrue("SOAP 1.2 :- Time element namespace mismatch",
                   element21.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));
        assertTrue("SOAP 1.2 :- Text value in Time element mismatch",
                   element21.getText().trim().equals("P3M"));

        XMLStreamReader soap11Parser = StAXUtils.createXMLStreamReader(
                new StringReader(soap11Message));
        OMXMLParserWrapper soap11Builder = new StAXSOAPModelBuilder(soap11Parser, null);
        SOAPEnvelope soap11Envelope = (SOAPEnvelope) soap11Builder.getDocumentElement();
//            soap11Envelope.build();
//            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
//            soap11Envelope.internalSerializeAndConsume(writer);
//		    writer.flush();

        assertTrue("SOAP 1.1 :- envelope local name mismatch",
                   soap11Envelope.getLocalName().equals(
                           SOAPConstants.SOAPENVELOPE_LOCAL_NAME));
        assertTrue("SOAP 1.1 :- envelope namespace uri mismatch",
                   soap11Envelope.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        header = soap11Envelope.getHeader();
        assertTrue("SOAP 1.1 :- Header local name mismatch",
                   header.getLocalName().equals(
                           SOAPConstants.HEADER_LOCAL_NAME));
        assertTrue("SOAP 1.1 :- Header namespace uri mismatch",
                   header.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        headerBlock = (SOAPHeaderBlock) header.getFirstElement();
        assertTrue("SOAP 1.1 :- Header block name mismatch",
                   headerBlock.getLocalName().equals("echoOk"));
        assertTrue("SOAP 1.1 :- Header block name space uri mismatch",
                   headerBlock.getNamespace().getNamespaceURI().equals(
                           "http://example.org/ts-tests"));
        assertTrue("SOAP 1.1 :- Headaer block text mismatch",
                   headerBlock.getText().trim().equals("foo"));

        // Attribute iteration is not in any guaranteed order.
        // Use QNames to get the OMAttributes.
        QName actorQName = new QName(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                     SOAP11Constants.ATTR_ACTOR);
        mustUnderstandQName = new QName(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI,
                                        SOAP11Constants.ATTR_MUSTUNDERSTAND);

        OMAttribute actorAttribute = headerBlock.getAttribute(actorQName);
        mustUnderstandAttribute = headerBlock.getAttribute(mustUnderstandQName);

        assertTrue("SOAP 1.1 :- Mustunderstand attribute not found",
                   mustUnderstandAttribute != null);
        assertTrue("SOAP 1.1 :- Mustunderstand value mismatch",
                   mustUnderstandAttribute.getAttributeValue().equals(
                           SOAPConstants.ATTR_MUSTUNDERSTAND_1));
        assertTrue(
                "SOAP 1.1 :- Mustunderstand attribute namespace uri mismatch",
                mustUnderstandAttribute.getNamespace().getNamespaceURI().equals(
                        SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        assertTrue("SOAP 1.1 :- Actor attribute name not found",
                   actorAttribute != null);
        assertTrue("SOAP 1.1 :- Actor value mismatch",
                   actorAttribute.getAttributeValue().trim().equals(
                           "http://schemas.xmlsoap.org/soap/" +
                                   SOAP11Constants.ATTR_ACTOR +
                                   "/" +
                                   "next"));
        assertTrue("SOAP 1.1 :- Actor attribute namespace uri mismatch",
                   actorAttribute.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        body = soap11Envelope.getBody();
        assertTrue("SOAP 1.1 :- Body local name mismatch",
                   body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
        assertTrue("SOAP 1.1 :- Body namespace uri mismatch",
                   body.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        fault = body.getFault();
        assertTrue("SOAP 1.1 :- Fault namespace uri mismatch",
                   fault.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

        iteratorInFault = fault.getChildren();

        iteratorInFault.next();
        code = (SOAPFaultCode) iteratorInFault.next();
        assertEquals("SOAP Fault code local name mismatch",
                     code.getLocalName(),
                     (SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME));

        assertEquals("SOAP 1.1 :- Fault code value mismatch", code.getText().trim(),
                     "env:Sender");

        iteratorInFault.next();
        reason = (SOAPFaultReason) iteratorInFault.next();
        assertTrue("SOAP 1.1 :- Fault string local name mismatch",
                   reason.getLocalName().equals(
                           SOAP11Constants.SOAP_FAULT_STRING_LOCAL_NAME));
        assertTrue("SOAP 1.1 :- Fault string value mismatch",
                   reason.getText().trim().equals("Sender Timeout"));

        iteratorInFault.next();
        role = (SOAPFaultRole) iteratorInFault.next();
        assertTrue("SOAP 1.1 :- Fault actor local name mismatch",
                   role.getLocalName().equals(
                           SOAP11Constants.SOAP_FAULT_ACTOR_LOCAL_NAME));
        assertTrue("SOAP 1.1 :- Actor value mismatch",
                   role.getText().trim().equals(
                           "http://schemas.xmlsoap.org/soap/envelope/actor/ultimateReceiver"));

        iteratorInFault.next();
        detail = (SOAPFaultDetail) iteratorInFault.next();
        assertTrue("SOAP 1.1 :- Fault detail local name mismatch",
                   detail.getLocalName().equals(
                           SOAP11Constants.SOAP_FAULT_DETAIL_LOCAL_NAME));
        assertTrue("SOAP 1.2 :- Text in detail mismatch",
                   detail.getText().trim().equals("Details of error"));

        iteratorInDetail = detail.getChildren();

        iteratorInDetail.next();
        element1 = (OMElement) iteratorInDetail.next();
        assertTrue("SOAP 1.1 :- MaxTime element mismatch",
                   element1.getLocalName().equals("MaxTime"));
        assertTrue("SOAP 1.1 :- MaxTime element namespace mismatch",
                   element1.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));
        assertTrue("SOAP 1.1 :- Text value in MaxTime element mismatch",
                   element1.getText().trim().equals("P5M"));

        attributeIterator = element1.getAllAttributes();
        attributeInMaxTime = (OMAttribute) attributeIterator.next();
        assertTrue("SOAP 1.1 :- Attribute local name mismatch",
                   attributeInMaxTime.getLocalName().equals("detail"));
        assertTrue("SOAP 1.1 :- Attribute namespace mismatch",
                   attributeInMaxTime.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));
        assertTrue("SOAP 1.1 :- Attribute value mismatch",
                   attributeInMaxTime.getAttributeValue().equals("This is only a test"));

        iteratorInDetail.next();
        element2 = (OMElement) iteratorInDetail.next();
        assertTrue("SOAP 1.1 :- AveTime element mismatch",
                   element2.getLocalName().equals("AveTime"));
        assertTrue("SOAP 1.1 :- AveTime element namespace mismatch",
                   element2.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));

        iteratorInAveTimeElement = element2.getChildren();

        iteratorInAveTimeElement.next();
        element21 = (OMElement) iteratorInAveTimeElement.next();
        assertTrue("SOAP 1.1 :- Time element mismatch",
                   element21.getLocalName().equals("Time"));
        assertTrue("SOAP 1.1 :- Time element namespace mismatch",
                   element21.getNamespace().getNamespaceURI().equals(
                           "http:www.sample.org"));
        assertTrue("SOAP 1.1 :- Text value in Time element mismatch",
                   element21.getText().trim().equals("P3M"));

        iteratorInFault.next();
        OMElement testElement = (OMElement) iteratorInFault.next();
        assertTrue("SOAP 1.1 :- Test element mismatch",
                   testElement.getLocalName().equals("Test"));
        assertTrue("SOAP 1.1 :- Test element namespace mismatch",
                   testElement.getNamespace().getNamespaceURI().equals(
                           "http:www.Test.org"));

        OMElement childOfTestElement = testElement.getFirstElement();
        assertTrue("SOAP 1.1 :- Test element child local name mismatch",
                   childOfTestElement.getLocalName().equals("TestElement"));
        assertTrue("SOAP 1.1 :- Test element child namespace mismatch",
                   childOfTestElement.getNamespace().getNamespaceURI().equals(
                           "http:www.Test.org"));
        assertTrue("SOAP 1.1 :- Test element child value mismatch",
                   childOfTestElement.getText().trim().equals("This is only a test"));
        
        soap12Parser.close();
        soap11Parser.close();
    }

    /**
     * Test a couple of malformed envelopes, make sure parsing fails correctly.
     *
     * @throws Exception
     */
    public void testBadEnvelope() throws Exception {
        String badEnvStart =
                "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">\n";
        String badEnvHeader =
                "       <test:echoOk xmlns:test=\"http://example.org/ts-tests\"\n" +
                        "                    env:role=\"http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver\"\n" +
                        "                    env:mustUnderstand=\"true\">\n" +
                        "                       foo\n" +
                        "       </test:echoOk>\n";
        String badEnvEnd =
                "   <env:Body><content/></env:Body>\n" +
                        "</env:Envelope>";
        String [] badHeaders = {
                "env:HeaDER",   // Bad case
                "Header"        // No namespace
        };

        for (int i = 0; i < badHeaders.length; i++) {
            String soap12Message = badEnvStart + "<" + badHeaders[i] + ">\n" +
                    badEnvHeader + "</" + badHeaders[i] + ">\n" +
                    badEnvEnd;
            XMLStreamReader soap12Parser = StAXUtils.createXMLStreamReader(
                    new StringReader(soap12Message));
            try {
                StAXSOAPModelBuilder soap12Builder = new StAXSOAPModelBuilder(soap12Parser, null);
                SOAPEnvelope soap12Envelope = (SOAPEnvelope) soap12Builder.getDocumentElement();
                try {
                    soap12Envelope.getHeader();
                } catch (OMException e) {
                    // Good, we failed.  Keep going.
                    continue;
                }
            } finally {
                soap12Parser.close();
            }
            fail("Successfully parsed bad envelope ('" + badHeaders[i] + "')");
        }
    }

    public void testFault() throws Exception {
        String soap11Fault = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<SOAP-ENV:Body>" +
                    "<SOAP-ENV:Fault>" +
                        "<faultcode>SOAP-ENV:Server</faultcode>" +
                        "<faultstring xml:lang=\"en\">handleMessage throws SOAPFaultException for ThrowsSOAPFaultToClientHandlersTest</faultstring>" +
                        "<detail>" +
                            "<somefaultentry/>" +
                        "</detail>" +
                        "<faultactor>faultActor</faultactor>" +
                        "</SOAP-ENV:Fault>" +
                    "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>";
        XMLStreamReader soap11Parser = StAXUtils.createXMLStreamReader(
                new StringReader(soap11Fault));
        StAXSOAPModelBuilder soap11Builder = new StAXSOAPModelBuilder(soap11Parser, null);
        OMElement element = soap11Builder.getDocumentElement();
        element.build();
        this.assertXMLEqual(soap11Fault, element.toString());
        soap11Parser.close();
    }
    
    /**
     * @throws Exception
     */
    public void testOptimizedFault() throws Exception {
        String soap11Fault = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<SOAP-ENV:Body>" +
                    "<SOAP-ENV:Fault>" +
                        "<faultcode>SOAP-ENV:Server</faultcode>" +
                        "<faultstring xml:lang=\"en\">handleMessage throws SOAPFaultException for ThrowsSOAPFaultToClientHandlersTest</faultstring>" +
                        "<detail>" +
                            "<somefaultentry/>" +
                        "</detail>" +
                        "<faultactor>faultActor</faultactor>" +
                        "</SOAP-ENV:Fault>" +
                    "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>";
        
        // Use the test parser that is aware of the first qname in the body.
        // This simulates the use of the parser that has this information built into its
        // implementation.
        
        XMLStreamReader soap11Parser = StAXUtils.createXMLStreamReader(
                new StringReader(soap11Fault));
        QName qname = new QName(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI, SOAP11Constants.BODY_FAULT_LOCAL_NAME, "SOAP-ENV");
        XMLStreamReaderWithQName parser = new XMLStreamReaderWithQName(soap11Parser, qname);
        StAXSOAPModelBuilder soap11Builder = new StAXSOAPModelBuilder(parser, null);
        SOAPEnvelope env = soap11Builder.getSOAPEnvelope();
        boolean isFault = env.hasFault();
        this.assertTrue(isFault);
        this.assertTrue(!parser.isReadBody());
        
        // Get the name of the first element in the body
        String localName = env.getSOAPBodyFirstElementLocalName();
        this.assertTrue(localName.equals("Fault"));
        this.assertTrue(!parser.isReadBody());
        parser.close();
    }
    
    public void testFaultWithCDATA() throws Exception {
        String soap11Fault = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<SOAP-ENV:Body>" +
                    "<SOAP-ENV:Fault>" +
                        "<faultcode>SOAP-ENV:Server</faultcode>" +
                        "<faultstring xml:lang=\"en\"><![CDATA[handleMessage throws SOAPFaultException for ThrowsSOAPFaultToClientHandlersTest]]></faultstring>" +
                        "<detail>" +
                            "<somefaultentry/>" +
                        "</detail>" +
                        "<faultactor>faultActor</faultactor>" +
                        "</SOAP-ENV:Fault>" +
                    "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>";
        XMLStreamReader soap11Parser = StAXUtils.createXMLStreamReader(
                new StringReader(soap11Fault));
        StAXSOAPModelBuilder soap11Builder = new StAXSOAPModelBuilder(soap11Parser, null);
        OMElement element = soap11Builder.getDocumentElement();
        element.build();
        assertTrue(element instanceof SOAPEnvelope);
        SOAPEnvelope se =  (SOAPEnvelope) element;
        SOAPFault fault = se.getBody().getFault();
        SOAPFaultReason reason = fault.getReason();
        assertTrue(reason.getText().equals("handleMessage throws SOAPFaultException for ThrowsSOAPFaultToClientHandlersTest"));
        soap11Parser.close();
    }
}
