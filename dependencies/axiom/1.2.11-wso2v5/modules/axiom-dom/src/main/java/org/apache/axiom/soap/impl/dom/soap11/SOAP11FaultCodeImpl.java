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

package org.apache.axiom.soap.impl.dom.soap11;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.serialize.StreamWriterToContentHandlerConverter;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.dom.SOAPFaultCodeImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SOAP11FaultCodeImpl extends SOAPFaultCodeImpl {
    /**
     * Constructor OMElementImpl
     *
     * @param parent
     * @param builder
     */
    public SOAP11FaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder,
                               SOAPFactory factory) {
        super(parent, builder, factory);
    }

    /** @param parent  */
    public SOAP11FaultCodeImpl(SOAPFault parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, false, factory);
    }


    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        if (!(subCode instanceof SOAP11FaultSubCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Sub " +
                            "Code. But received some other implementation");
        }
        super.setSubCode(subCode);
    }

    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        if (!(value instanceof SOAP11FaultValueImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault Value. " +
                            "But received some other implementation");
        }
        super.setValue(value);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP 1.1 implementation of SOAP Fault as the " +
                            "parent. But received some other implementation");
        }
    }

    public void internalSerialize(
            XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {

        // select the builder
        short builderType = PULL_TYPE_BUILDER;    // default is pull type
        if (builder != null) {
            builderType = this.builder.getBuilderType();
        }
        if ((builderType == PUSH_TYPE_BUILDER)
                && (builder.getRegisteredContentHandler() == null)) {
            builder.registerExternalContentHandler(
                    new StreamWriterToContentHandlerConverter(writer));
        }

        OMSerializerUtil.serializeStartpart(this,
                                            SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME,
                                            writer);

        writer.writeCharacters(this.getText());
        writer.writeEndElement();
    }

    public String getLocalName() {
        return SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME;
    }
}
