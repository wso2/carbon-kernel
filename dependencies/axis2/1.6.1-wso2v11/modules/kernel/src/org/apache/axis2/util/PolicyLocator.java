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
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.dataretrieval.Data;
import org.apache.axis2.dataretrieval.DataRetrievalException;
import org.apache.axis2.dataretrieval.DataRetrievalRequest;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.PolicyRegistry;

public class PolicyLocator implements PolicyRegistry {

	private static final Log logger = LogFactory.getLog(PolicyLocator.class);

	private AxisService service;

	public PolicyLocator(AxisService service) {
		this.service = service;
	}

	public Policy lookup(String identifier) {
		Policy policy = service.lookupPolicy(identifier);
		if (policy == null) {
			try {
				MessageContext msgContext = new MessageContext();
				msgContext.setAxisService(service);

				DataRetrievalRequest request = new DataRetrievalRequest();
				request.putDialect(DRConstants.SPEC.DIALECT_TYPE_POLICY);
				request.putIdentifier(identifier);

				Data[] data = service.getData(request, msgContext);
				if (data.length != 0) {
					OMElement element = (OMElement) data[0].getData();
					
					if (element != null) {
						return PolicyEngine.getPolicy(element);
					}
				}
				
			} catch (DataRetrievalException ex) {
				logger.error("" + ex);
			} catch (AxisFault ex) {
				logger.error("" + ex);
			}
		}
		return policy;
	}

	public void register(String identifier, Policy policy) {
		throw new UnsupportedOperationException();

	}

	public void remove(String identifier) {
		throw new UnsupportedOperationException();

	}
}
