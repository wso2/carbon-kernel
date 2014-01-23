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

package org.wso2.carbon.registry.core.test.jdbc;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.test.utils.BaseTestCase;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;

public class HandlerTest extends BaseTestCase {

    /**
     * Registry instance for use in tests. Note that there should be only one Registry instance in a
     * JVM.
     */
    protected static Registry registry = null;
    protected static Registry systemRegistry = null;
    protected static EmbeddedRegistryService embeddedRegistryService = null;

    public void setUp() {
        super.setUp();
        if (embeddedRegistryService != null) {
            return;
        }

        try {
            embeddedRegistryService = ctx.getEmbeddedRegistryService();
            RealmUnawareRegistryCoreServiceComponent comp =
                    new RealmUnawareRegistryCoreServiceComponent();
            comp.setRealmService(ctx.getRealmService());
            comp.registerBuiltInHandlers(embeddedRegistryService);

            registry = embeddedRegistryService.getUserRegistry("admin", "admin");
            systemRegistry = embeddedRegistryService.getSystemRegistry();
        } catch (RegistryException e) {
                fail("Failed to initialize the registry. Caused by: " + e.getMessage());
        }
    }

    public void testCommitHandlerExecution() throws Exception {

        final class TestData {
            private boolean handlerExecuted = false;

            public boolean isHandlerExecuted() {
                return handlerExecuted;
            }

            public void setHandlerExecuted(boolean handlerExecuted) {
                this.handlerExecuted = handlerExecuted;
            }
        }

        final TestData testData = new TestData();

        Handler handler = new Handler() {
            public void put(RequestContext requestContext) throws RegistryException {
                testData.setHandlerExecuted(true);
            }
        };

        URLMatcher filter = new URLMatcher();
        filter.setPattern(".*");

        CurrentSession.setCallerTenantId(MultitenantConstants.SUPER_TENANT_ID);
        try {
            registry.getRegistryContext().getHandlerManager().addHandler(null, filter, handler,
                    HandlerLifecycleManager.COMMIT_HANDLER_PHASE);
        } finally {
            CurrentSession.removeCallerTenantId();
        }

        Resource r1 = registry.newResource();
        String str = "My Content";
        r1.setContentStream(new ByteArrayInputStream(str.getBytes()));
        registry.put("/c1/c2/c3/c4/r1", r1);

        assertTrue(testData.isHandlerExecuted());
    }

    public void testRollbackHandlerExecution() throws Exception {

        final class TestData {
            private boolean handlerExecuted = false;

            public boolean isHandlerExecuted() {
                return handlerExecuted;
            }

            public void setHandlerExecuted(boolean handlerExecuted) {
                this.handlerExecuted = handlerExecuted;
            }
        }

        final TestData testData = new TestData();

        Handler handler = new Handler() {
            public void put(RequestContext requestContext) throws RegistryException {
                testData.setHandlerExecuted(true);
            }
        };

        Handler handler1 = new Handler() {
            public void put(RequestContext requestContext) throws RegistryException {
                throw new RegistryException("Sample Test Failure");
            }
        };

        URLMatcher filter = new URLMatcher();
        filter.setPattern(".*");

        registry.getRegistryContext().getHandlerManager().addHandler(null, filter, handler1);

        filter = new URLMatcher();
        filter.setPattern(".*");

        CurrentSession.setCallerTenantId(MultitenantConstants.SUPER_TENANT_ID);
        try {
            registry.getRegistryContext().getHandlerManager().addHandler(null, filter, handler,
                    HandlerLifecycleManager.ROLLBACK_HANDLER_PHASE);
        } finally {
            CurrentSession.removeCallerTenantId();
        }

        Resource r1 = registry.newResource();
        String str = "My Content";
        r1.setContentStream(new ByteArrayInputStream(str.getBytes()));
        try {
            registry.put("/c1/c2/c3/c4/r1", r1);
        } catch (RegistryException ignored) {
        }
        assertTrue(testData.isHandlerExecuted());
    }

}
