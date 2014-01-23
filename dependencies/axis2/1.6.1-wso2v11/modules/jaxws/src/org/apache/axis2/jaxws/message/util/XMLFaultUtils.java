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

package org.apache.axis2.jaxws.message.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.XMLFaultCode;
import org.apache.axis2.jaxws.message.XMLFaultReason;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Collection of utilities used by the Message implementation to process XMLFaults.
 *
 * @see XMLFault
 */
public class XMLFaultUtils {


    /**
     * @param envelope javax.xml.soap.SOAPEnvelope
     * @return true if the SOAPEnvelope contains a SOAPFault
     */
    public static boolean isFault(javax.xml.soap.SOAPEnvelope envelope) throws SOAPException {
        javax.xml.soap.SOAPBody body = envelope.getBody();
        if (body != null) {
            return (body.getFault() != null);
        }
        return false;
    }

    /**
     * @param envelope org.apache.axiom.soap.SOAPEnvelope
     * @return true if the SOAPEnvelope contains a SOAPFault
     */
    public static boolean isFault(SOAPEnvelope envelope) {
        return envelope.hasFault();
    }
    
    /**
     * @param block representing a message payload
     * @return true if the localname & namespace represent a SOAP 1.1 or SOAP 1.2 fault.
     */
    public static boolean containsFault(Block b) {
        if (b != null) {
            QName qn = b.getQName();
            if (qn != null &&
                qn.getLocalPart().equals(org.apache.axiom.soap.SOAPConstants.SOAPFAULT_LOCAL_NAME)
                && (qn.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)
                    || qn.getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))) {
                return true;
            }
        }        
        return false;
    }


    /**
     * Create an XMLFault object from a SOAPFault and detail Blocks
     *
     * @param soapFault
     * @param detailBlocks
     * @return
     */
    public static XMLFault createXMLFault(SOAPFault soapFault, Block[] detailBlocks)
            throws WebServiceException {

        // Here is a sample comprehensive SOAP 1.2 fault which will help you 
        // understand the structure.
        // <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
        //               xmlns:m="http://www.example.org/timeouts"
        //               xmlns:xml="http://www.w3.org/XML/1998/namespace">
        //   <env:Body>
        //      <env:Fault>
        //        <env:Code>
        //          <env:Value>env:Sender</env:Value>
        //          <env:Subcode>
        //            <env:Value>m:MessageTimeout</env:Value>
        //          </env:Subcode>
        //        </env:Code>
        //        <env:Reason>
        //          <env:Text xml:lang="en">Sender Timeout</env:Text>
        //          <env:Text xml:lang="de">Sender Timeout</env:Text>
        //        </env:Reason>
        //        <env:Node>http://my.example.org/Node</env:Node>
        //        <env:Role>http://my.example.org/Role</env:Role>
        //        <env:Detail>
        //          <m:MaxTime>P5M</m:MaxTime>
        //        </env:Detail>    
        //      </env:Fault>
        //   </env:Body>
        // </env:Envelope>

        // Get the code
        // TODO what if this fails ?  Log a message and treat like a RECEIVER fault ?

        //figureout the soap version
        boolean isSoap11 = soapFault.getNamespace().getNamespaceURI().equals(
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        SOAPFaultCode soapCode = soapFault.getCode();
        QName codeQName = null;
        if (isSoap11) {
            codeQName = soapCode.getTextAsQName();
        } else {
            codeQName = soapCode.getValue().getTextAsQName();
        }
        XMLFaultCode code = XMLFaultCode.fromQName(codeQName);

        // Get the primary reason text
        // TODO what if this fails
        SOAPFaultReason soapReason = soapFault.getReason();
        String text = null;
        String lang = null;
        List soapTexts = null;
        if (isSoap11) {
            text = soapReason.getText();
        } else {
            soapTexts = soapReason.getAllSoapTexts();
            SOAPFaultText reasonText = (SOAPFaultText)soapTexts.get(0);
            text = reasonText.getText();
            lang = reasonText.getLang();
        }
        XMLFaultReason reason = new XMLFaultReason(text, lang);

        // Construct the XMLFault from the required information (code, reason, detail blocks)
        XMLFault xmlFault = new XMLFault(code, reason, detailBlocks);

        // Add the secondary fault information

        // Get the SubCodes
        SOAPFaultSubCode soapSubCode = soapCode.getSubCode();
        if (soapSubCode != null) {
            List<QName> list = new ArrayList<QName>();

            // Walk the nested sub codes and collect the qnames
            while (soapSubCode != null) {
                SOAPFaultValue soapSubCodeValue = soapSubCode.getValue();
                QName qName = soapSubCodeValue.getTextAsQName();
                list.add(qName);
                soapSubCode = soapSubCode.getSubCode();
            }

            // Put the collected sub code qnames onto the xmlFault
            QName[] qNames = new QName[list.size()];
            xmlFault.setSubCodes(list.toArray(qNames));
        }

        // Get the secondary Reasons...the first reason was already saved as the primary reason
        if (soapTexts != null && soapTexts.size() > 1) {
            XMLFaultReason[] secondaryReasons = new XMLFaultReason[soapTexts.size() - 1];
            for (int i = 1; i < soapTexts.size(); i++) {
                SOAPFaultText soapReasonText = (SOAPFaultText)soapTexts.get(i);
                secondaryReasons[i - 1] = new XMLFaultReason(soapReasonText.getText(),
                                                             soapReasonText.getLang());
            }
            xmlFault.setSecondaryReasons(secondaryReasons);
        }

        // Get the Node
        SOAPFaultNode soapNode = soapFault.getNode();
        if (soapNode != null) {
            xmlFault.setNode(soapNode.getText());
        }

        // Get the Role
        SOAPFaultRole soapRole = soapFault.getRole();
        if (soapRole != null) {
            xmlFault.setRole(soapRole.getText());
        }
        return xmlFault;
    }

    /**
     * Create XMLFault
     *
     * @param soapFault
     * @return xmlFault
     * @throws WebServiceException
     */
    public static XMLFault createXMLFault(javax.xml.soap.SOAPFault soapFault)
            throws WebServiceException {
        Block[] detailBlocks = getDetailBlocks(soapFault);
        return createXMLFault(soapFault, detailBlocks);
    }

    /**
     * Create an XMLFault object from a SOAPFault and detail Blocks
     *
     * @param soapFault
     * @param detailBlocks
     * @return
     */
    public static XMLFault createXMLFault(javax.xml.soap.SOAPFault soapFault, Block[] detailBlocks)
            throws WebServiceException {

        // The SOAPFault structure is modeled after SOAP 1.2.  
        // Here is a sample comprehensive SOAP 1.2 fault which will help you understand the
        // structure.
        // <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
        //               xmlns:m="http://www.example.org/timeouts"
        //               xmlns:xml="http://www.w3.org/XML/1998/namespace">
        //   <env:Body>
        //      <env:Fault>
        //        <env:Code>
        //          <env:Value>env:Sender</env:Value>
        //          <env:Subcode>
        //            <env:Value>m:MessageTimeout</env:Value>
        //          </env:Subcode>
        //        </env:Code>
        //        <env:Reason>
        //          <env:Text xml:lang="en">Sender Timeout</env:Text>
        //          <env:Text xml:lang="de">Sender Timeout</env:Text>
        //        </env:Reason>
        //        <env:Node>http://my.example.org/Node</env:Node>
        //        <env:Role>http://my.example.org/Role</env:Role>
        //        <env:Detail>
        //          <m:MaxTime>P5M</m:MaxTime>
        //        </env:Detail>    
        //      </env:Fault>
        //   </env:Body>
        // </env:Envelope>

        // Get the code or default code
        QName codeQName = soapFault.getFaultCodeAsQName();
        XMLFaultCode code = XMLFaultCode.fromQName(codeQName);

        // Get the primary reason text
        // TODO what if this fails
        String text = soapFault.getFaultString();
        Locale locale = soapFault.getFaultStringLocale();
        XMLFaultReason reason = new XMLFaultReason(text, localeToXmlLang(locale));

        // Construct the XMLFault from the required information (code, reason, detail blocks)
        XMLFault xmlFault = new XMLFault(code, reason, detailBlocks);


        boolean isSOAP12 =
                soapFault.getNamespaceURI().equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);

        // Add the secondary fault information

        // Get the SubCodes
        if (isSOAP12) {
            Iterator it = soapFault.getFaultSubcodes();
            List<QName> list = new ArrayList<QName>();
            while (it.hasNext()) {
                QName qName = (QName)it.next();
                list.add(qName);
            }
            if (list.size() > 0) {
                QName[] subCodes = new QName[list.size()];
                subCodes = list.toArray(subCodes);
                xmlFault.setSubCodes(subCodes);
            }
        }

        // Get the secondary Reasons...the first reason was already saved as the primary reason
        if (isSOAP12) {
            try {
                Iterator it = soapFault.getFaultReasonLocales();
                boolean first = true;
                List<XMLFaultReason> list = new ArrayList<XMLFaultReason>();
                while (it.hasNext()) {
                    locale = (Locale)it.next();
                    if (first) {
                        first = false;
                    } else {
                        text = soapFault.getFaultReasonText(locale);
                        list.add(new XMLFaultReason(text, localeToXmlLang(locale)));
                    }
                }
                if (list.size() > 0) {
                    XMLFaultReason[] secondaryReasons = new XMLFaultReason[list.size()];
                    secondaryReasons = list.toArray(secondaryReasons);
                    xmlFault.setSecondaryReasons(secondaryReasons);
                }
            } catch (SOAPException se) {
                throw ExceptionFactory.makeWebServiceException(se);
            }
        }

        // Get the Node
        if (isSOAP12) {
            String soapNode = soapFault.getFaultNode();
            if (soapNode != null) {
                xmlFault.setNode(soapNode);
            }
        }

        // Get the Role
        String soapRole = soapFault
                .getFaultActor();  // getFaultActor works for both SOAP 1.1 and SOAP 1.2 per spec
        if (soapRole != null) {
            xmlFault.setRole(soapRole);
        }

        return xmlFault;
    }

    public static XMLFault createXMLFault(Block b, Protocol p) {
        // Because of the requirement that we have a full SOAP envelope structure as
        // the input to the StAXSOAPModelBuilder, we have to have a dummy envelope
        // that wraps our fault.  This will allow the Axiom SOAPFault object to
        // be created.        
        Message m = null;
        try {
            MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
            m = mf.create(p);
            m.setBodyBlock(b);
        } catch (XMLStreamException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        SOAPEnvelope dummyEnv = (SOAPEnvelope) m.getAsOMElement();        
        
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(dummyEnv.getXMLStreamReaderWithoutCaching());
        SOAPEnvelope newEnv = (SOAPEnvelope) builder.getDocumentElement();
        
        SOAPBody body = newEnv.getBody();
        SOAPFault fault = body.getFault();
        
        Block[] details = getDetailBlocks(fault);
        
        return XMLFaultUtils.createXMLFault(fault, details);
    }
    
    private static Block[] getDetailBlocks(SOAPFault soapFault) throws WebServiceException {
        try {
            Block[] blocks = null;
            SOAPFaultDetail detail = soapFault.getDetail();
            if (detail != null) {
                // Create a block for each element
                OMBlockFactory bf =
                        (OMBlockFactory) FactoryRegistry.getFactory(OMBlockFactory.class);
                ArrayList<Block> list = new ArrayList<Block>();
                Iterator it = detail.getChildElements();
                while (it.hasNext()) {
                    OMElement om = (OMElement) it.next();
                    Block b = bf.createFrom(om, null, om.getQName());
                    list.add(b);
                }
                blocks = new Block[list.size()];
                blocks = list.toArray(blocks);
            }
            return blocks;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    private static Block[] getDetailBlocks(javax.xml.soap.SOAPFault soapFault)
            throws WebServiceException {
        try {
            Block[] blocks = null;
            Detail detail = soapFault.getDetail();
            if (detail != null) {
                // Get a SAAJ->OM converter
                SAAJConverterFactory converterFactory = (SAAJConverterFactory)FactoryRegistry
                        .getFactory(SAAJConverterFactory.class);
                SAAJConverter converter = converterFactory.getSAAJConverter();

                // Create a block for each element
                OMBlockFactory bf =
                        (OMBlockFactory)FactoryRegistry.getFactory(OMBlockFactory.class);
                ArrayList<Block> list = new ArrayList<Block>();
                Iterator it = detail.getChildElements();
                while (it.hasNext()) {
                    DetailEntry de = (DetailEntry)it.next();
                    OMElement om = converter.toOM(de);
                    Block b = bf.createFrom(om, null, om.getQName());
                    list.add(b);
                }
                blocks = new Block[list.size()];
                blocks = list.toArray(blocks);
            }
            return blocks;
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

    /**
     * Create a SOAPFault representing the XMLFault and attach it to body. If there are 1 or more
     * detail Blocks on the XMLFault, a SOAPFaultDetail is attached. If ignoreDetailBlocks=false,
     * then OMElements are added to the SOAPFaultDetail. If ignoreDetailBlocks=true, then the Detail
     * Blocks are ignored (this is necessary for XMLSpine processing)
     *
     * @param xmlFault
     * @param body               - Assumes that the body is empty
     * @param ignoreDetailBlocks true or fals
     * @return SOAPFault (which is attached to body)
     */
    public static SOAPFault createSOAPFault(XMLFault xmlFault,
                                            SOAPBody body,
                                            boolean ignoreDetailBlocks) throws WebServiceException {

        // Get the factory and create the soapFault
        SOAPFactory factory = MessageUtils.getSOAPFactory(body);
        SOAPFault soapFault = factory.createSOAPFault(body);
        OMNamespace ns = body.getNamespace();

        // The SOAPFault structure is modeled after SOAP 1.2.  
        // Here is a sample comprehensive SOAP 1.2 fault which will help you understand the
        // structure.
        // <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
        //               xmlns:m="http://www.example.org/timeouts"
        //               xmlns:xml="http://www.w3.org/XML/1998/namespace">
        //   <env:Body>
        //      <env:Fault>
        //        <env:Code>
        //          <env:Value>env:Sender</env:Value>
        //          <env:Subcode>
        //            <env:Value>m:MessageTimeout</env:Value>
        //          </env:Subcode>
        //        </env:Code>
        //        <env:Reason>
        //          <env:Text xml:lang="en">Sender Timeout</env:Text>
        //          <env:Text xml:lang="de">Sender Timeout</env:Text>
        //        </env:Reason>
        //        <env:Node>http://my.example.org/Node</env:Node>
        //        <env:Role>http://my.example.org/Role</env:Role>
        //        <env:Detail>
        //          <m:MaxTime>P5M</m:MaxTime>
        //        </env:Detail>    
        //      </env:Fault>
        //   </env:Body>
        // </env:Envelope>

        boolean isSoap11 = soapFault.getNamespace().getNamespaceURI()
                .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        // Set the primary Code Value
        SOAPFaultCode soapCode = factory.createSOAPFaultCode(soapFault);
        QName soapValueQName = xmlFault.getCode().toQName(ns.getNamespaceURI());
        if (isSoap11) {
            soapCode.setText(soapValueQName);
        } else {
            SOAPFaultValue soapValue = factory.createSOAPFaultValue(soapCode);
            soapValue.setText(soapValueQName);
        }

        // Set the primary Reason Text
        SOAPFaultReason soapReason = factory.createSOAPFaultReason(soapFault);
        if (isSoap11) {
            soapReason.setText(xmlFault.getReason().getText());
        } else {
            SOAPFaultText soapText = factory.createSOAPFaultText(soapReason);
            soapText.setText(xmlFault.getReason().getText());
            soapText.setLang(xmlFault.getReason().getLang());
        }

        // Set the Detail and contents of Detail
        Block[] blocks = xmlFault.getDetailBlocks();
        if (blocks != null && blocks.length > 0) {
            SOAPFaultDetail detail = factory.createSOAPFaultDetail(soapFault);
            if (!ignoreDetailBlocks) {
                for (int i = 0; i < blocks.length; i++) {
                    // A Block implements OMDataSource.  So create OMSourcedElements
                    // for each of the Blocks.
                    OMSourcedElement element =
                            factory.createOMElement(blocks[i], blocks[i].getQName());
                    detail.addChild(element);
                }
            }
        }

        // Now set all of the secondary fault information
        // Set the SubCodes
        QName[] subCodes = xmlFault.getSubCodes();
        if (subCodes != null && subCodes.length > 0) {
            OMElement curr = soapCode;
            for (int i = 0; i < subCodes.length; i++) {
                SOAPFaultSubCode subCode = (i == 0) ?
                        factory.createSOAPFaultSubCode((SOAPFaultCode)curr) :
                        factory.createSOAPFaultSubCode((SOAPFaultSubCode)curr);
                SOAPFaultValue soapSubCodeValue = factory.createSOAPFaultValue(subCode);
                soapSubCodeValue.setText(subCodes[i]);
                curr = subCode;
            }
        }

        // Set the secondary reasons and languages
        XMLFaultReason reasons[] = xmlFault.getSecondaryReasons();
        if (reasons != null && reasons.length > 0) {
            for (int i = 0; i < reasons.length; i++) {
                SOAPFaultText soapReasonText = factory.createSOAPFaultText(soapReason);
                soapReasonText.setText(reasons[i].getText());
                soapReasonText.setLang(reasons[i].getLang());
            }
        }

        // Set the Role
        if (xmlFault.getRole() != null) {
            SOAPFaultRole soapRole = factory.createSOAPFaultRole();
            soapRole.setText(xmlFault.getRole());
            soapFault.setRole(soapRole);
        }

        // Set the Node
        if (xmlFault.getNode() != null) {
            SOAPFaultNode soapNode = factory.createSOAPFaultNode();
            soapNode.setText(xmlFault.getNode());
            soapFault.setNode(soapNode);
        }

        return soapFault;

    }


    /**
     * Create a SOAPFault representing the XMLFault. If there are 1 or more detail Blocks on the
     * XMLFault, a SOAPFaultDetail is attached.
     *
     * @param xmlFault
     * @param body
     * @return SOAPFault (which is attached to body)
     */
    public static javax.xml.soap.SOAPFault createSAAJFault(XMLFault xmlFault,
                                                           javax.xml.soap.SOAPBody body)
            throws SOAPException, WebServiceException {

        // Get the factory and create the soapFault
        String protocolNS = body.getNamespaceURI();

        javax.xml.soap.SOAPFault soapFault = body.addFault();

        // The SOAPFault structure is modeled after SOAP 1.2.  
        // Here is a sample comprehensive SOAP 1.2 fault which will help you understand the
        // structure.
        // <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
        //               xmlns:m="http://www.example.org/timeouts"
        //               xmlns:xml="http://www.w3.org/XML/1998/namespace">
        //   <env:Body>
        //      <env:Fault>
        //        <env:Code>
        //          <env:Value>env:Sender</env:Value>
        //          <env:Subcode>
        //            <env:Value>m:MessageTimeout</env:Value>
        //          </env:Subcode>
        //        </env:Code>
        //        <env:Reason>
        //          <env:Text xml:lang="en">Sender Timeout</env:Text>
        //          <env:Text xml:lang="de">Sender Timeout</env:Text>
        //        </env:Reason>
        //        <env:Node>http://my.example.org/Node</env:Node>
        //        <env:Role>http://my.example.org/Role</env:Role>
        //        <env:Detail>
        //          <m:MaxTime>P5M</m:MaxTime>
        //        </env:Detail>    
        //      </env:Fault>
        //   </env:Body>
        // </env:Envelope>

        // Set the primary Code Value
        QName soapValueQName = xmlFault.getCode().toQName(protocolNS);
        String prefix = soapFault.getPrefix();
        String soapValue = null;
        if (prefix == null || prefix.length() == 0) {
            soapValue = soapValueQName.getLocalPart();
        } else {
            soapValue = prefix + ":" + soapValueQName.getLocalPart();
        }
        soapFault.setFaultCode(soapValue);

        // Set the primary Reason Text
        String reasonText = xmlFault.getReason().getText();
        String reasonLang = xmlFault.getReason().getLang();
        Locale locale = (reasonLang != null && reasonLang.length() > 0) ?
                new Locale(reasonLang) :
                Locale.getDefault();
        soapFault.setFaultString(reasonText, locale);

        // Set the Detail and contents of Detail
        Block[] blocks = xmlFault.getDetailBlocks();
        if (blocks != null && blocks.length > 0) {
            Detail detail = soapFault.addDetail();
            // Get a OM->SAAJ converter
            SAAJConverterFactory converterFactory =
                    (SAAJConverterFactory)FactoryRegistry.getFactory(SAAJConverterFactory.class);
            SAAJConverter converter = converterFactory.getSAAJConverter();
            for (int i = 0; i < blocks.length; i++) {
                try {
                    converter.toSAAJ(blocks[i].getOMElement(), detail);
                } catch (XMLStreamException xse) {
                    throw ExceptionFactory.makeWebServiceException(xse);
                }
            }

        }

        // Now set all of the secondary fault information
        // Set the SubCodes
        QName[] subCodes = xmlFault.getSubCodes();
        if (subCodes != null && subCodes.length > 0 &&
                protocolNS.equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
            for (int i = 0; i < subCodes.length; i++) {
                soapFault.appendFaultSubcode(subCodes[i]);
            }
        }

        // Set the secondary reasons and languages
        XMLFaultReason reasons[] = xmlFault.getSecondaryReasons();
        if (reasons != null && reasons.length > 0 &&
                protocolNS.equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
            for (int i = 0; i < reasons.length; i++) {
                if (reasons[i].getLang() == null || reasons[i].getLang().length() == 0) {
                    locale = Locale.getDefault();
                } else {
                    locale = new Locale(reasons[i].getLang());
                }
                soapFault.addFaultReasonText(reasons[i].getText(), locale);
            }
        }

        // Set the Role
        if (xmlFault.getRole() != null) {
            soapFault.setFaultActor(
                    xmlFault.getRole());  // Use Fault actor because it is applicable for SOAP 1.1 and SOAP 1.2
        }

        // Set the Node...only applicable for SOAP 1.2
        if (xmlFault.getNode() != null && protocolNS.equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE))
        {
            soapFault.setFaultNode(xmlFault.getNode());
        }

        return soapFault;

    }
    
    /**
     * Converte a Locale object to an xmlLang String
     * @param locale
     * @return String of the form <locale.getLanguage()>-<locale.getCountry()>
     */
    private static String localeToXmlLang(Locale locale) {
        if (locale == null) {
            return Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        }
        String lang = locale.getLanguage();
        String countryCode = locale.getCountry();
        if (countryCode == null || countryCode.length() == 0) {
            return lang;
        } else {
            return new String(lang + "-" + countryCode);
        }
        
    }
}
