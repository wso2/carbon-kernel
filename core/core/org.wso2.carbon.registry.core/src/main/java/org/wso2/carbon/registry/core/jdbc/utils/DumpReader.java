/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.core.jdbc.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for the Reader that will cache the extra bytes consumed by xml stream reader in
 * registry.restore(path, reader) operation which restore the resources from a dump.xml This keep
 * track of the actual reading child resource (using setReadingChildResourceIndex) and consuming
 * child resource with normal read functions, if the consumed ones have higher values than actually
 * reading ones, this will buffer the extra bytes and release it after actual reading ones finished
 * using it.
 */
public class DumpReader extends Reader {

    private static final Log log = LogFactory.getLog(DumpReader.class);

    private Reader reader;
    private int offset = 0;
    private boolean withinChildrenTag = false;
    private int childResourceLevel = 0;
    private boolean withinElementTag;
    private StringBuffer elementTagStrBuff;
    private List<Integer> localBuffer = null;
    private int localBufferStartIndex = -1;
    private int readingChildResourceIndex = -1;
    private int consumedChildResourceIndex = -1;
    private Map<Integer, Integer> resourceStartingBufferIndices;
    // inclusive index
    private Map<Integer, Integer> resourceEndingBufferIndices;
    // this is absolute index of the ends
    private Map<Integer, Integer> resourceEndingOffset = new HashMap<Integer, Integer>();

    // to collect some statistics
    private static int totalRead = 0;
    private static int totalBuffered = 0;
    private static int maximumBuffer = 0;
    private static int totalBufferedRead = 0;

    // sometimes parent have to read a bit of child resource, as only child resource have the
    // the name of the resource, but parent need to know the child path to construct the child path,
    // (parent is always calculating the immediate children paths)
    private boolean checkingChildByParent = false;

    /**
     * Constructor of the Dump Reader wrapping another reader.
     *
     * @param reader the wrapped reader, could be file reader, http reader or another dump reader
     */
    public DumpReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * Read characters into a portion of an array. This method will block until some input is
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param cBuf destination buffer
     * @param off  offset at which to start storing characters
     * @param len  maximum number of characters to read
     *
     * @return the number of characters read, or -1 if the end of the stream has been reached
     * @throws IOException if an I/O error occurs
     */
    public int read(char cBuf[], int off, int len) throws IOException {
        int cBufLen = cBuf.length;
        int minLen = cBufLen < len ? cBufLen : len;
        int i;

        for (i = 0; i < minLen; i++) {
            int r = read();
            if (r == -1) {
                break;
            }
            cBuf[i + off] = (char) r;
        }
        return i == 0 ? -1 : i;
    }

    /**
     * Read a single character. This method will block until a character is available, an I/O error
     * occurs, or the end of the stream is reached.
     * <p/>
     * Subclasses that intend to support efficient single-character input should override this
     * method.
     *
     * @return the character read, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the
     *         end of the stream has been reached
     * @throws IOException if an I/O error occurs
     */
    public int read() throws IOException {
        if (localBuffer != null && offset < localBufferStartIndex) {
            // this is assumed unreachable
            String msg = "Offset to ask should be always greater than the buffer start index.";
            log.error(msg);
            throw new IOException(msg);
        }
        if (localBuffer != null && offset < localBufferStartIndex + localBuffer.size()) {
            // so this should be read from the buffer.
            // and we should check whether the currently consuming resource is ending.
            if (!checkingChildByParent) {
                Integer endIndexBufferObj = resourceEndingBufferIndices.get(
                        readingChildResourceIndex);
                if (endIndexBufferObj != null) {
                    // it is possible the end index obj is null, as it is still to reach the end of
                    // the tag.
                    int endIndex = localBufferStartIndex + endIndexBufferObj;
                    if (endIndex < offset) {
                        // if the offset is going beyond the end index, we will say no
                        clearBufferUpToNow(endIndex);
                        return -1;
                    }
                }
            }
            int r = localBuffer.get(offset - localBufferStartIndex);
            if (!checkingChildByParent && (offset - localBufferStartIndex) >
                    RegistryConstants.DEFAULT_BUFFER_SIZE) {
                // reset the buffer
                clearBufferUpToNow(offset);
            }

            if (!(reader instanceof DumpReader)) {
                totalBufferedRead++;
            }
            offset++;
            return r;
        }

        if (!checkingChildByParent && localBuffer != null &&
                readingChildResourceIndex >= consumedChildResourceIndex) {
            localBuffer = null;
            localBufferStartIndex = -1;
            resourceStartingBufferIndices = null;
            resourceEndingBufferIndices = null;
        }

        if (checkingChildByParent && localBuffer == null) {
            // the parent is reading some characters from the child, so should be checked and cached
            localBuffer = new ArrayList<Integer>();
            resourceStartingBufferIndices = new HashMap<Integer, Integer>();
            resourceEndingBufferIndices = new HashMap<Integer, Integer>();
            localBufferStartIndex = offset;
        }
        if (!checkingChildByParent && resourceEndingOffset != null) {
            Integer endIndexObj = resourceEndingOffset.get(readingChildResourceIndex);
            if (endIndexObj != null) {
                // it is possible the end index obj is null, as it is still to reach the end of the tag.
                if (endIndexObj < offset) {
                    // if the offset is going beyond the end index, we will say no
                    clearBufferUpToNow(endIndexObj);
                    return -1;
                }
            }
        }

        int r = reader.read();
        offset++;
        char c = (char) r;

        if (!(reader instanceof DumpReader)) {
            totalRead++;
        }

        if (withinElementTag) {
            if (c == '>') {
                if (elementTagStrBuff == null) {
                    // there can't be situations having '>' without '<', so throwing an exception
                    String msg = "'>' found before '<'.";
                    log.error(msg);
                    throw new IOException(msg);
                }
            }
            elementTagStrBuff.append(c);
            if (c == '>') {
                withinElementTag = false;
                String elementTagStr = elementTagStrBuff.toString();
                elementTagStrBuff = null;

                // right now we are doing string comparisons as we are not expecting to
                // have namespace prefixes
                if ("<children>".equals(elementTagStr) || "<childs>".equals(elementTagStr)) {
                    // only the first children element is mattered.
                    withinChildrenTag = true;
                } else if (elementTagStr.startsWith("</resource")) {
                    // so this is the end of a resource.
                    if (childResourceLevel == 1) {
                        // we are making a end flag for the consumed childResource
                        if (localBuffer != null) {
                            resourceEndingBufferIndices.put(consumedChildResourceIndex,
                                    localBuffer.size());
                        }
                        resourceEndingOffset.put(consumedChildResourceIndex, offset - 1);
                    }
                    childResourceLevel--;
                } else if (withinChildrenTag && elementTagStr.startsWith("<resource")) {
                    // so this is the start of a resource
                    childResourceLevel++;
                    if (childResourceLevel == 1) {
                        consumedChildResourceIndex++;
                        // so we have an immediate child resource.
                        if (readingChildResourceIndex < consumedChildResourceIndex) {
                            // that mean we have not ask to read this element, but the xml stream
                            // reader is trying to consume more bytes, so we should cache the thing
                            if (localBuffer == null) {
                                localBuffer = new ArrayList<Integer>();
                                resourceStartingBufferIndices = new HashMap<Integer, Integer>();
                                resourceEndingBufferIndices = new HashMap<Integer, Integer>();
                            }
                            // keep the local buffer's starting index
                            if (localBufferStartIndex == -1) {
                                // then add the whole resource xml to the buffer
                                // skipping last character, as it is added at the end of the method.
                                byte[] elementTagStrBytes = elementTagStr.getBytes();
                                for (int i = 0; i < elementTagStrBytes.length - 1; i++) {
                                    byte b = elementTagStrBytes[i];

                                    localBuffer.add((int) b);
                                    if (!(reader instanceof DumpReader)) {
                                        totalBuffered++;
                                        maximumBuffer = localBuffer.size() > maximumBuffer ?
                                                localBuffer.size() :
                                                maximumBuffer;
                                    }
                                }
                                localBufferStartIndex = offset - elementTagStr.length();
                            }
                            // the following line count the current character to be added at the end
                            int currentLocalBufferIndex = localBuffer.size() + 1;
                            resourceStartingBufferIndices.put(consumedChildResourceIndex,
                                    currentLocalBufferIndex - elementTagStr.length());
                        }
                    }
                }
            }
        } else {
            if (c == '<') {
                withinElementTag = true;
                elementTagStrBuff = new StringBuffer();
                elementTagStrBuff.append(c);
            }
        }
        if (localBuffer != null) {
            localBuffer.add(r);
            if (!(reader instanceof DumpReader)) {
                totalBuffered++;
                maximumBuffer = localBuffer.size() > maximumBuffer ? localBuffer.size() :
                        maximumBuffer;
            }
        }
        return r;
    }

    /**
     * Close the stream. Once a stream has been closed, further read(), ready(), mark(), or reset()
     * invocations will throw an IOException. Closing a previously-closed stream, however, has no
     * effect.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Set the reading child index
     *
     * @param readingChildResourceIndex the reading child index
     */
    public void setReadingChildResourceIndex(int readingChildResourceIndex) {
        this.readingChildResourceIndex = readingChildResourceIndex;
        // so we are just resetting our offset to get it from our buffer instead of the
        if (resourceStartingBufferIndices != null &&
                resourceStartingBufferIndices.get(readingChildResourceIndex) != null) {
            offset = localBufferStartIndex + resourceStartingBufferIndices.get(
                    readingChildResourceIndex);
            if (!checkingChildByParent) {
                resourceStartingBufferIndices.remove(readingChildResourceIndex);
            }
        }
    }

    /**
     * Set whether the check is done by the parent, in that case we buffer all the bytes read since
     * they will be re-read from the child side anyway. Normally in a dump restore parent check
     * child to read the child name to construct the child path.
     *
     * @param checkingChildByParent true if child is checked by parent, false otherwise
     */
    public void setCheckingChildByParent(boolean checkingChildByParent) {
        this.checkingChildByParent = checkingChildByParent;
    }

    /**
     * To check whether the stream is in the last child resource. If so it will be consume all the
     * bytes up to </resource> tag. This should be called only if he checkingChildByParent is set.
     *
     * @param readingChildResourceIndex the reading child index
     *
     * @return true, if this is the last child resource, false otherwise
     * @throws IOException throws if the operation failed.
     */
    public boolean isLastResource(int readingChildResourceIndex) throws IOException {
        // definitely the requesting index should be there in the endingOffset map.
        if (resourceEndingOffset.get(readingChildResourceIndex) == null) {
            throw new IOException("Error in checking the end of the resource index: " +
                    readingChildResourceIndex + ".");
        }
        offset = resourceEndingOffset.get(readingChildResourceIndex) + 1;
        // definitely this is buffered or still not read, so we just continue reading
        clearBufferUpToNow(offset);
        int r;
        do {
            r = read();
        } while (r != -1 && r != '<');

        if (r == -1) {
            return true;
        }
        StringBuffer nextTagBuffer = new StringBuffer();
        nextTagBuffer.append((char) r);
        do {
            r = read();
            nextTagBuffer.append((char) r);
        } while (r != -1 && r != '>');

        if (r == -1) {
            return true;
        }
        String nextTag = nextTagBuffer.toString();
        if (nextTag.startsWith("<resource")) {
            return false;
        }
        // if the resource is ending, we are consuming reader until the </resource> is finished
        while (true) {
            do {
                r = read();
            } while (r != -1 && r != '<');

            if (r == -1) {
                return true;
            }
            nextTagBuffer = new StringBuffer();
            nextTagBuffer.append((char) r);
            do {
                r = read();
                nextTagBuffer.append((char) r);
            } while (r != -1 && r != '>');

            if (r == -1) {
                return true;
            }
            // not going ahead </resource>
            if (nextTagBuffer.toString().equals("</resource>")) {
                return true;
            }
        }
    }

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return true if the next read() is guaranteed not to block for input, false otherwise. Note
     *         that returning false does not guarantee that the next read will block.
     * @throws IOException if an I/O error occurs
     */
    public boolean ready() throws IOException {
        return (localBuffer != null) || reader.ready();
    }

    private void clearBufferUpToNow(int offset) {
        if (localBuffer != null) {
            if (offset >= localBufferStartIndex + localBuffer.size()) {
                localBuffer = null;
                localBufferStartIndex = -1;
                resourceStartingBufferIndices = null;
                resourceEndingBufferIndices = null;
            } else {
                List<Integer> newLocalBuffer = localBuffer.subList(offset - localBufferStartIndex,
                        localBuffer.size());
                if (resourceStartingBufferIndices != null) {
                    Map<Integer, Integer> newResourceStartingBufferIndices =
                            new HashMap<Integer, Integer>();
                    for (int resourceNo : resourceStartingBufferIndices.keySet()) {
                        int oldBufferStartIndex = resourceStartingBufferIndices.get(resourceNo);
                        int startOffset = localBufferStartIndex + oldBufferStartIndex;
                        int newBuffStartIndex = startOffset - offset;
                        // we are anyway copying the -value as well
                        if (newBuffStartIndex >= 0) {
                            newResourceStartingBufferIndices.put(resourceNo, newBuffStartIndex);
                        }
                    }
                    resourceStartingBufferIndices = newResourceStartingBufferIndices;
                }
                if (resourceEndingBufferIndices != null) {
                    Map<Integer, Integer> newResourceEndingBufferIndices =
                            new HashMap<Integer, Integer>();
                    for (int resourceNo : resourceEndingBufferIndices.keySet()) {
                        int oldBufferEndIndex = resourceEndingBufferIndices.get(resourceNo);
                        int startOffset = localBufferStartIndex + oldBufferEndIndex;
                        int newBuffEndIndex = startOffset - offset;
                        // we are anyway copying the -value as well
                        if (newBuffEndIndex >= 0) {
                            newResourceEndingBufferIndices.put(resourceNo, newBuffEndIndex);
                        }
                    }
                    resourceEndingBufferIndices = newResourceEndingBufferIndices;
                }
                localBufferStartIndex = offset;
                localBuffer = newLocalBuffer;
            }
        }
    }

    /**
     * Return the total number of read bytes from the main reader.
     *
     * @return the total number of read bytes.
     */
    public static int getTotalRead() {
        return totalRead;
    }

    /**
     * Return the total number of buffered bytes from the main reader.
     *
     * @return the total number of buffed bytes.
     */
    public static int getTotalBuffered() {
        return totalBuffered;
    }

    /**
     * Return the maximum size of the buffer when reading from the main reader.
     *
     * @return the maximum size of the buffer.
     */
    public static int getMaxBufferedSize() {
        return maximumBuffer;
    }

    /**
     * Return the total number of bytes read from the buffer (buffered when reading from the main
     * reader)
     *
     * @return the total number of bytes read from the buffer.
     */
    public static int getTotalBufferedRead() {
        return totalBufferedRead;
    }
}
