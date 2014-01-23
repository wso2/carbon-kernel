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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.axis2.transport.base.BaseConstants;

public class BinaryFormatter implements MessageFormatterEx {
    public byte[] getBytes(MessageContext messageContext, OMOutputFormat format) throws AxisFault {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeTo(messageContext, format, baos, true);
        return baos.toByteArray();
    }

    private DataHandler getDataHandler(MessageContext messageContext) {
        OMElement firstChild = messageContext.getEnvelope().getBody().getFirstElement();
        if (BaseConstants.DEFAULT_BINARY_WRAPPER.equals(firstChild.getQName())) {
            OMNode omNode = firstChild.getFirstOMChild();
            if (omNode != null && omNode instanceof OMText) {
                Object dh = ((OMText)omNode).getDataHandler();
                if (dh != null && dh instanceof DataHandler) {
                    return (DataHandler)dh;
                }
            }
        }
        return null;
    }
    
    public void writeTo(MessageContext messageContext, OMOutputFormat format,
            OutputStream outputStream, boolean preserve) throws AxisFault {
        DataHandler dh = getDataHandler(messageContext);
        if (dh != null) {
            try {
                dh.writeTo(outputStream);
            } catch (IOException e) {
                throw new AxisFault("Error serializing binary content of element : " +
                                BaseConstants.DEFAULT_BINARY_WRAPPER, e);
            }
        }        
    }

    public String getContentType(MessageContext messageContext,
            OMOutputFormat format, String soapAction) {
        DataHandler dh = getDataHandler(messageContext);
        if (dh != null) {
            return dh.getContentType();
        } else {
            return null;
        }
    }

    public URL getTargetAddress(MessageContext messageContext,
            OMOutputFormat format, URL targetURL) throws AxisFault {
        return URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);
    }

    public String formatSOAPAction(MessageContext messageContext,
            OMOutputFormat format, String soapAction) {
        return null;
    }

    public DataSource getDataSource(MessageContext messageContext,
            OMOutputFormat format, String soapAction) throws AxisFault {
        return getDataHandler(messageContext).getDataSource();
    }
}
