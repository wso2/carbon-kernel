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

package org.apache.axis2.corba.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.corba.deployer.CorbaConstants;
import org.apache.axis2.corba.exceptions.CorbaInvocationException;
import org.apache.axis2.corba.idl.types.IDL;
import org.apache.axis2.corba.idl.types.Member;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.receivers.AbstractInOutAsyncMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA_2_3.ORB;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * This is takes care of the IN-OUT sync MEP in the server side
 *
 * @deprecated no longer needed, going away after 1.3
 */
public class CorbaInOutAsyncMessageReceiver extends AbstractInOutAsyncMessageReceiver implements CorbaConstants {

    private static Log log = LogFactory.getLog(CorbaInOutAsyncMessageReceiver.class);
    private ORB orb = null;
    private Map invokerCache = new HashMap();

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        try{
            invoke(inMessage, outMessage);
        } catch (org.omg.CORBA.TRANSIENT e) {
            log.info("org.omg.CORBA.TRANSIENT exception thrown.");
            /*
            * If cannot connect to the corba server
            * try again after clearing the cache
            * (eg. if the Corba server is restarted)
            */
            invokerCache.clear();
            invoke(inMessage, outMessage);
        }
    }

    private void invoke(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        String methodName = null;
        try {
            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            AxisService service = inMessage.getAxisService();
            OMElement methodElement = inMessage.getEnvelope().getBody().getFirstElement();

            AxisMessage inAxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            String messageNameSpace = null;
            QName elementQName;
            methodName = op.getName().getLocalPart();

            Invoker invoker = (Invoker) invokerCache.get(methodName);
            if (invoker==null) {
                if (orb==null) {
                    Parameter orbParam = service.getParameter(ORB_LITERAL);
                    orb = orbParam != null ? (ORB) orbParam.getValue() : CorbaUtil.getORB(service);
                }
                org.omg.CORBA.Object obj = CorbaUtil.resolveObject(service, orb);
                Parameter idlParameter = service.getParameter(IDL_LITERAL);
                if (idlParameter==null)
                    throw new CorbaInvocationException("IDL not found");
                IDL idl = (IDL) idlParameter.getValue();
                invoker = CorbaUtil.getInvoker(service, obj, idl, methodName);
                invokerCache.put(methodName, invoker);
            }

            Object resObject = null;
            Member[] params = null;
            Object[] outParamValues = null;
            if (inAxisMessage != null) {
                if (inAxisMessage.getElementQName()!=null) {
                    elementQName = inAxisMessage.getElementQName();
                    messageNameSpace = elementQName.getNamespaceURI();
                    OMNamespace namespace = methodElement.getNamespace();
                    if (messageNameSpace != null) {
                        if (namespace == null || !messageNameSpace.equals(namespace.getNamespaceURI())) {
                            throw new AxisFault("namespace mismatch require " +
                                    messageNameSpace +
                                    " found " + methodElement.getNamespace().getNamespaceURI());
                        }
                    } else if (namespace != null) {
                        throw new AxisFault("namespace mismatch. Axis Oepration expects non-namespace " +
                                "qualified element. But received a namespace qualified element");
                    }

                    Object[] objectArray = CorbaUtil.extractParameters(methodElement, invoker.getParameterMembers());
                    invoker.setParameters(objectArray);
                    params = invoker.getParameterMembers();
                    outParamValues = invoker.getOutParameterValuess();
                }
                resObject = invoker.invoke();
            }
            SOAPFactory fac = getSOAPFactory(inMessage);

            AxisMessage outaxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            if (messageNameSpace == null) {
                QName qname = outaxisMessage.getElementQName();
                if (qname != null) {
                    messageNameSpace = qname.getNamespaceURI();
                }
            }
            // Handling the response
            CorbaUtil.processResponse(resObject, params, outParamValues, invoker.getReturnType(), service, methodName, fac,
                    messageNameSpace, outMessage);
        } catch (CorbaInvocationException e) {
            String msg;
            Throwable cause = e.getCause();
            if (cause != null) {
                msg = cause.getMessage();
                if (msg == null) {
                    msg = "Exception occurred while trying to invoke service method " + methodName;
                }
                //log.error(msg, e);
                if (cause instanceof AxisFault) {
                    throw (AxisFault) cause;
                }
            } else {
                msg = e.getMessage();
            }
            throw new AxisFault(msg);
        }
    }
}
