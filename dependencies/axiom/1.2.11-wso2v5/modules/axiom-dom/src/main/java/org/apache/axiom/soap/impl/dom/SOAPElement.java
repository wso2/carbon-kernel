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

package org.apache.axiom.soap.impl.dom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NamespaceImpl;
import org.apache.axiom.om.impl.dom.ParentNode;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;

public abstract class SOAPElement extends ElementImpl {

    public SOAPElement(SOAPFactory factory) {
        super(factory);
    }

    /** @param parent  */
    protected SOAPElement(OMElement parent,
                          String localName,
                          boolean extractNamespaceFromParent,
                          SOAPFactory factory) throws SOAPProcessingException {
        super((ParentNode) parent, localName, null, factory);
        if (parent == null) {
            throw new SOAPProcessingException(
                    " Can not create " + localName +
                            " element without a parent !!");
        }
        checkParent(parent);

        if (extractNamespaceFromParent) {
            this.namespace = parent.getNamespace();
        }
        this.localName = localName;
    }


    protected SOAPElement(OMElement parent,
                          String localName,
                          OMXMLParserWrapper builder,
                          SOAPFactory factory) {
        super((ParentNode) parent, localName, null, builder, factory);
    }

    protected SOAPElement(DocumentImpl doc, String localName, OMNamespace ns,
                          SOAPFactory factory) {
        super(doc, localName, (NamespaceImpl) ns, factory);
    }

    protected SOAPElement(DocumentImpl ownerDocument, String tagName,
                          NamespaceImpl ns, OMXMLParserWrapper builder, SOAPFactory factory) {
        super(ownerDocument, tagName, ns, builder, factory);
    }

    /** This has to be implemented by all the derived classes to check for the correct parent. */
    protected abstract void checkParent(OMElement parent)
            throws SOAPProcessingException;

}
