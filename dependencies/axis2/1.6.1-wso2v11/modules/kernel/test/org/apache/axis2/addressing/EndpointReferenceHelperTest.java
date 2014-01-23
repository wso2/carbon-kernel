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

package org.apache.axis2.addressing;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.metadata.WSDLLocation;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.Map;

public class EndpointReferenceHelperTest extends TestCase {

    public void testToAndFromOMForFinalSpecEPR() throws Exception {
        String address = "http://ws.apache.org/axis2";
        EndpointReference epr = new EndpointReference(address);

        OMFactory omf = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = omf.createOMNamespace("http://uri1", "prefix1");
        OMAttribute attr1 = omf.createOMAttribute("attr1", ns1, "attr1value");
        OMNamespace ns2 = omf.createOMNamespace("http://uri2", "prefix2");
        OMAttribute attr2 = omf.createOMAttribute("attr2", ns2, "attr2value");
        epr.addAttribute(attr1);
        epr.addAttribute(attr2);
        OMElement md1 = omf.createOMElement("md1", "http://mduri1", "md1prefix");
        OMElement md2 = omf.createOMElement("md2", "http://mduri2", "md2prefix");
        epr.addMetaData(md1);
        epr.addMetaData(md2);
        OMElement ext1 = omf.createOMElement("ext1", "http://exturi1", "ext1prefix");
        OMElement ext2 = omf.createOMElement("ext2", "http://exturi2", "ext2prefix");
        epr.addExtensibleElement(ext1);
        epr.addExtensibleElement(ext2);
        QName rp1Qname = new QName("http://rp1uri", "refParm1", "rp1prefix");
        QName rp2Qname = new QName("http://rp2uri", "refParm2", "rp2prefix");
        epr.addReferenceParameter(rp1Qname, "rp1");
        epr.addReferenceParameter(rp2Qname, "rp2");

        ArrayList addressAttributes = new ArrayList();
        addressAttributes.add(attr1);
        addressAttributes.add(attr2);
        epr.setAddressAttributes(addressAttributes);

        ArrayList metadataAttributes = new ArrayList();
        metadataAttributes.add(attr1);
        metadataAttributes.add(attr2);
        epr.setMetadataAttributes(metadataAttributes);

        OMElement om = EndpointReferenceHelper.toOM(omf, epr, new QName("http://nsurl", "localName",
                                                                        "prefix"),
                                                              AddressingConstants.Final.WSA_NAMESPACE);

        //Test deserialize using fromOM(OMElement)
        EndpointReference deser = EndpointReferenceHelper.fromOM(om);

        assertEquals(epr.getAddress(), deser.getAddress());
        ArrayList addrAttrs = deser.getAddressAttributes();
        assertEquals(attr1, addrAttrs.get(0));
        assertEquals(attr2, addrAttrs.get(1));

        ArrayList attrs = deser.getAttributes();
        assertEquals(attr1, attrs.get(0));
        assertEquals(attr2, attrs.get(1));

        ArrayList metadata = deser.getMetaData();
        assertEquals(md1, metadata.get(0));
        assertEquals(md2, metadata.get(1));
        ArrayList mdAttrs = deser.getMetadataAttributes();
        assertEquals(attr1, mdAttrs.get(0));
        assertEquals(attr2, mdAttrs.get(1));

        ArrayList extelts = deser.getExtensibleElements();
        assertEquals(ext1, extelts.get(0));
        assertEquals(ext2, extelts.get(1));

        Map m = deser.getAllReferenceParameters();
        assertEquals("rp1", ((OMElement) m.get(rp1Qname)).getText());
        assertEquals("rp2", ((OMElement) m.get(rp2Qname)).getText());

        //Test deserialize using fromOM(EndpointReference, OMElement, String)
        deser = new EndpointReference("");
        EndpointReferenceHelper.fromOM(deser, om, AddressingConstants.Final.WSA_NAMESPACE);

        assertEquals(epr.getAddress(), deser.getAddress());
        addrAttrs = deser.getAddressAttributes();
        assertEquals(attr1, addrAttrs.get(0));
        assertEquals(attr2, addrAttrs.get(1));

        attrs = deser.getAttributes();
        assertEquals(attr1, attrs.get(0));
        assertEquals(attr2, attrs.get(1));

        metadata = deser.getMetaData();
        assertEquals(md1, metadata.get(0));
        assertEquals(md2, metadata.get(1));
        mdAttrs = deser.getMetadataAttributes();
        assertEquals(attr1, mdAttrs.get(0));
        assertEquals(attr2, mdAttrs.get(1));

        extelts = deser.getExtensibleElements();
        assertEquals(ext1, extelts.get(0));
        assertEquals(ext2, extelts.get(1));

        m = deser.getAllReferenceParameters();
        assertEquals("rp1", ((OMElement) m.get(rp1Qname)).getText());
        assertEquals("rp2", ((OMElement) m.get(rp2Qname)).getText());

        //Failure test
        try {
            deser = new EndpointReference("");
            EndpointReferenceHelper.fromOM(deser, om, AddressingConstants.Submission.WSA_NAMESPACE);
            fail("An exception should have been thrown due to failure to locate a wsa:Address field.");
        }
        catch (Exception e) {
            //pass
        }
    }

    public void testToAndFromOMForSubmissionSpecEPR() throws Exception {
        String address = "http://ws.apache.org/axis2";
        EndpointReference epr = new EndpointReference(address);

        OMFactory omf = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = omf.createOMNamespace("http://uri1", "prefix1");
        OMAttribute attr1 = omf.createOMAttribute("attr1", ns1, "attr1value");
        OMNamespace ns2 = omf.createOMNamespace("http://uri2", "prefix2");
        OMAttribute attr2 = omf.createOMAttribute("attr2", ns2, "attr2value");
        epr.addAttribute(attr1);
        epr.addAttribute(attr2);
        OMElement md1 = omf.createOMElement("md1", "http://mduri1", "md1prefix");
        OMElement md2 = omf.createOMElement("md2", "http://mduri2", "md2prefix");
        epr.addMetaData(md1);
        epr.addMetaData(md2);
        OMElement ext1 = omf.createOMElement("ext1", "http://exturi1", "ext1prefix");
        OMElement ext2 = omf.createOMElement("ext2", "http://exturi2", "ext2prefix");
        epr.addExtensibleElement(ext1);
        epr.addExtensibleElement(ext2);
        QName rp1Qname = new QName("http://rp1uri", "refParm1", "rp1prefix");
        QName rp2Qname = new QName("http://rp2uri", "refParm2", "rp2prefix");
        epr.addReferenceParameter(rp1Qname, "rp1");
        epr.addReferenceParameter(rp2Qname, "rp2");

        ArrayList addressAttributes = new ArrayList();
        addressAttributes.add(attr1);
        addressAttributes.add(attr2);
        epr.setAddressAttributes(addressAttributes);

        OMElement om = EndpointReferenceHelper.toOM(omf, epr, new QName("http://nsurl", "localName",
                                                                        "prefix"),
                                                              AddressingConstants.Submission.WSA_NAMESPACE);

        //Add some reference properties.
        QName p1Qname = new QName("http://p1uri", "refProp1", "p1prefix");
        QName p2Qname = new QName("http://p2uri", "refProp2", "p2prefix");
        QName qname = new QName(AddressingConstants.Submission.WSA_NAMESPACE, "ReferenceProperties",
                                AddressingConstants.WSA_DEFAULT_PREFIX);
        OMElement referenceProperties = omf.createOMElement(qname, om);
        OMElement prop1 = omf.createOMElement(p1Qname, referenceProperties);
        OMElement prop2 = omf.createOMElement(p2Qname, referenceProperties);
        prop1.setText("p1");
        prop2.setText("p2");

        //Test deserialize using fromOM(OMElement)
        EndpointReference deser = EndpointReferenceHelper.fromOM(om);

        assertEquals(epr.getAddress(), deser.getAddress());
        ArrayList addrAttrs = deser.getAddressAttributes();
        assertEquals(attr1, addrAttrs.get(0));
        assertEquals(attr2, addrAttrs.get(1));

        ArrayList attrs = deser.getAttributes();
        assertEquals(attr1, attrs.get(0));
        assertEquals(attr2, attrs.get(1));

        //Metadata will be lost unless it is saved as an extensibility element.
        ArrayList metadata = deser.getMetaData();
        assertNull(metadata);

        ArrayList extelts = deser.getExtensibleElements();
        assertEquals(ext1, extelts.get(0));
        assertEquals(ext2, extelts.get(1));

        //All reference properties are returned as reference parameters.
        Map m = deser.getAllReferenceParameters();
        assertEquals(4, m.size());
        assertEquals("rp1", ((OMElement) m.get(rp1Qname)).getText());
        assertEquals("rp2", ((OMElement) m.get(rp2Qname)).getText());
        assertEquals("p1", ((OMElement) m.get(p1Qname)).getText());
        assertEquals("p2", ((OMElement) m.get(p2Qname)).getText());

        //Test deserialize using fromOM(EndpointReference, OMElement, String)
        deser = new EndpointReference("");
        EndpointReferenceHelper.fromOM(deser, om, AddressingConstants.Submission.WSA_NAMESPACE);

        assertEquals(epr.getAddress(), deser.getAddress());
        addrAttrs = deser.getAddressAttributes();
        assertEquals(attr1, addrAttrs.get(0));
        assertEquals(attr2, addrAttrs.get(1));

        attrs = deser.getAttributes();
        assertEquals(attr1, attrs.get(0));
        assertEquals(attr2, attrs.get(1));

        //Metadata will be lost unless it is saved as an extensibility element.
        metadata = deser.getMetaData();
        assertNull(metadata);

        extelts = deser.getExtensibleElements();
        assertEquals(ext1, extelts.get(0));
        assertEquals(ext2, extelts.get(1));

        //All reference properties are returned as reference parameters.
        m = deser.getAllReferenceParameters();
        assertEquals(4, m.size());
        assertEquals("rp1", ((OMElement) m.get(rp1Qname)).getText());
        assertEquals("rp2", ((OMElement) m.get(rp2Qname)).getText());
        assertEquals("p1", ((OMElement) m.get(p1Qname)).getText());
        assertEquals("p2", ((OMElement) m.get(p2Qname)).getText());

        //Failure test
        try {
            deser = new EndpointReference("");
            EndpointReferenceHelper.fromOM(deser, om, AddressingConstants.Final.WSA_NAMESPACE);
            fail("An exception should have been thrown due to failure to locate a wsa:Address field.");
        }
        catch (Exception e) {
            //pass
        }
    }
    
    public void testSetAndGetWSDLLocationMetadataForFinalSpecEPR() throws Exception {
        String address = "http://ws.apache.org/axis2";
        String targetNamespace = "targetNamespace";
        String location = "wsdlLocation";
        
        EndpointReference epr = new EndpointReference(address);
        
        OMFactory omf = OMAbstractFactory.getOMFactory();
        
        // Uses final WSDLI namespace on wsdlLocation attribute
        EndpointReferenceHelper.setWSDLLocationMetadata(omf, epr, AddressingConstants.Final.WSA_NAMESPACE, new WSDLLocation(targetNamespace, location));
        
        WSDLLocation wsdlLocation = EndpointReferenceHelper.getWSDLLocationMetadata(epr, AddressingConstants.Final.WSA_NAMESPACE);
        assertEquals(wsdlLocation.getTargetNamespace(), targetNamespace);
        assertEquals(wsdlLocation.getLocation(), location);
    }
    
    public void testSetAndGetWSDLLocationMetadataForSubmissionSpecEPR() throws Exception {
        String address = "http://ws.apache.org/axis2";
        String targetNamespace = "targetNamespace";
        String location = "wsdlLocation";
        
        EndpointReference epr = new EndpointReference(address);
        
        OMFactory omf = OMAbstractFactory.getOMFactory();
        
        // Uses final WSDLI namespace on wsdlLocation attribute
        EndpointReferenceHelper.setWSDLLocationMetadata(omf, epr, AddressingConstants.Submission.WSA_NAMESPACE, new WSDLLocation(targetNamespace, location));
        
        WSDLLocation wsdlLocation = EndpointReferenceHelper.getWSDLLocationMetadata(epr, AddressingConstants.Submission.WSA_NAMESPACE);
        assertEquals(wsdlLocation.getTargetNamespace(), targetNamespace);
        assertEquals(wsdlLocation.getLocation(), location);
    }
    
    public void testGetWSDLLocationMetadataForFinalSpecEPRWithOldWsdliNamespace() throws Exception {
        String address = "http://ws.apache.org/axis2";
        String targetNamespace = "targetNamespace";
        String location = "wsdlLocation";
        
        EndpointReference epr = new EndpointReference(address);
        
        // Uses old candidate spec WSDLI namespace on wsdlLocation attribute
        OMFactory omf = OMAbstractFactory.getOMFactory();
        String value = new StringBuffer(targetNamespace).append(" ").append(location).toString();
        QName OLD_WSDLI = new QName("http://www.w3.org/2006/01/wsdl-instance", "wsdlLocation", "wsdli");
        OMNamespace wsdliNs = omf.createOMNamespace(OLD_WSDLI.getNamespaceURI(), OLD_WSDLI.getPrefix());
        OMAttribute attribute = omf.createOMAttribute(OLD_WSDLI.getLocalPart(), wsdliNs, value);
        
        ArrayList list = new ArrayList();
        list.add(attribute);
        epr.setMetadataAttributes(list);
        
        WSDLLocation wsdlLocation = EndpointReferenceHelper.getWSDLLocationMetadata(epr, AddressingConstants.Final.WSA_NAMESPACE);
        assertEquals(wsdlLocation.getTargetNamespace(), targetNamespace);
        assertEquals(wsdlLocation.getLocation(), location);  
    }
    
    public void testGetWSDLLocationMetadataForSubmissionSpecEPRWithOldWsdliNamespace() throws Exception {
        String address = "http://ws.apache.org/axis2";
        String targetNamespace = "targetNamespace";
        String location = "wsdlLocation";
        
        EndpointReference epr = new EndpointReference(address);
        
        // Uses old candidate spec WSDLI namespace on wsdlLocation attribute
        OMFactory omf = OMAbstractFactory.getOMFactory();
        String value = new StringBuffer(targetNamespace).append(" ").append(location).toString();
        QName OLD_WSDLI = new QName("http://www.w3.org/2006/01/wsdl-instance", "wsdlLocation", "wsdli");
        OMNamespace wsdliNs = omf.createOMNamespace(OLD_WSDLI.getNamespaceURI(), OLD_WSDLI.getPrefix());
        OMAttribute attribute = omf.createOMAttribute(OLD_WSDLI.getLocalPart(), wsdliNs, value);
        
        epr.addAttribute(attribute);
        
        WSDLLocation wsdlLocation = EndpointReferenceHelper.getWSDLLocationMetadata(epr, AddressingConstants.Submission.WSA_NAMESPACE);
        assertEquals(wsdlLocation.getTargetNamespace(), targetNamespace);
        assertEquals(wsdlLocation.getLocation(), location);  
    }
}
