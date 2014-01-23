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

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;

import javax.xml.stream.XMLStreamWriter;

public abstract class SOAPElement extends OMElementImpl {

    /**
     * @param parent
     * @param localName
     * @param extractNamespaceFromParent
     */
    protected SOAPElement(OMElement parent,
                          String localName,
                          boolean extractNamespaceFromParent,
                          SOAPFactory factory) throws SOAPProcessingException {
        super(localName, null, parent, factory);
        if (parent == null) {
            throw new SOAPProcessingException(
                    " Can not create " + localName +
                            " element without a parent !!");
        }
        checkParent(parent);

        if (extractNamespaceFromParent) {
            this.ns = parent.getNamespace();
        }
    }


    protected SOAPElement(OMElement parent,
                          String localName,
                          OMXMLParserWrapper builder,
                          SOAPFactory factory) {
        super(localName, null, parent, builder, factory);
    }

    /**
     * @param localName
     * @param ns
     */
    protected SOAPElement(String localName, OMNamespace ns,
                          SOAPFactory factory) {
        super(localName, ns, factory);
    }

    /** This has to be implemented by all the derived classes to check for the correct parent. */
    protected abstract void checkParent(OMElement parent) throws SOAPProcessingException;

    public void setParent(OMContainer element) {
        super.setParent(element);

        if (element instanceof OMElement) {
            checkParent((OMElement) element);
        }
    }

    /**
     * Utility method to register a content handler for 
     * push type builders.
     * @param writer
     * @return PULL_TYPE_BUILDER or PUSH_TYPE_BUILDER
     */
    protected short registerContentHandler(XMLStreamWriter writer) {
        //  select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(
                    new StreamWriterToContentHandlerConverter(writer));
        }
        return builderType;
    }

}
