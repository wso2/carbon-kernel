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

package org.apache.axis2.deployment.util;

/**
 * this class is used to keep the excludeProperties and includePropertes
 * of the given bean when generating the wsdl, and serializing.
 */
public class BeanExcludeInfo {

    // exclude property list given as a regualar expression
    // but we exclude this property only if it is not available
    // in the include properties list
    private String excludeProperties;
    private String includeProperties;

    public BeanExcludeInfo(String excludeProperties, String includeProperties) {
        this.excludeProperties = excludeProperties;
        this.includeProperties = includeProperties;
    }

    public String getExcludeProperties() {
        return excludeProperties;
    }

    public void setExcludeProperties(String excludeProperties) {
        this.excludeProperties = excludeProperties;
    }

    public String getIncludeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(String includeProperties) {
        this.includeProperties = includeProperties;
    }

    /**
     * a property is excluded if it ths given in the exclude list
     * but not in include list
     * @param property
     * @return is exclude the property or not
     */
    public boolean isExcludedProperty(String property){
       boolean isExclude = false;
       if ((excludeProperties != null) && (excludeProperties.trim().length() > 0)){
           if (property.matches(excludeProperties)){
               isExclude = true;
               if ((includeProperties != null) && (includeProperties.trim().length() > 0)){
                   if (property.matches(includeProperties)){
                       isExclude = false;
                   }
               }
           }
       }
       return isExclude;
    }


}
