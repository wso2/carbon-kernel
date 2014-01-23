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

package org.apache.axis2.json.gson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.gson.factory.JsonConstant;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class JsonFormatter implements MessageFormatter {
    private static final Log log = LogFactory.getLog(JsonFormatter.class);

    public byte[] getBytes(MessageContext messageContext, OMOutputFormat omOutputFormat) throws AxisFault {
        return new byte[0];
    }

    public void writeTo(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat, OutputStream outputStream, boolean b) throws AxisFault {
        String charSetEncoding = (String) outMsgCtxt.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
        JsonWriter jsonWriter;
        String msg;

        try {
            jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream, charSetEncoding));
            Object retObj = outMsgCtxt.getProperty(JsonConstant.RETURN_OBJECT);

            if (outMsgCtxt.isProcessingFault()) {
                OMElement element = outMsgCtxt.getEnvelope().getBody().getFirstElement();
                try {
                    jsonWriter.beginObject();
                    jsonWriter.name(element.getLocalName());
                    jsonWriter.beginObject();
                    Iterator childrenIterator = element.getChildElements();
                    while (childrenIterator.hasNext()) {
                        Object next = childrenIterator.next();
                        OMElement omElement = (OMElement) next;
                        jsonWriter.name(omElement.getLocalName());
                        jsonWriter.value(omElement.getText());
                    }
                    jsonWriter.endObject();
                    jsonWriter.endObject();
                    jsonWriter.flush();
                    jsonWriter.close();
                } catch (IOException e) {
                    throw new AxisFault("Error while processing fault code in JsonWriter");
                }

            } else if (retObj == null) {
                OMElement element = outMsgCtxt.getEnvelope().getBody().getFirstElement();
                QName elementQname = outMsgCtxt.getAxisOperation().getMessage
                        (WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getElementQName();

                ArrayList<XmlSchema> schemas = outMsgCtxt.getAxisService().getSchema();
                GsonXMLStreamWriter xmlsw = new GsonXMLStreamWriter(jsonWriter,
                                                                    elementQname,
                                                                    schemas,
                                                                    outMsgCtxt.getConfigurationContext());
                try {
                    xmlsw.writeStartDocument();
                    if (b) {
                        element.serialize(xmlsw);
                    } else {
                        element.serializeAndConsume(xmlsw);
                    }
                    xmlsw.writeEndDocument();
                } catch (XMLStreamException e) {
                    throw new AxisFault("Error while writing to the output stream using JsonWriter", e);
                }

            } else {
                try {
                    Gson gson = new Gson();
                    jsonWriter.beginObject();
                    jsonWriter.name(JsonConstant.RESPONSE);
                    Type returnType = (Type) outMsgCtxt.getProperty(JsonConstant.RETURN_TYPE);
                    gson.toJson(retObj, returnType, jsonWriter);
                    jsonWriter.endObject();
                    jsonWriter.flush();

                } catch (IOException e) {
                    msg = "Exception occur while writting to JsonWriter at the JsonFormatter ";
                    log.error(msg, e);
                    throw AxisFault.makeFault(e);
                }
            }
        } catch (UnsupportedEncodingException e) {
            msg = "Exception occur when try to encode output stream usig  " +
                    Constants.Configuration.CHARACTER_SET_ENCODING + " charset";
            log.error(msg, e);
            throw AxisFault.makeFault(e);
        }
    }

    public String getContentType(MessageContext outMsgCtxt, OMOutputFormat omOutputFormat, String s) {
        return (String)outMsgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
    }

    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat omOutputFormat, URL url) throws AxisFault {
        return null;
    }

    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat omOutputFormat, String s) {
        return null;
    }
}
