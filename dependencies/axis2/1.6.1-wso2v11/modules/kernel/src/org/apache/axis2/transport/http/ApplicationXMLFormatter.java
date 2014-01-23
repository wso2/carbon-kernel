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

package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Formates the request message as application/xml
 */
public class ApplicationXMLFormatter implements MessageFormatter {

    private static final Log log = LogFactory.getLog(ApplicationXMLFormatter.class);
    public byte[] getBytes(MessageContext 
                           messageContext, 
                           OMOutputFormat format) throws AxisFault {
        return getBytes(messageContext, format, false);
    }
    
    /**
     * Get the bytes for this message
     * @param messageContext
     * @param format
     * @param preserve (indicates if the OM should be preserved or consumed)
     * @return
     * @throws AxisFault
     */
    public byte[] getBytes(MessageContext messageContext, 
                           OMOutputFormat format, 
                           boolean preserve) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("start getBytes()");
            log.debug("  fault flow=" + 
                      (messageContext.getFLOW() == MessageContext.OUT_FAULT_FLOW));
        }
        try {
            OMElement omElement;

            // Find the correct element to serialize.  Normally it is the first element
            // in the body.  But if this is a fault, use the detail entry element or the 
            // fault reason.
            if (messageContext.getFLOW() == MessageContext.OUT_FAULT_FLOW) {
                SOAPFault fault = messageContext.getEnvelope().getBody().getFault();
                SOAPFaultDetail soapFaultDetail = fault.getDetail();
                omElement = soapFaultDetail.getFirstElement();

                if (omElement == null) {
                    omElement = fault.getReason();
                }

            } else {
                // Normal case: The xml payload is the first element in the body.
                omElement = messageContext.getEnvelope().getBody().getFirstElement();
            }
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

            if (omElement != null) {

                try {
                    if (preserve) {
                        omElement.serialize(bytesOut, format);
                    } else {
                        omElement.serializeAndConsume(bytesOut, format);
                    }
                } catch (XMLStreamException e) {
                    throw AxisFault.makeFault(e);
                }

                return bytesOut.toByteArray();
            }

            return new byte[0];
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("end getBytes()");
            }
        }
    }

    public void writeTo(MessageContext messageContext, OMOutputFormat format,
                        OutputStream outputStream, boolean preserve) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("start writeTo()");
        }
        try {
            OMElement omElement = null;

            if (messageContext.getFLOW() == MessageContext.OUT_FAULT_FLOW) {
                SOAPFault fault = messageContext.getEnvelope().getBody().getFault();
                SOAPFaultDetail soapFaultDetail = fault.getDetail();
                if (soapFaultDetail != null) {
                    omElement = soapFaultDetail.getFirstElement();
                }
                if (omElement == null) {
                    omElement = fault.getReason();
                }

            } else {
                omElement = messageContext.getEnvelope().getBody().getFirstElement();
            }
            if (omElement != null) {
                try {
                    if (preserve) {
                        omElement.serialize(outputStream, format);
                    } else {
                        omElement.serializeAndConsume(outputStream, format);
                    }
                } catch (XMLStreamException e) {
                    throw AxisFault.makeFault(e);
                }
            }
            try {
                outputStream.flush();
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("end writeTo()");
            }
        }
    }

    public String getContentType(MessageContext messageContext, OMOutputFormat format,
                                 String soapAction) {

        String encoding = format.getCharSetEncoding();
        String contentType;
        contentType = (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);

        if (log.isDebugEnabled()) {
            log.debug("contentType set from messageContext =" + contentType);
            log.debug("(NOTE) contentType from format is=" + format.getContentType());
        }
        
        if (contentType == null) {
            contentType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        } else if (isSOAPContentType(contentType)) {
            contentType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
            if (log.isDebugEnabled()) {
                log.debug("contentType is set incorrectly for Application XML.");
                log.debug("It is changed to " + contentType);
            }
        }

        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }

        if (log.isDebugEnabled()) {
            log.debug("contentType returned =" + contentType);
        }
        return contentType;
    }

    public URL getTargetAddress(MessageContext messageContext, 
                                OMOutputFormat format, 
                                URL targetURL)
            throws AxisFault {

        // Check whether there is a template in the URL, if so we have to replace then with data
        // values and create a new target URL.
        targetURL = URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);

        return targetURL;
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat format,
                                   String soapAction) {
        return soapAction;
    }
    
    private boolean isSOAPContentType(String contentType) {
        if (JavaUtils.indexOfIgnoreCase(contentType, SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
            return true;
        }
        // 'text/xml' can be a POX (REST) content type too
        // TODO - Remove 
        // search for "type=text/xml"
        /*else if (JavaUtils.indexOfIgnoreCase(contentType, 
                                             SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
            return true;
        } */
        return false;
    }
}
