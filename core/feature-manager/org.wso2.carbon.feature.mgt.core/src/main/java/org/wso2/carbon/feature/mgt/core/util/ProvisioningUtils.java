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
package org.wso2.carbon.feature.mgt.core.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.director.PlanExecutionHelper;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.*;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.ResolutionResult;
import org.wso2.carbon.feature.mgt.core.internal.ServiceHolder;
import org.wso2.carbon.feature.mgt.core.operations.ProfileChangeOperation;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Contains utility method which can be used for Profile Management. i.e Install/Uninstall/Revert operation on the system,
 * Querying installed features
 */
public final class ProvisioningUtils {

    private ProvisioningUtils() {
    }

    /**
     * Review the given provisioning action
     *
     * @param profileModificationAction should contains the appropriate provActionType and features to be
     *                                  installed and feature to be uninstalled
     * @return an instance of ResolutionResult and it contains proposed ProvisioningPlan and the status.
     * Depending on this status, client can decide whether to proceed with the provisioning action.
     * @throws ProvisioningException if an exception occurs while reviewing the action
     */
    public static ResolutionResult reviewProvisioningAction(ProfileChangeOperation profileModificationAction)
            throws ProvisioningException {
        return profileModificationAction.reviewProfileChangeAction(ProvisioningUtils.getProfile());
    }

    /**
     * Performs the given ProvisioningPlan
     *
     * @param resolutionResult   an instance of ResolutionResult and it contains proposed ProvisioningPlan and the status.
     *                           Depending on this status, client can decide whether to proceed with the provisioning action.
     * @param applyConfiguration - if true, apply config without restarting
     * @throws ProvisioningException if an exception occurs while performing the action
     */
    public static void performProvisioningAction(ResolutionResult
                                                         resolutionResult, boolean applyConfiguration) throws ProvisioningException {
        int severity = resolutionResult.getSummaryStatus().getSeverity();
        if (severity != IStatus.ERROR) {
            IStatus status = performProvisioningAction(resolutionResult.getProvisioningPlan(),
                    applyConfiguration);
            if (status.getSeverity() == IStatus.ERROR) {
                ResolutionResult rs = new ResolutionResult();
                rs.addSummaryStatus(status);
                String summaryReport = rs.getSummaryReport();
                throw new ProvisioningException(summaryReport);
            }
        } else {
            throw new ProvisioningException(resolutionResult.getSummaryReport());
        }
    }

    public static void performProvisioningAction(ResolutionResult resolutionResult)
            throws ProvisioningException {
        performProvisioningAction(resolutionResult, false);
    }

    @Deprecated
    public static ILicense[] getLicensingInformation(IProvisioningPlan provisioningPlan) throws
            ProvisioningException, URISyntaxException {
        ArrayList<ILicense> licenseArrayList = new ArrayList<ILicense>();
        IInstallableUnit[] installableUnits = provisioningPlan.getAdditions().query(QueryUtil.createIUAnyQuery(),
                new NullProgressMonitor()).toArray(IInstallableUnit.class);
        boolean found;
        for (IInstallableUnit iu : installableUnits) {
            found = false;
            ILicense license = IUPropertyUtils.getLicense(iu);
            if (license != null) {
                for (ILicense addedLicense : licenseArrayList) {
                    if (addedLicense.equals(license)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    licenseArrayList.add(license);
                }
            }
        }

        return licenseArrayList.toArray(new ILicense[licenseArrayList.size()]);
    }

    /**
     * @return a Map contains the License and the features associated to it. If there features that do not have
     * license then it Map will have null key which those features.
     */
    public static Map<ILicense, List<IInstallableUnit>> getLicensingInformation(IInstallableUnit[] installableUnits)
            throws ProvisioningException, URISyntaxException {

        Map<ILicense, List<IInstallableUnit>> licenseFeatureMap = new HashMap<ILicense, List<IInstallableUnit>>();
        List<IInstallableUnit> iInstallableUnits;
        ILicense iLicense;
        for (IInstallableUnit iu : installableUnits) {
            iLicense = IUPropertyUtils.getLicense(iu);
            if (licenseFeatureMap.containsKey(iLicense)) {
                iInstallableUnits = licenseFeatureMap.get(iLicense);
                iInstallableUnits.add(iu);
            } else {
                iInstallableUnits = new ArrayList<IInstallableUnit>();
                iInstallableUnits.add(iu);
                licenseFeatureMap.put(iLicense, iInstallableUnits);
            }
        }
        return licenseFeatureMap;
    }

    public static IInstallableUnit[] getAllInstalledIUs() {


        IInstallableUnit[] groupPropertyTrueIUs = getProfile().query(QueryUtil.createIUPropertyQuery(
                        MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.TRUE.toString()),
                new NullProgressMonitor()).toArray(IInstallableUnit.class);


        IInstallableUnit[] groupPropertyFalseIUs = getProfile().query(QueryUtil.createIUPropertyQuery(
                        MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.FALSE.toString()),
                new NullProgressMonitor()).toArray(IInstallableUnit.class);

        IInstallableUnit[] allInstalledIUs =
                new IInstallableUnit[groupPropertyFalseIUs.length + groupPropertyTrueIUs.length];

        System.arraycopy(groupPropertyTrueIUs, 0, allInstalledIUs, 0, groupPropertyTrueIUs.length);
        System.arraycopy(groupPropertyFalseIUs, 0, allInstalledIUs, groupPropertyTrueIUs.length, groupPropertyFalseIUs.length);

        return allInstalledIUs;
    }

    /**
     * Returns an instance of the SELF Profile. A SELF Profile contains information about installed IUs in the system
     *
     * @return SELF profile
     */
    public static IProfile getProfile() {
        IProfileRegistry profileRegistry;
        try {
            profileRegistry = ServiceHolder.getProfileRegistry();
            return profileRegistry.getProfile(IProfileRegistry.SELF);
        } catch (ProvisioningException e) {
            return null;
        }
    }

    public static IProfile getProfile(String id, long timestamp) {
        IProfileRegistry profileRegistry;
        try {
            profileRegistry = ServiceHolder.getProfileRegistry();
            return profileRegistry.getProfile(id, timestamp);
        } catch (ProvisioningException e) {
            return null;
        }
    }

    public static IInstallableUnit[] sort(IInstallableUnit[] ius) {
        Arrays.sort(ius, new Comparator<IInstallableUnit>() {
            public int compare(IInstallableUnit arg0, IInstallableUnit arg1) {
                return arg0.toString().compareTo(arg1.toString());
            }
        });
        return ius;
    }

    public static boolean isIUInstalled(IInstallableUnit iu, HashMap<String, IInstallableUnit> installedIUMap) {
        IInstallableUnit installedIU = installedIUMap.get(iu.getId());
        return installedIU != null && installedIU.getVersion().compareTo(iu.getVersion()) >= 0;
    }

    public static long[] getProfileTimestamps(String profileID) throws ProvisioningException {
        IProfileRegistry profileRegistry = ServiceHolder.getProfileRegistry();
        return profileRegistry.listProfileTimestamps(profileID);
    }

    private static IStatus performProvisioningAction(IProvisioningPlan
                                                             provisioningPlan, boolean applyConfiguration) throws ProvisioningException {
        ProvisioningContext context = ServiceHolder.getProvisioningContext();
        IEngine engine = ServiceHolder.getP2Engine();
        context.setMetadataRepositories(ServiceHolder.getMetadataRepositoryManager().getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL));
        context.setArtifactRepositories(ServiceHolder.getArtifactRepositoryManager().getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL));
        IStatus status = PlanExecutionHelper.executePlan(provisioningPlan, engine, context, new NullProgressMonitor());

        return status;
    }

    @Deprecated
    public static Collection<IInstallableUnit> getAllInstallableUnits(IQueryable queryable, boolean showLatest) {
        IQuery<IInstallableUnit> query;
        if (showLatest) {
            query = QueryUtil.createMatchQuery("latest(x | x.properties[$0] == $1)",
                    new String[]{MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.TRUE.toString()});
        } else {
            query = QueryUtil.createMatchQuery("properties[$0] == $1",
                    new String[]{MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.TRUE.toString()});
        }
        return queryable.query(query, new NullProgressMonitor()).toUnmodifiableSet();
    }

    /**
     * @param queryable
     * @param showLatest to query only the latest version of IU
     * @return Category type IUs collection
     */
    public static Collection<IInstallableUnit> getCategoryTypeInstallableUnits(IQueryable queryable, boolean showLatest) {
        IQuery<IInstallableUnit> query = null;
        if (showLatest) {
            query = QueryUtil.createQuery("latest(x | (x.properties[$0] == $1 && x.properties[$2] != $1))",
                    new String[]{MetadataFactory.InstallableUnitDescription.PROP_TYPE_CATEGORY, Boolean.TRUE.toString(),
                            MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP});
        } else {
            query = QueryUtil.createMatchQuery("properties[$0] == $1 && properties[$2] != $1",
                    new String[]{MetadataFactory.InstallableUnitDescription.PROP_TYPE_CATEGORY, Boolean.TRUE.toString(),
                            MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP});
        }
        return queryable.query(query, new NullProgressMonitor()).toUnmodifiableSet();
    }

    /**
     * @param queryable
     * @param showLatest to query only the latest version of IU
     * @return Group type IUs collection
     */
    public static Collection<IInstallableUnit> getGroupTypeInstallableUnits(IQueryable queryable, boolean showLatest) {
        IQuery<IInstallableUnit> query = null;
        if (showLatest) {
            query = QueryUtil.createQuery("latest(x | x.properties[$0] == $1)",
                    new String[]{MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.TRUE.toString()});
        } else {
            query = QueryUtil.createMatchQuery("properties[$0] == $1",
                    new String[]{MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.TRUE.toString()});
        }
        return queryable.query(query, new NullProgressMonitor()).toUnmodifiableSet();
    }

    public static Collection<IInstallableUnit> getAllInstalledRootFeatures(IQueryable queryable) {
        IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("properties[$0] == $1",
                new String[]{MetadataFactory.InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.TRUE.toString()});
        return queryable.query(query, new NullProgressMonitor()).toUnmodifiableSet();

    }
}
