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

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.URIEncoderDecoder;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * Base class for JSON message formatters.
 */


public abstract class AbstractJSONMessageFormatter implements MessageFormatter {

    public String getContentType(MessageContext msgCtxt, OMOutputFormat format,
                                 String soapActionString) {
        String contentType = (String)msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
        String encoding = format.getCharSetEncoding();
        if (contentType == null) {
            contentType = (String)msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE);
        }
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }
        return contentType;
    }

    /**
     * Gives the JSON message as an array of bytes. If the payload is an OMSourcedElementImpl and
     * it contains a JSONDataSource with a correctly formatted JSON String, gets it directly from
     * the DataSource and returns as a byte array. If not, the OM tree is expanded and it is
     * serialized into the output stream and byte array is returned.
     *
     * @param msgCtxt Message context which contains the soap envelope to be written
     * @param format  format of the message, this is ignored
     * @return the payload as a byte array
     * @throws AxisFault if there is an error in writing the message using StAX writer or IF THE
     *                   USER TRIES TO SEND A JSON MESSAGE WITH NAMESPACES USING THE "MAPPED"
     *                   CONVENTION.
     */

    public byte[] getBytes(MessageContext msgCtxt, OMOutputFormat format) throws AxisFault {
        OMElement element = msgCtxt.getEnvelope().getBody().getFirstElement();
        //if the element is an OMSourcedElementImpl and it contains a JSONDataSource with
        //correct convention, directly get the JSON string.

        //remove indentation from XML message, else XML > JSON conversion will fail
        reformatElement(msgCtxt.getEnvelope().getBody());

        if (element instanceof OMSourcedElementImpl &&
                getStringToWrite(((OMSourcedElementImpl)element).getDataSource()) != null) {
            String jsonToWrite = getStringToWrite(((OMSourcedElementImpl)element).getDataSource());
            return jsonToWrite.getBytes();
            //otherwise serialize the OM by expanding the tree
        } else {
            XMLStreamWriter jsonWriter = null;
            try {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                jsonWriter = getJSONWriter(bytesOut, format);
                //element.serializeAndConsume(jsonWriter);
                Iterator elems = msgCtxt.getEnvelope().getBody().getChildElements();
                serializeChildren(elems, jsonWriter);
                jsonWriter.writeEndDocument();

                return bytesOut.toByteArray();

            } catch (XMLStreamException e) {
                throw AxisFault.makeFault(e);
            } catch (FactoryConfigurationError e) {
                throw AxisFault.makeFault(e);
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            } catch (IllegalStateException e) {
                throw new AxisFault(
                        "Mapped formatted JSON with namespaces are not supported in Axis2. " +
                                "Make sure that your request doesn't include namespaces or " +
                                "use the Badgerfish convention");
            } finally {
                try {
                    if (jsonWriter != null) {
                        jsonWriter.close();
                    }
                } catch (XMLStreamException e) {
                    //ignore
                }
            }
        }
    }

    public String formatSOAPAction(MessageContext msgCtxt, OMOutputFormat format,
                                   String soapActionString) {
        return null;
    }

    private XMLStreamWriter getJSONWriter(OutputStream outStream, OMOutputFormat format)
            throws AxisFault {
        try {
            return getJSONWriter(new OutputStreamWriter(outStream, format.getCharSetEncoding()));
        } catch (UnsupportedEncodingException ex) {
            throw AxisFault.makeFault(ex);
        }
    }
    
    //returns the "Mapped" JSON writer
    protected abstract XMLStreamWriter getJSONWriter(Writer writer);

    /**
     * If the data source is a "Mapped" formatted data source, gives the JSON string by directly
     * taking from the data source.
     *
     * @param dataSource data source to be checked
     * @return the JSON string to write
     */
    protected abstract String getStringToWrite(OMDataSource dataSource);

    /**
     * Writes the JSON message to the output stream with the correct convention. If the payload is
     * an OMSourcedElementImpl and it contains a JSONDataSource with a correctly formatted JSON
     * String, gets it directly from the DataSource and writes to the output stream. If not, the OM
     * tree is expanded and it is serialized into the output stream.              *
     *
     * @param msgCtxt  Message context which contains the soap envelope to be written
     * @param format   format of the message, this is ignored
     * @param out      output stream to be written in to
     * @param preserve ignored
     * @throws AxisFault if there is an error in writing the message using StAX writer or IF THE
     *                   USER TRIES TO SEND A JSON MESSAGE WITH NAMESPACES USING THE "MAPPED"
     *                   CONVENTION.
     */

    public void writeTo(MessageContext msgCtxt, OMOutputFormat format,
                        OutputStream out, boolean preserve) throws AxisFault {
        OMElement element = msgCtxt.getEnvelope().getBody().getFirstElement();

        //remove indentation from XML message, else XML > JSON conversion will fail
        reformatElement(msgCtxt.getEnvelope().getBody());
        try {
            if (element instanceof OMSourcedElementImpl &&
                    getStringToWrite(((OMSourcedElementImpl)element).getDataSource()) != null) {
                String jsonToWrite =
                        getStringToWrite(((OMSourcedElementImpl)element).getDataSource());

                out.write(jsonToWrite.getBytes());
            } else {
                XMLStreamWriter jsonWriter = getJSONWriter(out, format);
                //element.serializeAndConsume(jsonWriter);
                Iterator elems = msgCtxt.getEnvelope().getBody().getChildElements();
                jsonWriter.writeStartDocument();
                serializeChildren(elems, jsonWriter);
                jsonWriter.writeEndDocument();
            }
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        } catch (IllegalStateException e) {
            throw new AxisFault(
                    "Mapped formatted JSON with namespaces are not supported in Axis2. " +
                            "Make sure that your request doesn't include namespaces or " +
                            "use the Badgerfish convention");
        }
    }

    public URL getTargetAddress(MessageContext msgCtxt, OMOutputFormat format, URL targetURL)
            throws AxisFault {

        String httpMethod =
                (String)msgCtxt.getProperty(Constants.Configuration.HTTP_METHOD);
        OMElement dataOut = msgCtxt.getEnvelope().getBody().getFirstElement();
        reformatElement(msgCtxt.getEnvelope().getBody());
        //if the http method is GET, send the json string as a parameter
        if (dataOut != null && (httpMethod != null)
                && Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
            XMLStreamWriter jsonWriter = null;
            try {
                String jsonString;
                if (dataOut instanceof OMSourcedElementImpl && getStringToWrite(
                        ((OMSourcedElementImpl) dataOut).getDataSource()) != null) {
                    jsonString = getStringToWrite(((OMSourcedElementImpl)
                            dataOut).getDataSource());
                } else {
                    StringWriter out = new StringWriter();
                    jsonWriter = getJSONWriter(out);
                    //dataOut.serializeAndConsume(jsonWriter);
                    Iterator elems = msgCtxt.getEnvelope().getBody().getChildElements();
                    jsonWriter.writeStartDocument();
                    serializeChildren(elems, jsonWriter);
                    jsonWriter.writeEndDocument();
                    jsonString = out.toString();
                }
                jsonString = URIEncoderDecoder.quoteIllegal(jsonString,
                        WSDL2Constants.LEGAL_CHARACTERS_IN_URL);
                String param = "query=" + jsonString;
                String returnURLFile = targetURL.getFile() + "?" + param;


                return new URL(targetURL.getProtocol(), targetURL.getHost(),
                        targetURL.getPort(), returnURLFile);
            } catch (MalformedURLException e) {
                throw AxisFault.makeFault(e);
            } catch (XMLStreamException e) {
                throw AxisFault.makeFault(e);
            } catch (UnsupportedEncodingException e) {
                throw AxisFault.makeFault(e);
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            } finally {
                if (jsonWriter != null) {
                    try {
                        jsonWriter.close();
                    } catch (XMLStreamException e) {
                        //ignore
                    }
                }
            }
        } else {
            return targetURL;
        }
    }

    private void reformatElement(OMElement elem) {
		removeIndentations(elem);
		removeNamespaces(elem, true);
	}

	private void removeIndentations(OMElement elem) {
		Iterator children = elem.getChildren();
		while (children.hasNext()) {
			OMNode child = (OMNode)children.next();
			if (child instanceof OMText) {
				if ("".equals(((OMText) child).getText().trim())) {
					children.remove();
				}
			} else if (child instanceof OMElement) {
				removeIndentations((OMElement) child);
			}
		}
	}

	private void removeNamespaces(OMElement elem, boolean processAttrbs) {
		OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = elem.getNamespace();
        String prefix;
        if (ns != null) {
            prefix = elem.getNamespace().getPrefix();
            elem.setNamespace(omFactory.createOMNamespace("", prefix));
        }
		Iterator children = elem.getChildElements();
		while (children.hasNext()) {
			removeNamespaces((OMElement)children.next(), processAttrbs);
		}
		if (!processAttrbs) {
			return;
		}
		Iterator attrbs = elem.getAllAttributes();
		while (attrbs.hasNext()) {
			OMAttribute attrb = (OMAttribute)attrbs.next();
			prefix = attrb.getQName().getPrefix();
			attrb.setOMNamespace(omFactory.createOMNamespace("", prefix));
		}
	}

    /**
     *
     * @param children  an Iterator to child elements.
     * @param jsonWriter
     * @throws XMLStreamException
     */
    private void serializeChildren(Iterator children, XMLStreamWriter jsonWriter)
            throws XMLStreamException {
        OMElement child;
        while (children.hasNext()) {
            child = (OMElement) children.next();
            child.serializeAndConsume(jsonWriter);
            children.remove();
        }
    }
}
