/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.internal.transports;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.transports.CarbonTransport;
import org.wso2.carbon.transports.TransportManager;

import java.util.Map;

@Component(
        name = "org.wso2.carbon.internal.transport.TransportServiceComponent",
        immediate = true
)
public class TransportServiceComponent {

    private TransportManager transportManager = new TransportManager();

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(TransportManager.class, transportManager, null);
    }

    @Reference(
            name = "carbon.transport",
            service = CarbonTransport.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterTransport"
    )
    protected void registerTransport(CarbonTransport transport, Map<String, ?> ref) {
        transportManager.registerTransport(transport);
    }

    protected void unregisterTransport(CarbonTransport transport, Map<String, ?> ref) {
        transportManager.unregisterTransport(transport);
    }
}
