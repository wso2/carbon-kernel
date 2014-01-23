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

public class HybridRoleManagerTest extends BaseTestCase {

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
        ds.setUrl("jdbc:h2:target/HybridRoleTest/CARBON_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
        // taking the tenant id = 0
     //   hybridRoleMan = new HybridRoleManager(ds, 0);
    }

    public void doHybridRoleStugg() throws Exception {
//        hybridRoleMan.addHybridRole("ThunderCats", new String[] { "Lionel", "Chitarah", "Willykat",
//                "Willykit" });
//        assertTrue(hybridRoleMan.isExistingRole("ThunderCats"));
//        hybridRoleMan.addHybridRole("Siblings", new String[] { "Willykat", "Willykit" });
//        assertEquals(2, hybridRoleMan.getHybridRoles().length);
//        assertEquals(4, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);
//        // kids are removed because they are just kids
//        hybridRoleMan.updateUserListOfHybridRole("ThunderCats", new String[] { "Willykat",
//                "Willykit" }, new String[] { "Snarf" });
//        assertEquals(3, hybridRoleMan.getUserListOfHybridRole("ThunderCats").length);
//        assertEquals(1, hybridRoleMan.getHybridRoleListOfUser("Lionel").length);
//        hybridRoleMan.deleteHybridRole("ThunderCats");
//        assertEquals(0, hybridRoleMan.getHybridRoleListOfUser("Lionel").length);
    }

   

}
