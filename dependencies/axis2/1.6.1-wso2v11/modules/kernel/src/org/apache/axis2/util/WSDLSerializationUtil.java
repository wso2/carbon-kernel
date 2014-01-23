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

package org.apache.axis2.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicySubject;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.wsdl.HTTPHeaderMessage;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.SOAPModuleMessage;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyReference;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Helps the AxisService to WSDL process
 */
public class WSDLSerializationUtil {

    public static final String CDATA_START = "<![CDATA[";
    public static final String CDATA_START_REGEX = "<!\\[CDATA\\[";
    public static final String CDATA_END = "]]>";
    public static final String CDATA_END_REGEX = "\\]\\]>";

    /**
     * Given a namespace it returns the prefix for that namespace
     * @param namespace - The namespace that the prefix is needed for
     * @param nameSpaceMap - The namespaceMap
     * @return - The prefix of the namespace
     */
    public static String getPrefix(String namespace, Map<String, String> nameSpaceMap) {
        Set<String> keySet;
        if (nameSpaceMap != null && (keySet = nameSpaceMap.keySet()) != null) {
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (nameSpaceMap.get(key).equals(namespace)) {
                    return key;
                }
            }
        }
        return null;
    }

    /**
     * Gets the correct element name for a given message
     * @param axisMessage - The axisMessage
     * @param nameSpaceMap - The namespaceMap
     * @return - The element name
     */
    public static String getElementName(AxisMessage axisMessage, Map nameSpaceMap) {
        QName elementQName = axisMessage.getElementQName();
        if (elementQName == null) {
            return WSDL2Constants.NMTOKEN_NONE;
        } else if (Constants.XSD_ANY.equals(elementQName)) {
            return WSDL2Constants.NMTOKEN_ANY;
        } else {
            String prefix =
                    WSDLSerializationUtil.getPrefix(elementQName.getNamespaceURI(), nameSpaceMap);
            return prefix + ":" + elementQName.getLocalPart();
        }
    }

    /**
     * Adds a soap header element to a given OMElement
     * @param omFactory - An OMFactory
     * @param list - The arraylist of soapHeaderMessages
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param element - The element that the header should be added to
     * @param nameSpaceMap - The namespaceMap
     */
    public static void addSOAPHeaderElements(OMFactory omFactory, ArrayList list, OMNamespace wsoap,
                                             OMElement element, Map nameSpaceMap) {
        for (int i = 0; i < list.size(); i++) {
            SOAPHeaderMessage soapHeaderMessage = (SOAPHeaderMessage) list.get(i);
            OMElement soapHeaderElement =
                    omFactory.createOMElement(WSDL2Constants.ATTRIBUTE_HEADER, wsoap);
            QName qName = soapHeaderMessage.getElement();
            soapHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_ELEMENT, null,
                    getPrefix(qName.getNamespaceURI(), nameSpaceMap) + ":" + qName.getLocalPart()));
            soapHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_MUST_UNDERSTAND, null,
                    Boolean.toString(soapHeaderMessage.isMustUnderstand())));
            soapHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_REQUIRED, null,
                    Boolean.toString(soapHeaderMessage.isRequired())));
            element.addChild(soapHeaderElement);
        }
    }

    /**
     * Adds a soap module element to a given OMElement
     * @param omFactory - An OMFactory
     * @param list - The arraylist of soapModuleMessages
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param element - The element that the header should be added to
     */
    public static void addSOAPModuleElements(OMFactory omFactory, ArrayList list, OMNamespace wsoap,
                                             OMElement element) {
        for (int i = 0; i < list.size(); i++) {
            SOAPModuleMessage soapModuleMessage = (SOAPModuleMessage) list.get(i);
            OMElement soapModuleElement =
                    omFactory.createOMElement(WSDL2Constants.ATTRIBUTE_MODULE, wsoap);
            soapModuleElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_REF, null, soapModuleMessage.getUri()));
            element.addChild(soapModuleElement);
        }
    }

    /**
     * Adds a HTTP header element to a given OMElement
     * @param omFactory - An OMFactory
     * @param list - The arraylist of HTTPHeaderMessages
     * @param whttp - The WSDL 2.0 HTTP namespace
     * @param element - The element that the header should be added to
     * @param nameSpaceMap - The namespaceMap
     */
    public static void addHTTPHeaderElements(OMFactory omFactory, ArrayList list, OMNamespace whttp,
                                             OMElement element, Map nameSpaceMap) {
        for (int i = 0; i < list.size(); i++) {
            HTTPHeaderMessage httpHeaderMessage = (HTTPHeaderMessage) list.get(i);
            OMElement httpHeaderElement =
                    omFactory.createOMElement(WSDL2Constants.ATTRIBUTE_HEADER, whttp);
            httpHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_NAME, null, httpHeaderMessage.getName()));
            QName qName = httpHeaderMessage.getqName();
            httpHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_TYPE, null,
                    getPrefix(qName.getNamespaceURI(), nameSpaceMap) + ":" + qName.getLocalPart()));
            httpHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_REQUIRED, null,
                    Boolean.valueOf(httpHeaderMessage.isRequired()).toString()));
            element.addChild(httpHeaderElement);
        }
    }

    /**
     * Generates a default SOAP 11 Binding for a given AxisService
     * @param fac - The OMFactory
     * @param axisService - The AxisService
     * @param wsdl the WSDL namespace
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param tns - The target namespace
     * @return - The generated SOAP11Binding element
     */
    public static OMElement generateSOAP11Binding(OMFactory fac, AxisService axisService,
                                                  OMNamespace wsdl, OMNamespace wsoap,
                                                  OMNamespace tns, String serviceName) {
        OMElement binding = fac.createOMElement(WSDL2Constants.BINDING_LOCAL_NAME, wsdl);
        binding.addAttribute(
                fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, serviceName +
                        Java2WSDLConstants.BINDING_NAME_SUFFIX));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME, null, tns
                .getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));

        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_TYPE, null,
                                                   WSDL2Constants.URI_WSDL2_SOAP));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_VERSION, wsoap,
                                                   WSDL2Constants.SOAP_VERSION_1_1));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_PROTOCOL, wsoap,
                                                           WSDL2Constants.HTTP_PROTOCAL));
        generateDefaultSOAPBindingOperations(axisService, fac, binding, wsdl, tns, wsoap);
        return binding;
    }

    /**
     * Generates a default SOAP 12 Binding for a given AxisService
     * @param fac - The OMFactory
     * @param axisService - The AxisService
     * @param wsdl the WSDL namespace
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param tns - The target namespace
     * @return - The generated SOAP12Binding element
     */
    public static OMElement generateSOAP12Binding(OMFactory fac, AxisService axisService,
                                                  OMNamespace wsdl, OMNamespace wsoap,
                                                  OMNamespace tns, String serviceName) {
        OMElement binding = fac.createOMElement(WSDL2Constants.BINDING_LOCAL_NAME, wsdl);
        binding.addAttribute(
                fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, serviceName +
                        Java2WSDLConstants.SOAP12BINDING_NAME_SUFFIX));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME, null, tns
                .getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));

        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_TYPE, null,
                                                   WSDL2Constants.URI_WSDL2_SOAP));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_VERSION, wsoap,
                                                   WSDL2Constants.SOAP_VERSION_1_2));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_PROTOCOL, wsoap,
                                                           WSDL2Constants.HTTP_PROTOCAL));
        generateDefaultSOAPBindingOperations(axisService, fac, binding, wsdl, tns, wsoap);
        return binding;
    }

    /**
     * Generates a default HTTP Binding for a given AxisService
     * @param fac - The OMFactory
     * @param axisService - The AxisService
     * @param wsdl the WSDL namespace
     * @param whttp - The WSDL 2.0 HTTP namespace
     * @param tns - The target namespace
     * @return - The generated HTTPBinding element
     */
    public static OMElement generateHTTPBinding(OMFactory fac, AxisService axisService,
                                                OMNamespace wsdl, OMNamespace whttp,
                                                OMNamespace tns, String serviceName) {
        OMElement binding = fac.createOMElement(WSDL2Constants.BINDING_LOCAL_NAME, wsdl);
        binding.addAttribute(
                fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, serviceName +
                        Java2WSDLConstants.HTTP_BINDING));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME, null, tns
                .getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));

        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_TYPE, null,
                                                   WSDL2Constants.URI_WSDL2_HTTP));
        Iterator iterator = axisService.getChildren();
        while (iterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            OMElement opElement = fac.createOMElement(WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(opElement);
            String name = axisOperation.getName().getLocalPart();
            opElement.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_REF, null,
                                                         tns.getPrefix() + ":" + name));
            opElement.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_LOCATION, whttp,
                                                         name));
        }
        return binding;
    }

    private static void generateDefaultSOAPBindingOperations(AxisService axisService,
                                                             OMFactory omFactory, OMElement binding,
                                                             OMNamespace wsdl, OMNamespace tns,
                                                             OMNamespace wsoap) {
        Iterator iterator = axisService.getChildren();
        while (iterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            OMElement opElement = omFactory.createOMElement(WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(opElement);
            String name = axisOperation.getName().getLocalPart();
            opElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_REF, null,
                                                         tns.getPrefix() + ":" + name));
            String soapAction = axisOperation.getSoapAction();
            if (soapAction != null) {
                opElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ACTION, wsoap,
                        soapAction));
            }
        }
    }

    /**
     * Generates a default service element
     * @param omFactory - The OMFactory
     * @param wsdl the WSDL namespace
     * @param tns - The targetnamespace
     * @param axisService - The AxisService
     * @param disableREST only generate REST endpoint if this is false
     * @param disableSOAP12 only generate SOAP 1.2 endpoint if this is false
     * @return - The generated service element
     * @throws AxisFault - Thrown in case an exception occurs
     */
    public static OMElement generateServiceElement(OMFactory omFactory, OMNamespace wsdl,
                                                   OMNamespace tns, AxisService axisService,
                                                   boolean disableREST, boolean disableSOAP12,  boolean disableSOAP11,
                                                   String serviceName)
            throws AxisFault {
        return generateServiceElement(omFactory, wsdl, tns, axisService, disableREST, disableSOAP12,disableSOAP11,
                                      null, serviceName);
    }
    
    /**
     * Generates a default service element
     * @param omFactory - The OMFactory
     * @param wsdl the WSDL namespace
     * @param tns - The targetnamespace
     * @param axisService - The AxisService
     * @param disableREST only generate REST endpoint if this is false
     * @param disableSOAP12 only generate SOAP 1.2 endpoint if this is false
     * @return - The generated service element
     * @throws AxisFault - Thrown in case an exception occurs
     */
    public static OMElement generateServiceElement(OMFactory omFactory, OMNamespace wsdl,
                                                   OMNamespace tns, AxisService axisService,
                                                   boolean disableREST, boolean disableSOAP12, boolean disableSOAP11,
                                                   String[] eprs, String serviceName)
            throws AxisFault {
        if(eprs == null){
            eprs = axisService.getEPRs();
            if (eprs == null) {
                eprs = new String[]{serviceName};
            }
        }
        OMElement serviceElement;
        serviceElement = omFactory.createOMElement(WSDL2Constants.SERVICE_LOCAL_NAME, wsdl);
                    serviceElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME,
                                                                            null, serviceName));
                    serviceElement.addAttribute(omFactory.createOMAttribute(
                            WSDL2Constants.INTERFACE_LOCAL_NAME, null,
                            tns.getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));
        for (int i = 0; i < eprs.length; i++) {
            String name = "";
            String epr = eprs[i];
            if (epr.startsWith("local://")) {
                continue;
            }
            if (epr.startsWith("https://")) {
                name = WSDL2Constants.DEFAULT_HTTPS_PREFIX;
            }

            OMElement soap11EndpointElement =
                    null;
            if (!disableSOAP11) {
                soap11EndpointElement = omFactory.createOMElement(WSDL2Constants.ENDPOINT_LOCAL_NAME, wsdl);
                soap11EndpointElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_NAME, null,
                        name + WSDL2Constants.DEFAULT_SOAP11_ENDPOINT_NAME));
                soap11EndpointElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.BINDING_LOCAL_NAME, null,
                        tns.getPrefix() + ":" + serviceName +
                                Java2WSDLConstants.BINDING_NAME_SUFFIX));
                soap11EndpointElement.addAttribute(
                    omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ADDRESS, null, epr));
                serviceElement.addChild(soap11EndpointElement);
            }

            OMElement soap12EndpointElement = null;
            if (!disableSOAP12) {
                soap12EndpointElement =
                        omFactory.createOMElement(WSDL2Constants.ENDPOINT_LOCAL_NAME, wsdl);
                soap12EndpointElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_NAME, null,
                        name + WSDL2Constants.DEFAULT_SOAP12_ENDPOINT_NAME));
                soap12EndpointElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.BINDING_LOCAL_NAME, null,
                        tns.getPrefix() + ":" + serviceName +
                                Java2WSDLConstants.SOAP12BINDING_NAME_SUFFIX));
                soap12EndpointElement.addAttribute(
                        omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ADDRESS, null, epr));
                serviceElement.addChild(soap12EndpointElement);
            }
            
            OMElement httpEndpointElement = null;
            if (!disableREST) {
                httpEndpointElement =
                        omFactory.createOMElement(WSDL2Constants.ENDPOINT_LOCAL_NAME, wsdl);
                httpEndpointElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_NAME, null,
                        name + WSDL2Constants.DEFAULT_HTTP_ENDPOINT_NAME));
                httpEndpointElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.BINDING_LOCAL_NAME, null,
                        tns.getPrefix() + ":" + serviceName + Java2WSDLConstants
                                .HTTP_BINDING));
                httpEndpointElement.addAttribute(
                        omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ADDRESS, null, epr));
                serviceElement.addChild(httpEndpointElement);
            }
            
            if (epr.startsWith("https://")) {
                if (!disableSOAP11) {
                    OMElement soap11Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    soap11Documentation.setText("This endpoint exposes a SOAP 11 binding over a HTTPS");
                    soap11EndpointElement.addChild(soap11Documentation);
                }
                if (!disableSOAP12) {
                    OMElement soap12Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    soap12Documentation.setText("This endpoint exposes a SOAP 12 binding over a HTTPS");
                    soap12EndpointElement.addChild(soap12Documentation);
                }
                if (!disableREST) {
                    OMElement httpDocumentation =
                            omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    httpDocumentation.setText("This endpoint exposes a HTTP binding over a HTTPS");
                    httpEndpointElement.addChild(httpDocumentation);
                }
            } else if (epr.startsWith("http://")) {
                if (!disableSOAP11) {
                    OMElement soap11Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    soap11Documentation.setText("This endpoint exposes a SOAP 11 binding over a HTTP");
                    soap11EndpointElement.addChild(soap11Documentation);
                }
                if (!disableSOAP12) {
                    OMElement soap12Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    soap12Documentation.setText("This endpoint exposes a SOAP 12 binding over a HTTP");
                    soap12EndpointElement.addChild(soap12Documentation);
                }
                if (!disableREST) {
                    OMElement httpDocumentation =
                            omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    httpDocumentation.setText("This endpoint exposes a HTTP binding over a HTTP");
                    httpEndpointElement.addChild(httpDocumentation);
                }
            }
        }
        return serviceElement;
    }

    /**
     * Adds the namespaces to the given OMElement
     *
     * @param descriptionElement - The OMElement that the namespaces should be added to
     * @param nameSpaceMap - The namespaceMap
     */
    public static void populateNamespaces(OMElement descriptionElement, Map nameSpaceMap) {
        if (nameSpaceMap != null) {
        Iterator keys = nameSpaceMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(key)) {
                descriptionElement.declareDefaultNamespace((String) nameSpaceMap.get(key));
            } else {
                descriptionElement.declareNamespace((String) nameSpaceMap.get(key), key);
            }
            }
        }
    }

    public static void addWSAWActionAttribute(OMElement element,
                                              String action ,
                                              OMNamespace wsaw) {
        if (action == null || action.length() == 0 || "\\\"\\\"".equals(action)) {
            return;
        }
        element.addAttribute("Action", action, wsaw);
    }

    public static void addExtensionElement(OMFactory fac, OMElement element,
                                     String name, String att1Name, String att1Value,
                                     OMNamespace soapNameSpace) {
        OMElement extElement = fac.createOMElement(name, soapNameSpace);
        element.addChild(extElement);
        extElement.addAttribute(att1Name, att1Value, null);
    }

    public static void addWSAddressingToBinding(String addressingFlag,
                                                OMFactory omFactory,
                                                OMElement bindingElement ,
                                                OMNamespace wsaw) {
        // Add WS-Addressing UsingAddressing element if appropriate
        // SHOULD be on the binding element per the specification
        if (addressingFlag.equals(AddressingConstants.ADDRESSING_OPTIONAL)) {
            WSDLSerializationUtil.addExtensionElement(omFactory, bindingElement,
                                AddressingConstants.USING_ADDRESSING,
                                "required", "true",
                                wsaw);
        } else if (addressingFlag.equals(AddressingConstants.ADDRESSING_REQUIRED)) {
            WSDLSerializationUtil.addExtensionElement(omFactory, bindingElement,
                                AddressingConstants.USING_ADDRESSING,
                                "required", "true",
                                wsaw);
        }
    }

    public static void addWSDLDocumentationElement(AxisDescription axisDescription,
                                                   OMElement omElement, OMFactory omFactory,
                                                   OMNamespace wsdl) {
        OMNode documentationNode = axisDescription.getDocumentationNode();
        OMElement documentation;
        if (documentationNode != null) {
            documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
            documentation.addChild(documentationNode);
            omElement.addChild(documentation);
        }
    }
    
    public static void addPoliciesAsExtensibleElement(
			AxisDescription description, OMElement descriptionElement) {
		PolicySubject policySubject = description.getPolicySubject();
		Collection attachPolicyComponents = policySubject
				.getAttachedPolicyComponents();
		ArrayList policies = new ArrayList();

		for (Iterator iterator = attachPolicyComponents.iterator(); iterator
				.hasNext();) {
			Object policyElement = iterator.next();

			if (policyElement instanceof Policy) {
				policies.add(policyElement);

			} else if (policyElement instanceof PolicyReference) {
				String key = ((PolicyReference) policyElement).getURI();

				if (key.startsWith("#")) {
					key = key.substring(key.indexOf("#") + 1);
				}
                AxisService service = getAxisService(description);
				PolicyLocator locator = new PolicyLocator(service);
				Policy p = locator.lookup(key);

				if (p != null) {
                    policies.add(p);
//                    throw new RuntimeException("Policy not found for uri : "
//							+ key);
                }
			}
		}

		ExternalPolicySerializer filter = null;
		if (!policies.isEmpty()) {
			filter = new ExternalPolicySerializer();
			AxisConfiguration axisConfiguration = description
					.getAxisConfiguration();
			if (axisConfiguration != null) {
				filter.setAssertionsToFilter(axisConfiguration
						.getLocalPolicyAssertions());
			}
		}

		for (Iterator iterator = policies.iterator(); iterator.hasNext();) {
			Policy policy = (Policy) iterator.next();
			OMElement policyElement;
			try {
				policyElement = PolicyUtil.getPolicyComponentAsOMElement(
						policy, filter);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			OMNode firstChild = descriptionElement.getFirstOMChild();
			if (firstChild != null) {
				firstChild.insertSiblingBefore(policyElement);
			} else {
				descriptionElement.addChild(policyElement);
			}
		}
	}

	private static AxisService getAxisService(AxisDescription description) {
		if (description == null || description instanceof AxisService) {
			return (AxisService) description;
		} else {
			return getAxisService(description.getParent());
		}
	}                              
        
	public static String extractHostIP(String serviceURL){
            
            String ip = null;
            
            if (serviceURL != null) {
            
                int ipindex = serviceURL.indexOf("//");
                        
                if (ipindex >= 0) {
                    ip = serviceURL.substring(ipindex + 2, serviceURL.length());
                    int seperatorIndex = ip.indexOf(":");
                    int slashIndex = ip.indexOf("/");
                        
                    if (seperatorIndex >= 0) {
                        ip = ip.substring(0, seperatorIndex);
                    } else {
                        ip = ip.substring(0, slashIndex);
                    }                      
                }
            }
               
            return ip;      
        }      

	
}
