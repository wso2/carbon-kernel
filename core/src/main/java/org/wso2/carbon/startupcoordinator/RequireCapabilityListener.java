/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.startupcoordinator;

/**
 * RequireCapabilityListener is a listener interface that may be implemented by a Carbon component developer. When
 * all the required capabilities are available, this event is asynchronously delivered to a RequireCapabilityListener.
 * <p>
 * A RequireCapabilityListener must be registered as an OSGi service. A bundle which registers an implementation of this
 * interface should contain a corresponding Provide-Capability manifest header.
 * <p>
 * e.g  Provide-Capability: osgi.service;effective:=active;
 * objectClass="org.wso2.carbon.startupcoordinator.RequireCapabilityListener";
 * required-service-interface="org.wso2.carbon.sample.transport.mgt.Transport"
 * <p>
 * This OSGi service must also be registered with a service property called "required-service-interface" as shown
 * in the above example. The value of this service property must be the key or the interface of the required OSGi
 * service.
 */
public interface RequireCapabilityListener {

    /**
     * Receives a notification when all the required services are available in the OSGi service registery.
     */
    void onAllRequiredCapabilitiesAvailable();
}
