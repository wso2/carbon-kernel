/*
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

package org.apache.axiom.om.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Helper class to provide the functionality of the digest value generation. This is an
 * implementation of the DOMHASH algorithm on OM.
 */
public class DigestGenerator {

    /**
     * This method is an overloaded method for the digest generation for OMDocument
     *
     * @param document
     * @param digestAlgorithm
     * @return Returns a byte array representing the calculated digest
     */
    public byte[] getDigest(OMDocument document, String digestAlgorithm) throws OMException {
        byte[] digest = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(9);
            Collection childNodes = getValidElements(document);
            dos.writeInt(childNodes.size());
            Iterator itr = childNodes.iterator();
            while (itr.hasNext()) {
                OMNode node = (OMNode) itr.next();
                if (node.getType() == OMNode.PI_NODE)
                    dos.write(getDigest((OMProcessingInstruction) node, digestAlgorithm));
                else if (
                        node.getType() == OMNode.ELEMENT_NODE)
                    dos.write(getDigest((OMElement) node, digestAlgorithm));
            }
            dos.close();
            md.update(baos.toByteArray());
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new OMException(e);
        } catch (IOException e) {
            throw new OMException(e);
        }
        return digest;
    }

    /**
     * This method is an overloaded method for the digest generation for OMNode
     *
     * @param node
     * @param digestAlgorithm
     * @return Returns a byte array representing the calculated digest value
     */
    public byte[] getDigest(OMNode node, String digestAlgorithm) {
        if (node.getType() == OMNode.ELEMENT_NODE)
            return getDigest((OMElement) node, digestAlgorithm);
        else if (
                node.getType() == OMNode.TEXT_NODE)
            return getDigest((OMText) node, digestAlgorithm);
        else if (node.getType() == OMNode.PI_NODE)
            return getDigest((OMProcessingInstruction) node, digestAlgorithm);
        else return new byte[0];
    }

    /**
     * This method is an overloaded method for the digest generation for OMElement
     *
     * @param element
     * @param digestAlgorithm
     * @return Returns a byte array representing the calculated digest value
     */
    public byte[] getDigest(OMElement element, String digestAlgorithm) throws OMException {
        byte[] digest = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(1);
            dos.write(getExpandedName(element).getBytes("UnicodeBigUnmarked"));
            dos.write((byte) 0);
            dos.write((byte) 0);
            Collection attrs = getAttributesWithoutNS(element);
            dos.writeInt(attrs.size());
            Iterator itr = attrs.iterator();
            while (itr.hasNext())
                dos.write(getDigest((OMAttribute) itr.next(), digestAlgorithm));
            OMNode node = element.getFirstOMChild();
            // adjoining Texts are merged,
            // there is  no 0-length Text, and
            // comment nodes are removed.
            int length = 0;
            itr = element.getChildElements();
            while (itr.hasNext()) {
                length++;
                itr.next();
            }
            dos.writeInt(length);
            while (node != null) {
                dos.write(getDigest(node, digestAlgorithm));
                node = node.getNextOMSibling();
            }
            dos.close();
            md.update(baos.toByteArray());
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new OMException(e);
        } catch (IOException e) {
            throw new OMException(e);
        }
        return digest;
    }

    /**
     * This method is an overloaded method for the digest generation for OMProcessingInstruction
     *
     * @param pi
     * @param digestAlgorithm
     * @return Returns a byte array representing the calculated digest value
     */
    public byte[] getDigest(OMProcessingInstruction pi, String digestAlgorithm) throws OMException {
        byte[] digest = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            md.update((byte) 0);
            md.update((byte) 0);
            md.update((byte) 0);
            md.update((byte) 7);
            md.update(pi.getTarget().getBytes("UnicodeBigUnmarked"));
            md.update((byte) 0);
            md.update((byte) 0);
            md.update(pi.getValue().getBytes("UnicodeBigUnmarked"));
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new OMException(e);
        } catch (UnsupportedEncodingException e) {
            throw new OMException(e);
        }
        return digest;
    }

    /**
     * This method is an overloaded method for the digest generation for OMAttribute
     *
     * @param attribute
     * @param digestAlgorithm
     * @return Returns a byte array representing the calculated digest value
     */
    public byte[] getDigest(OMAttribute attribute, String digestAlgorithm) throws OMException {
        byte[] digest = new byte[0];
        if (!(attribute.getLocalName().equals("xmlns") ||
                attribute.getLocalName().startsWith("xmlns:"))) try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            md.update((byte) 0);
            md.update((byte) 0);
            md.update((byte) 0);
            md.update((byte) 2);
            md.update(getExpandedName(attribute).getBytes("UnicodeBigUnmarked"));
            md.update((byte) 0);
            md.update((byte) 0);
            md.update(attribute.getAttributeValue().getBytes("UnicodeBigUnmarked"));
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new OMException(e);
        } catch (UnsupportedEncodingException e) {
            throw new OMException(e);
        }
        return digest;
    }

    /**
     * This method is an overloaded method for the digest generation for OMText
     *
     * @param text
     * @param digestAlgorithm
     * @return Returns a byte array representing the calculated digest value
     */
    public byte[] getDigest(OMText text, String digestAlgorithm) throws OMException {
        byte[] digest = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            md.update((byte) 0);
            md.update((byte) 0);
            md.update((byte) 0);
            md.update((byte) 3);
            md.update(text.getText().getBytes("UnicodeBigUnmarked"));
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new OMException(e);
        } catch (UnsupportedEncodingException e) {
            throw new OMException(e);
        }
        return digest;
    }

    /**
     * This method is an overloaded method for getting the expanded name namespaceURI followed by
     * the local name for OMElement
     *
     * @param element
     * @return Returns the expanded name of OMElement
     */
    public String getExpandedName(OMElement element) {
        return element.getNamespace().getNamespaceURI() + ":" + element.getLocalName();
    }

    /**
     * This method is an overloaded method for getting the expanded name namespaceURI followed by
     * the local name for OMAttribute
     *
     * @param attribute
     * @return Returns the expanded name of the OMAttribute
     */
    public String getExpandedName(OMAttribute attribute) {
        return attribute.getNamespace().getNamespaceURI() + ":" + attribute.getLocalName();
    }

    /**
     * Gets the collection of attributes which are none namespace declarations for an OMElement
     *
     * @param element
     * @return Returns the collection of attributes which are none namespace declarations
     */
    public Collection getAttributesWithoutNS(OMElement element) {
        SortedMap map = new TreeMap();
        Iterator itr = element.getAllAttributes();
        while (itr.hasNext()) {
            OMAttribute attribute = (OMAttribute) itr.next();
            if (!(attribute.getLocalName().equals("xmlns") ||
                    attribute.getLocalName().startsWith("xmlns:")))
                map.put(getExpandedName(attribute), attribute);
        }
        return map.values();
    }

    /**
     * Gets the valid element collection of an OMDocument. OMElement and OMProcessingInstruction
     * only
     *
     * @param document
     * @return Returns a collection of OMProcessingInstructions and OMElements
     */
    public Collection getValidElements(OMDocument document) {
        ArrayList list = new ArrayList();
        Iterator itr = document.getChildren();
        while (itr.hasNext()) {
            OMNode node = (OMNode) itr.next();
            if (node.getType() == OMNode.ELEMENT_NODE || node.getType() == OMNode.PI_NODE)
                list.add(node);
        }
        return list;
    }

    /**
     * Gets the String representation of the byte array
     *
     * @param array
     * @return Returns the String of the byte
     */
    public String getStringRepresentation(byte[] array) {
        StringBuilder str = new StringBuilder("");
        for (byte anArray : array) str.append(anArray);
        return str.toString();
    }

    /**
     * Compares two OMNodes for the XML equality
     *
     * @param node
     * @param comparingNode
     * @param digestAlgorithm
     * @return Returns true if the OMNode XML contents are equal
     */
    public boolean compareOMNode(OMNode node, OMNode comparingNode, String digestAlgorithm) {
        return Arrays.equals(getDigest(node, digestAlgorithm),
                             getDigest(comparingNode, digestAlgorithm));
    }

    /**
     * Compares two OMDocuments for the XML equality
     *
     * @param document
     * @param comparingDocument
     * @param digestAlgorithm
     * @return Returns true if the OMDocument XML content are equal
     */
    public boolean compareOMDocument(OMDocument document, OMDocument comparingDocument,
                                     String digestAlgorithm) {
        return Arrays.equals(getDigest(document, digestAlgorithm),
                             getDigest(comparingDocument, digestAlgorithm));
    }

    /**
     * Compares two OMAttributes for the XML equality
     *
     * @param attribute
     * @param comparingAttribute
     * @param digestAlgorithm
     * @return Returns true if the OMDocument XML content are equal
     */
    public boolean compareOMAttribute(OMAttribute attribute, OMAttribute comparingAttribute,
                                      String digestAlgorithm) {
        return Arrays.equals(getDigest(attribute, digestAlgorithm),
                             getDigest(comparingAttribute, digestAlgorithm));
    }

    /** String representing the MD5 digest algorithm */
    public static final String md5DigestAlgorithm = "MD5";

    /** String representing the SHA digest algorithm */
    public static final String shaDigestAlgorithm = "SHA";

    /** String representing the SHA1 digest algorithm */
    public static final String sha1DigestAlgorithm = "SHA1";
}
