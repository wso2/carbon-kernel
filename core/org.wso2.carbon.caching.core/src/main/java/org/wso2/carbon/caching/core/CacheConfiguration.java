/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.caching.core;

import net.sf.jsr107cache.CacheException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.CarbonBaseUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

/**
 * Configuration used by the CarbonCaching implementation.
 */
@SuppressWarnings("unused")
public final class CacheConfiguration {

    private static CacheConfiguration instance = new CacheConfiguration();
    
    public static final long DEFAULT_EXPIRATION = 900000L;     // 15 mins
    public static final long DEFAULT_EXPIRATION_L1 = 600000L;      // 10 mins
    public static final int MAX_ELEMENTS = 5000;      // 10 mins
    
    public static final String CACHE_MODE_REPLICATED = "replicated";
    public static final String CACHE_MODE_DISTRIBUTED = "distributed";
    public static final String CACHE_MODE_INVALIDATION = "invalidation";
    public static final String CACHE_MODE_LOCAL = "local";

    private Map<String, List<String>> configuration = null;
    private CarbonCacheManager cacheManager = null;
    private static final Log log = LogFactory.getLog(CacheConfiguration.class);

    // private constructor preventing initialization.
    private CacheConfiguration() {
    }

    /**
     * Method to obtain the singleton cache configuration object.
     *
     * @return the cache configuration object instance.
     */
    public static CacheConfiguration getInstance() {
        CarbonBaseUtils.checkSecurity();
        return instance;
    }

    /**
     * Method to obtain the cache manager instance.
     *
     * @return the cache manager instance.
     *
     * @throws CacheException if the operation failed.
     */
    public synchronized CarbonCacheManager getCacheManager() throws CacheException {
        if (cacheManager != null) {
            return cacheManager;
        }
        String cacheManagerClass = getProperty("cacheManager");
        if (cacheManagerClass == null) {
            throw new RuntimeException("The cache manager class was not defined");
        }
        try {
            Class clazz = Class.forName(cacheManagerClass);
            cacheManager = (CarbonCacheManager) clazz.newInstance();
            return cacheManager;
        } catch (ClassNotFoundException e) {
            throw new CacheException("No cache manager class by the name " + cacheManagerClass
                    + " was found.", e);
        } catch (Exception e) {
            throw new CacheException("Unable to create an instance of a cache manager.", e);
        }
    }

    /**
     * Method to load the cache configuration.
     * 
     * @param configFilePath the path of the cache configuration file.
     *
     * @throws CacheException if the operation failed.
     */
    public void load(String configFilePath) throws CacheException {
        if (this.configuration != null) {
            // The cache configuration has already been loaded.
            return;
        }
        if (configFilePath == null) {
            throw new CacheException("the path of the cache configuration file was not specified.");
        }
        InputStream configInputStream;
        try {
            configInputStream = new FileInputStream(new File(configFilePath));
        } catch (FileNotFoundException e) {
            throw new CacheException("Cache configuration file (cache.xml) does not exist in the " +
                    "path " + configFilePath, e);
        }
        try {
            OMElement documentElement = new StAXOMBuilder(configInputStream).getDocumentElement();
            Map<String, List<String>> configuration = new HashMap<String, List<String>>();
            readChildElements(documentElement, new Stack<String>(), configuration);
            synchronized (this) {
                if (this.configuration == null) {
                    this.configuration = configuration;
                }
            }
            log.debug("Successfully loaded Cache Configuration");
        } catch (XMLStreamException e) {
            throw new CacheException("Unable to parse the cache configuration.", e);
        } finally {
            try {
                configInputStream.close();
            } catch (IOException ignore) {
                // We only want to ensure that the configuration file is properly closed. Throwing
                // an exception here will mask any exceptions that have already been thrown.
            }
        }
    }

    private void readChildElements(OMElement cacheConfiguration,
                                   Stack<String> nameStack,
                                   Map<String, List<String>> configuration) {
        Iterator children = cacheConfiguration.getChildElements();
        while (children.hasNext()) {
            OMElement element = (OMElement) children.next();
            nameStack.push(element.getLocalName());
            String value = element.getText();
            if (value != null) {
                value = value.trim();
                if (value.length() > 0) {
                    String key = getKey(nameStack);
                    List<String> currentObject = configuration.get(key);
                    if (currentObject == null) {
                        currentObject = new ArrayList<String>(1);
                        currentObject.add(value);
                        configuration.put(key, currentObject);
                    } else if (!currentObject.contains(value)) {
                        currentObject.add(value);
                        configuration.put(key, currentObject);
                    }
                }
            }
            readChildElements(element, nameStack, configuration);
            nameStack.pop();
        }
    }

    private String getKey(Stack<String> nameStack) {
        StringBuffer key = new StringBuffer(nameStack.elementAt(0));
        for (int i = 1; i < nameStack.size(); i++) {
            key.append(".").append(nameStack.elementAt(i));
        }
        return key.toString();
    }

    /**
     * Method to obtain the value of the first property with the given key.
     *
     * @param key the key.
     *
     * @return the value.
     */
    public String getProperty(String key) {
        List<String> properties = configuration.get(key);
        return (properties != null) ? properties.get(0) : null;
    }

    /**
     * Method to obtain the values of the properties with the given key.
     *
     * @param key the key.
     *
     * @return the values.
     */
    public String[] getProperties(String key) {
        List<String> properties = configuration.get(key);
        return (properties != null) ?
                properties.toArray(new String[properties.size()]) : new String[0];
    }

}
