/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.service;

import java.util.List;
import java.util.Map;

/**
 * An API that can be used to implement a simulator to simulate handler execution.
 */
@SuppressWarnings("unused")
public interface SimulationService {

    /**
     * Starts or stops simulation mode.
     *
     * @param simulation set this to <b>true</b> to start or <b>false</b> to stop simulation.
     */
    void setSimulation(boolean simulation);

    /**
     * Retrieves results after running a simulation.
     *
     * @return the map of execution status of handlers. The key is the fully qualified handler class
     *         name and the values are a list of strings, which could contain, <b>Successful</b>,
     *         <b>Failed</b>, or the detail message of the exception that occurred.
     */
    Map<String, List<String[]>> getSimulationStatus();

}
