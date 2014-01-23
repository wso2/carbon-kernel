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
import org.apache.axis2.util.AxisPolicyLocator;
import org.apache.axis2.util.PolicyUtil;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.PolicyRegistry;
import org.apache.neethi.PolicyRegistryImpl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class PolicyInclude {

    public static final int ANON_POLICY = 100;

    public static final int AXIS_POLICY = 1;

    public static final int AXIS_MODULE_POLICY = 2;
    
    public static final int AXIS_MODULE_OPERATION_POLICY = 17;

    public static final int AXIS_SERVICE_POLICY = 3;

    public static final int AXIS_OPERATION_POLICY = 4;

    public static final int AXIS_MESSAGE_POLICY = 5;

    public static final int SERVICE_POLICY = 6;

    public static final int PORT_POLICY = 7;

    public static final int PORT_TYPE_POLICY = 8;

    public static final int BINDING_POLICY = 9;

    public static final int OPERATION_POLICY = 10;

    public static final int BINDING_OPERATION_POLICY = 11;

    public static final int INPUT_POLICY = 12;

    public static final int OUTPUT_POLICY = 13;

    public static final int BINDING_INPUT_POLICY = 14;

    public static final int BINDING_OUTPUT_POLICY = 15;

    public static final int MESSAGE_POLICY = 16;

    private Policy policy = null;

    private Policy effectivePolicy = null;

    private PolicyRegistry reg;

    private AxisDescription description;

    private Hashtable<String, Wrapper> wrapperElements = new Hashtable<String, Wrapper>();

    public PolicyInclude() {
        reg = new PolicyRegistryImpl();
    }

    public PolicyInclude(AxisDescription axisDescription) {

        if (axisDescription.getParent() != null) {
            PolicyInclude parentPolicyInclude = axisDescription.getParent().getPolicyInclude();
            reg = new PolicyRegistryImpl(parentPolicyInclude.getPolicyRegistry());
        } else {
            reg = new PolicyRegistryImpl();
        }
        setDescription(axisDescription);
    }

    public void setPolicyRegistry(PolicyRegistry reg) {
        this.reg = reg;
    }

    public PolicyRegistry getPolicyRegistry() {
        return reg;
    }

    /**
	 * @param policy
	 * @see org.apache.axis2.description.PolicySubject#attachPolicy(Policy)
	 * @see org.apache.axis2.description.PolicySubject#clear()
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #attachPolicy(Policy)} Use
	 *             {@link PolicySubject #clear()} beforehand effective policy of
	 *             {@link AxisDescription} has to be set as the argument.
	 * 
	 */
    public void setPolicy(Policy policy) {
        wrapperElements.clear();

        if (policy.getName() == null && policy.getId() == null) {
            policy.setId(UIDGenerator.generateUID());
        }

        Wrapper wrapper = new Wrapper(PolicyInclude.ANON_POLICY, policy);
        if (policy.getName() != null) {
            wrapperElements.put(policy.getName(), wrapper);
        } else {
            wrapperElements.put(policy.getId(), wrapper);
        }
        
        if (description != null) {
			description.getPolicySubject().clear();
			description.getPolicySubject().attachPolicy(policy);
		}
    }

    /**
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #updatePolicy(Policy)}.
	 */
    public void updatePolicy(Policy policy) {
        String key;

        if ((key = policy.getName()) == null && (key = policy.getId()) == null) {
            // TODO throw more meaningful exception ..
            throw new RuntimeException("policy doesn't have a name or an id ");
        }

        Wrapper wrapper = (Wrapper) wrapperElements.get(key);
        wrapper.value = policy;
        
        if (description != null) {
			description.getPolicySubject().updatePolicy(policy);
		}
    }

    /**
	 * @deprecated As of 1.4 release. You can't override a policies that
	 *             applicable for the current policy scope via
	 *             {@link PolicyInclude #setEffectivePolicy(Policy)}. In case
	 *             you need to make a policy the only policy that is within the
	 *             policy cache of an {@link AxisDescription} please use
	 *             {@link PolicySubject #clear()} and
	 *             {@link PolicySubject #attachPolicy(Policy)} accordingly.
	 * 
	 */
    public void setEffectivePolicy(Policy effectivePolicy) {
        this.effectivePolicy = effectivePolicy;
        
        if (description != null && effectivePolicy != null) {
			description.getPolicySubject().clear();
			description.getPolicySubject().attachPolicy(effectivePolicy);
		}
    }

    public void setDescription(AxisDescription description) {
        this.description = description;
    }

    public AxisDescription getDescription() {
        return description;
    }

    private PolicyInclude getParent() {

        if (description != null && description.getParent() != null) {
            return description.getParent().getPolicyInclude();
        }
        return null;
    }

    private void calculatePolicy() {

        Policy result = null;
        Iterator<Wrapper> iterator = wrapperElements.values().iterator();

        while (iterator.hasNext()) {
            Object policyElement = ((Wrapper) iterator.next()).getValue();
            Policy p;

            if (policyElement instanceof PolicyReference) {
                AxisPolicyLocator locator = new AxisPolicyLocator(description);
                p = (Policy) ((PolicyReference) policyElement)
                        .normalize(locator, false);

            } else if (policyElement instanceof Policy) {
                p = (Policy) policyElement;

            } else {
                // TODO AxisFault?
                throw new RuntimeException();
            }

            result = (result == null) ? (Policy) p : (Policy) result.merge(p);
        }

        this.policy = result;
    }

    private void calculateEffectivePolicy() {
        Policy result;

        if (getParent() != null) {
            Policy parentPolicy = getParent().getEffectivePolicy();

            if (parentPolicy == null) {
                result = getPolicy();

            } else {

                if (getPolicy() != null) {
                    result = (Policy) parentPolicy.merge(getPolicy());

                } else {
                    result = parentPolicy;
                }
            }

        } else {
            result = getPolicy();
        }
        setEffectivePolicy(result);
    }

    /**
	 * @deprecated As of 1.4 release. If you need to calculate merged policy of
	 *             all policies that are in the policy cache of
	 *             {@link AxisDescription}, use
	 *             {@link PolicySubject #getAttachedPolicyComponents() and {@link org.PolicyUtil #getMergedPolicy(List, AxisDescription)}}
	 */
    public Policy getPolicy() {
    	if (description != null) {
			ArrayList<PolicyComponent> policyList = new ArrayList<PolicyComponent>(description.getPolicySubject()
					.getAttachedPolicyComponents());
			return PolicyUtil.getMergedPolicy(policyList, description);
		}
    	
        calculatePolicy();
        return policy;
    }

    /**
	 * @deprecated As of 1.4 release. Use
	 *             {@link AxisMessage #getEffectivePolicy()} or
	 *             {@link AxisBindingMessage #getEffectivePolicy()} when
	 *             applicable.
	 */
    public Policy getEffectivePolicy() {
    	if (description != null) {
			if (description instanceof AxisMessage) {
				return ((AxisMessage) description).getEffectivePolicy();
			} else if (description instanceof AxisBindingMessage) {
				return ((AxisBindingMessage) description).getEffectivePolicy();
			}
		}
    	
        calculateEffectivePolicy();
        return effectivePolicy;
    }

    /**
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #getAttachedPolicyComponents()}
	 */
    public ArrayList getPolicyElements() {
    	if (description != null) {
			return new ArrayList<PolicyComponent>(description.getPolicySubject()
					.getAttachedPolicyComponents());
		}
    	
        ArrayList policyElementsList = new ArrayList();
        Iterator<Wrapper> policyElementIterator = wrapperElements.values().iterator();

        while (policyElementIterator.hasNext()) {
            policyElementsList
                    .add(policyElementIterator.next().getValue());
        }
        return policyElementsList;
    }

    /**
	 * @deprecated As of 1.4 release. The policy element type is no longer
	 *             required since we maintain a complete binding description
	 *             hierarchy for the static description the service. Hence use
	 *             {@link PolicySubject #getAttachedPolicyComponents()} on
	 *             appropriate description object.
	 */
    public ArrayList getPolicyElements(int type) {
        ArrayList policyElementList = new ArrayList();
        Iterator<Wrapper> wrapperElementIterator = wrapperElements.values().iterator();
        Wrapper wrapper;

        while (wrapperElementIterator.hasNext()) {
            wrapper = (Wrapper) wrapperElementIterator.next();

            if (wrapper.getType() == type) {
                policyElementList.add(wrapper.getValue());
            }
        }
        return policyElementList;
    }

    /**
	 * @deprecated As of 1.4 release. Use ServiceData.xml or Axis2 DataLocators
	 *             to configure policies that are stored separately.
	 */
    public void registerPolicy(String key, Policy policy) {
        reg.register(key, policy);
    }

    /**
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #getAttachedPolicyComponent(String)}
	 */
    public Policy getPolicy(String key) {
    	if (description != null) {
			PolicyComponent result = description.getPolicySubject()
					.getAttachedPolicyComponent(key);
			if (result != null && result instanceof Policy) {
				return (Policy) result;
			}
		}
    	
        return reg.lookup(key);
    }

    /**
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #attachPolicy(Policy)}
	 */
    public void addPolicyElement(int type, Policy policy) {

        String key;

        if ((key = policy.getName()) == null && (key = policy.getId()) == null) {
            policy.setId(UIDGenerator.generateUID());
        }

        key = (policy.getName() != null) ? policy.getName() : policy.getId();

        Wrapper wrapper = new Wrapper(type, policy);
        wrapperElements.put(key, wrapper);
        reg.register(key, policy);
        
        if (description != null) {
			description.getPolicySubject().attachPolicy(policy);
		}
    }

    /**
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #attachPolicyReference(PolicyReference)}
	 */
    public void addPolicyRefElement(int type, PolicyReference policyReference) {
        Wrapper wrapper = new Wrapper(type, policyReference);
        wrapperElements.put(policyReference.getURI(), wrapper);
        
        if (description != null) {
			description.getPolicySubject().attachPolicyReference(
					policyReference);
		}
    }

    class Wrapper {
        private int type;
        private Object value;

        Wrapper(int type, Object value) {
            setType(type);
            setValue(value);
        }

        void setType(int type) {
            this.type = type;
        }

        int getType() {
            return type;
        }

        void setValue(Object value) {
            this.value = value;
        }

        Object getValue() {
            return value;
        }
    }

    /**
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #detachPolicyComponent(String)}
	 */
    public void removePolicyElement(String policyURI) {
        wrapperElements.remove(policyURI);
        reg.remove(policyURI);
        
        if (description != null) {
			description.getPolicySubject().detachPolicyComponent(policyURI);
		}
    }

    /**
	 * @deprecated As of 1.4 release, replaced by {@link PolicySubject #clear()}
	 */
    public void removeAllPolicyElements() {
        wrapperElements.clear();
        
        if (description != null) {
			description.getPolicySubject().clear();
		}
    }
    
    /**
	 * @deprecated As of 1.4 release, replaced by
	 *             {@link PolicySubject #getAttachedPolicyComponents()}
	 */
    public List getAttachedPolicies() {
    	if (description != null) {
			return new ArrayList(description.getPolicySubject()
					.getAttachedPolicyComponents());
		}
    	
        ArrayList  arrayList = new ArrayList();
        
        for (Iterator iterator = wrapperElements.values().iterator(); iterator.hasNext();) {
            arrayList.add(((Wrapper) iterator.next()).value);
        }
        
        return arrayList;
    }
}
