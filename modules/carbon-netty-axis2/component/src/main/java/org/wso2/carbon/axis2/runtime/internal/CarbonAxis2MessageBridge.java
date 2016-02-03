/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.axis2.runtime.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.CarbonAxis2Exception;
import org.wso2.carbon.axis2.runtime.ResponseStatus;

import java.io.InputStream;
import java.io.OutputStream;

public class CarbonAxis2MessageBridge {
    private static final Logger logger = LoggerFactory.getLogger(CarbonAxis2MessageBridge.class);

    public static ResponseStatus processMessage(InputStream inputStream,
                                                OutputStream outputStream,
                                                String contentType,
                                                String soapActionHeader,
                                                String requestUri) throws CarbonAxis2Exception {
        MessageContext messageContext = createMessageContext(requestUri);
        messageContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);

        Handler.InvocationResponse pi;
        try {
            pi = HTTPTransportUtils.
                    processHTTPPostRequest(messageContext, inputStream, outputStream, contentType, soapActionHeader,
                            requestUri);

            if (pi.equals(Handler.InvocationResponse.SUSPEND)) {
                // TODO : Need to await for the response
                if (logger.isDebugEnabled()) {
                    logger.debug("InvocationResponse.SUSPEND");
                }
            }

            return ResponseStatus.READY;
        } catch (AxisFault axisFault) {
            throw new CarbonAxis2Exception("Failed to process message", axisFault);
        }
    }

    private static MessageContext createMessageContext(String requestUri) {
        ConfigurationContext configurationContext = DataHolder.getInstance().getConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();

        MessageContext msgContext = configurationContext.createMessageContext();
        //String requestURI = request.getRequestURI();

//        String trsPrefix = request.getRequestURL().toString();
        String trsPrefix = requestUri;
        int sepindex = trsPrefix.indexOf(':');
        if (sepindex > -1) {
            trsPrefix = trsPrefix.substring(0, sepindex);
            msgContext.setIncomingTransportName(trsPrefix);
        } else {
            msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
            trsPrefix = Constants.TRANSPORT_HTTP;
        }
        TransportInDescription transportIn =
                axisConfiguration.getTransportIn(msgContext.getIncomingTransportName());
        //set the default output description. This will be http

        TransportOutDescription transportOut = axisConfiguration.getTransportOut(trsPrefix);
        if (transportOut == null) {
            // if the req coming via https but we do not have a https sender
            transportOut = axisConfiguration.getTransportOut(Constants.TRANSPORT_HTTP);
        }


        msgContext.setTransportIn(transportIn);
        msgContext.setTransportOut(transportOut);
        msgContext.setServerSide(true);

//        if (!invocationType) {
//            String query = request.getQueryString();
//            if (query != null) {
//                requestURI = requestURI + "?" + query;
//            }
//        }

        msgContext.setTo(new EndpointReference(requestUri));
//        msgContext.setFrom(new EndpointReference(request.getRemoteAddr()));
//        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());
//        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
//                new ServletBasedOutTransportInfo(response));
        // set the transport Headers
//        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getTransportHeaders(request));
//        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, request);
//        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, response);
//        try {
//            ServletContext context = getServletContext();
//            if(context != null) {
//                msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, context);
//            }
//        } catch (Exception e){
//            log.debug(e.getMessage(), e);
//        }

        //setting the RequestResponseTransport object
//        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
//                new ServletRequestResponseTransport());

        return msgContext;
    }
}
