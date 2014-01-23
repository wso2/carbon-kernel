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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;

/**
 * An AxisBindingOperation represents a WSDL &lt;bindingOperation&gt;
 */
public class AxisBindingOperation extends AxisDescription {

	private AxisOperation axisOperation;

	private QName name;

	private Map<String, AxisBindingMessage> faults;

	private Map<String, Object> options;

	public AxisBindingOperation() {
		options = new HashMap<String, Object>();
		faults = new HashMap<String, AxisBindingMessage>();
	}

	public ArrayList<AxisBindingMessage> getFaults() {
		return new ArrayList<AxisBindingMessage>(faults.values());
	}

	public AxisBindingMessage getFault(String name) {
		return (AxisBindingMessage) faults.get(name);
	}

	public void addFault(AxisBindingMessage fault) {
		this.faults.put(fault.getName(), fault);
	}

	public QName getName() {
		return name;
	}

	public void setName(QName name) {
		this.name = name;
	}

	public AxisOperation getAxisOperation() {
		return axisOperation;
	}

	public void setAxisOperation(AxisOperation axisOperation) {
		this.axisOperation = axisOperation;
	}

	public void setProperty(String name, Object value) {
		options.put(name, value);
	}

	public Object getProperty(String name) {
		Object property = this.options.get(name);

		AxisBinding parent;
		if (property == null && (parent = getAxisBinding()) != null) {
			property = parent.getProperty(name);
		}

		if (property == null) {
			property = WSDL20DefaultValueHolder.getDefaultValue(name);
		}

		return property;
	}

	public Object getKey() {
		return name;
	}

	public void engageModule(AxisModule axisModule) throws AxisFault {
		throw new UnsupportedOperationException("Sorry we do not support this");
	}

	public boolean isEngaged(String moduleName) {
		throw new UnsupportedOperationException(
				"axisMessage.isEngaged() is not supported");

	}

	/**
	 * Generates the bindingOperation element
	 * 
	 * @param wsdl
	 *            The WSDL namespace
	 * @param tns
	 *            The targetnamespace
	 * @param wsoap
	 *            The SOAP namespace (WSDL 2.0)
	 * @param whttp
	 *            The HTTP namespace (WSDL 2.0)
	 * @param type
	 *            Indicates whether the binding is SOAP or HTTP
	 * @param namespaceMap
	 *            the service's namespace map (prefix -> namespace)
	 * @param serviceName
	 *            the name of the service
	 * @return The generated binding element
	 */
	public OMElement toWSDL20(OMNamespace wsdl, OMNamespace tns,
			OMNamespace wsoap, OMNamespace whttp, String type,
			Map<String, String> namespaceMap, String serviceName) {
		String property;
		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		OMElement bindingOpElement = omFactory.createOMElement(
				WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
		bindingOpElement.addAttribute(omFactory.createOMAttribute(
				WSDL2Constants.ATTRIBUTE_REF, null, tns.getPrefix() + ":"
						+ this.name.getLocalPart()));

		if (WSDL2Constants.URI_WSDL2_SOAP.equals(type)
				|| Constants.URI_SOAP11_HTTP.equals(type)
				|| Constants.URI_SOAP12_HTTP.equals(type)) {
			// SOAP Binding specific properties
			property = (String) this.options
					.get(WSDL2Constants.ATTR_WSOAP_ACTION);
			if (property != null) {
				bindingOpElement.addAttribute(omFactory.createOMAttribute(
						WSDL2Constants.ATTRIBUTE_ACTION, wsoap, property));
			}
			ArrayList soapModules = (ArrayList) this.options
					.get(WSDL2Constants.ATTR_WSOAP_MODULE);
			if (soapModules != null && soapModules.size() > 0) {
				WSDLSerializationUtil.addSOAPModuleElements(omFactory,
						soapModules, wsoap, bindingOpElement);
			}
			property = (String) this.options.get(WSDL2Constants.ATTR_WSOAP_MEP);
			if (property != null) {
				bindingOpElement.addAttribute(omFactory.createOMAttribute(
						WSDL2Constants.ATTRIBUTE_MEP, wsoap, property));
			}
		} else if (WSDL2Constants.URI_WSDL2_HTTP.equals(type)) {

			// HTTP Binding specific properties
			property = (String) this.options
					.get(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION);
			if (property != null) {
				bindingOpElement.addAttribute(omFactory.createOMAttribute(
						WSDL2Constants.ATTRIBUTE_INPUT_SERIALIZATION, whttp,
						property));
			}
			property = (String) this.options
					.get(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION);
			if (property != null) {
				bindingOpElement.addAttribute(omFactory.createOMAttribute(
						WSDL2Constants.ATTRIBUTE_OUTPUT_SERIALIZATION, whttp,
						property));
			}
			property = (String) this.options
					.get(WSDL2Constants.ATTR_WHTTP_FAULT_SERIALIZATION);
			if (property != null) {
				bindingOpElement.addAttribute(omFactory.createOMAttribute(
						WSDL2Constants.ATTRIBUTE_FAULT_SERIALIZATION, whttp,
						property));
			}
			Boolean ignoreUncited = (Boolean) this.options
					.get(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED);
			if (ignoreUncited != null) {
				bindingOpElement.addAttribute(omFactory.createOMAttribute(
						WSDL2Constants.ATTRIBUTE_IGNORE_UNCITED, whttp,
						ignoreUncited.toString()));
			}
			property = (String) this.options
					.get(WSDL2Constants.ATTR_WHTTP_METHOD);
			if (property != null) {
				bindingOpElement.addAttribute(omFactory.createOMAttribute(
						WSDL2Constants.ATTRIBUTE_METHOD, whttp, property));
			}
		}

		// Common properties
		property = (String) this.options
				.get(WSDL2Constants.ATTR_WHTTP_LOCATION);
		if (property != null) {
			bindingOpElement.addAttribute(omFactory.createOMAttribute(
					WSDL2Constants.ATTRIBUTE_LOCATION, whttp, property));
		}
		property = (String) this.options
				.get(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING);
		if (property != null) {
			bindingOpElement
					.addAttribute(omFactory.createOMAttribute(
							WSDL2Constants.ATTRIBUTE_CONTENT_ENCODING, whttp,
							property));
		}
		property = (String) this.options
				.get(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
		if (property != null) {
			bindingOpElement.addAttribute(omFactory.createOMAttribute(
					WSDL2Constants.ATTRIBUTE_QUERY_PARAMETER_SEPERATOR, whttp,
					property));
		}
		
		// Add the input element
		AxisBindingMessage inMessage = (AxisBindingMessage) this
				.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
		if (inMessage != null) {
			bindingOpElement.addChild(inMessage.toWSDL20(wsdl, tns, wsoap,
					whttp, namespaceMap));
		}

		// Add the output element
		AxisBindingMessage outMessage = (AxisBindingMessage) this
				.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
		if (outMessage != null) {
			bindingOpElement.addChild(outMessage.toWSDL20(wsdl, tns, wsoap,
					whttp, namespaceMap));
		}

		// Add any fault elements
		if (faults != null && faults.size() > 0) {
			Collection<AxisBindingMessage> faultValues = faults.values();
			Iterator<AxisBindingMessage> iterator = faultValues.iterator();
			while (iterator.hasNext()) {
				AxisBindingMessage faultMessage = (AxisBindingMessage) iterator
						.next();
				bindingOpElement.addChild(faultMessage.toWSDL20(wsdl, tns,
						wsoap, whttp, namespaceMap));
			}
		}
		WSDLSerializationUtil.addWSDLDocumentationElement(this,
				bindingOpElement, omFactory, wsdl);
		WSDLSerializationUtil.addPoliciesAsExtensibleElement(this,
				bindingOpElement);
		return bindingOpElement;
	}

	public Policy getEffectivePolicy() {
		ArrayList<PolicyComponent> policyList = new ArrayList<PolicyComponent>();

		PolicyInclude policyInclude;

		// AxisBindingOperation policies
		policyInclude = getPolicyInclude();
		policyList.addAll(policyInclude.getAttachedPolicies());

		// AxisBinding
		AxisBinding axisBinding = getAxisBinding();
		if (axisBinding != null) {
			policyList.addAll(axisBinding.getPolicyInclude()
					.getAttachedPolicies());
		}

		// AxisEndpoint
		AxisEndpoint axisEndpoint = null;
		if (axisBinding != null) {
			axisEndpoint = axisBinding.getAxisEndpoint();
		}

		if (axisEndpoint != null) {
			policyList.addAll(axisEndpoint.getPolicyInclude()
					.getAttachedPolicies());
		}

		// AxisOperation
		Policy axisOperationPolicy = axisOperation.getPolicyInclude()
				.getEffectivePolicy();

		if (axisOperationPolicy != null) {
			policyList.add(axisOperationPolicy);
		}

		return PolicyUtil.getMergedPolicy(policyList, this);
	}

	public AxisBinding getAxisBinding() {
		return (AxisBinding) parent;
	}
}
