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
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds a WS Timestamp and inserts it into the SOAP Envelope. Refer to the WS
 * specification 1.0. chapter 10 / appendix A.2
 * 
 * @author Christof Soehngen (Christof.Soehngen@syracom.de).
 * @author Werner Dittmann (werner@apache.org).
 */

public class WSSecTimestamp extends WSSecBase {
    private static Log log = LogFactory.getLog(WSSecTimestamp.class.getName());

    private Timestamp ts = null;

    private int timeToLive = 300; // time between Created and Expires

    /**
     * Constructor.
     */
    public WSSecTimestamp() {
    }

    /**
     * Set the time to live. This is the time difference in seconds between the
     * <code>Created</code> and the <code>Expires</code> in
     * <code>Timestamp</code>. <p/>
     * 
     * @param ttl The time to live in second
     */
    public void setTimeToLive(int ttl) {
        timeToLive = ttl;
    }

    /**
     * Creates a Timestamp element.
     * 
     * The method prepares and initializes a WSSec Timestamp structure after the
     * relevant information was set. Before calling <code>prepare()</code> the
     * parameter such as <code>timeToLive</code> can be set if the default
     * value is not suitable.
     * 
     * @param doc The SOAP envelope as W3C document
     */
    public void prepare(Document doc) {
        ts = new Timestamp(wssConfig.isPrecisionInMilliSeconds(), doc, timeToLive);
        String tsId = wssConfig.getIdAllocator().createId("Timestamp-", ts);
        ts.setID(tsId);
    }

    /**
     * Prepends the Timestamp element to the elements already in the Security
     * header.
     * 
     * The method can be called any time after <code>prepare()</code>. This
     * allows to insert the Timestamp element at any position in the Security
     * header.
     * 
     * @param secHeader The security header that holds the Signature element.
     */
    public void prependToHeader(WSSecHeader secHeader) {
        WSSecurityUtil.prependChildElement(secHeader.getSecurityHeader(), ts.getElement());
    }

    /**
     * Adds a new <code>Timestamp</code> to a soap envelope.
     * 
     * A complete <code>Timestamp</code> is constructed and added to the
     * <code>wsse:Security</code> header.
     * 
     * @param doc The SOAP envelope as W3C document
     * @param secHeader The security header that hold this Timestamp
     * @return Document with Timestamp added
     * @throws Exception
     */
    public Document build(Document doc, WSSecHeader secHeader) {
        log.debug("Begin add timestamp...");

        prepare(doc);
        prependToHeader(secHeader);

        return doc;
    }

    /**
     * Get the id generated during <code>prepare()</code>.
     * 
     * Returns the the value of wsu:Id attribute of this Timestamp.
     * 
     * @return Return the wsu:Id of this token or null if
     *         <code>prepareToken()</code> was not called before.
     */
    public String getId() {
        if (ts == null) {
            return null;
        }
        return ts.getID();
    }
    
    /**
     * Get the timestamp element generated during <code>prepare()</code>.
     */
    public Element getElement() {
        if (ts == null) {
            return null;
        }
        return ts.getElement();
    }
}
