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

import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11BodyImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11HeaderImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12HeaderImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

/**
 *
 */
public class SOAPEnvelopeImpl extends SOAPElementImpl implements javax.xml.soap.SOAPEnvelope {

    private org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl omSOAPEnvelope;
    private SOAPPartImpl soapPart;

    public SOAPEnvelopeImpl(final org.apache.axiom.soap.impl.dom.SOAPEnvelopeImpl envelope) {
        super(envelope);
        omSOAPEnvelope = envelope;
    }

    public void setOwnerDocument(Document document) {
        super.setOwnerDocument((DocumentImpl)document);
    }

    public org.apache.axiom.soap.SOAPEnvelope getOMEnvelope() {
        return omSOAPEnvelope;
    }

    /**
     * Creates a new <CODE>Name</CODE> object initialized with the given local name, namespace
     * prefix, and namespace URI.
     * <p/>
     * <P>This factory method creates <CODE>Name</CODE> objects for use in the SOAP/XML document.
     *
     * @param localName a <CODE>String</CODE> giving the local name
     * @param prefix    a <CODE>String</CODE> giving the prefix of the namespace
     * @param uri       a <CODE>String</CODE> giving the URI of the namespace
     * @return a <CODE>Name</CODE> object initialized with the given local name, namespace prefix,
     *         and namespace URI
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public Name createName(String localName, String prefix, String uri) throws SOAPException {
        try {
            return new PrefixedQName(uri, localName, prefix);
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Creates a new <CODE>Name</CODE> object initialized with the given local name.
     * <p/>
     * <P>This factory method creates <CODE>Name</CODE> objects for use in the SOAP/XML document.
     *
     * @param localName a <CODE>String</CODE> giving the local name
     * @return a <CODE>Name</CODE> object initialized with the given local name
     * @throws javax.xml.soap.SOAPException if there is a SOAP error
     */
    public Name createName(String localName) throws SOAPException {
        try {
            return new PrefixedQName(null, localName, null);
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Returns the <CODE>SOAPHeader</CODE> object for this <CODE> SOAPEnvelope</CODE> object.
     * <p/>
     * <P>A new <CODE>SOAPMessage</CODE> object is by default created with a
     * <CODE>SOAPEnvelope</CODE> object that contains an empty <CODE>SOAPHeader</CODE> object. As a
     * result, the method <CODE>getHeader</CODE> will always return a <CODE>SOAPHeader</CODE> object
     * unless the header has been removed and a new one has not been added.
     *
     * @return the <CODE>SOAPHeader</CODE> object or <CODE> null</CODE> if there is none
     * @throws javax.xml.soap.SOAPException if there is a problem obtaining the <CODE>SOAPHeader</CODE>
     *                                      object
     */
    public SOAPHeader getHeader() throws SOAPException {
        return (SOAPHeader)toSAAJNode((org.w3c.dom.Node)omSOAPEnvelope.getHeader());
    }

    /**
     * Returns the <CODE>SOAPBody</CODE> object associated with this <CODE>SOAPEnvelope</CODE>
     * object.
     * <p/>
     * <P>A new <CODE>SOAPMessage</CODE> object is by default created with a
     * <CODE>SOAPEnvelope</CODE> object that contains an empty <CODE>SOAPBody</CODE> object. As a
     * result, the method <CODE>getBody</CODE> will always return a <CODE>SOAPBody</CODE> object
     * unless the body has been removed and a new one has not been added.
     *
     * @return the <CODE>SOAPBody</CODE> object for this <CODE> SOAPEnvelope</CODE> object or
     *         <CODE>null</CODE> if there is none
     * @throws javax.xml.soap.SOAPException if there is a problem obtaining the <CODE>SOAPBody</CODE>
     *                                      object
     */
    public SOAPBody getBody() throws SOAPException {
        return (SOAPBody)toSAAJNode((org.w3c.dom.Node)omSOAPEnvelope.getBody());
    }

    /**
     * Creates a <CODE>SOAPHeader</CODE> object and sets it as the <CODE>SOAPHeader</CODE> object
     * for this <CODE> SOAPEnvelope</CODE> object.
     * <p/>
     * <P>It is illegal to add a header when the envelope already contains a header. Therefore, this
     * method should be called only after the existing header has been removed.
     *
     * @return the new <CODE>SOAPHeader</CODE> object
     * @throws javax.xml.soap.SOAPException if this <CODE> SOAPEnvelope</CODE> object already
     *                                      contains a valid <CODE>SOAPHeader</CODE> object
     */
    public SOAPHeader addHeader() throws SOAPException {
        org.apache.axiom.soap.SOAPHeader header = omSOAPEnvelope.getHeader();
        if (header == null) {
            SOAPHeaderImpl saajSOAPHeader;
            if (this.element.getOMFactory() instanceof SOAP11Factory) {
                header = new SOAP11HeaderImpl(omSOAPEnvelope,
                                              (SOAPFactory)this.element.getOMFactory());
                saajSOAPHeader = new SOAPHeaderImpl(header);
                saajSOAPHeader.setParentElement(this);
            } else {
                header = new SOAP12HeaderImpl(omSOAPEnvelope,
                                              (SOAPFactory)this.element.getOMFactory());
                saajSOAPHeader = new SOAPHeaderImpl(header);
                saajSOAPHeader.setParentElement(this);
            }
            ((NodeImpl)omSOAPEnvelope.getHeader()).setUserData(SAAJ_NODE, saajSOAPHeader, null);
            return saajSOAPHeader;
        } else {
            throw new SOAPException("Header already present, can't set header again without " +
                    "deleting the existing header. " +
                    "Use getHeader() method and detach the header instead.");
        }
    }

    /**
     * Creates a <CODE>SOAPBody</CODE> object and sets it as the <CODE>SOAPBody</CODE> object for
     * this <CODE> SOAPEnvelope</CODE> object.
     * <p/>
     * <P>It is illegal to add a body when the envelope already contains a body. Therefore, this
     * method should be called only after the existing body has been removed.
     *
     * @return the new <CODE>SOAPBody</CODE> object
     * @throws javax.xml.soap.SOAPException if this <CODE> SOAPEnvelope</CODE> object already
     *                                      contains a valid <CODE>SOAPBody</CODE> object
     */
    public SOAPBody addBody() throws SOAPException {
        org.apache.axiom.soap.SOAPBody body = omSOAPEnvelope.getBody();
        if (body == null) {
            body = new SOAP11BodyImpl(omSOAPEnvelope, (SOAPFactory)this.element.getOMFactory());
            SOAPBodyImpl saajSOAPBody = new SOAPBodyImpl(body);
            saajSOAPBody.setParentElement(this);
            ((NodeImpl)omSOAPEnvelope.getBody()).setUserData(SAAJ_NODE, saajSOAPBody, null);
            return saajSOAPBody;
        } else {
            throw new SOAPException("Body already present, can't set body again without " +
                    "deleting the existing body. Use getBody() method instead.");
        }
    }

    public SOAPElement addTextNode(String text) throws SOAPException {
        Node firstChild = element.getFirstChild();
        if (firstChild instanceof org.w3c.dom.Text) {
            ((org.w3c.dom.Text)firstChild).setData(text);
        } else {
            // Else this is a header
            TextImpl doomText = new TextImpl(text, this.element.getOMFactory());
            doomText.setNextOMSibling((NodeImpl)firstChild);
            doomText.setPreviousOMSibling(null);
            element.setFirstChild(doomText);
            ((NodeImpl)firstChild).setPreviousOMSibling(doomText);
        }
        return this;
    }

    /**
     * Override SOAPElement.addAttribute SOAP1.2 should not allow encodingStyle attribute to be set
     * on Envelop
     */
    public SOAPElement addAttribute(Name name, String value) throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP12Factory) {
            if ("encodingStyle".equals(name.getLocalName())) {
                throw new SOAPException(
                        "SOAP1.2 does not allow encodingStyle attribute to be set " +
                                "on Envelope");
            }
        }
        return super.addAttribute(name, value);
    }

    /**
     * Override SOAPElement.addChildElement SOAP 1.2 should not allow element to be added after body
     * element
     */
    public SOAPElement addChildElement(Name name) throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP12Factory) {
            throw new SOAPException("Cannot add elements after body element");
        } else if (this.element.getOMFactory() instanceof SOAP11Factory) {
            //Let elements to be added any where.
            return super.addChildElement(name);
        }
        return null;
    }
    
    /**
     * Set SOAPPart parent
     * @param sp
     */
    void setSOAPPartParent(SOAPPartImpl sp) {
        this.soapPart = sp;
    }
    
    /**
     * @return SOAPPart
     */
    SOAPPartImpl getSOAPPartParent() {
        return this.soapPart;
    }
}
