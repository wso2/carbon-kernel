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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for writer that dumps skipping resource name="foo"
 */
public class DumpWriter extends Writer {
    private static final Log log = LogFactory.getLog(DumpWriter.class);
    private Writer writer;
    private boolean writeDirectly;
    private int quotesCount;
    private StringBuffer strBuffer;
    private List<String> bufferedAttributes;

    /**
     * Construct dump writer
     *
     * @param writer base writer
     */
    public DumpWriter(Writer writer) {
        this.writer = writer;
        writeDirectly = false;
        quotesCount = 0;
        strBuffer = new StringBuffer();
        bufferedAttributes = new ArrayList<String>();
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cBuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @throws java.io.IOException  If an I/O error occurs
     */
    public void write(char cBuf[], int off, int len) throws IOException {
        if (writeDirectly) {
            writer.write(cBuf, off, len);
            return;
        }
        // now try iterate all the chars in the cBuf
        int minLen = (cBuf.length - off) < len ? (cBuf.length - off) : len;
        for (int i = 0; i < minLen; i ++) {
            char c = cBuf[off + i];
            if (writeDirectly) {
                writer.write(c);
                continue;
            }
            if (c == '"') {
                quotesCount ++;
            }
            strBuffer.append(c);
            if (quotesCount == 2) {
                // this is an end of an attribute, so we are checking what the attribute is,
                // the following attempt is to read the name of the attribute..
                String buffer = strBuffer.toString();
                buffer = buffer.trim();
                int lastEqualIndex = buffer.lastIndexOf('=');
                // last equal index should not be the 1st in the buf, anyway we do the check
                if (lastEqualIndex == 1) {
                    String msg = "Error in written xml. attribute should serialize with '='.";
                    log.error(msg);
                    throw new IOException(msg);
                } 
                String beforeEqualBuffer = buffer.substring(0, lastEqualIndex);
                beforeEqualBuffer = beforeEqualBuffer.trim(); // ignore the following spaces..
                int lastSpaceIndex = beforeEqualBuffer.lastIndexOf(' ');
                if (lastEqualIndex == -1) {
                    String msg = "Error in written xml. attribute should " +
                            "serialize with ' ' at the start.";
                    log.error(msg);
                    throw new IOException(msg);
                }
                String attributeName = beforeEqualBuffer.substring(lastSpaceIndex + 1);
                if (attributeName.equals("name")) {
                    writeDirectly = true;
                    for (String bufferedAttribute: bufferedAttributes) {
                        writer.write(" " + bufferedAttribute);
                    }
                } else {
                    quotesCount = 0;
                    String attributeWithValue = buffer.substring(lastSpaceIndex + 1);
                    bufferedAttributes.add(attributeWithValue);
                }
            }
        }
    }

    /**
     * Flush the writer
     *
     * @throws IOException, if an I/O error occurs
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Close the writer
     *
     * @throws IOException, if an I/O error occurs
     */
    public void close() throws IOException {
        writer.flush();      
    }
}
