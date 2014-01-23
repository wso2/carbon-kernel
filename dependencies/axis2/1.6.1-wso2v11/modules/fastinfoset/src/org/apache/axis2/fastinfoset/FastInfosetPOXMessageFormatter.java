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

package org.apache.axis2.fastinfoset;

import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class FastInfosetPOXMessageFormatter implements MessageFormatter {

	private static Log logger = LogFactory.getLog(FastInfosetMessageFormatter.class);
	
	/**
	 * Plain Fast Infoset message formatter doesn't need to handle SOAP. Hence do nothing.
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#formatSOAPAction(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.lang.String)
	 */
	public String formatSOAPAction(MessageContext messageContext,
			OMOutputFormat format, String soapAction) {

		return null;
	}

	/**
	 * Retrieves the raw bytes from the SOAP envelop.
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#getBytes(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat)
	 */
	public byte[] getBytes(MessageContext messageContext, OMOutputFormat format)
			throws AxisFault {
		//For POX drop the SOAP envelope and use the message body
		OMElement element = messageContext.getEnvelope().getBody().getFirstElement();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		try {
			//Creates StAX document serializer which actually implements the XMLStreamWriter
			XMLStreamWriter streamWriter = new StAXDocumentSerializer(outStream);
			//Since we drop the SOAP envelop we have to manually write the start document and the end document events
			streamWriter.writeStartDocument();
			element.serializeAndConsume(streamWriter);
			streamWriter.writeEndDocument();
			
			return outStream.toByteArray();
			
		} catch (XMLStreamException xmlse) {
			logger.error(xmlse.getMessage());
			throw new AxisFault(xmlse.getMessage(), xmlse);
		}
	}

	/**
	 * Returns the content type
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#getContentType(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.lang.String)
	 */
	public String getContentType(MessageContext messageContext,
			OMOutputFormat format, String soapAction) {
		String contentType = (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
		String encoding = format.getCharSetEncoding();
		
		//If the Content Type is not available with the property "Content Type" retrieve it from the property "Message Type"
		if (contentType == null) {
			contentType = (String) messageContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
		}

		if (encoding != null) {
			contentType += "; charset=" + encoding;
		}
	        
		return contentType;
	}

	/**
	 * Returns the target address to send the response
	 * FIXME This is very HTTP specific. What about other transport?
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#getTargetAddress(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.net.URL)
	 */
	public URL getTargetAddress(MessageContext messageContext,
			OMOutputFormat format, URL targetURL) throws AxisFault {
        String httpMethod =
            (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);

        URL targetAddress = targetURL; //Let's initialize to this
	    //if the http method is GET, parameters are attached to the target URL
	    if ((httpMethod != null)
	            && Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
	        String param = getParam(messageContext);
	
	        if (param.length() > 0) {
	            String returnURLFile = targetURL.getFile() + "?" + param;
	            try {
	                targetAddress = 
	                	new URL(targetURL.getProtocol(), targetURL.getHost(), targetURL.getPort(), returnURLFile);
	            } catch (MalformedURLException murle) {
	            	logger.error(murle.getMessage());
	                throw new AxisFault(murle.getMessage(), murle);
	            }
	        }
	    }
	    
	    return targetAddress;
	}

	/**
	 * Write the SOAP envelop to the given OutputStream.
	 * 
	 * @see org.apache.axis2.transport.MessageFormatter#writeTo(org.apache.axis2.context.MessageContext, org.apache.axiom.om.OMOutputFormat, java.io.OutputStream, boolean)
	 */
	public void writeTo(MessageContext messageContext, OMOutputFormat format,
			OutputStream outputStream, boolean preserve) throws AxisFault {
		//For POX drop the SOAP envelope and use the message body
		OMElement element = messageContext.getEnvelope().getBody().getFirstElement();
		
		try {
			//Create the StAX document serializer
			XMLStreamWriter streamWriter = new StAXDocumentSerializer(outputStream);
			//Since we drop the SOAP envelop we have to manually write the start document and the end document events			
			streamWriter.writeStartDocument();
			if (preserve) {
				element.serialize(streamWriter);
			} else {
				element.serializeAndConsume(streamWriter);
			}
			streamWriter.writeEndDocument();
		} catch (XMLStreamException xmlse) {
			logger.error(xmlse.getMessage());
			throw new AxisFault(xmlse.getMessage(), xmlse);
		}
	}
	
	/**
	 * Construct URL parameters like, "param1=value1&param2=value2"
	 * FIXME This is very HTTP specific. What about other transports
	 * 
	 * @param messageContext
	 * @return Formatted URL parameters
	 */
    private String getParam(MessageContext messageContext) {
        
    	OMElement dataOut = messageContext.getEnvelope().getBody().getFirstElement();
        Iterator it = dataOut.getChildElements();
        StringBuffer paramBuffer = new StringBuffer();
 
        while (it.hasNext()) {
            OMElement element = (OMElement) it.next();
            String parameter = element.getLocalName() + "=" + element.getText();
            paramBuffer.append(parameter);
            paramBuffer.append("&");
        }
        //We don't need a '&' at the end
        paramBuffer.deleteCharAt(paramBuffer.length() - 1);
        
        return paramBuffer.toString();
    }
}
