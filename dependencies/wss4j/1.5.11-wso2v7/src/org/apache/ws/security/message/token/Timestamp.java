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

package org.apache.ws.security.message.token;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.DOM2Writer;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Timestamp according to SOAP Message Security 1.0,
 * chapter 10 / appendix A.2
 *
 * @author Christof Soehngen (christof.soehngen@syracom.de)
 */
public class Timestamp {

    protected Element element = null;
    protected Vector customElements = null;
    protected Calendar created;
    protected Calendar expires;
    
    /**
     * Constructs a <code>Timestamp</code> object and parses the
     * <code>wsu:Timestamp</code> element to initialize it.
     *
     * @param element the <code>wsu:Timestamp</code> element that
     *                contains the timestamp data
     */
    public Timestamp(Element element) throws WSSecurityException {

        this.element = element;
        customElements = new Vector();

        String strCreated = null;
        String strExpires = null;

        for (Node currentChild = element.getFirstChild();
             currentChild != null;
             currentChild = currentChild.getNextSibling()
         ) {
            if (currentChild instanceof Element) {
                if (WSConstants.CREATED_LN.equals(currentChild.getLocalName()) &&
                        WSConstants.WSU_NS.equals(currentChild.getNamespaceURI())) {
                    if (strCreated == null) {
                        strCreated = ((Text) ((Element) currentChild).getFirstChild()).getData();
                    } else {
                        throw new WSSecurityException(
                            WSSecurityException.INVALID_SECURITY, "invalidTimestamp"
                        );
                    }
                } else if (WSConstants.EXPIRES_LN.equals(currentChild.getLocalName()) &&
                        WSConstants.WSU_NS.equals(currentChild.getNamespaceURI())) {
                    if (strExpires == null) {
                        strExpires = ((Text) ((Element) currentChild).getFirstChild()).getData();
                    } else {
                        throw new WSSecurityException(
                            WSSecurityException.INVALID_SECURITY, "invalidTimestamp"
                        );                        
                    }
                } else {
                    customElements.add((Element) currentChild);
                }
            }
        }

        DateFormat zulu = new XmlSchemaDateFormat();
        try {
            if (strCreated != null) {
                created = Calendar.getInstance();
                created.setTime(zulu.parse(strCreated));
            }
            if (strExpires != null) {
                expires = Calendar.getInstance();
                expires.setTime(zulu.parse(strExpires));
            }
        } catch (ParseException e) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY, "invalidTimestamp", null, e
            );
        }
    }


    /**
     * Constructs a <code>Timestamp</code> object according
     * to the defined parameters.
     *
     * @param doc the SOAP envelope as <code>Document</code>
     * @param ttl the time to live (validity of the security semantics) in seconds
     */
    public Timestamp(boolean milliseconds, Document doc, int ttl) {

        customElements = new Vector();
        element = 
            doc.createElementNS(
                WSConstants.WSU_NS, WSConstants.WSU_PREFIX + ":" + WSConstants.TIMESTAMP_TOKEN_LN
            );
        WSSecurityUtil.setNamespace(element, WSConstants.WSU_NS, WSConstants.WSU_PREFIX);

        DateFormat zulu = null;
        if (milliseconds) {
            zulu = new XmlSchemaDateFormat();
        } else {
            zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            zulu.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        created = getCurrentTime();

        Element elementCreated =
                doc.createElementNS(
                    WSConstants.WSU_NS, WSConstants.WSU_PREFIX + ":" + WSConstants.CREATED_LN
                );
        elementCreated.appendChild(doc.createTextNode(zulu.format(created.getTime())));
        element.appendChild(elementCreated);
        if (ttl != 0) {
            long currentTime = created.getTimeInMillis();
            currentTime += ttl * 1000;
            expires = getCurrentTime();
            expires.setTimeInMillis(currentTime);

            Element elementExpires =
                    doc.createElementNS(
                        WSConstants.WSU_NS, WSConstants.WSU_PREFIX + ":" + WSConstants.EXPIRES_LN
                    );
            elementExpires.appendChild(doc.createTextNode(zulu.format(expires.getTime())));
            element.appendChild(elementExpires);
        }
    }

    /**
     * Get the current time
     * 
     * @return calendar the current time
     */
    protected Calendar getCurrentTime() {
        return Calendar.getInstance();
    }
    
    /**
     * Returns the dom element of this <code>Timestamp</code> object.
     *
     * @return the <code>wsse:UsernameToken</code> element
     */
    public Element getElement() {
        return this.element;
    }

    /**
     * Returns the string representation of the token.
     *
     * @return a XML string representation
     */
    public String toString() {
        return DOM2Writer.nodeToString((Node) this.element);
    }

    /**
     * Get the time of creation.
     *
     * @return the "created" time
     */
    public Calendar getCreated() {
        return created;
    }

    /**
     * Get the time of expiration.
     *
     * @return the "expires" time
     */
    public Calendar getExpires() {
        return expires;
    }

    /**
     * Creates and adds a custom element to this Timestamp
     */
    public void addCustomElement(Document doc, Element customElement) {
        customElements.add(customElement);
        element.appendChild(customElement);
    }

    /**
     * Get the the custom elements from this Timestamp
     *
     * @return the vector containing the custom elements.
     */
    public Vector getCustomElements() {
        return this.customElements;
    }
    
    /**
     * Set wsu:Id attribute of this timestamp
     * @param id
     */
    public void setID(String id) {
        this.element.setAttributeNS(WSConstants.WSU_NS, WSConstants.WSU_PREFIX + ":Id", id);
    }
    
    /**
     * @return the value of the wsu:Id attribute
     */
    public String getID() {
        return this.element.getAttributeNS(WSConstants.WSU_NS, "Id");
    }
    
}
