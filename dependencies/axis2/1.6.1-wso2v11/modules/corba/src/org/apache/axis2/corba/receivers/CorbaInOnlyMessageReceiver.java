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
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.corba.deployer.CorbaConstants;
import org.apache.axis2.corba.exceptions.CorbaInvocationException;
import org.apache.axis2.corba.idl.types.IDL;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA_2_3.ORB;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class CorbaInOnlyMessageReceiver extends AbstractInMessageReceiver implements CorbaConstants {

    private static Log log = LogFactory.getLog(CorbaInOnlyMessageReceiver.class);
    private ORB orb = null;
    private Map invokerCache = new HashMap();

    public void invokeBusinessLogic(MessageContext inMessage) throws AxisFault {
        try{
            invoke(inMessage);
        } catch (org.omg.CORBA.TRANSIENT e) {
            log.info("org.omg.CORBA.TRANSIENT exception thrown.");
            /*
            * If cannot connect to the corba server
            * try again after clearing the cache
            * (eg. if the Corba server is restarted)
            */
            invokerCache.clear();
            invoke(inMessage);
        }
    }

    private void invoke(MessageContext inMessage) throws AxisFault {
        String methodName = null;
        try {
            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            AxisService service = inMessage.getAxisService();
            OMElement methodElement = inMessage.getEnvelope().getBody().getFirstElement();

            AxisMessage inAxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            String messageNameSpace;
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
                    throw new CorbaInvocationException("No IDL found");
                IDL idl = (IDL) idlParameter.getValue();
                invoker = CorbaUtil.getInvoker(service, obj, idl, methodName);
                invokerCache.put(methodName, invoker);
            }

            if (inAxisMessage != null) {
                if (inAxisMessage.getElementQName()!=null) {
                    elementQName = inAxisMessage.getElementQName();
                    messageNameSpace = elementQName.getNamespaceURI();
                    OMNamespace namespace = methodElement.getNamespace();
                    if (messageNameSpace != null) {
                        if (namespace == null ||
                                !messageNameSpace.equals(namespace.getNamespaceURI())) {
                            throw new AxisFault("namespace mismatch require " +
                                    messageNameSpace +
                                    " found " +
                                    methodElement.getNamespace().getNamespaceURI());
                        }
                    } else if (namespace != null) {
                        throw new AxisFault("namespace mismatch. Axis Oepration expects non-namespace " +
                                "qualified element. But received a namespace qualified element");
                    }

                    Object[] objectArray = CorbaUtil.extractParameters(methodElement, invoker.getParameterMembers());
                    invoker.setParameters(objectArray);
                }
                invoker.invoke();
            }
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
