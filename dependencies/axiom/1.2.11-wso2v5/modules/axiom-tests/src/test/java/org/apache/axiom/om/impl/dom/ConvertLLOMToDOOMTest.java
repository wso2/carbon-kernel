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

package org.apache.axiom.om.impl.dom;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

/** This test will test the conversion of a LLOM soap envelope to DOOM */
public class ConvertLLOMToDOOMTest extends TestCase {
    public void testConvert() throws Exception {
        String origXML = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "   <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" +
                "      <soapenv:Header>\n" +
                "         <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" soapenv:mustUnderstand=\"1\">\n" +
                "            <ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Id=\"Signature-6426875\">\n" +
                "               <ds:SignedInfo>\n" +
                "                  <ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\" />\n" +
                "                  <ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\" />\n" +
                "                  <ds:Reference URI=\"#id-3083604\">\n" +
                "                     <ds:Transforms>\n" +
                "                        <ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\" />\n" +
                "                     </ds:Transforms>\n" +
                "                     <ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" />\n" +
                "                     <ds:DigestValue>lDeZJk0/r6u4tOOhOKbN0IEvwi0=</ds:DigestValue>\n" +
                "                  </ds:Reference>\n" +
                "               </ds:SignedInfo>\n" +
                "               <ds:SignatureValue>KhUeWMoUxUFe5jeTlqLdIEIG2Z7Q2q2mh9HT3IAYwbCev+FzXcuLSiPSsb7/+PSDM2SD0gl9tMp+dHjfPxmq7WiduH9mbnP6gkrxxu0T5rR916WsboshJGJKiPlj71bwpMsrrZohx4evHPdQ2SZHthlNb6jZyjq+LS7qFydppHk=</ds:SignatureValue>\n" +
                "               <ds:KeyInfo Id=\"KeyId-2529687\">\n" +
                "                  <wsse:SecurityTokenReference xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"STRId-31966667\">\n" +
                "                     <ds:X509Data>\n" +
                "                        <ds:X509IssuerSerial>\n" +
                "                           <ds:X509IssuerName>CN=OASIS Interop Test CA,O=OASIS</ds:X509IssuerName>\n" +
                "                           <ds:X509SerialNumber>68652640310044618358965661752471103641</ds:X509SerialNumber>\n" +
                "                        </ds:X509IssuerSerial>\n" +
                "                     </ds:X509Data>\n" +
                "                  </wsse:SecurityTokenReference>\n" +
                "               </ds:KeyInfo>\n" +
                "            </ds:Signature>\n" +
                "            <wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"Timestamp-13986615\">\n" +
                "               <wsu:Created>2006-03-31T15:34:38.699Z</wsu:Created>\n" +
                "               <wsu:Expires>2006-03-31T15:39:38.699Z</wsu:Expires>\n" +
                "            </wsu:Timestamp>\n" +
                "         </wsse:Security>\n" +
                "         <wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://localhost:9080/axis2/services/Service</wsa:To>\n" +
                "         <wsa:ReplyTo xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" +
                "            <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
                "         </wsa:ReplyTo>\n" +
                "         <wsa:MessageID xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">urn:uuid:049875A6E153FCAAF011438192785862</wsa:MessageID>\n" +
                "         <wsa:Action xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT</wsa:Action>\n" +
                "      </soapenv:Header>\n" +
                "      <soapenv:Body xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"id-3083604\">\n" +
                "         <RequestSecurityToken xmlns=\"http://schemas.xmlsoap.org/ws/2005/02/trust\" Context=\"http://get.optional.attrs.working\">\n" +
                "            <TokenType>http://schemas.xmlsoap.org/ws/2005/02/sc/sct</TokenType>\n" +
                "            <RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</RequestType>\n" +
                "         </RequestSecurityToken>\n" +
                "      </soapenv:Body>\n" +
                "   </soapenv:Envelope>";

        XMLStreamReader reader = StAXUtils.createXMLStreamReader(
                new ByteArrayInputStream(origXML.getBytes()));
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, null);

        SOAPEnvelope env = builder.getSOAPEnvelope();

        env.build();

        StAXSOAPModelBuilder doomBuilder = new StAXSOAPModelBuilder(env.getXMLStreamReader(),
                                                                    DOOMAbstractFactory.getSOAP11Factory(),
                                                                    SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        SOAPEnvelope doomEnv = doomBuilder.getSOAPEnvelope();

        doomEnv.build();

        OMElement payload = doomEnv.getBody().getFirstElement();

        StAXOMBuilder llomBuilder =
                new StAXOMBuilder(payload.getXMLStreamReaderWithoutCaching());

        OMElement llomPayload = llomBuilder.getDocumentElement();

        llomPayload.build();

        String xml = llomPayload.toString();

        assertTrue("Conversion failed", xml.indexOf("</RequestSecurityToken>") != -1);
    }

    public void testConvert1() {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.getDefaultEnvelope();
        fac.createOMElement(new QName("http://test.org", "Test"), env.getBody());
        env.build();

        StAXSOAPModelBuilder doomBuilder = new StAXSOAPModelBuilder(env.getXMLStreamReader(),
                                                                    DOOMAbstractFactory.getSOAP11Factory(),
                                                                    SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        SOAPEnvelope doomEnv = doomBuilder.getSOAPEnvelope();

        doomEnv.build();
    }

    public void testAddChild() {
        SOAPFactory fac = DOOMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.getDefaultEnvelope();
        fac.createOMElement(new QName("http://test.org", "Test"), env.getBody());
        env.build();

        SOAPFactory llomFac = DOOMAbstractFactory.getSOAP11Factory();
        OMElement elem = llomFac.createOMElement("newDomElement", null);

        OMElement firstElement = env.getBody().getFirstElement();
        firstElement.addChild(elem);

        assertTrue("New DOM child missing",
                   env.toString().indexOf("newDomElement") > 0);
    }
}
