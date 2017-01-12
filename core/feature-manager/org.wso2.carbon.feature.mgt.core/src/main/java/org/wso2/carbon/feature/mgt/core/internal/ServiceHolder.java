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
package org.wso2.carbon.feature.mgt.core.internal;

import org.eclipse.equinox.internal.provisional.configurator.Configurator;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IEngine;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.utils.CarbonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class ServiceHolder {

    private static String P2_AGENT_LOCATION;
    static {
        String componentsPath =  System.getProperty(CarbonBaseConstants.CARBON_COMPONENTS_DIR_PATH);
        if(componentsPath != null) {
            P2_AGENT_LOCATION = Paths.get(componentsPath,"p2").toString();
        } else {
            P2_AGENT_LOCATION = Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME),"repository","components","p2").toString();
        }
    }
	//private static final String P2_AGENT_LOCATION = CarbonUtils.getComponentsRepo() + File.separator + "p2";
	private static final Log log = LogFactory.getLog(ServiceHolder.class);
    private static IProvisioningAgentProvider provisioningAgentProvider;
    private static IArtifactRepositoryManager artifactRepositoryManager;
    private static IMetadataRepositoryManager metadataRepositoryManager;
    private static IProfileRegistry profileRegistry;
    private static IPlanner planner;
    private static IEngine p2Engine;
    private static ProvisioningContext provisioningContext;
    private static Configurator p2Configurator;
    private static ServerConfigurationService serverConfigurationService;

    public static final String ID = "org.wso2.carbon.feature.mgt.core";

    public static IMetadataRepositoryManager getMetadataRepositoryManager() throws ProvisioningException {
        if (metadataRepositoryManager == null) {
            throw new ProvisioningException("No MetadataRepositoryManager Service is found");
        }
        return metadataRepositoryManager;
    }

    public static IArtifactRepositoryManager getArtifactRepositoryManager() throws ProvisioningException {
        if (artifactRepositoryManager == null) {
            throw new ProvisioningException("No ArtifactRepositoryManager Service is found");
        }
        return artifactRepositoryManager;
    }

    public static IProfileRegistry getProfileRegistry() throws ProvisioningException {
        if (profileRegistry == null) {
            throw new ProvisioningException("No ProfileRegistry Service is found");
        }
        return profileRegistry;
    }

    public static IPlanner getPlanner() throws ProvisioningException {
        if (planner == null) {
            throw new ProvisioningException("No IPlanner Service is found");
        }
        return planner;
    }

    public static IEngine getP2Engine() throws ProvisioningException {
        if (p2Engine == null) {
            throw new ProvisioningException("No IEngine Service is found");
        }
        return p2Engine;
    }

    public static ProvisioningContext getProvisioningContext() throws ProvisioningException {
        if (provisioningContext == null){
            throw new ProvisioningException("No ProvisioningContext found");
        }
        return provisioningContext;
    }
    
    public static ServerConfigurationService getServerConfigurationService() throws ProvisioningException {
        if (provisioningContext == null){
            throw new ProvisioningException("No ServerConfiguration Service found");
        }
        return serverConfigurationService;
    }

    private static URI getAgentURI(String path) {
    	URI uri = null;
    	try {
    		File file = new File(path);
    		uri = file.toURI().normalize(); //file paths in windows are different from canonical path
    		                                        //to 8.3 path. P2 uses only canonical paths. So
    		                                        //we have to always use canonical file path. 
    	} catch (NullPointerException e) {
    		//System.out.println("Error while extracting p2 agent URI");
    		log.error("Error while extracting p2 agent URI", e);
    	}
        return uri;
    }

    public static void setProvisioningAgentProvider(IProvisioningAgentProvider provisioningAgentProvider){
      ServiceHolder.provisioningAgentProvider = provisioningAgentProvider;
        if(ServiceHolder.provisioningAgentProvider != null){
            IProvisioningAgent provisioningAgent = null;
            try {
            	URI agentURI = getAgentURI(P2_AGENT_LOCATION);
                provisioningAgent = provisioningAgentProvider.createAgent(agentURI);
            } catch (ProvisionException e) {
                log.error("provision Exception occurred", e);
            }
            if(provisioningAgent != null) {
            IPlanner planner = (IPlanner)provisioningAgent.getService(IPlanner.SERVICE_NAME);
            setPlanner(planner);
            IMetadataRepositoryManager metadataRepositoryManager =
                    (IMetadataRepositoryManager)provisioningAgent.getService(IMetadataRepositoryManager.SERVICE_NAME);
            setMetadataRepositoryManager(metadataRepositoryManager);
            IArtifactRepositoryManager artifactRepositoryManager =
                    (IArtifactRepositoryManager)provisioningAgent.getService(IArtifactRepositoryManager.SERVICE_NAME);
            setArtifactRepositoryManager(artifactRepositoryManager);
            IProfileRegistry profileRegistry = (IProfileRegistry)provisioningAgent.getService(IProfileRegistry.SERVICE_NAME);
            setProfileRegistry(profileRegistry);
            IEngine engine = (IEngine)provisioningAgent.getService(IEngine.SERVICE_NAME);
            setP2Engine(engine);
            setProvisioningContext(new ProvisioningContext(provisioningAgent));
                //provisioningAgent.registerService("eclipse.p2.profile", System.getProperty("profile"));
				try {
					provisioningAgent.registerService(IProvisioningAgent.INSTALLER_AGENT,
					                                  provisioningAgentProvider.createAgent(null));
				} catch (ProvisionException e) {
					// ignore the catch for now
				}
            }else {
                log.error("Error while getting provisioning agent");
            }

        }else{
            setPlanner(null);
            setMetadataRepositoryManager(null);
            setArtifactRepositoryManager(null);
            setProfileRegistry(null);
            setP2Engine(null);

        }

    }

    public static void setProvisioningContext(ProvisioningContext provisioningContext) {
        ServiceHolder.provisioningContext = provisioningContext;
    }

    public static void setArtifactRepositoryManager(IArtifactRepositoryManager artifactRepositoryManager) {
        ServiceHolder.artifactRepositoryManager = artifactRepositoryManager;
    }

    public static void setMetadataRepositoryManager(IMetadataRepositoryManager metadataRepositoryManager) {
        ServiceHolder.metadataRepositoryManager = metadataRepositoryManager;
    }

    public static void setProfileRegistry(IProfileRegistry profileRegistry) {
        ServiceHolder.profileRegistry = profileRegistry;
    }

    public static void setPlanner(IPlanner planner) {
        ServiceHolder.planner = planner;
    }

    public static void setP2Engine(IEngine p2Engine) {
        ServiceHolder.p2Engine = p2Engine;
    }

    public static void setServerConfigurationService(ServerConfigurationService serverConfigService){
    	ServiceHolder.serverConfigurationService = serverConfigService;
    }

}
