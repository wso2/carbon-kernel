/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.dto;

import org.wso2.carbon.user.core.claim.ClaimMapping;

import java.util.Map;

/**
 * ClaimConfig class used to create ClaimConfig object which is containing claims and properties of those claims. After
 * reading the claim-config.xml file, which is done inside FileBasedClaimBuilder class. ClaimConfig will be created.
 */

public class ClaimConfig {

    /**
     * contains claim uri as the key and claim mapping of each claim as the value.
     */
    private Map<String, ClaimMapping> claims;

    /**
     * inside map contains meta data value as the key and value of that meta data as the value (including claim dialect
     * info). PropertyHolder map contains that meta data map and the related claim uri as key value.
     */
    private Map<String, Map<String, String>> propertyHolder;

    public ClaimConfig() {

    }

    /**
     * Contains the claims and the related meta data info.
     *
     * @param claims         contains claim uri as the key and claim mapping of each claim as the value.
     * @param propertyHolder inside map contains meta data value as the key and value of that meta data as the value
     *                       (including claim dialect info). PropertyHolder map contains that meta data map and the
     *                       related claim uri as key value.
     */

    public ClaimConfig(Map<String, ClaimMapping> claims, Map<String, Map<String, String>> propertyHolder) {
        this.claims = claims;
        this.propertyHolder = propertyHolder;
    }

    public Map<String, ClaimMapping> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, ClaimMapping> claims) {
        this.claims = claims;
    }

    public Map<String, Map<String, String>> getPropertyHolder() {
        return propertyHolder;
    }

    public void setPropertyHolder(Map<String, Map<String, String>> propertyHolder) {
        this.propertyHolder = propertyHolder;
    }

}
