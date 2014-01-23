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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.json.gson.rpc.JsonInOnlyRPCMessageReceiver;
import org.apache.axis2.json.gson.rpc.JsonRpcMessageReceiver;
import org.apache.axis2.json.gson.factory.JsonConstant;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import java.util.List;


public class JSONMessageHandler extends AbstractHandler {
    /**
     * This method will be called on each registered handler when a message
     * needs to be processed.  If the message processing is paused by the
     * handler, then this method will be called again for the handler that
     * paused the processing once it is resumed.
     * <p/>
     * This method may be called concurrently from multiple threads.
     * <p/>
     * Handlers that want to determine the type of message that is to be
     * processed (e.g. response vs request, inbound vs. outbound, etc.) can
     * retrieve that information from the MessageContext via
     * MessageContext.getFLOW() and
     * MessageContext.getAxisOperation().getMessageExchangePattern() APIs.
     *
     * @param msgContext the <code>MessageContext</code> to process with this
     *                   <code>Handler</code>.
     * @return An InvocationResponse that indicates what
     *         the next step in the message processing should be.
     * @throws org.apache.axis2.AxisFault if the handler encounters an error
     */

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        MessageReceiver messageReceiver = msgContext.getAxisOperation().getMessageReceiver();
        if (messageReceiver instanceof JsonRpcMessageReceiver || messageReceiver instanceof JsonInOnlyRPCMessageReceiver) {
            // do not need to parse XMLSchema list, as  this message receiver will not use GsonXMLStreamReader  to read the inputStream.
        } else {
            // Sever expect XML message
            Object tempObj = msgContext.getProperty(JsonConstant.IS_JSON_STREAM);
            if (tempObj != null) {
                // server has received a json stream and we need process it.
                boolean isJSON = Boolean.valueOf(tempObj.toString());
                Object o = msgContext.getProperty(JsonConstant.GSON_XML_STREAM_READER);
                if (o != null) {
                    GsonXMLStreamReader gsonXMLStreamReader = (GsonXMLStreamReader) o;
                    QName elementQname = msgContext.getAxisOperation().getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getElementQName();
                    List<XmlSchema> schemas = msgContext.getAxisService().getSchema();
                    gsonXMLStreamReader.initXmlStreamReader(elementQname, schemas, msgContext.getConfigurationContext());
                    StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(gsonXMLStreamReader);
                    OMElement omElement = stAXOMBuilder.getDocumentElement();
                    msgContext.getEnvelope().getBody().addChild(omElement);
                } else {
                    throw new AxisFault("GsonXMLStreamReader should not be null");
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }
}
