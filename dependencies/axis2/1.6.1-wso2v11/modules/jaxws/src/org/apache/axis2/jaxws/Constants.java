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

package org.apache.axis2.jaxws;

/**
 * Constants that apply to the JAX-WS implementation.
 *
 */
public interface Constants {
    public static final String ENDPOINT_CONTEXT_MAP =
        "org.apache.axis2.jaxws.addressing.util.EndpointContextMap";
    
    public static final String JAXWS_OUTBOUND_SOAP_HEADERS  = 
        org.apache.axis2.Constants.JAXWS_OUTBOUND_SOAP_HEADERS;
    public static final String JAXWS_INBOUND_SOAP_HEADERS   = 
        org.apache.axis2.Constants.JAXWS_INBOUND_SOAP_HEADERS;
    /**
     * Value that can be set on a MessageContext.  The property value should be a Boolean()
     * 
     * If set to false, then JAXB streaming of the XML body is disabled.
     * A value of false will result in slower performance for unmarshalling JAXB objects
     * but is a loss-less transformation.  
     *  
     * A value of true will cause the JAXB objects to be created when the XML body is initially 
     * parsed, which is more performant, but it may loose some information contained in the 
     * original XML such as namespace prefixes if the XML stream is recreated from the JAXB 
     * objects.
     * 
     * The default value is Boolean(true) if this property is not set.  
     * @deprecated see JAXWS_PAYLOAD_HIGH_FIDELITY
     */
    public static final String JAXWS_ENABLE_JAXB_PAYLOAD_STREAMING = 
        "org.apache.axis2.jaxws.enableJAXBPayloadStreaming";
    
    /**
     * Context Property:
     * Name: jaxws.payload.highFidelity
     * Value: Boolean.TRUE or Boolean.FALSE
     * Default: null, which is interpreted as FALSE....engine may set this to TRUE in some cases.
     * 
     * Configuration Parameter
     * Name: jaxws.payload.highFidelity
     * Value: String or Boolean representing true or false
     * Default: null, which is interpreted as FALSE
     * 
     * Description:
     * If the value is false, the jax-ws engine will transform the message in the most
     * performant manner.  In some cases these transformations will cause the loss of some information.
     * For example, JAX-B transformations are lossy.  
     * 
     * If the value is true, the jax-ws engine will transform the message in the most loss-less manner.
     * In some cases this will result in slower performance.  The message in such cases is "high fidelity",
     * which means that it is a close replica of the original message.
     * 
     * Customers should accept the default behavior (false), and only set the value to true if it is
     * necessary for a SOAP Handler or other code requires a high fidelity message.
     * 
     * The engine will first examine the Context property.  If not set, the value of the Configuration
     * property is used.
     */
    public static final String JAXWS_PAYLOAD_HIGH_FIDELITY =
        "jaxws.payload.highFidelity";
    
    /**
     * Context Property:
     * Name: jaxws.provider.interpretNullAsOneway
     * Value: Boolean.TRUE or Boolean.FALSE
     * Default: TRUE.
     * 
     * Configuration Parameter
     * Name: jaxws.provider.interpretNullAsOneway
     * Value: String or Boolean representing true or false
     * Default: true
     * 
     * Description:
     * If the value is false, the jax-ws engine will interpret a null response from a provider as an empty
     * response to a two-way operation.  As a result it will create a SOAPEnvelope with an empty SOAPBody and send that
     * as a response.
     * 
     * If the value is true, the jax-ws engine will intrepret a null return value from a provider as an indication of 
     * a one-way operation.  As a result, the engine will halt processing on the response.  Response handlers will not
     * be invoked.  An HTTP acknowledgment will be sent back to the client.  No SOAPEnvelope will be sent.  You must use
     * one-way client when invoking a Provider which returns null if this property is true.
     * 
     * This is only true for operations which are not defined in WSDL.  If the operation is defined in WSDL, the WSDL
     * determine the response for a Provider that returns null.  If the WSDL defines a two-way operation, a null
     * from a provider will continue to produce a SOAPEnvelope with and empty SOAPBody as the response.
     * 
     * The engine will first examine the Context property.  If not set, the value of the Configuration
     * property is used.
     */
    public static final String JAXWS_PROVIDER_NULL_ONEWAY =
        "jaxws.provider.interpretNullAsOneway";
    
    public static final String MEP_CONTEXT = 
        "org.apache.axis2.jaxws.handler.MEPContext";
    
    /**
     * If a checked exception is thrown by the webservice's webmethod, then
     * the name of the checked exception is placed in the outbound response context.
     */
    public static final String CHECKED_EXCEPTION =
        "org.apache.axis2.jaxws.checkedException";
    
    /**
     * If an exception is thrown by the JAXWS webservice's webmethod, the 
     * Throwable object is placed in the service outbound response context.
     */
    public static final String JAXWS_WEBMETHOD_EXCEPTION = 
        org.apache.axis2.Constants.JAXWS_WEBMETHOD_EXCEPTION;
    
    /**
     * This constant introduces an extension for @BindingType annotation.
     * When the value of BindingType annotation is set to this constant,
     * the javax.xml.ws.Provider java endpoints will cater to SOAP11 and SOAP12
     * messages.
     */
    public static final String SOAP_HTTP_BINDING ="SOAP_HTTP_BINDING";
    
    /**
     * This constant will be used to determine if a Exception will be throw by
     * JAX-WS layer when a SOAP Fault is received on response. 
     */
    public static final String THROW_EXCEPTION_IF_SOAP_FAULT = "jaxws.response.throwExceptionIfSOAPFault";
    
    /** 
     * Context Property:
     * Name: jaxws.header.parameter.isNull.write.element.with.xsi.nil
     * Value: Boolean.TRUE or Boolean.FALSE
     * Default: null, which is interpretted as Boolean.TRUE
     * 
     * If the @WebParam indicates that the parameter is mapped to a header 
     * and the argument for the parameter is null, this property is queried by the
     * JAX-WS runtime to determine if 
     *  a) TRUE: A SOAP header element is serialized with an xsi:nil="true" attribute
     *  b) FALSE: No SOAP header element is serialized.
     *  
     *  The default is TRUE because the JAX-WS developers feel that this is a safer
     *  approach.
     * 
     */
    public static final String WRITE_HEADER_ELEMENT_IF_NULL = "jaxws.header.parameter.isNull.write.element.with.xsi.nil";
    /**
     * This constant will be used to store the location of JAX-WS generated artifacts cache.
     */
    public static final String WS_CACHE="wsCache";
    
    /**
     * Context Property:
     * Name: jaxws.jaxb.write.remove.illegal.chars
     * Value: Boolean.TRUE or Boolean.FALSE
     * Default: null, which is interpreted as FALSE.
     * 
     * Configuration Parameter
     * Name: jaxws.jaxb.write.remove.illegal.chars
     * Value: String or Boolean representing true or false
     * Default: null, which is interpreted as FALSE
     * 
     * Description:
     * If the value is true, the jax-ws engine will detect and remove
     * illegal characters (characters not supported in xml) when writing
     * a JAXB data bean associated with a jaxws web method
     *  http://www.w3.org/TR/2008/REC-xml-20081126/#NT-Char
     * This extra filter may degrade performance.
     * 
     * Customers should accept the default behavior (false), and only set the value to true if the
     * character data produced by their web service is invalid and cannot be filtered by some
     * other mechanism.
     * 
     * The engine will first examine the Context property.  If not set, the value of the Configuration
     * property is used.
     */
    public static final String JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS = 
        "jaxws.jaxb.write.remove.illegal.chars";
    
    /**
     * javax.xml.ws.handler.MessageContext  Property:
     * Name: jaxws.message.as.string
     * Value: null or MessageAccessor
     * 
     * Description:
     * A handler or resource injection @WebServiceContext may use
     * this property to get access to a MessageAccessor object.
     * The MessageAccessor contains methods to allow a user to 
     * get additional attributes from the message (for example getMessageAsString)
     */
    public static final String JAXWS_MESSAGE_ACCESSOR = 
        "jaxws.message.accessor";

    /** 
     * Context Property:
     * Name: jaxws.dispatch.outbound.operation.resolution.enable
     * Value: String "false" or "true"
     * Default: null, which is interpreted as "true"
     * Can be set on:
     * - Axis Configuration, which affects operation resolution across all Dispatch<T> clients
     * - Request message context, which affects only the Dispatch<T> client using that context.
     *
     * Indicates if a Dispatch<T> message should be parsed to determine the operation indicated in the
     * message, and use that to determine the Action that should be placed in the outgoing message.  The
     * Action would be placed in the SOAPAction HTTP header and any WS-Addressing Action headers if 
     * WS-Addressing is enabled.  Prior to the introduction of this property and associated support, 
     * for Dispatch<T> the client would have to set the Action on the Request Message context in order to 
     * get a meaningful value set as the Action.
     * 
     * Note that parsing the outgoing message in order to determine the operation indicated in the
     * message can be slow.  Therefore, this property is provided to disable that operation resolution.
     * The default, however, is to do operation resolution.
     * 
     * Operation resolution will also be disabled on a Dispatch<T> client if an Action was set on the 
     * request message context.  
     * 
     * @see javax.xml.ws.BindingProvider.SOAPACTION_USE_PROPERTY
     * @see javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY
     */
    public static final String  DISPATCH_CLIENT_OUTBOUND_RESOLUTION = "jaxws.dispatch.outbound.operation.resolution.enable"; 

}
