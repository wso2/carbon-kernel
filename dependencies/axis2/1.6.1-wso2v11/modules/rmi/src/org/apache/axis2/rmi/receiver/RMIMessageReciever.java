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

package org.apache.axis2.rmi.receiver;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.rmi.databind.JavaObjectSerializer;
import org.apache.axis2.rmi.databind.RMIDataSource;
import org.apache.axis2.rmi.databind.XmlStreamParser;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.metadata.Operation;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.metadata.Service;
import org.apache.axis2.rmi.metadata.xml.XmlElement;
import org.apache.axis2.rmi.util.NamespacePrefix;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RMIMessageReciever extends AbstractInOutMessageReceiver {

    // this contains all the data regarding the service.
    private Service service;
    private JavaObjectSerializer javaObjectSerializer;
    private XmlStreamParser xmlStreamParser;

    public RMIMessageReciever(Service service) {
        this.service = service;
        this.javaObjectSerializer = new JavaObjectSerializer(
                service.getProcessedTypeMap(),
                service.getConfigurator(),
                service.getSchemaMap());
        this.xmlStreamParser = new XmlStreamParser(
                service.getProcessedTypeMap(),
                service.getConfigurator(),
                service.getSchemaMap());
    }

    public void invokeBusinessLogic(MessageContext inputMessageContext,
                                    MessageContext outputMessageContext) throws AxisFault {

        String operationName = inputMessageContext.getOperationContext().getAxisOperation().getName().getLocalPart();
        Operation operation = service.getOperation(operationName);
        XMLStreamReader reader = inputMessageContext.getEnvelope().getBody().getFirstElement().getXMLStreamReaderWithoutCaching();
        SOAPFactory soapFactory = getSOAPFactory(inputMessageContext);
        if (operation != null) {
            // invoke the method
            try {
                Object serviceObject = service.getJavaClass().newInstance();
                Method javaOperation = operation.getJavaMethod();
                Object returnObject = javaOperation.invoke(serviceObject,
                        this.xmlStreamParser.getInputParameters(reader, operation));
                OMElement returnOMElement = getOutputOMElement(returnObject, operation, this.javaObjectSerializer, soapFactory);
                SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
                soapEnvelope.getBody().addChild(returnOMElement);
                outputMessageContext.setEnvelope(soapEnvelope);
                // set the out put element to message context

            } catch (InstantiationException e) {
                throw AxisFault.makeFault(e);
            } catch (IllegalAccessException e) {
                throw AxisFault.makeFault(e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException != null) {
                    Parameter parameter = this.service.getExceptionParameter(targetException.getClass());
                    if (parameter != null) {
                        AxisFault axisFault = new AxisFault(targetException.getMessage());
                        axisFault.setDetail(getParameterOMElement(targetException,
                                                                  parameter,
                                                                  this.javaObjectSerializer,
                                                                  soapFactory));
                        throw axisFault;
                    }
                }
                throw AxisFault.makeFault(e);
            } catch (XmlParsingException e) {
                throw AxisFault.makeFault(e);
            } catch (XMLStreamException e) {
                throw AxisFault.makeFault(e);
            }

        } else {
            throw new AxisFault("Can not find the operation");
        }

    }

    public OMElement getOutputOMElement(final Object returnObject,
                                        final Operation operation,
                                        final JavaObjectSerializer javaObjectSerializer,
                                        SOAPFactory soapFactory) {
        OMDataSource omDataSource = new RMIDataSource() {

            public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                try {
                    javaObjectSerializer.serializeOutputElement(returnObject,
                            operation.getOutPutElement(),
                            operation.getOutputParameter(),
                            xmlWriter);
                } catch (XmlSerializingException e) {
                    new XMLStreamException("Problem in serializing the return object", e);
                }
            }
        };
        XmlElement outXmlElement = operation.getOutPutElement();
        QName outElementQName = new QName(outXmlElement.getNamespace(), outXmlElement.getName());
        return new OMSourcedElementImpl(outElementQName, soapFactory, omDataSource);
    }

    public OMElement getParameterOMElement(final Object exceptionObject,
                                           final Parameter parameter,
                                           final JavaObjectSerializer javaObjectSerializer,
                                           SOAPFactory soapFactory){
        OMDataSource omDataSource = new RMIDataSource(){

            public void serialize(MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException {
                try {
                    javaObjectSerializer.serializeParameter(exceptionObject,parameter,xmlWriter, new NamespacePrefix());
                } catch (XmlSerializingException e) {
                    throw new XMLStreamException("problem in serializing the exception object ",e);
                }
            }
        };
        XmlElement exceptionElement = parameter.getElement();
        QName outElementQName = new QName(exceptionElement.getNamespace(), exceptionElement.getName());
        OMElement omElement = new OMSourcedElementImpl(outElementQName, soapFactory, omDataSource);
        return omElement;
    }




}
