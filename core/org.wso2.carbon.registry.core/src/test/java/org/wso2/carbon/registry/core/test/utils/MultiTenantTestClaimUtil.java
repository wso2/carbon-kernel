/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.test.utils;

import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MultiTenantTestClaimUtil {
    
    public static final String CLAIM_URI1 = "http://wso2.org/givenname";
    public static final String CLAIM_URI2 = "http://wso2.org2/givenname2";
    public static final String CLAIM_URI3 = "http://wso2.org/givenname3";
    public static final String HOME_PROFILE_NAME = "HomeProfile";

    public static Map<String, ClaimMapping> getClaimTestData() {
        Map<String, ClaimMapping> claims = new HashMap<String, ClaimMapping>();
        Claim claim1 = new Claim();
        claim1.setClaimUri(CLAIM_URI1);
        claim1.setDescription("The description is nutts");
        claim1.setDialectURI("http://wso2.org/");
        claim1.setDisplayTag("Given Name");
        claim1.setRegEx("ty&*RegEx");
        claim1.setRequired(true);
        claim1.setSupportedByDefault(true);
        ClaimMapping cm1 = new ClaimMapping();
        cm1.setClaim(claim1);
        cm1.setMappedAttribute("attr1");
        claims.put("http://wso2.org/givenname", cm1);

        Claim claim2 = new Claim();
        claim2.setClaimUri(CLAIM_URI2);
        claim2.setDescription("The description is nutts2");
        claim2.setDialectURI("http://wso2.org2/");
        claim2.setDisplayTag("Given Name2");
        claim2.setRegEx("ty&*RegEx2");
        claim2.setRequired(true);
        claim2.setSupportedByDefault(true);
        ClaimMapping cm2 = new ClaimMapping();
        cm2.setClaim(claim2);
        cm2.setMappedAttribute("attr2");
        claims.put("http://wso2.org2/givenname2", cm2);

        Claim claim3 = new Claim();
        claim3.setClaimUri(CLAIM_URI3);
        claim3.setDescription("The description is nutts3");
        claim3.setDialectURI("http://wso2.org/");
        claim3.setDisplayTag("Given Name3");
        claim3.setRegEx("ty&*RegEx3");
        claim3.setRequired(true);
        claim3.setSupportedByDefault(true);
        ClaimMapping cm3 = new ClaimMapping();
        cm3.setClaim(claim3);
        cm3.setMappedAttribute("attr3");
        claims.put("http://wso2.org/givenname3", cm3);
     
        return claims;
        
    }

    public static Map<String, ProfileConfiguration> getProfileTestData() {
        Map<String, ProfileConfiguration> map = new HashMap<String, ProfileConfiguration>();
        ProfileConfiguration profConfig = new ProfileConfiguration();
        profConfig.setProfileName(HOME_PROFILE_NAME);
        profConfig.addHiddenClaim(CLAIM_URI1);
        profConfig.addInheritedClaim(CLAIM_URI2);
        profConfig.setDialectName("http://wso2.org/");
        map.put(HOME_PROFILE_NAME, profConfig);
        return map;
    }

}
