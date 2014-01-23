/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.feature.mgt.core.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.p2.engine.ProvisioningPlan;
import org.eclipse.equinox.internal.p2.engine.phases.Sizing;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.ResolutionResult;
import org.wso2.carbon.feature.mgt.core.internal.ServiceHolder;
import org.wso2.carbon.feature.mgt.core.util.ProvisioningUtils;
import org.wso2.carbon.feature.mgt.core.util.SizingPhaseSet;

public class RevertOperation extends ProfileChangeOperation {
    public RevertOperation(String actionType) {
        super(actionType);
    }

    public ResolutionResult reviewProfileChangeAction(IProfile profile) throws ProvisioningException {
        IProvisioningPlan plan;
        IPlanner planner;
        IProfile snapshot = ProvisioningUtils.getProfile(IProfileRegistry.SELF, timestamp);
        if (snapshot == null) {
            throw new ProvisioningException("Invalid Profile Configuration");
        }

        planner = ServiceHolder.getPlanner();
        plan = planner.getDiffPlan(profile, snapshot, new NullProgressMonitor());
       
        ProfileChangeRequest profileChangeRequest = generateProfileChangeRequest(profile, getInitialStatus(), null);    
        if(plan.getAdditions() != null){
        	IInstallableUnit[] planAdditions = plan.getAdditions().query(QueryUtil.createIUAnyQuery(),
        	                                                             new NullProgressMonitor()).toArray(IInstallableUnit.class);
        	profileChangeRequest.addInstallableUnits(planAdditions);
        }
        if(plan.getRemovals() != null){
        	IInstallableUnit[] planRemovals = plan.getRemovals().query(QueryUtil.createIUAnyQuery(),
                                                                       new NullProgressMonitor()).toArray(IInstallableUnit.class);
        	profileChangeRequest.removeInstallableUnits(planRemovals);
        }
              
        if (plan == null) {
            throw new ProvisioningException("Failed to generate the Provisioning Plan");
        }
        
        //TODO : wrong!! figure out a way to get profileModificationRequest from the plan
        //return generateResolutionResult(new ProfileChangeRequest(profile), plan, getInitialStatus());       
        return generateResolutionResult(profileChangeRequest, plan, getInitialStatus());
    }

    public ProfileChangeRequest generateProfileChangeRequest(IProfile profile, MultiStatus status,
                                                             IProgressMonitor monitor) {
    	return new ProfileChangeRequest(profile);
    }

    public long getSize(IProvisioningPlan provisioningPlan, IProfile profile, IEngine engine, ProvisioningContext context,
                        IProgressMonitor monitor) {
    	// If there is nothing to size, return 0
        if (provisioningPlan == null) {
            return SIZE_NOTAPPLICABLE;
        }
        if (((ProvisioningPlan)provisioningPlan).getOperands().length == 0) {
            return 0;
        }
        long installPlanSize = 0;
        if (provisioningPlan.getInstallerPlan() != null) {
            SizingPhaseSet set = new SizingPhaseSet(new Sizing(100));
            IStatus status = engine.perform(provisioningPlan, set, null);
            if (status.isOK()) {
                installPlanSize = set.getSizing().getDiskSize();
            }
        }
        SizingPhaseSet set = new SizingPhaseSet(new Sizing(100));
        IStatus status = engine.perform(provisioningPlan, set, null);
        if (status.isOK()) {
            return installPlanSize + set.getSizing().getDiskSize();
        }
        return SIZE_UNAVAILABLE;
    }

    public MultiStatus getInitialStatus() {
        return new MultiStatus(ServiceHolder.ID, 10001, "Your original revert request has been modified.", null);
    }
}
