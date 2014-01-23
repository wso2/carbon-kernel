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

/*
* Reflection based RPCMessageReceiver , request will be processed by looking at the method signature
* of the invocation method
*/
package org.apache.axis2.rpc.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.StreamWrapper;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RPCMessageReceiver extends AbstractInOutMessageReceiver {
    private static Log log = LogFactory.getLog(RPCMessageReceiver.class);

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
     * @param inMessage incoming MessageContext
     * @param outMessage outgoing MessageContext
     * @throws AxisFault
     */

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage)
            throws AxisFault {
        Method method = null;
        try {
            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(inMessage);

            Class implClass = obj.getClass();

            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            method = (Method)(op.getParameterValue("myMethod"));
            // If the declaring class has changed, then the cached method is invalid, so we need to
            // reload it. This is to fix AXIS2-3947.
            if (method != null && method.getDeclaringClass() != implClass) {
                method = null;
            }
            AxisService service = inMessage.getAxisService();
            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();
            AxisMessage inAxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            String messageNameSpace = null;


            if (method == null) {
                String methodName = op.getName().getLocalPart();
                Method[] methods = implClass.getMethods();

                for (Method method1 : methods) {
                    if (method1.isBridge()) {
                        continue;
                    }
                    if (method1.getName().equals(methodName)) {
                        method = method1;
                        op.addParameter("myMethod", method);
                        break;
                    }
                }
                if (method == null) {
                    throw new AxisFault("No such method '" + methodName +
                            "' in class " + implClass.getName());
                }
            }
            Object resObject = null;
            if (inAxisMessage != null) {
                resObject = RPCUtil.invokeServiceClass(inAxisMessage,
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
                	(method != null ? method.getName() : "null");
            }
            if (cause instanceof AxisFault) {
                log.debug(msg, cause);
                throw (AxisFault)cause;
            }

            Class[] exceptionTypes = method.getExceptionTypes();
            for (Class exceptionType : exceptionTypes){
                if (exceptionType.getName().equals(cause.getClass().getName())){
                    // this is an bussiness logic exception so handle it properly
                    String partQName = inMessage.getAxisService().getName() + getSimpleClassName(exceptionType);
                    TypeTable typeTable = inMessage.getAxisService().getTypeTable();
                    QName elementQName = typeTable.getQNamefortheType(partQName);
                    SOAPFactory fac = getSOAPFactory(inMessage);
                    OMElement exceptionElement = fac.createOMElement(elementQName);

                    if (exceptionType.getName().equals(Exception.class.getName())){
                        // this is an exception class. so create a element by hand and add the message
                       OMElement innterExceptionElement = fac.createOMElement(elementQName);
                       OMElement messageElement = fac.createOMElement("Message", inMessage.getAxisService().getTargetNamespace(), null);
                       messageElement.setText(cause.getMessage());

                       innterExceptionElement.addChild(messageElement);
                       exceptionElement.addChild(innterExceptionElement);
                    } else {
                        // if it is a normal bussiness exception we need to generate the schema assuming it is a pojo
                        QName innerElementQName = new QName(elementQName.getNamespaceURI(), getSimpleClassName(exceptionType));
                        XMLStreamReader xr = BeanUtil.getPullParser(cause,
                                innerElementQName, typeTable, true, false);
                        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(OMAbstractFactory.getOMFactory(), new StreamWrapper(xr));
                        OMElement documentElement = stAXOMBuilder.getDocumentElement();
                        exceptionElement.addChild(documentElement);
                    }

                    AxisFault axisFault = new AxisFault(cause.getMessage());
                    axisFault.setDetail(exceptionElement);
                    throw axisFault;
                }
            }

            log.error(msg, e);
            throw new AxisFault(msg, e);
        } catch(RuntimeException e) {
            log.error(e.getMessage(), e);
            throw AxisFault.makeFault(e);
        } catch (Exception e) {
            String msg = "Exception occurred while trying to invoke service method " +
            	(method != null ? method.getName() : "null");
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

     private String getSimpleClassName(Class type) {
        String simpleClassName = type.getName();
        int idx = simpleClassName.lastIndexOf('.');
        if (idx != -1 && idx < (simpleClassName.length() - 1)) {
            simpleClassName = simpleClassName.substring(idx + 1);
        }

        return simpleClassName.replace('$', '_');
    }
}
