/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.transport.msmq;

import java.util.HashSet;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.msmq.ctype.ContentTypeRuleSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class defines a MSMQ endpooint, it has the format
 */
public class MSMQEndpoint extends ProtocolEndpoint {

	private static final Log log = LogFactory.getLog(MSMQEndpoint.class);

	private final MSMQListener listener;
	private final WorkerPool workerPool;
	private boolean useTransaction = false;

	private ServiceTaskManager serviceTaskManager;
	private String msmqDestinationQueueName;

	public ContentTypeRuleSet getContentTypeRuleSet() {
		return contentTypeRuleSet;
	}

	public void setContentTypeRuleSet(ContentTypeRuleSet contentTypeRuleSet) {
		this.contentTypeRuleSet = contentTypeRuleSet;
	}

	private ContentTypeRuleSet contentTypeRuleSet;

	public boolean isUseTransaction() {
		return useTransaction;
	}

	public void setUseTransaction(boolean useTransaction) {
		this.useTransaction = useTransaction;
	}

	private Set<EndpointReference> endpointReferences = new HashSet<EndpointReference>();

	public String getMsmqDestinationQueueName() {
		return msmqDestinationQueueName;
	}

	public void setMsmqDestinationQueueName(String msmqDestinationQueueName) {
		this.msmqDestinationQueueName = msmqDestinationQueueName;
	}

	public ServiceTaskManager getServiceTaskManager() {
		return serviceTaskManager;
	}

	public void setServiceTaskManager(ServiceTaskManager serviceTaskManager) {
		this.serviceTaskManager = serviceTaskManager;
	}

	public MSMQEndpoint(MSMQListener listener, WorkerPool workerPool) {
		this.listener = listener;
		this.workerPool = workerPool;
	}

	@Override
	public boolean loadConfiguration(ParameterInclude params) {
		// only support endpoints configured at service level
		if (!(params instanceof AxisService)) {
			return false;
		}
		AxisService service = (AxisService) params;
		// we just assume that the service name==queue name
		Parameter destParam = service.getParameter(MSMQConstants.PARAM_DESTINATION);
		if (destParam != null) {
			msmqDestinationQueueName = (String) destParam.getValue();
		} else {
			msmqDestinationQueueName = service.getName();
		}

		endpointReferences.add(new EndpointReference(MSMQConnectionManager.getReceiverQueueFullName(getServiceName())));

		// TODO: improve MSMQ transport for two way messaging..
		Parameter contentTypeParam = service.getParameter(MSMQConstants.PARAM_CONTENT_TYPE);
		// TODO: deal with content type
		serviceTaskManager = ServiceTaskManagerFactory.createTaskManagerForService(service, workerPool);
		serviceTaskManager.setMsmqMessageReceiver(new MSMQMessageReceiver(listener, msmqDestinationQueueName, this));

		return true;
	}

	@Override
	public EndpointReference[] getEndpointReferences(AxisService service, String ip) throws AxisFault {
		return endpointReferences.toArray(new EndpointReference[endpointReferences.size()]);
	}

}
