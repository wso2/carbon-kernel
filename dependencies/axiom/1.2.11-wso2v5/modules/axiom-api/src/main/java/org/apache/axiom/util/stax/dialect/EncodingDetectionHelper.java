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
package org.apache.axiom.util.stax.dialect;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import javax.xml.stream.XMLStreamException;

/**
 * Implements the character encoding autodetection algorithm described in Appendix F.1 of the
 * XML 1.0 specifications (Fifth Edition).
 */
class EncodingDetectionHelper {
    private final InputStream stream;
    private final boolean useMark;
    
    public EncodingDetectionHelper(InputStream stream) {
        useMark = stream.markSupported();
        if (useMark) {
            this.stream = stream;
        } else {
            this.stream = new PushbackInputStream(stream, 4);
        }
    }
    
    public InputStream getInputStream() {
        return stream;
    }
    
    public String detectEncoding() throws XMLStreamException {
        byte[] startBytes = new byte[4];
        try {
            if (useMark) {
                stream.mark(4);
            }
            int read = 0;
            do {
                int c = stream.read(startBytes, read, 4-read);
                if (c == -1) {
                    throw new XMLStreamException("Unexpected end of stream");
                }
                read += c;
            } while (read < 4);
            if (useMark) {
                stream.reset();
            } else {
                ((PushbackInputStream)stream).unread(startBytes);
            }
        } catch (IOException ex) {
            throw new XMLStreamException("Unable to read start bytes", ex);
        }
        int marker = ((startBytes[0] & 0xFF) << 24) + ((startBytes[1] & 0xFF) << 16)
                + ((startBytes[2] & 0xFF) << 8) + (startBytes[3] & 0xFF);
        switch (marker) {
            case 0x0000FEFF:
            case 0xFFFE0000:
            case 0x0000FFFE:
            case 0xFEFF0000:
            case 0x0000003C:
            case 0x3C000000:
            case 0x00003C00:
            case 0x003C0000:
                return "UCS-4";
            case 0x003C003F:
                return "UTF-16BE";
            case 0x3C003F00:
                return "UTF-16LE";
            case 0x3C3F786D:
                return "UTF-8";
            default:
                if ((marker & 0xFFFF0000) == 0xFEFF0000) {
                    return "UTF-16BE";
                } else if ((marker & 0xFFFF0000) == 0xFFFE0000) {
                    return "UTF-16LE";
                } else {
                    return "UTF-8";
                }
        }
    }
}
