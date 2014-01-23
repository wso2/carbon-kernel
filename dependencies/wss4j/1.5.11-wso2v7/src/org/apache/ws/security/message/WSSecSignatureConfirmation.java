/**
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

package org.apache.ws.security.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.message.token.SignatureConfirmation;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds a WS SignatureConfirmation and inserts it into the SOAP Envelope.
 * 
 * @author Werner Dittmann (Werner.Dittmann@t-online.de).
 */

public class WSSecSignatureConfirmation extends WSSecBase {
    private static Log log = LogFactory.getLog(WSSecSignatureConfirmation.class
            .getName());

    private SignatureConfirmation sc = null;

    byte[] signatureValue = null;

    /**
     * Constructor.
     */
    public WSSecSignatureConfirmation() {
    }

    /**
     * Set the Signature value to store in this SignatureConfirmation.
     * 
     * @param signatureValue The Signature value to store in the SignatureConfirmation element
     */
    public void setSignatureValue(byte[] signatureValue) {
        this.signatureValue = signatureValue;
    }


    /**
     * Creates a SignatureConfimation element.
     * 
     * The method prepares and initializes a WSSec SignatureConfirmation structure after
     * the relevant information was set. Before calling <code>prepare()</code> the
     * filed <code>signatureValue</code> must be set
     * 
     * @param doc The SOAP envelope as W3C document
     */
    public void prepare(Document doc) {
        sc = new SignatureConfirmation(doc, signatureValue);
        sc.setID(wssConfig.getIdAllocator().createId("SigConf-", sc));
    }
    
    /**
     * Prepends the SignatureConfirmation element to the elements already in the
     * Security header.
     * 
     * The method can be called any time after <code>prepare()</code>.
     * This allows to insert the SignatureConfirmation element at any position in the
     * Security header.
     * 
     * @param secHeader The security header that holds the Signature element.
     */
    public void prependToHeader(WSSecHeader secHeader) {
        WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), sc.getElement());
    }
    
    /**
     * Adds a new <code>SignatureConfirmation</code> to a soap envelope.
     * 
     * A complete <code>SignatureConfirmation</code> is constructed and added
     * to the <code>wsse:Security</code> header.
     * 
     * @param doc The SOAP envelope as W3C document
     * @param sigVal the Signature value. This will be the content of the "Value" attribute.
     * @param secHeader The security header that holds the Signature element.
     * @return Document with SignatureConfirmation added
     */
    public Document build(Document doc, byte[] sigVal, WSSecHeader secHeader) {
        log.debug("Begin add signature confirmation...");
        
        signatureValue = sigVal;
        prepare(doc);
        prependToHeader(secHeader);
        
        return doc;
    }

    /**
     * Get the id generated during <code>prepare()</code>.
     * 
     * Returns the the value of wsu:Id attribute of this SignatureConfirmation. 
     * 
     * @return Return the wsu:Id of this token or null if <code>prepareToken()</code>
     * was not called before.
     */
    public String getId() {
        if (sc == null) {
            return null;
        }
        return sc.getID();
    }
    
    /**
     * Get the SignatureConfirmation element generated during 
     * <code>prepare()</code>.
     * 
     * @return Return the SignatureConfirmation element or null if <code>prepare()</code>
     * was not called before.
     */
    public Element getSignatureConfirmationElement() {
        return (this.sc != null) ? this.sc.getElement() : null;
    }
}
