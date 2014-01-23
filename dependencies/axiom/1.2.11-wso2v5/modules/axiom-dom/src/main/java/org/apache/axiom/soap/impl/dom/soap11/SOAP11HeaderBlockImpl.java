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

package org.apache.axiom.soap.impl.dom.soap11;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.impl.dom.SOAPHeaderBlockImpl;

public class SOAP11HeaderBlockImpl extends SOAPHeaderBlockImpl {
    /**
     * @param localName
     * @param ns
     */
    public SOAP11HeaderBlockImpl(String localName,
                                 OMNamespace ns,
                                 SOAPHeader parent,
                                 SOAPFactory factory) throws SOAPProcessingException {
        super(localName, ns, parent, factory);
        checkParent(parent);
    }

    public SOAP11HeaderBlockImpl(String localName, OMNamespace ns, SOAPFactory factory) {
        super(localName, ns, factory);
    }

    /**
     * Constructor SOAPHeaderBlockImpl
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public SOAP11HeaderBlockImpl(String localName,
                                 OMNamespace ns,
                                 OMElement parent,
                                 OMXMLParserWrapper builder,
                                 SOAPFactory factory) {
        super(localName, ns, parent, builder, factory);
    }


    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11HeaderImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Body as the parent. But received some other implementation");
        }
    }

    public void setRole(String roleURI) {
        setAttribute(SOAP11Constants.ATTR_ACTOR,
                     roleURI,
                     SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

    }

    public String getRole() {
        return getAttribute(SOAP11Constants.ATTR_ACTOR,
                            SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    //TODO : implement
    public void setRelay(boolean relay) {
        throw new UnsupportedOperationException("Not supported for SOAP 1.1");
    }

    //TODO : implement
    public boolean getRelay() {
        throw new UnsupportedOperationException("Not supported for SOAP 1.1");
    }


    public void setMustUnderstand(boolean mustUnderstand) {
        setAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND,
                     mustUnderstand ? "1" : "0",
                     SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    public void setMustUnderstand(String mustUnderstand) throws SOAPProcessingException {
        if (SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE.equals(mustUnderstand) ||
                SOAPConstants.ATTR_MUSTUNDERSTAND_FALSE.equals(mustUnderstand) ||
                SOAPConstants.ATTR_MUSTUNDERSTAND_0.equals(mustUnderstand) ||
                SOAPConstants.ATTR_MUSTUNDERSTAND_1.equals(mustUnderstand)) {
            setAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND,
                         mustUnderstand,
                         SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        } else {
            throw new SOAPProcessingException(
                    "mustUndertand should be one of \"true\", \"false\", \"0\" or \"1\" ");
        }
    }

    /**
     * Returns whether the mustUnderstand attribute for this <CODE>SOAPHeaderBlock</CODE> object is
     * turned on.
     *
     * @return <CODE>true</CODE> if the mustUnderstand attribute of this
     *         <CODE>SOAPHeaderBlock</CODE> object is turned on; <CODE>false</CODE> otherwise
     */
    public boolean getMustUnderstand() throws SOAPProcessingException {
        String mustUnderstand;
        if ((mustUnderstand =
                getAttribute(SOAPConstants.ATTR_MUSTUNDERSTAND,
                             SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
                != null) {
            if (SOAPConstants.ATTR_MUSTUNDERSTAND_TRUE.equals(mustUnderstand) ||
                    SOAPConstants.ATTR_MUSTUNDERSTAND_1.equals(mustUnderstand)) {
                return true;
            } else if (SOAPConstants.ATTR_MUSTUNDERSTAND_FALSE.equals(mustUnderstand) ||
                    SOAPConstants.ATTR_MUSTUNDERSTAND_0.equals(mustUnderstand)) {
                return false;
            } else {
                throw new SOAPProcessingException(
                        "Invalid value found in mustUnderstand value of " +
                                this.getLocalName() +
                                " header block");
            }
        }
        return false;

    }

    /**
     * What SOAP version is this HeaderBlock?
     *
     * @return a SOAPVersion, one of the two singletons.
     */
    public SOAPVersion getVersion() {
        return SOAP11Version.getSingleton();
    }

}
