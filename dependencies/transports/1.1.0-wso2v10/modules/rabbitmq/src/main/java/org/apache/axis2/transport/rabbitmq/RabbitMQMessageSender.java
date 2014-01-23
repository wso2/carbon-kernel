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

package org.apache.axis2.transport.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.rabbitmq.utils.RabbitMQUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Class that performs the actual sending of a RabbitMQ AMQP message,
 */

public class RabbitMQMessageSender {
    private static final Log log = LogFactory.getLog(RabbitMQMessageSender.class);

    private Connection connection = null;
    private String targetEPR = null;
    private Hashtable<String, String> properties;

    /**
     * Create a RabbitMQSender using a ConnectionFactory and target EPR
     *
     * @param factory   the ConnectionFactory
     * @param targetEPR the targetAddress
     */
    public RabbitMQMessageSender(ConnectionFactory factory, String targetEPR) {
        try {
            this.connection = factory.getConnectionPool();
        } catch (IOException e) {
            handleException("Error while creating connection pool", e);
        }
        this.targetEPR = targetEPR;
        if (!targetEPR.startsWith(RabbitMQConstants.RABBITMQ_PREFIX)) {
            handleException("Invalid prefix for a AMQP EPR : " + targetEPR);
        } else {
            this.properties = BaseUtils.getEPRProperties(targetEPR);
        }
    }

    /**
     * Perform actual send of RabbitMQ AMQP message to the destination
     *
     * @param message    the RabbitMQ AMQP message
     * @param msgContext the Axis2 MessageContext
     */
    public void send(RabbitMQMessage message, MessageContext msgContext) throws
                                                                         AxisRabbitMQException {

        String queueName = properties.get(RabbitMQConstants.QUEUE_NAME);
        String exchangeName = null;
        if (connection != null) {
            if (queueName != null) {
                Channel channel = null;
                try {
                    channel = connection.createChannel();
                    channel.queueDeclare(queueName,
                            RabbitMQUtils.isDurableQueue(properties),
                            RabbitMQUtils.isExclusiveQueue(properties),
                            RabbitMQUtils.isAutoDeleteQueue(properties), null);

                    exchangeName = properties.get(RabbitMQConstants.EXCHANGE_NAME);

                    if (exchangeName != null && !exchangeName.equals("")) {
                        String exchangerType = properties.get(RabbitMQConstants.EXCHANGE_TYPE);
                        if (exchangerType != null) {
                            String durable = properties.get(RabbitMQConstants.EXCHANGE_DURABLE);
                            if (durable != null) {
                                channel.exchangeDeclare(exchangeName, exchangerType, Boolean.parseBoolean(durable));
                            } else {
                                channel.exchangeDeclare(exchangeName, exchangerType, true);
                            }
                        } else {
                            channel.exchangeDeclare(exchangeName, "direct", true);
                        }
                        String routeKey = properties.get(RabbitMQConstants.QUEUE_ROUTING_KEY);
                        if (routeKey != null) {
                            channel.queueBind(queueName, exchangeName, routeKey);
                        } else {
                            channel.queueBind(queueName, exchangeName, queueName);
                        }
                    } else {
                        exchangeName = "";
                    }

                    AMQP.BasicProperties.Builder builder= buildBasicProperties(message);

                    // set delivery mode
                    String deliveryModeString = properties.get(RabbitMQConstants.QUEUE_DELIVERY_MODE);
                    if (deliveryModeString != null) {
                        int deliveryMode = Integer.parseInt(deliveryModeString);
                        builder.deliveryMode(deliveryMode);
                    }

                    AMQP.BasicProperties basicProperties = builder.build();
                    OMOutputFormat format = BaseUtils.getOMOutputFormat(msgContext);
                    MessageFormatter messageFormatter = null;
                    try {
                        messageFormatter = MessageProcessorSelector.getMessageFormatter(msgContext);
                    } catch (AxisFault axisFault) {
                        throw new AxisRabbitMQException("Unable to get the message formatter to use",
                                                axisFault);
                    }

                    OutputStream out = null;
                    try {
                        out = new BytesMessageOutputStream(channel, queueName,
                                                           exchangeName, basicProperties);
                    } catch (UnsupportedCharsetException ex) {
                        handleException("Unsupported encoding " + format.getCharSetEncoding(), ex);
                    }
                    try {
                        messageFormatter.writeTo(msgContext, format, out, true);
                    } catch (IOException e) {
                        handleException("IO Error while creating BytesMessage", e);
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }

                } catch (IOException e) {
                    handleException("Error while publishing message to queue ", e);
                }
            } else {
                throw new AxisRabbitMQException("AMQP queue name in not correctly defined");
            }
        }
    }

    /**
     * Close the connection
     */
    public void close() {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch (IOException e) {
                handleException("Error while closing the connection ..", e);
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Build and populate the AMQP.BasicProperties using the RabbitMQMessage
     *
     * @param message the RabbitMQMessage to be used to get the properties
     * @return AMQP.BasicProperties object
     */
    private AMQP.BasicProperties.Builder buildBasicProperties(RabbitMQMessage message) {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties().builder();
        builder.messageId(message.getMessageId());
        builder.contentType(message.getContentType());
        builder.replyTo(message.getReplyTo());
        builder.correlationId(message.getCorrelationId());
        builder.contentEncoding(message.getContentEncoding());
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(RabbitMQConstants.SOAP_ACTION, message.getSoapAction());
        builder.headers(headers);
        return builder;
    }

    private void handleException(String s) {
        log.error(s);
        throw new AxisRabbitMQException(s);
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new AxisRabbitMQException(message, e);
    }

}
