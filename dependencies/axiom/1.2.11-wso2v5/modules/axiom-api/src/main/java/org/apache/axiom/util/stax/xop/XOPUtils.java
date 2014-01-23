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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.util.stax.XMLStreamReaderUtils;

/**
 * Contains utility methods related to XOP.
 */
public class XOPUtils {
    private static final MimePartProvider nullMimePartProvider = new MimePartProvider() {
        public boolean isLoaded(String contentID) {
            throw new IllegalArgumentException("There are no MIME parts!");
        }
        
        public DataHandler getDataHandler(String contentID) throws IOException {
            throw new IllegalArgumentException("There are no MIME parts!");
        }
    };
    
    private XOPUtils() {}
    
    /**
     * Extract the content ID from a URL following the cid scheme defined by RFC2392.
     * 
     * @param url the URL
     * @return the corresponding content ID
     * @throws IllegalArgumentException if the URL doesn't use the cid scheme
     */
    public static String getContentIDFromURL(String url) {
        if (url.startsWith("cid:")) {
            try {
                // URIs should always be decoded using UTF-8 (see WSCOMMONS-429). On the
                // other hand, since non ASCII characters are not allowed in content IDs,
                // we can simply decode using ASCII (which is a subset of UTF-8)
                return URLDecoder.decode(url.substring(4), "ascii");
            } catch (UnsupportedEncodingException ex) {
                // We should never get here
                throw new Error(ex);
            }
        } else {
            throw new IllegalArgumentException("The URL doesn't use the cid scheme");
        }
    }
    
    /**
     * Build a cid URL from the given content ID as described in RFC2392.
     * <p>
     * Note that this implementation only encodes the percent character (replacing it by "%25"). The
     * reason is given by the following quotes from RFC3986:
     * <blockquote>
     * If a reserved character is
     * found in a URI component and no delimiting role is known for that character, then it must be
     * interpreted as representing the data octet corresponding to that character's encoding in
     * US-ASCII. [...]
     * <p>
     * Under normal circumstances, the only time when octets within a URI are percent-encoded is
     * during the process of producing the URI from its component parts. This is when an
     * implementation determines which of the reserved characters are to be used as subcomponent
     * delimiters and which can be safely used as data. [...]
     * <p>
     * Because the percent ("%") character serves as the indicator for percent-encoded octets, it
     * must be percent-encoded as "%25" for that octet to be used as data within a URI.
     * </blockquote>
     * <p>
     * Since RFC2392 doesn't define any subcomponents for the cid scheme and since RFC2045 specifies
     * that only US-ASCII characters are allowed in content IDs, the percent character (which is
     * specifically allowed by RFC2045) is the only character that needs URL encoding.
     * <p>
     * Another reason to strictly limit the set of characters to be encoded is that some
     * applications fail to decode cid URLs correctly if they contain percent encoded octets.
     * 
     * @param contentID the content ID (without enclosing angle brackets)
     * @return the corresponding URL in the cid scheme
     */
    public static String getURLForContentID(String contentID) {
        return "cid:" + contentID.replaceAll("%", "%25");
    }
    
    /**
     * Get an XOP encoded stream for a given stream reader. Depending on its
     * type and characteristics, this method may wrap or unwrap the stream
     * reader:
     * <ol>
     * <li>If the original reader is an {@link XOPEncodingStreamReader} it will
     * be preserved, since it is already XOP encoded.
     * <li>If the original reader is an {@link XOPDecodingStreamReader}, it will
     * be unwrapped to give access to the underlying XOP encoded reader.
     * <li>If the original reader is a plain XML stream reader implementing the
     * {@link org.apache.axiom.ext.stax.datahandler.DataHandlerReader}
     * extension, it will be wrapped in an {@link XOPEncodingStreamReader} so
     * that optimized binary data can be transferred using XOP.
     * <li>In all other cases, the original reader is simply preserved.
     * </ol>
     * 
     * @param reader
     *            the original reader
     * @return the XOP encoded stream
     */
    public static XOPEncodedStream getXOPEncodedStream(XMLStreamReader reader) {
        if (reader instanceof XOPEncodingStreamReader) {
            return new XOPEncodedStream(reader, (MimePartProvider)reader);
        } else if (reader instanceof XOPDecodingStreamReader) {
            return ((XOPDecodingStreamReader)reader).getXOPEncodedStream();
        } else if (XMLStreamReaderUtils.getDataHandlerReader(reader) != null) {
            XOPEncodingStreamReader wrapper = new XOPEncodingStreamReader(reader,
                    ContentIDGenerator.DEFAULT, OptimizationPolicy.ALL);
            return new XOPEncodedStream(wrapper, wrapper);
        } else {
            return new XOPEncodedStream(reader, nullMimePartProvider);
        }
    }
}
