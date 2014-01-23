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

package org.apache.axis2.json;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

/** Makes the OMSourcedElementImpl object with the JSONDataSource inside. */

public abstract class AbstractJSONOMBuilder implements Builder {


    public AbstractJSONOMBuilder() {
    }

    /**
     * gives the OMSourcedElementImpl using the incoming JSON stream
     *
     * @param inputStream - incoming message as an input stream
     * @param contentType - content type of the message (eg: application/json)
     * @param messageContext - inflow message context
     * @return OMSourcedElementImpl with JSONDataSource inside
     * @throws AxisFault
     */

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        String localName = "";
        String prefix = "";
        OMNamespace ns = factory.createOMNamespace("", "");

        //sets DoingREST to true because, security scenarios needs to handle in REST way
        messageContext.setDoingREST(true);

        Reader reader;
        
        //if the input stream is null, then check whether the HTTP method is GET, if so get the
        // JSON String which is received as a parameter, and make it an input stream

        if (inputStream == null) {
            EndpointReference endpointReference = messageContext.getTo();
            if (endpointReference == null) {
                throw new AxisFault("Cannot create DocumentElement without destination EPR");
            }

            String requestURL;
            try {
                requestURL = URIEncoderDecoder.decode(endpointReference.getAddress());
            } catch (UnsupportedEncodingException e) {
                throw AxisFault.makeFault(e);
            }

            String jsonString;
            int index;
            //As the message is received through GET, check for "=" sign and consider the second
            //half as the incoming JSON message
            if ((index = requestURL.indexOf("=")) > 0) {
                jsonString = requestURL.substring(index + 1);
                reader = new StringReader(jsonString);
            } else {
                //setting a default json string with operation name
                jsonString = this.getDefaultJSONString(messageContext,
                        messageContext.getAxisOperation().getName().getLocalPart());
                reader = new StringReader(jsonString);
            }
        } else {
            // Not sure where this is specified, but SOAPBuilder also determines the charset
            // encoding like that
            String charSetEncoding = (String)messageContext.getProperty(
                    Constants.Configuration.CHARACTER_SET_ENCODING);
            if (charSetEncoding == null) {
                charSetEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            try {
                reader = new InputStreamReader(inputStream, charSetEncoding);
            } catch (UnsupportedEncodingException ex) {
                throw AxisFault.makeFault(ex);
            }
        }

        /*
        Now we have to read the localname and prefix from the input stream
        if there is not prefix, message starts like {"foo":
        if there is a prefix, message starts like {"prefix:foo":
         */
        try {
            //read the stream until we find a : symbol
            char temp = (char)reader.read();
            while (temp != ':') {
                if (temp != ' ' && temp != '{' && temp != '\n' && temp != '\r' && temp != '\t') {
                    localName += temp;
                }
                temp = (char)reader.read();
            }

            //if the part we read ends with ", there is no prefix, otherwise it has a prefix
            if (localName.charAt(0) == '"') {
                if (localName.charAt(localName.length() - 1) == '"') {
                    localName = localName.substring(1, localName.length() - 1);
                } else {
                    prefix = localName.substring(1, localName.length()) + ":";
                    localName = "";
                    //so far we have read only the prefix, now lets read the localname
                    temp = (char)reader.read();
                    while (temp != ':') {
                        if (temp != ' ') {
                            localName += temp;
                        }
                        temp = (char)reader.read();
                    }
                    localName = localName.substring(0, localName.length() - 1);
                }
            }
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
        AbstractJSONDataSource jsonDataSource = getDataSource(reader, prefix, localName);
        return new OMSourcedElementImpl(localName, ns, factory, jsonDataSource);
    }

    protected abstract AbstractJSONDataSource getDataSource(Reader
            jsonReader, String prefix, String localName);

/**
     * This method returns a default JSON string whenever a GET request, which doesn't contain any
     * parameter, is served.
     *
     * @param msgCtx        MessageContext associated with the current request
     * @param localName     Name of the first element
     * @return              Default JSON string compliant with respective JSON To XML mapping type
     * @throws AxisFault    Is thrown when an unrecognized content type is received
     */
    public abstract String getDefaultJSONString(MessageContext msgCtx,
                                                String localName) throws AxisFault;


}
