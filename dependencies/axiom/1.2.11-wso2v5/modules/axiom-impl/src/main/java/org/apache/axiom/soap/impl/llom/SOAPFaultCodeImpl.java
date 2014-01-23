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

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;

public abstract class SOAPFaultCodeImpl extends SOAPElement implements SOAPFaultCode {
    protected SOAPFaultCodeImpl(String localName, OMNamespace ns, SOAPFactory factory) {
        super(localName, ns, factory);
    }

    protected SOAPFaultCodeImpl(OMNamespace ns, SOAPFactory factory) {
        this(factory.getSOAPVersion().getFaultCodeQName().getLocalPart(), ns, factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent, String localName, OMXMLParserWrapper builder,
                             SOAPFactory factory) {
        super(parent, localName, builder, factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder,
                             SOAPFactory factory) {
        this(parent, factory.getSOAPVersion().getFaultCodeQName().getLocalPart(), builder,
             factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent,
                             String localName,
                             boolean extractNamespaceFromParent,
                             SOAPFactory factory) throws SOAPProcessingException {
        super(parent,
              localName,
              extractNamespaceFromParent, factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent,
                             boolean extractNamespaceFromParent,
                             SOAPFactory factory) throws SOAPProcessingException {
        this(parent,
             factory.getSOAPVersion().getFaultCodeQName().getLocalPart(),
             extractNamespaceFromParent, factory);
    }

    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, value, value);
    }

    public void setSubCode(SOAPFaultSubCode value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, getSubCode(), value);
    }

}
