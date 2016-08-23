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
package org.wso2.carbon.user.core.hybrid;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;

public class AdvancedHybridRoleManagerTest extends BaseTestCase {

    private HybridRoleManager hybridRoleMan;

    public void setUp() throws Exception {
        super.setUp();
    }

    
    public void testHybridRoleManager() throws Exception {
        initRealmStuff();
        doHybridRoleStugg();
    }


    public void initRealmStuff() throws Exception {

        String dbFolder = "target/hybridroletest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:./target/hybridroletest/UM_ADV_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
 //       hybridRoleMan = new HybridRoleManager(ds, 0);
    }


    public void doHybridRoleStugg() throws Exception {

//        //add Hybrid roles
//        //role1
//        hybridRoleMan.addHybridRole("ThunderCats", new String[] { "Lionel", "Chitarah", "Willykat",
//                "Willykit" });
//        assertTrue(hybridRoleMan.isExistingRole("ThunderCats"));
//        assertEquals(4, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);
//        assertFalse(hybridRoleMan.isExistingRole(null));
//
//        //role2
//        hybridRoleMan.addHybridRole("Siblings", new String[] { "Willykat", "Willykit" });
//        assertEquals(2, hybridRoleMan.getHybridRoles().length);
//        assertEquals(2, hybridRoleMan.getUserListOfHybridRole("Siblings").length);
//        try{
//           hybridRoleMan.addHybridRole(null, new String[] { "Willykat1", "Willykit1" });
//           fail("Exception at using a Null Role name");
//        }catch(Exception ex){
//           //caught Exception 
//        }
//
//        //role3
//        hybridRoleMan.addHybridRole("decepticons", new String[] { "Fallen", "Megatron", "Starscreamer"});
//
//        
//        //Update User List of Role
//        hybridRoleMan.updateUserListOfHybridRole("ThunderCats", new String[] { "Willykat",
//                "Willykit" }, new String[] { "Snarf" });
//        assertEquals(3, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);
//        assertEquals(1, hybridRoleMan.getHybridRoleListOfUser("Lionel").length);
//        try{
//              hybridRoleMan.updateUserListOfHybridRole(null, new String[] { "Willykat",
//                "Willykit" }, new String[] { "Snarf" });
//              fail("Exception at updating a Null Role");
//        }catch(Exception ex){
//            //caught Exception
//        }
//        try{
//            hybridRoleMan.updateUserListOfHybridRole("ThunderCats",null, new String[] { "Snarf" });
//            fail("Exception at deleting a Null user list");
//        }catch(Exception ex){
//            //caught Exception
//        }
//
//
//        //Update Hybrid Role List of users
//        hybridRoleMan.updateHybridRoleListOfUser("Chitarah",new String[]{"ThunderCats"},new String[]{"decepticons"});
//        assertEquals(2, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);
//        assertEquals(4, hybridRoleMan.getUserListOfHybridRole("decepticons").length);
//        try{
//            hybridRoleMan.updateHybridRoleListOfUser(null,new String[]{"ThunderCats"},new String[]{"decepticons"});
//            fail("Exception at updateHybridRoleListOfUser with null user name");
//        }catch(Exception ex){
//             //caught exception
//        }
//         try{
//            hybridRoleMan.updateHybridRoleListOfUser("Chitarah",null,new String[]{"decepticons"});
//            fail("Exception at updateHybridRoleListOfUser with null user name");
//        }catch(Exception ex){
//             //caught exception
//        }
//
//        
//        //Update user list of two hybrid roles
//        hybridRoleMan.updateUserListOfHybridRole("decepticons",new String[] {"Starscreamer"},null);
//        assertEquals(3, hybridRoleMan.getUserListOfHybridRole("decepticons").length);
//        
//        hybridRoleMan.updateHybridRoleListOfUser("Snarf",new String[]{"ThunderCats"},null);
//        assertEquals(1, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);
//
//        // Delete a Hybrid Role
//        hybridRoleMan.deleteHybridRole("ThunderCats");
//        assertFalse(hybridRoleMan.isExistingRole("ThunderCats"));
    }

}

