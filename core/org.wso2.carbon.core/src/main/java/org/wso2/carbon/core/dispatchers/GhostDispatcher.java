/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.core.dispatchers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.util.GhostDispatcherUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;

public class GhostDispatcher extends AbstractDispatcher {

    private static Log log = LogFactory.getLog(GhostDispatcher.class);

    private static final String NAME = "GhostDispatcher";

    public InvocationResponse invoke(MessageContext msgctx) throws AxisFault {
        // if the service is not already dispatched, try to find the service within ghost list
        if (msgctx.getAxisService() == null) {
            AxisService transitService = GhostDeployerUtils.dispatchServiceFromTransitGhosts(msgctx);
            if (transitService != null) {
                // if the service is found in the temp ghost list, we have to wait until the
                // particular actual service is deployed or unloaded..
                handleTransitGhostService(transitService, msgctx);
            }
            return InvocationResponse.CONTINUE;
        }
        // find the service and operation
        AxisService service = findService(msgctx);
        if (service != null) {
            findOperation(service, msgctx);
        }
        // set the last usage timestamp in the dispatched service
        if (msgctx.getAxisService() != null) {
            GhostDeployerUtils.updateLastUsedTime(msgctx.getAxisService());
        }
        return InvocationResponse.CONTINUE;
    }

    @Override
    public AxisOperation findOperation(AxisService service, MessageContext
            messageContext) {
        AxisOperation newOperation = null;
        if (service != null && messageContext.getAxisOperation() != null) {
            AxisOperation existingOperation = messageContext.getAxisOperation();
            newOperation = service.getOperation(existingOperation.getName());
            if (newOperation != null) {
                messageContext.setAxisOperation(newOperation);
            }
        }
        return newOperation;
    }

    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        AxisService dispatchedService = messageContext.getAxisService();
        AxisService newService = null;

        // check whether this is a ghost service
        if (GhostDeployerUtils.isGhostService(dispatchedService)) {
            String serviceGroupName = null;
            if (dispatchedService != null) {
                if (dispatchedService.getAxisServiceGroup() != null) {
                    serviceGroupName = dispatchedService.getAxisServiceGroup().getServiceGroupName();
                } else {
                    serviceGroupName = dispatchedService.getName();
                }
            }
            AxisConfiguration axisConfig = messageContext.getConfigurationContext().getAxisConfiguration();

            if (CarbonUtils.isDepSyncEnabled() && CarbonUtils.isWorkerNode() &&
                    GhostDeployerUtils.isPartialUpdateEnabled()) {
                GhostDispatcherUtils.handleDepSynchUpdate(axisConfig, dispatchedService);
            }

            try {
                newService = GhostDeployerUtils.deployActualService(axisConfig,
                                                                    dispatchedService);
//                if (axisConfig != null) {
//                    GhostDispatcherUtils.deployServiceMetaFile(serviceGroupName, axisConfig);
//                }
            } catch (AxisFault e) {
                log.error("Error deploying service. ", e);
                throw e;
            }
            if (newService != null) {
                messageContext.setAxisService(newService);
                // we have to remove the old binding message as well. Message context will
                // generate the new binding message when needed..
                messageContext.removeProperty(Constants.AXIS_BINDING_MESSAGE);
            }
        }
        return newService;
    }


    @Override
    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }

    private void findServiceAndOperation(String serviceName, MessageContext msgctx)
            throws AxisFault {
        AxisService newService = null;
        AxisConfiguration axisConfig = msgctx.getConfigurationContext().getAxisConfiguration();
        AxisService ghostService = axisConfig.getService(serviceName);
        // find the service and operation
        Parameter dispatchedGhostParam = ghostService
                .getParameter(CarbonConstants.GHOST_SERVICE_PARAM);
        if (dispatchedGhostParam != null && "true".equals(dispatchedGhostParam.getValue())) {
            newService = GhostDeployerUtils.deployActualService(axisConfig, ghostService);
            if (newService != null) {
                msgctx.setAxisService(newService);
                // we have to remove the old binding message as well. Message context will
                // generate the new binding message when needed..
                msgctx.removeProperty(Constants.AXIS_BINDING_MESSAGE);
            }
        }
        if (newService != null) {
            findOperation(newService, msgctx);
        }
        // set the last usage timestamp in the dispatched service
        if (msgctx.getAxisService() != null) {
            GhostDeployerUtils.updateLastUsedTime(msgctx.getAxisService());
        }
    }

    private void handleTransitGhostService(AxisService transitService, MessageContext msgctx)
            throws AxisFault {
        // if the service is found in the temp ghost list, we have to wait until the
        // particular actual service is deployed or unloaded..
        Parameter isUnloadingParam = transitService.
                getParameter(CarbonConstants.IS_ARTIFACT_BEING_UNLOADED);
        if (isUnloadingParam != null && "true".equals(isUnloadingParam.getValue())) {
            // wait until service is unloaded by the unload task
            GhostDeployerUtils.waitForServiceToLeaveTransit(transitService.getName(),
                                                            msgctx.getConfigurationContext().
                                                                    getAxisConfiguration());
            // now the service is unloaded and in ghost form so we can safely
            // continue with invocation
            findServiceAndOperation(transitService.getName(), msgctx);
        } else {
            // wait until service is deployed
            GhostDeployerUtils.waitForServiceToLeaveTransit(transitService.getName(),
                                                            msgctx.getConfigurationContext().
                                                                    getAxisConfiguration());
        }
    }
}

