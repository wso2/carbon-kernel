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

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Hashtable;


/**
 * The RabbitMQOutTransportInfo is a holder of information to send an outgoing message
 * to a RabbitMQ AMQP destination.
 */

public class RabbitMQOutTransportInfo implements OutTransportInfo {

    private static final Log log = LogFactory.getLog(RabbitMQOutTransportInfo.class);

    private String targetEPR = null;

    private Hashtable<String,String> properties = null;

    private String contentTypeProperty;

    private ConnectionFactory connectionFactory;
    private String replyTo;


    /**
     * Creates and instance using the given URL
     *
     * @param targetEPR the target EPR
     */
    public RabbitMQOutTransportInfo(String targetEPR) {
        this.targetEPR = targetEPR;
        if (!targetEPR.startsWith(RabbitMQConstants.RABBITMQ_PREFIX)) {
            handleException("Invalid prefix for a AMQP EPR : " + targetEPR);
        } else {
            properties = BaseUtils.getEPRProperties(targetEPR);
            contentTypeProperty = properties.get(RabbitMQConstants.CONTENT_TYPE_PROPERTY_PARAM);
        }
    }

    /**
     * Creates an instance using the given connection factory and destination
     *
     * @param connectionFactory   the connection factory
     * @param replyTo             the destination
     * @param contentTypeProperty the content type
     */
    public RabbitMQOutTransportInfo(ConnectionFactory connectionFactory, String replyTo,
                                    String contentTypeProperty) {
        this.connectionFactory = connectionFactory;
        this.replyTo = replyTo;
        this.contentTypeProperty = contentTypeProperty;
    }

    private void handleException(String s) {
        log.error(s);
        throw new AxisRabbitMQException(s);
    }

    public Hashtable<String, String> getProperties() {
        return properties;
    }

    public void setContentType(String contentTypeProperty) {
        this.contentTypeProperty = contentTypeProperty;
    }
}
