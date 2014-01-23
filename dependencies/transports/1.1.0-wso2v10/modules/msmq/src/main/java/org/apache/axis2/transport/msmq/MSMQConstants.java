/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.transport.msmq;

public class MSMQConstants {
    public static final String TRANSPORT_NAME = "msmq";
    public static final String MSMQ_PREFIX = "msmq:";
    public static final String CONENT_TYPE_PROPERTY_PARAM = "messageType"; // TODO: put the content type of .NET messaging client here
    public static final String MSMQ_BYTE_MESSAGE = "MSMQ_BYTE_MESSAGE"; // TODO: set msmq byte message
    public static final String MSMQ_TEXT_MESSAGE = "MSMQ_TEXT_MESSAGE";
    public static final String MSMQ_CORRELATION_ID = "MS_MQ_CORRELATION_ID"; // TODO: only one messages are supported. Extend the capabilities for two way messaging
    public static final String MSMQ_MESSAGE_TYPE = "MSMQ_MESSAGE_TYPE"; //TODO: get the message type from MSMQ and set the property
    public static final String PARAM_DESTINATION = "transport.msmq.Destination";
    public static final String PARAM_CONTENT_TYPE = "transport.msmq.ContentType";
    public static final String MSMQ_SENDER_HOST="msmq.sender.host";
    public static final String MSMQ_RECEIVER_HOST="msmq.receiver.host";
    public static final String MSMQ_SENDER_QUEUE_NAME="msmq.sender.queue.name";
    public static final String MSMQ_RECEIVER_QUEUE_NAME="msmq.receiver.queue.name";
    public static final String DEFAULT_CONTENT_TYPE = "text/xml";
    public static final Integer MSMQ_RECEIVE_TIME_OUT=2000;
	public static final String DEFAULT_MSG_CORRELATION_ID = "LOOOONONEOOOOOOOOOO0";
 
}