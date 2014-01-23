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

package org.apache.axiom.attachments.impl;

import org.apache.axiom.attachments.utils.BAAInputStream;
import org.apache.axiom.om.OMException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * PartOnMemoryEnhanced stores the attachment in memory (in non-contigous byte arrays)
 * This implementation is used for smaller attachments to enhance 
 * performance.
 * 
 * The PartOnMemoryEnhanced object is created by the PartFactory
 * @see PartFactory
 */
public class PartOnMemoryEnhanced extends AbstractPart {

    private static Log log = LogFactory.getLog(PartOnMemoryEnhanced.class);
    ArrayList data;  // Arrays of 4K buffers
    int length;      // total length of data
    
    /**
     * Construct a PartOnMemory
     * @param headers
     * @param data array list of 4K byte[]
     * @param length (length of data in bytes)
     */
    PartOnMemoryEnhanced(Hashtable headers, ArrayList data, int length) {
        super(headers);
        this.data =  data;
        this.length = length;
    }

    public DataHandler getDataHandler() throws MessagingException {
        DataSource ds = new MyByteArrayDataSource();
        return new MyDataHandler(ds);
    }

    /* (non-Javadoc)
     * @see org.apache.axiom.attachments.impl.AbstractPart#getFileName()
     */
    public String getFileName() throws MessagingException {
        // There is no file name
        return null;
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        return new BAAInputStream(data, length);
    }
    
    public long getSize() throws MessagingException {
        return length;
    }
    
    
    static class MyDataHandler extends DataHandler {

        DataSource ds;
        public MyDataHandler(DataSource ds) {
            super(ds);
            this.ds = ds;
        }

        public void writeTo(OutputStream os) throws IOException {
            InputStream is = ds.getInputStream();
            if (is instanceof BAAInputStream) {
                ((BAAInputStream)is).writeTo(os);
            } else {
                BufferUtils.inputStream2OutputStream(is, os);
            }
        }
    }
    
    /**
     * A DataSource that is backed by the byte[] and 
     * headers map.
     */
    class MyByteArrayDataSource implements DataSource {

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getContentType()
         */
        public String getContentType() {
            String ct = getHeader("content-type");
            return (ct == null) ?
                    "application/octet-stream" :
                    ct;
        }

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getInputStream()
         */
        public InputStream getInputStream() throws IOException {
            InputStream is  = new BAAInputStream(data, length);
            String cte = null;
            try {
                cte = getContentTransferEncoding();
                if(cte != null){
                    if(log.isDebugEnabled()){
                        log.debug("Start Decoding stream");
                    }
                    return MimeUtility.decode(is, cte);

                }
            } catch (MessagingException e) {
                if(log.isDebugEnabled()){
                    log.debug("Stream Failed decoding");
                }
                throw new OMException(e);
            }
            return is;
        }

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getName()
         */
        public String getName() {
            return "MyByteArrayDataSource";
        }

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getOutputStream()
         */
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
        
    }

}
