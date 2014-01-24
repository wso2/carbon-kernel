/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.secverifier;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.SystemFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Web service which verifies security on the server side
 */
public class SecurityVerifierService {

    public boolean verifyAdminServices() throws AxisFault {

        // The following API calls check that the OSGi service retrieval API works
        ListenerManager listenerManager = (ListenerManager)
                PrivilegedCarbonContext.getCurrentContext().getOSGiService(ListenerManager.class);
        System.out.println("Is listener running: " + !listenerManager.isStopped());

        AxisConfiguration axisConfig =
                MessageContext.getCurrentMessageContext().
                        getConfigurationContext().getAxisConfiguration();
        List<String> failedAdminServices = new ArrayList<String>();
        for(AxisService service : axisConfig.getServices().values()){
            if(SystemFilter.isAdminService(service)) {
                List<String> exposedTransports = service.getExposedTransports();
                StringBuilder transports = new StringBuilder();
                for (String exposedTransport : exposedTransports) {
                     transports.append(exposedTransport).append(',');
                }
                if(exposedTransports.size() > 1) {
                	StringBuilder failedAdminServiceBuf = new StringBuilder();
                	failedAdminServiceBuf.append(service.getName()).append(" [").append(transports).append(']');
                    failedAdminServices.add(failedAdminServiceBuf.toString());
                }
            }
        }
        if(failedAdminServices.size() > 0){
            StringBuilder failedServicesBuffer = new StringBuilder();
            int i = 1;
            for (String failedAdminService : failedAdminServices) {
                failedServicesBuffer.append('\n').append(i++).append(". ").append(failedAdminService);
            }
            throw new AxisFault("The following admin services are vulnerable: " +
            		failedServicesBuffer.toString() + "\nThese are exposed not only on HTTPS.");
        }
        return true;
    }

}
