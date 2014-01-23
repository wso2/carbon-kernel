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

package org.apache.axis2.util;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.builder.XOPAwareStAXOMBuilder;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.ApplicationXMLFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.SOAPMessageFormatter;
import org.apache.axis2.transport.http.XFormURLEncodedFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * MessageProcessorSelector is utility class which encapsulate MessageBuilder and
 * MessageFormatter selection logic.
 *
 * @since 1.7.0
 */
public class MessageProcessorSelector {

    private static final Log log = LogFactory.getLog(MessageProcessorSelector.class);

    /**
     * Initial work for a builder selector which selects the builder for a given message format
     * based on the the content type of the recieved message. content-type to builder mapping can be
     * specified through the Axis2.xml.
     *
     * @param type       content-type
     * @param msgContext the active MessageContext
     * @return the builder registered against the given content-type
     * @throws org.apache.axis2.AxisFault
     */
    public static Builder getMessageBuilder(String type, MessageContext msgContext)
            throws AxisFault {
        boolean useFallbackBuilder = false;
        AxisConfiguration configuration =
                msgContext.getConfigurationContext().getAxisConfiguration();
        Parameter useFallbackParameter = configuration.getParameter(Constants.Configuration.USE_DEFAULT_FALLBACK_BUILDER);
        if (useFallbackParameter != null) {
            useFallbackBuilder = JavaUtils.isTrueExplicitly(useFallbackParameter.getValue(), useFallbackBuilder);
        }

        String cType = getContentTypeForBuilderSelection(type, msgContext);
        Builder builder = configuration.getMessageBuilder(cType, useFallbackBuilder);
        if (builder != null) {
            // Check whether the request has a Accept header if so use that as the response
            // message type.
            // If thats not present,
            // Setting the received content-type as the messageType to make
            // sure that we respond using the received message serialization format.

            Object contentNegotiation = configuration
                    .getParameterValue(Constants.Configuration.ENABLE_HTTP_CONTENT_NEGOTIATION);
            if (JavaUtils.isTrueExplicitly(contentNegotiation)) {
                Map transportHeaders =
                        (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
                if (transportHeaders != null) {
                    String acceptHeader = (String) transportHeaders.get(HTTPConstants.HEADER_ACCEPT);
                    if (acceptHeader != null) {
                        int index = acceptHeader.indexOf(";");
                        if (index > 0) {
                            acceptHeader = acceptHeader.substring(0, index);
                        }
                        String[] strings = acceptHeader.split(",");
                        for (String string : strings) {
                            String accept = string.trim();
                            // We dont want dynamic content negotoatin to work on text.xml as its
                            // ambiguos as to whether the user requests SOAP 1.1 or POX response
                            if (!HTTPConstants.MEDIA_TYPE_TEXT_XML.equals(accept) &&
                                    configuration.getMessageFormatter(accept) != null) {
                                type = string;
                                break;
                            }
                        }
                    }
                }
            }

            msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, type);
        }
        return builder;
    }

    /**
     * Initial work for a builder selector which selects the builder for a given message format based on the the content type of the recieved message.
     * content-type to builder mapping can be specified through the Axis2.xml.
     *
     * @param msgContext
     * @return the builder registered against the given content-type
     * @throws AxisFault
     */
    public static MessageFormatter getMessageFormatter(MessageContext msgContext)
            throws AxisFault {
        MessageFormatter messageFormatter = null;
        String messageFormatString = getMessageFormatterProperty(msgContext);
        messageFormatString = getContentTypeForFormatterSelection(messageFormatString, msgContext);
        if (messageFormatString != null) {
            messageFormatter = msgContext.getConfigurationContext()
                    .getAxisConfiguration().getMessageFormatter(messageFormatString);
            if (log.isDebugEnabled()) {
                log.debug("Message format is: " + messageFormatString
                        + "; message formatter returned by AxisConfiguration: " + messageFormatter);
            }
        }
        if (messageFormatter == null) {
            messageFormatter = (MessageFormatter) msgContext.getProperty(Constants.Configuration.MESSAGE_FORMATTER);
            if (messageFormatter != null) {
                return messageFormatter;
            }
        }
        if (messageFormatter == null) {

            // If we are doing rest better default to Application/xml formatter
            if (msgContext.isDoingREST()) {
                String httpMethod = (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);
                if (Constants.Configuration.HTTP_METHOD_GET.equals(httpMethod) ||
                        Constants.Configuration.HTTP_METHOD_DELETE.equals(httpMethod)) {
                    return new XFormURLEncodedFormatter();
                }
                return new ApplicationXMLFormatter();
            } else {
                // Lets default to SOAP formatter
                //TODO need to improve this to use the stateless nature
                messageFormatter = new SOAPMessageFormatter();
            }
        }
        return messageFormatter;
    }

    public static StAXBuilder getAttachmentBuilder(MessageContext msgContext,
                                                    Attachments attachments, XMLStreamReader streamReader,
                                                    String soapEnvelopeNamespaceURI,
                                                    boolean isSOAP)
            throws OMException, XMLStreamException, FactoryConfigurationError {

        StAXBuilder builder = null;

        if (isSOAP) {
            if (attachments.getAttachmentSpecType().equals(
                    MTOMConstants.MTOM_TYPE)) {
                //Creates the MTOM specific MTOMStAXSOAPModelBuilder
                builder = new MTOMStAXSOAPModelBuilder(streamReader,
                        attachments, soapEnvelopeNamespaceURI);
                msgContext.setDoingMTOM(true);
            } else if (attachments.getAttachmentSpecType().equals(
                    MTOMConstants.SWA_TYPE)) {
                builder = new StAXSOAPModelBuilder(streamReader,
                        soapEnvelopeNamespaceURI);
            } else if (attachments.getAttachmentSpecType().equals(
                    MTOMConstants.SWA_TYPE_12)) {
                builder = new StAXSOAPModelBuilder(streamReader,
                        soapEnvelopeNamespaceURI);
            }

        }
        // To handle REST XOP case
        else {
            if (attachments.getAttachmentSpecType().equals(MTOMConstants.MTOM_TYPE)) {
                builder = new XOPAwareStAXOMBuilder(streamReader, attachments);

            } else if (attachments.getAttachmentSpecType().equals(MTOMConstants.SWA_TYPE)) {
                builder = new StAXOMBuilder(streamReader);
            } else if (attachments.getAttachmentSpecType().equals(MTOMConstants.SWA_TYPE_12)) {
                builder = new StAXOMBuilder(streamReader);
            }
        }

        return builder;
    }

    private static String getMessageFormatterProperty(MessageContext msgContext) {
        String messageFormatterProperty = null;
        Object property = msgContext
                .getProperty(Constants.Configuration.MESSAGE_TYPE);
        if (property != null) {
            messageFormatterProperty = (String) property;
        }
        if (messageFormatterProperty == null) {
            Parameter parameter = msgContext
                    .getParameter(Constants.Configuration.MESSAGE_TYPE);
            if (parameter != null) {
                messageFormatterProperty = (String) parameter.getValue();
            }
        }
        return messageFormatterProperty;
    }

    private static String getContentTypeForBuilderSelection(String type, MessageContext msgContext) {
        /**
         * Handle special case where content-type : text/xml and SOAPAction = null consider as
         * POX (REST) message not SOAP 1.1.
         *
         * it's required use the Builder associate with "application/xml" here but should not
         * change content type of current message.
         *
         */
        String cType = type;
        if (msgContext.getSoapAction() == null && HTTPConstants.MEDIA_TYPE_TEXT_XML.equals(type) && msgContext.isDoingREST()) {
            cType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        }
        return cType;
    }

    private static String getContentTypeForFormatterSelection(String type, MessageContext msgContext) {
        /**
         * Handle special case where content-type : text/xml and SOAPAction = null consider as
         * POX (REST) message not SOAP 1.1.
         *
         * 1.) it's required use the Builder associate with "application/xml" here but should not
         * change content type of current message.
         */
        String cType = type;
        if (msgContext.isDoingREST() && HTTPConstants.MEDIA_TYPE_TEXT_XML.equals(type)) {
            cType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_TEXT_XML);
        }
        return cType;
    }

}