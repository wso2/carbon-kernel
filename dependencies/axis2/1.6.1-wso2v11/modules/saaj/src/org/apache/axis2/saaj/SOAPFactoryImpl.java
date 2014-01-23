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

package org.apache.axis2.saaj;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import java.util.Locale;

/**
 * 
 */
public class SOAPFactoryImpl extends SOAPFactory {

    protected String soapVersion = SOAPConstants.SOAP_1_1_PROTOCOL;

    /**
     * Create a <code>SOAPElement</code> object initialized with the given <code>Name</code>
     * object.
     *
     * @param name a <code>Name</code> object with the XML name for the new element
     * @return the new <code>SOAPElement</code> object that was created
     * @throws javax.xml.soap.SOAPException if there is an error in creating the <code>SOAPElement</code>
     *                                      object
     */
    public SOAPElement createElement(Name name) throws SOAPException {
        String localName = name.getLocalName();
        String prefix = name.getPrefix();
        String uri = name.getURI();
        OMElement omElement = null;
        if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            omElement = DOOMAbstractFactory.getSOAP12Factory().createOMElement(localName
                    , uri, prefix);
        } else {
            omElement = DOOMAbstractFactory.getSOAP11Factory().createOMElement(localName
                    , uri, prefix);
        }
        DOOMAbstractFactory.getOMFactory().createOMElement(localName, uri, prefix);
        return new SOAPElementImpl((ElementImpl)omElement);
    }

    /**
     * Create a <code>SOAPElement</code> object initialized with the given local name.
     *
     * @param localName a <code>String</code> giving the local name for the new element
     * @return the new <code>SOAPElement</code> object that was created
     * @throws javax.xml.soap.SOAPException if there is an error in creating the <code>SOAPElement</code>
     *                                      object
     */
    public SOAPElement createElement(String localName) throws SOAPException {
        OMDOMFactory omdomFactory = null;
        if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            omdomFactory = (OMDOMFactory)DOOMAbstractFactory.getSOAP12Factory();
        } else {
            omdomFactory = (OMDOMFactory)DOOMAbstractFactory.getSOAP11Factory();
        }
        OMElement omElement = omdomFactory.createOMElement(new QName(localName));
        return new SOAPElementImpl((ElementImpl)omElement);
    }

    /**
     * Create a new <code>SOAPElement</code> object with the given local name, prefix and uri.
     *
     * @param localName a <code>String</code> giving the local name for the new element
     * @param prefix    the prefix for this <code>SOAPElement</code>
     * @param uri       a <code>String</code> giving the URI of the namespace to which the new
     *                  element belongs
     * @return the new <code>SOAPElement</code> object that was created
     * @throws javax.xml.soap.SOAPException if there is an error in creating the <code>SOAPElement</code>
     *                                      object
     */
    public SOAPElement createElement(String localName, String prefix, String uri)
            throws SOAPException {
        OMElement omElement = null;
        if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            omElement = DOOMAbstractFactory.getSOAP12Factory().createOMElement(localName
                    , uri, prefix);
        } else {
            omElement = DOOMAbstractFactory.getSOAP11Factory().createOMElement(localName
                    , uri, prefix);
        }
        return new SOAPElementImpl((ElementImpl)omElement);
    }

    /**
     * Creates a new <code>Detail</code> object which serves as a container for
     * <code>DetailEntry</code> objects.
     * <p/>
     * This factory method creates <code>Detail</code> objects for use in situations where it is not
     * practical to use the <code>SOAPFault</code> abstraction.
     *
     * @return a <code>Detail</code> object
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public Detail createDetail() throws SOAPException {
        if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            return new DetailImpl(DOOMAbstractFactory.getSOAP12Factory().createSOAPFaultDetail());
        } else {
            return new DetailImpl(DOOMAbstractFactory.getSOAP11Factory().createSOAPFaultDetail());
        }
    }

    /**
     * Creates a new <code>Name</code> object initialized with the given local name, namespace
     * prefix, and namespace URI.
     * <p/>
     * This factory method creates <code>Name</code> objects for use in situations where it is not
     * practical to use the <code>SOAPEnvelope</code> abstraction.
     *
     * @param localName a <code>String</code> giving the local name
     * @param prefix    a <code>String</code> giving the prefix of the namespace
     * @param uri       a <code>String</code> giving the URI of the namespace
     * @return a <code>Name</code> object initialized with the given local name, namespace prefix,
     *         and namespace URI
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public Name createName(String localName, String prefix, String uri) throws SOAPException {
        return new PrefixedQName(uri, localName, prefix);
    }

    /**
     * Creates a new <code>Name</code> object initialized with the given local name.
     * <p/>
     * This factory method creates <code>Name</code> objects for use in situations where it is not
     * practical to use the <code>SOAPEnvelope</code> abstraction.
     *
     * @param localName a <code>String</code> giving the local name
     * @return a <code>Name</code> object initialized with the given local name
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public Name createName(String localName) throws SOAPException {
        return new PrefixedQName(null, localName, null);
    }

    /**
     * Creates a new default SOAPFault object
     *
     * @return a SOAPFault object
     * @throws SOAPException - if there is a SOAP error
     */
    public SOAPFault createFault() throws SOAPException {
        org.apache.axiom.soap.SOAPFactory soapFactory;
        if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            soapFactory = DOOMAbstractFactory.getSOAP12Factory();
            return new SOAPFaultImpl(soapFactory.createSOAPFault());
        } else {
            soapFactory = DOOMAbstractFactory.getSOAP11Factory();
            return new SOAPFaultImpl(soapFactory.createSOAPFault());
        }
    }

    /**
     * Creates a new SOAPFault object initialized with the given reasonText and faultCode
     *
     * @param reasonText - the ReasonText/FaultString for the fault faultCode - the FaultCode for
     *                   the fault
     * @return: a SOAPFault object
     * @throws: SOAPException - if there is a SOAP error
     */
    public SOAPFault createFault(String reasonText, QName faultCode) throws SOAPException {
        SOAPFault soapFault;
        if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            soapFault = new SOAPFaultImpl(DOOMAbstractFactory.getSOAP12Factory()
                    .createSOAPFault());
        } else {
            soapFault = new SOAPFaultImpl(DOOMAbstractFactory.getSOAP11Factory()
                    .createSOAPFault());
        }
        soapFault.setFaultCode(faultCode);
        try {
            soapFault.addFaultReasonText(reasonText, Locale.getDefault());
        } catch (UnsupportedOperationException e) {
            throw new SOAPException(e.getMessage());
        }

        return soapFault;
    }

    public void setSOAPVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }


    public SOAPElement createElement(QName qname) throws SOAPException {
        String localName = qname.getLocalPart();
        String prefix = qname.getPrefix();
        String uri = qname.getNamespaceURI();
        OMElement omElement = DOOMAbstractFactory.getOMFactory().createOMElement(localName
                , uri, prefix);
        return new SOAPElementImpl((ElementImpl)omElement);
    }

    public SOAPElement createElement(Element element) throws SOAPException {
        OMDOMFactory omdomFactory = null;
        if (soapVersion.equals(SOAPConstants.SOAP_1_2_PROTOCOL)) {
            omdomFactory = (OMDOMFactory)DOOMAbstractFactory.getSOAP12Factory();
        } else {
            omdomFactory = (OMDOMFactory)DOOMAbstractFactory.getSOAP11Factory();
        }
        OMNamespace ns = omdomFactory.createOMNamespace(element.getNamespaceURI()
                , element.getPrefix());
        OMElement omElement = omdomFactory.createOMElement(element.getLocalName(), ns);
        return new SOAPElementImpl((ElementImpl)omElement);
    }

}
