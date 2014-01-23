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

package org.tempuri.elementQualifier;

import org.custommonkey.xmlunit.ElementQualifier;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This interface implementation determines whether two elements should be compared
 * when comparing WSDL files.  The nodes are intended for comparison when they are 
 * defined by the same namespace URI, have the same tag name (without namespace), 
 * and have the same name attribute.
 */
public class WSDLElementQualifier implements ElementQualifier {
    
    /**
     * Determine whether two elements are comparable
     * @param control an Element from the control XML NodeList
     * @param test an Element from the test XML NodeList
     * @return true if the elements are comparable, otherwise return false.  Two 
     *  elements should be compared if they have the same namespace URI, the same
     *  tag name (with namespace not present) and the same name attribute.
     */
    public boolean qualifyForComparison(Element control, Element test) {
        return control != null 
            && test != null 
            && sameNamespaceURI(control, test)
            && sameTagName(control, test)
            && sameNameAttribute(control, test);
    }
    
    /**
     * Determine whether two nodes are associated with the same namespace URI 
     * @param control an Element from the control XML NodeList
     * @param test an Element from the test XML NodeList
     * @return true if the two nodes are associated with the same namespace URI,
     *  otherwise return false
     */
    protected boolean sameNamespaceURI(Node control, Node test) {
        String controlNS = control.getNamespaceURI();
        String testNS = test.getNamespaceURI();
        
        if (controlNS == null) {
            return testNS == null;
        }
        
        return controlNS.equals(testNS);
    }
    
    /**
     * Determine whether two nodes have the same tag name once any 
     * namespace information is removed
     * @param control an Element from the control XML NodeList
     * @param test an Element from the test XML NodeList
     * @return true if the two nodes have the same tag name not including 
     *  namespace information (if present), otherwise return false
     */
    protected boolean sameTagName(Node control, Node test) {
    	return getTagWithoutNamespace(control).equals(getTagWithoutNamespace(test));
    }
    
    /**
     * Remove any namespace information from a tag name
     * @param node an Element from an XML NodeList
     * @return the localName if the node includes namespace information, 
     *  otherwise return the nodeName
     */
    protected String getTagWithoutNamespace(Node node) {
        String name = node.getLocalName();
        
        if (name == null) {
            return node.getNodeName();
        }
        
        return name;
    } 
    
    /**
     * Determine whether two nodes have the same name attribute 
     * @param control an Element from the control XML NodeList
     * @param test an Element from the test XML NodeList
     * @return true if the two nodes have the same name attribute 
     *  (with the absence of a name attribute considered a specific
     *  name attribute), otherwise return false
     */
    protected boolean sameNameAttribute(Node control, Node test) {
    	return getNameAttribute(control).equals(getNameAttribute(test));
    }
    
    /**
     * Obtain the name attribute for the node
     * @param node an Element for which the Name attribute is sought
     * @return the name attribute for the node if the "name" attribute
     *  exists, otherwise return ""
     */
    protected String getNameAttribute(Node node) {
        NamedNodeMap nnMap = node.getAttributes();
        
        if (nnMap.getLength() == 0) {
            return "";
        }
        
        Node nameAttrNode = nnMap.getNamedItem("name");
        if (nameAttrNode == null) {
            return "";
        }
        
        return nameAttrNode.getNodeValue();
    } 

}
