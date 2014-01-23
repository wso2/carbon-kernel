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

package org.apache.axis2.clustering.state.commands;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public class UpdateServiceStateCommand extends UpdateStateCommand {

    private static final Log log = LogFactory.getLog(UpdateServiceStateCommand.class);

    protected String serviceGroupName;
    protected String serviceGroupContextId;
    protected String serviceName;

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setServiceGroupContextId(String serviceGroupContextId) {
        this.serviceGroupContextId = serviceGroupContextId;
    }

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Updating service context properties...");
        }
        ServiceGroupContext sgCtx =
                configurationContext.getServiceGroupContext(serviceGroupContextId);
        if (sgCtx != null) {
            try {
                AxisService axisService =
                        configurationContext.getAxisConfiguration().getService(serviceName);
                validateAxisService(axisService);
                ServiceContext serviceContext = sgCtx.getServiceContext(axisService);
                propertyUpdater.updateProperties(serviceContext);
            } catch (AxisFault e) {
                throw new ClusteringFault(e);
            }
        } else {
            sgCtx = configurationContext.getServiceGroupContext(serviceGroupContextId);
            AxisService axisService;
            try {
                axisService = configurationContext.getAxisConfiguration().getService(serviceName);
            } catch (AxisFault axisFault) {
                throw new ClusteringFault(axisFault);
            }
            validateAxisService(axisService);
            String scope = axisService.getScope();
            if (sgCtx == null) {
                AxisServiceGroup serviceGroup =
                        configurationContext.getAxisConfiguration().getServiceGroup(serviceGroupName);
                if(serviceGroup == null){
                    return;
                }
                sgCtx = new ServiceGroupContext(configurationContext, serviceGroup);
                sgCtx.setId(serviceGroupContextId);
                if (scope.equals(Constants.SCOPE_APPLICATION)) {
                    configurationContext.
                            addServiceGroupContextIntoApplicationScopeTable(sgCtx);
                } else if (scope.equals(Constants.SCOPE_SOAP_SESSION)) {
                    configurationContext.
                            addServiceGroupContextIntoSoapSessionTable(sgCtx);
                }
            }
            try {
                ServiceContext serviceContext = sgCtx.getServiceContext(axisService);
                propertyUpdater.updateProperties(serviceContext);
            } catch (AxisFault axisFault) {
                throw new ClusteringFault(axisFault);
            }
        }
    }

    private void validateAxisService(AxisService axisService) throws ClusteringFault {
        if (axisService == null){
            String msg = "Service " + serviceName + " not found";
            log.error(msg);
            throw new ClusteringFault(msg);
        }
    }

    public String toString() {
        return "UpdateServiceStateCommand";
    }
}
