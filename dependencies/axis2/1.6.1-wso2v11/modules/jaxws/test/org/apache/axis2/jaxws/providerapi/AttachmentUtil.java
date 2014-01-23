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

package org.apache.axis2.jaxws.providerapi;

import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * This will serve as a helper class for attachments utility methods. All methods
 * are intended to be referenced staticly.
 *
 */
public class AttachmentUtil {
    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope";
    public static final String SOAP12_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";

    public static final String MU_TEXT = "soap message mustUnderstand header request";
    public static final String UNDERSTOOD_MU_TEXT = "understood headers soap message mustUnderstand header request";
    public static final String TEXT = "soap message request";
    public static final String VALUE = "value";
    public static final String VALUE_NODE = "<"+VALUE+">";
    public static final String VALUE_NODE_SLASH = "</"+VALUE+">";
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String MUHEADER_CLIENT = "ns1:muclient";
    public static final String MUHEADER_SERVER = "ns1:muserver";
    public static final String MUHEADER_CLIENT_UNDERSTOOD = "ns1:muclientunderstood";
    public static final String MUHEADER_SERVER_UNDERSTOOD = "ns1:muserverunderstood";
    public static final String msgEnvMU = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Header>" +
                "<"+MUHEADER_CLIENT+" xmlns:ns1=\"http://ws.apache.org/axis2\" soapenv:mustUnderstand=\"1\">MUinfo</"+MUHEADER_CLIENT+">" +
            "</soapenv:Header>" +
            "<soapenv:Body>" +
                "<ns1:invoke xmlns:ns1=\"http://ws.apache.org/axis2\">" +
                    VALUE_NODE +
                    MU_TEXT +
                    VALUE_NODE_SLASH +
                "</ns1:invoke>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";
    
    public static final String msgEnv = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>" +
                "<ns1:invoke xmlns:ns1=\"http://ws.apache.org/axis2\">" +
                    VALUE_NODE +
                    MU_TEXT +
                    VALUE_NODE_SLASH +
                "</ns1:invoke>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";
    
    public static final String msgEnvPlain = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>" +
                "<ns1:invoke xmlns:ns1=\"http://ws.apache.org/axis2\">" +
                    VALUE_NODE +
                    TEXT +
                    VALUE_NODE_SLASH +
                "</ns1:invoke>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";

    public static final String msgEnvMU_understood = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Header>" +
                "<"+MUHEADER_CLIENT_UNDERSTOOD+" xmlns:ns1=\"http://ws.apache.org/axis2\" soapenv:mustUnderstand=\"1\">MUinfo</"+MUHEADER_CLIENT_UNDERSTOOD+">" +
            "</soapenv:Header>" +
            "<soapenv:Body>" +
                "<ns1:invoke xmlns:ns1=\"http://ws.apache.org/axis2\">" +
                    VALUE_NODE +
                    UNDERSTOOD_MU_TEXT +
                    VALUE_NODE_SLASH +
                "</ns1:invoke>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";
    
    public static final String msgEnv_understood = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>" +
                "<ns1:invoke xmlns:ns1=\"http://ws.apache.org/axis2\">" +
                    VALUE_NODE +
                    UNDERSTOOD_MU_TEXT +
                    VALUE_NODE_SLASH +
                "</ns1:invoke>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";
    
    /**
     * Store a given image to an Image output stream
     * @param mimeType
     * @param image
     * @param os
     * @throws Exception
     */
    public static void storeImage(String mimeType, Image image, OutputStream os) throws Exception {
        ImageWriter imageWriter = null;
        BufferedImage bufferedImage = (BufferedImage) image;
        
        Iterator iterator = javax.imageio.ImageIO.getImageWritersByMIMEType(mimeType);
        if (iterator.hasNext()) {
        	imageWriter = (ImageWriter) iterator.next();
        }
        ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(os);
        imageWriter.setOutput(ios);

        imageWriter.write(new IIOImage(bufferedImage, null, null));
        ios.flush();
        imageWriter.dispose();
    }
    
	/**
	 * Adapter method used to convert any type of Source to a String
	 * 
	 * @param input
	 * @return
	 */
	public static String toString(Source input) {

		if (input == null)
			return null;

		StringWriter writer = new StringWriter();
		Transformer trasformer;
		try {
			trasformer = TransformerFactory.newInstance().newTransformer();
			Result result = new StreamResult(writer);
			trasformer.transform(input, result);
		} catch (Exception e) {
			return null;
		}

		return writer.getBuffer().toString();
	}

	/**
	 * Adapter method used to convert any type of SOAPMessage to a String
	 * 
	 * @param input
	 * @return
	 */
	public static String toString(SOAPMessage input) {

		if (input == null)
			return null;

		Source result = null;
		try {
			result = input.getSOAPPart().getContent();
		} catch (SOAPException e) {
			e.printStackTrace();
		}

		return toString(result);
	}
	
	/**
	 * Method used to convert Strings to SOAPMessages
	 * 
	 * @param msgString
	 * @return
	 */
        public static SOAPMessage toSOAPMessage(String msgString) {

         if (msgString == null) return null;

         SOAPMessage message = null;
         try {
                 MessageFactory factory = null;

                 // Force the usage of specific MesasgeFactories
                 if (msgString.indexOf(SOAP11_NAMESPACE) >= 0) {
                         factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
                 } else {
                         factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
                 }
                 message = factory.createMessage();
                 message.getSOAPPart().setContent((Source) new StreamSource(new StringReader(msgString)));
                 message.saveChanges();
         } catch (SOAPException e) {
                 System.out.println("toSOAPMessage Exception encountered: " + e);
                 e.printStackTrace();
         }
         return message;     
    }
    
}

