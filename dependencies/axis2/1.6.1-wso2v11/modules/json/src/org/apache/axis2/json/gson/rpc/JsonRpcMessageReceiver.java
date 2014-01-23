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
package org.apache.axis2.json.gson.rpc;

import com.google.gson.stream.JsonReader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.json.gson.GsonXMLStreamReader;
import org.apache.axis2.json.gson.factory.JsonConstant;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class JsonRpcMessageReceiver extends RPCMessageReceiver {
    private static final Log log = LogFactory.getLog(RPCMessageReceiver.class);
    
    @Override
    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        Object tempObj = inMessage.getProperty(JsonConstant.IS_JSON_STREAM);
        boolean isJsonStream;
        if (tempObj != null) {
            isJsonStream = Boolean.valueOf(tempObj.toString());
        } else {
            // if IS_JSON_STREAM property  is not set then it is not a JSON request
            isJsonStream = false;
        }
        if (isJsonStream) {
            Object o = inMessage.getProperty(JsonConstant.GSON_XML_STREAM_READER);
            if (o != null) {
                GsonXMLStreamReader gsonXMLStreamReader = (GsonXMLStreamReader)o;
                JsonReader jsonReader = gsonXMLStreamReader.getJsonReader();
                if (jsonReader == null) {
                    throw new AxisFault("JsonReader should not be null");
                }
                Object serviceObj = getTheImplementationObject(inMessage);
                AxisOperation op = inMessage.getOperationContext().getAxisOperation();
                String operation = op.getName().getLocalPart();
                invokeService(jsonReader, serviceObj, operation , outMessage);
            } else {
                throw new AxisFault("GsonXMLStreamReader should be put as a property of messageContext " +
                        "to evaluate JSON message");
            }
        } else {
            super.invokeBusinessLogic(inMessage, outMessage);   // call RPCMessageReceiver if inputstream is null
        }
    }

    public void invokeService(JsonReader jsonReader, Object serviceObj, String operation_name,
                                   MessageContext outMes) throws AxisFault {
        String msg;
        Class implClass = serviceObj.getClass();
        Method[] allMethods = implClass.getDeclaredMethods();
        Method method = JsonUtils.getOpMethod(operation_name, allMethods);
        Class[] paramClasses = method.getParameterTypes();
        try {
            int paramCount = paramClasses.length;
            Object retObj = JsonUtils.invokeServiceClass(jsonReader, serviceObj, method, paramClasses, paramCount);

            // handle response
            outMes.setProperty(JsonConstant.RETURN_OBJECT, retObj);
            outMes.setProperty(JsonConstant.RETURN_TYPE, method.getReturnType());

        } catch (IllegalAccessException e) {
            msg = "Does not have access to " +
                    "the definition of the specified class, field, method or constructor";
            log.error(msg, e);
            throw AxisFault.makeFault(e);

        } catch (InvocationTargetException e) {
            msg = "Exception occurred while trying to invoke service method " +
                    (method != null ? method.getName() : "null");
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            msg = "Exception occur while encording or " +
                    "access to the input string at the JsonRpcMessageReceiver";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }
}
