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

package org.apache.axis2.saaj.util;

import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class SAAJDataSource implements javax.activation.DataSource {

    /** The content type. This defaults to <code>application/octet-stream</code>. */
    protected String contentType = "application/octet-stream";

    /** The incoming source stream. */
    private InputStream ss;

    /** Field MIN_MEMORY_DISK_CACHED */
    public static final int MIN_MEMORY_DISK_CACHED = -1;

    /** Field MAX_MEMORY_DISK_CACHED */
    public static final int MAX_MEMORY_DISK_CACHED = 16 * 1024;

    /** Field maxCached */
    protected int maxCached = MAX_MEMORY_DISK_CACHED;       // max in memory cached. Default.

    /** Field diskCacheFile */
    protected java.io.File diskCacheFile = null;

    // A list of open input Streams.

    /** Field readers */
    protected java.util.WeakHashMap readers = new java.util.WeakHashMap();

    /** Flag to show if the resources behind this have been deleted. */
    protected boolean deleted;

    /** Field READ_CHUNK_SZ */
    public static final int READ_CHUNK_SZ = 32 * 1024;


    /** The linked list to hold the in memory buffers. */
    protected java.util.LinkedList memorybuflist = new java.util.LinkedList();

    /** Hold the last memory buffer. */
    protected byte[] currentMemoryBuf = null;

    /** The number of bytes written to the above buffer. */
    protected int currentMemoryBufSz;

    /** The total size in bytes in this data source. */
    protected long totalsz;

    /** This is the cached disk stream. */
    protected java.io.BufferedOutputStream cachediskstream;

    /** If true the source input stream is now closed. */
    protected boolean closed = false;

    /** Constructor SAAJDataSource. */
    protected SAAJDataSource() {
    }

    /**
     * Create a new boundary stream.
     *
     * @param ss          is the source input stream that is used to create this data source.
     * @param maxCached   This is the max memory that is to be used to cache the data.
     * @param contentType the mime type for this data stream. by buffering you can some effiency in
     *                    searching.
     * @throws java.io.IOException
     */
    public SAAJDataSource(InputStream ss, int maxCached, String contentType)
            throws java.io.IOException {
        this(ss, maxCached, contentType, false);
    }

    /**
     * Create a new boundary stream.
     *
     * @param ss          is the source input stream that is used to create this data source.
     * @param maxCached   This is the max memory that is to be used to cache the data.
     * @param contentType the mime type for this data stream. by buffering you can some effiency in
     *                    searching.
     * @param readall     if true will read in the whole source.
     * @throws java.io.IOException
     */
    public SAAJDataSource(InputStream ss,
                          int maxCached,
                          String contentType, boolean readall) throws java.io.IOException {

        if (ss instanceof BufferedInputStream) {
            this.ss = ss;
        } else {
            this.ss = new BufferedInputStream(ss);
        }
        this.maxCached = maxCached;
        if ((null != contentType) && (contentType.length() != 0)) {
            this.contentType = contentType;
        }
        if (maxCached < MIN_MEMORY_DISK_CACHED) {
            throw new IllegalArgumentException("badMaxCached " + maxCached);
        }

        // for now read all in to disk.
        if (readall) {
            byte[] readbuffer = new byte[READ_CHUNK_SZ];

            int read = 0;
            do {
                read = ss.read(readbuffer);

                if (read > 0) {
                    writeToMemory(readbuffer, read);
                }
            } while (read > -1);
            close();
        }
    }

    /**
     * This method is a low level write. Close the stream.
     *
     * @throws java.io.IOException
     */
    protected synchronized void close() throws java.io.IOException {

        if (!closed) {
            closed = true;                    // Markit as closed.
            if (null != cachediskstream) {    // close the disk cache.
                cachediskstream.close();

                cachediskstream = null;
            }
            if (null != memorybuflist) {      // There is a memory buffer.
                if (currentMemoryBufSz > 0) {
                    byte[] tmp =
                            new byte[currentMemoryBufSz];    // Get the last buffer and make it the sizeof the actual data.

                    System.arraycopy(currentMemoryBuf, 0, tmp, 0,
                                     currentMemoryBufSz);
                    memorybuflist.set(memorybuflist.size() - 1,
                                      tmp);                 // Now replace the last buffer with this size.
                }
                currentMemoryBuf = null;      // No need for this anymore.
            }
        }
    }

    /**
     * Routine to flush data to disk if is in memory.
     *
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     */
    protected void flushToDisk() throws IOException, FileNotFoundException {
        LinkedList ml = memorybuflist;
        if (ml != null) {
            if (null == cachediskstream) {    // Need to create a disk cache
                try {
                    /* MessageContext mc = MessageContext.getCurrentContext();
            String attdir = (mc == null)
                            ? null
                            : mc.getStrProp(
                    MessageContext.ATTACHMENTS_DIR);*/


                    MessageContext messageContext = MessageContext.getCurrentMessageContext();
                    String attachementDir = "";
                    attachementDir = (String)messageContext.getProperty
                            (Constants.Configuration.ATTACHMENT_TEMP_DIR);

                    if (attachementDir.equals("")) {
                        Parameter param = (Parameter)messageContext.getParameter
                                (Constants.Configuration.ATTACHMENT_TEMP_DIR);
                        if (param != null) {
                            attachementDir = (String)param.getValue();
                        }
                    }

                    diskCacheFile = java.io.File.createTempFile("Axis", ".att",
                                                                (attachementDir == null)
                                                                        ? null
                                                                        : new File(
                                                                        attachementDir));
                    cachediskstream = new BufferedOutputStream(new FileOutputStream(diskCacheFile));
                    int listsz = ml.size();

                    // Write out the entire memory held store to disk.
                    for (java.util.Iterator it = ml.iterator();
                         it.hasNext();) {
                        byte[] rbuf = (byte[])it.next();
                        int bwrite = (listsz-- == 0)
                                ? currentMemoryBufSz
                                : rbuf.length;
                        cachediskstream.write(rbuf, 0, bwrite);
                        if (closed) {
                            cachediskstream.close();
                            cachediskstream = null;
                        }
                    }
                    memorybuflist = null;
                } catch (java.lang.SecurityException se) {
                    diskCacheFile = null;
                    cachediskstream = null;
                    maxCached = java.lang.Integer.MAX_VALUE;
                }
            }
        }
    }

    /**
     * Write bytes to the stream.
     *
     * @param data all bytes of this array are written to the stream
     * @throws java.io.IOException if there was a problem writing the data
     */
    protected void write(byte[] data) throws java.io.IOException {
        write(data, data.length);
    }

    /**
     * This method is a low level write. Note it is designed to in the future to allow streaming to
     * both memory AND to disk simultaneously.
     *
     * @param data
     * @param length
     * @throws java.io.IOException
     */
    protected synchronized void write(byte[] data, int length) throws java.io.IOException {

        if (closed) {
            throw new java.io.IOException("streamClosed");
        }

        int byteswritten = 0;

        if ((null != memorybuflist)
                && (totalsz + length > maxCached)) {    // Cache to disk.
            if (null == cachediskstream) {               // Need to create a disk cache
                flushToDisk();
            }
        }

        if (memorybuflist != null) {    // Can write to memory.
            do {
                if (null == currentMemoryBuf) {
                    currentMemoryBuf = new byte[READ_CHUNK_SZ];
                    currentMemoryBufSz = 0;

                    memorybuflist.add(currentMemoryBuf);
                }

                // bytes to write is the min. between the remaining bytes and what is left in this buffer.
                int bytes2write = Math.min((length - byteswritten),
                                           (currentMemoryBuf.length
                                                   - currentMemoryBufSz));

                // copy the data.
                System.arraycopy(data, byteswritten, currentMemoryBuf,
                                 currentMemoryBufSz, bytes2write);

                byteswritten += bytes2write;
                currentMemoryBufSz += bytes2write;

                if (byteswritten
                        < length) {    // only get more if we really need it.
                    currentMemoryBuf = new byte[READ_CHUNK_SZ];
                    currentMemoryBufSz = 0;
                    memorybuflist.add(currentMemoryBuf);    // add it to the chain.
                }
            } while (byteswritten < length);
        }

        if (null != cachediskstream) {    // Write to the out going stream.
            cachediskstream.write(data, 0, length);
        }
        totalsz += length;
    }


    /**
     * This method is a low level write. Writes only to memory
     *
     * @param data
     * @param length
     * @throws java.io.IOException
     */
    protected synchronized void writeToMemory(byte[] data, int length) throws java.io.IOException {

        if (closed) {
            throw new java.io.IOException("streamClosed");
        }

        int byteswritten = 0;
        if (memorybuflist != null) {    // Can write to memory.
            do {
                if (null == currentMemoryBuf) {
                    currentMemoryBuf = new byte[READ_CHUNK_SZ];
                    currentMemoryBufSz = 0;

                    memorybuflist.add(currentMemoryBuf);
                }

                // bytes to write is the min. between the remaining bytes and what is left in this buffer.
                int bytes2write = Math.min((length - byteswritten),
                                           (currentMemoryBuf.length
                                                   - currentMemoryBufSz));

                // copy the data.
                System.arraycopy(data, byteswritten, currentMemoryBuf,
                                 currentMemoryBufSz, bytes2write);

                byteswritten += bytes2write;
                currentMemoryBufSz += bytes2write;

                if (byteswritten
                        < length) {    // only get more if we really need it.
                    currentMemoryBuf = new byte[READ_CHUNK_SZ];
                    currentMemoryBufSz = 0;
                    memorybuflist.add(currentMemoryBuf);    // add it to the chain.
                }
            } while (byteswritten < length);
        }
        totalsz += length;
    }

    /**
     * get the filename of the content if it is cached to disk.
     *
     * @return file object pointing to file, or null for memory-stored content
     */
    public File getDiskCacheFile() {
        return diskCacheFile;
    }

    public InputStream getInputStream() throws IOException {
        return new SAAJInputStream();    // Return the memory held stream.
    }

    public OutputStream getOutputStream() throws IOException {
        //TODO: Method implementation
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        String ret = null;
        try {
            flushToDisk();
            if (diskCacheFile != null) {
                ret = diskCacheFile.getAbsolutePath();
            }
        } catch (Exception e) {
            diskCacheFile = null;
        }
        return ret;
    }


    /**
     * Inner class to handle getting an input stream to this data source Handles creating an input
     * stream to the source.
     */
    private class SAAJInputStream extends java.io.InputStream {

        /** bytes read. */
        protected long bread = 0;

        /** The real stream. */
        private FileInputStream fileIS;

        /** The position in the list were we are reading from. */
        int currentIndex;

        /** the buffer we are currently reading from. */
        byte[] currentBuf;

        /** The current position in there. */
        int currentBufPos;

        /** The read stream has been closed. */
        boolean readClosed;

        /**
         * Constructor Instream.
         *
         * @throws java.io.IOException if the Instream could not be created or if the data source
         *                             has been deleted
         */
        protected SAAJInputStream() throws java.io.IOException {
            if (deleted) {
                throw new java.io.IOException("resourceDeleted");
            }
            readers.put(this, null);
        }

        /**
         * Query for the number of bytes available for reading.
         *
         * @return the number of bytes left
         * @throws java.io.IOException if this stream is not in a state that supports reading
         */
        public int available() throws java.io.IOException {
            if (deleted) {
                throw new java.io.IOException("resourceDeleted");
            }
            if (readClosed) {
                throw new java.io.IOException("streamClosed");
            }
            return new Long(Math.min(Integer.MAX_VALUE, totalsz - bread)).intValue();
        }

        /**
         * Read a byte from the stream.
         *
         * @return byte read or -1 if no more data.
         * @throws java.io.IOException
         */
        public int read() throws java.io.IOException {
            synchronized (SAAJDataSource.this) {
                byte[] retb = new byte[1];
                int br = read(retb, 0, 1);
                if (br == -1) {
                    return -1;
                }
                return 0xFF & retb[0];
            }
        }

        /** Not supported. */
        public boolean markSupported() {
            return false;
        }

        /**
         * Not supported.
         *
         * @param readlimit
         */
        public void mark(int readlimit) {
        }

        /**
         * Not supported.
         *
         * @throws java.io.IOException
         */
        public void reset() throws IOException {
            throw new IOException("noResetMark");
        }

        public long skip(long skipped) throws IOException {

            if (deleted) {
                throw new IOException("resourceDeleted");
            }

            if (readClosed) {
                throw new IOException("streamClosed");
            }

            if (skipped < 1) {
                return 0;    // nothing to skip.
            }

            synchronized (SAAJDataSource.this) {
                skipped = Math.min(skipped, totalsz - bread);    // only skip what we've read.
                if (skipped == 0) {
                    return 0;
                }
                List ml = memorybuflist;    // hold the memory list.
                int bwritten = 0;
                if (ml != null) {
                    if (null == currentBuf) {    // get the buffer we need to read from.
                        currentBuf = (byte[])ml.get(currentIndex);
                        currentBufPos = 0;    // start reading from the begining.
                    }
                    do {
                        long bcopy = Math.min(currentBuf.length - currentBufPos,
                                              skipped - bwritten);

                        bwritten += bcopy;
                        currentBufPos += bcopy;
                        if (bwritten < skipped) {
                            currentBuf = (byte[])ml.get(++currentIndex);
                            currentBufPos = 0;
                        }
                    } while (bwritten < skipped);
                }

                if (null != fileIS) {
                    fileIS.skip(skipped);
                }

                bread += skipped;
            }
            return skipped;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (deleted) {
                throw new IOException("resourceDeleted");
            }

            if (readClosed) {
                throw new IOException("streamClosed");
            }

            if (b == null) {
                throw new RuntimeException("nullInput");
            }

            if (off < 0) {
                throw new IndexOutOfBoundsException("negOffset " + off);
            }

            if (len < 0) {
                throw new IndexOutOfBoundsException("length " + len);
            }

            if (len + off > b.length) {
                throw new IndexOutOfBoundsException("writeBeyond");
            }

            if (len == 0) {
                return 0;
            }

            int bwritten = 0;

            synchronized (SAAJDataSource.this) {
                if (bread == totalsz) {
                    return -1;
                }
                List ml = memorybuflist;
                long longlen = len;
                longlen = Math.min(longlen, totalsz -
                        bread);    // Only return the number of bytes in the data store that is left.
                len = new Long(longlen).intValue();
                if (ml != null) {
                    if (null == currentBuf) {    // Get the buffer we need to read from.
                        currentBuf = (byte[])ml.get(currentIndex);
                        currentBufPos = 0;    // New buffer start from the begining.
                    }
                    do {

                        // The bytes to copy, the minimum of the bytes left in this buffer or bytes remaining.
                        int bcopy = Math.min(currentBuf.length - currentBufPos, len - bwritten);

                        // Copy the data.
                        System.arraycopy(currentBuf, currentBufPos, b, off + bwritten, bcopy);
                        bwritten += bcopy;
                        currentBufPos += bcopy;
                        if (bwritten < len) {    // Get the next buffer.
                            currentBuf = (byte[])ml.get(++currentIndex);
                            currentBufPos = 0;
                        }
                    } while (bwritten < len);
                }

                if ((bwritten == 0) && (null != diskCacheFile)) {
                    if (null == fileIS) {           // we are now reading from disk.
                        fileIS = new java.io.FileInputStream(diskCacheFile);

                        if (bread > 0) {
                            fileIS.skip(bread);     // Skip what we've read so far.
                        }
                    }

                    if (cachediskstream != null) {
                        cachediskstream.flush();
                    }
                    bwritten = fileIS.read(b, off, len);
                }

                if (bwritten > 0) {
                    bread += bwritten;
                }
            }
            return bwritten;
        }

        /**
         * close the stream.
         *
         * @throws IOException
         */
        public synchronized void close() throws IOException {
            if (!readClosed) {
                readers.remove(this);
                readClosed = true;
                if (fileIS != null) {
                    fileIS.close();
                }

                fileIS = null;
            }
        }

        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }
    }
}
