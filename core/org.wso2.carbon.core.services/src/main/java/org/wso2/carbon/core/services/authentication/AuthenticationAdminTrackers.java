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
package org.wso2.carbon.core.services.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;


public class AuthenticationAdminTrackers {

    private static final Log log = LogFactory.getLog(AuthenticationAdminTrackers.class);
    
    private static BundleContext bundleContext  = null;
    private static ServiceTracker dlgtRealmTracker = null;
    private static ServiceTracker defRealmTracker = null;
    private static ServiceTracker registryTracker = null;

    public static void init(BundleContext bc){
        bundleContext = bc;
        try {
            /*ServiceReference[] serviceRefDefault = bundleContext.getServiceReferences(UserRealm.class.getName(), "(RealmGenre=Default)");
            defRealmTracker = new ServiceTracker(bundleContext, serviceRefDefault[0], null);
            defRealmTracker.open();  */
            
            ServiceReference[] serviceRefs = bundleContext.getServiceReferences(RealmService.class.getName(), null);
            if(serviceRefs != null && serviceRefs.length>0){
                dlgtRealmTracker = new ServiceTracker(bundleContext, serviceRefs[0], null);
                dlgtRealmTracker.open();
            }

            ServiceReference[] regServiceRef = bundleContext.getServiceReferences(RegistryService.class.getName(), null);
            if (regServiceRef != null && regServiceRef.length > 0) {
                registryTracker = new ServiceTracker(bundleContext, regServiceRef[0], null);
                registryTracker.open();
            } else {
                String msg = "Registry service is not registered.";
                log.error(msg);
            }
            
        } catch (InvalidSyntaxException e) {

            String msg = "Failed to initialize service trackers required by AuthenticationAdmin service. " +
                    e.getMessage();
            log.error(msg, e);
            e.printStackTrace();
        }
    }
    
    public RealmService getRealmService() throws Exception{
        RealmService realmService  = null;
        if(dlgtRealmTracker != null){
            realmService = (RealmService)dlgtRealmTracker.getService();
        }
        
        if(realmService == null){
            realmService = (RealmService)defRealmTracker.getService();
        }
        
        if(realmService == null){
            throw new Exception("System has not been started properly. Some components have not started");
        }
        
        return realmService;     
    }

    public RegistryService getRegistryService() throws Exception {

        if (registryTracker != null) {
            return (RegistryService) registryTracker.getService();
        } else {
            String msg = "Failed to get the registry service. Registry OSGi service is not initialized. " +
                    "User's registry will not be availale.";
            log.error(msg);
            throw new Exception(msg);
        }

    }
}
