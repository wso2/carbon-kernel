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

package org.apache.axiom.util.blob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.axiom.ext.io.ReadFromSupport;
import org.apache.axiom.ext.io.StreamCopyException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Blob implementation that stores data in a temporary file if the size exceeds a configurable
 * threshold. The data is first stored into a fixed size buffer. Once this buffer overflows, it is
 * transferred to a temporary file. The buffer is divided into a given number of fixed size chunks
 * that are allocated on demand. Since a temporary file may be created it is mandatory to call
 * {@link #release()} to discard the blob.
 */
public class OverflowBlob implements WritableBlob {
    private static final Log log = LogFactory.getLog(OverflowBlob.class);
    
    static final int STATE_NEW = 0;
    static final int STATE_UNCOMMITTED = 1;
    static final int STATE_COMMITTED = 2;
    
    class OutputStreamImpl extends BlobOutputStream {
        
        private FileOutputStream fileOutputStream;
        
        public WritableBlob getBlob() {
            return OverflowBlob.this;
        }

        public void write(byte[] b, int off, int len) throws IOException {

            if (fileOutputStream != null) {
                fileOutputStream.write(b, off, len);
            } else if (len > (chunks.length-chunkIndex)*chunkSize - chunkOffset) {

                // The buffer will overflow. Switch to a temporary file.
                fileOutputStream = switchToTempFile();
                
                // Write the new data to the temporary file.
                fileOutputStream.write(b, off, len);

            } else {

                // The data will fit into the buffer.
                while (len > 0) {

                    byte[] chunk = getCurrentChunk();

                    // Determine number of bytes that can be copied to the current chunk.
                    int c = Math.min(len, chunkSize-chunkOffset);
                    // Copy data to the chunk.
                    System.arraycopy(b, off, chunk, chunkOffset, c);

                    // Update variables.
                    len -= c;
                    off += c;
                    chunkOffset += c;
                    if (chunkOffset == chunkSize) {
                        chunkIndex++;
                        chunkOffset = 0;
                    }
                }
            }
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void write(int b) throws IOException {
            write(new byte[] { (byte)b }, 0, 1);
        }

        public void flush() throws IOException {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
            }
        }

        public void close() throws IOException {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            state = STATE_COMMITTED;
        }
    }
    
    class InputStreamImpl extends InputStream {

        private int currentChunkIndex;
        private int currentChunkOffset;
        private int markChunkIndex;
        private int markChunkOffset;
        
        public int available() throws IOException {
            return (chunkIndex-currentChunkIndex)*chunkSize + chunkOffset - currentChunkOffset;
        }

        public int read(byte[] b, int off, int len) throws IOException {

            if (len == 0) {
                return 0;
            }

            int read = 0;
            while (len > 0 && !(currentChunkIndex == chunkIndex
                    && currentChunkOffset == chunkOffset)) {

                int c;
                if (currentChunkIndex == chunkIndex) {
                    // The current chunk is the last one => take into account the offset
                    c = Math.min(len, chunkOffset-currentChunkOffset);
                } else {
                    c = Math.min(len, chunkSize-currentChunkOffset);
                }

                // Copy the data.
                System.arraycopy(chunks[currentChunkIndex], currentChunkOffset, b, off, c);

                // Update variables
                len -= c;
                off += c;
                currentChunkOffset += c;
                read += c;
                if (currentChunkOffset == chunkSize) {
                    currentChunkIndex++;
                    currentChunkOffset = 0;
                }
            }

            if (read == 0) {
                // We didn't read anything (and the len argument was not 0) => we reached the end of the buffer.
                return -1;
            } else {
                return read;
            }
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            return read(b) == -1 ? -1 : b[0] & 0xFF;
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int readlimit) {
            markChunkIndex = currentChunkIndex;
            markChunkOffset = currentChunkOffset;
        }

        public void reset() throws IOException {
            currentChunkIndex = markChunkIndex;
            currentChunkOffset = markChunkOffset;
        }

        public long skip(long n) throws IOException {

            int available = available();
            int c = n < available ? (int)n : available;
            int newOffset = currentChunkOffset + c;
            int chunkDelta = newOffset/chunkSize;
            currentChunkIndex += chunkDelta;
            currentChunkOffset = newOffset - (chunkDelta*chunkSize);
            return c;
        }
        
        public void close() throws IOException {
        }
    }
    
    /**
     * Size of the chunks that will be allocated in the buffer.
     */
    final int chunkSize;
    
    /**
     * The prefix to be used in generating the name of the temporary file.
     */
    final String tempPrefix;
    
    /**
     * The suffix to be used in generating the name of the temporary file.
     */
    final String tempSuffix;
    
    /**
     * Array of <code>byte[]</code> representing the chunks of the buffer.
     * A chunk is only allocated when the first byte is written to it.
     * This attribute is set to <code>null</code> when the buffer overflows and
     * is written out to a temporary file.
     */
    byte[][] chunks;
    
    /**
     * Index of the chunk the next byte will be written to.
     */
    int chunkIndex;
    
    /**
     * Offset into the chunk where the next byte will be written.
     */
    int chunkOffset;
    
    /**
     * The handle of the temporary file. This is only set when the memory buffer
     * overflows and is written out to a temporary file.
     */
    File temporaryFile;
    
    /**
     * The state of the blob.
     */
    int state = STATE_NEW;
    
    public OverflowBlob(int numberOfChunks, int chunkSize, String tempPrefix, String tempSuffix) {
        this.chunkSize = chunkSize;
        this.tempPrefix = tempPrefix;
        this.tempSuffix = tempSuffix;
        chunks = new byte[numberOfChunks][];
    }
    
    public boolean isSupportingReadUncommitted() {
        // This is actually a limitation of the implementation, not an intrinsic limitation
        return false;
    }

    /**
     * Get the current chunk to write to, allocating it if necessary.
     * 
     * @return the current chunk to write to (never null)
     */
    byte[] getCurrentChunk() {
        if (chunkOffset == 0) {
            // We will write the first byte to the current chunk. Allocate it.
            byte[] chunk = new byte[chunkSize];
            chunks[chunkIndex] = chunk;
            return chunk;
        } else {
            // The chunk has already been allocated.
            return chunks[chunkIndex];
        }
    }
    
    /**
     * Create a temporary file and write the existing in memory data to it.
     * 
     * @return an open FileOutputStream to the temporary file
     * @throws IOException
     */
    FileOutputStream switchToTempFile() throws IOException {
        temporaryFile = File.createTempFile(tempPrefix, tempSuffix);
        if (log.isDebugEnabled()) {
            log.debug("Using temporary file " + temporaryFile);
        }
        temporaryFile.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream(temporaryFile);
        // Write the buffer to the temporary file.
        for (int i=0; i<chunkIndex; i++) {
            fileOutputStream.write(chunks[i]);
        }

        if (chunkOffset > 0) {
            fileOutputStream.write(chunks[chunkIndex], 0, chunkOffset);
        }

        // Release references to the buffer so that it can be garbage collected.
        chunks = null;
        
        return fileOutputStream;
    }
    
    public BlobOutputStream getOutputStream() {
        if (state != STATE_NEW) {
            throw new IllegalStateException();
        } else {
            state = STATE_UNCOMMITTED;
            return new OutputStreamImpl();
        }
    }
    
    public long readFrom(InputStream in, long length, boolean commit) throws StreamCopyException {
        // TODO: this will not work if the blob is in state UNCOMMITTED and we have already switched to a temporary file
        long read = 0;
        long toRead = length == -1 ? Long.MAX_VALUE : length;
        while (true) {
            int c;
            try {
                int len = chunkSize-chunkOffset;
                if (len > toRead) {
                    len = (int)toRead;
                }
                c = in.read(getCurrentChunk(), chunkOffset, len);
            } catch (IOException ex) {
                throw new StreamCopyException(StreamCopyException.READ, ex);
            }
            if (c == -1) {
                break;
            }
            read += c;
            toRead -= c;
            chunkOffset += c;
            if (chunkOffset == chunkSize) {
                chunkIndex++;
                chunkOffset = 0;
                if (chunkIndex == chunks.length) {
                    FileOutputStream fileOutputStream;
                    try {
                        fileOutputStream = switchToTempFile();
                    } catch (IOException ex) {
                        throw new StreamCopyException(StreamCopyException.WRITE, ex);
                    }
                    byte[] buf = new byte[4096];
                    while (true) {
                        int c2;
                        try {
                            c2 = in.read(buf, 0, (int)Math.min(toRead, 4096));
                        } catch (IOException ex) {
                            throw new StreamCopyException(StreamCopyException.READ, ex);
                        }
                        if (c2 == -1) {
                            break;
                        }
                        try {
                            fileOutputStream.write(buf, 0, c2);
                        } catch (IOException ex) {
                            throw new StreamCopyException(StreamCopyException.WRITE, ex);
                        }
                        read += c2;
                        toRead -= c2;
                    }
                    try {
                        fileOutputStream.close();
                    } catch (IOException ex) {
                        throw new StreamCopyException(StreamCopyException.WRITE, ex);
                    }
                    break;
                }
            }
        }
        state = commit ? STATE_COMMITTED : STATE_UNCOMMITTED;
        return read;
    }
    
    public long readFrom(InputStream in, long length) throws StreamCopyException {
        return readFrom(in, length, state == STATE_NEW);
    }

    public InputStream getInputStream() throws IOException {
        if (state != STATE_COMMITTED) {
            throw new IllegalStateException();
        } else if (temporaryFile != null) {
            return new FileInputStream(temporaryFile);
        } else {
            return new InputStreamImpl();
        }
    }
    
    public void writeTo(OutputStream out) throws StreamCopyException {
        if (temporaryFile != null) {
            FileInputStream in;
            try {
                in = new FileInputStream(temporaryFile);
            } catch (IOException ex) {
                throw new StreamCopyException(StreamCopyException.READ, ex);
            }
            try {
                if (out instanceof ReadFromSupport) {
                    ((ReadFromSupport)out).readFrom(in, -1);
                } else {
                    byte[] buf = new byte[4096];
                    while (true) {
                        int c;
                        try {
                            c = in.read(buf);
                        } catch (IOException ex) {
                            throw new StreamCopyException(StreamCopyException.READ, ex);
                        }
                        if (c == -1) {
                            break;
                        }
                        try {
                            out.write(buf, 0, c);
                        } catch (IOException ex) {
                            throw new StreamCopyException(StreamCopyException.WRITE, ex);
                        }
                    }
                }
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    throw new StreamCopyException(StreamCopyException.READ, ex);
                }
            }
        } else {
            try {
                for (int i=0; i<chunkIndex; i++) {
                    out.write(chunks[i]);
                }
                if (chunkOffset > 0) {
                    out.write(chunks[chunkIndex], 0, chunkOffset);
                }
            } catch (IOException ex) {
                throw new StreamCopyException(StreamCopyException.WRITE, ex);
            }
        }
    }
    
    public long getLength() {
        if (temporaryFile != null) {
            return temporaryFile.length();
        } else {
            return chunkIndex*chunkSize + chunkOffset;
        }
    }
    
    public void release() {
        if (temporaryFile != null) {
            if (log.isDebugEnabled()) {
                log.debug("Deleting temporary file " + temporaryFile);
            }
            temporaryFile.delete();
        }
    }

    protected void finalize() throws Throwable {
        if (temporaryFile != null) {
            log.warn("Cleaning up unreleased temporary file " + temporaryFile);
            temporaryFile.delete();
        }
    }
}
