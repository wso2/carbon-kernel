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

import java.io.UnsupportedEncodingException;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.msmq.util.MSMQUtil;
import org.apache.axis2.transport.msmq.util.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the in coming message context retrieved via the MSMQ listener and
 * passing to the axis transport sender
 * and will be delivered to the end point defined
 * 
 */
public class MSMQMessageReceiver {
	
	private static Log log = LogFactory.getLog(MSMQMessageReceiver.class);
	
	private MSMQListener msmqListener = null;
	
	final MSMQEndpoint endpoint;
	
	private String destinationQueueName = null;

	MSMQMessageReceiver(MSMQListener msmqListener, String destination, MSMQEndpoint endpoint) {
		this.msmqListener = msmqListener;
		this.destinationQueueName = destination;
		this.endpoint = endpoint;
	}

	public boolean onMessage(Message message) {
		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("Received a new MSMQ message for queue: ").append(endpoint.getMsmqDestinationQueueName());
			sb.append("\nDestination     : ").append(endpoint.getMsmqDestinationQueueName());
			try {
				sb.append("\nCorrelation ID  : ").append(message.getCorrelationIdAsString());
				sb.append("\nMessage         :").append(message.getBodyAsString());
			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported message received: ", e);
			}
		}
		boolean sucessful = false;
		try {
			sucessful = processThroughEngine(message);
		} catch (AxisFault axisFault) {
			log.error("Cloud not pocess the message: ", axisFault);
		}
		return sucessful;
	}

	/**
	 * Set up message properties to header as it received as MSMQ message properties
	 * 
	 * @param message
	 * @return
	 * @throws AxisFault
	 */
	private boolean processThroughEngine(Message message) throws AxisFault {
		// TODO: this only support text messages, need to improve it for binay
		// messages
		
		String contentType = message.getLabel();
		if (log.isDebugEnabled()) {
			log.info("Content Type of the message is : " + contentType);
		}
		MessageContext msgContext = endpoint.createMessageContext();
		SOAPEnvelope soapEnvelope;

		if (message.getCorrelationId() != null) {
			msgContext.setProperty(MSMQConstants.MSMQ_CORRELATION_ID, message.getCorrelationId());
		}
		// TODO: get the contentType from the MSMQ message
		/*
		 * ContentTypeInfo contentTypeInfo =
		 * endpoint.getContentTypeRuleSet().getContentTypeInfo(message);
		 */

		MSMQUtil.setSOAPEnvelope(message, msgContext, contentType);
		soapEnvelope = msgContext.getEnvelope();
		msmqListener.handleIncomingMessage(msgContext, MSMQUtil.getTransportHeaders(message), null, contentType);
		return true;
	}
}
