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

package org.apache.axiom.om.ds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.attachments.impl.BufferUtils;
import org.apache.axiom.attachments.utils.BAAInputStream;
import org.apache.axiom.attachments.utils.BAAOutputStream;
import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.ds.OMDataSourceExtBase;
import org.apache.axiom.om.util.CommonUtils;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * A DataSource that is backed by an InputStream (read from a parser).
 * The Data in this Data source owns the payload inputStream. 
 */
public class ParserInputStreamDataSource extends OMDataSourceExtBase {
    private static final Log log = 
        LogFactory.getLog(ParserInputStreamDataSource.class);
    
    // This is the backing storage. 
    // The "data" object contains an InputStream that contains the actual bytes.
    // Copying/Marking of the InputStream is controlled by the requested Behavior.
    private Data data = null;
    
    // This behavior provides both safety and performance
    private final static int defaultBehavior = Behavior.NOT_DESTRUCTIVE;
    
    /**
     * This is the constructor that is normally called.
     * 
     * Note that the ParserInputStreamDataSource takes ownership of the 
     * payload InputStream.  It may copy, mark or reset the stream.  
     * Callers should not access the stream after this constructor is called
     * 
     * @param payload InputStream
     * @param encoding
     */
    public ParserInputStreamDataSource(InputStream payload, String encoding) {
        this(payload, 
            encoding, defaultBehavior );
    }
    
    /**
     * This constructor is used to test the different Behavior settings.
     * 
     * Note that the ParserInputStreamDataSource takes ownership of the 
     * payload InputStream.  It may copy, mark or reset the stream.  
     * Callers should not access the stream after this constructor is called.
     * 
     * @param payload
     * @param encoding
     * @param behavior
     */
    public ParserInputStreamDataSource(InputStream payload, 
            String encoding, 
            int behavior) {
        data = new  Data(payload,
                        (encoding!=null)?encoding:"UTF-8",
                         behavior);
    }
    

    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
        if(log.isDebugEnabled()){
            log.debug("Entry ParserInputStreamDataSource.serialize(OutputStream, OMOutputFormat");
        }
        
        String encoding = (format!=null)?format.getCharSetEncoding():null;
        
        try {
            if (!data.encoding.equalsIgnoreCase(encoding)) {
                byte[] bytes = getXMLBytes(encoding);
                output.write(bytes);
            } else {
                // Write the input stream to the output stream
                InputStream is = data.readParserInputStream();
                if(is!=null){
                    BufferUtils.inputStream2OutputStream(is, output);
                }
            }
            if(log.isDebugEnabled()){
                log.debug("Exit ParserInputStreamDataSource.serialize(OutputStream, OMOutputFormat");
            }
        } catch (UnsupportedEncodingException e) {
            throw new XMLStreamException(e);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
        if(log.isDebugEnabled()){
            log.debug("Entry ParserInputStreamDataSource.serialize(XMLStreamWriter)");
        }
        super.serialize(xmlWriter);
        if(log.isDebugEnabled()){
            log.debug("Exit ParserInputStreamDataSource.serialize(XMLStreamWriter)");
        }
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        if(log.isDebugEnabled()){
            log.debug("Entry ParserInputStreamDataSource.getReader()");
        }
        InputStream is = data.readParserInputStream();
        if(is == null){
            //Parser content has already been read.
            if(log.isDebugEnabled()){
                log.warn("Parser content has already been read");
            }
        }
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(is, data.encoding);
        if(log.isDebugEnabled()){
            log.debug("Exit ParserInputStreamDataSource.getReader()");
        }
        return reader;
    }

    /* 
     * Note that the returned InputStream may be different than the one
     * passed in the constructor. 
     * The caller may not used the mark or reset methods on the InputStream
     * (non-Javadoc)
     * @see org.apache.axiom.om.ds.OMDataSourceExtBase#getXMLInputStream(java.lang.String)
     */
    public InputStream getXMLInputStream(String encoding)  
        throws UnsupportedEncodingException {
        try{
            return data.readParserInputStream();
        }catch(XMLStreamException e){
            throw new OMException(e);
        }
    }

    public int numReads() {
        return data.numReads;
    }
    
    
    public Object getObject() {
        return data;
    }

    public boolean isDestructiveRead() {
        // If DESTRUCTIVE return true
        // If NOT_DESTRUCTIVE return false
        // If ONE_USE_UNSAFE, we lie and tell the engine false
        //      ...but we will intentionally fail on second access.
        return (data.behavior == Behavior.DESTRUCTIVE);
    }

    public boolean isDestructiveWrite() {
     // If DESTRUCTIVE return true
        // If NOT_DESTRUCTIVE return false
        // If ONE_USE_UNSAFE, we lie and tell the engine false
        //      ...but we will intentionally fail on second access.
        return (data.behavior == Behavior.DESTRUCTIVE);
    }

    public byte[] getXMLBytes(String encoding){
        if(log.isDebugEnabled()){
            log.debug("Entry ParserInputStreamDataSource.getXMLBytes(encoding)");
        }
        try{
            InputStream is = data.readParserInputStream();
            if(is != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OMOutputFormat format = new OMOutputFormat();
                format.setCharSetEncoding(encoding);
                try {
                    BufferUtils.inputStream2OutputStream(is, baos);
                    if(log.isDebugEnabled()){
                        log.debug("Exit ParserInputStreamDataSource.getXMLBytes(encoding)");
                    }
                    return baos.toByteArray();
                } catch (IOException e) {
                    throw new OMException(e);
                }
            }else{
                //Someone already read the parser, happens in serialize call. I expect user to invoke this
                //via SerializeAndConsume call
                if(log.isDebugEnabled()){
                    log.warn("Parser was already read, recovering by just returning new byte[0]");
                    log.debug("Exit ParserInputStreamDataSource.getXMLBytes(encoding)");
                }
                return new byte[0];
            }
        }catch(XMLStreamException e){
            throw new OMException(e);
        }
    }

    public void close() {
        if(log.isDebugEnabled()){
            log.debug("Entry ParserInputStreamDataSource.close()");
        }
        if (data.payload != null) {
            try {
                data.payload.close();
            } catch (IOException e) {
                throw new OMException(e);
            }
            data.payload = null;
        }
        if(log.isDebugEnabled()){
            log.debug("Exit ParserInputStreamDataSource.close()");
        }
    }

    /**
     * Return a InputStreamDataSource backed by a ByteArrayInputStream
     */
    public OMDataSourceExt copy() {
        if(log.isDebugEnabled()){
            log.debug("Enter ParserInputStreamDataSource.copy()");
        }
        try {
            BAAOutputStream baaos = new BAAOutputStream();
            BufferUtils.inputStream2OutputStream(data.readParserInputStream(), baaos);
            BAAInputStream baais = new BAAInputStream(baaos.buffers(), 
                        baaos.length());
            
            if (log.isDebugEnabled()) {
                log.debug("Exit ParserInputStreamDataSource.copy()");
            }
            return new ParserInputStreamDataSource(baais, data.encoding, data.behavior);
        } catch (Throwable t) {
            if(log.isDebugEnabled()){
                log.debug("Error ParserInputStreamDataSource.copy(): ", t);
            }
            throw new OMException(t);
        }
        
    }

    /**
     * @author scheu
     *
     */
    public static class Data{
        
        // The InputStream containing the byte data
        private InputStream payload = null;
        
        // The encoding (i.e. UTF-8)
        private String encoding = null;
        
        // The designated Behavior.  @see Behavior
        private int behavior;
        
        // Track the number of read accesses.  
        // ONE_USE_UNSAFE will intentionally fail on second read.
        private int numReads = 0;
        
        // Track the first use when ONE_USE_UNSAFE is requested
        private String firstUseStack = null;
        
        
        
        /** 
         * Intentionally provide.  Only created by ParserInputStreamDataSource
         * @param payload
         * @param encoding
         * @param behavior
         */
        private Data(InputStream payload,
                String encoding,
                int behavior) {
            this.payload = payload;
            this.encoding = encoding;
            this.behavior = behavior;

            setInputStream(payload);
        }
        
        /**
         * @return InputStream that consumer should use..this may be different
         * than the InputStream initially handed to the ParsedDataEntitySource
         * @throws XMLStreamException
         * @throws OMException if second access and ONE_USE_UNSAFE or other problems occur
         */
        public InputStream readParserInputStream() throws XMLStreamException{
            numReads++;
            
            // Dump our state
            if(log.isDebugEnabled()){
                log.debug("Entry readParserInputStream()");
                log.debug("Data Encoding = "+encoding);
                log.debug("numReads = "+numReads);
                log.debug("behavior = "+behavior);
                
                // The call stack is helpful to identify non-performant call flows
                String stack = CommonUtils.stackToString(new OMException());
                log.debug("call stack:" + stack);
            }
            
            
            // TODO NLS 
            if(payload == null){
                throw new OMException("ParserInputStreamDataSource's InputStream is null.");
            }   
            
            if (behavior == Behavior.NOT_DESTRUCTIVE) {
                if (numReads > 1) {
                    try {
                        // For NOT_DESTRUCTIVE, the 
                        // InputStream (either the original or copied InputStream)
                        // is reset for reuse.
                        if(log.isDebugEnabled()){
                            log.debug("reset InputStream for reuse");
                        }
                        payload.reset();
                    } catch (Throwable t) {
                        throw new OMException(t);
                    }
                }
            } else if (behavior == Behavior.ONE_USE_UNSAFE) {
                
                // For ONE_USE_UNSAFE, 
                //    remember the first call
                //    intentionally fail on the second call
                if (numReads == 1) {
                    firstUseStack = CommonUtils.stackToString(new OMException());
                    if(log.isDebugEnabled()){
                        log.debug("ONE_USE_UNSAFE mode stack:" + firstUseStack);
                    }
                } else {
                    // TODO NLS
                    OMException ome = 
                        new OMException("A second read of ParserInputStreamDataSource is not allowed."
                           + "The first read was done here: " + firstUseStack);
                    if(log.isDebugEnabled()){
                        log.debug("ONE_USE_UNSAFE second use exception:" + ome);
                    }
                    throw ome;
                }
            }
            
            
            if(log.isDebugEnabled()){
                log.debug("Exit readParserInputStream()");
            }
            return payload;
        }
        
        public void setInputStream(InputStream inputStream) {
            if(log.isDebugEnabled()){
                String clsName = inputStream == null ? 
                        null : 
                            inputStream.getClass().getName();
                log.debug("Enter setInputStream: The kind of InputStream is:" + clsName);
            }
            this.numReads = 0;
            this.firstUseStack = null;
            
            if (inputStream == null) {
                if(log.isDebugEnabled()){
                    log.debug("The inputStream is null");
                }
                payload = null;
            } else if (behavior == Behavior.NOT_DESTRUCTIVE) {
                if (inputStream.markSupported()) {
                    if(log.isDebugEnabled()){
                        log.debug("The inputStream supports mark().  Setting mark()");
                    }
                    // use mark/reset
                    payload = inputStream;
                    payload.mark(Integer.MAX_VALUE);
                } else {
                    try {
                        if(log.isDebugEnabled()){
                            log.debug("The inputStream does not supports mark().  Copying Stream");
                        }
                        // make a non-contiguous resettable input stream
                        BAAOutputStream baaos = new BAAOutputStream();
                        BufferUtils.inputStream2OutputStream(inputStream, baaos);
                        BAAInputStream baais = new BAAInputStream(baaos.buffers(), 
                                    baaos.length());
                        payload = baais;
                        payload.mark(Integer.MAX_VALUE);
                    } catch (Throwable t) {
                        if(log.isDebugEnabled()){
                            log.debug("Error:", t);
                        }
                        throw new OMException(t);
                    }
                }
            } else {
                payload = inputStream;
            }
            if(log.isDebugEnabled()){
                log.debug("Exit setInputStream");
            }
        }
   
    }

}
