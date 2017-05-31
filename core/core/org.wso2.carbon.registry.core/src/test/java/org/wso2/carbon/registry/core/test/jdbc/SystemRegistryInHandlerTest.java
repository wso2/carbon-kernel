/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.test.jdbc;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.Repository;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.RealmConfiguration;

public class SystemRegistryInHandlerTest  extends BaseTestCase {

    
    protected static EmbeddedRegistryService embeddedRegistryService = null;
    RealmConfiguration realmConfig;

    public void setUp() {
        super.setUp();
        if (embeddedRegistryService != null) {
            return;
        }
        try {
            embeddedRegistryService = ctx.getEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp =
                    new RealmUnawareRegistryCoreServiceComponent();
            comp.registerBuiltInHandlers(embeddedRegistryService);
            
        } catch (RegistryException e) {
            fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testNestedRegistryOperations() throws RegistryException {

        // get the realm config to retrieve admin username, password
        realmConfig = ctx.getRealmService().getBootstrapRealmConfiguration();
        Registry adminRegistry = embeddedRegistryService.getUserRegistry(
            realmConfig.getAdminUserName(), realmConfig.getAdminPassword());
        RegistryContext registryContext = adminRegistry.getRegistryContext();
        MyPrivateHandler myPrivateHandler = new MyPrivateHandler();

        HandlerManager handlerManager = registryContext.getHandlerManager();

        URLMatcher myPrivateHandlerMatcher = new URLMatcher();
        myPrivateHandlerMatcher.setGetPattern(".*/to/my/private/handler");
        myPrivateHandlerMatcher.setPutPattern(".*/to/my/private/handler");
        handlerManager.addHandler(
                new String[] {Filter.GET, Filter.PUT} , myPrivateHandlerMatcher, myPrivateHandler);

        Resource r = adminRegistry.newResource();
        String originalContent = "original content";
        r.setContent(originalContent.getBytes());

        adminRegistry.put("/to/my/private/handler", r);
        Resource rr = adminRegistry.get("/to/my/private/handler");

        byte[] newContent = (byte[])rr.getContent();
        String newContentString = RegistryUtils.decodeBytes(newContent);

        String expectedString = "<adminRegistry-output><systemRegistry-output>" +
                            "<systemRegistry-input><adminRegistry-input>" +
                            originalContent +
                            "</adminRegistry-input></systemRegistry-input>" +
                            "</systemRegistry-output></adminRegistry-output>";

        assertEquals("the returned content should be equal.", expectedString, newContentString);

    }

    private class MyPrivateHandler extends Handler {

        public Resource get(RequestContext requestContext) throws RegistryException {
            String path = requestContext.getResourcePath().getPath();
            String currentUser = CurrentSession.getUser();
            if (currentUser.equals(realmConfig.getAdminUserName())) {
                // we will get the system registry for this
                Registry systemRegistry = embeddedRegistryService.getSystemRegistry();
                Resource r = systemRegistry.get(path);
                byte[] content = (byte[])r.getContent();
                String contentString = RegistryUtils.decodeBytes(content);
                contentString = "<adminRegistry-output>" + contentString +
                        "</adminRegistry-output>";
                r.setContent(contentString.getBytes());

                // check the current user again,
                String newCurrentUser = CurrentSession.getUser();
                assertEquals("The session user should be the same", newCurrentUser, currentUser);

                return r;
            } else {
                // now this should be the system registry, so lets return the real content
                Repository repository = requestContext.getRepository();
                Resource r = repository.get(path);
                byte[] content = (byte[])r.getContent();
                String contentString = RegistryUtils.decodeBytes(content);
                contentString = "<systemRegistry-output>" + contentString +
                        "</systemRegistry-output>";
                r.setContent(contentString.getBytes());

                // check the current user again,
                String newCurrentUser = CurrentSession.getUser();
                assertEquals("The session user should be the same", newCurrentUser, currentUser);
                
                return r;
            }
        }

        public void put(RequestContext requestContext) throws RegistryException {
            String path = requestContext.getResourcePath().getPath();
            Resource r = requestContext.getResource();
            String currentUser = CurrentSession.getUser();
            if (currentUser.equals(realmConfig.getAdminUserName())) {
                // we will get the system registry for this
                Registry systemRegistry = embeddedRegistryService.getSystemRegistry();
                byte[] content = (byte[])r.getContent();
                String contentString = RegistryUtils.decodeBytes(content);
                contentString = "<adminRegistry-input>" + contentString +
                        "</adminRegistry-input>";
                r.setContent(contentString.getBytes());
                systemRegistry.put(path, r);
            } else {
                // now this should be the system registry, so lets return the real content
                Repository repository = requestContext.getRepository();
                byte[] content = (byte[])r.getContent();
                String contentString = RegistryUtils.decodeBytes(content);
                contentString = "<systemRegistry-input>" + contentString +
                        "</systemRegistry-input>";
                r.setContent(contentString.getBytes());
                repository.put(path, r);
            }

            // check the current user again,
            String newCurrentUser = CurrentSession.getUser();
            assertEquals("The session user should be the same", newCurrentUser, currentUser);
        }

    }
}
