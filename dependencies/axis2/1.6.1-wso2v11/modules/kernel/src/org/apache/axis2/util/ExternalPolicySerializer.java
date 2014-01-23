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

import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ExternalPolicySerializer {

    private List assertions2Filter = new ArrayList();

    public void addAssertionToFilter(QName name) {
        assertions2Filter.add(name);
    }

    public void setAssertionsToFilter(List assertions2Filter) {
        this.assertions2Filter = assertions2Filter;
    }

    public List getAssertionsToFilter() {
        return assertions2Filter;
    }


    public void serialize(Policy policy, OutputStream os) {

        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(os);
            policy = (Policy) policy.normalize(false);

            String wspPrefix = writer.getPrefix(Constants.URI_POLICY_NS);

			if (wspPrefix == null) {
				wspPrefix = Constants.ATTR_WSP;
				writer.setPrefix(wspPrefix, Constants.URI_POLICY_NS);
			}

			String wsuPrefix = writer.getPrefix(Constants.URI_WSU_NS);
			if (wsuPrefix == null) {
				wsuPrefix = Constants.ATTR_WSU;
				writer.setPrefix(wsuPrefix, Constants.URI_WSU_NS);
			}

			writer.writeStartElement(wspPrefix, Constants.ELEM_POLICY,
					Constants.URI_POLICY_NS);

			QName key;

			String prefix = null;
			String namespaceURI = null;
			String localName = null;

			HashMap prefix2ns = new HashMap();

			for (Iterator iterator = policy.getAttributes().keySet().iterator(); iterator
					.hasNext();) {

				key = (QName) iterator.next();
				localName = key.getLocalPart();

				namespaceURI = key.getNamespaceURI();
				namespaceURI = (namespaceURI == null || namespaceURI.length() == 0) ? null
						: namespaceURI;

				if (namespaceURI != null) {

					String writerPrefix = writer.getPrefix(namespaceURI);
					writerPrefix = (writerPrefix == null || writerPrefix
							.length() == 0) ? null : writerPrefix;

					if (writerPrefix == null) {
						prefix = key.getPrefix();
						prefix = (prefix == null || prefix.length() == 0) ? null
								: prefix;

					} else {
						prefix = writerPrefix;
					}

					if (prefix != null) {
						writer.writeAttribute(prefix, namespaceURI, localName,
								policy.getAttribute(key));
						prefix2ns.put(prefix, key.getNamespaceURI());

					} else {
						writer.writeAttribute(namespaceURI, localName, policy
								.getAttribute(key));
					}

				} else {
					writer.writeAttribute(localName, policy.getAttribute(key));
				}

			}

			// writes xmlns:wsp=".."
			writer.writeNamespace(wspPrefix, Constants.URI_POLICY_NS);

			String prefiX;

			for (Iterator iterator = prefix2ns.keySet().iterator(); iterator
					.hasNext();) {
				prefiX = (String) iterator.next();
				writer.writeNamespace(prefiX, (String) prefix2ns.get(prefiX));
			}

            writer.writeStartElement(Constants.ATTR_WSP,
                                     Constants.ELEM_EXACTLYONE, Constants.URI_POLICY_NS);
            // write <wsp:ExactlyOne>

            List assertionList;

            for (Iterator iterator = policy.getAlternatives(); iterator
                    .hasNext();) {

                assertionList = (List) iterator.next();

                // write <wsp:All>
                writer.writeStartElement(Constants.ATTR_WSP, Constants.ELEM_ALL,
                                         Constants.URI_POLICY_NS);

                Assertion assertion;

                for (Iterator assertions = assertionList.iterator(); assertions
                        .hasNext();) {
                    assertion = (Assertion) assertions.next();
                    if (assertions2Filter.contains(assertion.getName())) {
                        // since this is an assertion to filter, we will not serialize this
                        continue;
                    }
                    assertion.serialize(writer);
                }

                // write </wsp:All>
                writer.writeEndElement();
            }

            // write </wsp:ExactlyOne>
            writer.writeEndElement();
            // write </wsp:Policy>
            writer.writeEndElement();

            writer.flush();

        } catch (Exception ex) {

            throw new RuntimeException(ex);

        }
    }
}
