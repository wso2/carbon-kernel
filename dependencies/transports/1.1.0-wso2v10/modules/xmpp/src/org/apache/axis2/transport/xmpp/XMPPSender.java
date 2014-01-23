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

package org.apache.axis2.transport.xmpp;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Version;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.xmpp.util.XMPPClientResponseManager;
import org.apache.axis2.transport.xmpp.util.XMPPConnectionFactory;
import org.apache.axis2.transport.xmpp.util.XMPPConstants;
import org.apache.axis2.transport.xmpp.util.XMPPOutTransportInfo;
import org.apache.axis2.transport.xmpp.util.XMPPServerCredentials;
import org.apache.axis2.transport.xmpp.util.XMPPUtils;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroupBase;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;

public class XMPPSender extends AbstractHandler implements TransportSender {
	static Log log = null;
	
    XMPPServerCredentials serverCredentials;    
    private XMPPClientResponseManager xmppClientSidePacketListener;
    private XMPPConnectionFactory defaultConnectionFactory;
	
    public XMPPSender() {
        log = LogFactory.getLog(XMPPSender.class);
        xmppClientSidePacketListener  = new XMPPClientResponseManager();
    }
    
	public void cleanup(MessageContext msgContext) throws AxisFault {	
	}

    /**
     * Initialize the transport sender by reading pre-defined connection factories for
     * outgoing messages. These will create sessions (one per each destination dealt with)
     * to be used when messages are being sent.
     * @param confContext the configuration context
     * @param transportOut the transport sender definition from axis2.xml
     * @throws AxisFault on error
     */	
	public void init(ConfigurationContext confContext,
			TransportOutDescription transportOut) throws AxisFault {
		//if connection details are available from axis configuration
		//use those & connect to jabber server(s)
		serverCredentials = new XMPPServerCredentials();
		getConnectionDetailsFromAxisConfiguration(transportOut);	
		
		defaultConnectionFactory = new XMPPConnectionFactory();
	}

	
	public void stop() {}

	public InvocationResponse invoke(MessageContext msgContext)
			throws AxisFault {
        String targetAddress = (String) msgContext.getProperty(
                Constants.Configuration.TRANSPORT_URL);
            if (targetAddress != null) {
                sendMessage(msgContext, targetAddress, null);
            } else if (msgContext.getTo() != null && !msgContext.getTo().hasAnonymousAddress()) {
                targetAddress = msgContext.getTo().getAddress();

                if (!msgContext.getTo().hasNoneAddress()) {
                    sendMessage(msgContext, targetAddress, null);
                } else {
                    //Don't send the message.
                    return InvocationResponse.CONTINUE;
                }
            } else if (msgContext.isServerSide()) {
                // get the out transport info for server side when target EPR is unknown
                sendMessage(msgContext, null,
                    (OutTransportInfo) msgContext.getProperty(Constants.OUT_TRANSPORT_INFO));
            }
            return InvocationResponse.CONTINUE;
	}

    /**
     * Send the given message over XMPP transport
     *
     * @param msgCtx the axis2 message context
     * @throws AxisFault on error
     */
    public void sendMessage(MessageContext msgCtx, String targetAddress,
            
        OutTransportInfo outTransportInfo) throws AxisFault {
		XMPPConnection xmppConnection = null;
		XMPPOutTransportInfo xmppOutTransportInfo = null;
		XMPPConnectionFactory connectionFactory;
		
		//if on client side,create connection to xmpp server
		if(msgCtx.isServerSide()){
		    xmppOutTransportInfo = (XMPPOutTransportInfo)msgCtx.getProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO);
		    connectionFactory = xmppOutTransportInfo.getConnectionFactory();
		}else{
		    getConnectionDetailsFromClientOptions(msgCtx);
		    connectionFactory = defaultConnectionFactory;
		}
		
		synchronized (this) {
            xmppConnection = connectionFactory.getXmppConnection();
            if(xmppConnection == null){
                connectionFactory.connect(serverCredentials);   
                xmppConnection = connectionFactory.getXmppConnection();
            }
        }
		
		Message message = new Message();
		Options options = msgCtx.getOptions();    	
    	String serviceName = XMPPUtils.getServiceName(targetAddress);    
    	
    	SOAPVersion version = msgCtx.getEnvelope().getVersion();
    	if(version instanceof SOAP12Version){
    		message.setProperty(XMPPConstants.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML+ "; action="+ msgCtx.getSoapAction());
    	}else{
    		message.setProperty(XMPPConstants.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_TEXT_XML);
    	}
    	
    	
		if (targetAddress != null) {
			xmppOutTransportInfo = new XMPPOutTransportInfo(targetAddress);
			xmppOutTransportInfo.setConnectionFactory(defaultConnectionFactory);
		} else if (msgCtx.getTo() != null &&
				!msgCtx.getTo().hasAnonymousAddress()) {
			//TODO 
		} else if (msgCtx.isServerSide()) {
			xmppOutTransportInfo = (XMPPOutTransportInfo)
			msgCtx.getProperty(Constants.OUT_TRANSPORT_INFO);
		}
		try{
		if(msgCtx.isServerSide()){
			message.setProperty(XMPPConstants.IS_SERVER_SIDE, new Boolean(false));
			message.setProperty(XMPPConstants.IN_REPLY_TO, xmppOutTransportInfo.getInReplyTo());
			message.setProperty(XMPPConstants.SEQUENCE_ID, xmppOutTransportInfo.getSequenceID());
		}else{
			//message is going to be processed on server side
			message.setProperty(XMPPConstants.IS_SERVER_SIDE,new Boolean(true));
			//we are sending a soap envelope as a message
			message.setProperty(XMPPConstants.CONTAINS_SOAP_ENVELOPE, new Boolean(true));
			message.setProperty(XMPPConstants.SERVICE_NAME, serviceName);
			String action = options.getAction();
			if (action == null) {
				AxisOperation axisOperation = msgCtx.getAxisOperation();
				if (axisOperation != null) {
					action = axisOperation.getSoapAction();
				}
			}
			if (action != null) {
				message.setProperty(XMPPConstants.ACTION, action);
			}
		}		
    	if(xmppConnection == null){
    		handleException("Connection to XMPP Server is not established.");    		
    	}
		
    	
    	
		//initialize the chat manager using connection
		ChatManager chatManager = xmppConnection.getChatManager();
		Chat chat = chatManager.createChat(xmppOutTransportInfo.getDestinationAccount(), null);
		
			boolean waitForResponse =
				msgCtx.getOperationContext() != null &&
				WSDL2Constants.MEP_URI_OUT_IN.equals(
						msgCtx.getOperationContext().getAxisOperation().getMessageExchangePattern());
			
			OMElement msgElement;			
			String messageToBeSent = "";
			
			if(XMPPConstants.XMPP_CONTENT_TYPE_STRING.equals(xmppOutTransportInfo.getContentType())){
				//if request is received from a chat client, whole soap envelope
				//should not be sent.
				OMElement soapBodyEle = msgCtx.getEnvelope().getBody();
				OMElement responseEle = soapBodyEle.getFirstElement();
				if(responseEle != null){
					msgElement = responseEle.getFirstElement();					
				}else{
					msgElement = responseEle;
				}
			}else{
				//if request received from a ws client whole soap envelope 
				//must be sent.
				msgElement = msgCtx.getEnvelope();
			}	
			messageToBeSent = msgElement.toString();
			message.setBody(messageToBeSent);
			
			String key = null;
			if(waitForResponse && !msgCtx.isServerSide()){
				PacketFilter filter = new PacketTypeFilter(message.getClass());				
				xmppConnection.addPacketListener(xmppClientSidePacketListener,filter);
				key = UUID.randomUUID().toString();
				xmppClientSidePacketListener.listenForResponse(key, msgCtx);
				message.setProperty(XMPPConstants.SEQUENCE_ID, key);
			}			

			chat.sendMessage(message);
			log.debug("Sent message :"+message.toXML());

			//If this is on client side, wait for the response from server.
			//Is this the best way to do this?
			if(waitForResponse && !msgCtx.isServerSide()){
			    xmppClientSidePacketListener.waitFor(key);
				//xmppConnection.disconnect();
			    log.debug("Received response sucessfully");
			}
			

		} catch (XMPPException e) {
			log.error("Error occurred while sending the message : "+message.toXML(),e);
			handleException("Error occurred while sending the message : "+message.toXML(),e);
		} catch (InterruptedException e) {
		    log.error("Error occurred while sending the message : "+message.toXML(),e);
            handleException("Error occurred while sending the message : "+message.toXML(),e);
        }finally{
//			if(xmppConnection != null && !msgCtx.isServerSide()){
//				xmppConnection.disconnect();
//			}
		}
    }	
    
    /**
     * Process message requests that came in through chat clients
     * @param msgCtx
     * @throws AxisFault
     */
    public static void processChatMessage(MessageContext msgCtx) throws AxisFault {
    	Object obj = msgCtx.getProperty(XMPPConstants.MESSAGE_FROM_CHAT);
    	if(obj != null){
        	String message = (String)obj;
        	String response = "";       	
        	
    		if(message.trim().startsWith("help")){
    			response = prepareHelpTextForChat();						
    		}else if(message.trim().startsWith("listServices")){
    			response = prepareServicesList(msgCtx);
    		}else if (message.trim().startsWith("getOperations")){
    			response = prepareOperationList(msgCtx,message);
        	}else{
        		//TODO add support for more help commands
        	}
    		sendChatMessage(msgCtx,response);       	
    	}
    }    
   
    /**
     * Prepares a list of service names deployed in current runtime
     * @param msgCtx
     * @return
     */
	private static String prepareOperationList(MessageContext msgCtx,String chatMessage) {
		StringBuffer sb = new StringBuffer();
		//extract service name
		String serviceName = chatMessage.replace("getOperations", "");
		serviceName = serviceName.replaceAll(" ", "");
		if(log.isDebugEnabled()){
			log.debug("Finding operations for service :"+ serviceName);	
		}
		
		try {
			AxisService service = msgCtx.getConfigurationContext().getAxisConfiguration().getService(serviceName);
			Iterator itrOperations = service.getOperations();
			int index = 1;
			while(itrOperations.hasNext()){
				AxisOperation operation = (AxisOperation)itrOperations.next();
				String parameterList = getParameterListForOperation(operation);				
				sb.append(index +"."+operation.getName().getLocalPart()+"("+parameterList+")"+"\n");
				index++;
			}
		} catch (AxisFault e) {
			log.error("Error occurred while retreiving AxisService : "+serviceName,e);
			sb.append("Error occurred while retrieving operations for service : "+serviceName);
		}		
		return sb.toString();
	}

	/**
	 * Retrieves list of parameter names & their type for a given operation
	 * @param operation
	 */
	private static String getParameterListForOperation(AxisOperation operation) {
		//Logic copied from BuilderUtil.buildsoapMessage(...)
		StringBuffer paramList = new StringBuffer();
		AxisMessage axisMessage =
		    operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
		XmlSchemaElement xmlSchemaElement = axisMessage.getSchemaElement();
		if(xmlSchemaElement != null){			
		    XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
		    if (schemaType instanceof XmlSchemaComplexType) {
		        XmlSchemaComplexType complexType = ((XmlSchemaComplexType)schemaType);
		        XmlSchemaParticle particle = complexType.getParticle();
		        if (particle instanceof XmlSchemaSequence || particle instanceof XmlSchemaAll) {
		            XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase)particle;
		            Iterator iterator = xmlSchemaGroupBase.getItems().getIterator();

		            while (iterator.hasNext()) {
		                XmlSchemaElement innerElement = (XmlSchemaElement)iterator.next();
		                QName qName = innerElement.getQName();
		                if (qName == null && innerElement.getSchemaTypeName()
		                        .equals(org.apache.ws.commons.schema.constants.Constants.XSD_ANYTYPE)) {
		                    break;
		                }
		                long minOccurs = innerElement.getMinOccurs();
		                boolean nillable = innerElement.isNillable();
		                String name =
		                        qName != null ? qName.getLocalPart() : innerElement.getName();
		                String type = innerElement.getSchemaTypeName().toString();
		                paramList.append(","+type +" " +name);
		            }
		        }
		   }	            	
		}
		//remove first ","
		String list = paramList.toString();		
		return list.replaceFirst(",", "");
	}

	
    /**
     * Prepares a list of service names deployed in current runtime
     * @param msgCtx
     * @return
     */
	private static String prepareServicesList(MessageContext msgCtx) {
		Map services = msgCtx.getConfigurationContext().getAxisConfiguration().getServices();
		StringBuffer sb = new StringBuffer();
		if(services != null && services.size() > 0){
			Iterator itrServiceNames = services.keySet().iterator();			
			int index = 1;
			while (itrServiceNames.hasNext()) {
				String serviceName = (String) itrServiceNames.next();
				sb.append(index+"."+serviceName+"\n");
				index++;
			}
		}
		return sb.toString();
	}
    
    
    /**
     * Generate help text for chat client
     * @return {@link String}
     */
    private static String prepareHelpTextForChat(){
    	StringBuffer helpText = new StringBuffer();
    	helpText.append("Following commands are supported :"+"\n");
    	helpText.append("-----------------------------------"+"\n");
    	helpText.append("1. listServices"+"\n");
    	helpText.append("2. getOperations <service-name>"+"\n");
    	helpText.append("3. call <service-name>:<operation>(<param1>,<param2>,...)"+"\n");
    	return helpText.toString();
    }
    
    /**
     * Replies to IM clients via a chat message. The reply contains the invocation response as a string. 
     * @param msgCtx
     * @param responseMsg
     * @throws AxisFault
     */
    private static void sendChatMessage(MessageContext msgCtx,String responseMsg) throws AxisFault {
    		XMPPConnection xmppConnection = null;
    		XMPPOutTransportInfo xmppOutTransportInfo = null;    		
    		Message message = new Message();
        	
   			xmppOutTransportInfo = (XMPPOutTransportInfo)msgCtx.getProperty(Constants.OUT_TRANSPORT_INFO);
   			if(xmppOutTransportInfo != null){
   				message.setProperty(XMPPConstants.IN_REPLY_TO, xmppOutTransportInfo.getInReplyTo());
   	    		xmppConnection = xmppOutTransportInfo.getConnectionFactory().getXmppConnection();
   	        	if(xmppConnection == null){
   	        		handleException("Connection to XMPP Server is not established.");    		
   	        	}   	    		
   			}else{
   				handleException("Could not find message sender details.");
   			}   			
    		
    		//initialize the chat manager using connection
    		ChatManager chatManager = xmppConnection.getChatManager();
    		Chat chat = chatManager.createChat(xmppOutTransportInfo.getDestinationAccount(), null);    		
    		try{   			
    		    message.setProperty(XMPPConstants.SEQUENCE_ID, 
    		            xmppOutTransportInfo.getSequenceID());
   				message.setBody(responseMsg);		
    			chat.sendMessage(message);
    			log.debug("Sent message :"+message.toXML());
    		} catch (XMPPException e) {
    			XMPPSender.handleException("Error occurred while sending the message : "+message.toXML(),e);
    		}
        }	

    /**
     * Extract connection details from axis2.xml's transportsender section
     * @param serverCredentials
     * @param transportOut
     */
    private void getConnectionDetailsFromAxisConfiguration(TransportOutDescription transportOut){
    	if(transportOut != null){
			Parameter serverUrl = transportOut.getParameter(XMPPConstants.XMPP_SERVER_URL);
			if (serverUrl != null) {
				serverCredentials.setServerUrl(Utils.getParameterValue(serverUrl));
			}
			
			Parameter userName = transportOut.getParameter(XMPPConstants.XMPP_SERVER_USERNAME);
			if (userName != null) {
				serverCredentials.setAccountName(Utils.getParameterValue(userName));
			}
		
			Parameter password = transportOut.getParameter(XMPPConstants.XMPP_SERVER_PASSWORD);
			if (password != null) {
				serverCredentials.setPassword(Utils.getParameterValue(password));
			}

			Parameter serverType = transportOut.getParameter(XMPPConstants.XMPP_SERVER_TYPE);			
			if (serverType != null) {
				serverCredentials.setServerType(Utils.getParameterValue(serverType));
			}	
			
			Parameter domainName = transportOut.getParameter(XMPPConstants.XMPP_DOMAIN_NAME);
			if (serverUrl != null) {
				serverCredentials.setDomainName(Utils.getParameterValue(domainName));
			}
		}
	}
	
	/**
	 * Extract connection details from client options
	 * @param serverCredentials
	 * @param msgContext
	 */
    private void getConnectionDetailsFromClientOptions(MessageContext msgContext){
    	Options clientOptions = msgContext.getOptions();

		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_USERNAME) != null){
			serverCredentials.setAccountName((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_USERNAME));
		}
		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_PASSWORD) != null){
			serverCredentials.setPassword((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_PASSWORD));
		}
		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_URL) != null){
			serverCredentials.setServerUrl((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_URL));
		}
		if (clientOptions.getProperty(XMPPConstants.XMPP_SERVER_TYPE) != null){
			serverCredentials.setServerType((String)clientOptions.getProperty(XMPPConstants.XMPP_SERVER_TYPE));
		}		
	}  
	
    private static void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
    private static void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }
}
