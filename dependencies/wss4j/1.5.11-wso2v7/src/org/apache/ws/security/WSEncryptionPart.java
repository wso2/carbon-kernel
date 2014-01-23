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
 * @author Werner Dittmann (Werner.Dittmann@siemens.com)
 */
public class WSEncryptionPart {

    private String name;
    private String namespace;
    private String encModifier;
    private String encId;
    private String id;
    
    /**
     * An xpath expression pointing to the data element
     * that may be specified in case the encryption part is of type
     * <code>org.apache.ws.security.WSConstants.PART_TYPE_ELEMENT</code>
     */
    private String xpath;
    
    /**
     * Types of WSEncryptionPart
     * <code>org.apache.ws.security.WSConstants.PART_TYPE_HEADER</code>
     * <code>org.apache.ws.security.WSConstants.PART_TYPE_BODY</code>
     * <code>org.apache.ws.security.WSConstants.PART_TYPE_ELEMENT</code>
     */
    private int type = -1;

    /**
     * Constructor to initialize part structure with element, namespace, and modifier.
     * 
     * This constructor initializes the parts structure to lookup for a
     * fully qualified name of an element to encrypt or sign. The modifier
     * controls how encryption encrypts the element, signature processing does
     * not use the modifier information.
     * 
     * <p/>
     * 
     * Regarding the modifier ("Content" or "Element") refer to the W3C
     * XML Encryption specification. 
     * 
     * @param nm Element's name
     * @param nmspace Element's namespace
     * @param encMod The encryption modifier
     */
    public WSEncryptionPart(String nm, String nmspace, String encMod) {
        name = nm;
        namespace = nmspace;
        encModifier = encMod;
        id = null;
    }
    
    /**
     * Constructor to initialize part structure with element, namespace, and modifier,type.
     * 
     * This constructor initializes the parts structure to lookup for a
     * fully qualified name of an element to encrypt or sign. The modifier
     * controls how encryption encrypts the element, signature processing does
     * not use the modifier information.
     * 
     * <p/>
     * 
     * Regarding the modifier ("Content" or "Element") refer to the W3C
     * XML Encryption specification. 
     * 
     * @param nm Element's name
     * @param nmspace Element's namespace
     * @param encMod The encryption modifier
     * @param type Type of the WSEncryptionPart
     */
    public WSEncryptionPart(String nm, String nmspace, String encMod, int type) {
        name = nm;
        namespace = nmspace;
        encModifier = encMod;
        this.type = type;
        id = null;
    }

    /**
     * Constructor to initialize part structure with element id.
     * 
     * This constructor initializes the parts structure to lookup for a
     * an element with the given Id to encrypt or sign. 
     * 
     * @param id The Id to of the element to process
     */
    public WSEncryptionPart(String id) {
        this.id = id;
        name = namespace = encModifier = null;
    }
    
    /**
     * Constructor to initialize part structure with element id and modifier.
     * 
     * This constructor initializes the parts structure to lookup for a
     * an element with the given Id to encrypt or sign. The modifier
     * controls how encryption encrypts the element, signature processing does
     * not use the modifier information.
     * 
     * <p/>
     * 
     * Regarding the modifier ("Content" or "Element") refer to the W3C
     * XML Encryption specification. 
     * 
     * @param id The Id to of the element to process
     * @param encMod The encryption modifier
     */
    public WSEncryptionPart(String id, String encMod) {
        this.id = id;
        encModifier = encMod;
        name = namespace = null;
    }
    
    /**
     * Constructor to initialize part structure with element id, modifier and type.
     * 
     * This constructor initializes the parts structure to lookup for a
     * an element with the given Id to encrypt or sign. The modifier
     * controls how encryption encrypts the element, signature processing does
     * not use the modifier information. 
     * 
     * <p/>
     * 
     * Regarding the modifier ("Content" or "Element") refer to the W3C
     * XML Encryption specification. 
     * 
     * @param id The Id to of the element to process
     * @param encMod The encryption modifier
     * @param type of the element
     */
    public WSEncryptionPart(String id, String encMod,int type) {
        this.id = id;
        encModifier = encMod;
        this.type = type;
        name = namespace = null;
    }

    /**
     * @return the local name of the element to encrypt.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the namespace of the element to encrypt
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the encryption modifier
     */
    public String getEncModifier() {
        return encModifier;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }
    
    public void setEncId (String id) {
        encId = id;
    }
    
    public String getEncId() {
        return encId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public void setName(String name){
        this.name = name;
    }
}
