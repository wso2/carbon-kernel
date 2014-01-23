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

import org.apache.axiom.om.OMException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** This class takes the input stream and turns it multiple streams. */
public class BoundaryDelimitedStream extends java.io.FilterInputStream {

    /** The <code>Log</code> that this class should log all events to. */
    protected static Log log =
            LogFactory.getLog(BoundaryDelimitedStream.class.getName());

    protected byte[] boundary = null;

    /** The boundary length. */
    int boundaryLen = 0;

    /** The boundary length plus crlf. */
    int boundaryBufLen = 0;

    /** The source input stream. */
    java.io.InputStream is = null;

    /** The stream has been closed. */
    boolean closed = true;

    /** eof has been detected. */
    boolean eos = false;

    /** There are no more streams left. */
    boolean theEnd = false;

    /** Minimum to read at one time. */
    int readbufsz = 0;

    /** The buffer we are reading. */
    byte[] readbuf = null;

    /** Where we have read so far in the stream. */
    int readBufPos = 0;

    /** The number of bytes in array. */
    int readBufEnd = 0;

    /** Field BOUNDARY_NOT_FOUND. */
    protected static final int BOUNDARY_NOT_FOUND = Integer.MAX_VALUE;

    // Where in the stream a boundary is located.

    /** Field boundaryPos. */
    int boundaryPos = BOUNDARY_NOT_FOUND;

    /** The number of streams produced. */
    static int streamCount = 0;

    /** Signal that a new stream has been created. */
    protected static synchronized int newStreamNo() {

        log.debug("streamNo" + (streamCount + 1));

        return ++streamCount;
    }

    /** Field streamNo. */
    protected int streamNo = -1;    // Keeps track of stream

    /** Field isDebugEnabled. */
    static boolean isDebugEnabled = false;

    /**
     * Gets the next stream. From the previous using the same buffer size to read.
     *
     * @return the boundary delmited stream, null if there are no more streams.
     * @throws java.io.IOException if there was an error loading the data for the next stream
     */
    public synchronized BoundaryDelimitedStream getNextStream() throws java.io.IOException {
        return getNextStream(readbufsz);
    }

    /**
     * Gets the next stream. From the previous using  new buffer reading size.
     *
     * @param readbufsz
     * @return the boundary delmited stream, null if there are no more streams.
     * @throws java.io.IOException if there was an error loading the data for the next stream
     */
    protected synchronized BoundaryDelimitedStream getNextStream(
            int readbufsz) throws java.io.IOException {

        BoundaryDelimitedStream ret = null;

        if (!theEnd) {

            // Create an new boundary stream  that comes after this one.
            ret = new BoundaryDelimitedStream(this, readbufsz);
        }

        return ret;
    }

    /**
     * Constructor to create the next stream from the previous one.
     *
     * @param prev      the previous stream
     * @param readbufsz how many bytes to make the read buffer
     * @throws java.io.IOException if there was a problem reading data from <code>prev</code>
     */
    protected BoundaryDelimitedStream(BoundaryDelimitedStream prev,
                                      int readbufsz)
            throws java.io.IOException {
        super(null);

        streamNo = newStreamNo();
        boundary = prev.boundary;
        boundaryLen = prev.boundaryLen;
        boundaryBufLen = prev.boundaryBufLen;
        skip = prev.skip;
        is = prev.is;
        closed = false;    // The new one is not closed.
        eos = false;    // Its not at th EOS.
        this.readbufsz = readbufsz;
        readbuf = prev.readbuf;

        // Move past the old boundary.
        readBufPos = prev.readBufPos + boundaryBufLen;
        readBufEnd = prev.readBufEnd;

        // find the new boundary.
        boundaryPos = boundaryPosition(readbuf, readBufPos, readBufEnd);
        prev.theEnd = theEnd;      // The stream.
    }

    /**
     * Create a new boundary stream.
     *
     * @param is
     * @param boundary  is the boundary that separates the individual streams.
     * @param readbufsz lets you have some control over the amount of buffering. by buffering you
     *                  can some effiency in searching.
     * @throws OMException
     */
    BoundaryDelimitedStream(
            java.io.InputStream is, byte[] boundary, int readbufsz)
            throws OMException {

        // super (is);
        super(null);    // we handle everything so this is not necessary, don't won't to hang on to a reference.

        isDebugEnabled = log.isDebugEnabled();
        streamNo = newStreamNo();
        closed = false;
        this.is = is;

        // Copy the boundary array to make certain it is never altered.
        this.boundary = new byte[boundary.length];

        System.arraycopy(boundary, 0, this.boundary, 0, boundary.length);

        this.boundaryLen = this.boundary.length;

        // 2 for preceeding, and 2 for proceeding CRLF's
        this.boundaryBufLen = boundaryLen + 4;

        // allways leave room for at least a 2x boundary
        // Most mime boundaries are 40 bytes or so.
        this.readbufsz = Math.max((boundaryBufLen) * 2, readbufsz);
    }

    private int readFromStream(final byte[] b)
            throws java.io.IOException {
        return readFromStream(b, 0, b.length);
    }

    private int readFromStream(
            final byte[] b, final int start, final int length)
            throws java.io.IOException {

        int minRead = Math.max(boundaryBufLen * 2, length);

        minRead = Math.min(minRead, length - start);

        int br = 0;
        int brTotal = 0;

        do {

            br = is.read(b, brTotal + start, length - brTotal);

            if (br > 0) {
                brTotal += br;
            }
        } while ((br > -1) && (brTotal < minRead));

        return (brTotal != 0)
                ? brTotal
                : br;
    }

    /**
     * Read from the boundary delimited stream.
     *
     * @param b   is the array to read into.
     * @param off is the offset
     * @param len
     * @return the number of bytes read. -1 if endof stream.
     * @throws java.io.IOException
     */
    public synchronized int read(byte[] b, final int off, final int len)
            throws java.io.IOException {

        if (closed) {
            throw new java.io.IOException("streamClosed");
        }

        if (eos) {
            return -1;
        }

        if (readbuf == null) {    // Allocate the buffer.
            readbuf = new byte[Math.max(len, readbufsz)];
            readBufEnd = readFromStream(readbuf);

            if (readBufEnd < 0) {
                readbuf = null;
                closed = true;
                finalClose();

                throw new java.io.IOException("eosBeforeMarker");
            }

            readBufPos = 0;

            // Finds the boundary pos.
            boundaryPos = boundaryPosition(readbuf, 0, readBufEnd);
        }

        int bwritten = 0;    // Number of bytes written.

        // read and copy bytes in.
        do
        {                                // Always allow to have a boundary length left in the buffer.

            int bcopy = Math.min(readBufEnd - readBufPos - boundaryBufLen,
                                 len - bwritten);

            // never go past the boundary.
            bcopy = Math.min(bcopy, boundaryPos - readBufPos);

            if (bcopy > 0) {
                System.arraycopy(readbuf, readBufPos, b, off + bwritten, bcopy);

                bwritten += bcopy;
                readBufPos += bcopy;
            }

            if (readBufPos == boundaryPos) {
                eos = true;                 // hit the boundary so it the end of the stream.

                log.debug("atEOS" + streamNo);
            } else if (bwritten < len) {    // need to get more data.

                byte[] dstbuf = readbuf;

                if (readbuf.length < len) {
                    dstbuf = new byte[len];
                }

                int movecnt = readBufEnd - readBufPos;

                // copy what was left over.
                System.arraycopy(readbuf, readBufPos, dstbuf, 0, movecnt);

                // Read in the new data.
                int readcnt = readFromStream(dstbuf, movecnt,
                                             dstbuf.length - movecnt);

                if (readcnt < 0) {
                    readbuf = null;
                    closed = true;
                    finalClose();

                    throw new java.io.IOException("eosBeforeMarker");
                }

                readBufEnd = readcnt + movecnt;
                readbuf = dstbuf;
                readBufPos = 0;             // start at the begining.

                // just move the boundary by what we moved
                if (BOUNDARY_NOT_FOUND != boundaryPos) {
                    boundaryPos -= movecnt;
                } else {
                    boundaryPos = boundaryPosition(
                            readbuf, readBufPos,
                            readBufEnd);        // See if the boundary is now there.
                }
            }
        }

        // read till we get the amount or the stream is finished.
        while (!eos && (bwritten < len));

        if (log.isDebugEnabled()) {
            if (bwritten > 0) {
                byte tb[] = new byte[bwritten];

                System.arraycopy(b, off, tb, 0, bwritten);
                log.debug("readBStream" +
                        new String[] { "" + bwritten,
                                "" + streamNo,
                                new String(tb) });
            }
        }

        if (eos && theEnd) {
            readbuf = null;    // dealloc even in Java.
        }

        return bwritten;
    }

    /**
     * Read from the boundary delimited stream.
     *
     * @param b is the array to read into. Read as much as possible into the size of this array.
     * @return the number of bytes read. -1 if endof stream.
     * @throws java.io.IOException
     */
    public int read(byte[] b) throws java.io.IOException {
        return read(b, 0, b.length);
    }

    /**
     * Read from the boundary delimited stream.
     *
     * @return The byte read, or -1 if endof stream.
     * @throws java.io.IOException
     */
    public int read() throws java.io.IOException {

        byte[] b = new byte[1];    // quick and dirty. //for now
        int read = read(b);

        if (read < 0) {
            return -1;
        } else {
            return b[0] & 0xff;
        }
    }

    /**
     * Closes the stream.
     *
     * @throws java.io.IOException
     */
    public synchronized void close() throws java.io.IOException {

        if (closed) {
            return;
        }

        log.debug("bStreamClosed" + streamNo);

        closed = true;    // mark it closed.

        if (!eos) {    // We need get this off the stream.

            // Easy way to flush through the stream;
            byte[] readrest = new byte[1024 * 16];
            int bread;

            do {
                bread = read(readrest);
            } while (bread > -1);
        }
    }

    /**
     * mark the stream. This is not supported.
     *
     * @param readlimit
     */
    public void mark(int readlimit) {

        // do nothing
    }

    /**
     * reset the stream. This is not supported.
     *
     * @throws java.io.IOException
     */
    public void reset() throws java.io.IOException {
        throw new java.io.IOException("attach.bounday.mns");
    }

    /** markSupported return false; */
    public boolean markSupported() {
        return false;
    }

    public int available() throws java.io.IOException {

        int bcopy = readBufEnd - readBufPos - boundaryBufLen;

        // never go past the boundary.
        bcopy = Math.min(bcopy, boundaryPos - readBufPos);

        return Math.max(0, bcopy);
    }

    /**
     * Read from the boundary delimited stream.
     *
     * @param searchbuf buffer to read from
     * @param start     starting index
     * @param end       ending index
     * @return The position of the boundary. Detects the end of the source stream.
     * @throws java.io.IOException if there was an error manipulating the underlying stream
     */
    protected int boundaryPosition(byte[] searchbuf, int start, int end)
            throws java.io.IOException {

        int foundAt = boundarySearch(searchbuf, start, end);

        // First find the boundary marker
        if (BOUNDARY_NOT_FOUND != foundAt) {    // Something was found.
            if (foundAt + boundaryLen + 2 > end) {
                foundAt = BOUNDARY_NOT_FOUND;
            } else {

                // If the marker has a "--" at the end then this is the last boundary.
                if ((searchbuf[foundAt + boundaryLen] == '-')
                        && (searchbuf[foundAt + boundaryLen + 1] == '-')) {
                    finalClose();
                } else if ((searchbuf[foundAt + boundaryLen] != 13)
                        || (searchbuf[foundAt + boundaryLen + 1] != 10)) {

                    // If there really was no crlf at then end then this is not a boundary.
                    foundAt = BOUNDARY_NOT_FOUND;
                }

                if ((foundAt != BOUNDARY_NOT_FOUND)
                        && (searchbuf[foundAt - 2] == 13)
                        && (searchbuf[foundAt - 1] == 10)) {

                    // Section 7.2.1 of the MIME RFC (#1521) states that CRLF
                    // preceeding boundary is part of the encapsulation
                    // boundary
                    foundAt -= 2;
                }
            }
        }

        return foundAt;
    }

    /* The below uses a standard textbook Boyer-Moore pattern search. */

    private int[] skip = null;

    private int boundarySearch(final byte[] text, final int start,
                               final int end) {

        // log.debug(">>>>" + start + "," + end);
        int i = 0, j = 0, k = 0;

        if (boundaryLen > (end - start)) {
            return BOUNDARY_NOT_FOUND;
        }

        if (null == skip) {
            skip = new int[256];

            java.util.Arrays.fill(skip, boundaryLen);

            for (k = 0; k < boundaryLen - 1; k++) {
                skip[boundary[k]] = boundaryLen - k - 1;
            }
        }

        for (k = start + boundaryLen - 1; k < end;
             k += skip[text[k] & (0xff)]) {

            // log.debug(">>>>" + k);
            // printarry(text, k-boundaryLen+1, end);
            try {
                for (j = boundaryLen - 1, i = k;
                     (j >= 0) && (text[i] == boundary[j]); j--) {
                    i--;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                StringBuffer sb = new StringBuffer();
                sb.append(">>>").append(e);    // rr temporary till a boundary issue is resolved.
                sb.append("start=").append(start);
                sb.append("k=").append(k);
                sb.append("text.length=").append(text.length);
                sb.append("i=").append(i);
                sb.append("boundary.length=").append(boundary.length);
                sb.append("j=").append(j);
                sb.append("end=").append(end);
                log.warn("exception01" + sb.toString());
                throw e;
            }

            if (j == (-1)) {
                return i + 1;
            }
        }

        // log.debug(">>>> not found" );
        return BOUNDARY_NOT_FOUND;
    }

    /**
     * Close the underlying stream and remove all references to it.
     *
     * @throws java.io.IOException if the stream could not be closed
     */
    protected void finalClose() throws java.io.IOException {
        if (theEnd) return;
        theEnd = true;
        is.close();
        is = null;
    }

    /**
     * Method printarry
     *
     * @param b
     * @param start
     * @param end
     */
    public static void printarry(byte[] b, int start, int end) {

        if (log.isDebugEnabled()) {
            byte tb[] = new byte[end - start];

            System.arraycopy(b, start, tb, 0, end - start);
            log.debug("\"" + new String(tb) + "\"");
        }
    }
}
