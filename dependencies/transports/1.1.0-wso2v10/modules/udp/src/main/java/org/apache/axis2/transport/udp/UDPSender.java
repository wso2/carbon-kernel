/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.udp;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Transport sender for the UDP protocol.
 * 
 * @see org.apache.axis2.transport.udp
 */
public class UDPSender extends AbstractTransportSender {
    public UDPSender() {
        log = LogFactory.getLog(UDPSender.class);
    }
    
    @Override
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut)
            throws AxisFault {
        super.init(cfgCtx, transportOut);
    }
    
    @Override
    public void sendMessage(MessageContext msgContext, String targetEPR,
                            OutTransportInfo outTransportInfo) throws AxisFault {
        UDPOutTransportInfo udpOutInfo;
        if ((targetEPR == null) && (outTransportInfo != null)) {
            // this can happen only at the server side and send the message using back chanel
            udpOutInfo = (UDPOutTransportInfo) outTransportInfo;
        } else {
            udpOutInfo = new UDPOutTransportInfo(targetEPR);
        }
        MessageFormatter messageFormatter = MessageProcessorSelector.getMessageFormatter(msgContext);
        OMOutputFormat format = BaseUtils.getOMOutputFormat(msgContext);
        format.setContentType(udpOutInfo.getContentType());
        byte[] payload = messageFormatter.getBytes(msgContext, format);
        try {
            DatagramSocket socket = new DatagramSocket();
            if (log.isDebugEnabled()) {
                log.debug("Sending " + payload.length + " bytes to " + udpOutInfo.getAddress());
            }
            try {
                socket.send(new DatagramPacket(payload, payload.length, udpOutInfo.getAddress()));
                if (!msgContext.getOptions().isUseSeparateListener() &&
                        !msgContext.isServerSide()){
                    waitForReply(msgContext, socket, udpOutInfo.getContentType());
                }
            }
            finally {
                socket.close();
            }
        }
        catch (IOException ex) {
            throw new AxisFault("Unable to send packet", ex);
        }
    }

    private void waitForReply(MessageContext messageContext, DatagramSocket datagramSocket,
                              String contentType) throws IOException {

        // piggy back message constant is used to pass a piggy back
        // message context in asnych model
        if (!(messageContext.getAxisOperation() instanceof OutInAxisOperation) &&
                messageContext.getProperty(org.apache.axis2.Constants.PIGGYBACK_MESSAGE) == null) {
            return;
        }

        byte[] inputBuffer = new byte[4096]; //TODO set the maximum size parameter
        DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
        datagramSocket.receive(packet);

        // create the soap envelope
        try {
            MessageContext respMessageContext = messageContext.getOperationContext().
                    getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
            InputStream inputStream = new ByteArrayInputStream(inputBuffer, 0, packet.getLength());
            SOAPEnvelope envelope = TransportUtils.createSOAPMessage(respMessageContext,
                    inputStream, contentType);
            respMessageContext.setEnvelope(envelope);
        } catch (XMLStreamException e) {
            throw new AxisFault("Can not build the soap message ", e);
        }
    }
}
