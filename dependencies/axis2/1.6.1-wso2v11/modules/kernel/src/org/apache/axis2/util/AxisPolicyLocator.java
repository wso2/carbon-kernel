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

import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.PolicySubject;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyRegistry;

public class AxisPolicyLocator implements PolicyRegistry {

    AxisDescription subject = null;
    
    private static final short AXIS_BINDING_MESSAGE = 1;
    private static final short AXIS_MESSAGE = 2;
    private static final short AXIS_BINDING_OPERATION = 3;
    private static final short AXIS_OPERATION = 4;
    private static final short AXIS_BINDING = 5;
    private static final short AXIS_ENDPOINT = 6;
    private static final short AXIS_SERVICE = 7;
    private static final short AXIS_SERVICE_GROUP = 8;
    private static final short AXIS_CONFIGURATION = 9;
    
    
    
    public AxisPolicyLocator(AxisDescription subject) {
        this.subject = subject;
    }

    public Policy lookup(String key) {
        if (subject == null) {
            return null;
        }
        
        Policy policy = null;
        
        PolicySubject policySubject = subject.getPolicySubject();
		PolicyComponent attachedPolicyComponent = policySubject
				.getAttachedPolicyComponent(key);

		if (attachedPolicyComponent != null
				&& attachedPolicyComponent instanceof Policy) {
			policy = (Policy) attachedPolicyComponent;
			if (policy != null) {
				return policy;
			}
		}
		
        
		if (subject instanceof AxisService) {
			policy = ((AxisService) subject).lookupPolicy(key);
			if (policy != null) {
				return policy;
			}
		}
		
        
        short type = getType(subject);
        
        /*
         * processing the parallel level
         */
        AxisDescription parallelLevel = null;
        
        switch (type) {
        case AXIS_BINDING_MESSAGE:
            parallelLevel = ((AxisBindingMessage) subject).getAxisMessage();
            break;
        case AXIS_BINDING_OPERATION:
            parallelLevel = ((AxisBindingOperation) subject).getAxisOperation();
            break;
        default:
            break;
        }
        
        if (parallelLevel != null) {
            policy = (new AxisPolicyLocator(parallelLevel)).lookup(key);
            if (policy != null) {
                return policy;
            }
        }
        
        AxisDescription upperLevel = getUpperLevel(type, subject);
        if (upperLevel != null) {
            policy = (new AxisPolicyLocator(upperLevel)).lookup(key);
            return policy;
        }                
        
        return null;
    }
    
    public void register(String key, Policy policy) {
        throw new UnsupportedOperationException();
    }

    public void remove(String key) {
        throw new UnsupportedOperationException();
    }
    
    
    
    private AxisDescription getUpperLevel(short type, AxisDescription thisLevel) {
        
        switch (type) {
        case AXIS_BINDING_MESSAGE:
            return ((AxisBindingMessage) thisLevel).getAxisBindingOperation();
        case AXIS_BINDING_OPERATION:
            return ((AxisBindingOperation) thisLevel).getAxisBinding();
        case AXIS_BINDING:
            return ((AxisBinding) thisLevel).getAxisEndpoint();
        case AXIS_ENDPOINT:
            return ((AxisEndpoint) thisLevel).getAxisService();
        case AXIS_MESSAGE:
        	return ((AxisMessage) thisLevel).getAxisOperation();
        case AXIS_OPERATION:
        	return ((AxisOperation) thisLevel).getAxisService();
        case AXIS_SERVICE:
            return ((AxisService) thisLevel).getAxisServiceGroup();
        case AXIS_SERVICE_GROUP:
            return ((AxisServiceGroup) thisLevel).getAxisConfiguration();
        default:
            return null;
        }
    }
    
    private short getType(AxisDescription description) {
        
        if (description instanceof AxisBindingMessage) {
            return AXIS_BINDING_MESSAGE;
        } else if (description instanceof AxisMessage) {
            return AXIS_MESSAGE;
        } else if (description instanceof AxisBindingOperation) {
            return AXIS_BINDING_OPERATION;
        } else if (description instanceof AxisOperation) {
            return AXIS_OPERATION;
        } else if (description instanceof AxisBinding) {
            return AXIS_BINDING;
        } else if (description instanceof AxisEndpoint) {
            return AXIS_ENDPOINT;
        } else if (description instanceof AxisService) {
            return AXIS_SERVICE;
        } else if (description instanceof AxisServiceGroup) {
            return AXIS_SERVICE_GROUP;
        } else if (description instanceof AxisConfiguration) {
            return AXIS_CONFIGURATION;
        }
        
        return -1;
        
    }
}
