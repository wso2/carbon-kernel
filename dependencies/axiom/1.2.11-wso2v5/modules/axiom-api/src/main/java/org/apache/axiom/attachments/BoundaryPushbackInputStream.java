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

package org.apache.axiom.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.axiom.attachments.utils.ByteSearch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An InputStream that reads bytes up to a boundary.
 * The boundary is not logically part of the bytes to read.
 * The wrapped PushbackInputStream is set to to the byte after
 * the boundary once the bytes are read.
 * The boundary is not logically returned.
 * 
 * There are two forms that are supported, where . is a byte 
 * 
 * .......................boundary
 * 
 * and
 * 
 * ..................../r/nboundary
 * 
 * In both cases, only the bytes (.) are returned.
 *
 */
public class BoundaryPushbackInputStream extends InputStream {
    
    private static Log log = LogFactory.getLog(BoundaryPushbackInputStream.class);
    private static boolean isDebugEnabled = log.isDebugEnabled();
    PushbackInputStream is;

    boolean boundaryFound;
    byte[] boundary;
    int rnBoundaryLen;  // '/r/nboundary' length
    
    byte[] buffer;
    int bufferSize;     // BufferSize
    int numBytes;       // Number of bytes in the buffer
    int index = -1;     // Current index in buffer
    int bIndex = -1;    // Index of boundary or /r/nboundary
    final int MIN_BUF_SIZE = 32;
    protected static final int BOUNDARY_NT_FOUND = -1;
    
    // Skip search array
    private short[] skip = null;
    
    // Working byte for read()
    private byte[] read_byte = new  byte[1];

    /**
     * @param inStream
     * @param boundary
     * @param pushBackSize
     */
    public BoundaryPushbackInputStream(PushbackInputStream inStream, byte[] boundary, int pushBackSize) {
        super();
        this.is = inStream;
        this.boundary = boundary;
        this.rnBoundaryLen = boundary.length + 2;
        
        // The buffer must accomodate twice the boundary length and
        // the maximum that we will ever push back (which is the entire buffer except for 
        // the boundary)
        this.bufferSize = Math.max(rnBoundaryLen * 2, pushBackSize + boundary.length);
    }
    
    /**
     * Method readFromStream
     *
     * @param b
     * @param start
     * @param length
     *
     * @return
     *
     * @throws java.io.IOException
     */
    private final int readFromStream(
            final byte[] b, final int start, final int length)
            throws java.io.IOException {

        // We need to make sure to capture enough data to 
        // actually search for the rn + boundary
        int minRead = Math.max(rnBoundaryLen * 2, length);
        minRead = Math.min(minRead, length - start);

        int br = 0;
        int brTotal = 0;

        do {
            // Read data into the buffer
            br = is.read(b, brTotal + start, length - brTotal);

            if (br > 0) {
                brTotal += br;
            }
        } while ((br > 0) && (brTotal < minRead));

        return (brTotal != 0)
                ? brTotal
                : br;
    }
    
    
    /**
     * @param b
     * @return
     * @throws java.io.IOException
     */
    private final int readFromStream(final byte[] b)
            throws java.io.IOException {
        return readFromStream(b, 0, b.length);
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws java.io.IOException {
        return read(b, 0, b.length);
    }
    
    /**
     * Read from the boundary delimited stream.
     * Generally, this won't be called...callers will
     * most likely call the read(byte[]..) methods
     * @return The byte read, or -1 if endof stream.
     *
     * @throws java.io.IOException
     */
    
    public int read() throws java.io.IOException {
  
        // Short cut to avoid buffer copying
        if (buffer != null && index > 0) {
            if ((bIndex > 0 && (index+1) < bIndex) ||
                 bIndex < 0 && index < (numBytes - rnBoundaryLen)) {
                index++;
                return (buffer[index-1]);
            }
        }
  
        int read = read(read_byte);

        if (read < 0) {
            return -1;
        } else {
            return read_byte[0];
        }
    }

    /**
     * Read from the boundary delimited stream.
     * @param b is the array to read into.
     * @param off is the offset
     * @param len
     * @return the number of bytes read. -1 if endof stream.
     *
     * @throws java.io.IOException
     */
    public int read(byte[] b, final int off, final int len)
            throws java.io.IOException {

        // If already found the buffer, then we are done
        if (boundaryFound) {
            return -1;
        }

        // The first time called, read a chunk of data
        if (buffer == null) {    // Allocate the buffer.
            buffer = new byte[bufferSize];
            numBytes = readFromStream(buffer);

            if (numBytes < 0) {
                buffer = null;
                boundaryFound = true;
            }

            index = 0;

            // Finds the boundary pos.
            bIndex = boundaryPosition(buffer, index, numBytes);
            if (bIndex >=0) {
                unread();  // Unread pushback inputstream
            }
        }

        int bwritten = 0;    // Number of bytes written to b

        
        do {
            
            // Never read to the end of the buffer because
            // the boundary may span buffers.
            int bcopy = Math.min((numBytes - rnBoundaryLen) - index,
                    len - bwritten);

            // Never read past the boundary
            if (bIndex >= 0) {
                bcopy = Math.min(bcopy, bIndex - index);
            }

            // Copy the bytes 
            if (bcopy > 0) {
                System.arraycopy(buffer, index, b, off + bwritten, bcopy);

                bwritten += bcopy;
                index += bcopy;
            }

            if (index == bIndex) {
                boundaryFound = true;  

            } else if (bwritten < len) {    
                
                // If more data is needed,
                // create a temporary buffer to span 
                // the straggling bytes in the current buffer
                // and the new yet unread bytes
                byte[] dstbuf = buffer;

                // Move straggling bytes from the current buffer
                int movecnt = numBytes - index;
                System.arraycopy(buffer, index, dstbuf, 0, movecnt);

                // Read in the new data.
                int readcnt = readFromStream(dstbuf, movecnt,
                        dstbuf.length - movecnt);

                if (readcnt < 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("End of Stream, but boundary not found");
                        log.debug(toString());
                    }
                    buffer = null;
                    boundaryFound = true;
                    throw new java.io.IOException("End of Stream, but boundary not found");
                }

                numBytes = readcnt + movecnt;
                buffer = dstbuf;
                index = 0;             // start at the begining.

                // just move the boundary by what we moved
                if (bIndex >=0) {
                    bIndex -= movecnt;
                } else {
                    bIndex = boundaryPosition(
                            buffer, index,
                            numBytes);       
                    if (bIndex >= 0) {
                        unread();  // Unread pushback inputstream
                    }
                }
            }
        }
        // read till we get the amount or the stream is finished.
        while (!boundaryFound && (bwritten < len));

        if (boundaryFound) {
            buffer = null;    // GC the buffer
        }

        return bwritten;
    }
    
    /**
     * Unread the bytes past the buffer
     */
    private void unread() throws IOException {
        
        int i = bIndex;
        if (buffer[i] == 13) { // If /r, must be /r/nboundary
            i = i + this.rnBoundaryLen;
        } else { 
            i = i + boundary.length;
        }
        if (numBytes - i > 0) {
            is.unread(buffer, i, numBytes - i);
        }
    }
    
    /**
     * Read from the boundary delimited stream.
     *
     * @param searchbuf
     * @param start
     * @param end
     * @return The position of the boundary.
     *
     */
    protected int boundaryPosition(byte[] searchbuf, int start, int end) throws java.io.IOException  {

        if (skip == null) {
            skip = ByteSearch.getSkipArray(boundary, true);
        }
        int foundAt = ByteSearch.skipSearch(boundary, true,searchbuf, start, end, skip);

        // Backup 2 if the boundary is preceeded by /r/n
        // The /r/n are treated as part of the boundary
        if (foundAt >=2) {
            if (searchbuf[foundAt-2] == 13 &&
                searchbuf[foundAt-1] == 10) {
                foundAt = foundAt -2;
            }
        }

        return foundAt;
    }
    
    public boolean getBoundaryStatus()
    {
        return boundaryFound;
    }
    
    /**
     * toString
     * dumps state information.  Effective for debug trace.
     */
    public String toString() {
        final String newline = "\n";
        StringBuffer sb = new StringBuffer();
        
        sb.append("========================");
        sb.append(newline);
        sb.append("BoundaryPushbackInputStream");
        sb.append(newline);
        sb.append("  boundary       = " + new String(this.boundary) );
        sb.append(newline);
        sb.append(" boundaryFound   = " + boundaryFound);
        sb.append(newline);
        int available = 0;
        try {
            available = is.available();
        } catch (Throwable t) {
            ;  // Suppress...toString is called for debug
        }
        sb.append("  is available       = " + available );
        sb.append(newline);
        sb.append("  bufferSize     = " + bufferSize);
        sb.append(newline);
        sb.append("  bufferNumBytes = " + bufferSize);
        sb.append(newline);    
        sb.append("  bufferIndex    = " + index);
        sb.append(newline);
        sb.append("  boundaryIndex  = " + bIndex);
        sb.append(newline);
        sb.append("  buffer[0,num]  = " + new String(buffer, 0, numBytes));
        sb.append(newline);
        sb.append("========================");
        
        return sb.toString();
    }
}