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

package org.apache.axis2.corba.deployer;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.corba.idl.types.DataType;
import org.apache.axis2.corba.idl.types.ExceptionType;
import org.apache.axis2.corba.idl.types.IDL;
import org.apache.axis2.corba.idl.types.Interface;
import org.apache.axis2.corba.idl.types.Operation;
import org.apache.axis2.corba.receivers.CorbaUtil;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.util.Loader;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.omg.CORBA_2_3.ORB;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CorbaDeployer extends AbstractDeployer implements DeploymentConstants, CorbaConstants {
    private static final Log log = LogFactory.getLog(CorbaDeployer.class);
    private AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;

    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.axisConfig = this.configCtx.getAxisConfiguration();
        /*try {
            System.in.read();
        } catch (IOException e) {

        }*/
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String name = null;
        try {
            deploymentFileData.setClassLoader(axisConfig.getServiceClassLoader());
            AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
            serviceGroup.setServiceGroupClassLoader(deploymentFileData.getClassLoader());
            ArrayList serviceList = processService(deploymentFileData, serviceGroup, configCtx);
            DeploymentEngine.addServiceGroup(serviceGroup, serviceList, deploymentFileData.getFile().toURL(), deploymentFileData, axisConfig);
            name = deploymentFileData.getName();
            super.deploy(deploymentFileData);
            log.info("Deploying " + name);
        } catch (AxisFault axisFault) {
            log.error("Error while deploying " + name, axisFault);
        } catch (MalformedURLException e) {
            log.error("Error while deploying " + name, e);
        } catch (Exception e) {
            log.error("Error while deploying " + name, e);
        }
    }

    private ArrayList processService(DeploymentFileData deploymentFileData, AxisServiceGroup serviceGroup, ConfigurationContext configCtx) throws Exception {
        String filename = deploymentFileData.getAbsolutePath();
        File file = new File(filename);
        serviceGroup.setServiceGroupName(deploymentFileData.getName());
        String serviceName = DescriptionBuilder.getShortFileName(deploymentFileData.getName());

        AxisService axisService = new AxisService();
        axisService.setName(serviceName);
        axisService.setParent(serviceGroup);
        axisService.setClassLoader(deploymentFileData.getClassLoader());

        InputStream in = null;
        try {
            in = new FileInputStream(file);
            DescriptionBuilder builder = new DescriptionBuilder(in, configCtx);
            OMElement rootElement = builder.buildOM();
            String elementName = rootElement.getLocalName();
            if (TAG_SERVICE.equals(elementName)) {
                populateService(axisService, rootElement, file.getParent());
            } else {
                throw new AxisFault("Invalid " + deploymentFileData.getAbsolutePath() + " found");
            }
        } catch (FileNotFoundException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.FILE_NOT_FOUND, e.getMessage()));
        } catch (XMLStreamException e) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.XML_STREAM_EXCEPTION, e.getMessage()));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.info(Messages.getMessage("errorininputstreamclose"));
                }
            }
        }

        ArrayList serviceList = new ArrayList();
        serviceList.add(axisService);
        return serviceList;
    }

    private void populateService(AxisService service, OMElement service_element, String directory)
            throws DeploymentException {
        try {
            // Processing service level parameters
            Iterator itr = service_element.getChildrenWithName(new QName(TAG_PARAMETER));
            processParameters(itr, service, service.getParent());

            // process service description
            OMElement descriptionElement = service_element.getFirstChildWithName(new QName(TAG_DESCRIPTION));
            if (descriptionElement != null) {
                OMElement descriptionValue = descriptionElement.getFirstElement();
                if (descriptionValue != null) {
                    StringWriter writer = new StringWriter();
                    descriptionValue.build();
                    descriptionValue.serialize(writer);
                    writer.flush();
                    service.setDocumentation(writer.toString());
                } else {
                    service.setDocumentation(descriptionElement.getText());
                }
            } else {
                OMAttribute serviceNameatt = service_element.getAttribute(new QName(ATTRIBUTE_NAME));

                if (serviceNameatt != null) {
                    if (!"".equals(serviceNameatt.getAttributeValue().trim())) {
                        service.setDocumentation(serviceNameatt.getAttributeValue());
                    }
                }
            }
            OMAttribute serviceNameatt = service_element.getAttribute(new QName(ATTRIBUTE_NAME));

            // If the service name is explicitly specified in the services.xml
            // then use that as the service name
            if (serviceNameatt != null) {
                if (!"".equals(serviceNameatt.getAttributeValue().trim())) {
                    service.setName(serviceNameatt.getAttributeValue());
                    // To be on the safe side
                    if (service.getDocumentation() == null) {
                        service.setDocumentation(serviceNameatt.getAttributeValue());
                    }
                }
            }

            // Process WS-Addressing flag attribute
            OMAttribute addressingRequiredatt = service_element.getAttribute(new QName(
                    ATTRIBUTE_WSADDRESSING));
            if (addressingRequiredatt != null) {
                String addressingRequiredString = addressingRequiredatt.getAttributeValue();
                AddressingHelper.setAddressingRequirementParemeterValue(service, addressingRequiredString);
            }

            // Setting service target namespace if any
            OMAttribute targetNameSpace = service_element.getAttribute(new QName(TARGET_NAME_SPACE));
            if (targetNameSpace != null) {
                String nameSpeceVale = targetNameSpace.getAttributeValue();
                if (nameSpeceVale != null && !"".equals(nameSpeceVale)) {
                    service.setTargetNamespace(nameSpeceVale);
                }
            } else {
                if (service.getTargetNamespace() == null || "".equals(service.getTargetNamespace())) {
                    service.setTargetNamespace(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
                }
            }

            // Setting schema namespece if any
            OMElement schemaElement = service_element.getFirstChildWithName(new QName(SCHEMA));
            if (schemaElement != null) {
                OMAttribute schemaNameSpace = schemaElement.getAttribute(new QName(SCHEMA_NAME_SPACE));
                if (schemaNameSpace != null) {
                    String nameSpeceVale = schemaNameSpace.getAttributeValue();
                    if (nameSpeceVale != null && !"".equals(nameSpeceVale)) {
                        service.setSchemaTargetNamespace(nameSpeceVale);
                    }
                }
                OMAttribute elementFormDefault = schemaElement.getAttribute(new QName(
                        SCHEMA_ELEMENT_QUALIFIED));
                if (elementFormDefault != null) {
                    String value = elementFormDefault.getAttributeValue();
                    if ("true".equals(value)) {
                        service.setElementFormDefault(true);
                    } else if ("false".equals(value)) {
                        service.setElementFormDefault(false);
                    }
                }
            }

            // Removing exclude operations
            OMElement excludeOperations = service_element.getFirstChildWithName(new QName(TAG_EXCLUDE_OPERATIONS));
            ArrayList excludeops = null;
            if (excludeOperations != null) {
                excludeops = new ArrayList();
                Iterator excludeOp_itr = excludeOperations.getChildrenWithName(new QName(TAG_OPERATION));
                while (excludeOp_itr.hasNext()) {
                    OMElement opName = (OMElement) excludeOp_itr.next();
                    excludeops.add(opName.getText().trim());
                }
            }
            if (excludeops == null) {
                excludeops = new ArrayList();
            }

            // processing service-wide modules which required to engage globally
            Iterator moduleRefs = service_element.getChildrenWithName(new QName(TAG_MODULE));
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref.getAttribute(new QName(TAG_REFERENCE));
                String refName = moduleRefAttribute.getAttributeValue();
                axisConfig.addGlobalModuleRef(refName);
            }

            OMElement messageReceiver = service_element.getFirstChildWithName(
                    new QName(TAG_MESSAGE_RECEIVERS));
            ClassLoader loader = service.getClassLoader();

            // Set default message recievers
            service.addMessageReceiver("http://www.w3.org/2004/08/wsdl/in-only",
                    loadMessageReceiver(loader, "org.apache.axis2.corba.receivers.CorbaInOnlyMessageReceiver"));
            service.addMessageReceiver("http://www.w3.org/2004/08/wsdl/robust-in-only",
                    loadMessageReceiver(loader, "org.apache.axis2.corba.receivers.CorbaInOnlyMessageReceiver"));
            service.addMessageReceiver("http://www.w3.org/2004/08/wsdl/in-out",
                    loadMessageReceiver(loader, "org.apache.axis2.corba.receivers.CorbaMessageReceiver"));
            service.addMessageReceiver("http://www.w3.org/2004/08/wsdl/in-opt-out",
                    loadMessageReceiver(loader, "org.apache.axis2.corba.receivers.CorbaInOutAsyncMessageReceiver"));

            if (messageReceiver != null) {
                HashMap mrs = processMessageReceivers(loader, messageReceiver);
                Iterator keys = mrs.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    service.addMessageReceiver(key, (MessageReceiver) mrs.get(key));
                }
            }
            
            // processing transports
            OMElement transports = service_element.getFirstChildWithName(new QName(TAG_TRANSPORTS));
            if (transports != null) {
                Iterator transport_itr = transports.getChildrenWithName(new QName(TAG_TRANSPORT));
                ArrayList trs = new ArrayList();
                while (transport_itr.hasNext()) {
                    OMElement trsEle = (OMElement) transport_itr.next();
                    String tarnsportName = trsEle.getText().trim();
                    trs.add(tarnsportName);
                }
                service.setExposedTransports(trs);
            }

            // processing operations
            processOperations(service, axisConfig, excludeops, null, directory);
            Iterator operationsIterator = service.getOperations();
            while (operationsIterator.hasNext()) {
                AxisOperation operationDesc = (AxisOperation) operationsIterator.next();
                ArrayList wsamappings = operationDesc.getWSAMappingList();
                if (wsamappings == null) {
                    continue;
                }
                if (service.getOperation(operationDesc.getName()) == null) {
                    service.addOperation(operationDesc);
                }
                for (int j = 0; j < wsamappings.size(); j++) {
                    String mapping = (String) wsamappings.get(j);
                    if (mapping.length() > 0) {
                        service.mapActionToOperation(mapping, operationDesc);
                    }
                }
            }

            for (int i = 0; i < excludeops.size(); i++) {
                String opName = (String) excludeops.get(i);
                service.removeOperation(new QName(opName));
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        } catch (AxisFault axisFault) {
            throw new DeploymentException(Messages.getMessage(
                    DeploymentErrorMsgs.OPERATION_PROCESS_ERROR, axisFault.getMessage()), axisFault);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    protected HashMap processMessageReceivers(ClassLoader loader, OMElement element) throws DeploymentException {
        HashMap meps = new HashMap();
        Iterator iterator = element.getChildrenWithName(new QName(TAG_MESSAGE_RECEIVER));
        while (iterator.hasNext()) {
            OMElement receiverElement = (OMElement) iterator.next();
            OMAttribute receiverName = receiverElement.getAttribute(new QName(TAG_CLASS_NAME));
            String className = receiverName.getAttributeValue();
            MessageReceiver receiver = loadMessageReceiver(loader, className);
            OMAttribute mepAtt = receiverElement.getAttribute(new QName(TAG_MEP));
            meps.put(mepAtt.getAttributeValue(), receiver);
        }
        return meps;
    }

    protected MessageReceiver loadMessageReceiver(ClassLoader loader, String className) throws DeploymentException {
        MessageReceiver receiver = null;
        try {
            Class messageReceiver;
            if ((className != null) && !"".equals(className)) {
                messageReceiver = Loader.loadClass(loader, className);
                receiver = (MessageReceiver) messageReceiver.newInstance();
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(org.apache.axis2.i18n.Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER,
                    "ClassNotFoundException", className), e);
        } catch (IllegalAccessException e) {
            throw new DeploymentException(org.apache.axis2.i18n.Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER,
                    "IllegalAccessException", className), e);
        } catch (InstantiationException e) {
            throw new DeploymentException(org.apache.axis2.i18n.Messages.getMessage(
                    DeploymentErrorMsgs.ERROR_IN_LOADING_MESSAGE_RECEIVER,
                    "InstantiationException", className), e);
        }

        return receiver;
    }

    private void processParameters(Iterator parameters, ParameterInclude parameterInclude,
            ParameterInclude parent) throws DeploymentException {
        while (parameters.hasNext()) {
            // this is to check whether some one has locked the parmeter at the
            // top level
            OMElement parameterElement = (OMElement) parameters.next();
            Parameter parameter = new Parameter();
            // setting parameterElement
            parameter.setParameterElement(parameterElement);
            // setting parameter Name
            OMAttribute paramName = parameterElement.getAttribute(new QName(ATTRIBUTE_NAME));
            if (paramName == null) {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.BAD_PARAMETER_ARGUMENT, parameterElement.toString()));
            }
            parameter.setName(paramName.getAttributeValue());
            // setting parameter Value (the child element of the parameter)
            OMElement paramValue = parameterElement.getFirstElement();
            if (paramValue != null) {
                parameter.setValue(parameterElement);
                parameter.setParameterType(Parameter.OM_PARAMETER);
            } else {
                String paratextValue = parameterElement.getText();

                parameter.setValue(paratextValue);
                parameter.setParameterType(Parameter.TEXT_PARAMETER);
            }
            // setting locking attribute
            OMAttribute paramLocked = parameterElement.getAttribute(new QName(ATTRIBUTE_LOCKED));
            Parameter parentParam = null;
            if (parent != null) {
                parentParam = parent.getParameter(parameter.getName());
            }
            if (paramLocked != null) {
                String lockedValue = paramLocked.getAttributeValue();
                if (BOOLEAN_TRUE.equals(lockedValue)) {
                    // if the parameter is locked at some level parameter value
                    // replace by that
                    if ((parent != null) && parent.isParameterLocked(parameter.getName())) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.CONFIG_NOT_FOUND, parameter.getName()));
                    } else {
                        parameter.setLocked(true);
                    }
                } else {
                    parameter.setLocked(false);
                }
            }
            try {
                if (parent != null) {
                    if ((parentParam == null) || !parent.isParameterLocked(parameter.getName())) {
                        parameterInclude.addParameter(parameter);
                    }
                } else {
                    parameterInclude.addParameter(parameter);
                }
            } catch (AxisFault axisFault) {
                throw new DeploymentException(axisFault);
            }
        }
    }

    private void processOperations(AxisService axisService,
                                        AxisConfiguration axisConfig,
                                        ArrayList excludeOperations,
                                        ArrayList nonRpcMethods, String dirName) throws Exception {
        ORB orb = CorbaUtil.getORB(axisService);
        IDL idl = CorbaUtil.getIDL(axisService, orb, dirName);

        Parameter orbParam = new Parameter(ORB_LITERAL, orb);
        Parameter idlParam = new Parameter(IDL_LITERAL, idl);
        axisService.addParameter(orbParam);
        axisService.addParameter(idlParam);

        // adding name spaces
        NamespaceMap map = new NamespaceMap();
        map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX, Java2WSDLConstants.AXIS2_XSD);
        map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX, Java2WSDLConstants.URI_2001_SCHEMA_XSD);
        axisService.setNamespaceMap(map);

        Parameter interfaceNameParam = axisService.getParameter(INTERFACE_NAME);
        String interfaceName = (String) ((interfaceNameParam==null) ? null : interfaceNameParam.getValue());
        SchemaGenerator schemaGenerator = new SchemaGenerator(idl, interfaceName,
                axisService.getSchemaTargetNamespace(),
                axisService.getSchemaTargetNamespacePrefix());
        schemaGenerator.setExcludeMethods(excludeOperations);
        schemaGenerator.setNonRpcMethods(nonRpcMethods);
        if (!axisService.isElementFormDefault()) {
            schemaGenerator.setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
        }
        // package to namespace map
        schemaGenerator.setPkg2nsmap(axisService.getP2nMap());
        Collection schemas = schemaGenerator.generateSchema();
        axisService.addSchema(schemas);
        axisService.setSchemaTargetNamespace(schemaGenerator.getSchemaTargetNameSpace());
        axisService.setTypeTable(schemaGenerator.getTypeTable());
        if (Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE.equals(
                axisService.getTargetNamespace())) {
            axisService.setTargetNamespace(schemaGenerator.getTargetNamespace());
        }

        Interface intf = (Interface) idl.getInterfaces().get(interfaceName);
        Operation[] operations = intf.getOperations();

        TypeTable table = schemaGenerator.getTypeTable();
        PhasesInfo pinfo = axisConfig.getPhasesInfo();

        for (int i = 0; i < operations.length; i++) {
            Operation corbaOperation = operations[i];
            String opName = corbaOperation.getName();
            if (excludeOperations != null && excludeOperations.contains(opName)) {
                continue;
            }
            AxisOperation operation = axisService.getOperation(new QName(opName));
            // if the operation there in services.xml then try to set it schema element name
            if (operation != null) {
                AxisMessage inMessage = operation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inMessage != null) {
                    inMessage.setName(opName + Java2WSDLConstants.MESSAGE_SUFFIX);
                    QName complexSchemaType = table.getComplexSchemaType(opName);
                    inMessage.setElementQName(complexSchemaType);
                    if (complexSchemaType != null) {
                        axisService.addMessageElementQNameToOperationMapping(complexSchemaType,
                                operation);
                    }
                }
                DataType returnType = corbaOperation.getReturnType();
                if (returnType != null && !CorbaUtil.getQualifiedName(returnType).equals(VOID)) {
                    AxisMessage outMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    QName qNamefortheType = table.getQNamefortheType(opName + RESPONSE);
                    outMessage.setElementQName(qNamefortheType);
                    if (qNamefortheType != null) {
                        axisService.addMessageElementQNameToOperationMapping(qNamefortheType, operation);
                    }
                    outMessage.setName(opName + RESPONSE);
                }
                if (corbaOperation.hasRaises()) {
                    List extypes = corbaOperation.getRaises();
                    for (int j = 0; j < extypes.size(); j++) {
                        AxisMessage faultMessage = new AxisMessage();
                        ExceptionType extype = (ExceptionType) extypes.get(j);
                        String exname = extype.getName() ;
                        if(extypes.size()>1){
                            faultMessage.setName(opName + FAULT + j);
                        } else{
                            faultMessage.setName(opName + FAULT);
                        }
                        faultMessage.setElementQName(
                                table.getComplexSchemaType(exname + FAULT));
                        operation.setFaultMessages(faultMessage);
                    }

                }
            } else {
                operation = getAxisOperationforCorbaOperation(corbaOperation, table);
                MessageReceiver mr = axisService.getMessageReceiver(
                        operation.getMessageExchangePattern());
                if (mr != null) {
                    operation.setMessageReceiver(mr);
                } else {
                    mr = axisConfig.getMessageReceiver(operation.getMessageExchangePattern());
                    operation.setMessageReceiver(mr);
                }
                pinfo.setOperationPhases(operation);
                axisService.addOperation(operation);
            }
            if (operation.getInputAction() == null) {
                operation.setSoapAction("urn:" + opName);
            }
        }
    }

    private AxisOperation getAxisOperationforCorbaOperation(Operation corbaOperation,
                                                                  TypeTable table) throws AxisFault {
        AxisOperation operation;
        String opName = corbaOperation.getName();
        DataType returnType = corbaOperation.getReturnType();
        if (returnType == null || CorbaUtil.getQualifiedName(returnType).equals(VOID)) {
            if (corbaOperation.hasRaises()) {
                operation = AxisOperationFactory.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_OUT);
                AxisMessage outMessage = operation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                outMessage.setElementQName(table.getQNamefortheType(opName + RESPONSE));
                outMessage.setName(opName + RESPONSE);
            } else {
                operation = AxisOperationFactory.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_ONLY);
            }
        } else {
            operation = AxisOperationFactory.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_OUT);
            AxisMessage outMessage = operation.getMessage(
                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            outMessage.setElementQName(table.getQNamefortheType(opName + RESPONSE));
            outMessage.setName(opName + RESPONSE);
        }
        if (corbaOperation.hasRaises()) {
            List extypes = corbaOperation.getRaises();
            for (int j= 0 ; j < extypes.size() ; j++) {
                AxisMessage faultMessage = new AxisMessage();
                ExceptionType extype = (ExceptionType) extypes.get(j);
                String exname = extype.getName() ;
                if(extypes.size() >1){
                    faultMessage.setName(opName + FAULT + j);
                } else {
                    faultMessage.setName(opName + FAULT);
                }
                faultMessage.setElementQName(
                        table.getComplexSchemaType(exname + FAULT));
                operation.setFaultMessages(faultMessage);
            }
        }
        operation.setName(new QName(opName));
        AxisMessage inMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        if (inMessage != null) {
            inMessage.setElementQName(table.getComplexSchemaType(opName));
            inMessage.setName(opName + Java2WSDLConstants.MESSAGE_SUFFIX);
        }
        return operation;
    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        try {
            super.undeploy(fileName);
            fileName = Utils.getShortFileName(fileName);
            axisConfig.removeServiceGroup(fileName);
            log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED, fileName));
        } catch (AxisFault axisFault) {
            axisConfig.removeFaultyService(fileName);
        }
    }
}
