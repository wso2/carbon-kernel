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

package org.apache.axiom.soap.impl.llom.soap11;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.llom.SOAPFaultCodeImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SOAP11FaultCodeImpl extends SOAPFaultCodeImpl {

    public SOAP11FaultCodeImpl(SOAPFactory factory) {
        super(SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME, null, factory);
    }

    public SOAP11FaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder, SOAPFactory factory) {
        super(parent, SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME, builder, factory);
    }

    public SOAP11FaultCodeImpl(SOAPFault parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME, false, factory);
    }


    public void setSubCode(SOAPFaultSubCode subCode) throws SOAPProcessingException {
        if (!(subCode instanceof SOAP11FaultSubCodeImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultSubCodeImpl, got " + subCode.getClass());
        }
        super.setSubCode(subCode);
    }

    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        if (!(value instanceof SOAP11FaultValueImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultValueImpl, got " + value.getClass());
        }
        super.setValue(value);
    }

    protected void checkParent(OMElement parent) throws SOAPProcessingException {
        if (!(parent instanceof SOAP11FaultImpl)) {
            throw new SOAPProcessingException(
                    "Expecting SOAP11FaultImpl, got " + parent.getClass());
        }
    }

    public void internalSerialize(XMLStreamWriter writer, boolean cache)
            throws XMLStreamException {
        this.registerContentHandler(writer);

        OMSerializerUtil.serializeStartpart(this,
                                            SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME,
                                            writer);

        writer.writeCharacters(this.getText());
        writer.writeEndElement();
    }

    public String getLocalName() {
        return SOAP11Constants.SOAP_FAULT_CODE_LOCAL_NAME;
    }

    public SOAPFaultValue getValue() {
        return null;
//        throw new UnsupportedOperationException("getValue() not supported for SOAP 1.1 faults");
    }

    public SOAPFaultSubCode getSubCode() {
        return null;
//        throw new UnsupportedOperationException("getSubCode() not supported for SOAP 1.1 faults");
    }
}
