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

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultNode;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.impl.dom.SOAPFaultValueImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultReasonImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultRoleImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultDetailImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultRoleImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultValueImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFaultElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SOAPFaultImpl extends SOAPBodyElementImpl implements SOAPFault {

    protected org.apache.axiom.soap.SOAPFault fault;
    private boolean isDetailAdded;
    private Locale faultReasonLocale;
    private boolean defaultsSet;

    /** @param fault  */
    public SOAPFaultImpl(org.apache.axiom.soap.SOAPFault fault) {
        super((ElementImpl)fault);
        this.fault = fault;
    }

    void setDefaults() throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            setFaultCode(SOAP11Constants.QNAME_SENDER_FAULTCODE);
        } else {
            setFaultCode(SOAP12Constants.QNAME_SENDER_FAULTCODE);
        }
        setFaultString("Fault string, and possibly fault code, not set");
        defaultsSet = true;
    }
    
    void removeDefaults() {
        if (defaultsSet) {
            SOAPFaultReason reason = this.fault.getReason();
            if (reason != null) {
                reason.detach();
            }
            defaultsSet = false;
        }
    }
    
    /**
     * Sets this <CODE>SOAPFault</CODE> object with the given fault code.
     * <p/>
     * Fault codes, which given information about the fault, are defined in the SOAP 1.1
     * specification. This element is mandatory in SOAP 1.1. Because the fault code is required to
     * be a QName it is preferable to use the setFaultCode(Name)form of this method.
     *
     * @param faultCode - a String giving the fault code to be set. It must be of the form
     *                  "prefix:localName" where the prefix has been defined in a namespace
     *                  declaration.
     * @throws SOAPException - if there was an error in adding the faultCode to the underlying XML
     *                       tree.
     * @see setFaultCode(Name), getFaultCode(),SOAPElement.addNamespaceDeclaration(String, String)
     */
    public void setFaultCode(String faultCode) throws SOAPException {
        org.apache.axiom.soap.SOAPFactory soapFactory = null;
        SOAPFaultCode soapFaultCode = null;

        //It must be of the form "prefix:localName" where the prefix has been defined in a
        //namespace declaration.
        if (faultCode.indexOf(":") == -1) {
            throw new SOAPException("faultCode must be of the form prefix:localName");
        }
//        	else{
//            	String prefix,localName ="";
//        		prefix = faultCode.substring(0, faultCode.indexOf(":"));
//        		localName = faultCode.substring(faultCode.indexOf(":")+1);
//        	}

        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            soapFactory = (SOAP11Factory)this.element.getOMFactory();
            soapFaultCode = soapFactory.createSOAPFaultCode(fault);
            soapFaultCode.setText(faultCode);
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            soapFactory = (SOAP12Factory)this.element.getOMFactory();
            soapFaultCode = soapFactory.createSOAPFaultCode(fault);
            SOAPFaultValue soapFaultValue = soapFactory.createSOAPFaultValue(soapFaultCode);
            soapFaultCode.setValue(soapFaultValue);
            soapFaultValue.setText(faultCode);
        }

        this.fault.setCode(soapFaultCode);
    }

    /**
     * Gets the fault code for this <CODE>SOAPFault</CODE> object.
     *
     * @return a <CODE>String</CODE> with the fault code
     * @see #setFaultCode(String) setFaultCode(java.lang.String)
     */
    public String getFaultCode() {
        if (fault != null && fault.getCode() != null) {
            if (this.element.getOMFactory() instanceof SOAP11Factory) {
                return fault.getCode().getText();
            } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
                return fault.getCode().getValue().getText();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    /**
     * Sets this SOAPFault object with the given fault actor.The fault actor is the recipient in the
     * message path who caused the fault to happen. If this SOAPFault supports SOAP 1.2 then this
     * call is equivalent to setFaultRole(String)
     *
     * @param faultActor - a String identifying the actor that caused this SOAPFault object
     * @throws SOAPException - if there was an error in adding the faultActor to the underlying XML
     *                       tree.
     */
    public void setFaultActor(String faultActor) throws SOAPException {

        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            if (this.fault.getRole() == null) {
                SOAP11FaultRoleImpl faultRoleImpl = new SOAP11FaultRoleImpl(
                        this.fault, (SOAPFactory)this.element.getOMFactory());

                faultRoleImpl.setRoleValue(faultActor);
                this.fault.setRole(faultRoleImpl);
            } else {
                SOAPFaultRole role = this.fault.getRole();
                role.setRoleValue(faultActor);
            }
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            if (this.fault.getRole() == null) {
                SOAP12FaultRoleImpl faultRoleImpl = new SOAP12FaultRoleImpl(
                        this.fault, (SOAPFactory)this.element.getOMFactory());

                faultRoleImpl.setRoleValue(faultActor);
                this.fault.setRole(faultRoleImpl);
            } else {
                SOAPFaultRole role = this.fault.getRole();
                role.setRoleValue(faultActor);
            }
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getFaultActor()
      */
    public String getFaultActor() {
        if (this.fault.getRole() != null) {
            return this.fault.getRole().getRoleValue();
        }
        return null;
    }

    /**
     * Sets the fault string for this <CODE>SOAPFault</CODE> object to the given string.
     *
     * @param faultString a <CODE>String</CODE> giving an explanation of the fault
     * @throws SOAPException if there was an error in adding the <CODE>faultString</CODE> to the
     *                       underlying XML tree.
     * @see #getFaultString() getFaultString()
     */
    public void setFaultString(String faultString) throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            setFaultString(faultString, null);
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            setFaultString(faultString, Locale.getDefault());
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getFaultString()
      */
    public String getFaultString() {

        if (this.fault.getNamespace().getNamespaceURI().equals(
                SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            return this.fault.getReason().getText();
        } else {
            if (this.fault.getReason() != null && this.fault.getReason().getFirstSOAPText() != null)
            {
                return this.fault.getReason().getFirstSOAPText().getText();
            }
        }
        return null;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getDetail()
      */
    public Detail getDetail() {
        return (Detail)toSAAJNode((org.w3c.dom.Node)fault.getDetail());
    }


    /**
     * Sets this SOAPFault object with the given fault code.Fault codes, which give information
     * about the fault, are defined in the SOAP 1.1 specification. A fault code is mandatory and
     * must be of type QName. This method provides a convenient way to set a fault code. For
     * example,
     * <p/>
     * SOAPEnvelope se = ...; // Create a qualified name in the SOAP namespace with a localName //
     * of Client. Note that prefix parameter is optional and is null // here which causes the
     * implementation to use an appropriate prefix. Name qname = se.createName(Client,
     * null,SOAPConstants.URI_NS_SOAP_ENVELOPE); SOAPFault fault = ...; fault.setFaultCode(qname);
     * <p/>
     * It is preferable to use this method over setFaultCode(String).
     *
     * @param faultCodeQName - a Name object giving the fault code to be set. It must be namespace
     *                       qualified.
     * @throws SOAPException - if there was an error in adding the faultcode element to the
     *                       underlying XML tree.
     */
    public void setFaultCode(Name faultCodeName) throws SOAPException {
        if (faultCodeName.getURI() == null || faultCodeName.getURI().trim().length() == 0) {
            throw new SOAPException("faultCodeQName must be namespace qualified.");
        }
        QName faultCodeQName = 
            new QName(faultCodeName.getURI(), faultCodeName.getLocalName(), faultCodeName.getPrefix());
        setFaultCode(faultCodeQName);
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#addDetail()
      */
    public Detail addDetail() throws SOAPException {
        if (isDetailAdded) {
            throw new SOAPException("This SOAPFault already contains a Detail element. " +
                    "Please remove the existing Detail element before " +
                    "calling addDetail()");
        }

        SOAPFaultDetail omDetail;
        SOAPFactory factory = (SOAPFactory)this.element.getOMFactory();
        if (factory instanceof SOAP11Factory) {
            omDetail = new SOAP11FaultDetailImpl(this.fault,
                                                 factory);
        } else {
            omDetail = new SOAP12FaultDetailImpl(this.fault,
                                                 factory);
        }
        Detail saajDetail = new DetailImpl(omDetail);
        ((NodeImpl)fault.getDetail()).setUserData(SAAJ_NODE, saajDetail, null);
        isDetailAdded = true;
        return saajDetail;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPFault#getFaultCodeAsName()
      */
    public Name getFaultCodeAsName() {
        return new PrefixedQName(getFaultCodeAsQName());
    }


    /**
     * Sets the fault string for this SOAPFault object to the given string. If this SOAPFault is
     * part of a message that supports SOAP 1.2 then this call is equivalent to:
     * addFaultReasonText(faultString, Locale.getDefault());
     *
     * @param faultString - a String giving an explanation of the fault
     * @throws SOAPException - if there was an error in adding the faultString to the underlying XML
     *                       tree.
     * @see getFaultString()
     */

    public void setFaultString(String faultString, Locale locale) throws SOAPException {
        if (this.fault.getReason() != null) {
            SOAPFaultReason reason = this.fault.getReason();
            if (this.element.getOMFactory() instanceof SOAP11Factory) {
                reason.setText(faultString);
            } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
                addFaultReasonText(faultString, locale);
            }
        } else {
            if (this.element.getOMFactory() instanceof SOAP11Factory) {
                SOAPFaultReason reason = new SOAP11FaultReasonImpl(this.fault,
                                                                   (SOAPFactory)this.element
                                                                           .getOMFactory());
                reason.setText(faultString);
            } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
                addFaultReasonText(faultString, locale);
            }
        }
        this.faultReasonLocale = locale;
    }

    /**
     * Gets the locale of the fault string for this SOAPFault object. If this SOAPFault is part of a
     * message that supports SOAP 1.2 then this call is equivalent to:
     * <p/>
     * Locale locale = null; try { locale = (Locale) getFaultReasonLocales().next(); } catch
     * (SOAPException e) {} return locale;
     *
     * @return a Locale object indicating the native language of the fault string or null if no
     *         locale was specified
     * @see setFaultString(String, Locale)
     * @since SAAJ 1.2
     */
    public Locale getFaultStringLocale() {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            return this.faultReasonLocale;
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            Locale locale = null;
            try {
                if (getFaultReasonLocales().hasNext()) {
                    locale = (Locale)getFaultReasonLocales().next();
                }
            }
            catch (SOAPException e) {
                e.printStackTrace();
            }
            return locale;
        } else {
            return null;
        }

    }

    /**
     * Appends or replaces a Reason Text item containing the specified text message and an xml:lang
     * derived from locale. If a Reason Text item with this xml:lang already exists its text value
     * will be replaced with text. The locale parameter should not be null Code sample: SOAPFault
     * fault = ...; fault.addFaultReasonText(Version Mismatch, Locale.ENGLISH);
     *
     * @param text - reason message string locale - Locale object representing the locale of the
     *             message
     * @throws SOAPException - if there was an error in adding the Reason text or the locale passed
     *                       was null. java.lang.UnsupportedOperationException - if this message
     *                       does not support the SOAP 1.2 concept of Fault Reason.
     */
    public void addFaultReasonText(String text, Locale locale) throws SOAPException {
        if (locale == null) {
            throw new SOAPException("Received null for locale");
        }
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException("Not supported in SOAP 1.1");
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            removeDefaults();
            
            String existingReasonText = getFaultReasonText(locale);
            if (existingReasonText == null) {
                org.apache.axiom.soap.SOAPFactory soapFactory = null;
                soapFactory = (SOAP12Factory)this.element.getOMFactory();
                if (this.fault.getReason() == null) {
                    SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason(this.fault);
                    this.fault.setReason(soapFaultReason);
                }
                SOAPFaultText soapFaultText =
                        soapFactory.createSOAPFaultText(this.fault.getReason());
                soapFaultText.setText(text);
                soapFaultText.setLang(locale.toString());
            } else {
                //update the text
                Iterator soapTextsItr = this.fault.getReason().getAllSoapTexts().iterator();
                while (soapTextsItr.hasNext()) {
                    SOAPFaultText soapFaultText = (SOAPFaultText)soapTextsItr.next();
                    if (soapFaultText.getLang().equals(locale.toString())) {
                        soapFaultText.setText(text);
                    }
                }

            }

        }


    }


    /**
     * Adds a Subcode to the end of the sequence of Subcodes contained by this SOAPFault. Subcodes,
     * which were introduced in SOAP 1.2, are represented by a recursive sequence of subelements
     * rooted in the mandatory Code subelement of a SOAP Fault.
     *
     * @param subcode - a QName containing the Value of the Subcode.
     * @throws SOAPException - if there was an error in setting the Subcode java.lang.UnsupportedOperationException
     *                       - if this message does not support the SOAP 1.2 concept of Subcode.
     */

    public void appendFaultSubcode(QName subcode) throws SOAPException {
        org.apache.axiom.soap.SOAPFactory soapFactory = null;
        SOAPFaultSubCode soapFaultSubCode = null;

        if (subcode.getNamespaceURI() == null || subcode.getNamespaceURI().trim().length() == 0) {
            throw new SOAPException("Unqualified QName object : " + subcode);
        }
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            soapFactory = DOOMAbstractFactory.getSOAP12Factory();
        }

        if (this.fault.getCode() == null) {
            soapFactory.createSOAPFaultCode(this.fault);
            //if SOAPFault is null, there cannot be a subcode.
            //Hence should create one
            soapFaultSubCode = soapFactory.createSOAPFaultSubCode(this.fault.getCode());
        } else if (this.fault.getCode().getSubCode() != null) {
            //find the last subcode.parent of the new subcode should be the this last subcode
            soapFaultSubCode = soapFactory.createSOAPFaultSubCode(
                    getLastSubCode(this.fault.getCode().getSubCode()));
        } else {
            //FaultCode is there, but no FaultSubCode
            soapFaultSubCode = soapFactory.createSOAPFaultSubCode(this.fault.getCode());
        }


        if (soapFaultSubCode != null) {
            SOAPFaultValueImpl soapFaultValueimpl =
                    new SOAP12FaultValueImpl(soapFaultSubCode, soapFactory);
            soapFaultValueimpl.setText(subcode.getPrefix() + ":" + subcode.getLocalPart());
            soapFaultValueimpl.declareNamespace(subcode.getNamespaceURI(), subcode.getPrefix());
        }
    }

    private SOAPFaultSubCode getLastSubCode(SOAPFaultSubCode firstSubCodeElement) {
        SOAPFaultSubCode soapFaultSubCode = firstSubCodeElement.getSubCode();
        if (soapFaultSubCode != null) {
            return getLastSubCode(soapFaultSubCode);
        }
        return firstSubCodeElement;
    }

    /**
     * Gets the fault code for this SOAPFault object as a <CODE>QName</CODE> object.
     * <p/>
     */
    public QName getFaultCodeAsQName() {
        SOAPFaultCode soapFaultCode = this.fault.getCode();
        if (soapFaultCode != null) {
            if (this.element.getOMFactory() instanceof SOAP11Factory) {
                return soapFaultCode.getTextAsQName();
            } else {
                return soapFaultCode.getValue().getTextAsQName();
            }
        }
        return null;
    }

    /**
     * Returns the optional Node element value for this SOAPFault object. The Node element is
     * optional in SOAP 1.2.
     *
     * @return Content of the env:Fault/env:Node element as a String or null if none
     * @throws UnsupportedOperationException
     *          - if this message does not support the SOAP 1.2 concept of Fault Node.
     */
    public String getFaultNode() {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException("Message does not support the " +
                    "SOAP 1.2 concept of Fault Node");
        } else {
            if (fault != null && fault.getNode() != null && fault.getNode().getText() != null) {
                return fault.getNode().getText();
            }
        }
        return null;

    }

    /**
     * Returns an Iterator over a distinct sequence of Locales for which there are associated Reason
     * Text items. Any of these Locales can be used in a call to getFaultReasonText in order to
     * obtain a localized version of the Reason Text string.
     *
     * @return an Iterator over a sequence of Locale objects for which there are associated Reason
     *         Text items.
     * @throws SOAPException - if there was an error in retrieving the fault Reason locales.
     *                       java.lang.UnsupportedOperationException - if this message does not
     *                       support the SOAP 1.2 concept of Fault Reason.
     * @since SAAJ 1.3
     */
    public Iterator getFaultReasonLocales() throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException("Message does not support the " +
                    "SOAP 1.2 concept of Fault Reason");
        } else {
            ArrayList faultReasonLocales = new ArrayList();
            List soapTextList = this.fault.getReason().getAllSoapTexts();
            if (soapTextList != null) {
                Iterator faultReasons = soapTextList.iterator();
                while (faultReasons.hasNext()) {
                    SOAPFaultText soapFaultText = (SOAPFaultText)faultReasons.next();
                    String lang = soapFaultText.getLang();
                    if (lang == null) {
                        faultReasonLocales.add(Locale.getDefault());
                    } else {
                        if (lang.indexOf("_") != -1) {
                            String language = lang.substring(0, lang.indexOf("_"));
                            String country = lang.substring(lang.indexOf("_") + 1);
                            faultReasonLocales.add(new Locale(language, country));
                        } else {
                            faultReasonLocales.add(new Locale(lang));
                        }
                    }
                }
            }
            return faultReasonLocales.iterator();
        }
    }

    /**
     * Returns the Reason Text associated with the given Locale. If more than one such Reason Text
     * exists the first matching Text is returned
     *
     * @param locale - the Locale for which a localized Reason Text is desired
     * @return the Reason Text associated with locale
     * @throws SOAPException - if there was an error in retrieving the fault Reason text for the
     *                       specified locale. java.lang.UnsupportedOperationException - if this
     *                       message does not support the SOAP 1.2 concept of Fault Reason.
     * @since SAAJ 1.3
     */
    public String getFaultReasonText(Locale locale) throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException("Message does not support the " +
                    "SOAP 1.2 concept of Fault Reason");
        } else {
            Iterator soapTextsItr = null;
            SOAPFaultReason soapFaultReason = this.fault.getReason();
            if (soapFaultReason != null) {
                List soapTexts = soapFaultReason.getAllSoapTexts();
                if (soapTexts != null) {
                    soapTextsItr = soapTexts.iterator();
                    while (soapTextsItr.hasNext()) {
                        SOAPFaultText soapFaultText = (SOAPFaultText)soapTextsItr.next();
                        if (soapFaultText.getLang().equals(locale.toString())) {
                            return soapFaultText.getText();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns an Iterator over a sequence of String objects containing all of the Reason Text items
     * for this SOAPFault.
     *
     * @throws SOAPException if there is an error in retrieving texts for Reason objects
     *                       java.lang.UnsupportedOperationException - if this message does not
     *                       support the SOAP 1.2 concept of Fault Reason.
     */

    public Iterator getFaultReasonTexts() throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        }

        Iterator soapTextsItr = this.fault.getReason().getAllSoapTexts().iterator();
        ArrayList reasonTexts = new ArrayList();
        while (soapTextsItr.hasNext()) {
            SOAPFaultText soapFaultText = (SOAPFaultText)soapTextsItr.next();
            reasonTexts.add(soapFaultText.getText());
        }
        return reasonTexts.iterator();
    }

    /**
     * Returns the optional Role element value for this SOAPFault object. The Role element is
     * optional in SOAP 1.2.
     *
     * @return Content of the env:Fault/env:Role element as a String or null if none
     * @throws UnsupportedOperationException
     *          - if this message does not support the SOAP 1.2 concept of Fault Role.
     * @since SAAJ 1.3
     */
    public String getFaultRole() {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException("Message does not support the " +
                    "SOAP 1.2 concept of Fault Reason");
        } else {
            if (this.fault.getRole() != null) {
                return this.fault.getRole().getText();
            } else {
                return null;
            }
        }
    }

    /**
     * Gets the Subcodes for this SOAPFault as an iterator over QNames.
     *
     * @return an Iterator that accesses a sequence of QNames. This Iterator should not support the
     *         optional remove method. The order in which the Subcodes are returned reflects the
     *         hierarchy of Subcodes present in the fault from top to bottom.
     * @throws UnsupportedOperationException
     *          - if this message does not support the SOAP 1.2 concept of Subcode.
     */
    public Iterator getFaultSubcodes() {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        }
        ArrayList faultSubcodes = new ArrayList();
        SOAPFaultSubCode subCodeElement = this.fault.getCode().getSubCode();
        while (subCodeElement != null) {
            QName qname = subCodeElement.getValue().getTextAsQName();
            faultSubcodes.add(qname);
            subCodeElement = subCodeElement.getSubCode();
        }
        return faultSubcodes.iterator();
    }

    /** Returns true if this SOAPFault has a Detail subelement and false otherwise. */
    public boolean hasDetail() {
        if (this.fault.getDetail() != null) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Removes any Subcodes that may be contained by this SOAPFault. Subsequent calls to
     * getFaultSubcodes will return an empty iterator until a call to appendFaultSubcode is made.
     *
     * @throws UnsupportedOperationException
     *          - if this message does not support the SOAP 1.2 concept of Subcode.
     */
    public void removeAllFaultSubcodes() {
        if (factory instanceof SOAP11Factory) {
            throw new UnsupportedOperationException();
        } else {
            fault.getCode().getSubCode().detach();
        }
    }


    /**
     * Sets this SOAPFault object with the given fault code. It is preferable to use this method
     * over setFaultCode(Name)
     *
     * @param faultCodeQName - a QName object giving the fault code to be set. It must be namespace
     *                       qualified.
     * @throws SOAPException - if there was an error in adding the faultcode element to the
     *                       underlying XML tree.
     * @see getFaultCodeAsQName(), setFaultCode(Name), getFaultCodeAsQName()
     * @since SAAJ 1.3
     */
    public void setFaultCode(QName qname) throws SOAPException {
        if (qname.getNamespaceURI() == null
                || qname.getNamespaceURI().trim().length() == 0) {
            throw new SOAPException("Unqualified QName object : " + qname);
        }

        org.apache.axiom.soap.SOAPFactory soapFactory = null;
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            soapFactory = (SOAPFactory)this.element.getOMFactory();
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            if (!(qname.getNamespaceURI()
                    .equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE))) {
                throw new SOAPException("Incorrect URI"
                        + qname.getNamespaceURI());
            }
            soapFactory = (SOAPFactory)this.element.getOMFactory();
        } else {
            throw new SOAPException("Invalid SOAP version");
        }
        SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode(this.fault);

        String prefix = ((qname.getPrefix() != null) && !qname.getPrefix()
                .equals("")) ? qname.getPrefix() : this.fault.getQName()
                .getPrefix();

        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            soapFaultCode.setText(prefix + ":" + qname.getLocalPart());
            OMNamespace omNamespace = new OMNamespaceImpl(qname.getNamespaceURI(),
                                                          qname.getPrefix());
            soapFaultCode.declareNamespace(omNamespace);
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            SOAPFaultValue soapFaultValue = soapFactory.createSOAPFaultValue(soapFaultCode);
            // don't just use the default prefix, use the passed one or the parent's
            soapFaultValue.setText(prefix + ":" + qname.getLocalPart());
            OMNamespace omNamespace = new OMNamespaceImpl(qname.getNamespaceURI(),
                                                          qname.getPrefix());
            soapFaultValue.declareNamespace(omNamespace);
            soapFaultCode.setValue(soapFaultValue);
        }
        
        this.fault.setCode(soapFaultCode);
    }

    /**
     * Creates or replaces any existing Node element value for this SOAPFault object. The Node
     * element is optional in SOAP 1.2.
     *
     * @throws SOAPException - if there was an error in setting the Node for this SOAPFault object.
     *                       java.lang.UnsupportedOperationException - if this message does not
     *                       support the SOAP 1.2 concept of Fault Node.
     * @since SAAJ 1.3
     */

    public void setFaultNode(String s) throws SOAPException {
        org.apache.axiom.soap.SOAPFactory soapFactory = null;
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException("message does not support " +
                    "the SOAP 1.2 concept of Fault Node");
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            soapFactory = DOOMAbstractFactory.getSOAP12Factory();
        }
        SOAPFaultNode soapFaultNode = soapFactory.createSOAPFaultNode(this.fault);
        soapFaultNode.setText(s);
        this.fault.setNode(soapFaultNode);
    }

    /**
     * Creates or replaces any existing Role element value for this SOAPFault object. The Role
     * element is optional in SOAP 1.2.
     *
     * @param uri - the URI of the Role
     * @throws SOAPException - if there was an error in setting the Role for this SOAPFault object
     *                       java.lang.UnsupportedOperationException - if this message does not
     *                       support the SOAP 1.2 concept of Fault Role.
     */
    public void setFaultRole(String uri) throws SOAPException {
        org.apache.axiom.soap.SOAPFactory soapFactory = null;
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            throw new UnsupportedOperationException("message does not support the " +
                    "SOAP 1.2 concept of Fault Role");
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            soapFactory = DOOMAbstractFactory.getSOAP12Factory();
        }
        SOAPFaultRole soapFaultRole = soapFactory.createSOAPFaultRole(this.fault);
        soapFaultRole.setRoleValue(uri);
        this.fault.setRole(soapFaultRole);
    }

    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        return getChildren(element.getChildrenWithName(qName));
    }

    public Iterator getChildElements() {
        return getChildren(element.getChildren());
    }

    private Iterator getChildren(Iterator childIter) {
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            org.w3c.dom.Node domNode = (org.w3c.dom.Node)childIter.next();
            Node saajNode = toSAAJNode(domNode);
            if (!(saajNode instanceof SOAPFaultElement)) {
                // silently replace node, as per saaj 1.2 spec
                SOAPFaultElement bodyEle = new SOAPFaultElementImpl((ElementImpl)domNode);
                ((NodeImpl)domNode).setUserData(SAAJ_NODE, bodyEle, null);
                childElements.add(bodyEle);
            } else {
                childElements.add(saajNode);
            }
        }
        return childElements.iterator();
    }

}
