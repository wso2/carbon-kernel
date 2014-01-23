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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author Christian Geuer-Pollmann <geuerp@apache.org>
 *
 * modified to work with Axiom wrapper by Saliya Ekanayake (esaliya@gmail.com)
 */
public abstract class Canonicalizer20010315Excl extends CanonicalizerBase {
    /**
     * This Set contains the names (Strings like "xmlns" or "xmlns:foo") of
     * the inclusive namespaces.
     */
    TreeSet _inclusiveNSSet = new TreeSet();
    static final String XMLNS_URI = Constants.NamespaceSpecNS;
    final SortedSet result = new TreeSet(COMPARE);

    /**
     * Constructor Canonicalizer20010315Excl
     *
     * @param includeComments
     */
    public Canonicalizer20010315Excl(boolean includeComments) {
        super(includeComments);
    }

    /**
     * Method engineCanonicalizeSubTree
     *
     * @param rootNode
     * @throws org.apache.axiom.c14n.exceptions.CanonicalizationException
     *
     * @inheritDoc
     */
    public byte[] engineCanonicalizeSubTree(Node rootNode)
            throws CanonicalizationException {
        return this.engineCanonicalizeSubTree(rootNode, "", null);
    }

    /**
     * Method engineCanonicalizeSubTree
     *
     * @param rootNode
     * @param inclusiveNamespaces
     * @throws CanonicalizationException
     * @inheritDoc
     */
    public byte[] engineCanonicalizeSubTree(Node rootNode,
                                            String inclusiveNamespaces) throws CanonicalizationException {
        return this.engineCanonicalizeSubTree(rootNode, inclusiveNamespaces, null);
    }

    /**
     * Method engineCanonicalizeSubTree
     *
     * @param rootNode
     * @param inclusiveNamespaces
     * @param excl                A element to exclude from the org.apache.axiom.c14n.impl process.
     * @return the rootNode org.apache.axiom.c14n.impl.
     * @throws CanonicalizationException
     */
    public byte[] engineCanonicalizeSubTree(Node rootNode,
                                            String inclusiveNamespaces, Node excl) throws CanonicalizationException {
        this._inclusiveNSSet = (TreeSet) prefixStr2Set(inclusiveNamespaces);
        return super.engineCanonicalizeSubTree(rootNode, excl);
    }



    /**
     * Method handleAttributesSubtree
     *
     * @param E
     * @throws CanonicalizationException
     * @inheritDoc
     */
    Iterator handleAttributesSubtree(Element E, NameSpaceSymbTable ns)
            throws CanonicalizationException {
        SortedSet result = this.result;
        result.clear();
        NamedNodeMap attrs = null;

        int attrsLength = 0;
        if (E.hasAttributes()) {
            attrs = E.getAttributes();
            attrsLength = attrs.getLength();
        }
        //The prefix visibly utilized(in the attribute or in the name) in the element
        SortedSet visiblyUtilized = (SortedSet) _inclusiveNSSet.clone();

        for (int i = 0; i < attrsLength; i++) {
            Attr N = (Attr) attrs.item(i);

            if (XMLNS_URI != N.getNamespaceURI()) {
                //Not a namespace definition.
                //The Element is output element, add his prefix(if used) to visibyUtilized
                String prefix = N.getPrefix();
                if ((prefix != null) && (!prefix.equals(XML) && !prefix.equals(XMLNS))) {
                    visiblyUtilized.add(prefix);
                }
                //Add to the result.
                result.add(N);
                continue;
            }
            String NName = N.getLocalName();
            String NNodeValue = N.getNodeValue();

            if (ns.addMapping(NName, NNodeValue, N)) {
                //New definition check if it is relative.
                if (C14nHelper.namespaceIsRelative(NNodeValue)) {
                    Object exArgs[] = {E.getTagName(), NName,
                            N.getNodeValue()};
                    throw new CanonicalizationException(
                            "c14n.Canonicalizer.RelativeNamespace", exArgs);
                }
            }
        }
        String prefix;
        if (E.getNamespaceURI() != null) {
            prefix = E.getPrefix();
            if ((prefix == null) || (prefix.isEmpty())) {
                prefix = XMLNS;
            }

        } else {
            prefix = XMLNS;
        }
        visiblyUtilized.add(prefix);

        Iterator it = visiblyUtilized.iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            Attr key = ns.getMapping(s);
            if (key == null) {
                continue;
            }
            result.add(key);
        }

        return result.iterator();
    }

    /**
     * Decodes the <code>inclusiveNamespaces</code> String and returns all
     * selected namespace prefixes as a Set. The <code>#default</code>
     * namespace token is represented as an empty namespace prefix
     * (<code>"xmlns"</code>).
     * <BR/>
     * The String <code>inclusiveNamespaces=" xenc    ds #default"</code>
     * is returned as a Set containing the following Strings:
     * <UL>
     * <LI><code>xmlns</code></LI>
     * <LI><code>xenc</code></LI>
     * <LI><code>ds</code></LI>
     * </UL>
     *
     * @param inclusiveNamespaces
     * @return A set to string
     */
    public static SortedSet prefixStr2Set(String inclusiveNamespaces) {

        SortedSet prefixes = new TreeSet();

        if ((inclusiveNamespaces == null)
                || (inclusiveNamespaces.isEmpty())) {
            return prefixes;
        }

        StringTokenizer st = new StringTokenizer(inclusiveNamespaces, " \t\r\n");

        while (st.hasMoreTokens()) {
            String prefix = st.nextToken();

            if (prefix.equals("#default")) {
                prefixes.add("xmlns");
            } else {
                prefixes.add(prefix);
            }
        }
        return prefixes;
    }

}
