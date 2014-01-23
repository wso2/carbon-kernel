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
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Iterator;

public class JSONBuilder implements Builder {
    private static final QName JsonRoot = new QName("http://org.apache.synapse/json/", "xmlPayload", "sjnr");
    private static final int FiveKB = 5120;
    private static final String JSON_STREAM = "JSON_STREAM";
    private static final String JSON_STRING = "JSON_STRING";
    private XMLOutputFactory factory = XMLOutputFactory.newInstance();

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        SOAPEnvelope envelope = null;
        if (messageContext.getProperty(JSON_STREAM) == null
            && messageContext.getProperty(JSON_STRING) == null) {
            JSONStreamBuilder builder = new JSONStreamBuilder();
            envelope = (SOAPEnvelope) builder.processDocument(inputStream, contentType, messageContext);
        }
        StringBuilder stringBuilder = new StringBuilder(FiveKB);
        insertRoot(stringBuilder);
        if (messageContext.getProperty(JSON_STREAM) != null) {
            BufferedReader buf = new BufferedReader(
                    new InputStreamReader((InputStream) messageContext.getProperty(JSON_STREAM)));
            String line;
            try {
                while ((line = buf.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        } else if (messageContext.getProperty(JSON_STRING) != null) {
            stringBuilder.append((String) messageContext.getProperty(JSON_STRING));
        } else {
            throw new RuntimeException("No JSON data found in the request.");
        }
        insertEnd(stringBuilder);
        OMElement payload;
        try {
            payload = parse(stringBuilder);
        } catch (Exception e) {
            throw new AxisFault("Failed to convert JSON to XML payload. " + e.getMessage());
        }
        if (messageContext.getProperty(JSON_STRING) == null) {
            //messageContext.setProperty(JSON_STRING, stringBuilder.substring(17, stringBuilder.length() - 1));
        }
        if (envelope == null) {
            SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
            envelope = factory.getDefaultEnvelope();
        }
        boolean preserveRoot = messageContext.getProperty("PRESERVE_JSON_ROOT") != null;
        makeSoapBody(envelope.getBody(), payload,
                     !preserveRoot ? false : (Boolean) messageContext.getProperty("PRESERVE_JSON_ROOT"));
        return envelope;
    }

    private void insertRoot(StringBuilder sb) {
        sb.append("{ \"xmlPayload\" : ");
    }

    private void insertEnd(StringBuilder sb) {
        sb.append("}");
    }

    private OMElement parse(StringBuilder sb) throws JSONException, XMLStreamException, IOException {
        StringWriter sw = new StringWriter(FiveKB);
        JSONObject jsonObject = new JSONObject(sb.toString());
        AbstractXMLStreamReader reader = new MappedXMLStreamReader(jsonObject);

        XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(sw);

        xmlStreamWriter.writeStartDocument();
        while (reader.hasNext()) {
            int x = reader.next();
            switch (x) {
                case XMLStreamConstants.START_ELEMENT:
                    xmlStreamWriter.writeStartElement(reader.getPrefix(), reader.getLocalName(),
                                                      reader.getNamespaceURI());
                    int namespaceCount = reader.getNamespaceCount();
                    for (int i = namespaceCount - 1; i >= 0; i--) {
                        xmlStreamWriter.writeNamespace(reader.getNamespacePrefix(i),
                                                       reader.getNamespaceURI(i));
                    }
                    int attributeCount = reader.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        xmlStreamWriter.writeAttribute(reader.getAttributePrefix(i),
                                                       reader.getAttributeNamespace(i),
                                                       reader.getAttributeLocalName(i),
                                                       reader.getAttributeValue(i));
                    }
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.CHARACTERS:
                    xmlStreamWriter.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.CDATA:
                    xmlStreamWriter.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    xmlStreamWriter.writeEndElement();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    xmlStreamWriter.writeEndDocument();
                    break;
                case XMLStreamConstants.SPACE:
                    break;
                case XMLStreamConstants.COMMENT:
                    xmlStreamWriter.writeComment(reader.getText());
                    break;
                case XMLStreamConstants.DTD:
                    xmlStreamWriter.writeDTD(reader.getText());
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    xmlStreamWriter
                            .writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    xmlStreamWriter.writeEntityRef(reader.getLocalName());
                    break;
                default:
                    throw new RuntimeException("Error in converting JSON to XML");
            }
        }
        xmlStreamWriter.writeEndDocument();
        xmlStreamWriter.flush();
        xmlStreamWriter.close();
        OMElement element = AXIOMUtil.stringToOM(sw.toString());
        OMNamespace ns = OMAbstractFactory.getOMFactory().createOMNamespace(
                JsonRoot.getNamespaceURI(), JsonRoot.getPrefix());
        element.setNamespace(ns);
        return element;
    }

    private void makeSoapBody(SOAPBody body, OMElement payload, boolean preserveRoot) {
        if (preserveRoot) {
            body.addChild(payload);
        } else {
            Iterator children = payload.getChildren();
            while (children.hasNext()) {
                Object e = children.next();
                if (e instanceof OMElement) {
                    children.remove();
                    body.addChild((OMNode) e);
                }
            }
        }
    }
}
