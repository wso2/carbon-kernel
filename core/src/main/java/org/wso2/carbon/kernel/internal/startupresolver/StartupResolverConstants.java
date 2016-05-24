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
package org.wso2.carbon.kernel.internal.startupresolver;

/**
 * Constants required for the startup order resolver module.
 *
 * @since 5.1.0
 */
class StartupResolverConstants {

    static final String CARBON_COMPONENT_HEADER = "Carbon-Component";
    static final String CAPABILITY_NAME = "capabilityName";
    static final String COMPONENT_NAME = "componentName";
    static final String STARTUP_LISTENER_COMPONENT = "startup.listener";
    static final String OSGI_SERVICE_COMPONENT = "osgi.service";
    static final String DEPENDENT_COMPONENT_NAME = "dependentComponentName";
    static final String REQUIRED_BY_COMPONENT_NAME = "requiredByComponentName";
    static final String SERVICE_COUNT = "serviceCount";
    static final String OBJECT_CLASS = "objectClass";
    static final String CAPABILITY_NAME_SPLIT_CHAR = ",";
    static final String REQUIRED_SERVICE = "requiredService";


    private StartupResolverConstants() {
        throw new AssertionError("Trying to a instantiate a constant class");
    }
}
