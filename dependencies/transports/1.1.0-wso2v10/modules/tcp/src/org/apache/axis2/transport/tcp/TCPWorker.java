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

package org.apache.axis2.transport.tcp;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * This Class is the work hoarse of the TCP request, this process the incomming SOAP Message.
 */
public class TCPWorker implements Runnable {

    private static final Log log = LogFactory.getLog(TCPWorker.class);

    private TCPEndpoint endpoint;
    private Socket socket;

    public TCPWorker(TCPEndpoint endpoint, Socket socket) {
        this.endpoint = endpoint;
        this.socket = socket;
    }

    public void run() {

        MessageContext msgContext = null;

        try {
            msgContext = endpoint.createMessageContext();
            msgContext.setIncomingTransportName(Constants.TRANSPORT_TCP);
            //msgContext.setTransportIn(endpoint.getListener().getTransportInDescription());

            TCPOutTransportInfo outInfo = new TCPOutTransportInfo();
            outInfo.setSocket(socket);
            outInfo.setContentType(endpoint.getContentType());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outInfo);

            // create the SOAP Envelope
            SOAPEnvelope envelope = TransportUtils.createSOAPMessage(msgContext,
                    socket.getInputStream(), endpoint.getContentType());
            msgContext.setEnvelope(envelope);

            AxisEngine.receive(msgContext);

        } catch (Exception e) {
            sendFault(msgContext, e);

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error while closing a TCP socket", e);
            }
        }
    }

    private void sendFault(MessageContext msgContext, Exception fault) {
        log.error("Error while processing TCP request through the Axis2 engine", fault);
        try {
            if (msgContext != null) {
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, socket.getOutputStream());

                MessageContext faultContext =
                        MessageContextBuilder.createFaultMessageContext(msgContext, fault);

                AxisEngine.sendFault(faultContext);
            }
        } catch (Exception e) {
            log.error("Error while sending the fault response", e);
        }
    }
}
