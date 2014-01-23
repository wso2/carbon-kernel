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
 * WSDataRef stores information about decrypted/signed elements
 * 
 * When a processor decrypts an elements it stores information 
 * about that element in a WSDataRef so these information can 
 * be used for validation stages 
 * 
 */

import javax.xml.namespace.QName;
import org.w3c.dom.Element;

public class WSDataRef {
    
    /**
     * The protected DOM element
     */
    private Element protectedElement;
    
    /**
     * reference by which the Encrypted Data was referred 
     */
    private String dataref;
    
    /**
     * wsu:Id of the decrypted element (if present)
     */
    private String wsuId;
    
    /**
     * QName of the decrypted element
     */
    private QName name;
    
    /**
     * An xpath expression pointing to the data element
     */
    private String xpath;
    
    /**
     * Algorithm used to encrypt/sign the element
     */
    private String algorithm;
    
    /**
     * If this reference represents signed content, this field
     * represents the digest algorithm applied to the content.
     */
    private String digestAlgorithm;
    
    private boolean content;
    
    
    /**
     * @param dataref reference by which the Encrypted Data was referred 
     */
    public WSDataRef(String dataref) {
        this.dataref = dataref;
    }
    
    /**
     * @param dataref reference by which the Encrypted Data was referred 
     * @param wsuId Id of the decrypted element (if present)
     */
    public WSDataRef(String dataref, String wsuId) {
        this.dataref = dataref;
        this.wsuId = wsuId;
    }
    
    /**
     * @param dataref reference by which the Encrypted Data was referred 
     * @param wsuId Id of the decrypted element (if present)
     * @param name QName of the decrypted element
     */
    public WSDataRef(String dataref, String wsuId, QName name) {
        this.dataref = dataref;
        this.wsuId = wsuId;
        this.name = name;
    }

    /**
     * @return the data reference 
     */
    public String getDataref() {
        return dataref;
    }

    /**
     * @param dataref reference by which the Encrypted Data was referred 
     */
    public void setDataref(String dataref) {
        this.dataref = dataref;
    }

    /**
     * @return Id of the decrypted element (if present)
     */
    public String getWsuId() {
        return wsuId;
    }

    /**
     * @param wsuId Id of the decrypted element (if present)
     */
    public void setWsuId(String wsuId) {
        this.wsuId = wsuId;
    }

    /**
     * @return QName of the decrypted element
     */
    public QName getName() {
        return name;
    }

    /**
     * @param name QName of the decrypted element
     */
    public void setName(QName name) {
        this.name = name;
    }
    
    /**
     * @param element The protected DOM element to set
     */
    public void setProtectedElement(Element element) {
        protectedElement = element;
        String prefix = element.getPrefix();
        if (prefix == null) {
            name = 
                new QName(
                    element.getNamespaceURI(), element.getLocalName()
                );
        } else {
            name = 
                new QName(
                    element.getNamespaceURI(), element.getLocalName(), prefix
                );
        }
    }
     
    /**
     * @return the protected DOM element
     */
    public Element getProtectedElement() {
        return protectedElement;
    }

    /**
     * @return the xpath
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * @param xpath the xpath to set
     */
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    /**
     * @return the content
     */
    public boolean isContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(boolean content) {
        this.content = content;
    }
    
    /**
     * @return the algorithm used for encryption/signature
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @param algo algorithm used for encryption
     */
    public void setAlgorithm(String algo) {
        algorithm = algo;
    }

    /**
     * @return if this reference represents signed content, 
     * the digest algorithm applied to the content.
     */
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    /**
     * @param digestAlgorithm if this reference represents 
     * signed content, the digest algorithm applied to the content.
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }
}
