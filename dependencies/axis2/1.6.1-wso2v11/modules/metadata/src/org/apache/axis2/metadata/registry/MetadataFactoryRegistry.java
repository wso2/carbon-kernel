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

package org.apache.axis2.metadata.registry;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.jaxws.wsdl.WSDLReaderConfigurator;
import org.apache.axis2.jaxws.wsdl.WSDLReaderConfiguratorImpl;
import org.apache.axis2.metadata.factory.ResourceFinderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Map;

public class MetadataFactoryRegistry {
    
    private static Log log = LogFactory.getLog(MetadataFactoryRegistry.class);
    
    private static String configurationFileLoc = null;
    
    private final static Map<Class,Object> table;
        static {
                table = new Hashtable<Class,Object>();
                table.put(ResourceFinderFactory.class, new ResourceFinderFactory());
                table.put(ClientConfigurationFactory.class, new ClientConfigurationFactory());
                table.put(WSDLReaderConfigurator.class, new WSDLReaderConfiguratorImpl());
                if(configurationFileLoc == null) {
                    if(log.isDebugEnabled()) {
                        log.debug("A configuration file location was not set. The " +
                                        "following location will be used: " + 
                                        Constants.METADATA_REGISTRY_CONFIG_FILE);
                    }
                    configurationFileLoc = Constants.METADATA_REGISTRY_CONFIG_FILE;
                }
                loadConfigFromFile();
        }
        
        /**
         * FactoryRegistry is currently a static singleton
         */
        private MetadataFactoryRegistry() {
        }
        
        /**
         * getFactory
         * @param intface of the Factory
         * @return Object that is the factory implementation for the intface
         */
        public static Object getFactory(Class intface) {
                return table.get(intface);
        }
        
        /**
         * setFactory
         * @param intface
         * @param factoryObject
         */
        public static void setFactory(Class intface, Object factoryObject){
                table.put(intface, factoryObject);
        }
        
        /**
         * This method will load a file, if it exists, that contains a list
         * of interfaces and custom implementations. This allows for non-
         * programmatic registration of custom interface implementations
         * with the MDQ layer.
         */
        private static void loadConfigFromFile() {
            String pairSeparator = "|";
            try {
                ClassLoader classLoader = getContextClassLoader(null);
                URL url = null;
                url = classLoader.getResource(configurationFileLoc);
                if(url == null) {
                    File file = new File(configurationFileLoc);
                    url = file.toURL();
                }
                // the presence of this file is optional
                if(url != null) {
                    if(log.isDebugEnabled()) {
                        log.debug("Found URL to MetadataFactoryRegistry configuration file: " +
                                  configurationFileLoc);
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    String line = reader.readLine();
                    
                    // the separator of the file is the '|' character
                    // to the left of the separator will be the interface and
                    // to the right will be the custom implementation
                    if(line != null && 
                            line.indexOf("|") != -1) {
                        String interfaceName = line.substring(0, line.indexOf(pairSeparator));
                        String implName = line.substring(line.indexOf(pairSeparator) + 1, 
                                line.length());
                        if(log.isDebugEnabled()) {
                            log.debug("For registered class: " + interfaceName + " the " +
                                    "following implementation was found: " + implName);
                        }
                        Class intf = classLoader.loadClass(interfaceName);
                        Class impl = classLoader.loadClass(implName);
                        
                        // if we could load both we need to register them with our
                        // internal registry
                        if(intf != null && impl != null) {
                            if(log.isDebugEnabled()) {
                                log.debug("Loaded both interface and implementation class: " + 
                                        interfaceName + ":" + implName);
                            }
                            if(impl.getEnclosingClass() == null) {
                                table.put(intf, impl.newInstance()); 
                            }else {
                                if(log.isWarnEnabled()) {
                                    log.warn("The implementation class: " + impl.getClass().getName() + 
                                             " could not be lregistered because it is an inner class. " +
                                             "In order to register file-based overrides, implementations " +
                                             "must be public outer classes.");
                                }
                            }
                        }else {
                            if(log.isDebugEnabled()) {
                                log.debug("Could not load both interface and implementation class: " +
                                        interfaceName + ":" + implName);
                            }
                        }
                    }else {
                        if(log.isDebugEnabled()) {
                            log.debug("Did not find File for MetadataFactoryRegistry configuration " +
                                    "file: " + configurationFileLoc);
                        }
                    }
                }else {
                    if(log.isDebugEnabled()) {
                        log.debug("Did not find URL for MetadataFactoryRegistry configuration " +
                                "file: " + configurationFileLoc);
                    }
                }
            }
            catch(Throwable t) {
                if(log.isDebugEnabled()) {
                    log.debug("The MetadataFactoryRegistry could not process the configuration file: " + 
                             configurationFileLoc + " because of the following error: " + t.toString());
                }
            }
        }

        /**
         * Package protected method to allow tests to set the location
         * of the configuration file.
         * @param configFileName
         */
        static void setConfigurationFileLocation(String configFileLoc) {
            configurationFileLoc = configFileLoc;
            loadConfigFromFile();
        }

    private static ClassLoader getContextClassLoader(final ClassLoader classLoader) {
        ClassLoader cl;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e.getMessage(), e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }
}
