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

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.ClaimTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.claim.builder.ClaimBuilder;
import org.wso2.carbon.user.core.claim.dao.ClaimDAO;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

public class AdvancedClaimManagerTest extends BaseTestCase {
    private ClaimManager claimMan;
    private BasicDataSource ds;

    public void setUp() throws Exception {
            super.setUp();
    }


     public void testClaimManger() throws Exception{
        initObjStuff();
        doClaimStuff();
     }


    public void initObjStuff() throws Exception{

        String dbFolder = "target/claimtest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:target/claimtest/CARBON_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();

    }


    public void doClaimStuff() throws Exception{
        ClaimDAO claimDao = new ClaimDAO(ds,0);
        Map<String, ClaimMapping> defaultClaims;
        Map<String, ClaimMapping> FromDB;

        String claimU1 = ClaimTestUtil.CLAIM_URI1;
        String claimU2 = ClaimTestUtil.CLAIM_URI2;
        String claimU3 = ClaimTestUtil.CLAIM_URI3;

        //add claims to database
        defaultClaims = ClaimTestUtil.getClaimTestData();
        claimDao.addCliamMappings(defaultClaims.values().toArray(new ClaimMapping[defaultClaims.size()]));
        try{
            claimDao.addCliamMappings(null);
            fail("Exception at Null Claim Mappings fail");
        }catch(Exception e){
            //caught exception
        }
        FromDB=doClaimBuilderStuff();
        claimMan = new DefaultClaimManager(FromDB,ds,0);
        
        // get all the claim URIs
        String[] ClmURI = claimMan.getAllClaimUris();
        assertEquals(3,ClmURI.length);
        
        // get the attribute name for a given claimURI
        try{
            assertEquals(null,claimMan.getAttributeName(null));
            fail("Exception at Invalid Claim URI fails");
        }catch(Exception ex){
            //caught exception
        }

        //get the claim for a given claimURI
        Claim c1 = (Claim) claimMan.getClaim(claimU1);
        assertEquals("http://wso2.org/",c1.getDialectURI());
        assertEquals("Given Name",c1.getDisplayTag());
        assertEquals("ty&*RegEx",c1.getRegEx());
        assertEquals("The description is nutts",c1.getDescription());

        Claim c2 = (Claim) claimMan.getClaim("http://aaa");
        try{
            assertEquals(null,c2.getDialectURI());
            fail("Exception at Invalid Claim URI fails");
        }catch(Exception ex){
            //caught exception
        }

        //get the ClaimMapping for a given URI
        ClaimMapping cm1 = (ClaimMapping) claimMan.getClaimMapping(claimU2);
        assertEquals("attr2",cm1.getMappedAttribute());

        
        ClaimMapping cm2 = (ClaimMapping) claimMan.getClaimMapping("http://aaa");
        try{
            assertEquals(null,cm2.getMappedAttribute());
            fail("Exception at Invalid Claim URI fails");
        }catch(Exception ex){
           //caught exception
        }

        //lists all the claims asked at the registration
        ClaimMapping[] CM1 = (ClaimMapping[]) claimMan.getAllSupportClaimMappingsByDefault();
        Claim C1 = CM1[0].getClaim();
        Arrays.sort(CM1,new ClaimSorter());//sorting C1 array
        assertEquals("Given Name",C1.getDisplayTag());
        

        //lists all the claims required at the time user registration
        ClaimMapping[] CM2 = (ClaimMapping[]) claimMan.getAllRequiredClaimMappings();
        Arrays.sort(CM2,new ClaimSorter());//sorting C2 array

        assertEquals(3,CM2.length);
        Claim C2 = CM2[2].getClaim();
        assertEquals("Given Name3",C2.getDisplayTag());
        assertEquals(3,claimMan.getAllRequiredClaimMappings().length);
        assertEquals(3,claimMan.getAllSupportClaimMappingsByDefault().length);
        assertEquals(3,claimMan.getAllClaimMappings().length);


        //void addNewClaimMapping(ClaimMapping mapping) add new mappings
        ClaimMapping[] NewclaimMapping = AdvancedClaimManagerTest.makeClaimMap();
        for(ClaimMapping x:NewclaimMapping){
            claimMan.addNewClaimMapping(x);
        }
        assertEquals(4,claimMan.getAllSupportClaimMappingsByDefault().length);
        assertEquals(4,claimMan.getAllRequiredClaimMappings().length);
        assertEquals(6,claimMan.getAllClaimMappings().length);


        //update an existing mapping
        NewclaimMapping[1].getClaim().setDescription("The Update claim5");
        NewclaimMapping[1].getClaim().setRequired(true);
        claimMan.updateClaimMapping(NewclaimMapping[1]);

        assertEquals(5,claimMan.getAllRequiredClaimMappings().length);
        assertEquals("The Update claim5",NewclaimMapping[1].getClaim().getDescription());


        //delete an existing mapping
        claimMan.deleteClaimMapping(NewclaimMapping[0]);
        assertEquals(4,claimMan.getAllRequiredClaimMappings().length);
        assertEquals(4,claimMan.getAllSupportClaimMappingsByDefault().length);
        assertEquals(5,claimMan.getAllClaimMappings().length);
    }

    public Map<String, ClaimMapping> doClaimBuilderStuff() throws Exception{
        ClaimBuilder claimB = new ClaimBuilder(0);
        Map<String, ClaimMapping> claims ;

        claims = claimB.buildClaimMappingsFromDatabase(ds,"test");
       
       return claims;
    }

    public static ClaimMapping[] makeClaimMap(){
        
        Claim claim4 = new Claim();
        claim4.setClaimUri("http://wso2.org/givenname4");
        claim4.setDescription("The new claim4");
        claim4.setDialectURI("http://wso2.or42/");
        claim4.setDisplayTag("Given Name4");
        claim4.setRegEx("ty&*RegEx4");
        claim4.setRequired(true);
        claim4.setSupportedByDefault(false);

        Claim claim5 = new Claim();
        claim5.setClaimUri("http://wso2.org/givenname5");
        claim5.setDescription("The new claim5");
        claim5.setDialectURI("http://wso2.org5/");
        claim5.setDisplayTag("Given Name5");
        claim5.setRegEx("ty&*RegEx5");
        claim5.setRequired(false);
        claim5.setSupportedByDefault(true);

        Claim claim6 = new Claim();
        claim6.setClaimUri("http://wso2.org/givenname6");
        claim6.setDescription("The new claim6");
        claim6.setDialectURI("http://wso2.org6/");
        claim6.setDisplayTag("Given Name6");
        claim6.setRegEx("ty&*RegEx6");
        claim6.setRequired(false);
        claim6.setSupportedByDefault(false);

        ClaimMapping cm4 = new ClaimMapping();
        cm4.setClaim(claim4);
        cm4.setMappedAttribute("attr4");

        ClaimMapping cm5 = new ClaimMapping();
        cm5.setClaim(claim5);
        cm5.setMappedAttribute("attr5");

        ClaimMapping cm6 = new ClaimMapping();
        cm6.setClaim(claim6);
        cm6.setMappedAttribute("attr6");

        ClaimMapping[] claimMapArry = new ClaimMapping[3];
        claimMapArry[0]=cm4;
        claimMapArry[1]=cm5;
        claimMapArry[2]=cm6;

        return claimMapArry;

    }

}
