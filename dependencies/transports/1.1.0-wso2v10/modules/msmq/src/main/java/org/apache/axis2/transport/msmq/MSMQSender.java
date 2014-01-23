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

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.msmq.MSMQConnectionManager.ConnectionType;
import org.apache.axis2.transport.msmq.util.IMSMQClient;
import org.apache.axis2.transport.msmq.util.MSMQCamelClient;
import org.apache.axis2.transport.msmq.util.MSMQUtil;
import org.apache.axis2.transport.msmq.util.Message;
import org.apache.axis2.transport.msmq.util.MessageQueueException;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;

/**
 * Transport sender for MSMSQ
 * 
 * formats which will be supporting
 * 
 * msmq:msmqQueueName[?options]
 * ex: msmq:DIRECT=OS:localhost\\private$\\test?concurrentConsumers=1
 * msmq:DIRECT=OS:localhost\\private$\\test?deliveryPersistent=true&priority=5&
 * timeToLive=10
 */
public class MSMQSender extends AbstractTransportSender {

	
	

	@Override
	public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut) throws AxisFault {
		super.init(cfgCtx, transportOut);
		if (log.isDebugEnabled()) {
			log.info("MSMQ transport Sender initialized");
		}
		MSMQConnectionManager.init(transportOut, ConnectionType.SENDER);
	}

	/**
     * Performs the actual sending of the MSMQ message
     */
	@Override
	public void sendMessage(MessageContext msgCtx, String targetAddress, OutTransportInfo outTransportInfo) throws AxisFault {

		if (targetAddress == null) { // no queue name defined..return..
			return;
		}

		String queueName = targetAddress.substring(5, targetAddress.length());

		// TODO: get this property from .NET message client, i.e. in .NET
		// contentType or something
		String contentType = (String) msgCtx.getProperty(MSMQConstants.CONENT_TYPE_PROPERTY_PARAM); // message type of the incoming message
		if (contentType == null) {
			contentType = MSMQConstants.DEFAULT_CONTENT_TYPE; // TODO:fix sender content type
		}
		//Needs to synchronize as the JNI calls are not thread safe..
		synchronized (this) {
			sendOverMSMQ(msgCtx, queueName, contentType);
        }
		
	}

	private void sendOverMSMQ(MessageContext msgCtx, String queuName, String contentType) throws AxisFault {
		IMSMQClient mqClient = new MSMQCamelClient();
		Message message = null;
		try {
			message = createMSMQMessage(msgCtx, contentType, queuName);
			message.setLabel(contentType);
		} catch (AxisFault axisFault) {
			handleException("Error creaging the MSMQ message from the message context", axisFault);
		}

		// TODO: should we wait for a response, ok we need to support MSMQ
		// request/response scenario
		// get the reply queue name and start to receive from here. See
		// JMSSender.java:166 for more
		// information
		boolean waitForResponse = waitForSynchronousResponse(msgCtx);
	
//		Map<String, String> msmqPropertyMap = new HashMap<String, String>();
//		if (queuName.indexOf("?") > 0) {
//			MSMQUtil.getMsmsqPropertyByName(msmqPropertyMap, queuName);
//			queuName = queuName.substring(0, queuName.indexOf("?"));
//		}
		
		if(waitForResponse){
			try {
				String correlationId = "";
				if (msgCtx.getRelatesTo() != null) {
					correlationId = msgCtx.getRelatesTo().getValue();
				}else{
					correlationId = MSMQConstants.DEFAULT_MSG_CORRELATION_ID;// TODO: if we are having a one way message we don't have this
				}
				 
	            message.setCorrelationIdAsString(correlationId);
            } catch (UnsupportedEncodingException e) {
            	handleException("Error while setting up message Correlation",e);
            }
		}else{
			message.setCorrelationId(MSMQConstants.DEFAULT_MSG_CORRELATION_ID.getBytes());
		}
		
		try {
			try{
			  mqClient.create(queuName, "MSMQ-WSO2", false); //By default we are handling queues without transactional 
			}catch (MessageQueueException e) {
				 log.warn("Queue " + queuName + "  already there.");
			}
			mqClient.open(queuName,org.apache.axis2.transport.msmq.util.IMSMQClient.Access.SEND); //TODO: how to handle transactional messages
		} catch (AxisFault axisFault) {
			log.error("Queue " + queuName + "  already there.");
			handleException("Could not open the queu: " + queuName + " for lisinting", axisFault);
		}
		try {
			if (message != null) {
				mqClient.send(message);
			}
		} catch (AxisFault axisFault) {
			handleException("Cloud not send the message: " + axisFault);
		} finally {
			try {
				mqClient.close();
			} catch (AxisFault axisFault) {
				handleException("Cloud not close the queue: ", axisFault);
			}
		}

		if (waitForResponse) {
			// TODO: logic to be finalized on handling synchronous request/reply
			// for the given MSMQ message
			//MSMQClient replyClient = new MSMQClient();
	
		}

	}

	/**
	 * Generating MSMQ message wrapper in order to communicate with JNI
	 * @param msgCtx
	 * @param contentTypeProperty
	 * @param queuName
	 * @return
	 * @throws AxisFault
	 */
	private Message createMSMQMessage(MessageContext msgCtx, String messageContentType, String queuName) throws AxisFault {
		String msgBody = null;
		byte[] msgByteBody = null;
		String msgType = getProperty(msgCtx, MSMQConstants.MSMQ_MESSAGE_TYPE);
		//String msgLable = "L:" + queuName+"["+contentType+"]"; // TODO

		// check the first element of the SOAP body, do we have content wrapped
		// using the
		// default wrapper elements for binary
		// (BaseConstants.DEFAULT_BINARY_WRAPPER) or
		// text (BaseConstants.DEFAULT_TEXT_WRAPPER) ? If so, do not create SOAP
		// messages
		// for MSMQ but just get the payload in its native format
		String msmqMessageType = guessMessageType(msgCtx);

		if (msmqMessageType == null) {
			OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
			MessageFormatter messageFormatter = null;
			try {
				messageFormatter = MessageProcessorSelector.getMessageFormatter(msgCtx);
			} catch (AxisFault axisFault) {
				handleException("Unable to get the message formatter to use", axisFault);
			}

			String contentType = messageFormatter != null ?messageFormatter.getContentType(msgCtx, format, msgCtx.getSoapAction()):"";

			boolean useBytesMessage = msgType != null && MSMQConstants.MSMQ_BYTE_MESSAGE.equals(msgType) ||
			                                  contentType.indexOf(HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1;

			OutputStream out = null;
			StringWriter sw = null;
			if (useBytesMessage) {
				// TODO: handle MSMQ byte message here
			} else {
				sw = new StringWriter();
				try {
					out = new WriterOutputStream(sw, format.getCharSetEncoding());
				} catch (UnsupportedCharsetException ex) {
					handleException("Unsupported encoding " + format.getCharSetEncoding(), ex);
				}
			}

			try {
				if (out != null) {
					messageFormatter.writeTo(msgCtx, format, out, true);
					out.close();
				}
			} catch (IOException e) {
				handleException("IO Error while creating BytesMessage", e);
			}

			if (!useBytesMessage) {
				msgBody = sw.toString();
			}

		} else if (MSMQConstants.MSMQ_BYTE_MESSAGE.equals(msmqMessageType)) {
			// TODO. handle .net byte messages here.

		} else if (MSMQConstants.MSMQ_TEXT_MESSAGE.equals(msmqMessageType)) {
			msgBody = msgCtx.getEnvelope().getBody().getFirstChildWithName(BaseConstants.DEFAULT_TEXT_WRAPPER).getText();
		}

		try {
			Message message = new Message(msgBody !=null?msgBody:"", "", ""); //Keep message correlation empty will be deciding later on the process
			return message;
		} catch (UnsupportedEncodingException e) {
			log.error("Unsported message has been received: ", e);
			handleEexception("Unsported message has been received: ", e);
		}

		return null;
	}

	/**
	 * Based on the message payload body type invoking the message type
	 * 
	 * @param msgContext
	 * @return
	 */
	private String guessMessageType(MessageContext msgContext) {
		OMElement firstChild = msgContext.getEnvelope().getBody().getFirstElement();
		if (firstChild != null) {
			if (BaseConstants.DEFAULT_BINARY_WRAPPER.equals(firstChild.getQName())) {
				return MSMQConstants.MSMQ_BYTE_MESSAGE;
			} else if (BaseConstants.DEFAULT_TEXT_WRAPPER.equals(firstChild.getQName())) {
				return MSMQConstants.MSMQ_TEXT_MESSAGE;
			}
		}
		return null;
	}

	private String getProperty(MessageContext mc, String key) {
		return (String) mc.getProperty(key);
	}

	private void handleEexception(String msg, Exception e) throws AxisFault {
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}

}
