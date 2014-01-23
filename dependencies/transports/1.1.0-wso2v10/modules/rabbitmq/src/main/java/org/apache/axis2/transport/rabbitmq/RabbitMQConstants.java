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


public class RabbitMQConstants {
    public static final String CONTENT_TYPE_PROPERTY_PARAM = "rabbitmq.transport.ContentTypeProperty";
    public static final String RABBITMQ_REPLY_TO = "RABBITMQ_REPLY_TO";
    public static final String RABBITMQ_PREFIX = "rabbitmq";
    public static final String RABBITMQ_CON_FAC = "rabbitmq.connection.factory";

    public static final String SERVER_HOST_NAME = "rabbitmq.server.host.name";
    public static final String SERVER_PORT = "rabbitmq.server.port";
    public static final String SERVER_USER_NAME = "rabbitmq.server.user.name";
    public static final String SERVER_PASSWORD = "rabbitmq.server.password";
    public static final String SERVER_VIRTUAL_HOST = "rabbitmq.server.virtual.host";

    public static final String CORRELATION_ID = "rabbitmq.message.correlation.id";
    public static final String MESSAGE_ID = "rabbitmq.message.id";
    public static final String CONTENT_TYPE = "rabbitmq.message.content.type";
    public static final String CONTENT_ENCODING = "rabbitmq.message.content.encoding";

    public static final String SOAP_ACTION = "SOAP_ACTION";

    public static final String EXCHANGE_NAME = "rabbitmq.exchange.name";
    public static final String EXCHANGE_TYPE = "rabbitmq.exchange.type";
    public static final String EXCHANGE_DURABLE = "rabbitmq.exchange.durable";

    public static final String QUEUE_NAME = "rabbitmq.queue.name";
    public static final String QUEUE_DURABLE = "rabbitmq.queue.durable";
    public static final String QUEUE_EXCLUSIVE = "rabbitmq.queue.exclusive";
    public static final String QUEUE_AUTO_DELETE = "rabbitmq.queue.auto.delete";
    public static final String QUEUE_AUTO_ACK = "rabbitmq.queue.auto.ack";
    public static final String QUEUE_ROUTING_KEY = "rabbimq.queue.routing.key";
    public static final String QUEUE_DELIVERY_MODE = "rabbitmq.queue.delivery.mode";

    public static final String CONSUMER_TAG = "rabbitmq.consumer.tag";

}
