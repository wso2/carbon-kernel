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
package org.wso2.carbon.kernel.internal.transports;

import org.wso2.carbon.kernel.transports.CarbonTransport;

/**
 * This class acts as a dummy command transport for the test case
 * org.wso2.carbon.kernel.internal.transports.TransportMgtCommandProviderTest.
 *
 * @since 5.0.0
 */
public class DummyTransport extends CarbonTransport {
    private Boolean started = false;
    private Boolean stopped = false;
    private Boolean beganMaintenance = false;
    private Boolean endedMaintenance = false;

    public DummyTransport(String id) {
        super(id);
    }

    @Override
    protected void start() {
        started = true;
    }

    @Override
    protected void stop() {
        stopped = true;
    }

    @Override
    protected void beginMaintenance() {
        beganMaintenance = true;
    }

    @Override
    protected void endMaintenance() {
        endedMaintenance = true;
    }

    public Boolean getStarted() {
        return started;
    }

    public Boolean getStopped() {
        return stopped;
    }

    public Boolean getBeganMaintenance() {
        return beganMaintenance;
    }

    public Boolean getEndedMaintenance() {
        return endedMaintenance;
    }
}
