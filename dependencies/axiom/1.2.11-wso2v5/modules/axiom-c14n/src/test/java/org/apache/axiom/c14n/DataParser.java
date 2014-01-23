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

package org.apache.axiom.c14n;

import org.apache.axiom.c14n.omwrapper.factory.WrapperFactory;
import org.apache.axiom.c14n.omwrapper.interfaces.Document;
import org.apache.axiom.c14n.omwrapper.interfaces.Element;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;

import java.net.URL;
import java.io.RandomAccessFile;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class DataParser {
    private String fileName = null;
    private StAXOMBuilder builder = null;
    private byte [] bytes;

    public WrapperFactory fac = null;
    public Document doc = null;
    public Element docEle = null;
    public OMDocument omDoc = null;
    public OMElement omDocEle = null;

    public DataParser(String fileName) {
        this.fileName = fileName;
    }

    public void init() throws Exception {
        builder = new StAXOMBuilder(this.getClass().getResourceAsStream(fileName));
        fac = new WrapperFactory();
        omDoc = builder.getDocument();
        omDocEle = omDoc.getOMDocumentElement();
        doc = (Document) fac.getNode(omDoc);
        docEle = doc.getDocumentElement();
    }

    public void initWithNewFile(String fileName) throws Exception{
        this.fileName = fileName;
        init();
    }

    public byte[] getBytes() throws Exception{
        URL uri = getClass().getResource(fileName);
        RandomAccessFile raf = new RandomAccessFile(uri.getFile(),"r");
        byte[] bytes = new byte[(int)raf.length()];
        raf.readFully(bytes);
        return bytes;
    }
}
