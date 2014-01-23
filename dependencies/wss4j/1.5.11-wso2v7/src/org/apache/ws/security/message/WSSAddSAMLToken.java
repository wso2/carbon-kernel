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
public class WSSAddSAMLToken extends WSBaseMessage {

    private static Log log = LogFactory.getLog(WSSAddSAMLToken.class.getName());

    /**
     * Constructor.
     * 
     * @deprecated replaced by {@link WSSecSAMLToken#WSSecSAMLToken()}
     */
    public WSSAddSAMLToken() {
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param actor the name of the actor of the <code>wsse:Security</code>
     *              header
     * @deprecated replaced by {@link WSSecSAMLToken#WSSecSAMLToken()}
     *             and {@link WSSecHeader} for actor specification.
     */
    public WSSAddSAMLToken(String actor) {
        super(actor);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param actor The name of the actor of the <code>wsse:Security</code>
     *              header
     * @param mu    Set <code>mustUnderstand</code> to true or false
     * 
     * @deprecated replaced by {@link WSSecSAMLToken#WSSecSAMLToken()}
     *             and {@link WSSecHeader} for actor and mustunderstand
     *             specification.
     */
    public WSSAddSAMLToken(String actor, boolean mu) {
        super(actor, mu);
    }

    /**
     * Adds a new <code>SAMLAssertion</code> to a soap envelope.
     * <p/>
     * A complete <code>SAMLAssertion</code> is added to the
     * <code>wsse:Security</code> header.
     *
     * @param doc      The SOAP enevlope as W3C document
     * @param assertion TODO
     * @return Document with UsernameToken added
     * @deprecated replaced by {@link WSSecSAMLToken#build(Document, SAMLAssertion, WSSecHeader)}
     */
    public Document build(Document doc, SAMLAssertion assertion) {
        log.debug("Begin add SAMLAssertion token...");
        try {
            Element element = (Element) assertion.toDOM(doc);
            Element securityHeader = insertSecurityHeader(doc);
            WSSecurityUtil.prependChildElement(securityHeader, element);
        } catch (SAMLException ex) {
            if (log.isDebugEnabled()) {
                log.debug(ex.getMessage(), ex);
            }
            throw new RuntimeException(ex.toString(), ex);
        }
        return doc;
    }
}
