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

package org.apache.ws.security;

/**
 * WSDocInfo holds information about the document to process. Together
 * with the WSDocInfoStore it provides a method to store and access document
 * information about BinarySecurityToken, used Crypto, and others.
 * </p>
 * Using the Document's hash a caller can identify a document and get
 * the stored information that me be necessary to process the document.
 * The main usage for this is (are) the transformation functions that
 * are called during Signature/Verification process. 
 *
 * @author Werner Dittmann (Werner.Dittmann@siemens.com)
 *
 */

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.processor.Processor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class WSDocInfo {
    Document doc = null;
    Crypto crypto = null;
    Vector bst = null;
    Element assertion = null;
    Vector processors = null;
    List securityTokenReferences = null;

    public WSDocInfo(Document doc) {
        //
        // This is a bit of a hack. When the Document is a SAAJ SOAPPart instance, it may
        // be that the "owner" document of any child elements is an internal Document, rather
        // than the SOAPPart. This is the case for the SUN SAAJ implementation.
        // This causes problems with STRTransform, as:
        // WSDocInfoStore.lookup(transformObject.getDocument())
        // will not work. 
        //
        this.doc = doc.getDocumentElement().getOwnerDocument();
    }

    /**
     * Set a SecurityTokenReference element.
     */
    public void setSecurityTokenReference(Element securityTokenRef) {
        if (securityTokenReferences == null) {
            securityTokenReferences = new Vector();
        }
        //to avoid concurrent modification exception under high load
        securityTokenReferences.add(securityTokenRef);
    }

    /**
     * Get a SecurityTokenReference for the given (wsu) Id
     *
     * @param uri is the relative uri (starts with #) of the id
     * @return the STR element or null if nothing found
     */
    public Element getSecurityTokenReference(String uri) {
        if (securityTokenReferences != null) {
            //to avoid concurrent modification exception under high load
            synchronized (securityTokenReferences) {
                for (Iterator iter = securityTokenReferences.iterator(); iter.hasNext();) {
                    Element elem = (Element) iter.next();
                    String cId = elem.getAttributeNS(WSConstants.WSU_NS, "Id");
                    if (uri.equals(cId)) {
                        return elem;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Clears the info data except the hash code
     */
    public void clear() {
        crypto = null;
        assertion = null;
        if (bst != null && bst.size() > 0) {
            bst.removeAllElements();
        }
        if (processors != null && processors.size() > 0) {
            processors.removeAllElements();
        }

        bst = null;
        processors = null;
    }

    /**
     * Get a BinarySecurityToken for the given Id
     *
     * @param uri is the relative uri (starts with #) of the id
     * @return the BST element or null if nothing found
     */
    public Element getBst(String uri) {
        String id = uri.substring(1);

        if (bst != null) {
            for (Enumeration e = bst.elements(); e.hasMoreElements();) {
                Element elem = (Element) e.nextElement();
                String cId = elem.getAttributeNS(WSConstants.WSU_NS, "Id");
                if (id.equals(cId)) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * Get a Processor for the given Id
     *
     * @param id is the Id to look for
     * @return the Security processor identified with this Id or null if nothing found
     */
    public Processor getProcessor(String id) {

        if (id == null) {
            return null;
        }

        Processor p = null;
        if (processors != null) {
            for (Enumeration e = processors.elements(); e.hasMoreElements();) {
                p = (Processor) e.nextElement();
                String cId = p.getId();
                if (id.equals(cId)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Store a Processor for later access.
     *
     * @param p is the Processor to store
     */
    public void setProcessor(Processor p) {
        if (processors == null) {
            processors = new Vector();
        }
        processors.add(p);
    }

    /**
     * @return the signature crypto class used to process
     *         the signature/verify
     */
    public Crypto getCrypto() {
        return crypto;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * @param elem is the BinarySecurityToken to store
     */
    public void setBst(Element elem) {
        if (bst == null) {
            bst = new Vector();
        }
        bst.add(elem);
    }

    /**
     * @param crypto is the signature crypto class used to
     *               process signature/verify
     */
    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    /**
     * @return Returns the assertion.
     */
    public Element getAssertion() {
        return assertion;
    }

    /**
     * @param assertion The assertion to set.
     */
    public void setAssertion(Element assertion) {
        this.assertion = assertion;
    }
}
