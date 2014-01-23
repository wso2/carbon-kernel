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

package org.apache.axiom.mime;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataHandler;

/**
 * Writes a MIME multipart package as used by XOP/MTOM and SOAP with Attachments.
 * Objects implementing this interface are created using a {@link MultipartWriterFactory}. MIME
 * parts are written using {@link #writePart(String, String, String)} or
 * {@link #writePart(DataHandler, String, String)}. Calls to both methods can be mixed, i.e. it
 * is not required to use the same method for all MIME parts. Instead, the caller should choose
 * the most convenient method for each part (depending on the form in which the content is
 * available). After all parts have been written, {@link #complete()} must be called to write the
 * final MIME boundary.
 * <p>
 * The following semantics are defined for the <code>contentTransferEncoding</code> and
 * <code>contentID</code> arguments of the two write methods:
 * <ul>
 *   <li>It is the responsibility of the implementation to apply the content transfer encoding
 *       as specified by the <code>contentTransferEncoding</code> argument. The caller only
 *       provides the unencoded data. The implementation should support at least <tt>binary</tt>
 *       and <tt>base64</tt>. If it doesn't support the specified encoding, it may use an
 *       alternative one. In any case, the implementation must make sure that the MIME part
 *       has a <tt>Content-Transfer-Encoding</tt> header appropriate for the applied
 *       encoding.</li>
 *   <li>The content ID passed as argument is always the raw ID (without the angle brackets).
 *       It is the responsibility of the implementation to properly format the value
 *       of the <tt>Content-ID</tt> header.</li>
 * </ul>
 */
public interface MultipartWriter {
    /**
     * Start writing a MIME part. The methods returns an {@link OutputStream} that the caller can
     * use to write the content of the MIME part. After writing the content,
     * {@link OutputStream#close()} must be called to complete the writing of the MIME part.
     * 
     * @param contentType
     *            the value of the <tt>Content-Type</tt> header of the MIME part
     * @param contentTransferEncoding
     *            the content transfer encoding to be used (see above); must not be
     *            <code>null</code>
     * @param contentID
     *            the content ID of the MIME part (see above)
     * @return an output stream to write the content of the MIME part
     * @throws IOException
     *             if an I/O error occurs when writing to the underlying stream
     */
    OutputStream writePart(String contentType, String contentTransferEncoding, String contentID)
            throws IOException;
    
    /**
     * Write a MIME part. The content is provided by a {@link DataHandler} object, which also
     * specifies the content type of the part.
     * 
     * @param dataHandler
     *            the content of the MIME part to write
     * @param contentTransferEncoding
     *            the content transfer encoding to be used (see above); must not be
     *            <code>null</code>
     * @param contentID
     *            the content ID of the MIME part (see above)
     * @throws IOException
     *             if an I/O error occurs when writing the part to the underlying stream
     */
    void writePart(DataHandler dataHandler, String contentTransferEncoding, String contentID)
            throws IOException;
    
    /**
     * Complete writing of the MIME multipart package. This method does <b>not</b> close the
     * underlying stream.
     * 
     * @throws IOException
     *             if an I/O error occurs when writing to the underlying stream
     */
    void complete() throws IOException;
}
