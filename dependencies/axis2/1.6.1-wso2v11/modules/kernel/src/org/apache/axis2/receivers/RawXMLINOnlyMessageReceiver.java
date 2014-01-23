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


package org.apache.axis2.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;

import java.lang.reflect.Method;

/**
 * The RawXMLINOnlyMessageReceiver MessageReceiver hands over the raw request received to
 * the service implementation class as an OMElement. The implementation class is NOT
 * expected to return any value, but may do so and it would be ignored. This is a
 * synchronous MessageReceiver, and finds the service implementation class to invoke by
 * referring to the "ServiceClass" parameter value specified in the service.xml and
 * looking at the methods of the form void <<methodName>>(OMElement request)
 *
 * @see RawXMLINOutMessageReceiver
 * @see RawXMLINOutAsyncMessageReceiver
 */
public class RawXMLINOnlyMessageReceiver extends AbstractInMessageReceiver
        implements MessageReceiver {

    private Method findOperation(AxisOperation op, Class implClass) {
        Method method = (Method)(op.getParameterValue("myMethod"));
        if (method != null && method.getDeclaringClass() == implClass) return method;

        String methodName = op.getName().getLocalPart();
        try {
            // Looking for a method of the form "void method(OMElement)"
            method = implClass.getMethod(methodName, new Class [] { OMElement.class });
            if (method.getReturnType().equals(void.class)) {
                try {
                    op.addParameter("myMethod", method);
                } catch (AxisFault axisFault) {
                    // Do nothing here
                }
                return method;
            }
        } catch (NoSuchMethodException e) {
            // Fall through
        }

        return null;
    }

    /**
     * Invokes the business logic invocation on the service implementation class
     *
     * @param msgContext the incoming message context
     * @throws AxisFault on invalid method (wrong signature)
     */
    public void invokeBusinessLogic(MessageContext msgContext) throws AxisFault {
        try {
            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class implClass = obj.getClass();

            AxisOperation op = msgContext.getAxisOperation();
            Method method = findOperation(op, implClass);

            if (method == null) {
                throw new AxisFault(Messages.getMessage("methodDoesNotExistInOnly"));
            }

            method.invoke(obj,
                          new Object [] { msgContext.getEnvelope().getBody().getFirstElement() });

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
