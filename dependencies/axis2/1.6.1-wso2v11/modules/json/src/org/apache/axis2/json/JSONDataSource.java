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

import org.codehaus.jettison.json.JSONTokener;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.Reader;
import java.util.HashMap;

/**
 * JSON data source implementation for the "Mapped" convention.
 */

public class JSONDataSource extends AbstractJSONDataSource {

    public JSONDataSource(Reader jsonReader, String localName) {
        super(jsonReader, localName);
    }

    /**
     * Gives the StAX reader using the "Mapped" formatted input JSON String.
     *
     * @return The XMLStreamReader according to the JSON String.
     * @throws javax.xml.stream.XMLStreamException
     *          if there is an error while making the StAX reader.
     */
    @Override
    public XMLStreamReader getReader() throws XMLStreamException {

        HashMap XMLToJSNNamespaceMap = new HashMap();
        XMLToJSNNamespaceMap.put("", "");

        //input factory for "Mapped" convention
        MappedXMLInputFactory inputFactory = new MappedXMLInputFactory(XMLToJSNNamespaceMap);
        String jsonString = "{" + localName + ":" + this.getJSONString();
        return inputFactory.createXMLStreamReader(new JSONTokener(jsonString));
    }
}
