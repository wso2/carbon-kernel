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

package org.wso2.carbon.registry.core.test.utils;

import junit.framework.TestCase;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.File;
import java.io.InputStream;

public class BaseTestCase extends TestCase {

    protected RegistryContext ctx = null;
    protected InputStream is;

    public void setUp() {
        setupCarbonHome();
        setupContext();
    }

    protected void setupContext() {
        try {
            RealmService realmService = new InMemoryRealmService();
            is = this.getClass().getClassLoader().getResourceAsStream(
                    System.getProperty("registry.config"));
            ctx = RegistryContext.getBaseInstance(is, realmService);
        } catch (Exception e) {
        }
        ctx.setSetup(true);
        ctx.selectDBConfig("h2-db");
    }

    protected void setupCarbonHome() {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("../../distribution/kernel/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../../../distribution/kernel/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        // The line below is responsible for initializing the cache.
        CarbonContext.getThreadLocalCarbonContext();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);
    }

    public class RealmUnawareRegistryCoreServiceComponent extends
            RegistryCoreServiceComponent {

        public void setRealmService(RealmService realmService) {
            super.setRealmService(realmService);
        }
    }
}
