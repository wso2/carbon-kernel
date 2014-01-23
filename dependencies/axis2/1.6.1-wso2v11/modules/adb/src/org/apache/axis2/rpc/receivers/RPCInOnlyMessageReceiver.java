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

package org.apache.axis2.rpc.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RPCInOnlyMessageReceiver extends AbstractInMessageReceiver {

    private static Log log = LogFactory.getLog(RPCInOnlyMessageReceiver.class);

    public void invokeBusinessLogic(MessageContext inMessage) throws AxisFault {
        Method method = null;
        try {
            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(inMessage);

            Class ImplClass = obj.getClass();

            AxisOperation op = inMessage.getOperationContext().getAxisOperation();

            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();

            AxisMessage inAxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            String messageNameSpace = null;
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isBridge()) {
                    continue;
                }
                if (methods[i].getName().equals(methodName)) {
                    method = methods[i];
                    break;
                }
            }
            if (inAxisMessage != null) {
                RPCUtil.invokeServiceClass(inAxisMessage,
                        method,
                        obj,
                        messageNameSpace,
                        methodElement,inMessage);

            }
            replicateState(inMessage);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                String msg = cause.getMessage();
                if (msg == null) {
                    msg = "Exception occurred while trying to invoke service method " +
                            method.getName();
                }
                log.error(msg, cause);
            } else {
                cause = e;
            }
            throw AxisFault.makeFault(cause);
        } catch (Exception e) {
            String msg = "Exception occurred while trying to invoke service method " +
                    method.getName();
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }
}
