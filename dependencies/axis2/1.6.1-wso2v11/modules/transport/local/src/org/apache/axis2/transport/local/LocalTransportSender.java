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


package org.apache.axis2.transport.local;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalTransportSender extends AbstractHandler implements TransportSender {
    protected static final Log log = LogFactory.getLog(LocalTransportSender.class);

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault {
    }

    public void stop() {
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
    }

    /**
     * Method invoke
     *
     * @param msgContext the current MessageContext
     * @throws AxisFault
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        // Check for the REST behaviour, if you desire rest beahaviour
        // put a <parameter name="doREST" value="true"/> at the axis2.xml
        msgContext.setDoingMTOM(TransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(TransportUtils.doWriteSwA(msgContext));

        OutputStream out;
        EndpointReference epr = msgContext.getTo();

        if (log.isDebugEnabled()) {
            log.debug("Sending - " + msgContext.getEnvelope().toString());
        }

        if (epr != null) {
            if (!epr.hasNoneAddress()) {
                out = new ByteArrayOutputStream();
                TransportUtils.writeMessage(msgContext, out);
                finalizeSendWithToAddress(msgContext, (ByteArrayOutputStream)out);
            }
        } else {
            out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);

            if (out != null) {
                TransportUtils.writeMessage(msgContext, out);
            } else {
                throw new AxisFault(
                        "Both the TO and Property MessageContext.TRANSPORT_OUT is Null, No where to send");
            }
        }

        TransportUtils.setResponseWritten(msgContext, true);
        
        return InvocationResponse.CONTINUE;
    }

    public void finalizeSendWithToAddress(MessageContext msgContext, ByteArrayOutputStream out)
            throws AxisFault {
        try {
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            ByteArrayOutputStream response = new ByteArrayOutputStream();

            LocalTransportReceiver localTransportReceiver = new LocalTransportReceiver(this, isNonBlocking());
            localTransportReceiver.processMessage(msgContext, in, response);

            in.close();
            out.close();
            if (response.size() > 0) {
                in = new ByteArrayInputStream(response.toByteArray());
                msgContext.setProperty(MessageContext.TRANSPORT_IN, in);
            }
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }

    protected boolean isNonBlocking() {
        if (log.isDebugEnabled()) {
            log.debug("Local Transport Sender Selected");
        }
        return false;
    }
}
