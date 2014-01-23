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

package org.apache.axis2.builder;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class MTOMBuilder implements Builder {

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {
        XMLStreamReader streamReader;
        try {
            Attachments attachments = messageContext.getAttachmentMap();
            String charSetEncoding = (String) messageContext
            .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            
            // Get the actual encoding by looking at the BOM of the InputStream
            PushbackInputStream pis = BuilderUtil.getPushbackInputStream(inputStream);
            String actualCharSetEncoding = BuilderUtil.getCharSetEncoding(pis, charSetEncoding);
            
            // Get the XMLStreamReader for this input stream
            streamReader = StAXUtils.createXMLStreamReader(StAXParserConfiguration.SOAP, pis, actualCharSetEncoding);        
            StAXBuilder builder = new MTOMStAXSOAPModelBuilder(streamReader,
                    attachments);
            SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();
            BuilderUtil
                    .validateSOAPVersion(BuilderUtil.getEnvelopeNamespace(contentType), envelope);
            BuilderUtil.validateCharSetEncoding(charSetEncoding, builder.getDocument()
                    .getCharsetEncoding(), envelope.getNamespace().getNamespaceURI());
            //Overriding the earlier setting by MIMEBuilder
            messageContext.setDoingSwA(false);
            messageContext.setDoingMTOM(true);
            return envelope;
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
