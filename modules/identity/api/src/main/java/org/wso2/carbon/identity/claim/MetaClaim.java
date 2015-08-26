/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim;

import org.wso2.carbon.identity.authn.StoreIdentifier;
import org.wso2.carbon.identity.commons.AttributeIdentifier;

import java.util.Collections;
import java.util.Map;

public class MetaClaim {

    private ClaimIdentifier claimIdentifier;
    private Map<String, AttributeIdentifier> attributeIdentifierMap;
    private Map<String, String> properties;

    /**
     * @param claimIdentifier
     * @param attributeIdentifierMap
     */
    public MetaClaim(ClaimIdentifier claimIdentifier,
                     Map<String, AttributeIdentifier> attributeIdentifierMap) {
        this.claimIdentifier = claimIdentifier;
        this.attributeIdentifierMap = attributeIdentifierMap;
    }

    /**
     * @param claimUri
     * @param attributeIdentifierMap
     * @param properties
     */
    public MetaClaim(ClaimIdentifier claimUri,
                     Map<String, AttributeIdentifier> attributeIdentifierMap, Map<String, String> properties) {
        this.claimIdentifier = claimUri;
        this.attributeIdentifierMap = attributeIdentifierMap;
        this.properties = properties;
    }

    /**
     * @param storeIdentifier
     * @return
     */
    public AttributeIdentifier getAttributeIdentifier(StoreIdentifier storeIdentifier) {
        return attributeIdentifierMap.get(storeIdentifier.getValue());
    }

    /**
     * @return
     */
    public Map<String, AttributeIdentifier> getAttributeIdentifierMap() {
        return Collections.unmodifiableMap(attributeIdentifierMap);
    }

    /**
     * @return
     */
    public ClaimIdentifier getClaimIdentifier() {
        return claimIdentifier;
    }

    /**
     * @return
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}