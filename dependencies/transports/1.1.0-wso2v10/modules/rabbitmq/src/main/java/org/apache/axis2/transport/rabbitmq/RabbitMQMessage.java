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

import org.apache.axis2.context.MessageContext;

import java.util.Map;

/**
 * Class which wraps an RabbitMQ AMQP message which is used in Axis2 Engine
 */
public class RabbitMQMessage {
    private String contentType;
    private String contentEncoding;
    private String correlationId;
    private String replyTo;
    private String messageId;
    private String soapAction;
    private Map<String,Object> headers;
    private byte body[];
    private long deliveryTag;

    public RabbitMQMessage(){

    }
    public RabbitMQMessage(MessageContext msgCtx) {
        this.soapAction = msgCtx.getSoapAction();
        this.messageId = msgCtx.getMessageID();
        this.replyTo = msgCtx.getReplyTo().toString();
        this.contentType = (String) msgCtx.getProperty(RabbitMQConstants.CONTENT_TYPE);
        this.correlationId = (String) msgCtx.getProperty(RabbitMQConstants.CORRELATION_ID);
        this.contentEncoding = (String) msgCtx.getProperty(RabbitMQConstants.CONTENT_ENCODING);
    }

    public byte[] getBody() {
        return body;
    }


    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setDeliveryTag(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public long getDeliveryTag() {
        return deliveryTag;
    }
}
