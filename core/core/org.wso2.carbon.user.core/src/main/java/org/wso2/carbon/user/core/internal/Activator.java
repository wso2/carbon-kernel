/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.core.common.DefaultRealmService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.File;
import java.lang.management.ManagementPermission;

/**
 * This is one of the first bundles that start in Carbon.
 * <p/>
 * ServerConfiguration object is not available to this bundle.
 * Therefore we read properties but do not keep a reference to it.
 */
public class Activator extends BundleCheckActivator {
    private static final Log log = LogFactory.getLog(Activator.class);

    public void startDeploy(BundleContext bundleContext) throws Exception {
        // Need permissions in order to instantiate user core
        SecurityManager secMan = System.getSecurityManager();

		/*
         * Read the SSL trust store configurations from the Security.TrustStore
		 * element of the Carbon.xml
		 */
        ServerConfiguration config = ServerConfiguration.getInstance();
        String type = config.getFirstProperty("Security.TrustStore.Type");
        String password = config.getFirstProperty("Security.TrustStore.Password");
        String storeFile =
                new File(config.getFirstProperty("Security.TrustStore.Location")).getAbsolutePath();
        // set the SSL trust store System Properties
        System.setProperty("javax.net.ssl.trustStore", storeFile);
        System.setProperty("javax.net.ssl.trustStoreType", type);
        System.setProperty("javax.net.ssl.trustStorePassword", password);

        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            RealmService realmService = new DefaultRealmService(bundleContext);
            bundleContext.registerService(new String[]{RealmService.class.getName(), UserRealmService.class.getName()},
                    realmService, null);
            UserCoreUtil.setRealmService(realmService);
        } catch (Throwable e) {
            String msg = "Cannot start User Manager Core bundle";
            log.error(msg, e);
            // do not throw exceptions;
        }
    }

    public String getName() {
        return "UserCore";
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

}
