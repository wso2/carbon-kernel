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
 * 
 */
package org.apache.axis2.transport.msmq.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.format.TextMessageBuilder;
import org.apache.axis2.format.TextMessageBuilderAdapter;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.msmq.MSMQConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MSMQUtil {
    private static final Log log = LogFactory.getLog(MSMQUtil.class);

    public static void setSOAPEnvelope(Message message, MessageContext msgContext, String contentType)
            throws AxisFault {
        if (contentType == null) {
            contentType = "text/plain"; // TODO;we only support text/plain
            if (log.isDebugEnabled()) {
                log.debug("No content type specified; assuming " + contentType);
            }
        }
        int index = contentType.indexOf(';');
        String type = index > 0 ? contentType.substring(0, index) : contentType;
        Builder builder = BuilderUtil.getBuilderFromSelector(type, msgContext);
        String messageBody  =null;
        try {
	        messageBody = message.getBodyAsString();
        } catch (UnsupportedEncodingException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

        if (builder == null) {
            if (log.isDebugEnabled()) {
                log.debug("No message builder found for type' " + type + ".Using SOAP builder");
            }
            builder = new SOAPBuilder();
        }

        OMElement documentElement;
        // TODO: we need to handle the message types separately. Assume text message builder format
        TextMessageBuilder textMessageBuilder;
        if (builder instanceof TextMessageBuilder) {
            textMessageBuilder = (TextMessageBuilder) builder;
        } else {
            textMessageBuilder = new TextMessageBuilderAdapter(builder);
        }
        documentElement = textMessageBuilder.processDocument(messageBody, contentType, msgContext);
        msgContext.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
    }

    public static Map<String,Object> getTransportHeaders(Message message){
        Map<String, Object> map = new HashMap<String, Object>();
        if(message.getCorrelationId() != null){
            map.put(MSMQConstants.MSMQ_CORRELATION_ID, message.getCorrelationId());
        }
        // TODO: add the other properties
        return map;
    }
    
    public static String getMsmsqPropertyByName(Map<String,String> msmsqAdditionalPropertyMap , String queueName) {
	    int in = queueName.indexOf("?");
	    String propertyString=queueName.substring(in+1, queueName.length());
	    String [] properties =propertyString.split("&");
	    if(properties != null){
	    	for(String p : properties){
	    	  int index = p.indexOf("=");
	    	  String propertyName = p.substring(0, index);
	    	  String value  = p.substring(index+1,p.length());
	    	  msmsqAdditionalPropertyMap.put(propertyName, value);
	    	}
	    }
	    return null;
    }

}
