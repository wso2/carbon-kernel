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

package org.apache.axiom.c14n.helpers;

import org.apache.axiom.c14n.exceptions.CanonicalizationException;
import org.apache.axiom.c14n.omwrapper.interfaces.Attr;
import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.c14n.omwrapper.interfaces.NamedNodeMap;

/**
 * @author Christian Geuer-Pollmann <geuerp@apache.org>
 *
 * modified to work with Axiom wrapper by Saliya Ekanayake (esaliya@gmail.com)
 */
public class C14nHelper {

    /**
     * Constructor C14nHelper
     */
    private C14nHelper() {

        // don't allow instantiation
    }

    /**
     * Method namespaceIsRelative
     *
     * @param namespace
     * @return true if the given namespace is relative.
     */
    public static boolean namespaceIsRelative(Attr namespace) {
        return !namespaceIsAbsolute(namespace);
    }

    /**
     * Method namespaceIsRelative
     *
     * @param namespaceValue
     * @return true if the given namespace is relative.
     */
    public static boolean namespaceIsRelative(String namespaceValue) {
        return !namespaceIsAbsolute(namespaceValue);
    }

    /**
     * Method namespaceIsAbsolute
     *
     * @param namespace
     * @return true if the given namespace is absolute.
     */
    public static boolean namespaceIsAbsolute(Attr namespace) {
        return namespaceIsAbsolute(namespace.getValue());
    }

    /**
     * Method namespaceIsAbsolute
     *
     * @param namespaceValue
     * @return true if the given namespace is absolute.
     */
    public static boolean namespaceIsAbsolute(String namespaceValue) {

        // assume empty namespaces are absolute
        if (namespaceValue.isEmpty()) {
            return true;
        }
        return namespaceValue.indexOf(':') > 0;
    }

    /**
     * This method throws an exception if the Attribute value contains
     * a relative URI.
     *
     * @param attr
     * @throws org.apache.axiom.c14n.exceptions.CanonicalizationException
     */
    public static void assertNotRelativeNS(Attr attr)
            throws CanonicalizationException {

        if (attr == null) {
            return;
        }

        String nodeAttrName = attr.getNodeName();
        boolean definesDefaultNS = nodeAttrName.equals("xmlns");
        boolean definesNonDefaultNS = nodeAttrName.startsWith("xmlns:");

        if (definesDefaultNS || definesNonDefaultNS) {
            if (namespaceIsRelative(attr)) {
                String parentName = attr.getOwnerElement().getTagName();
                String attrValue = attr.getValue();
                Object exArgs[] = {parentName, nodeAttrName, attrValue};

                throw new CanonicalizationException(
                        "c14n.Canonicalizer.RelativeNamespace", exArgs);
            }
        }
    }

    /**
     * This method throws a CanonicalizationException if the supplied Element
     * contains any relative namespaces.
     *
     * @param ctxNode
     * @throws CanonicalizationException
     * @see C14nHelper#assertNotRelativeNS(Attr)
     */
    public static void checkForRelativeNamespace(Element ctxNode)
            throws CanonicalizationException {

        if (ctxNode != null) {
            NamedNodeMap attributes = ctxNode.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                C14nHelper.assertNotRelativeNS((Attr) attributes.item(i));
            }
        } else {
            throw new CanonicalizationException(
                    "Called checkForRelativeNamespace() on null");
        }
    }
}

