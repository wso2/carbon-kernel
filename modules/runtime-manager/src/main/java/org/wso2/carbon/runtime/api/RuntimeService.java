/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.runtime.api;

import org.wso2.carbon.runtime.exception.RuntimeServiceException;

/**
 * User level API's for consuming RuntimeManager functionality.
 * This will be registered as an OSGI service so that users can reference this in their component.
 */

public interface RuntimeService {

    /**
     * Users can call this method to start all registered runtime
     * @throws RuntimeServiceException - on error while starting registered runtime
     */
    void startRuntimes() throws RuntimeServiceException;

    /**
     * Users can call this method to stop all registered runtime
     * @throws RuntimeServiceException
     */
    void stopRuntimes() throws RuntimeServiceException;

    /**
     * Users can call this method to put all registered runtime into MAINTENANCE state
     * @throws RuntimeServiceException
     */
    void beginMaintenance() throws RuntimeServiceException;

    /**
     * Users can call this method to put all registered runtime into INACTIVE state
     * @throws RuntimeServiceException
     */
    void endMaintenance() throws RuntimeServiceException;

}
