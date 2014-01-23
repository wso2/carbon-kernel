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

package org.apache.axis2.context.externalize;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamReader;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * Utility to read/write the Message of a MessageContext
 * Message Object Format.
 * 
 * <tt>
 * Format := Prolog {DataBlocks} EndBlocks
 * 
 * Prolog :=
 *   NAME      (UTF) 
 *   REVISION  (INT)
 *   ACTIVE    (BOOL)
 *     [OPTIMIZED (BOOL)]  
 *        [OPTIMIZED_CONTENT_TYPE (UTF)]    <--- If OPTIMIZED=TRUE
 *     [CHARSET   (UTF)] 
 *     [NAMESPACE (UTF)]
 *   
 * DataBlock :=
 *   SIZE (INT >0)
 *   DATA (BYTES)
 *   
 * EndBlocks
 *   SIZE (INT)   {0 indicates end -1 indicates failure}
 *     
 *   
 * </tt>
 */
public class MessageExternalizeUtils  implements ExternalizeConstants {
    static final Log log = LogFactory.getLog(MessageExternalizeUtils.class);

    /*
     * @serial Tracks the revision level of a class to identify changes to the
     * class definition that are compatible to serialization/externalization.
     * If a class definition changes, then the serialization/externalization
     * of the class is affected.
     * Refer to the writeExternal() and readExternal() methods.
     */
    // supported revision levels, add a new level to manage compatible changes
    private static final int REVISION_2 = 2;
    // current revision level of this object
    private static final int revisionID = REVISION_2;
    
    
    /**
     * Private Constructor.
     * This class only supports static methods
     */
    private MessageExternalizeUtils() {}
    
    /**
     * Write out the Message
     * @param out
     * @param mc
     * @param correlationIDString
     * @param outputFormat
     * @throws IOException
     */
    public static void writeExternal(ObjectOutput out, 
                                     MessageContext mc,
                                     String correlationIDString,
                                     OMOutputFormat outputFormat) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(correlationIDString + ":writeExternal(): start");
        }
        SOAPEnvelope envelope = mc.getEnvelope();
        if (envelope == null) {
            // Case: No envelope
            out.writeUTF("NULL_ENVELOPE");
            out.writeInt(revisionID);
            out.writeBoolean(EMPTY_OBJECT); // Not Active
            out.writeInt(0);  // EndBlocks
            if (log.isDebugEnabled()) {
                log.debug(correlationIDString + ":writeExternal(): end: msg is Empty");
            }
            return;
        }
        
        // Write Prolog
        String msgClass = envelope.getClass().getName();
        out.writeUTF(msgClass);
        out.writeInt(revisionID);
        out.writeBoolean(ACTIVE_OBJECT);
        if (outputFormat.isOptimized()) {
            out.writeBoolean(true);
            // Write out the contentType.
            out.writeUTF(outputFormat.getContentType());
        } else {
            out.writeBoolean(false);
        }
        out.writeUTF(outputFormat.getCharSetEncoding());
        out.writeUTF(envelope.getNamespace().getNamespaceURI());
        if (log.isDebugEnabled()) {
            log.debug(correlationIDString + ":writeExternal(): " + 
                      "optimized=[" + outputFormat.isOptimized() + "]  " +
                      "optimizedContentType " + outputFormat.getContentType() + "]  " +
                      "charSetEnc=[" + outputFormat.getCharSetEncoding() + "]  " +
                      "namespaceURI=[" + envelope.getNamespace().getNamespaceURI() + "]");
        }
        
        // Write DataBlocks
        // MessageOutputStream writes out the DataBlocks in chunks
        // BufferedOutputStream buffers the data to prevent numerous, small blocks
        MessageOutputStream mos = new MessageOutputStream(out);  
        BufferedOutputStream bos = new BufferedOutputStream(mos);   
        boolean errorOccurred = false;
        try { 
            // Write out the message using the same logic as the 
            // transport layer.
            MessageFormatter msgFormatter = MessageProcessorSelector.getMessageFormatter(mc);
            msgFormatter.writeTo(mc, outputFormat, bos, 
                                 true); // Preserve the original message
            
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            throw AxisFault.makeFault(t);
        } finally {
            bos.flush();
            bos.close();
        }
        
        // Write End of Data Blocks
        if (errorOccurred) {
            out.writeInt(-1);
        } else {
            out.writeInt(0);
        }
        if (log.isDebugEnabled()) {
            log.debug(correlationIDString + ":writeExternal(): end");
        }
    }
    
    /**
     * Read the Message
     * @param in
     * @param mc
     * @param correlationIDString
     * @return
     * @throws IOException
     */
    public static SOAPEnvelope readExternal(ObjectInput in,
                                            MessageContext mc,
                                            String correlationIDString) throws IOException, ClassNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug(correlationIDString + ":readExternal(): start");
        }
        SOAPEnvelope envelope = null;
        
        // Read Prolog
        // Read the class name and object state
        String name = in.readUTF();
        int revision = in.readInt();
        
        if (log.isDebugEnabled()) {
            log.debug(correlationIDString + ":readExternal(): name= " + name  +
                      " revision= " + revision);
        }
        // make sure the object data is in a revision level we can handle
        if (revision != REVISION_2) {
            throw new ClassNotFoundException(ExternalizeConstants.UNSUPPORTED_REVID);
        }
        
        
        boolean gotMsg = in.readBoolean();
        if (gotMsg != ACTIVE_OBJECT) {
            if (log.isDebugEnabled()) {
                log.debug(correlationIDString + ":readExternal(): end:" +
                                "no message present");
            }
            in.readInt(); // Read end of data blocks
            return envelope;
        }
        
        // Read optimized, optimized content-type, charset encoding and namespace uri
        boolean optimized= in.readBoolean();
        String optimizedContentType = null;
        if (optimized) {
            optimizedContentType = in.readUTF();
        }
        String charSetEnc = in.readUTF();
        String namespaceURI = in.readUTF();
        if (log.isDebugEnabled()) {
            log.debug(correlationIDString + ":readExternal(): " +
                      "optimized=[" + optimized + "]  " +
                      "optimizedContentType=[" + optimizedContentType + "]  " +
                      "charSetEnc=[" + charSetEnc + "]  " +
                      "namespaceURI=[" + namespaceURI + "]");
        }
        
        MessageInputStream mis = new MessageInputStream(in);
        StAXBuilder  builder = null;
        try {
            if (optimized) {
                boolean isSOAP = true;
                builder =
                    BuilderUtil.getAttachmentsBuilder(mc,
                                                      mis,
                                                      optimizedContentType,
                                                      isSOAP);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
                envelope.buildWithAttachments();
            } else {
                XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(mis, charSetEnc);
                builder = new StAXSOAPModelBuilder(xmlreader, namespaceURI);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
                envelope.build();
            }
        } catch (Exception ex) {
            // TODO: what to do if can't get the XML stream reader
            // For now, log the event
            log.error(correlationIDString
                      + ":readExternal(): Error when deserializing persisted envelope: ["
                      + ex.getClass().getName() + " : " + ex.getLocalizedMessage() + "]", ex);
            envelope = null;
        } finally {
            if (builder != null) {
                builder.close();
            }
            // Close the message input stream.  This will ensure that the
            // underlying stream is advanced past the message.
            mis.close();
            if (log.isDebugEnabled()) {
                log.debug(correlationIDString + ":readExternal(): end");
            }
        }
        return envelope;
    }
    
    /**
     * MessageOutputStream writes DataBlock chunks to the ObjectOutput.
     */
    private static class MessageOutputStream extends OutputStream {
        ObjectOutput out;
        boolean isDebug;
        MessageOutputStream(ObjectOutput out) {
            this.out = out;
            isDebug = log.isDebugEnabled();
        }
        
         
        public void close() throws IOException {
            // NOOP: ObjectOutput will be closed externally
        }
        
        public void flush() throws IOException {
            out.flush();
        }

        /** 
         * Writes a chunk of data to the ObjectOutput
         */
        public void write(byte[] b, int off, int len) throws IOException {
            if (len > 0) {
                if (isDebug) {
                    log.debug("Write data chunk with len=" + len);
                }
                // Write out the length and the data chunk
                out.writeInt(len);
                out.write(b, off, len);
            }
        }

         
        /** 
         * Writes a chunk of data to the ObjectOutput
         */
        public void write(byte[] b) throws IOException {
            if (b != null &&  b.length > 0) {
                if (isDebug) {
                    log.debug("Write data chunk with size=" + b.length);
                }
                // Write out the length and the data chunk
                out.writeInt(b.length);
                out.write(b);
            }
        }

         
        /** 
         * Writes a single byte chunk of data to the ObjectOutput
         */
        public void write(int b) throws IOException {
            if (isDebug) {
                log.debug("Write one byte data chunk");
            }
            // Write out the length and the data chunk
            out.writeInt(1);
            out.write(b);
        }
    }
       
    /**
     * Provides a InputStream interface over ObjectInput.
     * MessageInputStream controls the reading of the DataBlock chunks
     *
     */
    private static class MessageInputStream extends InputStream {
        
        ObjectInput in;
        boolean isDebug;
        int chunkAvail = 0;
        boolean isEOD = false;
        
        /**
         * Constructor
         * @param in
         */
        MessageInputStream(ObjectInput in) {
            this.in = in;
            isDebug = log.isDebugEnabled();
        }

         
        /**
         * Read a single logical byte
         */
        public int read() throws IOException {
            if (isDebug) {
                log.debug("invoking read()");
            }
            // Determine how many bytes are left in the current data chunk
            updateChunkAvail();
            int ret = 0;
            if (isEOD) {
                ret = -1;
            } else {
                chunkAvail--;
                ret = in.readByte();
            }
            log.debug("returning " + ret);
            return ret;
        }

         
        /**
         * Read an array of logical bytes
         */
        public int read(byte[] b, int off, int len) throws IOException {
            if (isDebug) {
                log.debug("invoking read with off=" + off + " and len=" + len);
            }
            if (isEOD) {
                if (isDebug) {
                    log.debug("EOD returning -1");
                }
                return -1;
            }
            int bytesRead = 0;
            while ((len >0 && !isEOD)) {
                // Determine how many bytes are left in the current data chunk
                updateChunkAvail();
                if (!isEOD) {
                    // Read the amount of bytes requested or the number of bytes available in the current chunk
                    int readLength = len < chunkAvail ? len : chunkAvail;
                    int br = in.read(b, off, readLength);
                    if (br < 0) {
                        throw new IOException("End of File encountered");
                    }
                    // Update state with the number of bytes read
                    off += br;
                    len -= br;
                    chunkAvail -= br;
                    bytesRead += br;
                }
            }
            if (isDebug) {
                log.debug("bytes read = " + bytesRead);
            }
            return bytesRead;
        }

         
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        
        public void close() throws IOException {
            if (isDebug) {
                log.debug("start close");
            }
            // Keep reading chunks until EOD
            if (!isEOD) {
                byte[] tempBuffer = new byte[4 * 1024];
                while (!isEOD) {
                    read(tempBuffer);
                }
            }
            if (isDebug) {
                log.debug("end close");
            }
        }
        
        /**
         * updateChunkAvail updates the chunkAvail field with the
         * amount of data in the chunk.
         * @throws IOException
         */
        private void updateChunkAvail() throws IOException {
            
            // If there are no more bytes in the current chunk,
            // read the size of the next datablock
            if (chunkAvail == 0 && !isEOD) {
                chunkAvail = in.readInt();
                if (isDebug) {
                    log.debug("New DataBlock with size=" + chunkAvail);
                }
                if (chunkAvail <= 0) {
                    if (isDebug) {
                        log.debug("End of data");
                    }
                    isEOD = true;
                    chunkAvail = 0;
                }
            }
        }
    }
}
