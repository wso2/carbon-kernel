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

package org.apache.axiom.om.xpath;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AXIOMXPath extends BaseXPath {

    private static final long serialVersionUID = -5839161412925154639L;

    private Map namespaces = new HashMap();

    /**
     * Construct an XPath expression from a given string.
     *
     * @param xpathExpr the string representation of the XPath expression.
     * @throws JaxenException if there is a syntax error while parsing the expression
     */
    public AXIOMXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, new DocumentNavigator());
    }

    /**
     * Construct an XPath expression from a given string and initialize its
     * namespace context based on a given element.
     * 
     * @param element The element that determines the namespace context of the
     *                XPath expression. See {@link #addNamespaces(OMElement)}
     *                for more details.
     * @param xpathExpr the string representation of the XPath expression.
     * @throws JaxenException if there is a syntax error while parsing the expression
     *                        or if the namespace context could not be set up
     */
    public AXIOMXPath(OMElement element, String xpathExpr) throws JaxenException {
        this(xpathExpr);
        addNamespaces(element);
    }

    /**
     * Construct an XPath expression from a given attribute.
     * The string representation of the expression is taken from the attribute
     * value, while the attribute's owner element is used to determine the
     * namespace context of the expression. 
     * 
     * @param attribute the attribute to construct the expression from
     * @throws JaxenException if there is a syntax error while parsing the expression
     *                        or if the namespace context could not be set up
     */
    public AXIOMXPath(OMAttribute attribute) throws JaxenException {
        this(attribute.getOwner(), attribute.getAttributeValue());
    }

    /**
     * This override captures any added namespaces, as the Jaxen BaseXPath class nor
     * NamespaceContext (or SimpleNamespaceContext) exposes thier internal map of the prefixes to
     * the namespaces. This method - although is not the ideal solution to the issue, attempts to
     * provide an override to changing the Jaxen code.
     *
     * @param prefix a namespace prefix
     * @param uri    the URI to which the prefix matches
     * @throws JaxenException if the underlying implementation throws an exception
     */
    public void addNamespace(String prefix, String uri) throws JaxenException {
        try {
            super.addNamespace(prefix, uri);
        } catch (JaxenException e) {
            // the intention here is to prevent us caching a namespace, if the
            // underlying implementation does not accept it
            throw e;
        }
        namespaces.put(prefix, uri);
    }

    /**
     * Add the namespace declarations of a given {@link OMElement} to the namespace
     * context of an XPath expression. Typically this method is used with an XPath
     * expression appearing in an attribute of the given element.
     * <p>
     * Note that the default namespace is explicitly excluded and not added to the
     * namespace context. This makes the behaviour of this method consistent with
     * the rules followed in XSL stylesheets. Indeed, the XSLT specification defines
     * the namespace context of an XPath expression as follows:
     * <blockquote>
     * the set of namespace declarations are those in scope on the element which has the
     * attribute in which the expression occurs; [...] the default namespace
     * (as declared by xmlns) is not part of this set
     * </blockquote>
     * 
     * @param element the element to retrieve the namespace context from
     * @throws JaxenException if an error occurred when adding the namespace declarations
     */
    public void addNamespaces(OMElement element) throws JaxenException {
        OMElement current = element;
        // An element can redeclare a namespace prefix that has already been declared
        // by one of its ancestors. Since we visit the tree from child to parent, we
        // need to keep track of the prefixes we have already seen in order to avoid
        // adding namespace declarations that are overridden by a descendant of an element.
        Set seenPrefixes = new HashSet();
        while (true) {
            for (Iterator it = current.getAllDeclaredNamespaces(); it.hasNext(); ) {
                OMNamespace ns = (OMNamespace) it.next();
                if (ns != null) {
                    String prefix = ns.getPrefix();
                    // Exclude the default namespace as explained in the Javadoc above
                    if (prefix.length() != 0 && seenPrefixes.add(prefix)) {
                        addNamespace(ns.getPrefix(), ns.getNamespaceURI());
                    }
                }
            }
            OMContainer parent = current.getParent();
            if (parent == null || parent instanceof OMDocument) {
                break;
            } else {
                current = (OMElement)parent;
            }
        }
    }

    /**
     * Expose the prefix to namespace mapping for this expression
     *
     * @return a Map of namespace prefixes to the URIs
     */
    public Map getNamespaces() {
        return namespaces;
    }
}
