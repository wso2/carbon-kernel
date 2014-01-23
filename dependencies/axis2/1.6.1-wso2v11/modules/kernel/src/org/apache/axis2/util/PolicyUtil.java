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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyReference;

import com.ibm.wsdl.util.xml.DOM2Writer;

public class PolicyUtil {

	public static String getSafeString(String unsafeString) {
		StringBuffer sbuf = new StringBuffer();

		char[] chars = unsafeString.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];

			switch (c) {
			case '\\':
				sbuf.append('\\');
				sbuf.append('\\');
				break;
			case '"':
				sbuf.append('\\');
				sbuf.append('"');
				break;
			case '\n':
				sbuf.append('\\');
				sbuf.append('n');
				break;
			case '\r':
				sbuf.append('\\');
				sbuf.append('r');
				break;
			default:
				sbuf.append(c);
			}
		}

		return sbuf.toString();
	}

	public static PolicyReference createPolicyReference(Policy policy) {
		PolicyReference policyReference = new PolicyReference();
		String key = policy.getName();
		if (key == null) {
			key = policy.getId();
			if (key == null) {
				key = UIDGenerator.generateUID();
				policy.setId(key);
			}
			policyReference.setURI("#" + key);
		} else {
			policyReference.setURI(key);
		}
		return policyReference;
	}

	public static OMElement getPolicyComponentAsOMElement(
			PolicyComponent policyComponent,
			ExternalPolicySerializer externalPolicySerializer)
			throws XMLStreamException, FactoryConfigurationError {

		if (policyComponent instanceof Policy) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			externalPolicySerializer.serialize((Policy) policyComponent, baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos
					.toByteArray());
			return (OMElement) XMLUtils.toOM(bais);

		} else {
			OMFactory fac = OMAbstractFactory.getOMFactory();
			OMElement elem = fac.createOMElement(Constants.ELEM_POLICY_REF,
					Constants.URI_POLICY_NS, Constants.ATTR_WSP);
			elem.addAttribute(Constants.ATTR_URI,
					((PolicyReference) policyComponent).getURI(), null);
			return elem;
		}
	}

	public static OMElement getPolicyComponentAsOMElement(
			PolicyComponent component) throws XMLStreamException,
			FactoryConfigurationError {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance()
				.createXMLStreamWriter(baos);

		component.serialize(writer);
		writer.flush();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		return (OMElement) XMLUtils.toOM(bais);
	}

	public static PolicyComponent getPolicyComponentFromOMElement(
			OMElement policyComponent) throws IllegalArgumentException {

		if (Constants.Q_ELEM_POLICY.equals(policyComponent.getQName())) {
			return PolicyEngine.getPolicy(policyComponent);

		} else if (policyComponent.getQName().equals(
				new QName(Constants.URI_POLICY_NS, Constants.ELEM_POLICY_REF))) {
			return PolicyEngine.getPolicyReference(policyComponent);

		} else {
			throw new IllegalArgumentException(
					"Agrument is neither a <wsp:Policy> nor a <wsp:PolicyReference> element");
		}
	}

	public static Policy getPolicyFromOMElement(OMElement policyElement) {
		if (Constants.Q_ELEM_POLICY.equals(policyElement.getQName())) {
			return PolicyEngine.getPolicy(policyElement);
		} else {
			throw new IllegalArgumentException(
					"argument is not a <wsp:Policy ..> element");
		}
	}

	public static PolicyReference getPolicyReferenceFromOMElement(
			OMElement policyRefElement) {
		if (Constants.URI_POLICY_NS.equals(policyRefElement.getNamespace()
				.getNamespaceURI())
				&& Constants.ELEM_POLICY_REF.equals(policyRefElement
						.getLocalName())) {
			return PolicyEngine.getPolicyReference(policyRefElement);
		} else {
			throw new IllegalArgumentException(
					"argument is not a <wsp:PolicyReference> element");
		}
	}

	public static PolicyComponent getPolicyComponent(org.w3c.dom.Element element) {
		if (Constants.URI_POLICY_NS.equals(element.getNamespaceURI())) {

			if (Constants.ELEM_POLICY.equals(element.getLocalName())) {
				return PolicyEngine.getPolicy(nodeToStream(element));

			} else if (Constants.ELEM_POLICY_REF.equals(element.getLocalName())) {
				return PolicyEngine.getPolicyReferene(nodeToStream(element));
			}
		}
		throw new IllegalArgumentException(
				"Agrument is neither a <wsp:Policy> nor a <wsp:PolicyReference> element");
	}

	private static InputStream nodeToStream(org.w3c.dom.Element element) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Transformer tf;
		try {
			// tf = TransformerFactory.newInstance().newTransformer();
			// tf.transform(new DOMSource(element), new StreamResult(baos));
			String nodeString = DOM2Writer.nodeToString(element);
			return new ByteArrayInputStream(nodeString.getBytes());
		} catch (Exception e) {
			throw new RuntimeException("Unable to process policy");
		}
	}

	public static String policyComponentToString(PolicyComponent policyComponent)
			throws XMLStreamException, FactoryConfigurationError {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance()
				.createXMLStreamWriter(baos);

		policyComponent.serialize(writer);
		writer.flush();

		return baos.toString();
	}

	public static String generateId(AxisDescription description) {
		PolicyInclude policyInclude = description.getPolicyInclude();
		String identifier = "-policy-1";

		if (description instanceof AxisMessage) {
			identifier = "msg-" + ((AxisMessage) description).getName()
					+ identifier;
			description = description.getParent();
		}

		if (description instanceof AxisOperation) {
			identifier = "op-" + ((AxisOperation) description).getName()
					+ identifier;
			description = description.getParent();
		}

		if (description instanceof AxisService) {
			identifier = "service-" + ((AxisService) description).getName()
					+ identifier;
		}

		/*
		 * Int 49 is the value of the Character '1'. Here we want to change '1'
		 * to '2' or '2' to '3' .. etc. to construct a unique identifier.
		 */
		for (int index = 49; policyInclude.getPolicy(identifier) != null; index++) {
			identifier = identifier.replace((char) index, (char) (index + 1));
		}

		return identifier;
	}

	public static Policy getMergedPolicy(List policies,
			AxisDescription description) {

		Policy policy = null;

		for (Iterator iterator = policies.iterator(); iterator.hasNext();) {
			Object policyElement = iterator.next();
			if (policyElement instanceof Policy) {
				policy = (policy == null) ? (Policy) policyElement
						: (Policy) policy.merge((Policy) policyElement);

			} else {
				PolicyReference policyReference = (PolicyReference) policyElement;
				Policy policy2 = (Policy) policyReference.normalize(
						new AxisPolicyLocator(description), false);
				policy = (policy == null) ? policy2 : (Policy) policy
						.merge(policy2);
			}
		}

		if (policy != null) {
			policy = (Policy) policy.normalize(new AxisPolicyLocator(
					description), false);
		}

		return policy;
	}

	public static Policy getMergedPolicy(List policies, AxisService service) {

		Policy policy = null;

		for (Iterator iterator = policies.iterator(); iterator.hasNext();) {
			Object policyElement = iterator.next();
			if (policyElement instanceof Policy) {
				policy = (policy == null) ? (Policy) policyElement
						: (Policy) policy.merge((Policy) policyElement);

			} else {
				PolicyReference policyReference = (PolicyReference) policyElement;
				Policy policy2 = (Policy) policyReference.normalize(
						new PolicyLocator(service), false);
				policy = (policy == null) ? policy2 : (Policy) policy
						.merge(policy2);
			}
		}

		if (policy != null) {
			policy = (Policy) policy.normalize(new PolicyLocator(service),
					false);
		}

		return policy;
	}
}
