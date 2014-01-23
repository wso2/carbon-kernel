/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.tomcat.ext.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is to hold URL mappings Vs actual URL in order to use them at run time.
 */
public class URLMappingHolder {
    /**
     * concurrent Map to hold the URL mappings and actual URL from all tenants.
     */
    private static ConcurrentHashMap<String, String> urlMappingOfApplication = new ConcurrentHashMap<String, String>();

    private static URLMappingHolder urlMappingHolder = new URLMappingHolder();
    
    private String defaultHost;

    private URLMappingHolder() {
    }

    public static URLMappingHolder getInstance() {
         return urlMappingHolder;
    }

    public  String getDefaultHost() {
        return defaultHost;
    }

    public  void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
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
