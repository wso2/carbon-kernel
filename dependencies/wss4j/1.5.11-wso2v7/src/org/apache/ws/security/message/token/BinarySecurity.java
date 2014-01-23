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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.DOM2Writer;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;

/**
 * Binary Security Token.
 * 
 * @author Davanum Srinivas (dims@yahoo.com).
 * @author Werner Dittmann (Werner.Dittmann@t-onile.de).
 */
public class BinarySecurity {
    public static final QName TOKEN_BST = new QName(WSConstants.WSSE_NS, "BinarySecurityToken");
    public static final QName TOKEN_KI = new QName(WSConstants.WSSE_NS, "KeyIdentifier");
    public static final String BASE64_ENCODING = WSConstants.SOAPMESSAGE_NS + "#Base64Binary";
    private static final Log LOG = LogFactory.getLog(BinarySecurity.class.getName());
    protected Element element = null;

    /**
     * Constructor.
     * 
     * @param elem 
     * @throws WSSecurityException 
     */
    public BinarySecurity(Element elem) throws WSSecurityException {
        this.element = elem;
        QName el = new QName(this.element.getNamespaceURI(), this.element.getLocalName());
        if (!el.equals(TOKEN_BST) && !el.equals(TOKEN_KI)) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN, 
                "badTokenType",
                new Object[] {el}
            );
        }
        String encoding = getEncodingType();
        //
        // if the Element is a BinarySecurityToken then
        //     encoding may be null -> default is Base64
        //     if encoding is not null and not empty it must be Base64
        // else
        //     this is a keyidentifier element
        //     must contain an encoding attribute which must be Base64
        //     in this case
        //
        if (el.equals(TOKEN_BST)) {
            if (encoding != null && encoding.length() > 0 && !encoding.equals(BASE64_ENCODING)) {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY_TOKEN,
                    "badEncoding", 
                    new Object[] {encoding}
                );
            }
        } else if (el.equals(TOKEN_KI) && !BASE64_ENCODING.equals(encoding)) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN,
                "badEncoding", 
                new Object[] {encoding}
            );
        }
    }

    /**
     * Constructor.
     * 
     * @param doc 
     */
    public BinarySecurity(Document doc) {
        this.element = doc.createElementNS(WSConstants.WSSE_NS, "wsse:BinarySecurityToken");
        WSSecurityUtil.setNamespace(this.element, WSConstants.WSSE_NS, WSConstants.WSSE_PREFIX);
        setEncodingType(BASE64_ENCODING);
        this.element.appendChild(doc.createTextNode(""));
    }

    /**
     * get the value type.
     * 
     * @return TODO
     */
    public String getValueType() {
        return this.element.getAttribute("ValueType");
    }

    /**
     * set the value type.
     * 
     * @param type 
     */
    public void setValueType(String type) {
        this.element.setAttributeNS(null, "ValueType", type);
    }

    /**
     * get the encoding type.
     * 
     * @return TODO
     */
    public String getEncodingType() {
        return this.element.getAttribute("EncodingType");
    }

    /**
     * set the encoding type.
     * 
     * @param encoding 
     */
    public void setEncodingType(String encoding) {
        this.element.setAttributeNS(null, "EncodingType", encoding);
    }

    /**
     * get the byte array containing token information.
     * 
     * @return the byte array containing token information
     */
    public byte[] getToken() {
        Node node = element.getFirstChild();
        StringBuffer buffer = new StringBuffer();
        while (node != null) {
            if (Node.TEXT_NODE == node.getNodeType()) {
                buffer.append(((Text)node).getData());
            }
            node = node.getNextSibling();
        }
                
        try {
            return Base64.decode(buffer.toString());
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(ex.getMessage(), ex);
            }
            return null;
        }
    }

    /**
     * set the token information.
     * 
     * @param data 
     */
    public void setToken(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        Text node = getFirstNode();
        node.setData(Base64.encode(data));
    }

    /**
     * return the first text node.
     * 
     * @return TODO
     */
    protected Text getFirstNode() {
        Node node = this.element.getFirstChild();
        return ((node != null) && node instanceof Text) ? (Text) node : null;
    }

    /**
     * return the dom element.
     * 
     * @return TODO
     */
    public Element getElement() {
        return this.element;
    }

    /**
     * get the id.
     * 
     * @return TODO
     */
    public String getID() {
        return this.element.getAttributeNS(WSConstants.WSU_NS, "Id");
    }

    /**
     * set the id.
     * 
     * @param id 
     */
    public void setID(String id) {
        String prefix = 
            WSSecurityUtil.setNamespace(this.element, WSConstants.WSU_NS, WSConstants.WSU_PREFIX);
        this.element.setAttributeNS(WSConstants.WSU_NS, prefix + ":Id", id);
    }

    /**
     * return the string representation of the token.
     * 
     * @return TODO
     */
    public String toString() {
        return DOM2Writer.nodeToString((Node) this.element);
    }
}
