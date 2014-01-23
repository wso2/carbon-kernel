/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.message;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.activation.DataHandler;
import javax.mail.internet.ContentType;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MIMEOutputUtils;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.testkit.client.ClientOptions;
import org.apache.axis2.transport.testkit.util.ContentTypeUtil;

public interface MessageEncoder<T,U> {
    MessageEncoder<XMLMessage,AxisMessage> XML_TO_AXIS =
        new MessageEncoder<XMLMessage,AxisMessage>() {

        public ContentType getContentType(ClientOptions options, ContentType contentType) {
            return contentType;
        }

        public AxisMessage encode(ClientOptions options, XMLMessage message) throws Exception {
            XMLMessage.Type type = message.getType();
            AxisMessage result = new AxisMessage();
            SOAPFactory factory;
            if (type == XMLMessage.Type.SOAP12 || type == XMLMessage.Type.SWA) {
                factory = OMAbstractFactory.getSOAP12Factory();
            } else {
                factory = OMAbstractFactory.getSOAP11Factory();
            }
            result.setMessageType(type.getContentType().toString());
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            envelope.getBody().addChild(message.getPayload());
            result.setEnvelope(envelope);
            if (type == XMLMessage.Type.SWA) {
                result.setAttachments(message.getAttachments());
            }
            return result;
        }
    };
    
    MessageEncoder<XMLMessage,byte[]> XML_TO_BYTE =
        new MessageEncoder<XMLMessage,byte[]>() {

        public ContentType getContentType(ClientOptions options, ContentType contentType) throws Exception {
            if (contentType.getBaseType().equals(XMLMessage.Type.SWA.getContentType().getBaseType())) {
                OMOutputFormat outputFormat = new OMOutputFormat();
                outputFormat.setMimeBoundary(options.getMimeBoundary());
                outputFormat.setRootContentId(options.getRootContentId());
                return new ContentType(outputFormat.getContentTypeForSwA(SOAP12Constants.SOAP_12_CONTENT_TYPE));
            } else {
                return ContentTypeUtil.addCharset(contentType, options.getCharset());
            }
        }

        public byte[] encode(ClientOptions options, XMLMessage message) throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OMOutputFormat outputFormat = new OMOutputFormat();
            outputFormat.setCharSetEncoding(options.getCharset());
            outputFormat.setIgnoreXMLDeclaration(true);
            if (message.getType() == XMLMessage.Type.SWA) {
                outputFormat.setMimeBoundary(options.getMimeBoundary());
                outputFormat.setRootContentId(options.getRootContentId());
                StringWriter writer = new StringWriter();
                message.getMessageElement().serializeAndConsume(writer);
                MIMEOutputUtils.writeSOAPWithAttachmentsMessage(writer, baos, message.getAttachments(), outputFormat);
            } else {
                message.getMessageElement().serializeAndConsume(baos, outputFormat);
            }
            return baos.toByteArray();
        }
    };
    
    MessageEncoder<XMLMessage,String> XML_TO_STRING =
        new MessageEncoder<XMLMessage,String>() {

        public ContentType getContentType(ClientOptions options, ContentType contentType) {
            return contentType;
        }

        public String encode(ClientOptions options, XMLMessage message) throws Exception {
            if (message.getType() == XMLMessage.Type.SWA) {
                throw new UnsupportedOperationException();
            }
            OMOutputFormat format = new OMOutputFormat();
            format.setIgnoreXMLDeclaration(true);
            StringWriter sw = new StringWriter();
            message.getMessageElement().serializeAndConsume(sw, format);
            return sw.toString();
        }
    };
    
    MessageEncoder<byte[],AxisMessage> BINARY_WRAPPER =
        new MessageEncoder<byte[],AxisMessage>() {

        public ContentType getContentType(ClientOptions options, ContentType contentType) {
            return contentType;
        }

        public AxisMessage encode(ClientOptions options, byte[] message) throws Exception {
            AxisMessage result = new AxisMessage();
            result.setMessageType("application/octet-stream");
            SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            OMElement wrapper = factory.createOMElement(BaseConstants.DEFAULT_BINARY_WRAPPER);
            DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(message));
            wrapper.addChild(factory.createOMText(dataHandler, true));
            envelope.getBody().addChild(wrapper);
            result.setEnvelope(envelope);
            return result;
        }
    };
    
    MessageEncoder<String,AxisMessage> TEXT_WRAPPER =
        new MessageEncoder<String,AxisMessage>() {

        public ContentType getContentType(ClientOptions options, ContentType contentType) {
            return contentType;
        }

        public AxisMessage encode(ClientOptions options, String message) throws Exception {
            AxisMessage result = new AxisMessage();
            result.setMessageType("text/plain");
            SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            OMElement wrapper = factory.createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER);
            wrapper.addChild(factory.createOMText(message));
            envelope.getBody().addChild(wrapper);
            result.setEnvelope(envelope);
            return result;
        }
    };
    
    MessageEncoder<String,byte[]> STRING_TO_BYTE =
        new MessageEncoder<String,byte[]>() {

        public ContentType getContentType(ClientOptions options, ContentType contentType) {
            return ContentTypeUtil.addCharset(contentType, options.getCharset());
        }

        public byte[] encode(ClientOptions options, String message) throws Exception {
            return message.getBytes(options.getCharset());
        }
    };
    
    ContentType getContentType(ClientOptions options, ContentType contentType) throws Exception;
    U encode(ClientOptions options, T message) throws Exception;
}
