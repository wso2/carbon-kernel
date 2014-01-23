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

package org.apache.axis2.ping;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.Pingable;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

public class PingMessageReceiver extends AbstractInOutSyncMessageReceiver implements PingConstants {
    private static Log log = LogFactory.getLog(PingMessageReceiver.class);

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {

        try {
            int operationStatus;
            AxisOperation axisOperation;
            PingResponse pingResponse = new PingResponse();
            pingResponse.initPingResponse(inMessage);
            Iterator opListIterator = getAxisOperations(inMessage);

            while (opListIterator.hasNext()) {
                axisOperation = (AxisOperation) opListIterator.next();

                if (!PING_METHOD_NAME.equals(axisOperation.getName().getLocalPart())) {
                    operationStatus = invokePingableMR(inMessage, axisOperation);
                    pingResponse.addOperationStatus(axisOperation, operationStatus);
                }
            }

            SOAPEnvelope envelope = pingResponse.getResposeEnvelope();
            outMessage.setEnvelope(envelope);

        } catch (Exception e) {
            String msg = "Exception occurred while trying to ping the service" +
                    inMessage.getAxisService().getName();

            log.error(msg, e);
            throw new AxisFault(msg);
        }
    }

    /**
     * This method identifies whether the ping request is an operation level or a service level one.
     * If it is servicel level, returns the operation iterator from the AxisService
     *
     * @param inMessage
     * @return Iterator for the list of AxisOperations
     * @throws AxisFault
     */
    private Iterator getAxisOperations(MessageContext inMessage) throws AxisFault {
        boolean serviceLevel = false;
        Iterator operationsIterator;

        OMElement element = null;
        OMElement pingRequestElement = inMessage.getEnvelope().
                getBody().getFirstChildWithName(new QName(TYPE_PING_REQUEST));

        if (pingRequestElement == null) {
            //throw new AxisFault("Ping Request is not specified");
            //SOAP body is null or ping request is not specified
            //This is considered as a service level ping.
            serviceLevel = true;
        } else {
            element = pingRequestElement.getFirstChildWithName(new QName(TAG_OPERATION));
        }


        if (!serviceLevel && element != null) {
            //Operations to be pinged has been specified in the ping request
            Iterator elementIterator = pingRequestElement.getChildrenWithName(new QName(TAG_OPERATION));
            ArrayList operationList = new ArrayList();
            AxisOperation axisOperation;

            while (elementIterator.hasNext()) {
                OMElement opElement = (OMElement) elementIterator.next();
                String operationName = opElement.getText();
                axisOperation = inMessage.getAxisService().getOperation(new QName(operationName));

                if (axisOperation != null) {
                    operationList.add(axisOperation);
                } else {
                    String msg = "Operation not found: " + operationName +
                            " specified in the ping request for the service" +
                            inMessage.getAxisService().getName();
                    log.error(msg);
                    throw new AxisFault(msg);
                }
            }
            operationsIterator = operationList.iterator();
        } else {
            //No operation is mentioned in the request.. So this is a service level ping
            operationsIterator = inMessage.getAxisService().getOperations();
        }
        return operationsIterator;
    }

    /**
     * This method checks whether the MessageReceiver is Pingable
     * If it is Pingable invoke the ping() method after setting the operation name to be pinged as property.
     *
     * @param axisOperation
     * @return
     * @throws AxisFault
     */
    private int invokePingableMR(MessageContext inMessage, AxisOperation axisOperation) throws AxisFault {
        MessageReceiver msgReceiver = axisOperation.getMessageReceiver();

        if (msgReceiver != null && msgReceiver instanceof Pingable) {
            Pingable pingableMR = (Pingable) msgReceiver;
            //Adding the operation name to be pinged as a property
            inMessage.setProperty(Pingable.OPERATION_TO_PING, axisOperation.getName().getLocalPart());
            return pingableMR.ping();
        }

        return Pingable.PING_MODULE_LEVEL;
    }
}
