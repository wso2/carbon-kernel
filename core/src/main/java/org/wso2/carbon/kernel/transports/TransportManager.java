/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.transports;

import org.wso2.carbon.kernel.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for managing the Carbon Transports available in the kernel.
 *
 * @since 5.0.0
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
        Utils.checkSecurity();
        transport.startTransport();
    }

    public void stopTransport(String transportId) {
        CarbonTransport transport = transports.get(transportId);
        if (transport == null) {
            throw new IllegalArgumentException(transportId + " not found");
        }
        Utils.checkSecurity();
        transport.stopTransport();
    }

    public void startTransports() {
        Utils.checkSecurity();
        transports.values()
                .forEach(CarbonTransport::startTransport);
    }

    public void stopTransports() {
        Utils.checkSecurity();
        transports.values()
                .forEach(CarbonTransport::stopTransport);
    }

    public void beginMaintenance() {
        Utils.checkSecurity();
        transports.values()
                .forEach(CarbonTransport::beginTransportMaintenance);
    }

    public void endMaintenance() {
        Utils.checkSecurity();
        transports.values()
                .forEach(CarbonTransport::endTransportMaintenance);
    }

    public Map<String, CarbonTransport> getTransports() {
        Utils.checkSecurity();
        return transports;
    }
}
