/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tomcat.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.jndi.JNDIURLStreamHandlerService;

import java.util.Hashtable;

/**
 * The OSGi BundleActivator for the {@link org.wso2.carbon.tomcat} bundle
 */
public class TomcatBundleActivator implements BundleActivator {

    private static Log log = LogFactory.getLog(TomcatBundleActivator.class);
    private ServerManager serverManager;
    private ServiceRegistration serviceRegistration;


    public void start(BundleContext bundleContext) throws Exception {
        try {
            this.serverManager = new ServerManager();
            serverManager.init();
            serverManager.start();
            serviceRegistration = bundleContext.registerService(CarbonTomcatService.class.getName(), serverManager.getTomcatInstance(), null);
            if (log.isDebugEnabled()) {
                log.debug("Registering the JNDI stream handler...");
            }
            //registering JNDI stream handler
            Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
            properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{"jndi"});
            bundleContext.registerService(URLStreamHandlerService.class.getName(),
                    new JNDIURLStreamHandlerService(), properties);
        } catch (Throwable t) {
            log.fatal("Error while starting server " + t.getMessage(), t);
            //do not throw because framework will keep trying. catching throwable is a bad thing, but
            //looks like we have no other option.
        }
    }


    public void stop(BundleContext bundleContext) throws Exception {
        this.serverManager.stop();
        serviceRegistration.unregister();
        serverManager = null;
    }
}
