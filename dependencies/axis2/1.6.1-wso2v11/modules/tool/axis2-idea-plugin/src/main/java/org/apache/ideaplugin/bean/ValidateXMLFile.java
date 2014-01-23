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

package org.apache.ideaplugin.bean;


import org.apache.axis2.tools.component.WizardPanel;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;

/**
 * this calss used for check service xml validation
 */
public class ValidateXMLFile {

    public final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

    public  boolean Validate(String args) {
        try {
            // define the type of schema  get validation driver:
            SchemaFactory schemafactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);

            // create schema by reading it from an XSD file:
            java.net.URL resource = WizardPanel.class.getResource("/resources/service.xsd");
            Schema schema = schemafactory.newSchema(new StreamSource(resource.getPath()));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(args)));

            schema.newValidator().validate(new DOMSource(doc));

            return true;
        }catch (SAXException ex) {
         //   ex.printStackTrace();
            return false;
        } catch (Exception ex) {
          //  ex.printStackTrace();
             return false;
        }

    }
}
