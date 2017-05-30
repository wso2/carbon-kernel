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

import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.provisional.p2.director.PlannerStatus;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox .p2.planner.IPlanner;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.director.RequestStatus;
import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox. p2.engine.IProfile;
import org.eclipse.equinox. p2.engine.ProvisioningContext;
import org.eclipse.equinox. p2.metadata.IInstallableUnit;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.ResolutionResult;
import org.wso2.carbon.feature.mgt.core.internal.ServiceHolder;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public abstract class ProfileChangeOperation {

    private String actionType;

    /**
     * Indicates that the size is currently unknown
     */
    public static final long SIZE_UNKNOWN = -1L;

    /**
     * Indicates that the size is unavailable (an
     * attempt was made to compute size but it failed)
     */
    public static final long SIZE_UNAVAILABLE = -2L;

    /**
     * Indicates that there was nothing to size (there
     * was no valid plan that could be used to compute
     * size).
     */
    public static final long SIZE_NOTAPPLICABLE = -3L;

    protected IInstallableUnit[] iusToInstall = new IInstallableUnit[]{};

    public IInstallableUnit[] getIusToInstall() {
        return Arrays.copyOf(iusToInstall, iusToInstall.length);
    }

    public void setIusToInstall(IInstallableUnit[] iusToInstall) {
        this.iusToInstall = Arrays.copyOf(iusToInstall, iusToInstall.length);
    }

    public IInstallableUnit[] getIusToUninstall() {
        return Arrays.copyOf(iusToUninstall, iusToUninstall.length);
    }

    public void setIusToUninstall(IInstallableUnit[] iusToUninstall) {
        this.iusToUninstall = Arrays.copyOf(iusToUninstall, iusToUninstall.length);
    }

	protected IInstallableUnit[] iusToUninstall = new IInstallableUnit[]{};

    protected long timestamp;

    public ProfileChangeOperation(String actionType) {
        this.actionType = actionType;
    }

    public ResolutionResult reviewProfileChangeAction(IProfile profile) throws ProvisioningException {
        MultiStatus initialStatus = getInitialStatus();
        ProfileChangeRequest profileChangeRequest = generateProfileChangeRequest(
                profile, initialStatus, null);
        IProvisioningPlan provisioningPlan = generateProvisioningPlan(profileChangeRequest, ServiceHolder.getProvisioningContext());
        if (provisioningPlan == null) {
            throw new ProvisioningException("Failed to generate the Provisioning Plan");
        }

        return generateResolutionResult(profileChangeRequest, provisioningPlan, initialStatus);
    }

     private IProvisioningPlan generateProvisioningPlan(ProfileChangeRequest request, ProvisioningContext context)
            throws ProvisioningException {
        IPlanner planner = ServiceHolder.getPlanner();
        return planner.getProvisioningPlan(request, context, new NullProgressMonitor());
    }

    public ResolutionResult generateResolutionResult(ProfileChangeRequest originalRequest,
                                                     IProvisioningPlan plan,
                                                     MultiStatus originalStatus)
            throws ProvisioningException {
        IEngine engine = ServiceHolder.getP2Engine();

        ResolutionResult report = new ResolutionResult();
        report.setProvisioningPlan(plan);

        if (nothingToDo(originalRequest)) {
            report.addSummaryStatus(new Status(IStatus.ERROR, "temp", 10050,
                    "Cannot complete the request.  See the error log for details.", null));
            IStatus[] details = originalStatus.getChildren();
            for (IStatus detail : details) {
                report.addSummaryStatus(detail);
            }
            return report;
        }

        // If there was already some status supplied before resolution, this should get included
        // with the report.
        if (originalStatus != null && originalStatus.getChildren().length > 0) {
            report.addSummaryStatus(originalStatus);
        }

        // If the overall plan had a non-OK status, capture that in the report.
        if (!plan.getStatus().isOK()) {
            report.addSummaryStatus(plan.getStatus());
        }
        PlannerStatus plannerStatus = plan.getStatus() instanceof PlannerStatus ? (PlannerStatus) plan.getStatus() : null;
		// If there is no additional plannerStatus details just return the report
		if (plannerStatus == null) {
			return report;
		}

        // Now we compare what was requested with what is going to happen.
        if (plan.getStatus().getSeverity() != IStatus.ERROR) {
            Collection<IInstallableUnit> iusAdded = originalRequest.getAdditions();
            for (IInstallableUnit added : iusAdded) {
                RequestStatus rs = plannerStatus.getRequestChanges().get(added);
                if(rs != null){
                	if (rs.getSeverity() == IStatus.ERROR) {
                		// This is a serious error so it must also appear in the overall status
                		IStatus fail = new Status(IStatus.ERROR, "temp", 10011, added.getId() +
                		                          " is not applicable to the current configuration and will not be installed.", null);
                		report.addStatus(added, fail);
                		report.addSummaryStatus(fail);
                		report.addFailedInstallableUnit(added);
                	} else {
                    report.addReviewedInstallableUnit(added);
                	}
              }
            }
            Collection<IInstallableUnit> iusRemoved = originalRequest.getRemovals();
            for (IInstallableUnit removed : iusRemoved) {
                RequestStatus rs = plannerStatus.getRequestChanges().get(removed);
                if(rs != null){
                	if (rs.getSeverity() == IStatus.ERROR) {
                        // We are making assumptions here about why the planner chose to ignore an uninstall.
                        IStatus fail = new Status(IStatus.INFO, "temp", 10007, removed.getId() +
                                " cannot be fully uninstalled because other installed software requires it.  " +
                                "The parts that are not required will be uninstalled.", null);
                        report.addStatus(removed, fail);
                        report.addSummaryStatus(fail);
                        report.addFailedUninstallableUnit(removed);
                    } else {
                        report.addReviewedUninstallableUnit(removed);
                    }
                }
                
            }
        }

     // Now process the side effects
		/*Map sideEffects = plannerStatus.getRequestSideEffects();
		for (Object o : sideEffects.entrySet()) {
			IInstallableUnit iu = (IInstallableUnit) o;
			RequestStatus rs = (RequestStatus) sideEffects.get(iu);
			if (rs.getInitialRequestType() == RequestStatus.ADDED) {
				report.addStatus(iu,
				                 new Status(
				                            rs.getSeverity(),
				                            "temp",
				                            10010,
				                            iu.getId() +
				                                    " will also be installed in order to complete this operation.",
				                            null));
				report.addReviewedInstallableUnit(iu);
			} else {
				report.addStatus(iu,
				                 new Status(
				                            rs.getSeverity(),
				                            "temp",
				                            10009,
				                            iu.getId() +
				                                    " must be uninstalled in order to complete this operation.",
				                            null));
				report.addReviewedUninstallableUnit(iu);
			}
		}*/

        long size = 0;
        if (report.getReviewedInstallableUnits().length != 0) {
            size = getSize(plan, plan.getProfile(), engine, ServiceHolder.getProvisioningContext(), null);
        }
        report.setInstallationSize(getFormattedSize(size));
        return report;
    }

    public String getActionType() {
        return actionType;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public abstract ProfileChangeRequest generateProfileChangeRequest(IProfile profile,
                                                                      MultiStatus status, IProgressMonitor monitor);

    public abstract long getSize(
            IProvisioningPlan plan, IProfile profile, IEngine engine,
            ProvisioningContext context, IProgressMonitor monitor);

    public abstract MultiStatus getInitialStatus();



    private static boolean nothingToDo(ProfileChangeRequest request) {
        return request.getAdditions().size() == 0 && request.getRemovals().size() == 0 &&
                request.getInstallableUnitProfilePropertiesToAdd().size() == 0 &&
                request.getInstallableUnitProfilePropertiesToRemove().size() == 0;
    }

    private String getFormattedSize(long size) {
        if (size == SIZE_UNKNOWN || size == SIZE_UNAVAILABLE) {
            return "Unknown";
        }
        if (size > 1000L) {
            long kb = size / 1000L;
            return NumberFormat.getInstance().format(kb) + " kb";
        }
        return NumberFormat.getInstance().format(size);
    }
}
