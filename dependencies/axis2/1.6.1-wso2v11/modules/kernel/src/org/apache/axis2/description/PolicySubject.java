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

import org.apache.axiom.util.UIDGenerator;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PolicySubject {

	private boolean updated = false;
	private Date lastUpdatedTime = new Date();
	
	private ConcurrentHashMap<String, PolicyComponent> attachedPolicyComponents = new ConcurrentHashMap<String, PolicyComponent>();

	public void attachPolicy(Policy policy) {
		String key = policy.getName();
		if (key == null) {
			key = policy.getId();
			if (key == null) {
				key = UIDGenerator.generateUID();
				policy.setId(key);
			}
		}
		attachPolicyComponent(key, policy);
	}

	public void attachPolicyReference(PolicyReference reference) {
		attachedPolicyComponents.put(reference.getURI(), reference);
		setLastUpdatedTime(new Date()); 
	}

	public void attachPolicyComponents(List<PolicyComponent> policyComponents) {
		for (Iterator<PolicyComponent> iterator = policyComponents.iterator(); iterator
				.hasNext();) {
			attachPolicyComponent((PolicyComponent) iterator.next());
		}
	}

	public void attachPolicyComponent(PolicyComponent policyComponent) {
		if (policyComponent instanceof Policy) {
			attachPolicy((Policy) policyComponent);
		} else if (policyComponent instanceof PolicyReference) {
			attachPolicyReference((PolicyReference) policyComponent);
		} else {
			throw new IllegalArgumentException(
					"Invalid top level policy component type");
		}

	}

	public void attachPolicyComponent(String key,
			PolicyComponent policyComponent) {
		attachedPolicyComponents.put(key, policyComponent);
		setLastUpdatedTime(new Date());
		
		if (!isUpdated()) {
			setUpdated(true);
		}
	}

	public PolicyComponent getAttachedPolicyComponent(String key) {
		return (PolicyComponent) attachedPolicyComponents.get(key);

	}

	public Collection<PolicyComponent> getAttachedPolicyComponents() {
		return attachedPolicyComponents.values();
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public void updatePolicy(Policy policy) {
		String key = (policy.getName() != null) ? policy.getName() : policy
				.getId();
		if (key == null) {
			throw new IllegalArgumentException(
					"policy doesn't have a name or an id ");
		}
		attachedPolicyComponents.put(key, policy);
		setLastUpdatedTime(new Date());
		
		if (!isUpdated()) {
			setUpdated(true);
		}
	}

	public void detachPolicyComponent(String key) {
		attachedPolicyComponents.remove(key);
		setLastUpdatedTime(new Date());
		if (!isUpdated()) {
			setUpdated(true);
		}
	}

	public void clear() {
		attachedPolicyComponents.clear();
		setLastUpdatedTime(new Date());
		if (!isUpdated()) {
			setUpdated(true);
		}
	}

	public Date getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	public void setLastUpdatedTime(Date lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}
}
