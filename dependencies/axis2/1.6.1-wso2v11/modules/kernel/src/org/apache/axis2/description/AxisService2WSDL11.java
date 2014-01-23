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

import org.apache.axiom.om.*;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.*;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.PolicyRegistry;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class AxisService2WSDL11 implements Java2WSDLConstants {

	private AxisService axisService;

	private String serviceName;

    private String targetNamespace;

	private OMElement definition;

	private OMNamespace soap;

	private OMNamespace soap12;

	private OMNamespace http;

	private OMNamespace mime;

	private OMNamespace tns;

	private OMNamespace wsdl;

	private OMNamespace wsaw;

	private String style = DOCUMENT;

	private String use = LITERAL;

	private HashMap policiesInDefinitions;

	private ExternalPolicySerializer serializer;

	private HashMap messagesMap;

    public AxisService2WSDL11(AxisService service) throws Exception {
        this.axisService = service;
        this.serviceName = service.getName();
        init();
	}

	private void init() throws AxisFault {
/*
		// the EPR list of AxisService contains REST EPRs as well. Those REST
		// EPRs will be used to generated HTTPBinding
		// and rest of the EPRs will be used to generate SOAP 1.1 and 1.2
		// bindings. Let's first initialize those set of
		// EPRs now to be used later, especially when we generate the WSDL.
        String[] serviceEndpointURLs = axisService.getEPRs();
		if (serviceEndpointURLs == null) {
			Map endpointMap = axisService.getEndpoints();
			if (endpointMap.size() > 0) {
				Iterator endpointItr = endpointMap.values().iterator();
				if (endpointItr.hasNext()) {
					AxisEndpoint endpoint = (AxisEndpoint) endpointItr.next();
					serviceEndpointURLs = new String[] { endpoint
							.getEndpointURL() };
				}

			} else {
				serviceEndpointURLs = new String[] { axisService
						.getEndpointName() };
			}
		}
*/
		this.targetNamespace = axisService.getTargetNamespace();

		serializer = new ExternalPolicySerializer();
		// CHECKME check whether service.getAxisConfiguration() return null ???

		AxisConfiguration configuration = axisService.getAxisConfiguration();
		if (configuration != null) {
			serializer.setAssertionsToFilter(configuration
					.getLocalPolicyAssertions());
		}
	}

	public AxisService2WSDL11(AxisService service, String serviceName)
			throws Exception {
		this.axisService = service;
		this.serviceName = serviceName;
		init();
	}

    /**
     * Build the OM structure of the WSDL document
     *
     * @return an OMElement containing a WSDL document
     * @throws Exception
     */
    public OMElement generateOM() throws Exception {

		OMFactory fac = OMAbstractFactory.getOMFactory();
		wsdl = fac.createOMNamespace(WSDL_NAMESPACE, DEFAULT_WSDL_NAMESPACE_PREFIX);
		OMElement ele = fac.createOMElement("definitions", wsdl);
		setDefinitionElement(ele);

		policiesInDefinitions = new HashMap();

		Map namespaceMap = axisService.getNamespaceMap();
		if (namespaceMap == null)
			namespaceMap = new HashMap();

		WSDLSerializationUtil.populateNamespaces(ele, namespaceMap);
		soap = ele.declareNamespace(URI_WSDL11_SOAP, SOAP11_PREFIX);
		soap12 = ele.declareNamespace(URI_WSDL12_SOAP, SOAP12_PREFIX);
		http = ele.declareNamespace(HTTP_NAMESPACE, HTTP_PREFIX);
		mime = ele.declareNamespace(MIME_NAMESPACE, MIME_PREFIX);
		wsaw = ele.declareNamespace(AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
		String prefix = WSDLSerializationUtil.getPrefix(axisService.getTargetNamespace(),
                                                        namespaceMap);
		if (prefix == null || "".equals(prefix)) {
            if (axisService.getTargetNamespacePrefix() != null) {
                prefix = axisService.getTargetNamespacePrefix();
            } else {
                prefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
            }
		}

		namespaceMap.put(prefix, axisService.getTargetNamespace());
		tns = ele.declareNamespace(axisService.getTargetNamespace(), prefix);

		// axis2.xml indicated no HTTP binding?
		boolean disableREST = false;
		Parameter disableRESTParameter = axisService.getParameter(
                org.apache.axis2.Constants.Configuration.DISABLE_REST);
		if (disableRESTParameter != null
				&& JavaUtils.isTrueExplicitly(disableRESTParameter.getValue())) {
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
		Parameter disableSOAP12Parameter = axisService
				.getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP12);
		if (disableSOAP12Parameter != null
				&& JavaUtils.isTrueExplicitly(disableSOAP12Parameter.getValue())) {
			disableSOAP12 = true;
		}

		// adding documentation element
		// <documentation>&lt;b&gt;NEW!&lt;/b&gt; This method accepts an ISBN
		// string and returns &lt;b&gt;Amazon.co.uk&lt;/b&gt; Sales Rank for
		// that book.</documentation>
		WSDLSerializationUtil.addWSDLDocumentationElement(axisService, ele,	fac, wsdl);

		ele.addAttribute("targetNamespace", axisService.getTargetNamespace(), null);
		OMElement wsdlTypes = fac.createOMElement("types", wsdl);
		ele.addChild(wsdlTypes);

		// populate the schema mappings
		axisService.populateSchemaMappings();

		ArrayList schemas = axisService.getSchema();
		for (int i = 0; i < schemas.size(); i++) {
			StringWriter writer = new StringWriter();

			// XmlSchema schema = (XmlSchema) schemas.get(i);
			XmlSchema schema = axisService.getSchema(i);

			String targetNamespace = schema.getTargetNamespace();
			if (!Constants.NS_URI_XML.equals(targetNamespace)) {
				schema.write(writer);
				String schemaString = writer.toString();
				if (!"".equals(schemaString)) {
					wsdlTypes.addChild(XMLUtils.toOM(new StringReader(schemaString)));
				}
			}
		}
		generateMessages(fac, ele);
		generatePortType(fac, ele);

		// generateSOAP11Binding(fac, ele);
		// if (!disableSOAP12) {
		// generateSOAP12Binding(fac, ele);
		// }
		// if (!disableREST) {
		// generateHTTPBinding(fac, ele);
		// }

		generateService(fac, ele, disableREST, disableSOAP12 , disableSOAP11);
		addPoliciesToDefinitionElement(policiesInDefinitions.values().iterator(), definition);

		return ele;
	}

	private void generateMessages(OMFactory fac, OMElement defintions) {
		HashSet faultMessageNames = new HashSet();
		messagesMap = new HashMap();

		Iterator operations = axisService.getOperations();
		while (operations.hasNext()) {
			AxisOperation axisOperation = (AxisOperation) operations.next();
			if (axisOperation.isControlOperation()) {
				continue;
			}
			String MEP = axisOperation.getMessageExchangePattern();
			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage =
                        axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (inaxisMessage != null) {
					writeMessage(inaxisMessage, fac, defintions);
					generateHeaderMessages(inaxisMessage, fac, defintions);
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisMessage outAxisMessage = axisOperation
						.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (outAxisMessage != null) {
					writeMessage(outAxisMessage, fac, defintions);
					generateHeaderMessages(outAxisMessage, fac, defintions);
				}
			}

			// generate fault Messages
			ArrayList faultyMessages = axisOperation.getFaultMessages();
			if (faultyMessages != null) {
                for (Object faultyMessage : faultyMessages) {
                    AxisMessage axisMessage = (AxisMessage)faultyMessage;
                    String name = axisMessage.getName();
                    if (faultMessageNames.add(name)) {
                        writeMessage(axisMessage, fac, defintions);
                        generateHeaderMessages(axisMessage, fac, defintions);
                    }
                }
			}
		}
	}

	private void generateHeaderMessages(AxisMessage axismessage, OMFactory fac,
			OMElement defintions) {
		ArrayList extList = axismessage.getSoapHeaders();
        for (Object anExtList : extList) {
            SOAPHeaderMessage header = (SOAPHeaderMessage)anExtList;
    		if (header.getMessage() != null && messagesMap.get(header.getMessage().getLocalPart()) == null) {
    			messagesMap.put(header.getMessage().getLocalPart(),header);
				OMElement messageElement = fac.createOMElement(
						MESSAGE_LOCAL_NAME, wsdl);
				messageElement.addAttribute(ATTRIBUTE_NAME, header.getMessage()
						.getLocalPart(), null);
				defintions.addChild(messageElement);
				OMElement messagePart = fac.createOMElement(
						PART_ATTRIBUTE_NAME, wsdl);
				messageElement.addChild(messagePart);
				messagePart.addAttribute(ATTRIBUTE_NAME, header.part(), null);
				if (header.getElement() == null) {
					throw new RuntimeException(ELEMENT_ATTRIBUTE_NAME
							+ " is null for " + header.getMessage());
				}
				messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
						WSDLSerializationUtil.getPrefix(header.getElement()
								.getNamespaceURI(), axisService
								.getNamespaceMap())
								+ ":" + header.getElement().getLocalPart(),
						null);
			}
        }
	}

	private void writeMessage(AxisMessage axismessage, OMFactory fac, OMElement defintions) {
		if (axismessage.getName() != null && messagesMap.get(axismessage.getName()) == null) {
			messagesMap.put(axismessage.getName(), axismessage);
			QName schemaElementName = axismessage.getElementQName();
			OMElement messageElement = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
			messageElement.addAttribute(ATTRIBUTE_NAME, axismessage.getName(), null);
			defintions.addChild(messageElement);
			if (schemaElementName != null) {
				OMElement messagePart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
				messageElement.addChild(messagePart);
				if (axismessage.getMessagePartName() != null) {
					messagePart.addAttribute(ATTRIBUTE_NAME,
                                             axismessage.getMessagePartName(),
                                             null);
				} else {
					messagePart.addAttribute(ATTRIBUTE_NAME, axismessage.getPartName(), null);
				}
				messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
						WSDLSerializationUtil.getPrefix(schemaElementName.getNamespaceURI(),
                                                        axisService.getNamespaceMap())
								+ ":" + schemaElementName.getLocalPart(), null);
			}
		}

	}

	/**
	 * Builds the &lt;portType&gt; element in the passed WSDL definition. When
	 * this returns successfully, there will be a new child element under
	 * definitons for the portType.
	 * 
	 * @param fac
	 *            the active OMFactory
	 * @param defintions
	 *            the WSDL &lt;definitions&gt; element
	 * @throws Exception
	 *             if there's a problem
	 */
	private void generatePortType(OMFactory fac, OMElement defintions)
			throws Exception {
		OMElement portType = fac.createOMElement(PORT_TYPE_LOCAL_NAME, wsdl);
		defintions.addChild(portType);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		portType.addAttribute(ATTRIBUTE_NAME, portTypeName, null);

		addPolicyAsExtAttribute(axisService, portType, fac);

		for (Iterator operations = axisService.getOperations(); operations
				.hasNext();) {
			AxisOperation axisOperation = (AxisOperation) operations.next();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String operationName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			WSDLSerializationUtil.addWSDLDocumentationElement(axisOperation,
					operation, fac, wsdl);
			portType.addChild(operation);
			operation.addAttribute(ATTRIBUTE_NAME, operationName, null);
			addPolicyAsExtAttribute(axisOperation, operation,fac);

			String MEP = axisOperation.getMessageExchangePattern();
			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisMessage inaxisMessage = axisOperation
						.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (inaxisMessage != null) {
					OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
							wsdl);
					WSDLSerializationUtil.addWSDLDocumentationElement(
							inaxisMessage, input, fac, wsdl);
					input.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
							+ ":" + inaxisMessage.getName(), null);
					addPolicyAsExtAttribute(inaxisMessage, input,fac);

					WSDLSerializationUtil.addWSAWActionAttribute(input,
							axisOperation.getInputAction(), wsaw);
					operation.addChild(input);
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisMessage outAxisMessage = axisOperation
						.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (outAxisMessage != null) {
					OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
							wsdl);
					WSDLSerializationUtil.addWSDLDocumentationElement(
							outAxisMessage, output, fac, wsdl);
					output.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
							+ ":" + outAxisMessage.getName(), null);
					addPolicyAsExtAttribute(outAxisMessage, output, fac);
					WSDLSerializationUtil.addWSAWActionAttribute(output,
							axisOperation.getOutputAction(), wsaw);
					operation.addChild(output);
				}
			}

			// generate fault Messages
			ArrayList faultMessages = axisOperation.getFaultMessages();
			if (faultMessages != null) {
                for (Object faultMessage : faultMessages) {
                    AxisMessage faultyMessage = (AxisMessage)faultMessage;
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME, wsdl);
                    WSDLSerializationUtil.addWSDLDocumentationElement(faultyMessage,
                                                                      fault,
                                                                      fac,
                                                                      wsdl);
                    fault.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                                                           + ":" + faultyMessage.getName(), null);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessage.getName(), null);
                    WSDLSerializationUtil.addWSAWActionAttribute(fault,
                                                                 axisOperation.getFaultAction(
                                                                         faultyMessage.getName()),
                                                                 wsaw);
                    // TODO add policies for fault messages
                    operation.addChild(fault);
                }
			}

		}
	}

	/**
	 * Generate the WSDL &lt;service&gt; element
	 * 
	 * @param fac
	 *            the active OMFactory
	 * @param defintions
	 *            the WSDL &lt;definitions&gt; element under which to put the
	 *            service
	 * @param disableREST
	 *            if false, generate REST binding, if true, don't
	 * @param disableSOAP12
	 *            if false, generate SOAP 1.2 binding, if true, don't
	 * @throws Exception
	 *             if there's a problem
	 */
	public void generateService(OMFactory fac, OMElement defintions, boolean disableREST,
                                boolean disableSOAP12, boolean disableSOAP11)
            throws Exception {
		OMElement service = fac.createOMElement(SERVICE_LOCAL_NAME, wsdl);
		defintions.addChild(service);
		service.addAttribute(ATTRIBUTE_NAME, serviceName, null);

        if (!disableSOAP11) {
			generateSoap11Port(fac, defintions, service);
		}

        if (!disableSOAP12) {
			// generateSOAP12Ports(fac, service);
			generateSoap12Port(fac, defintions, service);
		}


        addPolicyAsExtElement(PolicyInclude.SERVICE_POLICY, axisService.getPolicyInclude(),
                              service);
		// addPolicyAsExtElement(PolicyInclude.AXIS_SERVICE_POLICY, axisService.
		// getPolicyInclude(), service);

		if (!disableREST) {
			// generateHTTPPorts(fac, service);
			generateHttpPort(fac, definition, service);
		}
	}

	private void writeSoapHeaders(AxisMessage inaxisMessage, OMFactory fac,
			OMElement input, OMNamespace soapNameSpace) throws Exception {
		ArrayList extElementList;
		extElementList = inaxisMessage.getSoapHeaders();
		if (extElementList != null) {
			Iterator elements = extElementList.iterator();
			while (elements.hasNext()) {
				SOAPHeaderMessage soapheader = (SOAPHeaderMessage) elements
						.next();
				addSOAPHeader(fac, input, soapheader, soapNameSpace);
			}
		}
	}

	private void addExtensionElement(OMFactory fac, OMElement element,
			String name, String att1Name, String att1Value, String att2Name,
			String att2Value, OMNamespace soapNameSpace) {
		OMElement soapbinding = fac.createOMElement(name, soapNameSpace);
		element.addChild(soapbinding);
		soapbinding.addAttribute(att1Name, att1Value, null);
		if (att2Name != null) {
			soapbinding.addAttribute(att2Name, att2Value, null);
		}
	}

	private void setDefinitionElement(OMElement defintion) {
		this.definition = defintion;
	}

	private void addSOAPHeader(OMFactory fac, OMElement element,
			SOAPHeaderMessage header, OMNamespace soapNameSpace) {
		OMElement extElement = fac.createOMElement("header", soapNameSpace);
		element.addChild(extElement);
		String use = header.getUse();
		if (use != null) {
			extElement.addAttribute("use", use, null);
		}
		if (header.part() != null) {
			extElement.addAttribute("part", header.part(), null);
		}
		if (header.getMessage() != null) {
			extElement.addAttribute("message", WSDLSerializationUtil.getPrefix(
					targetNamespace, axisService.getNamespaceMap())
					+ ":" + header.getMessage().getLocalPart(), null);
		}
	}

	private void addPolicyAsExtElement(int type, PolicyInclude policyInclude,
			OMElement parentElement) throws Exception {
		ArrayList elementList = policyInclude.getPolicyElements(type);

        for (Object policyElement : elementList) {
            if (policyElement instanceof Policy) {
                OMElement child = PolicyUtil.getPolicyComponentAsOMElement(
                        (PolicyComponent)policyElement, serializer);

//                OMNode firstChildElem = parentElement.getFirstElement();

                // if (firstChildElem == null) {
                // parentElement.addChild(child);
                // } else {
                // firstChildElem.insertSiblingBefore(child);
                // }
                // there is a problem with the OM insertSiblingBefore element
                // with
                // drops the already exists elements.
                // since there is no any techical problem of adding policy
                // elements after other
                // children temporaliy fix this as it is.
                // one OM fix this issue we can revert this change.
                parentElement.addChild(child);

            } else if (policyElement instanceof PolicyReference) {
                OMElement child = PolicyUtil
                        .getPolicyComponentAsOMElement((PolicyComponent)policyElement);
                OMElement firstChildElem = parentElement.getFirstElement();

                if (firstChildElem == null) {
                    parentElement.addChild(child);
                } else {
                    firstChildElem.insertSiblingBefore(child);
                }

                PolicyRegistry reg = policyInclude.getPolicyRegistry();
                String key = ((PolicyReference)policyElement).getURI();

                if (key.startsWith("#")) {
                    key = key.substring(key.indexOf("#") + 1);
                }

                Policy p = reg.lookup(key);

                if (p == null) {
                    throw new Exception("Policy not found for uri : " + key);
                }

                addPolicyToDefinitionElement(key, p);
            }
        }
	}

	private void addPoliciesToDefinitionElement(Iterator iterator,
			OMElement definitionElement) throws Exception {
		Policy policy;
		OMElement policyElement;
		OMNode firstChild;

		for (; iterator.hasNext();) {
			policy = (Policy) iterator.next();
			policyElement = PolicyUtil.getPolicyComponentAsOMElement(policy,
					serializer);
			firstChild = definition.getFirstOMChild();
			if (firstChild != null) {
				firstChild.insertSiblingBefore(policyElement);
			} else {
				definitionElement.addChild(policyElement);
			}
		}
	}

	private void addPolicyToDefinitionElement(String key, Policy policy) {
		policiesInDefinitions.put(key, policy);
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	private void generateSoap11Port(OMFactory fac, OMElement definition,
			OMElement service) throws Exception {
		Iterator iterator = axisService.getEndpoints().values().iterator();
		AxisEndpoint axisEndpoint;
		AxisBinding axisBinding;
		for (; iterator.hasNext();) {
			axisEndpoint = (AxisEndpoint) iterator.next();
			/*
			 * Some transports might not be active at runtime.
			 */
			if (!axisEndpoint.isActive()) {
				continue;
			}
			axisBinding = axisEndpoint.getBinding();
            if (axisEndpoint.getEndpointURL() == null || axisEndpoint.getEndpointURL().startsWith("local://")) {
                //skip adding local endpoints to wsdl or if endpoint doesn't have url
                continue;
            }
			String type = axisBinding.getType();
			if (Java2WSDLConstants.TRANSPORT_URI.equals(type)
					|| WSDL2Constants.URI_WSDL2_SOAP.equals(type)) {
				String version = (String) axisBinding
						.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
				if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(version)) {
					OMElement port = fac.createOMElement(PORT, wsdl);
					service.addChild(port);
					port.addAttribute(ATTRIBUTE_NAME, axisEndpoint.getName(),
							null);
					QName qname = axisBinding.getName();
					port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
							+ qname.getLocalPart(), null);
					String endpointURL = getEndpointURL(axisEndpoint);
					WSDLSerializationUtil.addExtensionElement(fac, port,
							SOAP_ADDRESS, LOCATION, (endpointURL == null) ? ""
									: endpointURL, soap);
					generateEPRElement(fac, port, endpointURL);
					addPolicyAsExtElement(axisEndpoint, port);
					generateSoap11Binding(fac, definition, axisEndpoint
							.getBinding());
				}
			}
		}
	}

	private void generateSoap12Port(OMFactory fac, OMElement definition,
			OMElement service) throws Exception {

		// /////////////////// FIXME //////////////////////////////////////////
		Iterator iterator = axisService.getEndpoints().values().iterator();
		AxisEndpoint axisEndpoint;
		AxisBinding axisBinding;
		for (; iterator.hasNext();) {
			axisEndpoint = (AxisEndpoint) iterator.next();
			/*
			 * 
			 */
			if (!axisEndpoint.isActive()) {
				continue;
			}
            if (axisEndpoint.getEndpointURL() == null || axisEndpoint.getEndpointURL().startsWith("local://")) {
                //skip adding local endpoints to wsdl or if endpoint doesn't have url
                continue;
            }
            axisBinding = axisEndpoint.getBinding();
			String type = axisBinding.getType();
			if (Java2WSDLConstants.TRANSPORT_URI.equals(type)
					|| WSDL2Constants.URI_WSDL2_SOAP.equals(type)) {
				String version = (String) axisBinding
						.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
				if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(version)) {

					OMElement port = fac.createOMElement(PORT, wsdl);
					service.addChild(port);
					port.addAttribute(ATTRIBUTE_NAME, axisEndpoint.getName(),
							null);
					QName qname = axisBinding.getName();
					port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
							+ qname.getLocalPart(), null);
					String endpointURL = getEndpointURL(axisEndpoint);
					WSDLSerializationUtil.addExtensionElement(fac, port,
							SOAP_ADDRESS, LOCATION, (endpointURL == null) ? ""
									: endpointURL, soap12);
					generateEPRElement(fac, port, endpointURL);
					addPolicyAsExtElement(axisEndpoint, port);
					generateSoap12Binding(fac, definition, axisEndpoint
							.getBinding());
				}
			}
		}
	}

	private void generateHttpPort(OMFactory fac, OMElement definition,
			OMElement service) throws Exception {

		Iterator iterator = axisService.getEndpoints().values().iterator();
		AxisEndpoint axisEndpoint;
		AxisBinding axisBinding;
		for (; iterator.hasNext();) {
			axisEndpoint = (AxisEndpoint) iterator.next();
			/*
			 * 
			 */
			if (!axisEndpoint.isActive()) {
				continue;
			}
			axisBinding = axisEndpoint.getBinding();
			String type = axisBinding.getType();
			if (WSDL2Constants.URI_WSDL2_HTTP.equals(type)) {
				OMElement port = fac.createOMElement(PORT, wsdl);
				service.addChild(port);
				port.addAttribute(ATTRIBUTE_NAME, axisEndpoint.getName(), null);
				QName qname = axisBinding.getName();
				port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
						+ qname.getLocalPart(), null);
				OMElement extElement = fac.createOMElement("address", http);
				String endpointURL = getEndpointURL(axisEndpoint);
				extElement.addAttribute("location", (endpointURL == null) ? ""
						: endpointURL, null);
				port.addChild(extElement);

				addPolicyAsExtElement(axisEndpoint, port);
				generateHttpBinding(fac, definition, axisEndpoint.getBinding());
			}
		}
	}

	private void generateSoap11Binding(OMFactory fac, OMElement defintions,
			AxisBinding axisBinding) throws Exception {
		if (isAlreadyAdded(axisBinding, defintions)) {
			return;
		}
		OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
		OMElement serviceElement = defintions.getFirstChildWithName(new QName(
				wsdl.getNamespaceURI(), SERVICE_LOCAL_NAME));
		serviceElement.insertSiblingBefore(binding);

		QName qname = axisBinding.getName();
		binding.addAttribute(ATTRIBUTE_NAME, qname.getLocalPart(), null);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		binding.addAttribute("type", tns.getPrefix() + ":" + portTypeName, null);

		// Adding ext elements
		addPolicyAsExtElement(axisBinding, binding);
		addExtensionElement(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
				TRANSPORT_URI, STYLE, style, soap);

		// /////////////////////////////////////////////////////////////////////
		// Add WS-Addressing UsingAddressing element if appropriate
		// SHOULD be on the binding element per the specification
		if (AddressingHelper
				.getAddressingRequirementParemeterValue(axisService).equals(
						AddressingConstants.ADDRESSING_OPTIONAL)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "false", wsaw);
		} else if (AddressingHelper.getAddressingRequirementParemeterValue(
				axisService).equals(AddressingConstants.ADDRESSING_REQUIRED)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true", wsaw);
		}
		// //////////////////////////////////////////////////////////////////////

		for (Iterator axisBindingOperations = axisBinding.getChildren(); axisBindingOperations
				.hasNext();) {
			AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBindingOperations
					.next();
			AxisOperation axisOperation = axisBindingOperation
					.getAxisOperation();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String opeartionName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			binding.addChild(operation);
			String soapAction = axisOperation.getSoapAction();
			if (soapAction == null) {
				soapAction = "";
			}
			addPolicyAsExtElement(axisBindingOperation, operation);
			addExtensionElement(fac, operation, OPERATION_LOCAL_NAME,
					SOAP_ACTION, soapAction, STYLE, style, soap);

			// addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
			// axisOperation.getPolicyInclude(), operation);

			String MEP = axisOperation.getMessageExchangePattern();

			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisBindingMessage axisBindingInMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (axisBindingInMessage != null) {
					AxisMessage inaxisMessage = axisBindingInMessage
							.getAxisMessage();

					if (inaxisMessage != null) {
						operation.addAttribute(ATTRIBUTE_NAME, opeartionName,
								null);
						OMElement input = fac.createOMElement(
								IN_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingInMessage, input);
						addExtensionElement(fac, input, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap);
						// addPolicyAsExtElement(PolicyInclude.BINDING_INPUT_POLICY,
						// inaxisMessage.getPolicyInclude(), input);
						operation.addChild(input);
						writeSoapHeaders(inaxisMessage, fac, input, soap12);
					}
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {

				AxisBindingMessage axisBindingOutMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (axisBindingOutMessage != null) {
					AxisMessage outAxisMessage = axisBindingOutMessage
							.getAxisMessage();
					if (outAxisMessage != null) {
						OMElement output = fac.createOMElement(
								OUT_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingOutMessage, output);
						addExtensionElement(fac, output, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap);
						// addPolicyAsExtElement(PolicyInclude.BINDING_OUTPUT_POLICY,
						// outAxisMessage.getPolicyInclude(), output);
						operation.addChild(output);
						writeSoapHeaders(outAxisMessage, fac, output, soap12);
					}
				}
			}

			// generate fault Messages
			ArrayList faultyMessages = axisBindingOperation.getFaults();
			if (faultyMessages != null) {
                for (Object faultyMessage1 : faultyMessages) {
                    AxisBindingMessage bindingFaultyMessage = (AxisBindingMessage)faultyMessage1;
                    if (bindingFaultyMessage != null) {
                        AxisMessage faultyMessage = bindingFaultyMessage.getAxisMessage();

                        OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME, wsdl);

                        addPolicyAsExtElement(bindingFaultyMessage, fault);
                        addExtensionElement(fac, fault, FAULT_LOCAL_NAME,
                                            SOAP_USE, use, ATTRIBUTE_NAME,
                                            faultyMessage.getName(), soap);
                        fault.addAttribute(ATTRIBUTE_NAME, faultyMessage.getName(), null);
                        // add policies for fault messages
                        operation.addChild(fault);
                        writeSoapHeaders(faultyMessage, fac, fault, soap);
                    }
                }
			}
		}
	}

	private void generateSoap12Binding(OMFactory fac, OMElement definitions,
			AxisBinding axisBinding) throws Exception {
		if (isAlreadyAdded(axisBinding, definitions)) {
			return;
		}
		OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
		OMElement serviceElement = definitions.getFirstChildWithName(new QName(
				wsdl.getNamespaceURI(), SERVICE_LOCAL_NAME));
		serviceElement.insertSiblingBefore(binding);

		QName qname = axisBinding.getName();
		binding.addAttribute(ATTRIBUTE_NAME, qname.getLocalPart(), null);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		binding.addAttribute("type", tns.getPrefix() + ":" + portTypeName, null);

		// Adding ext elements
		addPolicyAsExtElement(axisBinding, binding);
		addExtensionElement(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
				TRANSPORT_URI, STYLE, style, soap12);

		// /////////////////////////////////////////////////////////////////////
		// Add WS-Addressing UsingAddressing element if appropriate
		// SHOULD be on the binding element per the specification
		if (AddressingHelper
				.getAddressingRequirementParemeterValue(axisService).equals(
						AddressingConstants.ADDRESSING_OPTIONAL)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "false", wsaw);
		} else if (AddressingHelper.getAddressingRequirementParemeterValue(
				axisService).equals(AddressingConstants.ADDRESSING_REQUIRED)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true", wsaw);
		}
		// //////////////////////////////////////////////////////////////////////

		for (Iterator axisBindingOperations = axisBinding.getChildren(); axisBindingOperations
				.hasNext();) {
			AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBindingOperations
					.next();
			AxisOperation axisOperation = axisBindingOperation
					.getAxisOperation();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String opeartionName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			binding.addChild(operation);
			String soapAction = axisOperation.getSoapAction();
			if (soapAction == null) {
				soapAction = "";
			}
			addPolicyAsExtElement(axisBindingOperation, operation);
			addExtensionElement(fac, operation, OPERATION_LOCAL_NAME,
					SOAP_ACTION, soapAction, STYLE, style, soap12);

			// addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
			// axisOperation.getPolicyInclude(), operation);

			String MEP = axisOperation.getMessageExchangePattern();

			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisBindingMessage axisBindingInMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (axisBindingInMessage != null) {
					AxisMessage inaxisMessage = axisBindingInMessage
							.getAxisMessage();

					if (inaxisMessage != null) {
						operation.addAttribute(ATTRIBUTE_NAME, opeartionName,
								null);
						OMElement input = fac.createOMElement(
								IN_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingInMessage, input);
						addExtensionElement(fac, input, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap12);
						operation.addChild(input);
						writeSoapHeaders(inaxisMessage, fac, input, soap12);
					}
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {

				AxisBindingMessage axisBindingOutMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (axisBindingOutMessage != null) {
					AxisMessage outAxisMessage = axisBindingOutMessage
							.getAxisMessage();
					if (outAxisMessage != null) {
						OMElement output = fac.createOMElement(
								OUT_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingOutMessage, output);
						addExtensionElement(fac, output, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap12);
						// addPolicyAsExtElement(PolicyInclude.BINDING_OUTPUT_POLICY,
						// outAxisMessage.getPolicyInclude(), output);
						operation.addChild(output);
						writeSoapHeaders(outAxisMessage, fac, output, soap12);
					}
				}
			}

			// generate fault Messages
			ArrayList faultyMessages = axisBindingOperation.getFaults();
			if (faultyMessages != null) {
                for (Object faultyMessage1 : faultyMessages) {
                    AxisBindingMessage bindingFaultyMessage = (AxisBindingMessage)faultyMessage1;
                    if (bindingFaultyMessage != null) {
                        AxisMessage faultyMessage = bindingFaultyMessage
                                .getAxisMessage();
                        OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                                                              wsdl);
                        addPolicyAsExtElement(bindingFaultyMessage, fault);
                        addExtensionElement(fac, fault, FAULT_LOCAL_NAME,
                                            SOAP_USE, use, ATTRIBUTE_NAME, faultyMessage
                                .getName(), soap12);
                        fault.addAttribute(ATTRIBUTE_NAME, faultyMessage
                                .getName(), null);
                        // add policies for fault messages
                        operation.addChild(fault);
                        writeSoapHeaders(faultyMessage, fac, fault, soap12);
                    }
                }
			}
		}
	}

	private void generateHttpBinding(OMFactory fac, OMElement definitions,
			AxisBinding axisBinding) throws Exception {
		if (isAlreadyAdded(axisBinding, definitions)) {
			return;
		}
		OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
		OMElement serviceElement = definitions.getFirstChildWithName(new QName(
				wsdl.getNamespaceURI(), SERVICE_LOCAL_NAME));
		serviceElement.insertSiblingBefore(binding);

		QName qname = axisBinding.getName();
		binding.addAttribute(ATTRIBUTE_NAME, qname.getLocalPart(), null);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		binding.addAttribute("type", tns.getPrefix() + ":" + portTypeName, null);

		OMElement httpBinding = fac.createOMElement("binding", http);
		binding.addChild(httpBinding);
		httpBinding.addAttribute("verb", "POST", null);

		for (Iterator axisBindingOperations = axisBinding.getChildren(); axisBindingOperations
				.hasNext();) {
			AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBindingOperations
					.next();
			AxisOperation axisOperation = axisBindingOperation
					.getAxisOperation();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String opeartionName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			binding.addChild(operation);

			OMElement httpOperation = fac.createOMElement("operation", http);
			operation.addChild(httpOperation);
            String location = (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);
            location = location.replace('{','(');
            location = location.replace('}',')');
            httpOperation.addAttribute("location", location, null);

			String MEP = axisOperation.getMessageExchangePattern();

			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisBindingMessage axisBindingInMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (axisBindingInMessage != null) {
					AxisMessage inaxisMessage = axisBindingInMessage
							.getAxisMessage();

					if (inaxisMessage != null) {
						operation.addAttribute(ATTRIBUTE_NAME, opeartionName,
								null);
						OMElement input = fac.createOMElement(
								IN_PUT_LOCAL_NAME, wsdl);
						OMElement inputelement = fac.createOMElement("content",
								mime);
						input.addChild(inputelement);
                        String inputType=(String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION);
                        inputelement.addAttribute("type", (inputType!=null? inputType:"text/xml"), null);
						inputelement.addAttribute("part", inaxisMessage.getPartName(), null);
						operation.addChild(input);
					}
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {

				AxisBindingMessage axisBindingOutMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (axisBindingOutMessage != null) {
					AxisMessage outAxisMessage = axisBindingOutMessage
							.getAxisMessage();
					if (outAxisMessage != null) {
						OMElement output = fac.createOMElement(
								OUT_PUT_LOCAL_NAME, wsdl);
						OMElement outElement = fac.createOMElement("content",
								mime);
						outElement.addChild(outElement);
                        String outputType=(String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION);
                        outElement.addAttribute("type", (outputType!=null? outputType:"text/xml"), null);
						outElement.addAttribute("part", outAxisMessage.getPartName(), null);
						output.addChild(outElement);
						operation.addChild(output);
					}
				}
			}
		}
	}

	private void addPolicyAsExtElement(AxisDescription axisDescription,
			OMElement wsdlElement) throws Exception {
		PolicySubject policySubject = axisDescription.getPolicySubject();
		Collection attachPolicyComponents = policySubject
				.getAttachedPolicyComponents();

        for (Object policyElement : attachPolicyComponents) {
            if (policyElement instanceof Policy) {
                PolicyReference policyReference =
                        PolicyUtil.createPolicyReference((Policy)policyElement);
                OMElement policyRefElement =
                        PolicyUtil.getPolicyComponentAsOMElement(policyReference, serializer);

                OMNode firstChildElem = wsdlElement.getFirstElement();
                if (firstChildElem == null) {
                    wsdlElement.addChild(policyRefElement);
                } else {
                    firstChildElem.insertSiblingBefore(policyRefElement);
                }
                String key = policyReference.getURI();
                if (key.startsWith("#")) {
                    key = key.substring(key.indexOf("#") + 1);
                }
                addPolicyToDefinitionElement(key, (Policy)policyElement);

            } else if (policyElement instanceof PolicyReference) {
                OMElement child =
                        PolicyUtil.getPolicyComponentAsOMElement((PolicyComponent)policyElement,
                                                                 serializer);
                OMElement firstChildElem = wsdlElement.getFirstElement();

                if (firstChildElem == null) {
                    wsdlElement.addChild(child);
                } else {
                    firstChildElem.insertSiblingBefore(child);
                }

                String key = ((PolicyReference)policyElement).getURI();
                if (key.startsWith("#")) {
                    key = key.substring(key.indexOf("#") + 1);
                }

                PolicyLocator locator = new PolicyLocator(axisService);
                Policy p = locator.lookup(key);

                if (p != null) {
                    addPolicyToDefinitionElement(key, p);
                    //p = ((PolicyReference)policyElement).getRemoteReferencedPolicy(((PolicyReference) policyElement).getURI());
                    //throw new Exception("Policy not found for uri : " + key);
                }
            }
        }
	}

	private void addPolicyAsExtAttribute(AxisDescription axisDescription,
			OMElement element, OMFactory factory) throws Exception {

		PolicySubject policySubject = axisDescription.getPolicySubject();
		ArrayList policyURIs = new ArrayList();

        for (Object policyElement : policySubject.getAttachedPolicyComponents()) {
            String key;

            if (policyElement instanceof Policy) {
                Policy p = (Policy)policyElement;

                if (p.getId() != null) {
                    key = "#" + p.getId();
                } else if (p.getName() != null) {
                    key = p.getName();
                } else {
                    throw new RuntimeException(
                            "Can't add the Policy as an extensibility attribute since it doesn't have a id or a name attribute");
                }

                policyURIs.add(key);
                addPolicyToDefinitionElement(key, p);
            } else {
                String uri = ((PolicyReference)policyElement).getURI();
                PolicyLocator locator = new PolicyLocator(axisService);
                if (uri.startsWith("#")) {
                    key = uri.substring(uri.indexOf('#') + 1);
                } else {
                    key = uri;
                }

                Policy p = locator.lookup(key);

                if (p == null) {
                    throw new RuntimeException("Cannot resolve " + uri
                                               + " to a Policy");
                }
                policyURIs.add(uri);
                addPolicyToDefinitionElement(key, p);
            }
        }

		if (!policyURIs.isEmpty()) {
			String value = null;

			/*
			 * We need to create a String that is like 'uri1 uri2 .." to set as
			 * the value of the wsp:PolicyURIs attribute.
			 */
            for (Object policyURI : policyURIs) {
                String uri = (String)policyURI;
                value = (value == null) ? uri : value + " " + uri;
            }

			OMNamespace ns = factory.createOMNamespace(
					org.apache.neethi.Constants.URI_POLICY_NS,
					org.apache.neethi.Constants.ATTR_WSP);
			OMAttribute URIs = factory.createOMAttribute("PolicyURIs", ns,
					value);
			element.addAttribute(URIs);
		}
	}

	private boolean isAlreadyAdded(AxisBinding axisBinding,
			OMElement definitionElement) {
		QName bindingName = axisBinding.getName();
		QName name = new QName("name");
		for (Iterator iterator = definitionElement
				.getChildrenWithName(new QName(wsdl.getNamespaceURI(),
						BINDING_LOCAL_NAME)); iterator.hasNext();) {
			OMElement element = (OMElement) iterator.next();
			String value = element.getAttributeValue(name);
			if (bindingName.getLocalPart().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	private String getEndpointURL(AxisEndpoint axisEndpoint) {
		Parameter modifyAddressParam = axisService.getParameter("modifyUserWSDLPortAddress");
            String endpointURL = axisEndpoint.getEndpointURL();
            if (modifyAddressParam != null &&
                    !Boolean.parseBoolean((String)modifyAddressParam.getValue())) {
                return endpointURL;
            }

            String hostIP;
    
            // First check the hostname parameter 
            hostIP = Utils.getHostname(axisService.getAxisConfiguration());
        
            //If it is not set extract the hostIP from the URL
            if (hostIP == null) {
                hostIP = WSDLSerializationUtil.extractHostIP(axisService.getEndpointURL());
            }
        
            //TODO This is to prevent problems when JAVA2WSDL tool is used where there is no
            //Axis server running. calculateEndpointURL fails in this scenario, refer to 
            // SimpleHTTPServer#getEPRsForService()
  
            if (hostIP != null) {
                return axisEndpoint.calculateEndpointURL(hostIP);
            } else {
                return endpointURL;
            }
	}
	
	/**
	 * Generate the Identity element according to the WS-AddressingAndIdentity if the 
	 * AddressingConstants.IDENTITY_PARAMETER parameter is set. 
	 * http://schemas.xmlsoap.org/ws/2006/02/addressingidentity/ 
	 */
	
	private void generateIdentityElement(OMFactory fac,OMElement epr, Parameter wsaIdParam) {
	    
	    // Create the Identity element
	    OMElement identity = fac.createOMElement(AddressingConstants.QNAME_IDENTITY);
	    OMElement keyInfo = fac.createOMElement(AddressingConstants.QNAME_IDENTITY_KEY_INFO);
	    OMElement x509Data = fac.createOMElement(AddressingConstants.QNAME_IDENTITY_X509_DATA);
	    OMElement x509cert = fac.createOMElement(AddressingConstants.QNAME_IDENTITY_X509_CERT);
	    x509cert.setText((String)wsaIdParam.getValue());
	    
	    x509Data.addChild(x509cert);
	    keyInfo.addChild(x509Data);
	    identity.addChild(keyInfo);
	    
	    epr.addChild(identity);
	    
	}
	
	
	/*
	 * Generate the EndpointReference element
	 * <wsa:EndpointReference>
         *    <wsa:Address>
         *        http://some.service.epr/
         *     </wsa:Address>
         * </wsa:EndpointReference>
	 * 
	 */
	private void generateEPRElement(OMFactory fac, OMElement port, String endpointURL){
	    
	    Parameter parameter = axisService.getParameter(AddressingConstants.IDENTITY_PARAMETER);
	            
	    // If the parameter is not set, return
	    if (parameter == null || parameter.getValue() == null) {
	        return;
	    }
	    
	    OMElement wsaEpr = fac.createOMElement(AddressingConstants.Final.WSA_ENDPOINT_REFERENCE);
	    
	    OMElement address = fac.createOMElement(AddressingConstants.Final.WSA_ADDRESS);
	    address.setText((endpointURL == null) ? "": endpointURL);
	    
	    wsaEpr.addChild(address);
	    
	    // This will generate the identity element if the service parameter is set
	    generateIdentityElement(fac, wsaEpr, parameter);
	    
	    port.addChild(wsaEpr);   
	    
	}
}
