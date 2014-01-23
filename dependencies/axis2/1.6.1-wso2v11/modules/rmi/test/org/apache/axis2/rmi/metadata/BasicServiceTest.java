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

package org.apache.axis2.rmi.metadata;

import junit.framework.TestCase;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.service.BasicService;
import org.apache.axis2.rmi.wsdl.WSDL11DefinitionBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class BasicServiceTest extends TestCase {

    public void testPopulateMetaData(){

        try {
            Configurator configurator = new Configurator();
            Service service = new Service(BasicService.class,configurator);
            service.populateMetaData();
            System.out.println("OK");
        } catch (MetaDataPopulateException e) {
            fail();
        }

    }

    public void testGenerateSchema(){
        Configurator configurator = new Configurator();
        Service service = new Service(BasicService.class,configurator);
        try {
            service.populateMetaData();
            service.generateSchema();
            WSDL11DefinitionBuilder definitionBuilder = new WSDL11DefinitionBuilder(service);
            Definition definition = definitionBuilder.generateWSDL();

            WSDL11ToAxisServiceBuilder bulder = new WSDL11ToAxisServiceBuilder(definition,null,null);
            bulder.populateService();

        } catch (MetaDataPopulateException e) {
            fail();
        } catch (SchemaGenerationException e) {
            fail();
        } catch (IOException e) {
            fail();
        }
    }

    public void testWSDLDefinitionObjects() {
        try {
            Definition definition = WSDLFactory.newInstance().newDefinition();

            ExtensionRegistry extensionRegistry =
                    WSDLFactory.newInstance().newPopulatedExtensionRegistry();
            definition.setExtensionRegistry(extensionRegistry);
            Schema schema = (Schema) extensionRegistry.createExtension(Types.class,
                    new QName("http://www.w3.org/2001/XMLSchema","schema"));
            schema.setDocumentBaseURI("urn:test");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element element = document.createElementNS("http://www.w3.org/2001/XMLSchema","schema");
            element.setPrefix("tst");
            schema.setElement(element);
            schema.setElementType(new QName("http://www.w3.org/2001/XMLSchema","schema"));
            Types types = definition.createTypes();
            definition.setTypes(types);
            definition.getTypes().addExtensibilityElement(schema);

            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
            wsdlWriter.writeWSDL(definition, System.out);
            System.out.println("OK");
        } catch (WSDLException e) {
            fail();
        } catch (ParserConfigurationException e) {
            fail();
        }

    }
}
