/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.api;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the claim mapping between a given claim to the actual
 * attribute which resides in the user store.
 */
public class ClaimMapping {

    /**
     * The claim object
     */
    protected Claim claim;

    /**
     * The mapped attribute in the user store
     */
    protected String mappedAttribute;

    protected Map<String, String> mappedAttributes = new HashMap<String, String>();

    public ClaimMapping() {

    }

    public ClaimMapping(Claim claim, String mappedAttribute) {
        this.claim = claim;
        this.mappedAttribute = mappedAttribute;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public String getMappedAttribute() {
        return mappedAttribute;
    }

    public void setMappedAttribute(String mappedAttribute) {
        int index;
        if (mappedAttribute != null) {
            index = mappedAttribute.indexOf("/");

            if (index > 0) {
                String domainName = mappedAttribute.substring(0, index);
                if (domainName != null && mappedAttribute != null) {
                    mappedAttributes.put(domainName.toUpperCase(),
                            mappedAttribute);
                }
            } else {
                this.mappedAttribute = mappedAttribute;
            }
        }
    }

    public void setMappedAttribute(String domainName, String mappedAttribute) {
        if (domainName != null && mappedAttribute != null) {
            mappedAttributes.put(domainName.toUpperCase(), mappedAttribute);
        }
        if (domainName == null) {
            this.mappedAttribute = mappedAttribute;
        }
    }

    public String getMappedAttribute(String domainName) {
        if (domainName != null) {
            return mappedAttributes.get(domainName.toUpperCase());
        } else {
            return null;
        }
    }

    public Map<String, String> getMappedAttributes() {
        return mappedAttributes;

    }

    public void setMappedAttributes(Map<String, String> attrMap) {
        mappedAttributes = attrMap;
    }

    public void setMappedAttributeWithNoDomain(String mappedAttribute) {
        this.mappedAttribute = mappedAttribute;
    }

    public String getMappedAttributeWithNoDomain() {
        return mappedAttribute;
    }
}
