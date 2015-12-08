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
package org.wso2.carbon.kernel.transports.transporter;

import org.wso2.carbon.kernel.transports.CarbonTransport;

/**
 * Custom Carbon Transport class.
 *
 * @since 5.0.0
 */
public class FailingCarbonTransport extends CarbonTransport {

    private boolean failStart = true;
    private boolean failBeginMaintenance = true;

    public FailingCarbonTransport(String id) {
        super(id);
    }

    @Override
    public void start() {
        if (failStart) {
            throw new RuntimeException();
        }
    }

    @Override
    protected void stop() {
        throw new RuntimeException();
    }

    @Override
    protected void beginMaintenance() {
        if (failBeginMaintenance) {
            throw new RuntimeException();
        }
    }

    @Override
    protected void endMaintenance() {
        throw new RuntimeException();
    }

    public void setFailStart(boolean failStart) {
        this.failStart = failStart;
    }

    public void setFailBeginMaintenance(boolean failBeginMaintenance) {
        this.failBeginMaintenance = failBeginMaintenance;
    }
}
