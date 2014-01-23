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

package org.apache.axis2.transport.java;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.rpc.receivers.RPCUtil;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

public class JavaTransportSender extends AbstractHandler implements TransportSender {

    public void cleanup(MessageContext msgContext) throws AxisFault {
    }
    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault {
    }
    public void stop() {
    }
    public InvocationResponse invoke(MessageContext msgContext)
            throws AxisFault {
        SOAPEnvelope resultEnvelope = invokeJavaMethod(msgContext);
        Object responseMCObject = msgContext.getOperationContext().getMessageContext(
                WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        if (responseMCObject != null) {
            MessageContext responseMC = (MessageContext) responseMCObject;
            responseMC.setEnvelope(resultEnvelope);
        }
        return InvocationResponse.CONTINUE;
    }
    private SOAPEnvelope invokeJavaMethod(MessageContext inMessage)
            throws AxisFault {
        Class ImplClass;
        Object obj;
        Parameter implementationClass = inMessage.getParameter("className");
        if (implementationClass == null) {
            throw new AxisFault("Service Class Paramater does not find for the service : "
                    + inMessage.getAxisService().getName());
        }
        Object serviceImpleClass = implementationClass.getValue();
        try {
            ImplClass = Class.forName(serviceImpleClass.toString());
            obj = ImplClass.newInstance();
        } catch (Exception e) {
            throw new AxisFault("Exception occur while creating [ " + serviceImpleClass + " ]", e);
        }
        AxisService service = inMessage.getAxisService();
        OMElement methodElement = inMessage.getEnvelope().getBody()
                .getFirstElement();
        String messageNameSpace = inMessage.getAxisService().getTargetNamespace();
        String methodName = methodElement.getLocalName();
        //Serive impplementation class
        Method method = null;
          Method[] methods = ImplClass.getMethods();
	        for (int i = 0; i < methods.length; i++) {
	            if (methods[i].getName().equals(methodName)) {
	                method = methods[i];
	                break;
	            }
	        }
       if (method == null) {
            throw new AxisFault("method : " + methodName +
                    " : does not find in the service implementation class : " +
                    serviceImpleClass);
        }
        //gets the object array from the request OMElement
        ObjectSupplier obj1 = new DefaultObjectSupplier();
        Object[] objectArray = RPCUtil.processRequest(methodElement, method, obj1);
        //reflective invocation
        Object resObject ;
        try {
            resObject = method.invoke(obj, objectArray);
        } catch (Exception e) {
            throw new AxisFault("Error occured while invoking the method [ " + methodName + " ]", e);
        }
        SOAPFactory fac = getSOAPFactory(inMessage);
        OMNamespace ns = fac.createOMNamespace(messageNameSpace,
                service.getSchemaTargetNamespacePrefix());
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMElement bodyContent = null;
        if (resObject == null) {
            //Send empty body
            envelope.getBody().addChild(fac.createOMElement("item", ns));
        } else if (resObject instanceof Object[]) {
            QName resName = new QName(service.getSchemaTargetNamespace(),
                    method.getName() + "Response",
                    service.getSchemaTargetNamespacePrefix());
            //create the omelement from the response array
            OMElement bodyChild = RPCUtil.getResponseElement(resName,
                    (Object[]) resObject, false, null);
            //return type is sent with array size
            bodyChild.addAttribute("returnType",
                    method.getReturnType().getClass().getName() + ((Object[]) resObject).length,
                    envelope.getBody().getDefaultNamespace());
            envelope.getBody().addChild(bodyChild);
        } else {
            RPCUtil.processResponse(fac, resObject, bodyContent, ns, envelope, method, false, null);
            envelope.getBody().getFirstElement().addAttribute("returnType",
                    method.getReturnType().getClass().getName(), envelope.getBody().getDefaultNamespace());
        }
        return envelope;
    }

    private SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
        String nsURI = msgContext.getEnvelope().getNamespace().getNamespaceURI();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }
}
