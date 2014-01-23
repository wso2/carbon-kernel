/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.axis2.transport.rabbitmq.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.rabbitmq.RabbitMQConstants;
import org.apache.axis2.transport.rabbitmq.RabbitMQMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class RabbitMQUtils {

    private static final Log log = LogFactory.getLog(RabbitMQUtils.class);

    public static Connection createConnection(ConnectionFactory factory) throws IOException {
        Connection connection = factory.newConnection();
        return connection;
    }

    public static String getProperty(MessageContext mc, String key) {
        return (String) mc.getProperty(key);
    }

    public static void setSOAPEnvelope(RabbitMQMessage message, MessageContext msgContext,
                                       String contentType) throws AxisFault {

        int index = contentType.indexOf(';');
        String type = index > 0 ? contentType.substring(0, index) : contentType;
        Builder builder = BuilderUtil.getBuilderFromSelector(type, msgContext);
        if (builder == null) {
            if (log.isDebugEnabled()) {
                log.debug("No message builder found for type '" + type + "'. Falling back to SOAP.");
            }
            builder = new SOAPBuilder();
        }

        OMElement documentElement;
        String charSetEnc = null;
        try {
            if (contentType != null) {
                charSetEnc = new ContentType(contentType).getParameter("charset");
            }
        } catch (ParseException ex) {
            // ignore
        }
        msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

        documentElement = builder.processDocument(
                new ByteArrayInputStream(message.getBody()), contentType,
                msgContext);


        msgContext.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
    }

    public static String getSOAPActionHeader(RabbitMQMessage message) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static Map getTransportHeaders(RabbitMQMessage message) {
        Map<String, Object> map = new HashMap<String, Object>();

        // correlation ID
        try {
            if (message.getCorrelationId() != null) {
                map.put(RabbitMQConstants.CORRELATION_ID, message.getCorrelationId());
            }
        } catch (Exception ignore) {
        }

        // if a AMQP message ID is found
        try {
            if (message.getMessageId() != null) {
                map.put(RabbitMQConstants.MESSAGE_ID, message.getMessageId());
            }
        } catch (Exception ignore) {
        }


        // replyto destination name
        try {
            if (message.getReplyTo() != null) {
                String dest = message.getReplyTo();
                map.put(RabbitMQConstants.RABBITMQ_REPLY_TO, dest);
            }
        } catch (Exception ignore) {
        }

        // any other transport properties / headers
        Map<String, Object> headers = message.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (String headerName : headers.keySet()) {
                map.put(headerName, headers.get(headerName));
            }
        }

        return map;
    }


    public static boolean isDurableQueue(Hashtable<String, String> properties) {
        String durable = properties.get(RabbitMQConstants.QUEUE_DURABLE);
        return durable != null && Boolean.parseBoolean(durable);
    }

    public static boolean isExclusiveQueue(Hashtable<String, String> properties) {
        String exclusive = properties.get(RabbitMQConstants.QUEUE_EXCLUSIVE);
        return exclusive != null && Boolean.parseBoolean(exclusive);
    }

    public static boolean isAutoDeleteQueue(Hashtable<String, String> properties) {
        String autoDelete = properties.get(RabbitMQConstants.QUEUE_AUTO_DELETE);
        return autoDelete != null && Boolean.parseBoolean(autoDelete);
    }


}
