/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.core.init;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.util.HashMap;
import java.util.Map;

/**
 * This class waits until all Axis2 services which are deployed as OSGi bundles
 * become ACTIVE
 */
public class OSGiAxis2ServicesListener implements BundleListener {
    private BundleContext bundleContext;
    private CarbonServerManager carbonServerManager;
    private boolean listenerRegistered;
    private Map<String, Bundle> osgiAxis2Services = new HashMap<String, Bundle>();

    public OSGiAxis2ServicesListener(BundleContext bundleContext,
                                     CarbonServerManager carbonServerManager) {
        this.bundleContext = bundleContext;
        this.carbonServerManager = carbonServerManager;
        bundleContext.addBundleListener(this);
    }

    public void addOSGiAxis2Service(Bundle bundle) {
        osgiAxis2Services.put(getKey(bundle), bundle);
    }

    private String getKey(Bundle bundle) {
        return bundle.getSymbolicName() + "-" + bundle.getVersion();
    }

    boolean registerBundleListener() {
        //what if there are no pending things, then don't register the listener
        if (osgiAxis2Services.isEmpty() && osgiAxis2Services.isEmpty()) {
            listenerRegistered = false;
        } else {
            bundleContext.addBundleListener(this);
            listenerRegistered = true;
        }
        return listenerRegistered;
    }

    void unregisterBundleListener() {
        if (listenerRegistered) {
            bundleContext.removeBundleListener(this);
        }
    }

    synchronized void start() {
        //Searching Non ACTIVE Bundles and add them to the pending list.
        for (Bundle bundle : osgiAxis2Services.values()) {
            if (bundle.getState() != Bundle.ACTIVE) {
                carbonServerManager.addPendingItem(getKey(bundle), "OSGiAxis2Service");
            }
        }
    }

    public synchronized void bundleChanged(BundleEvent event) {
        String key = getKey(event.getBundle());
        if (event.getType() != BundleEvent.UNINSTALLED &&
            event.getType() != BundleEvent.STARTED) {
            return;
        }
        if (osgiAxis2Services.containsKey(key)) {
            carbonServerManager.removePendingItem(key);
        }
    }
}
