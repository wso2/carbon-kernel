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

package org.apache.axis2.jaxws.message.util.impl;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Namespace information available at the current scope. Utility class for XMLStreamReaderFromDOM
 *
 * @see XMLStreamReaderFromDOM
 */
public class NamespaceContextFromDOM implements NamespaceContext {

    private Element element;

    /** @param element representing the current scope */
    NamespaceContextFromDOM(Element element) {
        this.element = element;
    }

    /* (non-Javadoc)
      * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
      */
    public String getPrefix(String namespaceURI) {
        return element.lookupPrefix(namespaceURI);
    }

    /* (non-Javadoc)
      * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
      */
    public Iterator getPrefixes(String namespaceURI) {
        if (element instanceof javax.xml.soap.SOAPElement) {
            Iterator it = ((javax.xml.soap.SOAPElement)element).getVisibleNamespacePrefixes();
            ArrayList list = new ArrayList();
            while (it.hasNext()) {
                String prefix = (String)it.next();
                if (getNamespaceURI(prefix).equals(namespaceURI)) {
                    if (prefix != null && prefix.length() == 0) {
                        prefix = null;
                    }
                    list.add(prefix);
                }
            }
            return list.iterator();
        } else {
            ArrayList list = new ArrayList();
            Node node = element;
            while (node != null) {
                if (node instanceof Element) {
                    // Walk the attributes looking for namespace declarations
                    NamedNodeMap attrs = ((Element)node).getAttributes();
                    for (int i = 0; i < attrs.getLength(); i++) {
                        Attr attr = (Attr)attrs.item(i);
                        if (attr.getNodeValue().equals(namespaceURI)) {
                            String name = attr.getNodeName();

                            if (name.startsWith("xmlns")) {
                                String prefix = "";
                                if (name.startsWith("xmlns:")) {
                                    prefix = name.substring(6);
                                }
                                // Found a namespace declaration with the prefix.
                                // Make sure this is not overridden by a declaration
                                // in a closer scope.
                                if (!list.contains(prefix) &&
                                        getNamespaceURI(prefix).equals(namespaceURI)) {
                                    list.add(prefix);
                                }
                            }
                        }
                    }
                }
                // Pop up to the parent node
                node = node.getParentNode();
            }
            return list.iterator();
        }
    }

    public String getNamespaceURI(String prefix) {
        return element.lookupNamespaceURI(prefix);
    }

}
