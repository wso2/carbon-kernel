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
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.receivers.AbstractInOutAsyncMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RPCInOutAsyncMessageReceiver extends AbstractInOutAsyncMessageReceiver {

    private static Log log = LogFactory.getLog(RPCInOnlyMessageReceiver.class);

    /**
     * reflect and get the Java method - for each i'th param in the java method - get the first
     * child's i'th child -if the elem has an xsi:type attr then find the deserializer for it - if
     * not found, lookup deser for th i'th param (java type) - error if not found - deserialize &
     * save in an object array - end for
     * <p/>
     * - invoke method and get the return value
     * <p/>
     * - look up serializer for return value based on the value and type
     * <p/>
     * - create response msg and add return value as grand child of <soap:body>
     *
     * @param inMessage
     * @param outMessage
     * @throws AxisFault
     */

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage)
            throws AxisFault {
        Method method = null;
        try {
            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(inMessage);

            Class ImplClass = obj.getClass();

            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            AxisService service = inMessage.getAxisService();
            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();

            AxisMessage inaxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
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
            Object resObject = null;
            if (inaxisMessage != null) {
                resObject = RPCUtil.invokeServiceClass(inaxisMessage,
                        method,
                        obj,
                        messageNameSpace,
                        methodElement,inMessage);
            }


            SOAPFactory fac = getSOAPFactory(inMessage);

            // Handling the response

            AxisMessage outaxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            if (outaxisMessage != null && outaxisMessage.getElementQName() !=null) {
                messageNameSpace = outaxisMessage.getElementQName().getNamespaceURI();
            } else {
                messageNameSpace = service.getTargetNamespace();
            }

            OMNamespace ns = fac.createOMNamespace(messageNameSpace,
                                                   service.getSchemaTargetNamespacePrefix());
            SOAPEnvelope envelope = fac.getDefaultEnvelope();
            OMElement bodyContent = null;

            if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(
                    op.getMessageExchangePattern())){
                OMElement bodyChild = fac.createOMElement(outMessage.getAxisMessage().getName(), ns);
                envelope.getBody().addChild(bodyChild);
                outMessage.setEnvelope(envelope);
                return;
            }
            Parameter generateBare = service.getParameter(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
            if (generateBare!=null && "true".equals(generateBare.getValue())) {
                RPCUtil.processResonseAsDocLitBare(resObject, service,
                        envelope, fac, ns,
                        bodyContent, outMessage);
            } else {
                RPCUtil.processResponseAsDocLitWrapped(resObject, service,
                        method, envelope, fac, ns,
                        bodyContent, outMessage);
            }
             outMessage.setEnvelope(envelope);
        } catch (InvocationTargetException e) {
            String msg = null;
            Throwable cause = e.getCause();
            if (cause != null) {
                msg = cause.getMessage();
            }
            if (msg == null) {
                msg = "Exception occurred while trying to invoke service method " +
                        method.getName();
            }
            log.error(msg, e);
            if (cause instanceof AxisFault) {
                throw (AxisFault)cause;
            }
            throw new AxisFault(msg);
        } catch (Exception e) {
            String msg = "Exception occurred while trying to invoke service method " +
                    method.getName();
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }
}
