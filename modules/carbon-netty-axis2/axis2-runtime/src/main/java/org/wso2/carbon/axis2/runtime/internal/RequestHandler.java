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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Netty Axis2 Runtime RequestHandler.
 *
 * @since 1.0.0
 */
public class RequestHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LoggerFactory.getLogger(Axis2NettyInitializerComponent.class.getName());
    private static final String AXIS2_CONTEXT_PATH = "/services/";

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;

            if (fullHttpRequest.getUri() != null && !fullHttpRequest.getUri().startsWith(AXIS2_CONTEXT_PATH)) {
                throw new HandlerException(HttpResponseStatus.BAD_REQUEST, "Invalid context path");
            }

            String contentType = fullHttpRequest.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            if (contentType != null &&
                    !(contentType.startsWith("application/soap+xml") || contentType.startsWith("text/xml"))) {
                throw new HandlerException(HttpResponseStatus.BAD_REQUEST, "Unsupported content type");
            }

            handleRequest(channelHandlerContext, fullHttpRequest, contentType);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        String soapEnvelope = "";
        if (cause instanceof HandlerException) {
            HandlerException handlerException = ((HandlerException) cause);
            String details = (handlerException.getCause() != null) ? handlerException.getCause().toString() : "";
            soapEnvelope = createSOAPErrorMessage(handlerException.getFailureStatus().toString(),
                    handlerException.getMessage(), details, "");
        } else {
            soapEnvelope = createSOAPErrorMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.toString(),
                    cause.getMessage(), "", "");
        }
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(soapEnvelope.getBytes(StandardCharsets.UTF_8)));
        HttpHeaders responseHeaders = response.headers();
        responseHeaders.set(HttpHeaders.Names.CONTENT_TYPE, "text/xml");
        responseHeaders.set(HttpHeaders.Names.CONTENT_LENGTH, soapEnvelope.length());
        channelHandlerContext.channel().writeAndFlush(response);

    }

    private void handleRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest,
                               String contentType) throws HandlerException {
        String requestContent = fullHttpRequest.content().toString(Charset.defaultCharset());
        String contextUri = fullHttpRequest.getUri();
        InputStream inputStream = new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8));
        OutputStream outputStream = new ByteArrayOutputStream();

        MessageContext messageContext = createMessageContext(contextUri, contentType, inputStream, outputStream);
        try {
            AxisEngine.receive(messageContext);
        } catch (AxisFault axisFault) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "AxisEngine failed to process the message", axisFault);
        }

        ByteBuf responseContent;
        try {
            responseContent = Unpooled.wrappedBuffer(((ByteArrayOutputStream) outputStream)
                    .toString(StandardCharsets.UTF_8.name()).getBytes(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Unable to extract response", e);
        }

        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                responseContent);
        HttpHeaders responseHeaders = response.headers();
        responseHeaders.set(HttpHeaders.Names.CONTENT_TYPE, contentType);
        responseHeaders.set(HttpHeaders.Names.CONTENT_LENGTH, responseContent.readableBytes());
        channelHandlerContext.channel().writeAndFlush(response);

    }

    private MessageContext createMessageContext(String contextUri, String contentType, InputStream inputStream,
                                                OutputStream outputStream) throws HandlerException {
        MessageContext messageContext = DataHolder.getInstance().getConfigurationContext().createMessageContext();
        messageContext.setServerSide(true);
        messageContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
        messageContext.setProperty(MessageContext.TRANSPORT_OUT, outputStream);

        String endpoint = contextUri.substring(contextUri.indexOf("/services/") + 10);
        String serviceName = endpoint.substring(0, endpoint.indexOf('.'));

        AxisService axisService = DataHolder.getInstance().getConfigurationContext().getAxisConfiguration()
                .getServices().get(serviceName);
        messageContext.setAxisService(axisService);

        SOAPEnvelope soapEnvelope = null;
        try {
            soapEnvelope = createSOAPMessage(contentType, messageContext, inputStream);
            messageContext.setEnvelope(soapEnvelope);
        } catch (AxisFault axisFault) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create SOAP message from the request", axisFault);
        }
        return messageContext;
    }

    private SOAPEnvelope createSOAPMessage(String contentType,
                                           MessageContext messageContext,
                                           InputStream inputStream) throws AxisFault {
        SOAPBuilder soapBuilder = new SOAPBuilder();
        OMElement documentElement = soapBuilder.processDocument(inputStream, contentType, messageContext);

        SOAPEnvelope soapEnvelope;
        if (documentElement instanceof SOAPEnvelope) {
            soapEnvelope = (SOAPEnvelope) documentElement;
        } else {
            SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
            soapEnvelope = soapFactory.getDefaultEnvelope();
            if (documentElement != null) {
                soapEnvelope.getBody().addChild(documentElement);
            }
        }
        return soapEnvelope;
    }

    private String createSOAPErrorMessage(String faultCode, String faultString, String faultDetails,
                                          String contentType) {
        String errorMessage = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <soapenv:Body>\n" +
                "      <soapenv:Fault>\n" +
                "         <faultcode>" + faultCode + "</faultcode>\n" +
                "         <faultstring>" + faultString + "</faultstring>\n" +
                ((faultDetails != null && !faultDetails.isEmpty()) ? "         <detail>" + faultDetails + "<detail>\n" :
                        "         <detail/>\n") +
                "      </soapenv:Fault>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        return errorMessage;
    }
}
