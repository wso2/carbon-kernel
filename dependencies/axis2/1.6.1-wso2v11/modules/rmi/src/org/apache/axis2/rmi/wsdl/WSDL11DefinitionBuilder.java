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

package org.apache.axis2.rmi.wsdl;

import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.Operation;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.metadata.Service;
import org.apache.axis2.rmi.metadata.xml.XmlSchema;
import org.apache.axis2.rmi.util.Util;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * this class is used to create the wsdl11 object sturcture from the
 * service object structure.
 */
public class WSDL11DefinitionBuilder {

    /**
     * service to generate the
     */
    private Service service;

    /**
     * wsdl definition object to populate
     */
    private Definition wsdlDefinition;

    /**
     * port type for this wsdl
     */
    private PortType portType;

    private Binding httpSoapBinding;

    //TODO: generate code for this binding.
    private Binding httpSoap12Binding;


    public WSDL11DefinitionBuilder(Service service) {
        this.service = service;
    }

    /**
     * generates the wsdl for this service
     *
     * @throws SchemaGenerationException
     */
    public Definition generateWSDL() throws SchemaGenerationException {
        try {
            this.wsdlDefinition = WSDLFactory.newInstance().newDefinition();
            //TODO: keep the namespace prefix map if needed
            this.wsdlDefinition.addNamespace(Util.getNextNamespacePrefix(), this.service.getNamespace());
            this.wsdlDefinition.addNamespace(Util.getNextNamespacePrefix(), "http://schemas.xmlsoap.org/wsdl/soap/");
            this.wsdlDefinition.setTargetNamespace(this.service.getNamespace());
            // first generate the schemas
            generateTypes();
            generatePortType();
            generateBindings();
            generateService();
            generateOperationsAndMessages();
            return this.wsdlDefinition;
        } catch (WSDLException e) {
            throw new SchemaGenerationException("Error in creating a new wsdl definition", e);
        }
    }

    /**
     * generates the schemas for the wsdl
     *
     * @throws SchemaGenerationException
     */
    private void generateTypes() throws SchemaGenerationException {
        Types types = this.wsdlDefinition.createTypes();
        this.wsdlDefinition.setTypes(types);
        XmlSchema xmlSchema;
        for (Iterator iter = this.service.getSchemaMap().values().iterator(); iter.hasNext();) {
            xmlSchema = (XmlSchema) iter.next();
            xmlSchema.generateWSDLSchema();
            types.addExtensibilityElement(xmlSchema.getWsdlSchema());
        }
    }

    /**
     * generates the port type for the wsdl
     */
    private void generatePortType() {
        this.portType = this.wsdlDefinition.createPortType();
        this.portType.setUndefined(false);
        this.portType.setQName(new QName(this.service.getNamespace(), this.service.getName() + "PortType"));
        this.wsdlDefinition.addPortType(portType);
    }

    private void generateBindings() throws SchemaGenerationException {
        this.httpSoapBinding = this.wsdlDefinition.createBinding();
        this.httpSoapBinding.setUndefined(false);
        this.httpSoapBinding.setQName(new QName(this.service.getNamespace(),
                this.service.getName() + "HttpSoapBinding"));
        this.httpSoapBinding.setPortType(this.portType);
        // add soap transport parts
        ExtensionRegistry extensionRegistry = null;
        try {
            extensionRegistry = WSDLFactory.newInstance().newPopulatedExtensionRegistry();
            SOAPBinding soapBinding = (SOAPBinding) extensionRegistry.createExtension(
                    Binding.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "binding"));
            soapBinding.setTransportURI("http://schemas.xmlsoap.org/soap/http");
            soapBinding.setStyle("document");
            this.httpSoapBinding.addExtensibilityElement(soapBinding);
        } catch (WSDLException e) {
            throw new SchemaGenerationException("Can not crete a wsdl factory");
        }
        this.wsdlDefinition.addBinding(this.httpSoapBinding);
        this.wsdlDefinition.getBindings().put(this.httpSoapBinding.getQName(),
                this.httpSoapBinding);
    }

    private void generateService()
            throws SchemaGenerationException {
        // now add the binding portType and messages corresponding to every operation
        javax.wsdl.Service service = this.wsdlDefinition.createService();
        service.setQName(new QName(this.service.getNamespace(), this.service.getName()));

        Port port = this.wsdlDefinition.createPort();
        port.setName(this.service.getName() + "HttpSoapPort");
        port.setBinding(this.httpSoapBinding);
        ExtensionRegistry extensionRegistry = null;
        try {
            extensionRegistry = WSDLFactory.newInstance().newPopulatedExtensionRegistry();
            SOAPAddress soapAddress = (SOAPAddress) extensionRegistry.createExtension(
                    Port.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address"));
            soapAddress.setLocationURI("http://localhost:8080/axis2/services/" + this.service.getName());
            port.addExtensibilityElement(soapAddress);
        } catch (WSDLException e) {
            throw new SchemaGenerationException("Can not crete a wsdl factory");
        }
        service.addPort(port);
        this.wsdlDefinition.addService(service);
    }

    private void generateOperationsAndMessages()
            throws SchemaGenerationException {

        org.apache.axis2.rmi.metadata.Operation operation;
        Message inputMessage;
        Message outputMessage;
        javax.wsdl.Operation wsdlOperation;
        BindingOperation bindingOperation;

        //generate messages for exceptions
        Map exceptionMessagesMap = new HashMap();
        Class exceptionClass;
        Parameter parameter;
        Message faultMessage;
        String messageName;
        Part part;

        for (Iterator iter = this.service.getExceptionClassToParameterMap().keySet().iterator(); iter.hasNext();) {
            exceptionClass = (Class) iter.next();
            parameter = (Parameter) this.service.getExceptionClassToParameterMap().get(exceptionClass);
            messageName = exceptionClass.getName();
            messageName = messageName.substring(messageName.lastIndexOf(".") + 1);
            faultMessage = this.wsdlDefinition.createMessage();
            faultMessage.setUndefined(false);
            faultMessage.setQName(new QName(this.service.getNamespace(), messageName));

            part = this.wsdlDefinition.createPart();
            part.setName("fault");
            // add this element namespace to the definition
            if (this.wsdlDefinition.getPrefix(parameter.getElement().getNamespace()) == null) {
                this.wsdlDefinition.addNamespace(Util.getNextNamespacePrefix(), parameter.getElement().getNamespace());
            }
            part.setElementName(parameter.getElement().getType().getQname());
            faultMessage.addPart(part);
            exceptionMessagesMap.put(exceptionClass, faultMessage);
            this.wsdlDefinition.addMessage(faultMessage);
        }

        for (Iterator iter = this.service.getOperations().iterator(); iter.hasNext();) {
            operation = (org.apache.axis2.rmi.metadata.Operation) iter.next();
            // add input and out put messages
            inputMessage = getWSDLInputMessage(this.wsdlDefinition, operation);
            outputMessage = getWSDLOutputMessage(this.wsdlDefinition, operation);
            this.wsdlDefinition.addMessage(inputMessage);
            this.wsdlDefinition.addMessage(outputMessage);

            wsdlOperation = getWSDLOperation(this.wsdlDefinition,
                    inputMessage,
                    outputMessage,
                    exceptionMessagesMap,
                    operation);
            this.portType.addOperation(wsdlOperation);
            bindingOperation = getWSDLBindingOperation(this.wsdlDefinition,
                    wsdlOperation,
                    operation);
            this.httpSoapBinding.addBindingOperation(bindingOperation);

        }
    }

    /**
     * creates the corresponding wsdl message for input
     *
     * @param definition
     * @return input message
     */
    public Message getWSDLInputMessage(Definition definition,
                                       Operation operation) {
        Message inputMessage = definition.createMessage();
        inputMessage.setUndefined(false);
        inputMessage.setQName(new QName(operation.getNamespace(), operation.getName() + "RequestMessage"));
        Part part = definition.createPart();
        part.setName("input");
        part.setElementName(new QName(operation.getInputElement().getNamespace(), operation.getInputElement().getName()));
        inputMessage.addPart(part);
        return inputMessage;
    }

    /**
     * creates the corresponding wsdl message for output
     *
     * @param definition
     * @return output message
     */
    public Message getWSDLOutputMessage(Definition definition,
                                        Operation operation) {
        Message outputMessage = definition.createMessage();
        outputMessage.setUndefined(false);
        outputMessage.setQName(new QName(operation.getNamespace(), operation.getName() + "ResponseMessage"));
        Part part = definition.createPart();
        part.setName("output");
        part.setElementName(new QName(operation.getOutPutElement().getNamespace(), operation.getOutPutElement().getName()));
        outputMessage.addPart(part);
        return outputMessage;
    }

    /**
     * generates the wsdl operation for the rmi operation
     *
     * @param definition
     * @param inputMessage
     * @param outputMessage
     * @param faultMessageMap
     * @param operation
     * @return wsdl operation
     */
    public javax.wsdl.Operation getWSDLOperation(Definition definition,
                                                 Message inputMessage,
                                                 Message outputMessage,
                                                 Map faultMessageMap,
                                                 Operation operation) {
        javax.wsdl.Operation wsdlOperation = definition.createOperation();
        wsdlOperation.setUndefined(false);
        wsdlOperation.setName(operation.getName());
        Input input = definition.createInput();
        input.setMessage(inputMessage);
        input.setName("input");

        Output output = definition.createOutput();
        output.setMessage(outputMessage);
        output.setName("output");
        wsdlOperation.setInput(input);
        wsdlOperation.setOutput(output);
        wsdlOperation.setStyle(OperationType.REQUEST_RESPONSE);

        // add fault messages
        Class[] exceptionClasses = operation.getJavaMethod().getExceptionTypes();
        Message faultMessage;
        Fault fault;
        String faultName;
        for (int i = 0; i < exceptionClasses.length; i++) {
            faultName = exceptionClasses[i].getName();
            faultName = faultName.substring(faultName.lastIndexOf(".") + 1);
            faultMessage = (Message) faultMessageMap.get(exceptionClasses[i]);
            fault = definition.createFault();
            fault.setMessage(faultMessage);
            fault.setName("fault" + faultName);
            wsdlOperation.addFault(fault);
        }

        return wsdlOperation;
    }

    /**
     * generates the soap11 binding operation for the soap 11 binding.
     *
     * @param definition
     * @param wsdlOperation
     * @param operation
     * @return
     * @throws SchemaGenerationException
     */
    public BindingOperation getWSDLBindingOperation(Definition definition,
                                                    javax.wsdl.Operation wsdlOperation,
                                                    Operation operation)
            throws SchemaGenerationException {
        BindingOperation bindingOperation = definition.createBindingOperation();
        bindingOperation.setName(operation.getName());
        bindingOperation.setOperation(wsdlOperation);

        BindingInput bindingInput = definition.createBindingInput();
        bindingOperation.setBindingInput(bindingInput);

        BindingOutput bindingOutput = definition.createBindingOutput();
        bindingOperation.setBindingOutput(bindingOutput);

        ExtensionRegistry extensionRegistry = null;
        try {
            extensionRegistry = WSDLFactory.newInstance().newPopulatedExtensionRegistry();
            SOAPOperation soapOperation = (SOAPOperation) extensionRegistry.createExtension(
                    BindingOperation.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "operation"));
            soapOperation.setSoapActionURI("urn:" + operation.getName());
            soapOperation.setStyle("document");
            bindingOperation.addExtensibilityElement(soapOperation);

            SOAPBody inputSoapBody = (SOAPBody) extensionRegistry.createExtension(
                    BindingInput.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "body"));
            inputSoapBody.setUse("literal");
            bindingInput.addExtensibilityElement(inputSoapBody);

            SOAPBody outputSoapBody = (SOAPBody) extensionRegistry.createExtension(
                    BindingOutput.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "body"));
            outputSoapBody.setUse("literal");
            bindingOutput.addExtensibilityElement(outputSoapBody);

            // adding fault messages
            Class[] exceptionClasses = operation.getJavaMethod().getExceptionTypes();
            BindingFault bindingFault;
            String faultName;
            for (int i = 0; i < exceptionClasses.length; i++) {
                faultName = exceptionClasses[i].getName();
                faultName = faultName.substring(faultName.lastIndexOf(".") + 1);
                bindingFault = definition.createBindingFault();
                bindingFault.setName("fault" + faultName);
                bindingOperation.addBindingFault(bindingFault);

                SOAPFault soapFault = (SOAPFault) extensionRegistry.createExtension(
                        BindingFault.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "fault"));
                soapFault.setUse("literal");
                soapFault.setName("fault" + faultName);
                bindingFault.addExtensibilityElement(soapFault);
            }


        } catch (WSDLException e) {
            throw new SchemaGenerationException("Can not crete a wsdl factory");
        }

        return bindingOperation;
    }


}
