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
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.bridge.CarbonAxis2Bridge;
import org.wso2.carbon.axis2.runtime.bridge.ResponseStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

            ServiceReference serviceReference = DataHolder.getInstance().getBundleContext()
                    .getServiceReference(CarbonAxis2Bridge.class);
            CarbonAxis2Bridge carbonAxis2Bridge = (CarbonAxis2Bridge) DataHolder.getInstance().getBundleContext()
                    .getService(serviceReference);

            String requestUri = fullHttpRequest.getUri();
            String contentType = fullHttpRequest.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            String requestContent = fullHttpRequest.content().toString(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(requestContent
                    .getBytes(StandardCharsets.UTF_8));
            OutputStream outputStream = new ByteArrayOutputStream();
            ResponseStatus responseStatus = carbonAxis2Bridge.processMessage(inputStream, outputStream,
                    contentType, null, requestUri);

            if (ResponseStatus.READY == responseStatus) {
                ByteBuf responseContent;
                try {
                    responseContent = Unpooled.wrappedBuffer(((ByteArrayOutputStream) outputStream)
                            .toString(StandardCharsets.UTF_8.name()).getBytes(StandardCharsets.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            "Unable to extract response", e);
                }

                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                        responseContent);
                HttpHeaders responseHeaders = response.headers();
                responseHeaders.set(HttpHeaders.Names.CONTENT_TYPE, contentType);
                responseHeaders.set(HttpHeaders.Names.CONTENT_LENGTH, responseContent.readableBytes());
                channelHandlerContext.channel().writeAndFlush(response);
            } else {
                throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error in response status");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        String soapEnvelope;
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
