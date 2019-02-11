/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.test.secure;

import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;

public class CommentsTest extends BaseTestCase {

    private static EmbeddedRegistryService embeddedRegistryService = null;

    public void setUp() {
        super.setUp();
        if (embeddedRegistryService == null) {
            try {
                embeddedRegistryService = ctx.getEmbeddedRegistryService();
                RealmUnawareRegistryCoreServiceComponent comp =
                        new RealmUnawareRegistryCoreServiceComponent();
                comp.registerBuiltInHandlers(embeddedRegistryService);

            } catch (RegistryException e) {
                fail("Failed to initialize the embeddedRegistryService. Caused by: " +
                        e.getMessage());
            }
        }
    }

    public void testCommentDelete() throws Exception {

        //embeddedRegistryService = new InMemoryEmbeddedRegistryService();
        // get the realm config to retrieve admin username, password
        RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
        UserRegistry adminReg = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());

        UserRealm adminRealm = adminReg.getUserRealm();
        adminRealm.getUserStoreManager().addRole("rolex", null, null);
        adminRealm.getUserStoreManager()
                .addUser("cu1", "cu1111", new String[]{"rolex"}, null, null);

        UserRegistry cu1Reg = embeddedRegistryService.getUserRegistry("cu1", "cu1111");

        ResourceImpl r1 = new ResourceImpl();
        r1.setContent("r1 content");
        adminReg.put("/CRTest/r1", r1);

        adminRealm.getAuthorizationManager().
                authorizeRole("rolex", "/CRTest/r1", ActionConstants.PUT);

        ResourceImpl r2 = new ResourceImpl();
        r2.setContent("r2 content");
        adminReg.put("/CRTest/r2", r2);

        String c1 =
                adminReg.addComment("/CRTest/r1",
                        new Comment("administrator's comment on /CRTest/r1"));
        assertTrue(adminRealm.getAuthorizationManager()
                .isUserAuthorized("cu1", "/CRTest/r1", ActionConstants.PUT));

        String c2 =
                adminReg.addComment("/CRTest/r2",
                        new Comment("administrator's comment on /CRTest/r2"));

        String c3 =
                cu1Reg.addComment("/CRTest/r1", new Comment("cu1's comment on /CRTest/r1"));

        String c4 =
                cu1Reg.addComment("/CRTest/r2", new Comment("cu1's comment on /CRTest/r2"));

        cu1Reg.delete(c1);

        boolean failed = false;
        try {
            cu1Reg.delete(c2);
        } catch (RegistryException e) {
            failed = true;
        }
        assertTrue("cu1 should not have " +
                "permission to delete administrator's comment on administrator's resource.",
                failed);

        try {
            cu1Reg.delete(c3);
        } catch (RegistryException e) {
            fail("cu1 should be able to delete cu1's comment on administrator's resource even " +
                    "he does not have write permissions for it.");
        }

        try {
            cu1Reg.delete(c4);
        } catch (RegistryException e) {
            fail("admin should be able to delete cu1's comment on administrator's resource. In " +
                    "this case cu1 also has write permissions on administrator's resource.");
        }

    }
}
