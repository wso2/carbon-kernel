/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.core.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.multitenancy.MultitenantMessageReceiver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Arrays;
import java.util.List;

/**
 * This service component is responsible for loading the `tenantOutClientService` Axis2 service and two operations for
 * that service which is used in sending the message out in tenant mode.
 * The Two operations are In-Out axis operation and Out-Only operation. The operation context for
 * the individual requests will be set dynamically in the TenantTransportSender.invoke() method depending on the
 * incoming msgContext message exchange pattern. Same operation will be shared only with the similar MEPs.
 */
@Component(name = "org.wso2.carbon.core.internal.MultitenantTransportOutServiceComponent", immediate = true)
public class MultitenantTransportOutServiceComponent {

    private static final Log log = LogFactory.getLog(MultitenantTransportOutServiceComponent.class);
    private ConfigurationContext configCtx;

    protected void activate(ComponentContext context) {
        try {
            deployMultiTenantClientOutService(configCtx.getAxisConfiguration());
        } catch (AxisFault axisFault) {
            log.error("Failed to activate the MultitenantTransportOutServiceComponent", axisFault);
        }
    }

    protected void deactivate(ComponentContext context) {
    }

    /**
     * In this method, We create a new Axis2 service named `tenantClientService` with two operations OUT_ONLY_OPERATION
     * and IN_OUT_OPERATION. This
     * service is added to the super tenant's configuration context. This service is used by TenantTransportSender
     * when sending messages out from the axis engine.
     * server.
     *
     * @param axisCfg Super Tenant's axis configurations
     * @throws AxisFault
     */
    private void deployMultiTenantClientOutService(AxisConfiguration axisCfg) throws AxisFault {
        AxisServiceGroup superTenantSenderServiceGroup = new AxisServiceGroup(axisCfg);
        superTenantSenderServiceGroup.setServiceGroupName(MultitenantConstants.MULTITENANT_CLIENT_OUT_SERVICE);
        AxisService superTenantSenderClientService = new AxisService(MultitenantConstants.MULTITENANT_CLIENT_OUT_SERVICE);
        superTenantSenderClientService.addParameter(CarbonConstants.HIDDEN_SERVICE_PARAM_NAME, "true");
        superTenantSenderServiceGroup.addService(superTenantSenderClientService);

        superTenantSenderClientService.addOperation(new InOutAxisOperation(
                MultitenantConstants.MULTITENANT_CLIENT_SERVICE_IN_OUT_OPERATION));
        superTenantSenderClientService.getOperation(MultitenantConstants.MULTITENANT_CLIENT_SERVICE_IN_OUT_OPERATION)
                .setMessageReceiver(new MultitenantMessageReceiver());

        superTenantSenderClientService.addOperation(new OutOnlyAxisOperation(
                MultitenantConstants.MULTITENANT_CLIENT_SERVICE_OUT_ONLY_OPERATION));
        superTenantSenderClientService.getOperation(MultitenantConstants.MULTITENANT_CLIENT_SERVICE_OUT_ONLY_OPERATION)
                .setMessageReceiver(new MultitenantMessageReceiver());
        axisCfg.addServiceGroup(superTenantSenderServiceGroup);

        superTenantSenderClientService.setClientSide(true);
        List exposedTransportList = Arrays.asList("http","https","local");
        superTenantSenderClientService.setExposedTransports(exposedTransportList);
        if (log.isDebugEnabled()) {
            log.debug("Deployed " + MultitenantConstants.MULTITENANT_CLIENT_OUT_SERVICE);
        }
    }

    @Reference(name = "org.wso2.carbon.configCtx", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContext")
    protected void setConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = configCtx.getServerConfigContext();
    }

    protected void unsetConfigurationContext(ConfigurationContextService configCtx) {
        this.configCtx = null;
    }

}
