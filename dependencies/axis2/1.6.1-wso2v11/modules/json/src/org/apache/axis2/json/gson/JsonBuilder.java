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

import com.google.gson.stream.JsonReader;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.gson.factory.JsonConstant;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class JsonBuilder implements Builder {
    public OMElement processDocument(InputStream inputStream, String s, MessageContext messageContext) throws AxisFault {
        messageContext.setProperty(JsonConstant.IS_JSON_STREAM , true);
        JsonReader jsonReader;
        String charSetEncoding=null;
           try {
               charSetEncoding = (String) messageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
               jsonReader = new JsonReader(new InputStreamReader(inputStream , charSetEncoding));
               GsonXMLStreamReader gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);
               messageContext.setProperty(JsonConstant.GSON_XML_STREAM_READER , gsonXMLStreamReader);
               // dummy envelop
               SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
               return soapFactory.getDefaultEnvelope();
           } catch (UnsupportedEncodingException e) {
               throw new AxisFault(charSetEncoding + " encoding is may not supported by json inputStream ", e);
           }
    }

}
