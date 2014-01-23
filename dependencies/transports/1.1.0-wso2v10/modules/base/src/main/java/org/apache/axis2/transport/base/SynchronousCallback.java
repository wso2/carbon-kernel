/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.transport.base;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.wsdl.WSDLConstants;


public class SynchronousCallback {

    private MessageContext outMessageContext;
    private MessageContext inMessageContext;

    private boolean isComplete;

    public SynchronousCallback(MessageContext outMessageContext) {
        this.outMessageContext = outMessageContext;
        this.isComplete = false;
    }

    public synchronized void setInMessageContext(MessageContext inMessageContext) throws AxisFault {

        // if some other thread has access and complete then return without doing any thing.
        // thread should have activate by the first message.
        if (!isComplete) {
            // this code is invoked only if the code use with axis2 at the client side
            // when axis2 client receive messages it waits in the sending thread until the response comes.
            // so this thread only notify the waiting thread and hence we need to build the message here.
            inMessageContext.getEnvelope().build();
            OperationContext operationContext = outMessageContext.getOperationContext();
            MessageContext msgCtx =
                    operationContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            if (msgCtx == null) {
                // try to see whether there is a piggy back message context
                if (outMessageContext.getProperty(org.apache.axis2.Constants.PIGGYBACK_MESSAGE) != null) {

                    msgCtx = (MessageContext) outMessageContext.getProperty(org.apache.axis2.Constants.PIGGYBACK_MESSAGE);
                    msgCtx.setTransportIn(inMessageContext.getTransportIn());
                    msgCtx.setTransportOut(inMessageContext.getTransportOut());
                    msgCtx.setServerSide(false);
                    msgCtx.setProperty(BaseConstants.MAIL_CONTENT_TYPE,
                            inMessageContext.getProperty(BaseConstants.MAIL_CONTENT_TYPE));
                    // FIXME: this class must not be transport dependent since it is used by AbstractTransportListener
                    msgCtx.setIncomingTransportName(org.apache.axis2.Constants.TRANSPORT_MAIL);
                    msgCtx.setEnvelope(inMessageContext.getEnvelope());

                } else {
                    inMessageContext.setOperationContext(operationContext);
                    inMessageContext.setServiceContext(outMessageContext.getServiceContext());
                    if (!operationContext.isComplete()) {
                        operationContext.addMessageContext(inMessageContext);
                    }
                    AxisOperation axisOp = operationContext.getAxisOperation();
                    AxisMessage inMessage = axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    inMessageContext.setAxisMessage(inMessage);
                    inMessageContext.setServerSide(false);
                }

            } else {
                msgCtx.setOperationContext(operationContext);
                msgCtx.setServiceContext(outMessageContext.getServiceContext());
                AxisOperation axisOp = operationContext.getAxisOperation();
                AxisMessage inMessage = axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                msgCtx.setAxisMessage(inMessage);
                msgCtx.setTransportIn(inMessageContext.getTransportIn());
                msgCtx.setTransportOut(inMessageContext.getTransportOut());
                msgCtx.setServerSide(false);
                msgCtx.setProperty(BaseConstants.MAIL_CONTENT_TYPE,
                        inMessageContext.getProperty(BaseConstants.MAIL_CONTENT_TYPE));
                // FIXME: this class must not be transport dependent since it is used by AbstractTransportListener
                msgCtx.setIncomingTransportName(org.apache.axis2.Constants.TRANSPORT_MAIL);
                msgCtx.setEnvelope(inMessageContext.getEnvelope());

            }
            this.inMessageContext = inMessageContext;
            isComplete = true;
            this.notifyAll();
        }

    }


    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

}
