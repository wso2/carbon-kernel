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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.JavaUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.ResponseStatus;
import org.wso2.carbon.axis2.runtime.util.CarbonMessageUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Axis2CarbonMessageProcessor implements CarbonMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(Axis2CarbonMessageProcessor.class);

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            ByteBuffer byteBuffer = carbonMessage.getMessageBody();

            Charset charset = Charset.defaultCharset();
            CharsetDecoder charsetDecoder = charset.newDecoder();
            CharBuffer charBuffer = charsetDecoder.decode(byteBuffer);
            stringBuilder.append(charBuffer.toString());

            if (carbonMessage.isEomAdded() && carbonMessage.isEmpty()) {
                break;
            }
        }

        String requestUri = (String) carbonMessage.getProperty(Constants.TO);
        String contentType = carbonMessage.getHeader(Constants.HTTP_CONTENT_TYPE);

        String requestIp = null;
        String host = carbonMessage.getHeader(Constants.HTTP_HOST);
//        if (host != null && !host.isEmpty() && host.contains(":")) {
//            requestIp = host.substring(0, host.indexOf(":"));
//        }
        // TODO : Need properly set the port in the listeners, as a temporary fix, sending the IP:PORT
        requestIp = host;

        InputStream inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        OutputStream outputStream = new ByteArrayOutputStream();

        Map<String, Object> properties = carbonMessage.getProperties();
        String httpMethod = (String) properties.get(Constants.HTTP_METHOD);
        if ("GET".equals(httpMethod)) {
            CarbonMessage responseMessage = processGetRequest(requestIp, requestUri, outputStream);
            carbonCallback.done(responseMessage);
            return true;
        }

        ResponseStatus responseStatus = CarbonAxis2MessageBridge.processMessage(inputStream, outputStream,
                contentType, null, requestUri);

        if (ResponseStatus.READY == responseStatus) {
            ByteBuffer responseContent = ByteBuffer.wrap(((ByteArrayOutputStream) outputStream).toByteArray());

            DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
            defaultCarbonMessage.addMessageBody(responseContent);
            defaultCarbonMessage.setHeader("Content-Length", Integer.toString(responseContent.limit()));
            defaultCarbonMessage.setEomAdded(true);
            carbonCallback.done(defaultCarbonMessage);
            return true;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Received response is not in the state of READY");
            }
            return false;
        }
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {

    }

    @Override
    public String getId() {
        return Axis2CarbonMessageProcessor.class.getName();
    }

    private CarbonMessage processGetRequest(String requestIp, String uri, OutputStream outputStream) {
        if (uri != null && uri.endsWith("?wsdl")) {
            /**
             * service name can be hierarchical (axis2/services/foo/1.0.0/Version?wsdl) or
             * normal (axis2/services/Version?wsdl).
             */

            String serviceName = uri.substring(uri.lastIndexOf('/') + 1, uri.length() - 5);

            HashMap services = DataHolder.getInstance().getConfigurationContext().getAxisConfiguration().getServices();
            AxisService service = (AxisService) services.get(serviceName);
            if (service != null) {
                boolean canExposeServiceMetadata = false;
                try {
                    canExposeServiceMetadata = canExposeServiceMetadata(service);
                } catch (IOException e) {
                    logger.error("Error while checking visibility of metadata", e);
                }
                if (canExposeServiceMetadata) {
                    try {
                        service.printWSDL(outputStream, requestIp);
                        return CarbonMessageUtil.createHttpCarbonResponse(outputStream, HttpStatus.SC_OK, null);
                    } catch (AxisFault axisFault) {
                        logger.error("Error while printing WSDL", axisFault);
                    }
                } else {
                    return CarbonMessageUtil.createHttpCarbonErrorResponse("FORBIDDEN", HttpStatus.SC_FORBIDDEN, null);
                }
            }
        }
        return CarbonMessageUtil.createHttpCarbonErrorResponse("INTERNAL_SERVER_ERROR",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
    }

    private boolean canExposeServiceMetadata(AxisService service) throws IOException {
        Parameter exposeServiceMetadata = service.getParameter("exposeServiceMetadata");
        if (exposeServiceMetadata != null &&
                JavaUtils.isFalseExplicitly(exposeServiceMetadata.getValue())) {
            return false;
        }
        return true;
    }
}
