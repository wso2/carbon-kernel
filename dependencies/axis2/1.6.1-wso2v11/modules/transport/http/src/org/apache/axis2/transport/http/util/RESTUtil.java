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

package org.apache.axis2.transport.http.util;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.dispatchers.HTTPLocationBasedDispatcher;
import org.apache.axis2.dispatchers.RequestURIBasedDispatcher;
import org.apache.axis2.dispatchers.RequestURIOperationDispatcher;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class RESTUtil {

    public static Handler.InvocationResponse processXMLRequest(MessageContext msgContext,
                                                               InputStream in,
                                                               OutputStream out, String contentType)
            throws AxisFault {
        try {
            msgContext.setDoingREST(true);
            String charSetEncoding = BuilderUtil.getCharSetEncoding(contentType);
            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEncoding);
            dispatchAndVerify(msgContext);
            in = HTTPTransportUtils.handleGZip(msgContext, in);
            SOAPEnvelope soapEnvelope;
            if (msgContext.getAxisService() == null) {
                soapEnvelope = TransportUtils.createSOAPEnvelope(null);
            } else {
                soapEnvelope = TransportUtils.createSOAPMessage(msgContext, in, contentType);
            }

            msgContext.setEnvelope(soapEnvelope);
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                                   contentType);

            msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

        } catch (AxisFault axisFault) {
            throw axisFault;
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } finally {
            String messageType =
                    (String) msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
            if (HTTPConstants.MEDIA_TYPE_X_WWW_FORM.equals(messageType) ||
                    HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA.equals(messageType)) {
                msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                                       HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
            }
        }
        return invokeAxisEngine(msgContext);
    }

    public static Handler.InvocationResponse processURLRequest(MessageContext msgContext,
                                                               OutputStream out, String contentType)
            throws AxisFault {
        // here, only the parameters in the URI are supported. Others will be discarded.
        try {

            if (contentType == null || "".equals(contentType)) {
                contentType = HTTPConstants.MEDIA_TYPE_X_WWW_FORM;
            }

            // set the required properties so that even if there is an error during the dispatch
            // phase the response message will be passed to the client well. 
            msgContext.setDoingREST(true);
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
            String charSetEncoding = BuilderUtil.getCharSetEncoding(contentType);
            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEncoding);
            // 1. First dispatchAndVerify and find out the service and the operation.
            dispatchAndVerify(msgContext);
            SOAPEnvelope soapEnvelope;
            if (msgContext.getAxisService() == null) {
                soapEnvelope = TransportUtils.createSOAPEnvelope(null);
                msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, TransportUtils.getContentType(contentType, msgContext));
            } else {
                try {
                    soapEnvelope = TransportUtils.createSOAPMessage(msgContext, null, contentType);
                } catch (XMLStreamException e) {
                    throw AxisFault.makeFault(e);
                }
            }

            msgContext.setEnvelope(soapEnvelope);


        } catch (AxisFault axisFault) {
            throw axisFault;
        }
        catch (IOException e) {
            throw AxisFault.makeFault(e);
        } finally {
            String messageType =
                    (String) msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
            if (HTTPConstants.MEDIA_TYPE_X_WWW_FORM.equals(messageType) ||
                    HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA.equals(messageType)) {
                msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                                       HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
            }
        }
        return invokeAxisEngine(msgContext);
    }

    private static Handler.InvocationResponse invokeAxisEngine(MessageContext messageContext)
            throws AxisFault {
        return AxisEngine.receive(messageContext);

    }

    private static void dispatchAndVerify(MessageContext msgContext) throws AxisFault {
        RequestURIBasedDispatcher requestDispatcher = new RequestURIBasedDispatcher();
        requestDispatcher.invoke(msgContext);
        AxisService axisService = msgContext.getAxisService();
        if (axisService != null) {
            HTTPLocationBasedDispatcher httpLocationBasedDispatcher =
                    new HTTPLocationBasedDispatcher();
            httpLocationBasedDispatcher.invoke(msgContext);
            if (msgContext.getAxisOperation() == null) {
                RequestURIOperationDispatcher requestURIOperationDispatcher =
                        new RequestURIOperationDispatcher();
                requestURIOperationDispatcher.invoke(msgContext);
            }

            AxisOperation axisOperation;
            if ((axisOperation = msgContext.getAxisOperation()) != null) {
                AxisEndpoint axisEndpoint =
                        (AxisEndpoint) msgContext.getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
                if (axisEndpoint != null) {
                    AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisEndpoint
                            .getBinding().getChild(axisOperation.getName());
                    msgContext.setProperty(Constants.AXIS_BINDING_OPERATION, axisBindingOperation);
                }
                msgContext.setAxisOperation(axisOperation);
            }
        }
    }


}


