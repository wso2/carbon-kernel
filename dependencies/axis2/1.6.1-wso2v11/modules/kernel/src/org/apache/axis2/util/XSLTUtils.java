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

package org.apache.axis2.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XSLTUtils {

    public static Document getDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        return documentBuilderFactory.newDocumentBuilder().newDocument();
    }

    /**
     * Utility method to add an attribute to a given element
     *
     * @param document
     * @param AttribName
     * @param attribValue
     * @param element
     */
    public static void addAttribute(Document document,
                                    String AttribName,
                                    String attribValue,
                                    Element element) {
        Attr attribute = document.createAttribute(AttribName);
        attribute.setValue(attribValue);
        element.setAttributeNode(attribute);
    }

    public static Element getElement(Document document,
                                     String elementName) {
        return document.createElement(elementName);

    }


    /**
     * Utility method to add an attribute to a given element
     *
     * @param document
     * @param elementName
     * @param parentNode
     */
    public static Element addChildElement(Document document,
                                          String elementName,
                                          Node parentNode) {
        Element elt = document.createElement(elementName);
        parentNode.appendChild(elt);
        return elt;
    }

}
