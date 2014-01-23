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
import org.apache.ws.security.util.WSSecurityUtil;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds a WS SAML Assertion and inserts it into the SOAP Envelope. Refer to
 * the WS specification, SAML Token profile
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class WSSecSAMLToken extends WSSecBase {

    private static Log log = LogFactory.getLog(WSSecSAMLToken.class.getName());
    
    private Document document = null;
    
    private SAMLAssertion saml = null;

    /**
     * Constructor.
     */
    public WSSecSAMLToken() {
    }

    /**
     * Creates a SAML token.
     * 
     * The method prepares and initializes a WSSec UsernameToken structure after
     * the relevant information was set. A Before calling
     * <code>prepare()</code> all parameters such as user, password,
     * passwordType etc. must be set. A complete <code>UsernameToken</code> is
     * constructed.
     * 
     * @param doc
     *            The SOAP envelope as W3C document
     */
    public void prepare(Document doc, SAMLAssertion assertion) {
        document = doc;
        saml = assertion;
    }

    /**
     * Prepends the SAML Assertion to the elements already in the
     * Security header.
     * 
     * The method can be called any time after <code>prepare()</code>.
     * This allows to insert the SAML assertion at any position in the
     * Security header.
     * 
     * @param secHeader
     *            The security header that holds the Signature element.
     */
    public void prependToHeader(WSSecHeader secHeader) {
        Element element = null;        
        try {
            element = (Element) saml.toDOM(document);
        } catch (SAMLException ex) {
            throw new RuntimeException(ex.toString(), ex);
        }
        
        WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), element);
    }
    
    /**
     * Get the id generated during <code>prepare()</code>.
     * 
     * Returns the the value of wsu:Id attribute of this Timestamp. 
     * 
     * @return Return the wsu:Id of this token or null if <code>prepareToken()</code>
     * was not called before.
     */
    public String getId() {
        if (saml == null) {
            return null;
        }
        return saml.getId();
    }
    
    /**
     * Adds a new <code>SAMLAssertion</code> to a soap envelope.
     * <p/>
     * A complete <code>SAMLAssertion</code> is added to the
     * <code>wsse:Security</code> header.
     *
     * @param doc      The SOAP envelope as W3C document
     * @param assertion TODO
     * @return Document with UsernameToken added
     */
    public Document build(Document doc, SAMLAssertion assertion, WSSecHeader secHeader) {
        log.debug("Begin add SAMLAssertion token...");
        
        prepare(doc, assertion);
        prependToHeader(secHeader);
        
        return doc;
    }
}
