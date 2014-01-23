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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream wrapper that writes the message content to RabbitMQ out channel
 */
public class BytesMessageOutputStream extends OutputStream {

    private final Channel channel;
    private final String exchangeName;
    private final String queueName;
    private final AMQP.BasicProperties basicProperties;

    public BytesMessageOutputStream(Channel channel, String queueName, String exchangeName,
                                    AMQP.BasicProperties basicProperties) {
        this.channel = channel;
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.basicProperties = basicProperties;
    }

    @Override
    public void write(int i) throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] messageBody = b;
        if (b.length > len) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(b, off, len);
            messageBody = bos.toByteArray();
        }
        try {
            channel.basicPublish(exchangeName, queueName, basicProperties, messageBody);
        } finally {
            channel.close();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            channel.basicPublish(exchangeName, queueName, basicProperties, b);
        } finally {
            channel.close();
        }
    }
}
