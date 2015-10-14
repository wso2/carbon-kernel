/*
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.transports;

import java.util.HashMap;
import java.util.Map;

/**
 * Carbon Transport Manager
 */
public class TransportManager {

    private Map<String, CarbonTransport> transports = new HashMap<>();

    public void registerTransport(CarbonTransport transport) {
        transports.put(transport.getId(), transport);
    }

    public void unregisterTransport(CarbonTransport transport) {
        transports.remove(transport.getId());
    }

    public void startTransport(String transportId) {
        CarbonTransport transport = transports.get(transportId);
        if (transport == null) {
            throw new IllegalArgumentException(transportId + " not found");
        }
        transport.startTransport();
    }

    public void stopTransport(String transportId) {
        CarbonTransport transport = transports.get(transportId);
        if (transport == null) {
            throw new IllegalArgumentException(transportId + " not found");
        }
        transport.stopTransport();
    }

    public void startTransports() {
        transports.entrySet()
                .stream()
                .forEach(entry -> entry.getValue().startTransport());
    }

    public void stopTransports() {
        transports.values().forEach(CarbonTransport::stopTransport);
    }

    public void beginMaintenance() {
        transports.values().forEach(CarbonTransport::beginTransportMaintenance);
    }

    public void endMaintenance() {
        transports.values().forEach(CarbonTransport::endTransportMaintenance);
    }
}
