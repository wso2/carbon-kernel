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

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;
import org.custommonkey.xmlunit.XMLTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

public class CharacterEncoding2Test extends XMLTestCase {
    String xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" +
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soap:Body>" +
            "<AgendaPesquisa>" +
            "<status>0</status>" +
            "<ListaContatosPesquisa>" +
            "<tipo>C</tipo>" +
            "<dono>lucia</dono>" +
            "<posicao>177</posicao>" +
            "<nome>xxx</nome>" +
            "<email></email>" +
            "</ListaContatosPesquisa>" +
            "</AgendaPesquisa>" +
            "</soap:Body>" +
            "</soap:Envelope>";

    public void testISO99591() throws Exception {
        ByteArrayInputStream byteInStr = new ByteArrayInputStream(xml.getBytes("iso-8859-1"));

        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(
                StAXUtils.createXMLStreamReader(byteInStr));

        SOAPEnvelope envelope = builder.getSOAPEnvelope();
        envelope.build();

        assertEquals("iso-8859-1", envelope.getXMLStreamReader().getCharacterEncodingScheme());

        ByteArrayOutputStream byteOutStr = new ByteArrayOutputStream();
        OMOutputFormat outputFormat = new OMOutputFormat();
        outputFormat.setCharSetEncoding("iso-8859-1");
        envelope.serialize(byteOutStr, outputFormat);

        assertXMLEqual(new InputStreamReader(new ByteArrayInputStream(xml.getBytes("iso-8859-1")),"iso-8859-1"),
                new InputStreamReader(new ByteArrayInputStream(byteOutStr.toByteArray()),"iso-8859-1"));
        
        builder.close();
    }
}
