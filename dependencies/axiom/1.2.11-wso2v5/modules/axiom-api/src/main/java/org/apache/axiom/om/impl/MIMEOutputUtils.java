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

package org.apache.axiom.om.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.ConfigurableDataHandler;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.CommonUtils;
import org.apache.axiom.util.activation.DataHandlerWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @deprecated The features of this class are now implemented by {@link OMMultipartWriter}, which
 *             has as cleaner API and supports streaming of individual MIME parts, in particular the
 *             SOAP part.
 */
public class MIMEOutputUtils {
    
    private static Log log = LogFactory.getLog(MIMEOutputUtils.class);
    private static boolean isDebugEnabled = log.isDebugEnabled();

    private static byte[] CRLF = { 13, 10 };

    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void complete(OutputStream outStream, 
                                byte[] xmlData,
                                LinkedList binaryNodeList, 
                                String boundary, 
                                String contentId,
                                String charSetEncoding, 
                                String SOAPContentType) {
        complete(outStream, xmlData, binaryNodeList, boundary,
                 contentId, charSetEncoding, SOAPContentType, null);
    }
    
    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void complete(OutputStream outStream, 
                                byte[] xmlData,
                                LinkedList binaryNodeList, 
                                String boundary, 
                                String contentId,
                                String charSetEncoding, 
                                String SOAPContentType, 
                                OMOutputFormat omOutputFormat) {
        try {
            if (isDebugEnabled) {
                log.debug("Start: write the SOAPPart and the attachments");
            }
            
            // Write out the mime boundary
            startWritingMime(outStream, boundary);

            javax.activation.DataHandler dh = 
                new javax.activation.DataHandler(new ByteArrayDataSource(xmlData,
                                                 "text/xml; charset=" + charSetEncoding));
            MimeBodyPart rootMimeBodyPart = new MimeBodyPart();
            rootMimeBodyPart.setDataHandler(dh);

            rootMimeBodyPart.addHeader("Content-Type",
                                       "application/xop+xml; charset=" + charSetEncoding +
                                               "; type=\"" + SOAPContentType + "\"");
            rootMimeBodyPart.addHeader("Content-Transfer-Encoding", "binary");
            rootMimeBodyPart.addHeader("Content-ID", "<" + contentId + ">");

            // Write out the SOAPPart
            writeBodyPart(outStream, rootMimeBodyPart, boundary);

            // Now write out the Attachment parts (which are represented by the
            // text nodes int the binary node list)
            Iterator binaryNodeIterator = binaryNodeList.iterator();
            while (binaryNodeIterator.hasNext()) {
                OMText binaryNode = (OMText) binaryNodeIterator.next();
                writeBodyPart(outStream, createMimeBodyPart(binaryNode
                        .getContentID(), (DataHandler) binaryNode
                        .getDataHandler(), omOutputFormat), boundary);
            }
            finishWritingMime(outStream);
            outStream.flush();
            if (isDebugEnabled) {
                log.debug("End: write the SOAPPart and the attachments");
            }
        } catch (IOException e) {
            throw new OMException("Error while writing to the OutputStream.", e);
        } catch (MessagingException e) {
            throw new OMException("Problem writing Mime Parts.", e);
        }
    }

    /**
     * @deprecated This method is only useful in conjunction with
     *             {@link #writeBodyPart(OutputStream, MimeBodyPart, String)}, which is deprecated.
     */
    public static MimeBodyPart createMimeBodyPart(String contentID,
                                                  DataHandler dataHandler) 
            throws MessagingException {
        return createMimeBodyPart(contentID, dataHandler, null);
    }
                                                  
    /**
     * @deprecated This method is only useful in conjunction with
     *             {@link #writeBodyPart(OutputStream, MimeBodyPart, String)}, which is deprecated.
     */
    public static MimeBodyPart createMimeBodyPart(String contentID,
                                                  DataHandler dataHandler,
                                                  OMOutputFormat omOutputFormat)
           throws MessagingException {
        String contentType = dataHandler.getContentType();
        
        // Get the content-transfer-encoding
        String contentTransferEncoding = "binary";
        if (dataHandler instanceof ConfigurableDataHandler) {
            ConfigurableDataHandler configurableDataHandler = (ConfigurableDataHandler) dataHandler;
            contentTransferEncoding = configurableDataHandler.getTransferEncoding();
        }
        
        if (isDebugEnabled) {
            log.debug("Create MimeBodyPart");
            log.debug("  Content-ID = " + contentID);
            log.debug("  Content-Type = " + contentType);
            log.debug("  Content-Transfer-Encoding = " + contentTransferEncoding);
        }
        
        boolean useCTEBase64 = omOutputFormat != null &&
            Boolean.TRUE.equals(
               omOutputFormat.getProperty(
                   OMOutputFormat.USE_CTE_BASE64_FOR_NON_TEXTUAL_ATTACHMENTS));
        if (useCTEBase64) {
            if (!CommonUtils.isTextualPart(contentType) && 
                "binary".equals(contentTransferEncoding)) {
                if (isDebugEnabled) {
                    log.debug(" changing Content-Transfer-Encoding from " + 
                              contentTransferEncoding + " to base-64");
                }
                contentTransferEncoding = "base64";
            }
            
        }
        
        // Now create the mimeBodyPart for the datahandler and add the appropriate content headers
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setDataHandler(dataHandler);
        mimeBodyPart.addHeader("Content-ID", "<" + contentID + ">");
        mimeBodyPart.addHeader("Content-Type", contentType);
        mimeBodyPart.addHeader("Content-Transfer-Encoding", contentTransferEncoding);
        return mimeBodyPart;
    }

    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void writeMimeBoundary(OutputStream outStream,
                                         String boundary) throws IOException {
        // REVIEW: This conversion is hard-coded to UTF-8.
        // The complete solution is to respect the charset setting of the message.
        // However this may cause problems in BoundaryDelimittedStream and other
        // lower level classes.
        outStream.write(new byte[] { 45, 45 });
        outStream.write(boundary.getBytes("UTF-8"));
    }

    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void startWritingMime(OutputStream outStream,
                                        String boundary)
            throws IOException {
        writeMimeBoundary(outStream, boundary);
        //outStream.write(CRLF);
    }

    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void writeBodyPart(OutputStream outStream,
                                     MimeBodyPart part,
                                     String boundary) throws IOException,
            MessagingException {
        if (isDebugEnabled) {
            log.debug("Start writeMimeBodyPart for " + part.getContentID());
        }
        outStream.write(CRLF);
        part.writeTo(outStream);
        outStream.write(CRLF);
        writeMimeBoundary(outStream, boundary);
        outStream.flush();
        if (isDebugEnabled) {
            log.debug("End writeMimeBodyPart");
        }
    }

    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void finishWritingMime(OutputStream outStream)
            throws IOException {
        if (isDebugEnabled) {
            log.debug("Write --, which indicates the end of the last boundary");
        }
        outStream.write(new byte[] { 45, 45 });
    }

    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void writeSOAPWithAttachmentsMessage(StringWriter writer,
                                                       OutputStream outputStream,
                                                       Attachments attachments,
                                                       OMOutputFormat format) {
        try {
            OMMultipartWriter mpw = new OMMultipartWriter(outputStream, format);
            
            Writer rootPartWriter = new OutputStreamWriter(mpw.writeRootPart(), format.getCharSetEncoding());
            rootPartWriter.write(writer.toString());
            rootPartWriter.close();
            
            // Get the collection of ids associated with the attachments
            Collection ids;         
            if (respectSWAAttachmentOrder(format)) {
                // ContentIDList is the order of the incoming/added attachments
                ids = Arrays.asList(attachments.getAllContentIDs());
            } else {
                // ContentIDSet is an undefined order (the implementation currently
                // orders the attachments using the natural order of the content ids)
                ids = attachments.getContentIDSet();
            }
            
            for (Iterator it = ids.iterator(); it.hasNext(); ) {
                String id = (String)it.next();
                mpw.writePart(attachments.getDataHandler(id), id);
            }
            
            mpw.complete();
        } catch (IOException ex) {
            throw new OMException("Error writing SwA message", ex);
        }
    }

    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void writeDataHandlerWithAttachmentsMessage(DataHandler rootDataHandler,
            String contentType,
            OutputStream outputStream,
            Map attachments,
            OMOutputFormat format) {
        writeDataHandlerWithAttachmentsMessage(rootDataHandler,
                contentType,
                outputStream,
                attachments,
                format,
                null);
                    
    }
    
    /**
     * @deprecated Use {@link OMMultipartWriter} instead.
     */
    public static void writeDataHandlerWithAttachmentsMessage(DataHandler rootDataHandler,
                                                       final String contentType,
                                                       OutputStream outputStream,
                                                       Map attachments,
                                                       OMOutputFormat format,
                                                       Collection ids) {
        try {
            if (!rootDataHandler.getContentType().equals(contentType)) {
                rootDataHandler = new DataHandlerWrapper(rootDataHandler) {
                    public String getContentType() {
                        return contentType;
                    }
                };
            }
            
            OMMultipartWriter mpw = new OMMultipartWriter(outputStream, format);
            
            mpw.writePart(rootDataHandler, format.getRootContentId());

            Iterator idIterator = null;
            if (ids == null) {
                // If ids are not provided, use the attachment map
                // to get the keys
                idIterator = attachments.keySet().iterator();  
            } else {
                // if ids are provided (normal case), iterate
                // over the ids so that the attachments are 
                // written in the same order as the id keys.
                idIterator = ids.iterator();
            }
            
            while (idIterator.hasNext()) {
                String key = (String) idIterator.next();
                mpw.writePart((DataHandler) attachments.get(key), key);
            }
            mpw.complete();
            outputStream.flush();
        } catch (IOException e) {
            throw new OMException("Error while writing to the OutputStream.", e);
        }
    }

    /**
     * @deprecated Axiom only supports standard SwA messages. However, {@link OMMultipartWriter}
     *             provides a flexible way to build MIME packages for non standard formats such as
     *             MM7.
     */
    public static void writeMM7Message(StringWriter writer,
                                       OutputStream outputStream, Attachments attachments,
                                       OMOutputFormat format, String innerPartCID,
                                       String innerBoundary) {
        try {
            OMMultipartWriter mpw = new OMMultipartWriter(outputStream, format);
            
            Writer rootPartWriter = new OutputStreamWriter(mpw.writeRootPart(), format.getCharSetEncoding());
            rootPartWriter.write(writer.toString());
            rootPartWriter.close();
            
            if (attachments.getContentIDSet().size() != 0) {
                OMOutputFormat innerFormat = new OMOutputFormat(format);
                innerFormat.setMimeBoundary(innerBoundary);
                OutputStream innerOutputStream = mpw.writePart("multipart/related; boundary=\"" + innerBoundary + "\"", innerPartCID);
                OMMultipartWriter innerMpw = new OMMultipartWriter(innerOutputStream, innerFormat);
                Collection ids;
                if (respectSWAAttachmentOrder(format)) {
                    // ContentIDList is the order of the incoming/added attachments
                    ids = Arrays.asList(attachments.getAllContentIDs());
                } else {
                    // ContentIDSet is an undefined order (the implementation currently
                    // orders the attachments using the natural order of the content ids)
                    ids = attachments.getContentIDSet();
                }
                for (Iterator it = ids.iterator(); it.hasNext(); ) {
                    String id = (String)it.next();
                    innerMpw.writePart(attachments.getDataHandler(id), id);
                }
                innerMpw.complete();
                innerOutputStream.close();
            }
            
            mpw.complete();
        } catch (IOException e) {
            throw new OMException("Error while writing to the OutputStream.", e);
        }
    }
    
    /**
     * @param format
     * @return true if the incoming attachment order should be respected
     */
    private static boolean respectSWAAttachmentOrder(OMOutputFormat format) {
        Boolean value = (Boolean) format.getProperty(OMOutputFormat.RESPECT_SWA_ATTACHMENT_ORDER);
        if (value == null) {
            value = OMOutputFormat.RESPECT_SWA_ATTACHMENT_ORDER_DEFAULT;
        }
        return value.booleanValue();
    }
}
