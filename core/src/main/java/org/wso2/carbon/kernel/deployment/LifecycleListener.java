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
package org.wso2.carbon.kernel.deployment;

/**
 * The interface for lifecycle listeners for artifact deployment events.
 *
 * To listen on artifact deployment events, developers should implement this
 * interface, and register it as an OSGi service
 *
 * @since 5.1.0
 */
public interface LifecycleListener {

    /**
     * Listen in on artifact deployment events and perform an action
     *
     * @param event The lifecycle event. The Artifact object and
     *              the currently triggered lifecycle event is stored
     *              in this.
     */
    void lifecycleEvent(LifecycleEvent event);

}
