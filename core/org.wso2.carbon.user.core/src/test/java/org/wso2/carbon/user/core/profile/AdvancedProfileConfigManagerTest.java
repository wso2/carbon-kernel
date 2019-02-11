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
package org.wso2.carbon.user.core.profile;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.builder.ClaimBuilder;
import org.wso2.carbon.user.core.claim.dao.ClaimDAO;
import org.wso2.carbon.user.core.profile.builder.ProfileConfigurationBuilder;
import org.wso2.carbon.user.core.profile.dao.ProfileConfigDAO;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdvancedProfileConfigManagerTest extends BaseTestCase {
    private ProfileConfigurationManager profileConfigMan;
    private BasicDataSource ds;
    private String realmName="test";
    private int tenantId = MultitenantConstants.SUPER_TENANT_ID;
    
    public void setUp() throws Exception {
            super.setUp();
    }


    public void testProfileConfigManager() throws Exception{
        initObjStuff();
        doClaimStuff();
        //doProfileConfigManStuff();
    }


    public void initObjStuff() throws Exception{
        String dbFolder = "target/proftest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:./target/proftest/CARBON_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
    }


    public void doClaimStuff()throws Exception{
        ClaimDAO claimDao = new ClaimDAO(ds,tenantId);
        Map<String, ClaimMapping> defaultClaims;
        Map<String, ClaimMapping> FromDB;

        defaultClaims = ClaimTestUtil.getClaimTestData();
        claimDao.addCliamMappings(defaultClaims.values().toArray(new ClaimMapping[defaultClaims.size()]));
        //the mappings have not been added to the db in given order at ClaimTestUtil
        ClaimBuilder builder = new ClaimBuilder(0);
    }


    public void doProfileConfigManStuff() throws Exception {

        String[] claimset = {ClaimTestUtil.CLAIM_URI1,ClaimTestUtil.CLAIM_URI2,ClaimTestUtil.CLAIM_URI3};
        ProfileConfiguration[] newProfConfigs;
        ProfileConfigDAO dao = new ProfileConfigDAO(ds,tenantId);
        ProfileConfigurationBuilder builder = new ProfileConfigurationBuilder(tenantId);
        Map<String, ProfileConfiguration> newProfileMaps;

        //add profile configuration
        newProfConfigs = createProfiles();
        dao.addProfileConfig(newProfConfigs);
        
        ProfileConfiguration TestProfile=null;
        try{
            dao.addProfileConfig(TestProfile);
            fail("Exception at Null Profile Configuration failed");
        }catch(Exception e){
             //caught exception
        }
        try{
            newProfileMaps = builder.buildProfileConfigurationFromDatabase(null,realmName);
            fail("Exception at Null Data Source failed");
        }catch(Exception e){
            //caught exception
        }

        //build Profile configuration from database
        newProfileMaps = builder.buildProfileConfigurationFromDatabase(ds,realmName);

        profileConfigMan = new DefaultProfileConfigurationManager(newProfileMaps,ds,tenantId);

        ProfileConfiguration p4 = new ProfileConfiguration();
        p4.setProfileName("week");
        p4.addHiddenClaim(claimset[0]);
        p4.addInheritedClaim(claimset[1]);
        p4.addOverriddenClaim(claimset[2]);
        p4.setDialectName("http://wso2.org/");

        ProfileConfiguration p5 = new ProfileConfiguration();
        p5.setProfileName("week");
        p5.addHiddenClaim(claimset[0]);
        p5.addInheritedClaim(claimset[1]);
        p5.addOverriddenClaim(claimset[2]);
        p5.setDialectName("http://wso2.org5/");

        ProfileConfiguration p6 = new ProfileConfiguration();


        //add configurations to manager
        profileConfigMan.addProfileConfig(p4);
        assertEquals(1,profileConfigMan.getProfileConfig("week").getHiddenClaims().size());
        assertEquals("http://wso2.org2/givenname2",profileConfigMan.getProfileConfig("week").getInheritedClaims().get(0));

        try{
            profileConfigMan.addProfileConfig(p5);
            fail("Exception occured at Incorrect Dialect Name fails");
        }catch(Exception e){
            //caught exception
        }
        try{
            profileConfigMan.addProfileConfig(p6);
            fail("Exception occured at Null Profile Configuration fails");
        }catch(Exception e){
            //caught Exception
        }


        //get profile by name
        ProfileConfiguration pConfig1 = (ProfileConfiguration) profileConfigMan.getProfileConfig("Party");
        assertEquals("Party",pConfig1.getProfileName());
        assertEquals(2,pConfig1.getOverriddenClaims().toArray().length);
        System.out.println();
        try{
            String pConfig2 = profileConfigMan.getProfileConfig(null).getProfileName();
            fail("Exception occured at Null Profile Name Failed");
        }catch(Exception e){
            //caught Exception
        }


        //update a profile
        p4.addHiddenClaim(claimset[1]);
        profileConfigMan.updateProfileConfig(p4);
        assertEquals(2,profileConfigMan.getProfileConfig("week").getHiddenClaims().toArray().length);
        try{
            profileConfigMan.updateProfileConfig(p5);
            fail("Exception occured at Incorrect Dialect Name fails");
        }catch(Exception e){
            //caught Exception
        }
        try{
            profileConfigMan.updateProfileConfig(p6);
            fail("Exception occured at null Profile Configuration fails");
        }catch(Exception e){
           //caught Exception
        }


        //get all profiles
        ProfileConfiguration[] arr = (ProfileConfiguration[]) profileConfigMan.getAllProfiles();
        assertEquals(5,arr.length); //Profiles not in order


        //delete profiles
        profileConfigMan.deleteProfileConfig(profileConfigMan.getProfileConfig("Residence"));
        assertEquals(4,profileConfigMan.getAllProfiles().length);
        
        try{
            profileConfigMan.deleteProfileConfig(null);
            fail("Exception at Deleting Null ProfileConfiguration Fails");
        }catch(Exception e){
            //caught Exception
        }
        try{
            profileConfigMan.deleteProfileConfig(profileConfigMan.getProfileConfig("Residence"));
            fail("Exception at Deleting an already Deleted ProfileConfiguration Fails");
        }catch(Exception e){
            //caught exception
        }

    }

    
    public ProfileConfiguration[] createProfiles() throws Exception{
        
        ProfileConfiguration[] PConf = new ProfileConfiguration[4];
        String[] claims = {ClaimTestUtil.CLAIM_URI1,ClaimTestUtil.CLAIM_URI2,ClaimTestUtil.CLAIM_URI3};
        List<String> claimList1 = new ArrayList<String>();
        claimList1.add(claims[0]);claimList1.add(claims[1]);
        List<String> claimList2 = new ArrayList<String>();
        claimList2.add(claims[1]); claimList2.add(claims[2]);

        ProfileConfiguration p1 = new ProfileConfiguration();
        p1.setProfileName("Residence");
        p1.addHiddenClaim(claims[0]);
        p1.addInheritedClaim(claims[1]);
        p1.addOverriddenClaim(claims[2]);
        p1.setDialectName("http://wso2.org/");
        assertEquals("Residence",p1.getProfileName());
        assertEquals("http://wso2.org/givenname",p1.getHiddenClaims().get(0));

        ProfileConfiguration p2 = new ProfileConfiguration();
        p2.setProfileName("Office");
        p2.setHiddenClaims(claimList1);
        p2.addInheritedClaim(claims[2]);
        p2.setDialectName("http://wso2.org/");
        assertEquals("http://wso2.org/givenname3",p2.getInheritedClaims().get(0));

        ProfileConfiguration p3 = new ProfileConfiguration();
        p3.setProfileName("Field");
        p3.addHiddenClaim(claims[0]);
        p3.setOverriddenClaims(claimList1);
        p3.addInheritedClaim(claims[2]);
        p3.setDialectName("http://wso2.org/");
        assertEquals("http://wso2.org2/givenname2",p3.getOverriddenClaims().get(1));

        ProfileConfiguration p4 = new ProfileConfiguration();
        p4.setProfileName("Party");
        p4.addHiddenClaim(claims[0]);
        p4.addInheritedClaim(claims[1]);
        p4.setOverriddenClaims(claimList2);
        p4.setDialectName("http://wso2.org/");
        assertEquals("http://wso2.org/",p4.getDialectName());

        PConf[0]=p1;PConf[1]=p2; PConf[2]=p3; PConf[3]=p4;

        return PConf;
    }
}
