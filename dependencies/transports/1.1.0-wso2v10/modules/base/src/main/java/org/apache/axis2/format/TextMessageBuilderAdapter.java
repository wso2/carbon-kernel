/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.format;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.io.input.ReaderInputStream;

/**
 * Adapter to add the {@link TextMessageBuilder} interface to an
 * existing {@link Builder}.
 * It implements the {@link TextMessageBuilder#processDocument(Reader, String, MessageContext)}
 * and {@link TextMessageBuilder#processDocument(String, String, MessageContext)} by converting
 * the character stream to a byte stream using {@link ReaderInputStream}.
 * 
 * TODO: specifying encoding
 */
public class TextMessageBuilderAdapter implements TextMessageBuilder {
    private final Builder builder;

    public TextMessageBuilderAdapter(Builder builder) {
        this.builder = builder;
    }

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        return builder.processDocument(inputStream, contentType, messageContext);
    }

    public OMElement processDocument(Reader reader, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        String charset;
        try {
            ContentType ct = new ContentType(contentType);
            charset = ct.getParameter("charset");
        } catch (ParseException ex) {
            charset = null;
        }
        if (charset == null) {
            charset = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }
        messageContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charset);        
        return processDocument(new ReaderInputStream(reader, charset), contentType,
                messageContext);
    }

    public OMElement processDocument(String content, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        return processDocument(new StringReader(content), contentType, messageContext);
    }
}
