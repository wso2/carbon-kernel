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
package org.wso2.carbon.context;

import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.utils.multitenancy.CarbonApplicationContextHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContext {
    // The reason to why we decided to have a ApplicationContext is to follow the same flow of CarbonContext and
    // to keep the deployed application name and application specific information through out the whole carbon platform.

    private CarbonApplicationContextHolder carbonApplicationContextHolder = null;
    
    private static Map<String, String> urlMappingOfApplication = new HashMap<String, String>();

    /**
     * Creates a ApplicationContext using the given ApplicationContext holder as its backing instance.
     *
     * @param carbonApplicationContextHolder the ApplicationContext holder that backs this ApplicationContext object.
     *
     * @see ApplicationContext
     */
    protected ApplicationContext(CarbonApplicationContextHolder carbonApplicationContextHolder) {
        this.carbonApplicationContextHolder = carbonApplicationContextHolder;
    }

    /**
     * Utility method to obtain the current ApplicationContext holder after an instance of a
     * ApplicationContext has been created.
     *
     * @return the current ApplicationContext holder
     */
    protected CarbonApplicationContextHolder getCarbonApplicationContextHolder() {
        if (carbonApplicationContextHolder == null) {
            return CarbonApplicationContextHolder.getCurrentCarbonAppContextHolder();
        }
        return carbonApplicationContextHolder;
    }

    /**
     * Obtains the ApplicationContext instance stored on the ApplicationContext holder.
     *
     * @return the ApplicationContext instance.
     */
    public static ApplicationContext getCurrentApplicationContext() {
        return new ApplicationContext(null);
    }

    /**
     * Method to obtain the application name on this ApplicationContext instance.
     *
     * @return the Application Name.
     */
    public String getApplicationName() {
        CarbonBaseUtils.checkSecurity();
        return getCarbonApplicationContextHolder().getApplicationName();
    }

    /**
     * Method to put url mapping with application to the map
     *
     * @param urlMapping  url mapping for an application
     * @param application  application which has url mapping
     */
    public void putUrlMappingForApplication(String urlMapping, String application) {
        urlMappingOfApplication.put(urlMapping, application);
    }

    /**
     * Method to obtain url application of url mapping from the map.
     *
     * @param urlMapping url mapping for an application
     * @return  application which has url mapping
     */
    public String getApplicationFromUrlMapping(String urlMapping) {
        return urlMappingOfApplication.get(urlMapping);
    }

    /**
     * Method to remove url mapping from the map.
     *
     * @param urlMapping url mapping for an application
     */
    public void removeUrlMappingMap(String urlMapping) {
        urlMappingOfApplication.remove(urlMapping);
        
    }

    /**
     *  Method to check whether the mapping exists or not.
     *
     * @param urlMapping  url mapping for an application
     * @return  if mapping exists
     */
    public boolean isUrlMappingExists(String urlMapping) {
        return urlMappingOfApplication.containsKey(urlMapping);
    }

    /**
     * Method to get url mappings per application
     *
     * @param applicationContext the application to get the url mappings
     * @return  list of url mappings for an application
     */
    public List<String> getUrlMappingsPerApplication(String applicationContext) {
        List<String> urlMapping = new ArrayList<String>();
        
        for(String key: urlMappingOfApplication.keySet()) {
            if(urlMappingOfApplication.get(key).equalsIgnoreCase(applicationContext)) {
                urlMapping.add(key);
            }
        }
        return urlMapping;
    }

    /**
     * Method to get the whole url mapping.
     *
     * @return the map of url mapping
     */
    public Map<String, String> getUrlMappingOfApplication() {
        return urlMappingOfApplication;
    }
}