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
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.feature.mgt.core.CompMgtMessages;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.internal.ServiceHolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Contains utility methods which can be used for P2 repository management.
 */
public class RepositoryUtils {

    /**
     * Use the given repository URL as the artifact repository URL and the metadata repository URL
     *
     * @param location URL of the repository to be added
     * @param nickName user defined name for the repository
     * @throws ProvisioningException if the add repository operation is failed.
     */
    public static void addRepository(URI location, String nickName) throws ProvisioningException {
        IStatus status;
        IMetadataRepositoryManager metadataRepositoryManager = null;
        IArtifactRepositoryManager artifactRepositoryManager = null;
        if (location == null) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_LOCATION, "null value");
        }

        if (nickName == null || nickName.length() == 0) {
            throw ProvisioningException.makeExceptionFromErrorCode(CompMgtMessages.INVALID_REPO_NAME,
                    (nickName == null) ? "null value" : nickName);
        }

        try {
            metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
            artifactRepositoryManager = ServiceHolder.getArtifactRepositoryManager();

            if (metadataRepositoryManager.contains(location)) {
                return;
            }
            //adding metadata and artifact repositories
            metadataRepositoryManager.addRepository(location);
            artifactRepositoryManager.addRepository(location);
            //Loading the metadata repository
            metadataRepositoryManager.loadRepository(location, new NullProgressMonitor());
            metadataRepositoryManager.setRepositoryProperty(location, IRepository.PROP_NICKNAME, nickName);
       
            //Loading the artifact repository
            artifactRepositoryManager.loadRepository(location, new NullProgressMonitor());
            artifactRepositoryManager.setRepositoryProperty(location, IRepository.PROP_NICKNAME, nickName);
        } catch (Exception e) {
        	//System.err.println(e.getStackTrace());
        	//removing the erroneous repository added above
        	metadataRepositoryManager.removeRepository(location);
        	artifactRepositoryManager.removeRepository(location);
        	throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.FAILD_TO_ADD_REPSITORY, e, location);
            
        }
    }

    public static void updateRepository(URI prevLocation, String prevNickName, URI updatedLocation,
                                        String updatedNickName) throws ProvisioningException {
        if (updatedLocation == null) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_LOCATION, "null value");
        }

        if (updatedNickName == null || updatedNickName.length() == 0) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_NAME, (updatedNickName == null) ? "null value" : updatedNickName);
        }

        try {
            if (!prevLocation.equals(updatedLocation)) {
                removeRepository(prevLocation);
                addRepository(updatedLocation, updatedNickName);

            } else if (!prevNickName.equals(updatedNickName)) {
                setMetadataRepositoryProperty(prevLocation, IRepository.PROP_NICKNAME, updatedNickName);
            }
        } catch (Exception e) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.FAILD_TO_UPDATE_REPSITORY, e, updatedLocation);
        }
    }

    public static void removeRepository(URI location) throws ProvisioningException {
        if (location == null) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.INVALID_REPO_LOCATION, "null value");
        }

        try {
            //Removing metadata repository
            removeMetadataRepository(location);
            //Removing artifact repository
            removeArtifactRepository(location);
        } catch (Exception e) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.FAILD_TO_REMOVE_REPSITORY, e, location);
        }
    }

    public static void enableRepository(URI location, boolean enable) throws ProvisioningException {
        IMetadataRepositoryManager metadataRepositoryManager;
        IArtifactRepositoryManager artifactRepositoryManager;

        if (location == null) {
            throw ProvisioningException.makeExceptionFromErrorCode(CompMgtMessages.INVALID_REPO_LOCATION,
                    "null value");
        }

        try {
            metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
            artifactRepositoryManager = ServiceHolder.getArtifactRepositoryManager();
            metadataRepositoryManager.setEnabled(location, enable);
            artifactRepositoryManager.setEnabled(location, enable);
        } catch (Exception e) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.FAILD_TO_ENABLE_REPSITORY, e, location);
        }

    }

    public static boolean isRepositoryEnabled(URI location) throws ProvisioningException {
        IMetadataRepositoryManager metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
        IArtifactRepositoryManager artifactRepositoryManager = ServiceHolder.getArtifactRepositoryManager();
        return metadataRepositoryManager.isEnabled(location) && artifactRepositoryManager.isEnabled(location);
    }

    public static URI[] getRepositoryList(int type) throws ProvisioningException {
        try {
            IMetadataRepositoryManager metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
            return metadataRepositoryManager.getKnownRepositories(type);
        } catch (Exception e) {
            throw ProvisioningException.makeExceptionFromErrorCode(
                    CompMgtMessages.FAILD_TO_GET_REPSITORY_LIST, e);
        }
    }

    public static String getMetadataRepositoryProperty(URI location, String key) throws ProvisioningException {
        IMetadataRepositoryManager metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
        return metadataRepositoryManager.getRepositoryProperty(location, key);
    }

    /**
     * Returns the installable units that match the given query
     * in the given metadata repository.
     *
     * @param location  The location of the metadata repo to search.  <code>null</code> indicates
     *                  search all known repos.
     * @param query     The query to perform
     * @return The IUs that match the query
     * @throws ProvisioningException
     */
    public static Collection getInstallableUnitsInRepositories(URI location, IQuery query) throws ProvisioningException {
        IQueryable queryable;
        if (location != null) {
            queryable = getMetadataRepository(location);
        } else {
            queryable = ServiceHolder.getMetadataRepositoryManager();
        }
        return queryable.query(query,new NullProgressMonitor()).toUnmodifiableSet();
    }

    public static IQueryable getQuerybleRepositoryManager(URI location) {
        IQueryable queryable;
        try {
            if (location != null) {
                queryable = getMetadataRepository(location);
            } else {
                queryable = ServiceHolder.getMetadataRepositoryManager();
            }
            return queryable;
        } catch (ProvisioningException ignore) {
            //TODO log and explain why we have ignored the exception
            return null;
        }
    }

    public static IInstallableUnit getInstallableUnit(String id, String version) throws ProvisioningException {

        return getInstallableUnit(QueryUtil.createIUQuery(id,Version.create(version)));
    }
    
	public static String addDefaultRepository() throws ProvisioningException, URISyntaxException {
		String repositoryName = null;
		String repositoryURL = null;
		ServerConfigurationService serverConfigService = ServiceHolder.getServerConfigurationService();		
		repositoryName = serverConfigService.getFirstProperty("FeatureRepository.RepositoryName");
		repositoryURL = serverConfigService.getFirstProperty("FeatureRepository.RepositoryURL");
		if (repositoryName != null && repositoryURL != null) {
			IMetadataRepositoryManager metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
			IArtifactRepositoryManager artifactRepositoryManager = ServiceHolder.getArtifactRepositoryManager();
			try {
				//adding the default feature repository defined in carbon.xml
				URI location = new URI(repositoryURL);
				if (!metadataRepositoryManager.contains(location)) {
					metadataRepositoryManager.addRepository(location);
					metadataRepositoryManager.setRepositoryProperty(location,
					                                                IRepository.PROP_NICKNAME,
					                                                repositoryName);
					artifactRepositoryManager.addRepository(location);
					artifactRepositoryManager.setRepositoryProperty(location,
					                                                IRepository.PROP_NICKNAME,
					                                                repositoryName);
				}
			} catch (Exception e) {
				throw ProvisioningException.makeExceptionFromErrorCode(CompMgtMessages.FAILD_TO_ADD_REPSITORY,
				                                                       e, repositoryURL);
			}
		}
		return repositoryURL;
	}
	
    public static IInstallableUnit getInstallableUnit(IQuery query) throws ProvisioningException {
        Collection collection = RepositoryUtils.getInstallableUnitsInRepositories(null, query);
        IInstallableUnit[] installableUnits = (IInstallableUnit[]) collection.toArray(new IInstallableUnit[0]);

        if (installableUnits == null || installableUnits.length == 0) {
            return null;
        }
        return installableUnits[0];
    }


    private static IMetadataRepository getMetadataRepository(URI location) throws ProvisioningException {
        IMetadataRepositoryManager metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
        try {
            return metadataRepositoryManager.loadRepository(location, null);
        } catch (ProvisionException e) {
            throw new ProvisioningException(e.getMessage(), e);
        }
    }

    private static void setMetadataRepositoryProperty(URI location,
                                                      String key,
                                                      String value) throws ProvisioningException {
        IMetadataRepositoryManager metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
        metadataRepositoryManager.setRepositoryProperty(location, key, value);
    }

    private static void removeMetadataRepository(URI location) throws ProvisioningException {
        IMetadataRepositoryManager metadataRepositoryManager = ServiceHolder.getMetadataRepositoryManager();
        metadataRepositoryManager.removeRepository(location);
    }

    private static void removeArtifactRepository(URI location) throws ProvisioningException {
        IArtifactRepositoryManager artifactRepositoryManager = ServiceHolder.getArtifactRepositoryManager();
        artifactRepositoryManager.removeRepository(location);
    }
}
