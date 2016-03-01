/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.jndi.internal.util;

import org.osgi.framework.ServiceReference;

import java.util.Comparator;

import static org.wso2.carbon.jndi.internal.Constants.*;

/**
 * An implementation of the {@code Comparator} which compares OSGi services based service.ranking service property.
 */
public class ServiceRankComparator implements Comparator<ServiceReference<?>> {

    @Override
    public int compare(ServiceReference<?> ref1, ServiceReference<?> ref2) {
        int rank1 = (Integer) ref1.getProperty(SERVICE_RANKING);
        int rank2 = (Integer) ref2.getProperty(SERVICE_RANKING);
        int diff = rank1 - rank2;
        if (diff == 0) {
            int serviceId1 = (Integer) ref1.getProperty(SERVICE_ID);
            int serviceId2 = (Integer) ref2.getProperty(SERVICE_ID);
            return -(serviceId1 - serviceId2);
        } else {
            return diff;
        }
    }
}
