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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.activation.DataSource;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.ds.WrappedTextNodeOMDataSourceFromDataSource;
import org.apache.axiom.om.ds.WrappedTextNodeOMDataSourceFromReader;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.BaseUtils;

/**
 * Message builder for plain text payloads.
 * <p>
 * This builder processes the input message as plain text and wraps
 * the text in a wrapper element. The name of the wrapper element can
 * be configured as a service parameter (see {@link BaseConstants#WRAPPER_PARAM}).
 * It defaults to {@link BaseConstants#DEFAULT_TEXT_WRAPPER}.
 * If the content is provided as an {@link InputStream} and the content type specifies a
 * <tt>charset</tt> parameter (e.g. <tt>text/plain; charset=ISO-8859-15</tt>),
 * this information is used to decode the text.
 * If the content is provided as an {@link InputStream} but no <tt>charset</tt> parameter
 * is specified on the content type, the default charset encoding specified by
 * {@link MessageContext#DEFAULT_CHAR_SET_ENCODING} is used.
 */
public class PlainTextBuilder implements TextMessageBuilder, DataSourceMessageBuilder {
    private static QName getWrapperQName(MessageContext msgContext) {
        QName wrapperQName = BaseConstants.DEFAULT_TEXT_WRAPPER;
        if (msgContext.getAxisService() != null) {
            Parameter wrapperParam
                    = msgContext.getAxisService().getParameter(BaseConstants.WRAPPER_PARAM);
            if (wrapperParam != null) {
                wrapperQName = BaseUtils.getQNameFromString(wrapperParam.getValue());
            }
        }
        return wrapperQName;
    }
    
    public OMElement processDocument(InputStream inputStream,
                                     String contentType,
                                     MessageContext msgContext) throws AxisFault {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
        QName wrapperQName = getWrapperQName(msgContext);
        Reader reader;
        try {
            reader = new InputStreamReader(inputStream, charSetEnc);
        } catch (UnsupportedEncodingException ex) {
            throw new AxisFault("Unsupported encoding: " + charSetEnc, ex);
        }
        return new OMSourcedElementImpl(wrapperQName, factory,
                new WrappedTextNodeOMDataSourceFromReader(wrapperQName, reader));
    }

    public OMElement processDocument(Reader reader,
                                     String contentType,
                                     MessageContext msgContext) throws AxisFault {
        
        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName wrapperQName = getWrapperQName(msgContext);
        return new OMSourcedElementImpl(wrapperQName, factory,
                new WrappedTextNodeOMDataSourceFromReader(wrapperQName, reader));
    }

    public OMElement processDocument(String content,
                                     String contentType,
                                     MessageContext msgContext) throws AxisFault {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement wrapper = factory.createOMElement(getWrapperQName(msgContext), null);
        factory.createOMText(wrapper, content);
        return wrapper;
    }

    public OMElement processDocument(DataSource dataSource,
                                     String contentType,
                                     MessageContext msgContext) throws AxisFault {
        
        OMFactory factory = OMAbstractFactory.getOMFactory();
        Charset cs = Charset.forName(BuilderUtil.getCharSetEncoding(contentType));
        QName wrapperQName = getWrapperQName(msgContext);
        return new OMSourcedElementImpl(wrapperQName, factory,
                new WrappedTextNodeOMDataSourceFromDataSource(wrapperQName, dataSource, cs));
    }
}
