/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.xmpp.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.xmpp.XMPPSender;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class XMPPPacketListener implements PacketListener {
	private static final Log log = LogFactory.getLog(XMPPPacketListener.class);
	private XMPPConnectionFactory xmppConnectionFactory = null;
	private ConfigurationContext configurationContext = null;
	private Executor workerPool = null;
    
    public final static String CONTENT_TYPE = "mail.contenttype";

    public XMPPPacketListener(XMPPConnectionFactory xmppConnectionFactory, ConfigurationContext configurationContext, Executor workerPool) {
		this.xmppConnectionFactory = xmppConnectionFactory;
		this.configurationContext = configurationContext;
		this.workerPool = workerPool;
	}

	/**
	 * This method gets triggered when server side gets a message
	 */
	public void processPacket(Packet packet) {
		log.debug("Received : "+packet.toXML());
		if(packet instanceof Message){
			workerPool.execute(new Worker(packet));			
		}
	}

	/**
	 * Creates message context using values received in XMPP packet
	 * @param packet
	 * @return MessageContext
	 * @throws AxisFault
	 */
	private MessageContext createMessageContext(Packet packet) throws AxisFault {
		Message message = (Message) packet;		

		Boolean isServerSide = (Boolean) message
				.getProperty(XMPPConstants.IS_SERVER_SIDE);
		String serviceName = (String) message
				.getProperty(XMPPConstants.SERVICE_NAME);
		String action = (String) message.getProperty(XMPPConstants.ACTION);
		MessageContext msgContext = null;

		TransportInDescription transportIn = configurationContext
				.getAxisConfiguration().getTransportIn("xmpp");
		TransportOutDescription transportOut = configurationContext
				.getAxisConfiguration().getTransportOut("xmpp");
		if ((transportIn != null) && (transportOut != null)) {
			msgContext = configurationContext.createMessageContext();
			msgContext.setTransportIn(transportIn);
			msgContext.setTransportOut(transportOut);
			if (isServerSide != null) {
				msgContext.setServerSide(isServerSide.booleanValue());
			}
			msgContext.setProperty(
					CONTENT_TYPE,
					"text/xml");
			msgContext.setProperty(
					Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
			msgContext.setIncomingTransportName("xmpp");

			Map services = configurationContext.getAxisConfiguration()
					.getServices();

			AxisService axisService = (AxisService) services.get(serviceName);
			msgContext.setAxisService(axisService);
			msgContext.setSoapAction(action);

			// pass the configurationFactory to transport sender
			msgContext.setProperty("XMPPConfigurationFactory",
					this.xmppConnectionFactory);

			if (packet.getFrom() != null) {
				msgContext.setFrom(new EndpointReference(packet.getFrom()));
			}
			if (packet.getTo() != null) {
				msgContext.setTo(new EndpointReference(packet.getTo()));
			}

			XMPPOutTransportInfo xmppOutTransportInfo = new XMPPOutTransportInfo();
			xmppOutTransportInfo
					.setConnectionFactory(this.xmppConnectionFactory);

			String packetFrom = packet.getFrom();
			if (packetFrom != null) {
				EndpointReference fromEPR = new EndpointReference(packetFrom);
				xmppOutTransportInfo.setFrom(fromEPR);
				xmppOutTransportInfo.setDestinationAccount(packetFrom);
			}

			// Save Message-Id to set as In-Reply-To on reply
			String xmppMessageId = packet.getPacketID();
			if (xmppMessageId != null) {
				xmppOutTransportInfo.setInReplyTo(xmppMessageId);
			}
			xmppOutTransportInfo.setSequenceID((String)message.getProperty(XMPPConstants.SEQUENCE_ID));
			msgContext.setProperty(
					org.apache.axis2.Constants.OUT_TRANSPORT_INFO,
					xmppOutTransportInfo);
			buildSOAPEnvelope(packet, msgContext);
		} else {
			throw new AxisFault("Either transport in or transport out is null");
		}
		return msgContext;
	}

    /**
     * builds SOAP envelop using message contained in packet
     * @param packet
     * @param msgContext
     * @throws AxisFault
     */
	private void buildSOAPEnvelope(Packet packet, MessageContext msgContext) throws AxisFault{
		Message message = (Message)packet;		
		String logMsg = "Trying to create " +
		"message content using XMPP message received :"+packet.toXML();
			
		String messageBody = StringEscapeUtils.unescapeXml(message.getBody());
		if(msgContext.isServerSide()){
			log.debug("Received Envelope : "+messageBody);
		}
		
		InputStream inputStream = new ByteArrayInputStream(messageBody.getBytes());
		SOAPEnvelope envelope = null;
		try {
			Object obj = message.getProperty(XMPPConstants.CONTAINS_SOAP_ENVELOPE); 
			if(obj != null && ((Boolean)obj).booleanValue()){
				String contentType = (String)message.getProperty(XMPPConstants.CONTENT_TYPE);
				if(contentType == null){
					throw new AxisFault("Can not Find Content type Property in the XMPP Message");
				}
				envelope = TransportUtils.createSOAPMessage(msgContext, inputStream, contentType);
				msgContext.setProperty(XMPPConstants.CONTAINS_SOAP_ENVELOPE, new Boolean(true));
			}else{
				//A text message has been received from a chat client
				//This message could either be a service call or a help command
				if(!(messageContainsCommandsFromChat(messageBody,msgContext))){
					envelope = createSOAPEnvelopeForRawMessage(msgContext, messageBody);					
				}				
			}
			if(envelope != null){
				msgContext.setEnvelope(envelope);				
			}
		}catch (OMException e) {
			log.error(logMsg, e);
			throw new AxisFault(logMsg);
		}catch (XMLStreamException e) {
			log.error(logMsg, e);
			throw new AxisFault(logMsg);
		}catch (FactoryConfigurationError e) {
			log.error(logMsg, e);
			throw new AxisFault(logMsg);
		}catch (AxisFault e){
			log.error(logMsg, e);
			throw new AxisFault(logMsg);
		}
	}

	/**
	 * In the direct chat client scenario, client can send commands & retrieve details
	 * on available services, operations,etc. This method checks if a client has sent
	 * such command. Only limited set of commands are available as of now. 
	 * @param message
	 * @param msgContext
	 * @return
	 */
	private boolean messageContainsCommandsFromChat(String message,MessageContext msgContext){
		boolean containsKnownCommand = false;
		if(message.trim().startsWith("help")){
			containsKnownCommand = true;						
		}else if(message.trim().startsWith("listServices")){
			containsKnownCommand = true;
		}else if (message.trim().startsWith("getOperations")){
			containsKnownCommand = true;
		}
		
		if(containsKnownCommand){
			msgContext.setProperty(XMPPConstants.MESSAGE_FROM_CHAT,message.trim());	
		}
		return containsKnownCommand;
	}
	
	/**
	 * Creates a SOAP envelope using details found in chat message.
	 * @param msgCtx
	 * @param chatMessage
	 * @return
	 */
	private SOAPEnvelope createSOAPEnvelopeForRawMessage(MessageContext msgCtx,String chatMessage)
	throws AxisFault{
		//TODO : need to add error handling logic 
    	String callRemoved = chatMessage.replaceFirst("call", "");
    	//extract Service name
    	String serviceName = callRemoved.trim().substring(0, callRemoved.indexOf(":")-1);
    	String operationName = callRemoved.trim().substring(callRemoved.indexOf(":"), callRemoved.indexOf("(")-1);

    	//Extract parameters from IM message
    	String parameterList = callRemoved.trim().substring(callRemoved.indexOf("("),callRemoved.trim().length()-1); 	
    	StringTokenizer st = new StringTokenizer(parameterList,",");
		MultipleEntryHashMap parameterMap = new MultipleEntryHashMap();
    	while(st.hasMoreTokens()){
    		String token = st.nextToken();
    		String name = token.substring(0, token.indexOf("="));
    		String value = token.substring(token.indexOf("=")+1);
    		parameterMap.put(name, value);
    	}
    	
		SOAPEnvelope envelope = null;
		try {
			msgCtx.setProperty(XMPPConstants.CONTAINS_SOAP_ENVELOPE, new Boolean(true));
			if(serviceName != null && serviceName.trim().length() > 0){
				AxisService axisService = msgCtx.getConfigurationContext().getAxisConfiguration().getService(serviceName);
				msgCtx.setAxisService(axisService);	
				
				AxisOperation axisOperation = axisService.getOperationBySOAPAction("urn:"+operationName);
				if(axisOperation != null){
					msgCtx.setAxisOperation(axisOperation);
				}
			}
	    	
			if(operationName != null && operationName.trim().length() > 0){
				msgCtx.setSoapAction("urn:"+operationName);
			}
			
			XMPPOutTransportInfo xmppOutTransportInfo = (XMPPOutTransportInfo)msgCtx.getProperty(
					org.apache.axis2.Constants.OUT_TRANSPORT_INFO);
			//This should be only set for messages received via chat.
			//TODO : need to read from a constant
			xmppOutTransportInfo.setContentType("xmpp/text");
			
			msgCtx.setServerSide(true);
			
			//TODO : need to support SOAP12 as well
			SOAPFactory soapFactory = new SOAP11Factory();
			envelope = BuilderUtil.buildsoapMessage(msgCtx, parameterMap,
                    soapFactory);
			//TODO : improve error handling & messages
		} catch (AxisFault e) {
			throw new AxisFault(e.getMessage());
		} catch (OMException e) {
			throw new AxisFault(e.getMessage());
		} catch (FactoryConfigurationError e) {
			throw new AxisFault(e.getMessage());
		}		
		return envelope;
	}

	/**
	 * The actual Runnable Worker implementation which will process the
	 * received XMPP messages in the worker thread pool
	 */
	class Worker implements Runnable {
		private Packet packet = null;
		Worker(Packet packet) {
			this.packet = packet;
		}

		public void run() {
			MessageContext msgCtx = null;
			try {
				msgCtx = createMessageContext(packet);
				Object obj = msgCtx.getProperty(XMPPConstants.CONTAINS_SOAP_ENVELOPE);
				if(obj != null && ((Boolean)obj).booleanValue()){
					if(msgCtx.isProcessingFault() && msgCtx.isServerSide()){
						AxisEngine.sendFault(msgCtx);
					}else{
						AxisEngine.receive(msgCtx);
					}					
				}else{
					//Send a text reply message to command received from chat client
					XMPPSender.processChatMessage(msgCtx);
				}
			} catch (AxisFault e) {
				log.error("Error occurred while sending message"+e);
   				if (msgCtx != null && msgCtx.isServerSide()) {
    				MessageContext faultContext;
					try {
						faultContext = MessageContextBuilder.createFaultMessageContext(msgCtx, e);
	    				AxisEngine.sendFault(faultContext);
					} catch (AxisFault e1) {
						log.error("Error occurred while creating SOAPFault message"+e1);
					}
   				}
			}
		}
	}
}
