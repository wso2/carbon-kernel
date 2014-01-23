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

package org.apache.axis2.deployment;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.*;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Builds a service description from OM
 */
public class ServiceBuilder extends DescriptionBuilder {
	private static final Log log = LogFactory.getLog(ServiceBuilder.class);
	private AxisService service;
	private HashMap<String,AxisService> wsdlServiceMap = new HashMap<String,AxisService>();

	public ServiceBuilder(ConfigurationContext configCtx, AxisService service) {
		this.service = service;
		this.configCtx = configCtx;
		this.axisConfig = this.configCtx.getAxisConfiguration();
	}

	public ServiceBuilder(InputStream serviceInputStream,
			ConfigurationContext configCtx, AxisService service) {
		super(serviceInputStream, configCtx);
		this.service = service;
	}

	/**
	 * Populates service from corresponding OM.
	 * 
	 * @param service_element
	 *            an OMElement for the &lt;service&gt; tag
	 * @return a filled-in AxisService, configured from the passed XML
	 * @throws DeploymentException
	 *             if there is a problem
	 */
	public AxisService populateService(OMElement service_element)
			throws DeploymentException {
		try {
			// Determine whether service should be activated.
			String serviceActivate = service_element
					.getAttributeValue(new QName(ATTRIBUTE_ACTIVATE));
			if (serviceActivate != null) {
				if ("true".equals(serviceActivate)) {
					service.setActive(true);
				} else if ("false".equals(serviceActivate)) {
					service.setActive(false);
				}
			}

			// Processing service level parameters
			OMAttribute serviceNameatt = service_element
					.getAttribute(new QName(ATTRIBUTE_NAME));

			// If the service name is explicitly specified in the services.xml
			// then use that as the service name
			if (serviceNameatt != null) {
				if (!"".equals(serviceNameatt.getAttributeValue().trim())) {
					AxisService wsdlService = wsdlServiceMap
							.get(serviceNameatt.getAttributeValue());
					if (wsdlService != null) {
						wsdlService.setClassLoader(service.getClassLoader());
						wsdlService.setParent(service.getAxisServiceGroup());
						service = wsdlService;
						service.setWsdlFound(true);
						service.setCustomWsdl(true);
					}
					service.setName(serviceNameatt.getAttributeValue());
					// To be on the safe side
					if (service.getDocumentation() == null) {
						service.setDocumentation(serviceNameatt
								.getAttributeValue());
					}
				}
			}

			Iterator itr = service_element.getChildrenWithName(new QName(
					TAG_PARAMETER));
			processParameters(itr, service, service.getParent());

            Parameter childFirstClassLoading =
                    service.getParameter(Constants.Configuration.ENABLE_CHILD_FIRST_CLASS_LOADING);
            if (childFirstClassLoading != null) {
                ClassLoader cl = service.getClassLoader();
                if (cl instanceof DeploymentClassLoader) {
                    DeploymentClassLoader deploymentClassLoader = (DeploymentClassLoader) cl;
                    if (JavaUtils.isTrueExplicitly(childFirstClassLoading.getValue())){
                        deploymentClassLoader.setChildFirstClassLoading(true);
                    } else if (JavaUtils.isFalseExplicitly(childFirstClassLoading.getValue())){
                        deploymentClassLoader.setChildFirstClassLoading(false);
                    }
                }
            }

			// If multiple services in one service group have different values
			// for the PARENT_FIRST
			// parameter then the final value become the value specified by the
			// last service in the group
			// Parameter parameter =
			// service.getParameter(DeploymentClassLoader.PARENT_FIRST);
			// if (parameter !=null && "false".equals(parameter.getValue())) {
			// ClassLoader serviceClassLoader = service.getClassLoader();
			// ((DeploymentClassLoader)serviceClassLoader).setParentFirst(false);
			// }
			// process service description
			OMElement descriptionElement = service_element
					.getFirstChildWithName(new QName(TAG_DESCRIPTION));
			if (descriptionElement != null) {
				OMElement descriptionValue = descriptionElement
						.getFirstElement();
				if (descriptionValue != null) {
					service.setDocumentation(descriptionValue);
				} else {
					service.setDocumentation(descriptionElement.getText());
				}
			} else {
				serviceNameatt = service_element.getAttribute(new QName(
						ATTRIBUTE_NAME));

				if (serviceNameatt != null) {
					if (!"".equals(serviceNameatt.getAttributeValue().trim())
							&& service.getDocumentation() == null) {
						service.setDocumentation(serviceNameatt
								.getAttributeValue());
					}
				}
			}

			if (service.getParameter("ServiceClass") == null) {
				log.debug("The Service " + service.getName()
						+ " does not specify a Service Class");
			}

			// Process WS-Addressing flag attribute
			OMAttribute addressingRequiredatt = service_element
					.getAttribute(new QName(ATTRIBUTE_WSADDRESSING));
			if (addressingRequiredatt != null) {
				String addressingRequiredString = addressingRequiredatt
						.getAttributeValue();
				AddressingHelper.setAddressingRequirementParemeterValue(
						service, addressingRequiredString);
			}

			// Setting service target namespace if any
			OMAttribute targetNameSpace = service_element
					.getAttribute(new QName(TARGET_NAME_SPACE));

			if (targetNameSpace != null) {
				String nameSpeceVale = targetNameSpace.getAttributeValue();
				if (nameSpeceVale != null && !"".equals(nameSpeceVale)) {
					service.setTargetNamespace(nameSpeceVale);
				}
			} else {
				if (service.getTargetNamespace() == null
						|| "".equals(service.getTargetNamespace())) {
					service
							.setTargetNamespace(Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE);
				}
			}

			// Processing service lifecycle attribute
			OMAttribute serviceLifeCycleClass = service_element
					.getAttribute(new QName(TAG_CLASS_NAME));
			if (serviceLifeCycleClass != null) {
				String className = serviceLifeCycleClass.getAttributeValue();
				loadServiceLifeCycleClass(className);
			}
			// Setting schema namespece if any
			OMElement schemaElement = service_element
					.getFirstChildWithName(new QName(SCHEMA));
			if (schemaElement != null) {
				OMAttribute schemaNameSpace = schemaElement
						.getAttribute(new QName(SCHEMA_NAME_SPACE));
				if (schemaNameSpace != null) {
					String nameSpeceVale = schemaNameSpace.getAttributeValue();
					if (nameSpeceVale != null && !"".equals(nameSpeceVale)) {
						service.setSchemaTargetNamespace(nameSpeceVale);
					}
				}
				OMAttribute elementFormDefault = schemaElement
						.getAttribute(new QName(SCHEMA_ELEMENT_QUALIFIED));
				if (elementFormDefault != null) {
					String value = elementFormDefault.getAttributeValue();
					if ("true".equals(value)) {
						service.setElementFormDefault(true);
					} else if ("false".equals(value)) {
						service.setElementFormDefault(false);
					}
				}

				// package to namespace mapping. This will be an element that
				// maps pkg names to a namespace
				// when this is doing AxisService.getSchemaTargetNamespace will
				// be overridden
				// This will be <mapping/> with @namespace and @package
				Iterator mappingIterator = schemaElement
						.getChildrenWithName(new QName(MAPPING));
				if (mappingIterator != null) {
					Map<String,String> pkg2nsMap = new Hashtable<String,String>();
					while (mappingIterator.hasNext()) {
						OMElement mappingElement = (OMElement) mappingIterator
								.next();
						OMAttribute namespaceAttribute = mappingElement
								.getAttribute(new QName(ATTRIBUTE_NAMESPACE));
						OMAttribute packageAttribute = mappingElement
								.getAttribute(new QName(ATTRIBUTE_PACKAGE));
						if (namespaceAttribute != null
								&& packageAttribute != null) {
							String namespaceAttributeValue = namespaceAttribute
									.getAttributeValue();
							String packageAttributeValue = packageAttribute
									.getAttributeValue();
							if (namespaceAttributeValue != null
									&& packageAttributeValue != null) {
								pkg2nsMap.put(packageAttributeValue.trim(),
										namespaceAttributeValue.trim());
							} else {
								log
										.warn("Either value of @namespce or @packagename not available. Thus, generated will be selected.");
							}
						} else {
							log
									.warn("Either @namespce or @packagename not available. Thus, generated will be selected.");
						}
					}
					service.setP2nMap(pkg2nsMap);

				}

			}

			// processing Default Message receivers
			OMElement messageReceiver = service_element
					.getFirstChildWithName(new QName(TAG_MESSAGE_RECEIVERS));
			if (messageReceiver != null) {
				HashMap<String,MessageReceiver> mrs = processMessageReceivers(service.getClassLoader(),
						messageReceiver);
				for (Map.Entry<String,MessageReceiver> entry : mrs.entrySet()) {
					service.addMessageReceiver(entry.getKey(), entry.getValue());
				}
			}

			// Removing exclude operations
			OMElement excludeOperations = service_element
					.getFirstChildWithName(new QName(TAG_EXCLUDE_OPERATIONS));
			ArrayList<String> excludeops = null;
			if (excludeOperations != null) {
				excludeops = processExcludeOperations(excludeOperations);
			}
			if (excludeops == null) {
				excludeops = new ArrayList<String>();
			}
			Utils.addExcludeMethods(excludeops);

			// <schema targetNamespace="http://x.y.z"/>
			// setting the PolicyInclude
			// processing <wsp:Policy> .. </..> elements
			Iterator policyElements = service_element
					.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY));

			if (policyElements != null && policyElements.hasNext()) {
				processPolicyElements(policyElements, service.getPolicySubject());
			}

			// processing <wsp:PolicyReference> .. </..> elements
			Iterator policyRefElements = service_element
					.getChildrenWithName(new QName(POLICY_NS_URI,
							TAG_POLICY_REF));

			if (policyRefElements != null && policyRefElements.hasNext()) {
				processPolicyRefElements(policyRefElements, service.getPolicySubject());
			}

			// processing service scope
			String sessionScope = service_element.getAttributeValue(new QName(
					ATTRIBUTE_SCOPE));
			if (sessionScope != null) {
				service.setScope(sessionScope);
			}

			// processing service-wide modules which required to engage globally
			Iterator moduleRefs = service_element
					.getChildrenWithName(new QName(TAG_MODULE));

			processModuleRefs(moduleRefs);

			// processing transports
			OMElement transports = service_element
					.getFirstChildWithName(new QName(TAG_TRANSPORTS));
			if (transports != null) {
				Iterator transport_itr = transports
						.getChildrenWithName(new QName(TAG_TRANSPORT));
				ArrayList<String> trs = new ArrayList<String>();
				while (transport_itr.hasNext()) {
					OMElement trsEle = (OMElement) transport_itr.next();
					String transportName = trsEle.getText().trim();
					if (axisConfig.getTransportIn(transportName) == null) {
                        log.warn("Service [ " + service.getName()
								+ "] is trying to expose in a transport : "
								+ transportName
								+ " and which is not available in Axis2");
					} else {
                        trs.add(transportName);
                    }
				}

                if(trs.isEmpty()){
                    throw new AxisFault("Service [" + service.getName()
                        + "] is trying expose in tranpsorts: "
                        + transports
                        + " and which is/are not available in Axis2");
                }
				service.setExposedTransports(trs);
			}
			// processing operations
			Iterator operationsIterator = service_element
					.getChildrenWithName(new QName(TAG_OPERATION));
			ArrayList ops = processOperations(operationsIterator);

			for (int i = 0; i < ops.size(); i++) {
				AxisOperation operationDesc = (AxisOperation) ops.get(i);
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
			String objectSupplierValue = (String) service
					.getParameterValue(TAG_OBJECT_SUPPLIER);
			if (objectSupplierValue != null) {
				loadObjectSupplierClass(objectSupplierValue);
			}
			// Set the default message receiver for the operations that were
			// not listed in the services.xml
			setDefaultMessageReceivers();
			Utils.processBeanPropertyExclude(service);
			if (!service.isUseUserWSDL()) {
				// Generating schema for the service if the impl class is Java
				if (!service.isWsdlFound()) {
					// trying to generate WSDL for the service using JAM and
					// Java reflection
					try {
						if (generateWsdl(service)) {
							Utils.fillAxisService(service, axisConfig,
									excludeops, null);
						} else {
							ArrayList nonRpcOperations = getNonRPCMethods(service);
							Utils.fillAxisService(service, axisConfig,
									excludeops, nonRpcOperations);
						}
					} catch (Exception e) {
						throw new DeploymentException(Messages.getMessage(
								"errorinschemagen", e.getMessage()), e);
					}
				}
			}
			if (service.isCustomWsdl()) {
				OMElement mappingElement = service_element
						.getFirstChildWithName(new QName(TAG_PACKAGE2QNAME));
				if (mappingElement != null) {
					processTypeMappings(mappingElement);
				}
			}

			for (String opName : excludeops) {
				service.removeOperation(new QName(opName));
			}

			// Need to call the same logic towice
			setDefaultMessageReceivers();
			Iterator moduleConfigs = service_element
					.getChildrenWithName(new QName(TAG_MODULE_CONFIG));
			processServiceModuleConfig(moduleConfigs, service, service);

			// Loading Data Locator(s) configured
			OMElement dataLocatorElement = service_element
					.getFirstChildWithName(new QName(
							DRConstants.DATA_LOCATOR_ELEMENT));
			if (dataLocatorElement != null) {
				processDataLocatorConfig(dataLocatorElement, service);
			}

			processEndpoints(service);
			processPolicyAttachments(service_element, service);
			

		} catch (AxisFault axisFault) {
			throw new DeploymentException(axisFault);
		}

        startupServiceLifecycle();
		return service;
	}

	private void setDefaultMessageReceivers() {
		Iterator operations = service.getPublishedOperations().iterator();
		while (operations.hasNext()) {
			AxisOperation operation = (AxisOperation) operations.next();
			if (operation.getMessageReceiver() == null) {
				MessageReceiver messageReceiver = loadDefaultMessageReceiver(
						operation.getMessageExchangePattern(), service);
				if (messageReceiver == null &&
				// we assume that if the MEP is ROBUST_IN_ONLY then the in-out
						// MR can handle that
						WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(operation
								.getMessageExchangePattern())) {
					messageReceiver = loadDefaultMessageReceiver(
							WSDL2Constants.MEP_URI_IN_OUT, service);

				}
				operation.setMessageReceiver(messageReceiver);
			}
		}
	}

	private void loadObjectSupplierClass(String objectSupplierValue)
			throws AxisFault {
		try {
			ClassLoader loader = service.getClassLoader();
			Class objectSupplierImpl = Loader.loadClass(loader,
					objectSupplierValue.trim());
			ObjectSupplier objectSupplier = (ObjectSupplier) objectSupplierImpl
					.newInstance();
			service.setObjectSupplier(objectSupplier);
		} catch (Exception e) {
			throw AxisFault.makeFault(e);
		}
	}

	/**
	 * Process the package name to QName mapping:
	 * 
	 * &lt;packageMapping&gt; &lt;mapping packageName="foo.bar"
	 * qname="http://foo/bar/xsd"%gt; ...... ...... &lt;/packageMapping&gt;
	 * 
	 * @param packageMappingElement
	 *            OMElement for the packageMappingElement
	 */
	private void processTypeMappings(OMElement packageMappingElement) {
		Iterator elementItr = packageMappingElement
				.getChildrenWithName(new QName(TAG_MAPPING));
		TypeTable typeTable = service.getTypeTable();
		if (typeTable == null) {
			typeTable = new TypeTable();
		}
		while (elementItr.hasNext()) {
			OMElement mappingElement = (OMElement) elementItr.next();
			String packageName = mappingElement.getAttributeValue(new QName(
					TAG_PACKAGE_NAME));
			String qName = mappingElement
					.getAttributeValue(new QName(TAG_QNAME));
			if (packageName == null || qName == null) {
				continue;
			}
			Iterator keys = service.getNamespaceMap().keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (qName.equals(service.getNamespaceMap().get(key))) {
					typeTable.addComplexSchema(packageName, new QName(qName,
							packageName, key));
				}
			}
		}
		service.setTypeTable(typeTable);
	}

	private void loadServiceLifeCycleClass(String className)
			throws DeploymentException {
        if (className != null) {
            try {
                ClassLoader loader = service.getClassLoader();
                Class serviceLifeCycleClassImpl = Loader.loadClass(loader,
						className);
                ServiceLifeCycle serviceLifeCycle =
                        (ServiceLifeCycle) serviceLifeCycleClassImpl.newInstance();
				service.setServiceLifeCycle(serviceLifeCycle);
			} catch (Exception e) {
				throw new DeploymentException(e.getMessage(), e);
			}
		}
	}

	private boolean generateWsdl(AxisService axisService) {
		Iterator operatins = axisService.getOperations();
		if (operatins.hasNext()) {
			while (operatins.hasNext()) {
				AxisOperation axisOperation = (AxisOperation) operatins.next();

				if (axisOperation.isControlOperation()) {
					continue;
				}

				if (axisOperation.getMessageReceiver() == null) {
					continue;
				}
				String messageReceiverClass = axisOperation
						.getMessageReceiver().getClass().getName();
				if (!("org.apache.axis2.rpc.receivers.RPCMessageReceiver"
						.equals(messageReceiverClass)
						|| "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver"
								.equals(messageReceiverClass)
						|| "org.apache.axis2.rpc.receivers.RPCInOutAsyncMessageReceiver"
								.equals(messageReceiverClass) || "org.apache.axis2.jaxws.server.JAXWSMessageReceiver"
						.equals(messageReceiverClass))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * To get the methods which do not use RPC* MessageReceivers
	 * 
	 * @param axisService
	 *            the AxisService to search
	 * @return an ArrayList of the LOCAL PARTS of the QNames of any non-RPC
	 *         operations TODO: Why not just return the AxisOperations
	 *         themselves??
	 */
	private ArrayList<String> getNonRPCMethods(AxisService axisService) {
		ArrayList<String> excludeOperations = new ArrayList<String>();
		Iterator<AxisOperation> operatins = axisService.getOperations();
		if (operatins.hasNext()) {
			while (operatins.hasNext()) {
				AxisOperation axisOperation = operatins.next();
				if (axisOperation.getMessageReceiver() == null) {
					continue;
				}
				String messageReceiverClass = axisOperation
						.getMessageReceiver().getClass().getName();
				if (!("org.apache.axis2.rpc.receivers.RPCMessageReceiver"
						.equals(messageReceiverClass)
						|| "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver"
								.equals(messageReceiverClass)
						|| "org.apache.axis2.rpc.receivers.RPCInOutAsyncMessageReceiver"
								.equals(messageReceiverClass) || "org.apache.axis2.jaxws.server.JAXWSMessageReceiver"
						.equals(messageReceiverClass))) {
					excludeOperations.add(axisOperation.getName()
							.getLocalPart());
				}
			}
		}
		return excludeOperations;
	}

	/**
	 * Process &lt;excludeOperation&gt; element in services.xml. Each operation
	 * referenced will be removed from the AxisService.
	 * 
	 * @param excludeOperations
	 *            the &lt;excludeOperations&gt; element from services.xml
	 * @return an ArrayList of the String contents of the &lt;operation&gt;
	 *         elements
	 */
	private ArrayList<String> processExcludeOperations(OMElement excludeOperations) {
		ArrayList<String> exOps = new ArrayList<String>();
		Iterator excludeOp_itr = excludeOperations
				.getChildrenWithName(new QName(TAG_OPERATION));
		while (excludeOp_itr.hasNext()) {
			OMElement opName = (OMElement) excludeOp_itr.next();
			exOps.add(opName.getText().trim());
		}
		return exOps;
	}

	private void processMessages(Iterator messages, AxisOperation operation)
			throws DeploymentException {
		while (messages.hasNext()) {
			OMElement messageElement = (OMElement) messages.next();
			OMAttribute label = messageElement
					.getAttribute(new QName(TAG_LABEL));

			if (label == null) {
				throw new DeploymentException(Messages
						.getMessage("messagelabelcannotfound"));
			}

			AxisMessage message = operation.getMessage(label
					.getAttributeValue());

			Iterator parameters = messageElement.getChildrenWithName(new QName(
					TAG_PARAMETER));

			// processing <wsp:Policy> .. </..> elements
			Iterator policyElements = messageElement
					.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY));

			if (policyElements != null) {
				processPolicyElements(policyElements, message.getPolicySubject());
			}

			// processing <wsp:PolicyReference> .. </..> elements
			Iterator policyRefElements = messageElement
					.getChildrenWithName(new QName(POLICY_NS_URI,
							TAG_POLICY_REF));

			if (policyRefElements != null) {
				processPolicyRefElements(policyRefElements, message.getPolicySubject());
			}

			processParameters(parameters, message, operation);

		}
	}

	/**
	 * Gets the list of modules that is required to be engaged globally.
	 * 
	 * @param moduleRefs
	 *            <code>java.util.Iterator</code>
	 * @throws DeploymentException
	 *             <code>DeploymentException</code>
	 */
	protected void processModuleRefs(Iterator moduleRefs)
			throws DeploymentException {
//		try {
			while (moduleRefs.hasNext()) {
				OMElement moduleref = (OMElement) moduleRefs.next();
				OMAttribute moduleRefAttribute = moduleref
						.getAttribute(new QName(TAG_REFERENCE));

				if (moduleRefAttribute != null) {
					String refName = moduleRefAttribute.getAttributeValue();
                    service.addModuleref(refName);
//					if (axisConfig.getModule(refName) == null) {
//						throw new DeploymentException(Messages.getMessage(
//								DeploymentErrorMsgs.MODULE_NOT_FOUND, refName));
//					} else {
//						service.addModuleref(refName);
//					}
				}
			}
//		} catch (AxisFault axisFault) {
//			throw new DeploymentException(axisFault);
//		}
	}

	protected void processOperationModuleConfig(Iterator moduleConfigs,
			ParameterInclude parent, AxisOperation operation)
			throws DeploymentException {
		while (moduleConfigs.hasNext()) {
			OMElement moduleConfig = (OMElement) moduleConfigs.next();
			OMAttribute moduleName_att = moduleConfig.getAttribute(new QName(
					ATTRIBUTE_NAME));

			if (moduleName_att == null) {
				throw new DeploymentException(Messages
						.getMessage(DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
			} else {
				String module = moduleName_att.getAttributeValue();
				ModuleConfiguration moduleConfiguration = new ModuleConfiguration(
						module, parent);
				Iterator parameters = moduleConfig
						.getChildrenWithName(new QName(TAG_PARAMETER));

				processParameters(parameters, moduleConfiguration, parent);
				operation.addModuleConfig(moduleConfiguration);
			}
		}
	}

	private ArrayList<AxisOperation> processOperations(Iterator operationsIterator)
			throws AxisFault {
		ArrayList<AxisOperation> operations = new ArrayList<AxisOperation>();
		while (operationsIterator.hasNext()) {
			OMElement operation = (OMElement) operationsIterator.next();
			// getting operation name
			OMAttribute op_name_att = operation.getAttribute(new QName(
					ATTRIBUTE_NAME));
			if (op_name_att == null) {
				throw new DeploymentException(Messages.getMessage(Messages
						.getMessage(DeploymentErrorMsgs.INVALID_OP,
								"operation name missing")));
			}

			// setting the MEP of the operation
			OMAttribute op_mep_att = operation.getAttribute(new QName(TAG_MEP));
			String mepurl = null;

			if (op_mep_att != null) {
				mepurl = op_mep_att.getAttributeValue();
			}

			String opname = op_name_att.getAttributeValue();
			AxisOperation op_descrip = null;

			// getting the namesapce from the attribute.
			OMAttribute operationNamespace = operation.getAttribute(new QName(
					ATTRIBUTE_NAMESPACE));
			if (operationNamespace != null) {
				String namespace = operationNamespace.getAttributeValue();
				op_descrip = service.getOperation(new QName(namespace, opname));
			}
			if (op_descrip == null) {
				op_descrip = service.getOperation(new QName(opname));
			}

			if (op_descrip == null) {
				op_descrip = service.getOperation(new QName(service
						.getTargetNamespace(), opname));
			}
			if (op_descrip == null) {
				if (mepurl == null) {
					// assumed MEP is in-out
					op_descrip = new InOutAxisOperation();
					op_descrip.setParent(service);

				} else {
					op_descrip = AxisOperationFactory
							.getOperationDescription(mepurl);
				}
				op_descrip.setName(new QName(opname));
				String MEP = op_descrip.getMessageExchangePattern();
				if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
						|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
						|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
						|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
						|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
						|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
					AxisMessage inaxisMessage = op_descrip
							.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
					if (inaxisMessage != null) {
						inaxisMessage.setName(opname
								+ Java2WSDLConstants.MESSAGE_SUFFIX);
					}
				}

				if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
						|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
						|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
						|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
						|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
					AxisMessage outAxisMessage = op_descrip
							.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
					if (outAxisMessage != null) {
						outAxisMessage.setName(opname
								+ Java2WSDLConstants.RESPONSE);
					}
				}
			}

			// setting the PolicyInclude

			// processing <wsp:Policy> .. </..> elements
			Iterator policyElements = operation.getChildrenWithName(new QName(
					POLICY_NS_URI, TAG_POLICY));

			if (policyElements != null && policyElements.hasNext()) {
				processPolicyElements(policyElements, op_descrip.getPolicySubject());
			}

			// processing <wsp:PolicyReference> .. </..> elements
			Iterator policyRefElements = operation
					.getChildrenWithName(new QName(POLICY_NS_URI,
							TAG_POLICY_REF));

			if (policyRefElements != null && policyRefElements.hasNext()) {
				processPolicyRefElements(policyRefElements, op_descrip.getPolicySubject());
			}

			// Operation Parameters
			Iterator parameters = operation.getChildrenWithName(new QName(
					TAG_PARAMETER));
			processParameters(parameters, op_descrip, service);
			// To process wsamapping;
			processActionMappings(operation, op_descrip);

			// loading the message receivers
			OMElement receiverElement = operation
					.getFirstChildWithName(new QName(TAG_MESSAGE_RECEIVER));

			if (receiverElement != null) {
				MessageReceiver messageReceiver = loadMessageReceiver(service
						.getClassLoader(), receiverElement);

				op_descrip.setMessageReceiver(messageReceiver);
			} else {
				// setting default message receiver
				MessageReceiver msgReceiver = loadDefaultMessageReceiver(
						op_descrip.getMessageExchangePattern(), service);
				op_descrip.setMessageReceiver(msgReceiver);
			}

			// Process Module Refs
			Iterator modules = operation.getChildrenWithName(new QName(
					TAG_MODULE));

			processOperationModuleRefs(modules, op_descrip);

			// processing Messages
			Iterator messages = operation.getChildrenWithName(new QName(
					TAG_MESSAGE));

			processMessages(messages, op_descrip);

			// setting Operation phase
			if (axisConfig != null) {
				PhasesInfo info = axisConfig.getPhasesInfo();

				info.setOperationPhases(op_descrip);
			}
			Iterator moduleConfigs = operation.getChildrenWithName(new QName(
					TAG_MODULE_CONFIG));
			processOperationModuleConfig(moduleConfigs, op_descrip, op_descrip);
			// adding the operation
			operations.add(op_descrip);
		}
		return operations;
	}

	protected void processServiceModuleConfig(Iterator moduleConfigs,
			ParameterInclude parent, AxisService service)
			throws DeploymentException {
		while (moduleConfigs.hasNext()) {
			OMElement moduleConfig = (OMElement) moduleConfigs.next();
			OMAttribute moduleName_att = moduleConfig.getAttribute(new QName(
					ATTRIBUTE_NAME));

			if (moduleName_att == null) {
				throw new DeploymentException(Messages
						.getMessage(DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
			} else {
				String module = moduleName_att.getAttributeValue();
				ModuleConfiguration moduleConfiguration = new ModuleConfiguration(
						module, parent);
				Iterator parameters = moduleConfig
						.getChildrenWithName(new QName(TAG_PARAMETER));

				processParameters(parameters, moduleConfiguration, parent);
				service.addModuleConfig(moduleConfiguration);
			}
		}
	}

	/*
	 * process data locator configuration for data retrieval.
	 */
	private void processDataLocatorConfig(OMElement dataLocatorElement,
			AxisService service) {
		OMAttribute serviceOverallDataLocatorclass = dataLocatorElement
				.getAttribute(new QName(DRConstants.CLASS_ATTRIBUTE));
		if (serviceOverallDataLocatorclass != null) {
			String className = serviceOverallDataLocatorclass
					.getAttributeValue();
			service.addDataLocatorClassNames(DRConstants.SERVICE_LEVEL,
					className);
		}
		Iterator iterator = dataLocatorElement.getChildrenWithName(new QName(
				DRConstants.DIALECT_LOCATOR_ELEMENT));

		while (iterator.hasNext()) {
			OMElement locatorElement = (OMElement) iterator.next();
			OMAttribute dialect = locatorElement.getAttribute(new QName(
					DRConstants.DIALECT_ATTRIBUTE));
			OMAttribute dialectclass = locatorElement.getAttribute(new QName(
					DRConstants.CLASS_ATTRIBUTE));
			service.addDataLocatorClassNames(dialect.getAttributeValue(),
					dialectclass.getAttributeValue());

		}

	}

	public void setWsdlServiceMap(HashMap<String,AxisService> wsdlServiceMap) {
		this.wsdlServiceMap = wsdlServiceMap;
	}

	private void processEndpoints(AxisService axisService) throws AxisFault {
		String endpointName = axisService.getEndpointName();
		if (endpointName == null || endpointName.length() == 0) {
			Utils.addEndpointsToService(axisService, service.getAxisConfiguration());
		}
	}
	
	private void processPolicyAttachments(OMElement serviceElement,
                                          AxisService service) throws DeploymentException {
		Iterator attachmentElements =
                serviceElement.getChildrenWithName(new QName(POLICY_NS_URI, TAG_POLICY_ATTACHMENT));
		try {
			Utils.processPolicyAttachments(attachmentElements, service);
		} catch (Exception e) {
			throw new DeploymentException(e);
		}
	}

    private void startupServiceLifecycle() {
        if (service.getServiceLifeCycle() != null) {
            service.getServiceLifeCycle().startUp(configCtx, service);
        }
    }
}
