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

package org.apache.axis2.rmi.databind;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.databinding.utils.writer.MTOMAwareOMBuilder;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.Writer;


public abstract class RMIDataSource implements OMDataSource {
    public void serialize(OutputStream outputStream,
                          OMOutputFormat omOutputFormat) throws XMLStreamException {
        XMLStreamWriter xmlStreamWriter = StAXUtils.createXMLStreamWriter(outputStream);
        serialize(xmlStreamWriter);
        xmlStreamWriter.flush();
    }

    public void serialize(Writer writer,
                          OMOutputFormat omOutputFormat) throws XMLStreamException {
        serialize(StAXUtils.createXMLStreamWriter(writer));
    }

    public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        MTOMAwareXMLStreamWriter mtomAwareXMLStreamWriter = new MTOMAwareXMLSerializer(xmlStreamWriter);
        serialize(mtomAwareXMLStreamWriter);
    }

    public XMLStreamReader getReader() throws XMLStreamException {
        // since only ADBBeans related to elements can be serialized
        // we are safe in passing null here.
        MTOMAwareOMBuilder mtomAwareOMBuilder = new MTOMAwareOMBuilder();
        serialize(mtomAwareOMBuilder);
        return mtomAwareOMBuilder.getOMElement().getXMLStreamReader();
    }

    public abstract void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException;
}
