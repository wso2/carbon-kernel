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

package org.apache.axis2.rpc.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.DefaultObjectSupplier;

import javax.xml.namespace.QName;
import java.net.URL;

public class RPCServiceClient extends ServiceClient {

    private boolean notNullService;

    public RPCServiceClient(ConfigurationContext configContext, AxisService service)
            throws AxisFault {
        super(configContext, service);
        if (service != null) {
            notNullService = true;
        }
    }

    public RPCServiceClient() throws AxisFault {
        super();
    }

    public RPCServiceClient(ConfigurationContext configContext,
                            URL wsdlURL, QName wsdlServiceName,
                            String portName) throws AxisFault {
        super(configContext, wsdlURL, wsdlServiceName, portName);
        notNullService = true;
    }

    /**
     * Return value can be a single a object or an object array (itself an object) , but it is
     * difficulty to figure the return object correctly unless we have TyepMapping in the client
     * side too. Until it is finalized lets return OMElement as return value. And the retuen value
     * will be the body first element user has to deal with that and create his own object out of
     * that.
     *
     * @param opName Operation QName (to get the body wrapper element)
     * @param args   Arraylist of objects
     * @return Response OMElement
     * @throws AxisFault in case of a problem - this can either be a processing fault or a received
     *                   on-the-wire fault.
     */
    public OMElement invokeBlocking(QName opName, Object [] args) throws AxisFault {
        OMElement omElement = BeanUtil.getOMElement(opName, args, null, false, null);
        if (notNullService) {
            return super.sendReceive(opName, omElement);
        }
        return super.sendReceive(omElement);
    }

    /**
     * @param opName      Operation QName (to get the body wrapper element)
     * @param args        Arraylist of objects
     * @param returnTypes , this array contains the JavaTypes for the return object , it could be
     *                    one or more depending on the return type , most of the type array will
     *                    contain just one element It should be noted that the array should only
     *                    contains JavaTypes NOT real object , what this methods does is , get the
     *                    body first element , and if it contains more than one childern take ith
     *                    element and convert that to ith javatype and fill the return arrya the
     *                    array will look like as follows [Integer, String, MyBean , etc]
     * @return Object array , whic will contains real object , but the object can either be simple
     *         type object or the JavaBeans, thats what this method can handle right now the return
     *         array will contains [10, "Axis2Echo", {"foo","baa","11"}]
     * @throws AxisFault a problem occurred, either locally or on the other side of the wire
     */

    public Object[] invokeBlocking(QName opName, Object [] args, Class [] returnTypes)
            throws AxisFault {
        OMElement omElement = BeanUtil.getOMElement(opName, args, null, false, null);
        OMElement response;
        if (notNullService) {
            response = super.sendReceive(opName, omElement);
        } else {
            response = super.sendReceive(omElement);
        }
        return BeanUtil.deserialize(response, returnTypes,
                new DefaultObjectSupplier());
    }


    /**
     * Invoke the nonblocking/Asynchronous call
     *
     * @param opName Operation QName (to get the body wrapper element)
     * @param args an array of argument Objects
     * @param callback object implementing AxisCallback which will receive notifications
     * @throws AxisFault in case of a local processing error
     */
    public void invokeNonBlocking(QName opName,
                                  Object [] args,
                                  AxisCallback callback)
            throws AxisFault {
        OMElement omElement = BeanUtil.getOMElement(opName, args, null, false, null);
        // call the underlying implementation
        if (notNullService) {
            super.sendReceiveNonBlocking(opName, omElement, callback);
        } else {
            super.sendReceiveNonBlocking(omElement, callback);
        }
    }

    public void invokeRobust(QName opName,
                             Object [] args) throws AxisFault {
        OMElement omElement = BeanUtil.getOMElement(opName, args, null, false, null);
        //call the underline implementation
        if (notNullService) {
            super.sendRobust(opName, omElement);
        } else {
            super.sendRobust(omElement);
        }
    }
}
