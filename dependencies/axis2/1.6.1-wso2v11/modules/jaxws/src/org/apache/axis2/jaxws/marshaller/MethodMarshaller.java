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

package org.apache.axis2.jaxws.marshaller;

import java.util.Map;

import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;

import javax.xml.ws.WebServiceException;

/**
 * This class marshals and unmarshals method invocations.
 * <p/>
 * Here is the high-level view of marshalling: SIGNATURE_ARGS ---> Type Enabled Object  ----->
 * Element Enabled Object ---> MESSAGE (XML)
 * <p/>
 * The Signature objects are the objects from the SEI method signature.  They may be values or
 * holders of values. The values are "type enabled objects" (i.e. String), which means that they
 * cannot be marshalled or unmarshalled.
 *
 * @see org.apache.axis2.jaxws.util.XMLRootElementUtils for details on Type Enabled and Element
 *      Enabled objects.
 *      <p/>
 *      The values are enhanced (if necessary) into Element Enabled Objects.  These can be
 *      marshalled or unmarshalled using JAXB.
 * @see org.apache.axis2.jaxws.marshaller.impl.alt.PDElement
 *      <p/>
 *      The element enabled objects are put onto the message.
 *      <p/>
 *      The high-level view of unmarshalling is the reverse. SIGNATURE_ARGS <---- Type Enabled
 *      Object  <----- Element Enabled Object <---- MESSAGE (XML)
 *      <p/>
 *      See the specific MethodMarshaller implementations to see how doc/lit wrapped, doc/lit bare
 *      and rpc/lit affect the process of going from SIGNATURE_ARGS to the element enabled objects.
 *      <p/>
 *      If there are any problems, a WebServiceException is thrown.  (Each of the methods is
 *      guranteed to catch any unchecked exception and wrap it in a WebServiceException).
 */
public interface MethodMarshaller {

    /**
     * This method converts SIGNATURE_ARGS into a Message. It is used on the client
     *
     * @param signatureArgs
     * @param opDesc
     * @param requestContext
     * @return Message
     */
    public Message marshalRequest(Object[] signatureArgs, 
                OperationDescription opDesc,
                Map<String, Object> requestContext) 
            throws WebServiceException;

    /**
     * This method converts the SIGNATURE_ARGS and RETURN object into a Message. It is used on the
     * server
     *
     * @param returnObject
     * @param signatureArgs
     * @param OperationDesc
     * @param Protocol      for response
     * @return Message
     */
    public Message marshalResponse(Object returnObject, Object[] signatureArgs,
                                   OperationDescription opDesc, Protocol protocol)
            throws WebServiceException;


    /**
     * This method converts the Message into a SIGNATURE_ARGS It is used on the server
     *
     * @param message
     * @return signature args
     */
    public Object[] demarshalRequest(Message message, OperationDescription opDesc)
            throws WebServiceException;

    /**
     * This method gets the objects from the Message and sets them onto the SIGNATURE_ARGS It also
     * returns the RETURN object. Called on client
     *
     * @param message
     * @param signatureAgs  (same array of args that were used for marshalRequest.  The out/inout
     *                      holders are populated with new values)
     * @param OperationDesc
     * @return returnObject
     */
    public Object demarshalResponse(Message message, Object[] signatureArgs,
                                    OperationDescription opDesc) throws WebServiceException;

    /**
     * This method converts a Message (containing a fault) into a JAX-WS Service or
     * WebServiceException. Used on the client.
     *
     * @param message
     * @param Message
     * @return Throwable
     */
    public Throwable demarshalFaultResponse(Message message, OperationDescription opDesc)
            throws WebServiceException;

    /**
     * This method creates a Message from a Throwable input parameter. Used on the server.
     *
     * @param Throwable
     * @param OperationDesc
     * @param Protocol      for response
     * @return
     */
    public Message marshalFaultResponse(Throwable throwable, OperationDescription opDesc,
                                        Protocol protocol) throws WebServiceException;


}
