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
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds a WS UsernameToken and inserts it into the SOAP Envelope.
 * Refer to the WS specification, UsernameToken profile
 *
 * @author Werner Dittmann (Werner.Dittmann@siemens.com).
 */

public class WSSAddUsernameToken extends WSBaseMessage {
    private static Log log = LogFactory.getLog(WSSAddUsernameToken.class.getName());
    private String passwordType = WSConstants.PASSWORD_DIGEST;

    private UsernameToken ut = null;
    private String id = null;
    
    /**
     * Constructor.
     * 
     * @deprecated replaced by {@link WSSecUsernameToken#WSSecUsernameToken()}
     */
    public WSSAddUsernameToken() {
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param actor the name of the actor of the <code>wsse:Security</code> header
     * @deprecated replaced by {@link WSSecUsernameToken#WSSecUsernameToken()}
     *             and {@link WSSecHeader} for actor specification.
     */
    public WSSAddUsernameToken(String actor) {
        super(actor);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param actor The name of the actor of the <code>wsse:Security</code> header
     * @param mu    Set <code>mustUnderstand</code> to true or false
     * @deprecated replaced by {@link WSSecUsernameToken#WSSecUsernameToken()}
     *             and {@link WSSecHeader} for actor and mustunderstand
     *             specification.
     */
    public WSSAddUsernameToken(String actor, boolean mu) {
        super(actor, mu);
    }

    /**
     * Defines how to construct the password element of the
     * <code>UsernameToken</code>.
     *
     * @param pwType contains the password type. Only allowed values are
     *               {@link WSConstants#PASSWORD_DIGEST} and
     *               {@link WSConstants#PASSWORD_TEXT}.
     * @deprecated replaced by {@link WSSecUsernameToken#setPasswordType(String)}
     */
    public void setPasswordType(String pwType) {
        if (pwType == null) {
            passwordType = WSConstants.PASSWORD_DIGEST;
        } else if (pwType.equals(WSConstants.PASSWORD_DIGEST) || pwType.equals(WSConstants.PASSWORD_TEXT)) {
            passwordType = pwType;
        }
    }

    /**
     * Creates and adds a Nonce element to the UsernameToken.
     * @deprecated replaced by {@link WSSecUsernameToken#addNonce()}
     */
    public void addNonce(Document doc) {
        ut.addNonce(doc);
    }

    /**
     * Creates and adds a Created element to the UsernameToken.
     * @deprecated replaced by {@link WSSecUsernameToken#addCreated()}
     */
    public void addCreated(Document doc) {
        ut.addCreated(wssConfig.isPrecisionInMilliSeconds(), doc);
    }

    /**
     * set the id
     * @param id
     * @deprecated no replacement, id is created by default in
     *             {@link WSSecUsernameToken}
     */ 
    public void setId(String id) {
        this.id = id;
        if (ut != null)
            ut.setID(id);
    }

    /**
     * Get a secret key derived from values in UsernameToken.
     * @return a secret key
     * @deprecated replaced by {@link WSSecUsernameToken#getSecretKey()}
     */
    public byte[] getSecretKey() {
        return ut.getSecretKey();
    }
    /**
     * get the id
     * @return The id
     * @deprecated replaced by {@link WSSecUsernameToken#getId()}
     */ 
    public String getId() {
        return id;
    }

    public Document preSetUsernameToken(Document doc, String username, String password) {
        ut = new UsernameToken(wssConfig.isPrecisionInMilliSeconds(), doc, passwordType);
        ut.setName(username);
        ut.setPassword(password);
        return doc;
    }
    /**
     * Adds a new <code>UsernameToken</code> to a soap envelope.
     * <p/>
     * A complete <code>UsernameToken</code> is constructed and added to
     * the <code>wsse:Security</code> header.
     *
     * @param doc      The SOAP envelope as W3C document
     * @param username The username to set in the UsernameToken
     * @param password The password of the user
     * @return Document with UsernameToken added
     * @deprecated replaced by
     *             {@link WSSecUsernameToken#build(Document, WSSecHeader)} and
     *             {@link WSSecBase#setUserInfo(String, String)}
     */
    public Document build(Document doc, String username, String password) { // throws Exception {
        log.debug("Begin add username token...");
        Element securityHeader = insertSecurityHeader(doc);
        if (ut == null) {
            preSetUsernameToken(doc, username, password);
        }
        if (id != null)
            ut.setID(id);
        WSSecurityUtil.prependChildElement(securityHeader, ut.getElement());
        return doc;
    }

}

