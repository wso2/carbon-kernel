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
import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.p2.engine.ProvisioningPlan;
import org.eclipse.equinox.internal.simpleconfigurator.utils.SimpleConfiguratorConstants;
import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.internal.p2.engine.phases.Sizing;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.planner.ProfileInclusionRules;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.wso2.carbon.feature.mgt.core.internal.ServiceHolder;
import org.wso2.carbon.feature.mgt.core.util.SizingPhaseSet;

import java.io.File;
import java.util.Collection;

public class InstallOperation extends ProfileChangeOperation {

    public InstallOperation(String actionType) {
        super(actionType);
    }

    public ProfileChangeRequest generateProfileChangeRequest(IProfile profile, MultiStatus status,
                                                             IProgressMonitor monitor) {
        ProfileChangeRequest request = new ProfileChangeRequest(profile);
        String carbonHome = System.getProperty("carbon.home");
        String cacheLocation = carbonHome + File.separator + "repository" + File.separator + "components";
        request.setProfileProperty(IProfile.PROP_CACHE,cacheLocation);
        request.setProfileProperty(SimpleConfiguratorConstants.PROP_KEY_USE_REFERENCE,Boolean.TRUE.toString());
        for (IInstallableUnit iu : iusToInstall) {
            // If the user is installing a patch, we mark it optional.  This allows
            // the patched IU to be updated later by removing the patch.
            if (Boolean.toString(true).equals(iu.getProperty(MetadataFactory.InstallableUnitDescription.PROP_TYPE_PATCH))) {
                request.setInstallableUnitInclusionRules(iu, ProfileInclusionRules.createOptionalInclusionRule(iu));
            }

            // Check to see if it is already installed.  This may alter the request.
            Collection alreadyInstalled = profile.query(QueryUtil.createIUQuery(iu.getId()),new NullProgressMonitor()).toUnmodifiableSet();

            if (alreadyInstalled.size() > 0) {
                IInstallableUnit installedIU = (IInstallableUnit) alreadyInstalled.iterator().next();
                int compareTo = iu.getVersion().compareTo(installedIU.getVersion());
                // If the iu is a newer version of something already installed, consider this an
                // update request
                if (compareTo > 0) {
                    boolean lockedForUpdate = false;
                    String value = profile.getInstallableUnitProperty(installedIU,
                            IProfile.PROP_PROFILE_LOCKED_IU);
                    if (value != null) {
                        lockedForUpdate = (Integer.parseInt(value) & IProfile.LOCK_UPDATE) ==
                                IProfile.LOCK_UPDATE;
                    }
                    if (lockedForUpdate) {
                        // Add a status telling the user that this implies an update, but the
                        // iu should not be updated
                        status.merge(new Status(IStatus.WARNING, "temp", 10013, installedIU.getId() + "-" +
                                installedIU.getVersion() +
                                " will be ignored because it is already installed, " +
                                "and updates are not permitted.", null));
                    } else {
                        request.addInstallableUnits(new IInstallableUnit[]{iu});
                        request.removeInstallableUnits(new IInstallableUnit[]{installedIU});
                        // Add a status informing the user that the update has been inferred
                        status.merge(new Status(IStatus.WARNING, "temp", 10013, installedIU.getId() + "-" +
                                installedIU.getVersion() +
                                " is already installed, so an update will be performed instead.", null));
                        // Mark it as a root if it hasn't been already
                        if (!Boolean.toString(true).equals(profile.getInstallableUnitProperty(installedIU,
                                IProfile.PROP_PROFILE_ROOT_IU))) {
                            request.setInstallableUnitProfileProperty(iu, IProfile.PROP_PROFILE_ROOT_IU,
                                    Boolean.toString(true));
                        }
                    }
                } else if (compareTo < 0) {
                    // An implied downgrade.  We will not put this in the plan, add a status informing the user
                    status.merge(new Status(IStatus.WARNING, "temp", 10004, installedIU.getId() + "-" +
                            installedIU.getVersion() +
                            " will be ignored because a newer version is already installed.", null));
                } else {
                    if (Boolean.toString(true).equals(profile.getInstallableUnitProperty(installedIU,
                            IProfile.PROP_PROFILE_ROOT_IU)))
                    // It is already a root, nothing to do. We tell the user it was already installed
                    {
                        status.merge(new Status(IStatus.WARNING, "temp", 10005, installedIU.getId() + "-" +
                                installedIU.getVersion() + " will be ignored because it is already installed.", null));
                    } else {
                        // It was already installed but not as a root.
                        // Tell the user that parts of it are already installed and mark it as a root.
                        status.merge(new Status(IStatus.WARNING, "temp", 10006, installedIU.getId() + "-" +
                                installedIU.getVersion() +
                                " is already present because other installed software requires it.  " +
                                "It will be added to the installed software list.", null));
                        request.setInstallableUnitProfileProperty(iu, "org.eclipse.equinox.p2.type.root",
                                Boolean.toString(true));
                    }
                }
            } else {
                //install this if only this is not category type
            	if(!Boolean.toString(true).equals(iu.getProperty(MetadataFactory.InstallableUnitDescription.PROP_TYPE_CATEGORY))) {
            		// Install it and mark as a root
                    request.addInstallableUnits(new IInstallableUnit[]{iu});
                    request.setInstallableUnitProfileProperty(iu, "org.eclipse.equinox.p2.type.root",
                            Boolean.toString(true));
            	}
            }
        }
        return request;
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
            IStatus status = engine.perform(provisioningPlan,set, null);
            if (status.isOK()) {
                installPlanSize = set.getSizing().getDiskSize();
            }
        }
        SizingPhaseSet set = new SizingPhaseSet(new Sizing(100));
        IStatus status = engine.perform(provisioningPlan, set,null);
        if (status.isOK()) {
            return installPlanSize + set.getSizing().getDiskSize();
        }
        return SIZE_UNAVAILABLE;
    }

    public MultiStatus getInitialStatus() {
        return new MultiStatus(ServiceHolder.ID, 10001, "Your original install request has been modified.", null);
    }
}
