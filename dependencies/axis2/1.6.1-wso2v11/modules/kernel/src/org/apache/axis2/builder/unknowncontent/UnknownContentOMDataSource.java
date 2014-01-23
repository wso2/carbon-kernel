package org.apache.axis2.builder.unknowncontent;
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

import java.io.OutputStream;
import java.io.Writer;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axis2.transport.MessageFormatter;

public class UnknownContentOMDataSource implements OMDataSource {

	public static QName unknownContentQName = new QName(
			"http://ws.apache.org/axis2", "UnknownContent");

	DataHandler genericContent;
	OMElement wrapperElement;
	
	public UnknownContentOMDataSource(DataHandler rootDataHandler) {
		genericContent = rootDataHandler;
		wrapperElement = createElement();
	}

	public XMLStreamReader getReader() throws XMLStreamException {
		return wrapperElement.getXMLStreamReader();
	}

	public void serialize(OutputStream output, OMOutputFormat format)
			throws XMLStreamException {
		wrapperElement.serialize(output,format);
	}

	public void serialize(Writer writer, OMOutputFormat format)
			throws XMLStreamException {
		wrapperElement.serialize(writer,format);
	}

	public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
		wrapperElement.serialize(xmlWriter);
	}

	public DataHandler getContent() {
		return genericContent;
	}
	
	private OMElement createElement()
	{
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMText textNode = factory.createOMText(genericContent, true);
		OMElement wrapperElement = factory.createOMElement(unknownContentQName);
		wrapperElement.addChild(textNode);
		return wrapperElement;
	}
}
