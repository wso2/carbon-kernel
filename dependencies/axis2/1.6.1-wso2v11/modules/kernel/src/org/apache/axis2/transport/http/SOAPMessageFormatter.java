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

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.OMMultipartWriter;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class SOAPMessageFormatter implements MessageFormatter {

    private static final Log log = LogFactory.getLog(SOAPMessageFormatter.class);
    
    public void writeTo(MessageContext msgCtxt, OMOutputFormat format,
                        OutputStream out, boolean preserve) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("start writeTo()");
            log.debug("  preserve=" + preserve);
            log.debug("  isOptimized=" + format.isOptimized());
            log.debug("  isDoingSWA=" + format.isDoingSWA());
        }
        
        if (msgCtxt.isDoingMTOM()) {        	
            int optimizedThreshold = Utils.getMtomThreshold(msgCtxt);       
            if(optimizedThreshold > 0){
            	if(log.isDebugEnabled()){
            		log.debug("Setting MTOM optimized Threshold Value on OMOutputFormat");
            	}
            	format.setOptimizedThreshold(optimizedThreshold);
            }        	
        }
        try {
            if (!(format.isOptimized()) && format.isDoingSWA()) {
                writeSwAMessage(msgCtxt, out, format, preserve);
            } else {
                OMElement element = msgCtxt.getEnvelope();
                if (preserve) {
                    element.serialize(out, format);
                } else {
                    element.serializeAndConsume(out, format);
                }
            }
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("end writeTo()");
            }
        }
    }

    public byte[] getBytes(MessageContext msgCtxt, OMOutputFormat format)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("start getBytes()");
            log.debug("  isOptimized=" + format.isOptimized());
            log.debug("  isDoingSWA=" + format.isDoingSWA());
        }
        OMElement element = msgCtxt.getEnvelope();
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            if (!format.isOptimized()) {
                if (format.isDoingSWA()) {
                    writeSwAMessage(msgCtxt, bytesOut, format, false);
                } else {
                    element.serializeAndConsume(bytesOut, format);
                }
                return bytesOut.toByteArray();
            } else {
                element.serializeAndConsume(bytesOut, format);
                return bytesOut.toByteArray();
            }
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        } catch (FactoryConfigurationError e) {
            throw AxisFault.makeFault(e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("end getBytes()");
            }
        }
    }

    public String getContentType(MessageContext msgCtxt, OMOutputFormat format,
                                 String soapActionString) {
        String encoding = format.getCharSetEncoding();
        String contentType = format.getContentType();
        if (log.isDebugEnabled()) {
            log.debug("contentType from the OMOutputFormat =" + contentType);
        }
         if (encoding != null && contentType != null &&
        		contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED)==-1) {
             contentType += "; charset=" + encoding;
         }

        // action header is not mandated in SOAP 1.2. So putting it, if
        // available
        if (!msgCtxt.isSOAP11() && (soapActionString != null)
                && !"".equals(soapActionString.trim())
                && !"\"\"".equals(soapActionString.trim())) {
            contentType = contentType + "; action=\"" + soapActionString+ "\"";
        }
        
        // This is a quick safety catch.  Prior versions of SOAPFormatter
        // placed a ';' at the end of the content-type.  Many vendors ignore this
        // last ';'.  However it is not legal and some vendors report an error.
        // To increase interoperability, the ';' is stripped off.
        contentType = contentType.trim();
        if (contentType.lastIndexOf(";") == (contentType.length()-1)) {
            contentType = contentType.substring(0, contentType.length()-1);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("contentType returned =" + contentType);
        }
        return contentType;
    }

    public String formatSOAPAction(MessageContext msgCtxt, OMOutputFormat format,
                                   String soapActionString) {
        // if SOAP 1.2 we attach the soap action to the content-type
        // No need to set it as a header.
        if (msgCtxt.isSOAP11()) {
            if ("".equals(soapActionString)) {
                return "\"\"";
            } else {
                if (soapActionString != null
                        && !soapActionString.startsWith("\"")) {
                    // SOAPAction string must be a quoted string
                    soapActionString = "\"" + soapActionString + "\"";
                }
                return soapActionString;
            }
        }
        return null;
    }

    public URL getTargetAddress(MessageContext msgCtxt, OMOutputFormat format,
                                URL targetURL) throws AxisFault {

        // Check whether there is a template in the URL, if so we have to replace then with data
        // values and create a new target URL.
        targetURL = URLTemplatingUtil.getTemplatedURL(targetURL, msgCtxt, false);
        return targetURL;
    }

    private void writeSwAMessage(MessageContext msgCtxt, OutputStream outputStream,
                                 OMOutputFormat format, boolean preserve) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("start writeSwAMessage()");
        }
        Object property = msgCtxt
                .getProperty(Constants.Configuration.MM7_COMPATIBLE);
        boolean MM7CompatMode = false;
        if (property != null) {
            MM7CompatMode = JavaUtils.isTrueExplicitly(property);
        }
        
        try {
            OMMultipartWriter mpw = new OMMultipartWriter(outputStream, format);
            
            OutputStream rootPartOutputStream = mpw.writeRootPart();
            OMElement element = msgCtxt.getEnvelope();
            if (preserve) {
                element.serialize(rootPartOutputStream, format);
            } else {
                element.serializeAndConsume(rootPartOutputStream, format);
            }
            rootPartOutputStream.close();
            
            OMMultipartWriter attachmentsWriter;
            OutputStream innerOutputStream;
            if (!MM7CompatMode) {
                attachmentsWriter = mpw;
                innerOutputStream = null;
            } else {
                String innerBoundary;
                String partCID;
                Object innerBoundaryProperty = msgCtxt
                        .getProperty(Constants.Configuration.MM7_INNER_BOUNDARY);
                if (innerBoundaryProperty != null) {
                    innerBoundary = (String) innerBoundaryProperty;
                } else {
                    innerBoundary = "innerBoundary"
                            + UIDGenerator.generateMimeBoundary();
                }
                Object partCIDProperty = msgCtxt
                        .getProperty(Constants.Configuration.MM7_PART_CID);
                if (partCIDProperty != null) {
                    partCID = (String) partCIDProperty;
                } else {
                    partCID = "innerCID"
                            + UIDGenerator.generateContentId();
                }
                OMOutputFormat innerFormat = new OMOutputFormat(format);
                innerFormat.setMimeBoundary(innerBoundary);
                innerOutputStream = mpw.writePart("multipart/related; boundary=\"" + innerBoundary + "\"", partCID);
                attachmentsWriter = new OMMultipartWriter(innerOutputStream, innerFormat);
            }
            
            Attachments attachments = msgCtxt.getAttachmentMap();
            for (String contentID : attachments.getAllContentIDs()) {
                attachmentsWriter.writePart(attachments.getDataHandler(contentID), contentID);
            }
            
            if (MM7CompatMode) {
                attachmentsWriter.complete();
                innerOutputStream.close();
            }
            
            mpw.complete();
        } catch (IOException ex) {
            throw AxisFault.makeFault(ex);
        } catch (XMLStreamException ex) {
            throw AxisFault.makeFault(ex);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("end writeSwAMessage()");
        }
    }

}
