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

package org.apache.axiom.c14n.impl;

import org.apache.axiom.c14n.exceptions.CanonicalizationException;
import org.apache.axiom.c14n.helpers.C14nHelper;
import org.apache.axiom.c14n.omwrapper.interfaces.Attr;
import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.c14n.omwrapper.interfaces.NamedNodeMap;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.c14n.utils.Constants;

import java.util.*;

/**
 * @author Christian Geuer-Pollmann <geuerp@apache.org>
 *
 * modified to work with Axiom wrapper by Saliya Ekanayake (esaliya@gmail.com)
 */
public abstract class Canonicalizer20010315 extends CanonicalizerBase {
    boolean firstCall = true;
    final SortedSet result = new TreeSet(COMPARE);
    static final String XMLNS_URI = Constants.NamespaceSpecNS;
    static final String XML_LANG_URI = Constants.XML_LANG_SPACE_SpecNS;

    static class XmlAttrStack {
        int currentLevel = 0;
        int lastlevel = 0;
        XmlsStackElement cur;

        static class XmlsStackElement {
            int level;
            boolean rendered = false;
            List nodes = new ArrayList();
        }

        ;
        List levels = new ArrayList();

        void push(int level) {
            currentLevel = level;
            if (currentLevel == -1)
                return;
            cur = null;
            while (lastlevel > currentLevel) {
                levels.remove(levels.size() - 1);
                if (levels.size() == 0) {
                    lastlevel = 0;
                    return;
                }
                lastlevel = ((XmlsStackElement) levels.get(levels.size() - 1)).level;
            }
        }

        void addXmlnsAttr(Attr n) {
            if (cur == null) {
                cur = new XmlsStackElement();
                cur.level = currentLevel;
                levels.add(cur);
                lastlevel = currentLevel;
            }
            cur.nodes.add(n);
        }

        void getXmlnsAttr(Collection col) {
            int size = levels.size() - 1;
            if (cur == null) {
                cur = new XmlsStackElement();
                cur.level = currentLevel;
                lastlevel = currentLevel;
                levels.add(cur);
            }
            boolean parentRendered = false;
            XmlsStackElement e = null;
            if (size == -1) {
                parentRendered = true;
            } else {
                e = (XmlsStackElement) levels.get(size);
                if (e.rendered && e.level + 1 == currentLevel)
                    parentRendered = true;

            }
            if (parentRendered) {
                col.addAll(cur.nodes);
                cur.rendered = true;
                return;
            }

            Map loa = new HashMap();
            for (; size >= 0; size--) {
                e = (XmlsStackElement) levels.get(size);
                Iterator it = e.nodes.iterator();
                while (it.hasNext()) {
                    Attr n = (Attr) it.next();
                    if (!loa.containsKey(n.getName()))
                        loa.put(n.getName(), n);
                }

            }
            ;
            cur.rendered = true;
            col.addAll(loa.values());
        }

    }

    XmlAttrStack xmlattrStack = new XmlAttrStack();

    /**
     * Constructor Canonicalizer20010315
     *
     * @param includeComments
     */
    public Canonicalizer20010315(boolean includeComments) {
        super(includeComments);
    }

    /**
     * @param E
     * @param ns
     * @return the Attr[]s to be outputted
     * @throws CanonicalizationException
     */
    Iterator handleAttributesSubtree(Element E, NameSpaceSymbTable ns)
            throws CanonicalizationException {
        if (!E.hasAttributes() && !firstCall) {
            return null;
        }
        // result will contain the attrs which have to be outputted
        final SortedSet result = this.result;
        result.clear();
        NamedNodeMap attrs = E.getAttributes();
        int attrsLength = attrs.getLength();

        for (int i = 0; i < attrsLength; i++) {
            Attr N = (Attr) attrs.item(i);
            String NUri = N.getNamespaceURI();

            if (XMLNS_URI != NUri) {
                //It's not a namespace attr node. Add to the result and continue.
                result.add(N);
                continue;
            }

            String NName = N.getLocalName();
            String NValue = N.getValue();
            if (XML.equals(NName)
                    && XML_LANG_URI.equals(NValue)) {
                //The default mapping for xml must not be output.
                continue;
            }

            Node n = ns.addMappingAndRender(NName, NValue, N);

            if (n != null) {
                //Render the ns definition
                result.add(n);
                if (C14nHelper.namespaceIsRelative(N)) {
                    Object exArgs[] = {E.getTagName(), NName, N.getNodeValue()};
                    throw new CanonicalizationException(
                            "c14n.Canonicalizer.RelativeNamespace", exArgs);
                }
            }
        }

        if (firstCall) {
            //It is the first node of the subtree
            //Obtain all the namespaces defined in the parents, and added to the output.
            ns.getUnrenderedNodes(result);
            //output the attributes in the xml namespace.
            xmlattrStack.getXmlnsAttr(result);
            firstCall = false;
        }

        return result.iterator();
    }

    /**
     * Always throws a CanonicalizationException because this is inclusive org.apache.axiom.c14n.impl.
     *
     * @param rootNode
     * @param inclusiveNamespaces
     * @return none it always fails
     * @throws org.apache.axiom.c14n.exceptions.CanonicalizationException
     *
     */
    public byte[] engineCanonicalizeSubTree(Node rootNode, String inclusiveNamespaces)
            throws CanonicalizationException {

        throw new CanonicalizationException("All Namespaces are Included by Default");
    }

    void handleParent(Element e, NameSpaceSymbTable ns) {
        if (!e.hasAttributes()) {
            return;
        }
        xmlattrStack.push(-1);
        NamedNodeMap attrs = e.getAttributes();
        int attrsLength = attrs.getLength();
        for (int i = 0; i < attrsLength; i++) {
            Attr N = (Attr) attrs.item(i);
            if (Constants.NamespaceSpecNS != N.getNamespaceURI()) {
                //Not a namespace definition, ignore.
                if (XML_LANG_URI == N.getNamespaceURI()) {
                    xmlattrStack.addXmlnsAttr(N);
                }
                continue;
            }

            String NName = N.getLocalName();
            String NValue = N.getNodeValue();
            if (XML.equals(NName)
                    && Constants.XML_LANG_SPACE_SpecNS.equals(NValue)) {
                continue;
            }
            ns.addMapping(NName, NValue, N);
        }
    }
}
