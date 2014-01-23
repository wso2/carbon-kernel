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

package org.apache.axiom.util.stax.xop;

import javax.xml.stream.XMLStreamReader;

/**
 * Represents an XOP encoded stream. Since an XOP message is a MIME package with
 * an main part in XML and a set of additional (binary) parts referenced from
 * the main part, this class encapsulates an {@link XMLStreamReader}
 * representing the main part and a {@link MimePartProvider} giving access to
 * the attachments. Instances of this class can be obtained from
 * {@link XOPUtils#getXOPEncodedStream(XMLStreamReader)}.
 */
public class XOPEncodedStream {
    private final XMLStreamReader reader;
    private final MimePartProvider mimePartProvider;
    
    XOPEncodedStream(XMLStreamReader reader, MimePartProvider mimePartProvider) {
        this.reader = reader;
        this.mimePartProvider = mimePartProvider;
    }

    /**
     * Get the stream reader for the main part of the XOP message.
     * 
     * @return the stream reader for the main part
     */
    public XMLStreamReader getReader() {
        return reader;
    }

    /**
     * Get the provider object for the additional MIME parts referenced by the
     * main part.
     * 
     * @return the MIME part provider
     */
    public MimePartProvider getMimePartProvider() {
        return mimePartProvider;
    }
}
