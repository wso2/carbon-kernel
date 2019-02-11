/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * The factory to create registry. As we create registries with OSGI services, this class got
 * obsolete.
 */
@Deprecated
public abstract class RegistryFactory {

    public static final String PROPFILE_PROPERTY = "registry.propFile";
    public static final String FACTORY_CLASS_PROPERTY = "registry.factoryClass";
    public static final String REG_CLASS_PROPERTY = "registry.class";
    public static final String PROPERTY_FILE_NAME = "registry.properties";

    /**
     * Get a Registry instance, using the defaults.
     *
     * @return a fully initialized Registry instance.
     * @throws RegistryException if there is a problem
     */
    public static RegistryFactory newInstance() throws RegistryException {
        String propertyFileName = System.getProperty(PROPFILE_PROPERTY);
        return newInstance(propertyFileName);
    }

    /**
     * Get a Registry instance, using the specified property file.
     *
     * @param propertyFileName the file name of a properties file, or null for the default.
     *
     * @return a fully initialized Registry instance.
     * @throws RegistryException if there is a problem
     */
    public static RegistryFactory newInstance(String propertyFileName) throws RegistryException {
        if (propertyFileName == null) {
            propertyFileName = PROPERTY_FILE_NAME;
        }

        // Look for properties file
        Properties properties = new Properties();
        File props = new File(propertyFileName);
        if (props.exists()) {
            try {
                FileInputStream fileIn = null;
                try {
                    fileIn = new FileInputStream(props);
                    properties.load(fileIn);
                } finally {
                    if (fileIn != null) {
                        fileIn.close();
                    }
                }
            } catch (IOException e) {
                throw new RegistryException("Couldn't load properties file '" + propertyFileName +
                        "'");
            }
        }

        return newInstance(properties);
    }

    /**
     * Get a Registry, passing specific configuration information via a Properties object.
     *
     * @param properties configuration properties to affect the Registry returned
     *
     * @return an initialized Registry instance
     * @throws RegistryException if there is a problem
     */
    public static RegistryFactory newInstance(Properties properties) throws RegistryException {
        Class regClass = InMemoryEmbeddedRegistry.class;

        if (properties != null) {
            String factoryClassname = properties.getProperty(FACTORY_CLASS_PROPERTY);
            if (factoryClassname != null) {
                Class factoryClass;

                try {
                    factoryClass = Class.forName(factoryClassname);
                } catch (ClassNotFoundException e) {
                    throw new RegistryException("Couldn't load factory class " +
                            factoryClassname, e);
                }
                try {
                    Constructor c = factoryClass.getConstructor(Properties.class);
                    try {
                        return (RegistryFactory) c.newInstance(properties);
                    } catch (Exception e) {
                        throw new RegistryException("Couldn't create factory of type " +
                                factoryClassname, e);
                    }
                } catch (NoSuchMethodException e) {
                    // No property-reading constructor.  OK, if there's a default constructor
                    // let's just use this class anyway.
                    try {
                        return (RegistryFactory) factoryClass.newInstance();
                    } catch (Exception e1) {
                        // Nope - don't know how to deal!
                        throw new RegistryException("Couldn't create factory of type " +
                                factoryClassname, e);
                    }
                }
            }
            String registryClassname = properties.getProperty(REG_CLASS_PROPERTY);
            if (registryClassname != null) {
                try {
                    regClass = Class.forName(registryClassname);
                } catch (ClassNotFoundException e) {
                    throw new RegistryException("Couldn't load Registry class " +
                            registryClassname, e);
                }
            }
        }

        // Return the default.
        return new BasicFactory(regClass);
    }

    /**
     * Get a Registry with no security credentials.
     *
     * @return a correctly configured Registry instance
     * @throws RegistryException if a Registry couldn't be created
     */
    public abstract Registry getRegistry() throws RegistryException;

    /**
     * Get a Registry with the provided credentials.
     *
     * @param username username to connect with
     * @param password password to connect with
     *
     * @return a correctly configured Registry instance
     * @throws RegistryException if a Registry couldn't be created
     */
    public abstract Registry getRegistry(String username, String password) throws RegistryException;
}

