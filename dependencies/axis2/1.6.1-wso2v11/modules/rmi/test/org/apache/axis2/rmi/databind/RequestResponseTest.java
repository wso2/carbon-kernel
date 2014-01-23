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

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.metadata.Operation;
import org.apache.axis2.rmi.metadata.Service;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

public class RequestResponseTest extends DataBindTest {

    protected Service service;
    protected Object serviceObject;
    protected Class serviceClass;


    protected void setUp() throws Exception {
        super.setUp();
        this.service = new Service(this.serviceClass,this.configurator);
        this.service.populateMetaData();
        this.service.generateSchema();
        this.javaObjectSerializer = new JavaObjectSerializer(service.getProcessedTypeMap(),
                this.service.getConfigurator(),
                this.service.getSchemaMap());
        this.xmlStreamParser = new XmlStreamParser(service.getProcessedTypeMap(),
                this.service.getConfigurator(),
                this.service.getSchemaMap());

    }

    protected Object[] getInputObject(List inputObjects, Operation operation)
              throws XMLStreamException, XmlSerializingException, XmlParsingException {
          StringWriter inputStringWriter = new StringWriter();
          XMLStreamWriter inputXmlStreamWriter = StAXUtils.createXMLStreamWriter(inputStringWriter);
          this.javaObjectSerializer.serializeInputElement(inputObjects.toArray(),
                  operation.getInputElement(),
                  operation.getInputParameters(),
                  inputXmlStreamWriter);
          inputXmlStreamWriter.flush();
          String inputXmlString = inputStringWriter.toString();

          System.out.println("input Xml String ==> " + inputXmlString);

          XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(inputXmlString.getBytes()));
          Object[] objects = this.xmlStreamParser.getInputParameters(xmlReader, operation);
          return objects;
      }

      protected Object getReturnObject(Object returnObject, Operation operation)
              throws XMLStreamException, XmlSerializingException, XmlParsingException {
          // get the response xml serializer
          StringWriter outputStringWriter = new StringWriter();
          XMLStreamWriter outputXMLStringWriter = StAXUtils.createXMLStreamWriter(outputStringWriter);

          this.javaObjectSerializer.serializeOutputElement(returnObject,
                  operation.getOutPutElement(),
                  operation.getOutputParameter(),
                  outputXMLStringWriter);
          outputXMLStringWriter.flush();
          String outputXmlString = outputStringWriter.toString();
          System.out.println("output Xml String ==> " + outputXmlString);

          XMLStreamReader outputXmlReader =
                  StAXUtils.createXMLStreamReader(new ByteArrayInputStream(outputXmlString.getBytes()));

          return this.xmlStreamParser.getOutputObject(outputXmlReader, operation);
      }


}
