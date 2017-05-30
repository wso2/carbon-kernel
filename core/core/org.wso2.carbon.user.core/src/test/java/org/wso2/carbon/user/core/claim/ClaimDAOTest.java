/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.claim;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.claim.dao.ClaimDAO;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.dao.ProfileConfigDAO;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimDAOTest extends BaseTestCase {
    
    private ClaimDAO claimDAO = null;
    private Map<String, ClaimMapping> claims = new HashMap<String, ClaimMapping>();
    //private Map<String, ProfileConfiguration> profConfigs = null;
    //private ProfileConfigDAO profileDAO = null;

    public void setUp() throws Exception {
        super.setUp();
        
        String dbFolder = "target/ClaimTestDatabase";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
       // ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
       // ds.setUrl("jdbc:derby:target/ClaimTestDatabase/CARBON_TEST;create=true");

        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:./target/ClaimTestDatabase/CARBON_TEST");
        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();       
        
        claims = ClaimTestUtil.getClaimTestData();
        //profConfigs = ClaimTestUtil.getProfileTestData();
        claimDAO = new ClaimDAO(ds, 0);
        //profileDAO = new ProfileConfigDAO(ds, 0);
    }

    public void testClaimsAndProfilePersisting() throws Exception {
        checkCliamPersistStuff();
        checkProfilePersistStuff();
        checkDeleteDialectStuff();
    }

    public void checkCliamPersistStuff() throws Exception {

        //add
        claimDAO.addClaimMapping(claims.get(ClaimTestUtil.CLAIM_URI1));
        claimDAO.addClaimMapping(claims.get(ClaimTestUtil.CLAIM_URI2));
        claimDAO.addClaimMapping(claims.get(ClaimTestUtil.CLAIM_URI3));
        
        int count = claimDAO.getDialectCount();
        TestCase.assertEquals(2, count);
        List<ClaimMapping> lstActual = claimDAO.loadClaimMappings();
        ClaimMapping cm1 = lstActual.get(0);
        String claimUri = cm1.getClaim().getClaimUri();
        if (ClaimTestUtil.CLAIM_URI1.equals(claimUri)) {
            this.assertClaimMapping(claims.get(ClaimTestUtil.CLAIM_URI1), cm1);
        } else if (ClaimTestUtil.CLAIM_URI2.equals(claimUri)) {
            this.assertClaimMapping(claims.get(ClaimTestUtil.CLAIM_URI2), cm1);
        } else if (ClaimTestUtil.CLAIM_URI3.equals(claimUri)) {
            this.assertClaimMapping(claims.get(ClaimTestUtil.CLAIM_URI3), cm1);
        } else {
            TestCase.assertTrue(false);
        }
        
        //delete
        claimDAO.deleteClaimMapping(claims.get(ClaimTestUtil.CLAIM_URI2));
        count = claimDAO.getDialectCount();
        TestCase.assertEquals(2, count);

        //update
        claims.get(ClaimTestUtil.CLAIM_URI1).setMappedAttribute("zorus");
        claimDAO.updateClaim(claims.get(ClaimTestUtil.CLAIM_URI1));
    }

    public void checkProfilePersistStuff() throws Exception {
        //test add
//        profileDAO.addProfileConfig(profConfigs.get(ClaimTestUtil.HOME_PROFILE_NAME));
//        Map<String, ProfileConfiguration> map = profileDAO.loadProfileConfigs();
//        TestCase.assertEquals(1, map.size());
//        ProfileConfiguration gotConfig = map.get(ClaimTestUtil.HOME_PROFILE_NAME);
//        TestCase.assertEquals(gotConfig.getHiddenClaims().get(0), ClaimTestUtil.CLAIM_URI1);
//        TestCase.assertEquals(gotConfig.getInheritedClaims().size(), 0);
//        TestCase.assertEquals(gotConfig.getOverriddenClaims().size(), 0);
        
        //test update
//        ProfileConfiguration profConfig = profConfigs.get(ClaimTestUtil.HOME_PROFILE_NAME);
//        profConfig.setInheritedClaims(new ArrayList<String>());
//        profConfig.addOverriddenClaim(ClaimTestUtil.CLAIM_URI3);
//        profileDAO.updateProfileConfig(profConfig);
//        map = profileDAO.loadProfileConfigs();
//        gotConfig = map.get(profConfig.getProfileName());
//        TestCase.assertEquals(gotConfig.getHiddenClaims().get(0), ClaimTestUtil.CLAIM_URI1);
//        TestCase.assertEquals(gotConfig.getInheritedClaims().size(),0);
//        TestCase.assertEquals(gotConfig.getOverriddenClaims().get(0), ClaimTestUtil.CLAIM_URI3);
//        
//        //test delete
//        profileDAO.deleteProfileConfig(profConfig);
//        map = profileDAO.loadProfileConfigs();
//        TestCase.assertEquals(0, map.size());
//        
//        profileDAO.addProfileConfig(profConfig);
    }

    public void checkDeleteDialectStuff() throws Exception {
        claimDAO.deleteDialect("http://wso2.org2/");
    }

    private void assertClaimMapping(ClaimMapping cm1, ClaimMapping cm2) {
        Claim claim1 = cm1.getClaim();
        Claim claim2 = cm2.getClaim();
        TestCase.assertEquals(claim1.getClaimUri(), claim2.getClaimUri());
        TestCase.assertEquals(claim1.getDescription(), claim2.getDescription());
        TestCase.assertEquals(claim1.getDialectURI(), claim1.getDialectURI());
        TestCase.assertEquals(claim1.getDisplayTag(), claim2.getDisplayTag());
        TestCase.assertEquals(claim1.getRegEx(), claim2.getRegEx());
        TestCase.assertEquals(claim1.isRequired(), claim2.isRequired());
        TestCase.assertEquals(claim1.isSupportedByDefault(), claim2.isSupportedByDefault());
        TestCase.assertEquals(cm1.getMappedAttribute(), cm2.getMappedAttribute());
    }

}
