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

package org.apache.axis2.rmi.client;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.databind.JavaObjectSerializer;
import org.apache.axis2.rmi.databind.RMIDataSource;
import org.apache.axis2.rmi.databind.XmlStreamParser;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.metadata.Operation;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.metadata.Service;
import org.apache.axis2.rmi.metadata.xml.XmlElement;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.net.URL;


public class RMIClient extends ServiceClient {

    private Service service;
    private JavaObjectSerializer javaObjectSerializer;
    private XmlStreamParser xmlStreamParser;

    public RMIClient(ConfigurationContext configContext,
                     AxisService axisService)
            throws AxisFault {
        super(configContext, axisService);
    }

    public RMIClient(ConfigurationContext configContext,
                     Definition wsdl4jDefinition,
                     QName wsdlServiceName,
                     String portName)
            throws AxisFault {
        super(configContext, wsdl4jDefinition, wsdlServiceName, portName);
    }

    public RMIClient(ConfigurationContext configContext,
                     URL wsdlURL,
                     QName wsdlServiceName,
                     String portName) throws AxisFault {
        super(configContext, wsdlURL, wsdlServiceName, portName);
    }

    public RMIClient(Class serviceClass,
                     Configurator configurator,
                     String epr) throws AxisFault {
        this.service = new Service(serviceClass, configurator);
        this.setTargetEPR(new EndpointReference(epr));
        try {
            this.service.populateMetaData();
            this.service.generateSchema();
            this.javaObjectSerializer = new JavaObjectSerializer(
                    this.service.getProcessedTypeMap(),
                    configurator,
                    this.service.getSchemaMap());
            this.xmlStreamParser = new XmlStreamParser(
                    this.service.getProcessedTypeMap(),
                    configurator,
                    this.service.getSchemaMap());
        } catch (MetaDataPopulateException e) {
            throw AxisFault.makeFault(e);
        } catch (SchemaGenerationException e) {
            throw AxisFault.makeFault(e);
        }
    }

    public RMIClient(Class serviceClass,
                     String epr) throws AxisFault {
        this(serviceClass, new Configurator(), epr);
    }

    public Object invokeMethod(String operationName,
                               Object[] inputObjects) throws Exception {

        Operation operation = this.service.getOperation(operationName);
        OMElement inputOMElement = getInputOMElement(inputObjects,
                operation,
                this.javaObjectSerializer,
                OMAbstractFactory.getOMFactory());
        this.getOptions().setAction("urn:" + operationName);
        Object returnObject = null;
        try {

            OMElement returnOMElement = this.sendReceive(inputOMElement);
            returnObject = this.xmlStreamParser.getOutputObject(
                    returnOMElement.getXMLStreamReaderWithoutCaching(),
                    operation);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        } catch (XmlParsingException e) {
            throw AxisFault.makeFault(e);
        } catch (AxisFault axisFault){
            OMElement detailElement = axisFault.getDetail();
            if (detailElement != null){
               QName elementQName = detailElement.getQName();
               Parameter parameter = this.service.getExceptionParameter(elementQName);
               if (parameter != null){
                   Exception customException = (Exception) this.xmlStreamParser.getObjectForParameter(
                           detailElement.getXMLStreamReaderWithoutCaching(),
                           parameter);
                   throw customException;
               }
               throw axisFault;
            }
            throw axisFault;
        }

        return returnObject;
    }

    private OMElement getInputOMElement(final Object[] inputObjects,
                                        final Operation operation,
                                        final JavaObjectSerializer javaObjectSerializer,
                                        OMFactory omFactory) {
        OMDataSource omDataSource = new RMIDataSource() {

            public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                try {
                    javaObjectSerializer.serializeInputElement(inputObjects,
                            operation.getInputElement(),
                            operation.getInputParameters(),
                            xmlWriter);
                } catch (XmlSerializingException e) {
                    new XMLStreamException("Problem in serializing the return object", e);
                }
            }
        };
        XmlElement inputXmlElement = operation.getInputElement();
        QName inputElementQName = new QName(inputXmlElement.getNamespace(), inputXmlElement.getName());
        return new OMSourcedElementImpl(inputElementQName, omFactory, omDataSource);
    }

}
