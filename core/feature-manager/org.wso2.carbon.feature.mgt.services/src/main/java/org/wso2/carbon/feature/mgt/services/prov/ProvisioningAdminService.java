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
package org.wso2.carbon.feature.mgt.services.prov;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.ICopyright;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.wso2.carbon.feature.mgt.core.ResolutionResult;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.core.operations.ProfileChangeOperation;
import org.wso2.carbon.feature.mgt.core.util.IUPropertyUtils;
import org.wso2.carbon.feature.mgt.core.util.ProvisioningUtils;
import org.wso2.carbon.feature.mgt.services.prov.data.*;
import org.wso2.carbon.feature.mgt.services.prov.utils.ProvWSUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WS API for Profile management. i.e to perform install/uninstall/revert action
 */
public class ProvisioningAdminService {
    private static final Log log = LogFactory.getLog(ProvisioningAdminService.class);

    /**
     * Clients can get a list of all installed features in the running Carbon instance. Array contains all the
     * top level(root) features. A feature is called a root feature if it contains the property
     * "org.eclipse.equinox.p2.type.root" and the value should be true. Root features may contains
     * child features.
     * <p/>
     * e.g. consider logging mgt feature
     * The root feature (org.wso2.carbon.logging.mgt.feature) has two child features named
     * "org.wso2.carbon.logging.mgt.server.feature" and "org.wso2.carbon.logging.mgt.ui.feature"
     *
     * @return an array of installed features
     * @throws AxisFault if an exception occurs while querying the list of features.
     */
    public Feature[] getAllInstalledFeatures() throws AxisFault {
        Feature[] features = null;
        try {
            features = ProvWSUtils.wrapInstalledFeatures(ProvisioningUtils.getAllInstalledRootFeatures(
                    ProvisioningUtils.getProfile()).toArray(new IInstallableUnit[0]),ProvisioningUtils.getProfile());
        } catch (Exception e) {
            handleException("Error occurred querying installed features",
                    "failed.get.installed.features", e);
        }
        return features;
    }

    /**
     * This is to support the concept of FE/BE separation. Most of the Carbon features contains child features called
     * common, server or console feature. These feature are identified using the
     * property "org.wso2.carbon.p2.category.type" and the values can be server, common, or console
     * <p/>
     * This method removes all the features which are marked as server features.
     * <p/>
     * First it uninstall all the root/parents features and server features. Because you cannot uninstall
     * child feature if there are parent features installed in the system. Then it installs all the common and
     * console features.
     *
     * @return true only if the operation is successful
     * @throws AxisFault if an exception occurs while removing all service features.
     */
    public boolean removeAllServerFeatures() throws AxisFault {
        try {
            removeConsoleOrServerFeatures("server");
        } catch (Exception e) {
            handleException("Error occurred while removing server features",
                    "failed.remove.server.features", e);
        }
        return true;
    }

    /**
     * Very similar to the removeAllServerFeatures() method, but uninstalls all the console features.
     *
     * @return true only if the operation is successful
     * @throws AxisFault if an exception occurs while removing all service features.
     */
    public boolean removeAllConsoleFeatures() throws AxisFault {
        try {
            removeConsoleOrServerFeatures("console");
        } catch (Exception e) {
            handleException("Error occurred while removing console features",
                    "failed.remove.console.features", e);
        }
        return true;
    }

    /**
     * @deprecated
     * @param key
     * @param value
     * @return
     * @throws AxisFault
     */
    public FeatureInfo[] getInstalledFeaturesWithProperty(String key, String value) throws AxisFault {
//        FeatureInfo[] featureInfo = null;
//       try {
//            IInstallableUnit[] installableUnits = ProvisioningUtils.getAllInstalledIUs(key, value);
//            featureInfo = DataWrapper.wrapIUsAsFeatures(installableUnits);
//        } catch (Exception e) {
//           handleException("Error occurred while querying the installed feature with property :" + key + ":" + value,
//                    "failed.get.installed.feature.with.prop", e);
//        }
        return null;
    }

    /**
     * Use this method to get information about an installed feature. This method returns license text, copyright text
     * feature description, etc.
     *
     * @param featureID      The id of the requested feature
     * @param featureVersion The version of the requested feature
     * @return an instance of FeatureInfo which contains all the informations of the requested feature.
     * @throws AxisFault if an exception occurs while querying specified feature information
     */
    public FeatureInfo getInstalledFeatureInfo(String featureID, String featureVersion) throws AxisFault {
        FeatureInfo featureInfo = null;
        try {
            IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(featureID,Version.create(featureVersion));
            IInstallableUnit[] installableUnits = ProvisioningUtils.getProfile().query(query,
                    new NullProgressMonitor()).toArray(IInstallableUnit.class);
            if (installableUnits == null || installableUnits.length == 0) {
                log.error("Error occurred while querying feature information :" + featureID + "-" + featureVersion);
                throw new AxisFault("failed.get.feature.information");
            }

            IInstallableUnit iu = installableUnits[0];
            ILicense license = IUPropertyUtils.getLicense(iu);
            ICopyright copyright = IUPropertyUtils.getCopyright(iu);
            featureInfo = ProvWSUtils.wrapIUsAsFeaturesWithDetails(iu, license, copyright);
        } catch (Exception e) {
            handleException("Error occurred while querying feature information :" + featureID + "-" + featureVersion,
                    "failed.get.feature.information", e);
        }
        return featureInfo;
    }

    /**
     * Use this method to review your provisioning operation before performing it. As a result of the reviewing
     * process P2 generates a ProvisioningPlan which can later be used to perform the provisioning operation.
     * <p/>
     * Some features depend on other features. For an example, Service Hosting feature depends on Service Management
     * feature. In order to install Service Hosting feature, either Service Mgt feature should be already installed
     * in your system or it should be available in one of the added repositories. Otherwise Equinox P2 complains that
     * installation of Service Hosting feature cannot be performed due to a missing dependency. This review process
     * allows you to get these sort of information.
     * <p/>
     * There are three types of supported provisioning operations.
     * 1) Install
     * 2) Uninstall
     * 3) Revert
     * <p/>
     * Clients need to set the actionType of the ProvisioningActionInfo instance before invoking this method.
     * <p/>
     * ProvisiongPlan is stored in ServletSession for lates usages. Generating a plan is a heavy operation.
     *
     * @param provActionInfo contains infomation about the provisioning action to be performed,
     *                       such features to be installed, features to be uninstalled
     * @return an instance of ProvisioningActionResultInfo which contain the result of the review
     * @throws AxisFault if an exception occurs while reviewing the specified provisionging action
     */
    public ProvisioningActionResultInfo reviewProvisioningAction(ProvisioningActionInfo provActionInfo)
            throws AxisFault {
        ProfileChangeOperation profileChangeOperation =
                OperationFactory.getProfileChangeOperation(provActionInfo.getActionType());
    	
    	if (profileChangeOperation == null) {
            handleException("Error occurred while reviewing provisioning action",
                    "failed.review.prov.action");
        }

        if (log.isDebugEnabled()) {
            log.debug("Reviewing the provisioning action: " + profileChangeOperation.getActionType());
        }

        ProvisioningActionResultInfo provisioningActionResultInfo = null;
        try {
            //Derive IInstallableUnits from the features to be installed. These are available in added repositories
            IInstallableUnit[] repositoryIUs = ProvWSUtils.deriveRepositoryIUsFromFeatures(
                    provActionInfo.getFeaturesToInstall());
            profileChangeOperation.setIusToInstall(repositoryIUs);

            //Derive IInstallableUnits from the features to be uninstalled.
            //These features are alread installed, hence available in the profile            
            IInstallableUnit[] profileIUs = ProvWSUtils.deriveProfileIUsFromFeatures(
                    provActionInfo.getFeaturesToUninstall());
            profileChangeOperation.setIusToUninstall(profileIUs);

            //This is required for revert operations.
            profileChangeOperation.setTimestamp(provActionInfo.getTimestamp());
            //Review the profile modifiation operation
            ResolutionResult resolutionResult = profileChangeOperation.reviewProfileChangeAction(
                    ProvisioningUtils.getProfile());
                     
            //Wrap ResolutionResult so that it can be send through the wire.
            provisioningActionResultInfo = ProvWSUtils.wrapResolutionResult(resolutionResult);
 
            int severity = resolutionResult.getSummaryStatus().getSeverity();
            if (severity == IStatus.ERROR) {
                provisioningActionResultInfo.setProceedWithInstallation(false);
                if (log.isDebugEnabled()) {
                    log.debug("Failed to proceed with the provisioning action due an error," +
                            resolutionResult.getSummaryReport());
                }
            } else {
                provisioningActionResultInfo.setProceedWithInstallation(true);
                ProvWSUtils.saveResolutionResult(profileChangeOperation.getActionType(), resolutionResult,
                        MessageContext.getCurrentMessageContext());
                if (log.isDebugEnabled()) {
                    log.debug("Successfully reviewed the provisioning action");
                }
            }
        } catch (Exception e) {
            handleException("Error occurred while reviewing provisioning action",
                    "failed.review.prov.action", e);
        }
        return provisioningActionResultInfo;
    }

    /**
     * Returns all the license terms associated with features to be installed. License terms are calculated
     * from the previously reviewed provisioning plan stored in the ServletSession
     *
     * @return all the license terms
     * @throws AxisFault if an exception occurs while querying licensing
     * @deprecated
     */
    public LicenseInfo[] getLicensingInformation() throws AxisFault {
        LicenseInfo[] licenseInfo = null;
        try {
            ResolutionResult resolutionResult = ProvWSUtils.getResolutionResult(
                    OperationFactory.INSTALL_ACTION, MessageContext.getCurrentMessageContext());
            if (resolutionResult == null) {
                return new LicenseInfo[0];
            }
            ILicense[] licenses = ProvisioningUtils.getLicensingInformation(resolutionResult.getProvisioningPlan());
            licenseInfo = ProvWSUtils.wrapP2LicensesAsLicenses(licenses);
        } catch (Exception e) {
            handleException("Error occurred while querying license information",
                    "failed.get.license.info", e);
        }
        return licenseInfo;
    }

    /**
     * Returns all the license terms for the features to be installed. If some of the features does not have a license
     * then return the list of features that does not have license. License terms are calculated from the reviewed
     * installable units stored in the ServeletSession.
     *
     * @return FeatureLicense which will have all the license terms or list of features which does not have license
     * @throws AxisFault if an exception occurs while querying licensing
     */
    public License[] getFeatureLicenseInfo() throws AxisFault {
        License[] license = null;

        try {
            ResolutionResult resolutionResult = ProvWSUtils.getResolutionResult(
                    OperationFactory.INSTALL_ACTION, MessageContext.getCurrentMessageContext());
            if (resolutionResult == null) {
                return new License[0];
            }
            Map<ILicense, ArrayList<IInstallableUnit>> licenseFeatureMap = ProvisioningUtils.getLicensingInformation
                    (resolutionResult.getReviewedInstallableUnits());
            license = ProvWSUtils.wrapP2LicensesAsLicenses(licenseFeatureMap);
        } catch (Exception e) {
            handleException("Error occurred while querying license information", "failed.get.license.info", e);
        }
        return license;
    }

    /**
     * Performs a previously reviewed provisioning action.
     *
     * @param actionType type of the provioning operation to be performed
     * @return true only if the operation is successful
     * @throws AxisFault if an exception occurs while performing the prov action
     */
    public boolean performProvisioningAction(String actionType) throws AxisFault {
        try {
            ResolutionResult resolutionResult = ProvWSUtils.getResolutionResult(actionType,
                    MessageContext.getCurrentMessageContext());
            if (resolutionResult == null) {
                handleException("Error occurred while performing provisioning action",
                        "failed.perform.prov.action");
            }

            ProvisioningUtils.performProvisioningAction(resolutionResult);
        } catch (Exception e) {
            handleException("Error occurred while performing provisioning action",
                    "failed.perform.prov.action", e);
        }
        return true;
    }

    /**
     * Returns all the previous configurations of the system. i.e. history of provisioning operations performed on
     * the system.
     * <p/>
     * previous configurations can be identified as previous states of the system. A state/configuration can be simply
     * identified by the set of installed features. When you perform a provisioning operation such as
     * installing/uninstalling of features, system state/configuration change occurs.
     *
     * @return an array of ProfileHistory instances
     * @throws AxisFault if an exception occurs while getting the profile history
     */
    public ProfileHistory[] getProfileHistory() throws AxisFault {
        int installableFeatures = 0;
        int uninstallableFeatures = 0;
        List<ProfileHistory> profileHistoryList = new ArrayList<ProfileHistory>();
        try {
            long[] timestamps = ProvisioningUtils.getProfileTimestamps(IProfileRegistry.SELF);

            
			ProfileChangeOperation profileChangeOperation =
			                                                OperationFactory.getProfileChangeOperation(OperationFactory.REVERT_ACTION);	
            for (long timestamp : timestamps) {
                profileChangeOperation.setTimestamp(timestamp);
                ResolutionResult resolutionResult = profileChangeOperation.reviewProfileChangeAction(
                        ProvisioningUtils.getProfile());

                if (resolutionResult.getReviewedInstallableUnits() != null) {
                    installableFeatures = resolutionResult.getReviewedInstallableUnits().length;
                }

                if (resolutionResult.getReviewedUninstallableUnits() != null) {
                    uninstallableFeatures = resolutionResult.getReviewedUninstallableUnits().length;
                }

                if (installableFeatures != 0 || uninstallableFeatures != 0) {
                	ProfileHistory history = ProvWSUtils.getProfileHistoryFromTimestamp(timestamp);
                	profileHistoryList.add(history);
                }
            }
        } catch (Exception e) {
            handleException("Error occurred while querying profile history",
                    "failed.get.profile.history", e);
        }
        return profileHistoryList.toArray(new ProfileHistory[profileHistoryList.size()]);
    }

    private void removeConsoleOrServerFeatures(String propertyValue) throws Exception {
        List<IInstallableUnit> iusToUninstall = new ArrayList<IInstallableUnit>();
        List<IInstallableUnit> iusToInstall = new ArrayList<IInstallableUnit>();

        IInstallableUnit[] allInstalledIUs = ProvisioningUtils.getAllInstalledIUs();
        for (IInstallableUnit iu : allInstalledIUs) {
            String categoryPropValue = iu.getProperty("org.wso2.carbon.p2.category.type");

            if (iu.getId().startsWith("org.wso2.carbon.core.runtime") || iu.getId().startsWith("carbon.product")) {
                //do nothing, just ignore.
            } else if (categoryPropValue != null && !categoryPropValue.equalsIgnoreCase(propertyValue)) {
                iusToInstall.add(iu);
            } else {
                iusToUninstall.add(iu);
            }
        }

        //Uninstalling all the composite features.
        ProfileChangeOperation profileChangeOperation =
                OperationFactory.getProfileChangeOperation(
                        OperationFactory.UNINSTALL_ACTION);

        profileChangeOperation.setIusToUninstall(iusToUninstall.toArray(new IInstallableUnit[iusToUninstall.size()]));

        ResolutionResult resolutionResult = profileChangeOperation.reviewProfileChangeAction(
                ProvisioningUtils.getProfile());
        ProvisioningUtils.performProvisioningAction(resolutionResult);

        //Installing server, console and common features depending on the action
        profileChangeOperation =
                OperationFactory.getProfileChangeOperation(
                        OperationFactory.INSTALL_ACTION);
       
        profileChangeOperation.setIusToInstall(iusToInstall.toArray(new IInstallableUnit[iusToInstall.size()]));

        resolutionResult = profileChangeOperation.reviewProfileChangeAction(
                ProvisioningUtils.getProfile());
        ProvisioningUtils.performProvisioningAction(resolutionResult);
    }

    private void handleException(String msg, String faultCode, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, faultCode, e);
    }

    private void handleException(String msg, String faultCode) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg, faultCode);
    }
}
