/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.kernel.transports;

import java.util.Map;

/**
 * MBean interface for exposing TransportManager functionalities.
 *
 * @since 5.1.0
 */
public interface TransportManagerMBean {

    public void startTransport(String transportId);

    public void stopTransport(String transportId);

    public void startTransports();

    public void stopTransports();

    public void beginMaintenance();

    public void endMaintenance();

    public Map<String, CarbonTransport> getTransports();

}
