/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.json;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class JSONStreamBuilder implements Builder {
    public OMElement processDocument(InputStream inputStream, String s,
                                     MessageContext messageContext) throws AxisFault {
        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();

        if (inputStream != null) {
            messageContext.setProperty("JSON_STREAM", inputStream);
        } else {
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
                messageContext.setProperty("JSON_STRING", jsonString);
            } else {
                //setting empty json object for no data
                messageContext.setProperty("JSON_STRING", "{}");
            }
        }

        return envelope;
    }
}
