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

package org.apache.axis2.description;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.ExternalPolicySerializer;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyLocator;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AxisService2WSDL20 implements WSDL2Constants {

    private AxisService axisService;
    private String serviceName;
    private String[] eprs = null;
    private OMNamespace wsaw;

    private HashMap policiesInDescription = new HashMap();
    private ExternalPolicySerializer filter = null;
    
    public AxisService2WSDL20(AxisService service) {
        this.axisService = service;
        this.serviceName = service.getName();
    }

    public AxisService2WSDL20(AxisService service, String serviceName) {
        this.axisService = service;
        this.serviceName = serviceName;
    }

    /**
     * Generates a WSDL 2.0 document for this web service
     * @return The WSDL2 document element
     * @throws Exception - Thrown in case an exception occurs
     */
    public OMElement generateOM() throws Exception {

        Map nameSpacesMap = axisService.getNamespaceMap();
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace wsdl;
        
        //
        filter = new ExternalPolicySerializer();
		AxisConfiguration axisConfiguration = axisService
				.getAxisConfiguration();
		if (axisConfiguration != null) {
			filter.setAssertionsToFilter(axisConfiguration
					.getLocalPolicyAssertions());
		}
		//

        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.WSDL_NAMESPACE)) {
            wsdl = omFactory
                    .createOMNamespace(WSDL2Constants.WSDL_NAMESPACE,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.WSDL_NAMESPACE, nameSpacesMap));
        } else {
            wsdl = omFactory
                    .createOMNamespace(WSDL2Constants.WSDL_NAMESPACE,
                                       WSDL2Constants.DEFAULT_WSDL_NAMESPACE_PREFIX);
        }

        OMElement descriptionElement = omFactory.createOMElement(WSDL2Constants.DESCRIPTION, wsdl);

        // Declare all the defined namespaces in the document
        WSDLSerializationUtil.populateNamespaces(descriptionElement, nameSpacesMap);

        descriptionElement.declareNamespace(axisService.getTargetNamespace(),
                                            axisService.getTargetNamespacePrefix());
        wsaw = descriptionElement.declareNamespace(AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
        // Need to add the targetnamespace as an attribute according to the wsdl 2.0 spec
        OMAttribute targetNamespace = omFactory
                .createOMAttribute(WSDL2Constants.TARGET_NAMESPACE, null,
                                   axisService.getTargetNamespace());
        descriptionElement.addAttribute(targetNamespace);

        // Check whether the required namespaces are already in namespaceMap, if they are not
        // present declare them.
        OMNamespace wsoap;
        OMNamespace whttp;
        OMNamespace wsdlx;
        OMNamespace wrpc;

        OMNamespace tns = omFactory
                .createOMNamespace(axisService.getTargetNamespace(),
                                   axisService.getTargetNamespacePrefix());
        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_SOAP)) {
            wsoap = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_SOAP,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_SOAP, nameSpacesMap));
        } else {
            wsoap = descriptionElement
                    .declareNamespace(WSDL2Constants.URI_WSDL2_SOAP, WSDL2Constants.SOAP_PREFIX);
        }
        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_HTTP)) {
            whttp = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_HTTP,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_HTTP, nameSpacesMap));
        } else {
            whttp = descriptionElement
                    .declareNamespace(WSDL2Constants.URI_WSDL2_HTTP, WSDL2Constants.HTTP_PREFIX);
        }
        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_EXTENSIONS)) {
            wsdlx = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_EXTENSIONS,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_EXTENSIONS, nameSpacesMap));
        } else {
            wsdlx = descriptionElement.declareNamespace(WSDL2Constants.URI_WSDL2_EXTENSIONS,
                                                        WSDL2Constants.WSDL_EXTENTION_PREFIX);
        }
        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_RPC)) {
            wrpc = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_RPC,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_RPC, nameSpacesMap));
        } else {
            wrpc = descriptionElement.declareNamespace(WSDL2Constants.URI_WSDL2_RPC,
                                                        WSDL2Constants.WSDL_RPC_PREFIX);
        }

        // Add the documentation element
        WSDLSerializationUtil
                .addWSDLDocumentationElement(axisService, descriptionElement, omFactory, wsdl);

        // Add types element
        OMElement typesElement = omFactory.createOMElement(WSDL2Constants.TYPES_LOCAL_NALE, wsdl);
        axisService.populateSchemaMappings();
        ArrayList schemas = axisService.getSchema();
        for (int i = 0; i < schemas.size(); i++) {
            StringWriter writer = new StringWriter();
            XmlSchema schema = axisService.getSchema(i);

            if (!org.apache.axis2.namespace.Constants.URI_2001_SCHEMA_XSD
                    .equals(schema.getTargetNamespace())) {
                schema.write(writer);
                String schemaString = writer.toString();

                if (!"".equals(schemaString)) {
                    try {
                        typesElement.addChild(
                                XMLUtils.toOM(new ByteArrayInputStream(schemaString.getBytes())));
                    } catch (XMLStreamException e) {
                        throw AxisFault.makeFault(e);
                    }
                }
            }
        }
        descriptionElement.addChild(typesElement);

        Parameter parameter = axisService.getParameter(WSDL2Constants.INTERFACE_LOCAL_NAME);
        String interfaceName;
        if (parameter != null) {
            interfaceName = (String) parameter.getValue();
        } else {
            interfaceName = WSDL2Constants.DEFAULT_INTERFACE_NAME;
        }

        // Add the interface element
        descriptionElement.addChild(getInterfaceElement(wsdl, tns, wsdlx, wrpc, omFactory,
                                                        interfaceName));

        // axis2.xml indicated no HTTP binding?
        boolean disableREST = false;
        Parameter disableRESTParameter =
                axisService.getParameter(Constants.Configuration.DISABLE_REST);
        if (disableRESTParameter != null &&
                JavaUtils.isTrueExplicitly(disableRESTParameter.getValue())) {
            disableREST = true;
        }

        boolean disableSOAP11 = false;
        Parameter disableSOAP11Parameter = axisService
                .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP11);
        if (disableSOAP11Parameter != null
                && JavaUtils.isTrueExplicitly(disableSOAP11Parameter.getValue())) {
            disableSOAP11 = true;
        }

        // axis2.xml indicated no SOAP 1.2 binding?
        boolean disableSOAP12 = false;
        Parameter disableSOAP12Parameter =
        axisService.getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP12);
        if (disableSOAP12Parameter != null &&
                JavaUtils.isTrueExplicitly(disableSOAP12Parameter.getValue())) {
            disableSOAP12 = true;
        }        
        
        // Check whether the axisService has any endpoints. If they exists serialize them else
        // generate default endpoint elements.
        Set bindings = new HashSet();
        Map endpointMap = axisService.getEndpoints();
        Object value = axisService.getParameterValue("isCodegen");
        boolean isCodegen = false;
        if (JavaUtils.isTrueExplicitly(value)) {
           isCodegen = true;
        }
        if (endpointMap != null && endpointMap.size() > 0) {

            OMElement serviceElement = getServiceElement(wsdl, tns, omFactory, interfaceName);
            Iterator iterator = endpointMap.values().iterator();
            while (iterator.hasNext()) {
                // With the new binding hierachy in place we need to do some extra checking here.
                // If a service has both http and https listners up we should show two separate eprs
                // If the service was deployed with a WSDL and it had two endpoints for http and
                // https then we have two endpoints populated so we should serialize them instead
                // of updating the endpoints.
                AxisEndpoint axisEndpoint = (AxisEndpoint) iterator.next();
                /*
			    * Some transports might not be active at runtime.
			    */
                if (!isCodegen && !axisEndpoint.isActive()) {
                    continue;
                }
                if (axisEndpoint.getEndpointURL() == null || axisEndpoint.getEndpointURL().startsWith("local://")) {
                    //skip adding local endpoints to wsdl or if endpoint doesn't have url
                    continue;
                }

                AxisBinding axisBinding = axisEndpoint.getBinding();
                String type = axisBinding.getType();
                
                // If HTTP binding is disabled, do not add.
                if (WSDL2Constants.URI_WSDL2_HTTP.equals(type)) {
                    if (disableREST) {
                        continue;
                    }
                }
                
                // If SOAP 1.2 binding is disabled, do not add.
                String propertySOAPVersion =
                        (String) axisBinding.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
                if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(propertySOAPVersion)) {
                    if (disableSOAP12) {
                        continue;
                    }
                }

                if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(propertySOAPVersion)) {
                    if (disableSOAP11) {
                        continue;
                    }
                }



                bindings.add(axisBinding);
                OMElement endpointElement = axisEndpoint.toWSDL20(wsdl, tns, whttp);
                boolean endpointAlreadyAdded = false;
                Iterator endpointsAdded = serviceElement.getChildren();
                while (endpointsAdded.hasNext()) {
                    OMElement endpoint = (OMElement) endpointsAdded.next();
                    // Checking whether a endpoint with the same binding and address exists.
                    if (endpoint.getAttribute(new QName(WSDL2Constants.BINDING_LOCAL_NAME))
                            .getAttributeValue().equals(endpointElement.getAttribute(
                            new QName(WSDL2Constants.BINDING_LOCAL_NAME)).getAttributeValue())
                            && endpoint
                            .getAttribute(new QName(WSDL2Constants.ATTRIBUTE_ADDRESS))
                            .getAttributeValue().equals(endpointElement.getAttribute(
                            new QName(WSDL2Constants.ATTRIBUTE_ADDRESS)).getAttributeValue())) {
                        endpointAlreadyAdded = true;
                    }

                }
                if (!endpointAlreadyAdded) {
//                	addPolicyAsExtensibleElement(axisEndpoint, endpointElement);
                	Parameter modifyAddressParam = axisService
							.getParameter("modifyUserWSDLPortAddress");
					if (modifyAddressParam != null) {
						if (Boolean.parseBoolean((String) modifyAddressParam
								.getValue())) {
							String endpointURL = axisEndpoint
									.calculateEndpointURL();
							endpointElement
									.getAttribute(
											new QName(
													WSDL2Constants.ATTRIBUTE_ADDRESS))
									.setAttributeValue(endpointURL);
						}
					}
                	serviceElement.addChild(endpointElement);
                }
            }
            Iterator iter = bindings.iterator();
            while (iter.hasNext()) {
                AxisBinding binding = (AxisBinding) iter.next();
                OMElement bindingElement = binding.toWSDL20(wsdl, tns, wsoap, whttp,
                        interfaceName,
                        axisService.getNamespaceMap(),
                        AddressingHelper.getAddressingRequirementParemeterValue(axisService),
                        serviceName,wsaw);
                descriptionElement
                        .addChild(bindingElement);
            }

            descriptionElement.addChild(serviceElement);
        } else {

            // There are no andpoints defined hence generate default bindings and endpoints
            descriptionElement.addChild(
                    WSDLSerializationUtil.generateSOAP11Binding(omFactory, axisService, wsdl, wsoap,
                                                                tns, serviceName));
            if (!disableSOAP12) {
                descriptionElement.addChild(
                        WSDLSerializationUtil.generateSOAP12Binding(omFactory, axisService, wsdl, wsoap,
                                tns, serviceName));
            }
            if (!disableSOAP11) {
                descriptionElement.addChild(
                        WSDLSerializationUtil.generateSOAP11Binding(omFactory, axisService, wsdl, wsoap,
                                tns, serviceName));
            }
            if (!disableREST) {
                descriptionElement.addChild(
                        WSDLSerializationUtil.generateHTTPBinding(omFactory, axisService, wsdl,
                                                                  whttp,
                                                                  tns, serviceName));
            }
            descriptionElement
                    .addChild(WSDLSerializationUtil.generateServiceElement(omFactory, wsdl, tns,
                                                                           axisService, disableREST,
                                                                           disableSOAP12,disableSOAP11, eprs,
                                                                          serviceName));
        }
        
        ArrayList policies = new ArrayList(policiesInDescription.values());
        addPoliciesToDescriptionElement(policies, descriptionElement);

        return descriptionElement;
    }

    /**
     * Generates the interface element for the service
     *
     * @param wsdl The WSDL namespace
     * @param tns The target namespace
     * @param wsdlx The WSDL extensions namespace
     * @param fac The active OMFactory
     * @param interfaceName The name of the interface
     * @return The generated interface element
     * @throws FactoryConfigurationError 
     * @throws XMLStreamException 
     */
    private OMElement getInterfaceElement(OMNamespace wsdl, OMNamespace tns, OMNamespace wsdlx,
                                          OMNamespace wrpc, OMFactory fac, String interfaceName)
            throws URISyntaxException, AxisFault, XMLStreamException, FactoryConfigurationError {

        OMElement interfaceElement = fac.createOMElement(WSDL2Constants.INTERFACE_LOCAL_NAME, wsdl);
        interfaceElement.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null,
                                                            interfaceName));
        
        addPolicyAsExtensibleElement(axisService, interfaceElement);
        
        Iterator iterator = axisService.getOperations();
        ArrayList interfaceOperations = new ArrayList();
        ArrayList interfaceFaults = new ArrayList();
        int i = 0;
        while (iterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            interfaceOperations.add(i, generateInterfaceOperationElement(axisOperation, wsdl, tns,
                                                                         wsdlx, wrpc));
            i++;
            Iterator faultsIterator = axisOperation.getFaultMessages().iterator();
            while (faultsIterator.hasNext()) {
                AxisMessage faultMessage = (AxisMessage) faultsIterator.next();
                String name = faultMessage.getName();
                if (!interfaceFaults.contains(name)) {
                    OMElement faultElement =
                            fac.createOMElement(WSDL2Constants.FAULT_LOCAL_NAME, wsdl);
                    faultElement.addAttribute(
                            fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, name));
                    faultElement.addAttribute(fac.createOMAttribute(
                            WSDL2Constants.ATTRIBUTE_ELEMENT, null, WSDLSerializationUtil
                            .getElementName(faultMessage, axisService.getNamespaceMap())));
                    interfaceFaults.add(name);
                    interfaceElement.addChild(faultElement);
                }
            }
                     
        }
        for (i = 0; i < interfaceOperations.size(); i++) {
            interfaceElement.addChild((OMNode) interfaceOperations.get(i));
        }
        return interfaceElement;
    }

    /**
     * Generates the service element for the service
     *
     * @param wsdl the WSDL namespace
     * @param tns the target namespace
     * @param omFactory the active OMFactory
     * @param interfaceName the name of the interface
     * @return the generated service element
     */
    private OMElement getServiceElement(OMNamespace wsdl, OMNamespace tns, OMFactory omFactory,
                                        String interfaceName) {
        OMElement serviceElement =
                omFactory.createOMElement(WSDL2Constants.SERVICE_LOCAL_NAME, wsdl);
        serviceElement.addAttribute(
                omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null,
                                            serviceName));
        serviceElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME,
                                                                null, tns.getPrefix() + ":" +
                interfaceName));
        return serviceElement;
    }

    /**
     * Generates the interface Operation element. As with the binding operations we dont need to
     * ask AxisMessage to serialize its message cause AxisMessage does not have specific properties
     * as bindings.
     *
     * @param axisOperation the operation to write
     * @param wsdl the WSDL namespace
     * @param tns the target namespace
     * @param wsdlx the WSDL extentions namespace (WSDL 2.0)
     * @return the generated &lt;operation&gt; element
     * @throws FactoryConfigurationError 
     * @throws XMLStreamException 
     */
    public OMElement generateInterfaceOperationElement(AxisOperation axisOperation,
                                                       OMNamespace wsdl,
                                                       OMNamespace tns,
                                                       OMNamespace wsdlx,
                                                       OMNamespace wrpc) throws
            URISyntaxException, AxisFault, XMLStreamException, FactoryConfigurationError {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement axisOperationElement =
                omFactory.createOMElement(WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
        WSDLSerializationUtil
                .addWSDLDocumentationElement(axisOperation, axisOperationElement, omFactory, wsdl);
        axisOperationElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME,
                                                                      null,
                                                                      axisOperation
                                                                              .getName().getLocalPart()));
        addPolicyAsExtensibleElement(axisOperation, axisOperationElement);
        
        URI[] opStyle = (URI[]) axisOperation.getParameterValue(WSDL2Constants.OPERATION_STYLE);
        if (opStyle == null) {
            opStyle = checkStyle(axisOperation);
            Parameter opStyleParameter = new Parameter();
            opStyleParameter.setName(WSDL2Constants.OPERATION_STYLE);
            opStyleParameter.setValue(opStyle);
            axisOperation.addParameter(opStyleParameter);
        }
        if (opStyle != null && opStyle.length > 0) {
            String style = opStyle[0].toString();
            for (int i = 1; i < opStyle.length; i++) {
                URI uri = opStyle[i];
                style = style + " " + uri;
            }
            axisOperationElement.addAttribute(
                    omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_STYLE, null, style));
            if (style.indexOf(WSDL2Constants.STYLE_RPC) >= 0) {
                axisOperationElement.addAttribute(
                    omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_SIGNATURE, wrpc,
                                                (String) axisOperation.getParameterValue(
                                                        WSDL2Constants.ATTR_WRPC_SIGNATURE)));
            }
        }
        axisOperationElement.addAttribute(omFactory.createOMAttribute(
                WSDL2Constants.ATTRIBUTE_NAME_PATTERN, null, axisOperation.getMessageExchangePattern()));
        Parameter param = axisOperation.getParameter(WSDL2Constants.ATTR_WSDLX_SAFE);
        if (param != null) {
            axisOperationElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_SAFE, wsdlx, (param.getValue()).toString()));
        }
        AxisService axisService = axisOperation.getAxisService();
        Map nameSpaceMap = axisService.getNamespaceMap();

        // Add the input element
        AxisMessage inMessage = (AxisMessage) axisOperation.getChild(WSDLConstants.WSDL_MESSAGE_IN_MESSAGE);
        if (inMessage != null) {
            OMElement inMessageElement = omFactory.createOMElement(WSDL2Constants.IN_PUT_LOCAL_NAME, wsdl);
            inMessageElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_ELEMENT, null,
                    WSDLSerializationUtil.getElementName(inMessage, nameSpaceMap)));
            WSDLSerializationUtil.addWSAWActionAttribute(inMessageElement, axisOperation.getInputAction(),wsaw);
            WSDLSerializationUtil.addWSDLDocumentationElement(inMessage, inMessageElement, omFactory, wsdl);
            axisOperationElement.addChild(inMessageElement);
        }

        // Add the output element
        AxisMessage outMessage = (AxisMessage) axisOperation.getChild(WSDLConstants.WSDL_MESSAGE_OUT_MESSAGE);
        if (outMessage != null) {
            OMElement outMessageElement = omFactory.createOMElement(WSDL2Constants.OUT_PUT_LOCAL_NAME, wsdl);
            outMessageElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_ELEMENT, null,
                    WSDLSerializationUtil.getElementName(outMessage, nameSpaceMap)));
            WSDLSerializationUtil.addWSAWActionAttribute(outMessageElement, axisOperation.getOutputAction(),wsaw);
            WSDLSerializationUtil.addWSDLDocumentationElement(outMessage, outMessageElement, omFactory, wsdl);
            axisOperationElement.addChild(outMessageElement);
        }

        // Add the fault element
        ArrayList faults = axisOperation.getFaultMessages();
        if (faults != null) {
            Iterator iterator = faults.iterator();
            while (iterator.hasNext()) {
                AxisMessage faultMessage = (AxisMessage) iterator.next();
                OMElement faultElement;
                if (WSDLConstants.WSDL_MESSAGE_DIRECTION_IN.equals(faultMessage.getDirection())) {
                    faultElement = omFactory.createOMElement(WSDL2Constants.IN_FAULT_LOCAL_NAME, wsdl);
                } else {
                    faultElement = omFactory.createOMElement(WSDL2Constants.OUT_FAULT_LOCAL_NAME, wsdl);
                }
                faultElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_REF,
                                                                      null, tns.getPrefix() + ":" +
                        faultMessage.getName()));
                WSDLSerializationUtil.addWSAWActionAttribute(faultElement,
                                                             axisOperation.getFaultAction(
                                                                     faultMessage.getName()), wsaw);
                WSDLSerializationUtil
                        .addWSDLDocumentationElement(faultMessage, faultElement, omFactory, wsdl);
                axisOperationElement.addChild(faultElement);
            }
        }
        return axisOperationElement;
    }

    public void setEPRs(String[] eprs) {
        this.eprs = eprs;
    }

    /**
     * This function checks the schema and returns the WSDL 2.0 styles that it conform to.
     * It checks for RPC, IRI and Multipart styles.
     * For full details on the rules please refer http://www.w3.org/TR/2007/REC-wsdl20-adjuncts-20070626/#styles
     * @param axisOperation - The axisOperation that needs to be checked
     * @return String [] - An array of styles that the operation adheres to.
     */
    private URI [] checkStyle(AxisOperation axisOperation) throws URISyntaxException, AxisFault {
        boolean isRPC = true;
        boolean isMultipart = true;
        boolean isIRI = true;
        ArrayList styles = new ArrayList(3);

        String mep = axisOperation.getMessageExchangePattern();
        if (!(WSDL2Constants.MEP_URI_IN_ONLY.equals(mep) ||
                WSDL2Constants.MEP_URI_IN_OUT.equals(mep))) {
            isRPC = false;
        }

        QName inMessageElementQname;
        Map inMessageElementDetails = new LinkedHashMap();
        AxisMessage inMessage = axisOperation.getMessage(WSDL2Constants.MESSAGE_LABEL_IN);
        if (inMessage != null) {
            QName qName = inMessage.getElementQName();
            if (qName == null || Constants.XSD_ANY.equals(qName)) {
                return new URI [0];
            }
            XmlSchemaElement schemaElement = inMessage.getSchemaElement();
            if (schemaElement != null) {
                if (!axisOperation.getName().getLocalPart().equals(schemaElement.getName())) {
                    return new URI [0];
                }
                inMessageElementQname = schemaElement.getQName();
                XmlSchemaType type = schemaElement.getSchemaType();
                if (type != null && type instanceof XmlSchemaComplexType){
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                    XmlSchemaParticle particle = complexType.getParticle();
                    if (particle != null && particle instanceof XmlSchemaSequence){
                        XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
                        XmlSchemaObjectCollection schemaObjectCollection =
                                xmlSchemaSequence.getItems();
                        if (schemaObjectCollection != null) {
                            Iterator iterator = schemaObjectCollection.getIterator();
                            while (iterator.hasNext()) {
                                Object next = iterator.next();
                                if (!(next instanceof XmlSchemaElement)) {
                                    return new URI [0];
                                }
                                XmlSchemaElement innerElement = (XmlSchemaElement) next;
                                if (innerElement.getRefName() != null) {
                                    return new URI [0];
                                }
                                if (innerElement.getMinOccurs() != 1 ||
                                        innerElement.getMaxOccurs() != 1) {
                                    isMultipart = false;
                                }
                                XmlSchemaType schemaType = innerElement.getSchemaType();
                                QName innerElementQName = innerElement.getSchemaTypeName();
                                if (schemaType instanceof XmlSchemaSimpleType) {
                                    if (Constants.XSD_QNAME.equals(innerElementQName) ||
                                            Constants.XSD_NOTATION.equals(innerElementQName) ||
                                            Constants.XSD_HEXBIN.equals(innerElementQName) ||
                                            Constants.XSD_BASE64.equals(innerElementQName)) {
                                            isIRI = false;
                                    }
                                } else {
                                    isIRI = false;
                                }
                                if (Constants.XSD_ANY.equals(innerElementQName) && iterator.hasNext()) {
                                    isRPC = false;
                                }
                                String name = innerElement.getName();
                                if (inMessageElementDetails.get(name) != null) {
                                    isRPC = false;
                                    isMultipart = false;
                                }
                                inMessageElementDetails.put(name, innerElementQName);
                            }
                        }
                    } else {
                        return new URI [0];
                    }
                } else {
                        return new URI [0];
                    }
            } else {
                return new URI [0];
            }
        } else {
            return new URI [0];
        }
        AxisMessage outMessage = null;
        Map outMessageElementDetails = new LinkedHashMap();
        if (isRPC && !WSDL2Constants.MEP_URI_IN_ONLY.equals(mep)) {
            outMessage = axisOperation.getMessage(WSDL2Constants.MESSAGE_LABEL_OUT);
            QName qName = outMessage.getElementQName();
            if (qName == null && Constants.XSD_ANY.equals(qName)) {
                isRPC = false;
            }
            XmlSchemaElement schemaElement = outMessage.getSchemaElement();
            if (schemaElement != null) {
                if (!(axisOperation.getName().getLocalPart() + Java2WSDLConstants.RESPONSE)
                        .equals(schemaElement.getName())) {
                    isRPC = false;
                }
                if (!schemaElement.getQName().getNamespaceURI()
                        .equals(inMessageElementQname.getNamespaceURI())) {
                    isRPC = false;
                }
                XmlSchemaType type = schemaElement.getSchemaType();
                if (type != null && type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                    XmlSchemaParticle particle = complexType.getParticle();
                    if (particle != null && particle instanceof XmlSchemaSequence) {
                        XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
                        XmlSchemaObjectCollection schemaObjectCollection =
                                xmlSchemaSequence.getItems();
                        if (schemaObjectCollection != null) {
                            Iterator iterator = schemaObjectCollection.getIterator();
                            while (iterator.hasNext()) {
                                Object next = iterator.next();
                                if (!(next instanceof XmlSchemaElement)) {
                                    isRPC = false;
                                } else {
                                	XmlSchemaElement innerElement = (XmlSchemaElement) next;
                                	QName schemaTypeName = innerElement.getSchemaTypeName();
                                	String name = innerElement.getName();
                                
	                                if (innerElement.getRefName() != null) {
	                                    isRPC = false;
	                                }
	                                if (outMessageElementDetails.get(name) != null) {
	                                    isRPC = false;
	                                }
	                                QName inMessageElementType =
	                                        (QName) inMessageElementDetails.get(name);
	                                if (inMessageElementType != null &&
	                                        inMessageElementType != schemaTypeName) {
	                                    isRPC = false;
	                                }
	                                outMessageElementDetails.put(name, schemaTypeName);
                                }
                            }
                        }
                    } else {
                        isRPC = false;
                    }
                } else {
                    isRPC = false;
                }
            } else {
                isRPC = false;
            }
        }
        int count = 0;
        if (isRPC) {
            styles.add(new URI(WSDL2Constants.STYLE_RPC));
            axisOperation.addParameter(WSDL2Constants.ATTR_WRPC_SIGNATURE, generateRPCSignature(
                    inMessageElementDetails, outMessageElementDetails));
            count ++;
        }
        if (isIRI) {
            styles.add(new URI(WSDL2Constants.STYLE_IRI));
            count ++;
        }
        if (isMultipart) {
            styles.add(new URI(WSDL2Constants.STYLE_MULTIPART));
            count ++;
        }
        return (URI[]) styles.toArray(new URI[count]);
    }

    private String generateRPCSignature(Map inMessageElementDetails, Map outMessageElementDetails) {
        String in = "";
        String out = "";
        String inOut = "";
        Set inElementSet = inMessageElementDetails.keySet();
        Set outElementSet = outMessageElementDetails.keySet();

        Iterator inElementIterator = inElementSet.iterator();
        while (inElementIterator.hasNext()) {
            String inElementName = (String) inElementIterator.next();
            if (outElementSet.contains(inElementName)) {
                inOut = inOut + inElementName + " " + WSDL2Constants.RPC_INOUT + " ";
                outElementSet.remove(inElementName);
            } else {
                in = in + inElementName + " " + WSDL2Constants.RPC_IN + " ";
            }
        }
        Iterator outElementIterator = outElementSet.iterator();
        while (outElementIterator.hasNext()) {
            String outElementName = (String) outElementIterator.next();
            out = out + outElementName + " " + WSDL2Constants.RPC_RETURN + " ";
        }
        return in + out + inOut;
    }
    
	private void addPolicyAsExtensibleElement(AxisDescription axisDescription,
			OMElement descriptionElement) throws XMLStreamException,
			FactoryConfigurationError, AxisFault {
		PolicySubject policySubject = axisDescription.getPolicySubject();
		Collection attachPolicyComponents = policySubject
				.getAttachedPolicyComponents();

		for (Iterator iterator = attachPolicyComponents.iterator(); iterator
				.hasNext();) {
			Object policyElement = iterator.next();

			if (policyElement instanceof Policy) {
				PolicyReference policyReference = PolicyUtil
						.createPolicyReference((Policy) policyElement);
				OMElement policyRefElement = PolicyUtil
						.getPolicyComponentAsOMElement(
								(PolicyComponent) policyReference, filter);

				OMNode firstChildElem = descriptionElement.getFirstElement();
				if (firstChildElem == null) {
					descriptionElement.addChild(policyRefElement);
				} else {
					firstChildElem.insertSiblingBefore(policyRefElement);
				}
				String key = ((PolicyReference) policyReference).getURI();
				if (key.startsWith("#")) {
					key = key.substring(key.indexOf("#") + 1);
				}
				policiesInDescription.put(key, (Policy) policyElement);

			} else if (policyElement instanceof PolicyReference) {
				OMElement child = PolicyUtil
						.getPolicyComponentAsOMElement((PolicyComponent) policyElement);
				OMElement firstChildElem = descriptionElement.getFirstElement();

				if (firstChildElem == null) {
					descriptionElement.addChild(child);
				} else {
					firstChildElem.insertSiblingBefore(child);
				}

				String key = ((PolicyReference) policyElement).getURI();
				if (key.startsWith("#")) {
					key = key.substring(key.indexOf("#") + 1);
				}

				PolicyLocator locator = new PolicyLocator(axisService);
				Policy p = locator.lookup(key);

				if (p == null) {
					throw new AxisFault("Policy not found for uri : " + key);
				}
				policiesInDescription.put(key, p);
			}
		}
	}
	
	private void addPoliciesToDescriptionElement(List policies,
			OMElement descriptionElement) throws XMLStreamException,
			FactoryConfigurationError {

		for (int i = 0; i < policies.size(); i++) {
			Policy policy = (Policy) policies.get(i);
			OMElement policyElement = PolicyUtil.getPolicyComponentAsOMElement(
					policy, filter);
			OMNode firstChild = descriptionElement.getFirstOMChild();
			if (firstChild != null) {
				firstChild.insertSiblingBefore(policyElement);
			} else {
				descriptionElement.addChild(policyElement);
			}
		}
	}
}