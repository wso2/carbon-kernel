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

package org.apache.axis2.jaxws;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.PrivilegedAction;


/** This class serves as a factory for ConfigurationContexts suitable in the client environment. */
public class ClientConfigurationFactory {
    
    private static final Log log = LogFactory.getLog(ClientConfigurationFactory.class);
    private ConfigurationContext configContext = null;
    private String repoPath = null;
    private String axisConfigPath = null;

    /** Returns a ClientConfigurationFactory object. */
    public static ClientConfigurationFactory newInstance() {
        return (ClientConfigurationFactory)MetadataFactoryRegistry.getFactory(ClientConfigurationFactory.class);       
    }

    /**
     * Create a ConfigurationContext from the specified configurator 
     * 
     * @param configurator
     */
    public ClientConfigurationFactory(AxisConfigurator configurator) {
        try {
            configContext = ConfigurationContextFactory.createConfigurationContext(configurator);
        } catch (AxisFault e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("clientConfigCtxtErr", e.getMessage()));
        }
    }

    /**
     * Create a ConfigurationContext from the specified repository and axis2.xml 
     * 
     * @param configurator
     */
    public ClientConfigurationFactory(String repoPath,
            String axisConfigPath) {
        this.repoPath = repoPath;
        this.axisConfigPath = axisConfigPath;
    }

    /**
     * Default constructor
     */
    public ClientConfigurationFactory() {
    }

    /**
     * Loads up a ConfigurationContext object using the configuration builder.
     *
     * @return a ConfigurationContext object that is suitable for the client environment
     */
    public synchronized ConfigurationContext getClientConfigurationContext() {
        // If the user has specified on use it.
        if (configContext != null) {
            return configContext;
        }
        
        if(repoPath == null)  {
            // Get the system properties for the repository.
            repoPath = getProperty_doPriv(Constants.AXIS2_REPO_PATH);
        }
        if(axisConfigPath == null) {
            // Get the system properties for axis2.xml 
            axisConfigPath = getProperty_doPriv(Constants.AXIS2_CONFIG_PATH);
        }
        if (log.isDebugEnabled()) {
            log.debug("Axis2 repository path : " + repoPath);
            log.debug("Axis2 Config path : " + axisConfigPath);
        }
        
        // If they are not specified, create a default one from the axis2_default.xml inside the kernel jar.
//        if (repoPath == null && axisConfigPath == null) {
//            try {
//                configContext = ConfigurationContextFactory.createDefaultConfigurationContext();
//            } catch (Exception e) {
//                throw ExceptionFactory.makeWebServiceException(Messages.getMessage("clientConfigCtxtErr", e.getMessage()));
//            }
//        }
        
        // Try the file system with the specified system properties.
        try {
            return ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(repoPath, axisConfigPath);
        } catch (AxisFault e) {
        	throw ExceptionFactory.makeWebServiceException(Messages.getMessage("clientConfigCtxtErr",e.getMessage()));
        }
    }

    private static String getProperty_doPriv(final String property) {
        return (String)
         AccessController.doPrivileged(
              new PrivilegedAction() {

                  public Object run() {
                      try {
                          return System.getProperty(property);
                      } catch (Throwable t) {
                          return null;
                      }
                  }
              });
    }
    
    /**
     * Perform any final client-specific configuration on a newly created AxisService.
     *
     * @param service A newly created AxisService on which to perform any final client-related
     *                configuration.
     * @throws DeploymentException
     * @throws Exception
     */
    public synchronized void completeAxis2Configuration(AxisService service)
            throws DeploymentException, Exception {
    }
}
