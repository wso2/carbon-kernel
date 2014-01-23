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

package org.apache.axiom.om.impl.dom;

import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class DOMImplementationImpl implements DOMImplementation {

    public boolean hasFeature(String feature, String version) {
        boolean anyVersion = version == null || version.isEmpty();
        return (feature.equalsIgnoreCase("Core") || feature.equalsIgnoreCase("XML"))
                && (anyVersion || version.equals("1.0") || version.equals("2.0"));
    }

    public Document createDocument(String namespaceURI, String qualifiedName,
                                   DocumentType doctype) throws DOMException {

        // TODO Handle docType stuff
        OMDOMFactory fac = new OMDOMFactory();
        DocumentImpl doc = new DocumentImpl(fac);
        fac.setDocument(doc);

        new ElementImpl(doc, DOMUtil.getLocalName(qualifiedName),
                        new NamespaceImpl(namespaceURI, DOMUtil
                                .getPrefix(qualifiedName)), fac);

        return doc;
    }

    public DocumentType createDocumentType(String qualifiedName,
                                           String publicId, String systemId) throws DOMException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /*
     * DOM-Level 3 methods
     */

    public Object getFeature(String arg0, String arg1) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

}
