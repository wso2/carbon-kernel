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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.*;

public class SecureRegistryTest extends BaseTestCase {

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

    public void testUserBasedAuthorization() throws RegistryException {

        // get the realm config to retrieve admin username, password
        RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
        UserRegistry adminRegistry = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());

        UserRealm adminRealm = adminRegistry.getUserRealm();

        Resource r1 = adminRegistry.newCollection();

        String r2Content = "R1";
        Resource r2 = adminRegistry.newResource();
        r2.setContent(r2Content.getBytes());

        try {
            adminRegistry.put("/r1", r1);
            adminRegistry.put("/r1/r2", r2);
        } catch (RegistryException e) {
            fail("Valid put scenario failed.");
        }

        try {
            adminRealm.getUserStoreManager().addRole("rolex", null, null);
            adminRealm.getUserStoreManager()
                    .addUser("bar", "cce123", new String[]{"rolex"}, null, null);
            adminRealm.getUserStoreManager().addUser("foo", "uuu123", null, null, null);

            adminRealm.getAuthorizationManager()
                    .authorizeRole("rolex", "/r1", ActionConstants.PUT);
            adminRealm.getAuthorizationManager()
                    .denyRole("rolex", "/r1/r2", ActionConstants.PUT);
        } catch (UserStoreException e) {
            e.printStackTrace();
        }

        UserRegistry barRegistry =
                embeddedRegistryService.getUserRegistry("bar", "cce123");

        try {
            barRegistry.get("/r1");
        } catch (AuthorizationFailedException e) {
            fail("Could not get authorized resource.");
        }

        try {
            barRegistry.put("/r1", r1);
        } catch (AuthorizationFailedException e) {
            fail("Could not put authorized resource.");
        }
        try {
            barRegistry.put("/r1/r2", r2);
            fail("Could put non-authorized resource.");
        } catch (AuthorizationFailedException e) {
        }

        UserRegistry fooRegistry = embeddedRegistryService.getUserRegistry("foo", "uuu123");

        boolean failed = false;
        try {
            fooRegistry.put("/r1", r1);
        } catch (RegistryException e) {
            failed = true;
        }

        assertTrue("Allowed to get unauthorized resource.", failed);
    }

    public void testRoleBasedAuthorization() throws RegistryException {

        //embeddedRegistryService = new InMemoryEmbeddedRegistryService();
        // get the realm config to retrieve admin username, password
        RealmConfiguration realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
        UserRegistry adminRegistry = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        UserRealm adminRealm = adminRegistry.getUserRealm();

        String r1Content = "R1";
        Resource r1 = adminRegistry.newResource();
        r1.setContent(r1Content.getBytes());

        try {
            adminRegistry.put("/r1", r1);
        } catch (RegistryException e) {
            fail("Valid put scenario failed.");
        }

        try {
            adminRealm.getUserStoreManager().addRole("registryTeam", null, null);
            adminRealm.getAuthorizationManager()
                    .authorizeRole("registryTeam", "/r1", ActionConstants.PUT);

            adminRealm.getUserStoreManager().addUser("barX", "xxx123", null, null, null);
            adminRealm.getUserStoreManager()
                    .updateRoleListOfUser("barX", null, new String[]{"registryTeam"});

            adminRealm.getUserStoreManager().addUser("fooX", "xxx123", null, null, null);
        } catch (UserStoreException e) {
            e.printStackTrace();
        }

        UserRegistry barRegistry =
                embeddedRegistryService.getUserRegistry("barX", "xxx123");

        try {
            barRegistry.get("/r1");
        } catch (AuthorizationFailedException e) {
            fail("Could not get authorized resource.");
        }

        UserRegistry fooRegistry = embeddedRegistryService.getUserRegistry("fooX", "xxx123");

        boolean failed = false;
        try {
            fooRegistry.put("/r1", r1);
        } catch (RegistryException e) {
            failed = true;
        }

        assertTrue("Allowed to get unauthorized resource.", failed);
    }

    @SuppressWarnings("unused")
    public static String getResourceMediaTypeMappingsF(String fileName) throws Exception {
        InputStream input = new FileInputStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String mediaTypeString = null;
        try {
            while (reader.ready()) {
                String mediaTypeData = reader.readLine().trim();
                if (mediaTypeData.startsWith("#")) {
                    // ignore the comments
                    continue;
                }

                if (mediaTypeData.length() == 0) {
                    // ignore the blank lines
                    continue;
                }

                // mime.type file delimits mediaTypes:extensions by tabs. if there is no
                // extension associated with a media type, there are no tabs in the line. so we
                // don't need such lines.
                if (mediaTypeData.indexOf("\t") > 0) {

                    String[] parts = mediaTypeData.split("\t+");
                    if (parts.length == 2) {

                        // there can multiple extensions associated with a single media type. in
                        // that case, extensions are delimited by a space.
                        String[] extensions = parts[1].trim().split(" ");
                        for (String ext : extensions) {

                            if (ext.length() > 0) {
                                String mediaTypeMapping = ext + ":" + parts[0];

                                if (mediaTypeString == null) {
                                    mediaTypeString = mediaTypeMapping;
                                } else {
                                    mediaTypeString = mediaTypeString + "," + mediaTypeMapping;
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            String msg = "Could not read the media type mappings file from the location: ";
            throw new RegistryException(msg);
        } finally {
            try {
                reader.close();
                input.close();
            } catch (IOException ignore) {
            }
        }

        return mediaTypeString;
    }
}
