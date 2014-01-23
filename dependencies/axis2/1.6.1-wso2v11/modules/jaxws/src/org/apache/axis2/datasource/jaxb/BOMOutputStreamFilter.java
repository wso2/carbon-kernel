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
package org.apache.axis2.datasource.jaxb;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Strip off the BOM when serializing an embedded JAXB object.
 */
public class BOMOutputStreamFilter extends FilterOutputStream {

    private static final Log log = LogFactory.getLog(BOMOutputStreamFilter.class);
    int count = 0;
    int bomLength = 2;
    
    
    /**
     * Create a BOMOutputStreamFilter to remove the BOM (Byte Order Mark)
     * @param encoding
     * @param out
     */
    public BOMOutputStreamFilter(String encoding, OutputStream out) {
        super(out);
        if (encoding == null || encoding.equalsIgnoreCase("UTF-8")) {
            bomLength = 0;
        } else if (encoding.equalsIgnoreCase("UTF-16")) {
            // UTF-16LE and UTF-16BE shouldn't have a BOM
            bomLength = 2;  // FF FE or FE FF
        } else {
            
            bomLength = 0;
            if (log.isDebugEnabled()) {
                log.debug("Don't know the BOM length for " + encoding + ". assuming zero.");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("encoding = " + encoding);
            log.debug("expected BOM length = " + bomLength);
        }
    }


    @Override
    public void write(int b) throws IOException {
        // Don't write the first two characters because they represent the BOM
        if (count >= bomLength) {
            out.write(b);
        } else {
            if (b == -1 || // 0xFF
                b == -2) { // 0xFE
                // skip...this is a BOM character
                if (log.isDebugEnabled()) {
                    log.debug("Skipping BOM character " + b + " at position " + count);
                }
            } else {
                out.write(b);
            }
        }
        count++;
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        // Delegate to our 3 argument write method
        this.write(b, 0, b.length);
    }
    
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        // Delegate the first couple of bytes to our 
        // single argument write constructor
        while (count < bomLength && len > 0) {
            this.write(b[off]);
            off++;
            len--;
        }
        
        // Delegate the remaining bytes to the target output stream
        if (len > 0) {
            out.write(b, off, len);
            count = count + len;
        }
    }
}

