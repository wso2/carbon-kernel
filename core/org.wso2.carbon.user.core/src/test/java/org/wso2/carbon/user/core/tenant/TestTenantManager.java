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
package org.wso2.carbon.user.core.tenant;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.TenantTestUtil;
import org.wso2.carbon.user.core.UserCoreTestConstants;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.FileInputStream;

public class TestTenantManager extends BaseTestCase{
    private TenantManager tenantMan;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testTenantManager() throws Exception {
        tenantDbStuff(); //create db instance
        doTenantStuff(); // do tenantManager stuff
    }

    public void tenantDbStuff() throws Exception{
        String dbFolder = "target/Tenanttest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(UserCoreTestConstants.DB_DRIVER);
        ds.setUrl("jdbc:h2:target/Tenanttest/TEN_TEST");

        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();
        tenantMan = new JDBCTenantManager(ds, "super.com");
    }

    public void doTenantStuff() throws Exception{
        RealmConfigXMLProcessor processor = new RealmConfigXMLProcessor();
        RealmConfiguration realmConfig = processor.buildRealmConfiguration(new FileInputStream(CarbonUtils.getUserMgtXMLPath()));
        Tenant[] tarray = TenantTestUtil.createTenant(realmConfig);

        assertEquals(1,tenantMan.addTenant(tarray[0]));
        assertEquals(2,tenantMan.addTenant(tarray[1]));
        assertEquals(3,tenantMan.addTenant(tarray[2]));

        //activate tenant
        tenantMan.activateTenant(1);
        assertTrue(tenantMan.getTenant(1).isActive());
        tenantMan.activateTenant(2);
        assertTrue(tenantMan.getTenant(2).isActive());
        assertFalse(tenantMan.getTenant(3).isActive());
        try{
            boolean a = tenantMan.getTenant(4).isActive();
            fail("NullPointer Exception failed");
        }catch(Exception e){
            //caught exception
        }

        //update tenant
        Tenant updateOne = (Tenant) tenantMan.getTenant(1);
        assertEquals("domain1",tenantMan.getTenant(1).getDomain());
        updateOne.setDomain("updatedomain1");
        tenantMan.updateTenant(updateOne);
        assertEquals("updatedomain1",tenantMan.getTenant(1).getDomain());

        //getDomain
        assertEquals("domain2",tenantMan.getDomain(2));
        assertEquals("domain3",tenantMan.getDomain(3));
        assertEquals(null,tenantMan.getDomain(4));

        //getTenantId
        assertEquals(1,tenantMan.getTenantId("updatedomain1"));
        assertEquals(2,tenantMan.getTenantId("domain2"));
        assertEquals(3,tenantMan.getTenantId("domain3"));
        assertEquals(MultitenantConstants.INVALID_TENANT_ID, tenantMan.getTenantId("abxxxx")); //a Non existing domain name

        assertEquals(3,tenantMan.getAllTenants().length);

        //deactivate tenant
        tenantMan.deactivateTenant(2);
        assertFalse(tenantMan.getTenant(2).isActive());

        //delete tenant
        tenantMan.deleteTenant(3);
        assertEquals(2,tenantMan.getAllTenants().length);

    }

}
