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

package org.apache.axis2.description;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import java.io.InputStream;

public class RobustOutOnlyAxisOperation extends OutInAxisOperation {

    public RobustOutOnlyAxisOperation() {
        super();
        //setup a temporary name
        QName tmpName = new QName(this.getClass().getName() + "_" + UIDGenerator.generateUID());
        this.setName(tmpName);
        setMessageExchangePattern(WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY);
    }

    public RobustOutOnlyAxisOperation(QName name) {
        super(name);
        setMessageExchangePattern(WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY);
    }

    public OperationClient createClient(ServiceContext sc, Options options) {
        return new RobustOutOnlyOperationClient(this, sc, options);
    }

    class RobustOutOnlyOperationClient extends OutInAxisOperationClient {

        public RobustOutOnlyOperationClient(OutInAxisOperation axisOp, ServiceContext sc,
                                            Options options) {
            super(axisOp, sc, options);
        }


        protected void timeOut() throws AxisFault {
           //Nothing to worry
        }


        /**
         * If there is a fault then need to handle that
         * @param responseMessageContext responseMessageContext
         * @throws AxisFault
         */
        protected void handleResponse(MessageContext responseMessageContext) throws AxisFault {
            SOAPEnvelope envelope = responseMessageContext.getEnvelope();
            if (envelope == null) {
                // If request is REST we assume the responseMessageContext is REST, so
                // set the variable
                InputStream inStream = (InputStream) responseMessageContext.
                        getProperty(MessageContext.TRANSPORT_IN);
                if (inStream != null && checkContentLength(responseMessageContext)) {
                    envelope = TransportUtils.createSOAPMessage(
                            responseMessageContext);
                    responseMessageContext.setEnvelope(envelope);
                }
                responseMessageContext.setEnvelope(envelope);
            }
            if (envelope != null) {
                if (envelope.hasFault()|| responseMessageContext.isProcessingFault()) {
                    //receiving a fault
                    AxisEngine.receive(responseMessageContext);
                    throw Utils.getInboundFaultFromMessageContext(responseMessageContext);
                }
            }
        }

    }
}
