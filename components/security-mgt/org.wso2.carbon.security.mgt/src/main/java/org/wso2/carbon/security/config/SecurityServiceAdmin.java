/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.persistence.file.ServiceGroupFilePersistenceManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.ServerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SecurityServiceAdmin {

    private static Log log = LogFactory.getLog(SecurityServiceAdmin.class);
    protected AxisConfiguration axisConfig = null;


    public SecurityServiceAdmin(AxisConfiguration config) throws ServerException {
        this.axisConfig = config;

    }

    public SecurityServiceAdmin(AxisConfiguration config, Registry registry) {
        this.axisConfig = config;
    }

    /**
     * This method add Policy to service at the Registry. Does not add the
     * policy to Axis2. To all Bindings available
     *
     * @param axisService Service
     * @param policy      Policy
     * @throws org.wso2.carbon.utils.ServerException se
     */
    public void addSecurityPolicyToAllBindings(AxisService axisService, Policy policy)
            throws ServerException {
        String serviceGroupId = axisService.getAxisServiceGroup().getServiceGroupName();
        try {
            if (policy.getId() == null) {
                policy.setId(UUIDGenerator.getUUID());
            }

            Map endPointMap = axisService.getEndpoints();
            for (Object o : endPointMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                AxisEndpoint point = (AxisEndpoint) entry.getValue();
                AxisBinding binding = point.getBinding();
                String bindingName = binding.getName().getLocalPart();

                //only UTOverTransport is allowed for HTTP
                if (bindingName.endsWith("HttpBinding") &&
                        (!policy.getAttributes().containsValue("UTOverTransport"))) {
                    continue;
                }
                binding.getPolicySubject().attachPolicy(policy);

            }
        } catch (Exception e) {
            log.error("Error in adding security policy to all bindings", e);
            throw new ServerException("addPoliciesToService", e);
        }
    }

    public void removeSecurityPolicyFromAllBindings(AxisService axisService, String uuid)
            throws ServerException {
        if (log.isDebugEnabled()) {
            log.debug("Removing  security policy from all bindings.");
        }
        Map endPointMap = axisService.getEndpoints();
        for (Object o : endPointMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            AxisEndpoint point = (AxisEndpoint) entry.getValue();
            AxisBinding binding = point.getBinding();
            if (binding.getPolicySubject().getAttachedPolicyComponent(uuid) != null) {
                binding.getPolicySubject().detachPolicyComponent(uuid);
            }
        }
    }

    public void setServiceParameterElement(String serviceName, Parameter parameter)
            throws AxisFault {
        AxisService axisService = axisConfig.getService(serviceName);

        if (axisService == null) {
            throw new AxisFault("Invalid service name '" + serviceName + "'");
        }

        Parameter p = axisService.getParameter(parameter.getName());
        if (p != null) {
            if (!p.isLocked()) {
                axisService.addParameter(parameter);
            }
        } else {
            axisService.addParameter(parameter);
        }

    }

}
