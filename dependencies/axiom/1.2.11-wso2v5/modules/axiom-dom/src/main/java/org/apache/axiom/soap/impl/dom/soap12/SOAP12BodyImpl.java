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

package org.apache.axiom.soap.impl.dom.soap12;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.dom.SOAPBodyImpl;

public class SOAP12BodyImpl extends SOAPBodyImpl {
    /** @param envelope  */
    public SOAP12BodyImpl(SOAPEnvelope envelope, SOAPFactory factory)
            throws SOAPProcessingException {
        super(envelope, factory);
    }

    /**
     * Constructor SOAPBodyImpl
     *
     * @param envelope
     * @param builder
     */
    public SOAP12BodyImpl(SOAPEnvelope envelope, OMXMLParserWrapper builder,
                          SOAPFactory factory) {
        super(envelope, builder, factory);
    }

    public SOAPFault addFault(Exception e) throws OMException {
        SOAPFault soapFault = new SOAP12FaultImpl(this, e,
                                                  (SOAPFactory) this.factory);
        this.hasSOAPFault = true;
        return soapFault;
    }
}
