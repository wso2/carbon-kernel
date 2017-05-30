/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.deployment;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ClusterMessage for sending a deployment repository synchronization request
 */
public class SynchronizeRepositoryRequest extends ClusteringMessage {

    private transient static final Log log = LogFactory.getLog(SynchronizeRepositoryRequest.class);
    private int tenantId;
    private String  tenantDomain;

    public SynchronizeRepositoryRequest() {
    }

    public SynchronizeRepositoryRequest(int tenantId, String tenantDomain) {
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        log.info("Received [" + this + "] ");
        // Run only if the tenant is loaded
        Parameter parameter = null;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            parameter = configContext.getAxisConfiguration().
                    getParameter(CarbonDeploymentSchedulerTask.REPO_UPDATE_REQUIRED);
        } else if (TenantAxisUtils.getTenantConfigurationContexts(configContext).
                get(tenantDomain) != null) {   // if tenant is loaded
            parameter = TenantAxisUtils.getTenantConfigurationContexts(configContext).
                    get(tenantDomain).getAxisConfiguration().
                    getParameter(CarbonDeploymentSchedulerTask.REPO_UPDATE_REQUIRED);
        }
        log.debug("Updating repo update required parameter");
        if (parameter != null) {
            ((AtomicBoolean) parameter.getValue()).compareAndSet(false, true);
        }

    }

    public ClusteringCommand getResponse(){
        return null;
    }


    @Override
    public String toString() {
        return "SynchronizeRepositoryRequest{" +
               "tenantId=" + tenantId +
               ", tenantDomain='" + tenantDomain + '\'' +
               ", messageId=" + getUuid() +
               '}';
    }

    /**
     * Two messages are considered equal if the tenant ID & domain are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SynchronizeRepositoryRequest)) return false;
        if (!super.equals(o)) return false;
        SynchronizeRepositoryRequest that = (SynchronizeRepositoryRequest) o;
        return tenantId == that.tenantId && tenantDomain.equals(that.tenantDomain);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + tenantId;
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }
}
