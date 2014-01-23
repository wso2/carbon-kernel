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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.utils.BAAOutputStream;
import org.apache.axiom.ext.io.ReadFromSupport;
import org.apache.axiom.util.activation.DataSourceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Attachment processing uses a lot of buffers.
 * The BufferUtils class attempts to reuse buffers to prevent 
 * excessive GarbageCollection
 */
public class BufferUtils {
    private static Log log = LogFactory.getLog(BufferUtils.class);
    // Performance testing indicates that 4K is the best size for medium
    // and small payloads.  And there is a neglible effect on large payloads.
    public final static int BUFFER_LEN = 4 * 1024;   // Copy Buffer size
    static boolean ENABLE_FILE_CHANNEL = true;       // Enable file channel optimization
    static boolean ENABLE_BAAOS_OPT = true;          // Enable BAAOutputStream opt
    
    private static byte[] _cacheBuffer = new byte[BUFFER_LEN];
    private static boolean _cacheBufferInUse = false;
    
    private static ByteBuffer _cacheByteBuffer = ByteBuffer.allocate(BUFFER_LEN);
    private static boolean _cacheByteBufferInUse = false;
    
    /**
     * Private utility to write the InputStream contents to the OutputStream.
     * @param is
     * @param os
     * @throws IOException
     */
    public static void inputStream2OutputStream(InputStream is, 
                                                OutputStream os)
        throws IOException {
            
        
        // If this is a FileOutputStream, use the optimized method
        if (ENABLE_FILE_CHANNEL && os instanceof FileOutputStream) {
            if (inputStream2FileOutputStream(is, (FileOutputStream) os)) {
                return;
            }
        }
        
        // If the stream implements ReadFromSupport, use the optimized method
        if (ENABLE_BAAOS_OPT && os instanceof ReadFromSupport) {
            ((ReadFromSupport)os).readFrom(is, Long.MAX_VALUE);
            return;
        }
        
        byte[] buffer = getTempBuffer();
        
        try {
        int bytesRead = is.read(buffer);
        
        
        // Continue reading until no bytes are read and no
        // bytes are now available.
        while (bytesRead > 0 || is.available() > 0) {
            if (bytesRead > 0) {
                os.write(buffer, 0, bytesRead);
            }
            bytesRead = is.read(buffer);
        }
        } finally {
            releaseTempBuffer(buffer);
        }
        
    }
    
    /**
     * @param is InputStream
     * @param os OutputStream
     * @param limit maximum number of bytes to read
     * @return total bytes read
     * @throws IOException
     */
    public static int inputStream2OutputStream(InputStream is, 
                                                OutputStream os,
                                                int limit) 
        throws IOException {
        
        // If the stream implements ReadFromSupport, use the optimized method
        if (ENABLE_BAAOS_OPT && os instanceof ReadFromSupport) {
            return (int) ((ReadFromSupport)os).readFrom(is, limit);
        }
            
        byte[] buffer = getTempBuffer();
        int totalWritten = 0;
        int bytesRead = 0;
        
        try {
            do {
                int len = (limit-totalWritten) > BUFFER_LEN ? BUFFER_LEN : (limit-totalWritten);
                bytesRead = is.read(buffer, 0, len);
                if (bytesRead > 0) {
                    os.write(buffer, 0, bytesRead);
                    if (bytesRead > 0) {
                        totalWritten += bytesRead;
                    }
                }
            } while (totalWritten < limit && (bytesRead > 0 || is.available() > 0));
            return totalWritten;
        } finally {
            releaseTempBuffer(buffer);
        }
    }
    
    /**
     * Opimized writing to FileOutputStream using a channel
     * @param is
     * @param fos
     * @return false if lock was not aquired
     * @throws IOException
     */
    public static boolean inputStream2FileOutputStream(InputStream is, 
                                                FileOutputStream fos)
        throws IOException {
        
        // See if a file channel and lock can be obtained on the FileOutputStream
        FileChannel channel = null;
        FileLock lock = null;
        ByteBuffer bb = null;
        try {
            channel = fos.getChannel();
            if (channel != null) {
                lock = channel.tryLock();
            }
            bb = getTempByteBuffer();
        } catch (Throwable t) {
        }
        if (lock == null || bb == null || !bb.hasArray()) {
            releaseTempByteBuffer(bb);
            return false;  // lock could not be set or bb does not have direct array access
        }
        
        try {

            // Read directly into the ByteBuffer array
            int bytesRead = is.read(bb.array());
            // Continue reading until no bytes are read and no
            // bytes are now available.
            while (bytesRead > 0 || is.available() > 0) {
                if (bytesRead > 0) {
                    int written = 0;
                    
                    
                    if (bytesRead < BUFFER_LEN) {
                        // If the ByteBuffer is not full, allocate a new one
                        ByteBuffer temp = ByteBuffer.allocate(bytesRead);
                        temp.put(bb.array(), 0, bytesRead);
                        temp.position(0);
                        written = channel.write(temp);
                    } else {
                        // Write to channel
                        bb.position(0);
                        written = channel.write(bb);
                        bb.clear();
                    }
                  
                }
                
                // REVIEW: Do we need to ensure that bytesWritten is 
                // the same as the number of bytes sent ?
                
                bytesRead = is.read(bb.array());
            }
        } finally {
            // Release the lock
           lock.release();
           releaseTempByteBuffer(bb);
        }
        return true;
    }
    
    /** 
     * inputStream2BAAOutputStream
     * @param is
     * @param baaos
     * @param limit
     * @return TODO
     */
    public static long inputStream2BAAOutputStream(InputStream is, 
                                               BAAOutputStream baaos,
                                               long limit) throws IOException {
        return baaos.receive(is, limit);
    }
    
    /**
     * Exception used by SizeLimitedOutputStream if the size limit has been exceeded.
     */
    private static class SizeLimitExceededException extends IOException {
        private static final long serialVersionUID = -6644887187061182165L;
    }
    
    /**
     * An output stream that counts the number of bytes written to it and throws an
     * exception when the size exceeds a given limit.
     */
    private static class SizeLimitedOutputStream extends OutputStream {
        private final int maxSize;
        private int size;
        
        public SizeLimitedOutputStream(int maxSize) {
            this.maxSize = maxSize;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            size += len;
            checkSize();
        }

        public void write(byte[] b) throws IOException {
            size += b.length;
            checkSize();
        }

        public void write(int b) throws IOException {
            size++;
            checkSize();
        }
        
        private void checkSize() throws SizeLimitExceededException {
            if (size > maxSize) {
                throw new SizeLimitExceededException();
            }
        }
    }

    /**
     * The method checks to see if attachment is eligble for optimization.
     * An attachment is eligible for optimization if and only if the size of 
     * the attachment is greated then the optimzation threshold size limit. 
     * if the Content represented by DataHandler has size less than the 
     * optimize threshold size, the attachment will not be eligible for 
     * optimization, instead it will be inlined.
     * @param dh
     * @param limit
     * @return 1 if DataHandler data is bigger than limit, 0 if DataHandler data is smaller or
     * -1 if an error occurs or unsupported.
     * @throws IOException
     */
    public static int doesDataHandlerExceedLimit(DataHandler dh, int limit){
        //If Optimized Threshold not set return true.
        if(limit==0){
            return -1;
        }
        long size = DataSourceUtils.getSize(dh.getDataSource());
        if (size != -1) {
            return size > limit ? 1 : 0;
        } else {
            // In all other cases, we prefer DataHandler#writeTo over DataSource#getInputStream.
            // The reason is that if the DataHandler was constructed from an Object rather than
            // a DataSource, a call to DataSource#getInputStream() will start a new thread and
            // return a PipedInputStream. This is so for Geronimo's as well as Sun's JAF
            // implementaion. The reason is that DataContentHandler only has a writeTo and no
            // getInputStream method. Obviously starting a new thread just to check the size of
            // the data is an overhead that we should avoid.
            try {
                dh.writeTo(new SizeLimitedOutputStream(limit));
            } catch (SizeLimitExceededException ex) {
                return 1;
            } catch (IOException ex) {
                log.warn(ex.getMessage());
                return -1;
            }
            return 0;
        }
    }
    
    private static synchronized byte[] getTempBuffer() {
        // Try using cached buffer
        synchronized(_cacheBuffer) {
            if (!_cacheBufferInUse) {
                _cacheBufferInUse = true;
                return _cacheBuffer;
            }
        }
        
        // Cache buffer in use, create new buffer
        return new byte[BUFFER_LEN];
    }
    
    private static void releaseTempBuffer(byte[] buffer) {
        // Try using cached buffer
        synchronized(_cacheBuffer) {
            if (buffer == _cacheBuffer) {
                _cacheBufferInUse = false;
            }
        }
    }
    
    private static synchronized ByteBuffer getTempByteBuffer() {
        // Try using cached buffer
        synchronized(_cacheByteBuffer) {
            if (!_cacheByteBufferInUse) {
                _cacheByteBufferInUse = true;
                return _cacheByteBuffer;
            }
        }
        
        // Cache buffer in use, create new buffer
        return ByteBuffer.allocate(BUFFER_LEN);
    }
    
    private static void releaseTempByteBuffer(ByteBuffer buffer) {
        // Try using cached buffer
        synchronized(_cacheByteBuffer) {
            if (buffer == _cacheByteBuffer) {
                _cacheByteBufferInUse = false;
            }
        }
    }
}
