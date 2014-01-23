/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.server.admin.module.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.server.admin.internal.ServerAdminDataHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class is responsible for creating CarbonContexts for for all non-servlet transports.
 */
public class CarbonContextCreatorHandler extends AbstractHandler {
    private static Log log = LogFactory.getLog(CarbonContextCreatorHandler.class);

    public Handler.InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        // If cc is already populated ( servlet transport ) just return
        // Here we are checking the availability of the tenant domain or tenant ID. There are scenarios where only
        // the tenant ID is available and there are cases where only the tenant domain is available.
        // We need to refactor the code base to use only the tenant Domain where necessary.
        if (carbonContext.getTenantDomain() != null ||
                carbonContext.getTenantId() != MultitenantConstants.INVALID_TENANT_ID) {
            return InvocationResponse.CONTINUE;
        }

        // For non-http we assume that it's for ST
        // This is the only way to check whether a given transport is non-http at the moment.
        if (messageContext.getTransportIn() != null && messageContext.getTransportIn().getName() != null &&
                !messageContext.getTransportIn().getName().contains("http")) {
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            return InvocationResponse.CONTINUE;
        }

        EndpointReference epr = messageContext.getTo();
        if (epr != null &&
                epr.getAddress() != null &&
                epr.getAddress().contains("/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/")) {

            String tenantDomain = null;
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            try {
                String toAddress = epr.getAddress();
                String temp = toAddress.substring(
                        toAddress.indexOf("/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/") + 3);
                tenantDomain = temp.substring(0, temp.indexOf("/"));
                carbonContext.setTenantDomain(tenantDomain, true);
            } catch (Exception e) {
                String msg = "Failed to populate the CarbonContext for tenant whose tenantDomain :" +
                        tenantDomain + " and tenantID: " + tenantId;
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        }

        return InvocationResponse.CONTINUE;
    }
}