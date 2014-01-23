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

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import java.io.IOException;
import java.util.Enumeration;

/**
 * The MultipartAttachmentStreams class is used to create IncomingAttachmentInputStream objects when
 * the HTTP stream shows a marked separation between the SOAP and each attachment parts. Unlike the
 * DIME version, this class will use the BoundaryDelimitedStream to parse data in the SwA format.
 * Another difference between the two is that the MultipartAttachmentStreams class must also provide
 * a way to hold attachment parts parsed prior to where the SOAP part appears in the HTTP stream
 * (i.e. the root part of the multipart-related message). Our DIME counterpart didn't have to worry
 * about this since the SOAP part is guaranteed to be the first in the stream. But since SwA has no
 * such guarantee, we must fall back to caching these first parts. Afterwards, we can stream the
 * rest of the attachments that are after the SOAP part of the request message.
 */
public final class MultipartAttachmentStreams extends IncomingAttachmentStreams {
    private BoundaryDelimitedStream _delimitedStream = null;

    public MultipartAttachmentStreams(BoundaryDelimitedStream delimitedStream)
            throws OMException {
        this._delimitedStream = delimitedStream;
    }

    public IncomingAttachmentInputStream getNextStream() throws OMException {
        IncomingAttachmentInputStream stream;

        if (!isReadyToGetNextStream()) {
            throw new IllegalStateException("nextStreamNotReady");
        }

        InternetHeaders headers;

        try {
            _delimitedStream = _delimitedStream.getNextStream();
            if (_delimitedStream == null) {
                return null;
            }

            headers = new InternetHeaders(_delimitedStream);

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new OMException(ioe);
        } catch (MessagingException me) {
            me.printStackTrace();
            throw new OMException(me);
        }

        stream = new IncomingAttachmentInputStream(_delimitedStream, this);

        Header header;
        String name;
        String value;
        Enumeration e = headers.getAllHeaders();
        while (e != null && e.hasMoreElements()) {
            header = (Header) e.nextElement();
            name = header.getName();
            value = header.getValue();
            if (IncomingAttachmentInputStream.HEADER_CONTENT_ID.equals(name)
                    || IncomingAttachmentInputStream.HEADER_CONTENT_TYPE.equals(name)
                    || IncomingAttachmentInputStream.HEADER_CONTENT_LOCATION.equals(name)) {
                value = value.trim();
            }
            stream.addHeader(name, value);
        }

        setReadyToGetNextStream(false);
        return stream;
    }
}
