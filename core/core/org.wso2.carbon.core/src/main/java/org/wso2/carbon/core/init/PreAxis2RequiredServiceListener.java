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
package org.wso2.carbon.core.init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.util.HashMap;
import java.util.Map;

/**
 * This class keeps track of the required OSGi Services which should be registered before initializing Axis2
 */
public class PreAxis2RequiredServiceListener implements ServiceListener {
    private static Log log = LogFactory.getLog(PreAxis2RequiredServiceListener.class);

    private Map<String, Bundle> requiredOSGiServiceMap = new HashMap<String, Bundle>();

    private BundleContext bundleContext;
    private CarbonServerManager carbonServerManager;
    private String orFilter;
    private boolean listenerRegistered;

    public PreAxis2RequiredServiceListener(BundleContext bundleContext, CarbonServerManager carbonServerManager) {
        this.bundleContext = bundleContext;
        this.carbonServerManager = carbonServerManager;
    }

    /**
     * Registering PreAxis2RequiredServiceListener as a ServiceListener
     *
     * @return boolean : whether the listener is registered
     */
    boolean registerServiceListener() {
        if (requiredOSGiServiceMap.isEmpty()) {
            //There are no required OSGi services
            listenerRegistered = false;
        } else {
            try {
                //Registering PreAxis2RequiredServiceListener as a ServiceListener
                setORFilter();
                bundleContext.addServiceListener(this, orFilter);
                listenerRegistered = true;
            } catch (InvalidSyntaxException e) {
                //SyntaxError Occured. Ignoring
                log.error(e.getCause(), e);
            }
        }
        return listenerRegistered;
    }

    void unregisterServiceListener() {
        if (listenerRegistered) {
            bundleContext.removeServiceListener(this);
        }
    }

    synchronized void start() {
        try {
            //Getting the registered required OSGi services
            ServiceReference[] references = bundleContext.getServiceReferences((String)null, orFilter);
            if (references != null && references.length > 0) {
                for (ServiceReference reference : references) {
                    String serviceClazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
                    if (requiredOSGiServiceMap.containsKey(serviceClazz)) {
                        requiredOSGiServiceMap.remove(serviceClazz);
                    }
                }
            }

            //adding pending services
            for (String service : requiredOSGiServiceMap.keySet()) {
                carbonServerManager.addPendingItem(service, "OSGi Service");
            }
        } catch (InvalidSyntaxException e) {
            //SyntaxError Occured. Ignoring
            log.error(e.getCause(), e);
        }
    }

    void addRequiredServiceBundle(Bundle bundle, String servicesList) {
        String[] services = servicesList.split(",");
        for (String service : services) {
            requiredOSGiServiceMap.put(service, bundle);
        }
    }

    public synchronized void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            String serviceClazz = ((String[]) event.getServiceReference().getProperty(Constants.OBJECTCLASS))[0];
            carbonServerManager.removePendingItem(serviceClazz);
        }
    }

    private void setORFilter() {
        orFilter = "(|";
        for (String service : requiredOSGiServiceMap.keySet()) {
            orFilter += "(" + Constants.OBJECTCLASS + "=" + service + ")";
        }
        orFilter += ")";
//        if(requiredOSGiServiceMap.isEmpty())
//            return;
//
//        String[] servicesArray = requiredOSGiServiceMap.keySet().toArray(new String[requiredOSGiServiceMap.size()]);
//
//        for(int i = 0; i <servicesArray.length; i++){
//            String service = servicesArray[i];
//            service = "("+ Constants.OBJECTCLASS + "=" + service + ")";
//            servicesArray[i] = service;
//        }
//
//        if(servicesArray.length == 1){
//           orFilter = createSegment(servicesArray, 1, servicesArray[0], null);
//        } else {
//           orFilter = createSegment(servicesArray, 2, servicesArray[0], servicesArray[1]);
//        }
//
    }

//    public String createSegment(String[] services, int count, String part1, String part2) {
//        String segment;
//        if (part2 == null) {
//            segment = part1;
//        } else {
//            segment = "(|" + part1 + part2 + ")";
//        }
//
//        if (count == services.length) {
//            return segment;
//        } else {
//            String part2temp = services[count];
//            return createSegment(services, ++count, segment, part2temp);
//        }
//    }

}
