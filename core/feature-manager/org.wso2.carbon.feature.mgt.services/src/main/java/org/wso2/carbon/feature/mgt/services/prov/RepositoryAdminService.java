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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.metadata.ICopyright;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.wso2.carbon.feature.mgt.core.CompMgtMessages;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.util.IUPropertyUtils;
import org.wso2.carbon.feature.mgt.core.util.ProvisioningUtils;
import org.wso2.carbon.feature.mgt.core.util.RepositoryUtils;
import org.wso2.carbon.feature.mgt.services.prov.data.Feature;
import org.wso2.carbon.feature.mgt.services.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.services.prov.data.RepositoryInfo;
import org.wso2.carbon.feature.mgt.services.prov.utils.ProvWSUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WS API for Repository management.
 */
public class RepositoryAdminService {

    private static final Log log = LogFactory.getLog(RepositoryAdminService.class);

    /**
     * Use this method to add a P2 repository to the system.
     *
     * @param location of the repository to be added
     * @param nickName of the repository to be added
     * @return true only if the operation is successful
     * @throws AxisFault if an exception occurs while adding the repository
     */
    public boolean addRepository(String location, String nickName) throws AxisFault {
        try {
            URI uri = new URI(location);
            RepositoryUtils.addRepository(uri, nickName);

        } catch (ProvisioningException e) {
            handleException(e);
        } catch (URISyntaxException e) {
            handleException(ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_LOCATION, e, location));
        }
        return true;
    }

    /**
     * Use this method to update an existing repository
     *
     * @param prevLocation    current location of the repository
     * @param prevNickName    current name of the repository
     * @param updatedLocation new location of the repository
     * @param updatedNickName new name of the repository
     * @throws AxisFault if an exception occurs while updating the repository
     */
    public void updateRepository(String prevLocation, String prevNickName,
                                 String updatedLocation, String updatedNickName) throws AxisFault {
        try {
            URI prevURI = new URI(prevLocation);
            URI updatedURI = new URI(updatedLocation);
            RepositoryUtils.updateRepository(prevURI, prevNickName, updatedURI, updatedNickName);
        } catch (ProvisioningException e) {
            handleException(e);
        } catch (URISyntaxException e) {
            handleException(ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_LOCATION, e, updatedLocation));
        }
    }

    /**
     * Use this method to remove an exsisting repository
     *
     * @param location of the repository
     * @throws AxisFault if an exception occurs while removing the repository
     */
    public void removeRepository(String location) throws AxisFault {
        try {
            URI uri = new URI(location);
            RepositoryUtils.removeRepository(uri);
        } catch (ProvisioningException e) {
            handleException(e);
        } catch (URISyntaxException e) {
            handleException(ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_LOCATION, e, location));
        }

    }

    /**
     * Use this method to enable/disable a repository
     *
     * @param location of the repository
     * @param enabled  true/false
     * @throws AxisFault if an exception occurs while setting enablement the repository
     */
    public void enableRepository(String location, boolean enabled) throws AxisFault {
        try {
            URI uri = new URI(location);
            RepositoryUtils.enableRepository(uri, enabled);
        } catch (ProvisioningException e) {
            handleException(e);
        } catch (URISyntaxException e) {
            handleException(ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_LOCATION, e, location));
        }
    }

    /**
     * Returns the list of enabled repositories in the system.
     *
     * @return an array of RepositoryInfo instances
     * @throws AxisFault if an exception occurs while querying the list of enabled repositories
     */
    public RepositoryInfo[] getEnabledRepositories() throws AxisFault {
        RepositoryInfo[] repositoryInfo = null;
        try {
            URI[] repositoryURIs = RepositoryUtils.getRepositoryList(IRepositoryManager.REPOSITORIES_NON_SYSTEM);
            repositoryInfo = ProvWSUtils.wrapURIsAsRepositories(repositoryURIs);
        } catch (ProvisioningException e) {
            handleException(e);
        } catch (Exception e) {
            handleException("Error occurred while querying repositories",
                    "failed.get.repositories", e);
        }
        return repositoryInfo;
    }

    /**
     * Returns the list of all repositories in the system.
     *
     * @return an array of RepositoryInfo instances
     * @throws AxisFault if an exception occurs while querying the list of all repositories
     */
    public RepositoryInfo[] getAllRepositories() throws AxisFault {
        RepositoryInfo[] repositoryInfo = null;
        try {
            URI[] nonSystemRepoURIs = RepositoryUtils.getRepositoryList(IRepositoryManager.REPOSITORIES_NON_SYSTEM);
            URI[] disabledRepoURIs = RepositoryUtils.getRepositoryList(IRepositoryManager.REPOSITORIES_DISABLED);
            URI[] allRepoURIs = ProvWSUtils.mergeURIArrays(nonSystemRepoURIs, disabledRepoURIs);
            repositoryInfo = ProvWSUtils.wrapURIsAsRepositories(allRepoURIs);
        } catch (ProvisioningException e) {
            handleException(e);
        } catch (Exception e) {
            handleException("Error occurred while querying repositories",
                    "failed.get.repositories", e);
        }
        return repositoryInfo;
    }

    /**
     * Return a list of available features in added repositories
     *
     * @param location                  of the repository or null. TODO document null here.
     * @param groupByCategory           boolean flag to retrieve categoryIUs(true) or groupIUs(false)
     * @param hideInstalledFeatures     by deault the value is true
     * @param showOnlyTheLatestFeatures if the value true, only the lateset versions of the features will be returned
     * @return the array of Feature matches with the specified condition.
     * @throws AxisFault if an exception occurs while querying the list of feature available in added repositories
     */
    public Feature[] getInstallableFeatures(String location, boolean groupByCategory, boolean hideInstalledFeatures,
                                            boolean showOnlyTheLatestFeatures) throws AxisFault {
		URI repoURI = null;
		Feature[] features = null;
		IInstallableUnit[] availableInstallableUnits = null;
		try {
			if (location != null) {
				repoURI = new URI(location);
			}
			if (groupByCategory) {
				availableInstallableUnits =
				                            ProvisioningUtils.getCategoryTypeInstallableUnits(RepositoryUtils.getQuerybleRepositoryManager(repoURI), showOnlyTheLatestFeatures)
				                                             .toArray(new IInstallableUnit[0]);
			} else {
				availableInstallableUnits =
				                            ProvisioningUtils.getGroupTypeInstallableUnits(RepositoryUtils.getQuerybleRepositoryManager(repoURI), showOnlyTheLatestFeatures)
				                                             .toArray(new IInstallableUnit[0]);
			}
			//querying installed group type IUs
			IInstallableUnit[] installedInstallableUnits = ProvisioningUtils.getGroupTypeInstallableUnits(
			                                                                                        ProvisioningUtils.getProfile(),false).toArray(new IInstallableUnit[0]);
            features = ProvWSUtils.wrapAvailableFeatures(availableInstallableUnits,installedInstallableUnits,
                    RepositoryUtils.getQuerybleRepositoryManager(repoURI));
        } catch (URISyntaxException e) {
            handleException("Invalid Repository Location :" + location, "invalid.repo.location", e);
        } catch (Exception e) {
            handleException("Error occurred while querying installable features",
                    "failed.get.installable.features", e);
        }
        return features;
    }

    public FeatureInfo getInstallableFeatureInfo(String featureID, String featureVersion) throws AxisFault {
        FeatureInfo featureInfo = null;
        try {
            IInstallableUnit iu = RepositoryUtils.getInstallableUnit(featureID, featureVersion);
            if (iu == null) {
                handleException("Error occurred while querying feature information :" + featureID + "-" + featureVersion,
                        "failed.get.feature.information");
            }

            ILicense licence = IUPropertyUtils.getLicense(iu);
            ICopyright copyright = IUPropertyUtils.getCopyright(iu);
            featureInfo = ProvWSUtils.wrapIUsAsFeaturesWithDetails(iu, licence, copyright);
        } catch (Exception e) {
            handleException("Error occurred while querying feature information :" + featureID + "-" + featureVersion,
                    "failed.get.feature.information", e);
        }
        return featureInfo;
    }
    
    /**
     * adding the default P2 repository defined in the carbon.xml.
     * @return default repository URL as a String
     * @deprecated
     */
	public String addDefaultRepository() {
		String defaultRepoURL = null;
		try {
			//defaultRepoURL = RepositoryUtils.addDefaultRepository();
		} catch (Exception e) {
			handleException("Error occurred while adding default feature repository",e);
        }
		return defaultRepoURL;
	}
	
    private void handleException(String msg, String faultCode, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, faultCode, e);
    }

    private void handleException(String msg, String faultCode) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg, faultCode);
    }

    private void handleException(ProvisioningException e) throws AxisFault {
        log.error(e.getMessage(), e);
        throw new AxisFault(e.getMessage(), e.getErrorCode());
    }
    
    private void handleException(String msg, Exception e) {
        log.error(msg, e);
    }
}
