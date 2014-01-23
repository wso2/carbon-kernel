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

package org.apache.axis2.datasource;


import org.apache.axiom.om.OMDataSourceExt;
import org.apache.axiom.om.ds.OMDataSourceExtBase;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * OMDataSource backed by a string containing xml data
 */
public class XMLStringDataSource extends OMDataSourceExtBase {
    String data;

    public XMLStringDataSource(String data) {
        super();
        this.data = data;
    }

    public void close() {
    }

    public OMDataSourceExt copy() {
        return new XMLStringDataSource(data);
    }

    public Object getObject() {
        return data;
    }

    public XMLStreamReader getReader() throws XMLStreamException {

        try {
            String encoding = "utf-8";
            InputStream is = new ByteArrayInputStream(getXMLBytes(encoding));
            return StAXUtils.createXMLStreamReader(is, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new XMLStreamException(e);
        }
    }

    public byte[] getXMLBytes(String encoding) throws UnsupportedEncodingException {
        return data.getBytes(encoding);
    }

    public boolean isDestructiveRead() {
        return false;
    }

    public boolean isDestructiveWrite() {
        return false;
    }
    
}
