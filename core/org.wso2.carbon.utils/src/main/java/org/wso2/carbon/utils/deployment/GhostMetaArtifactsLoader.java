/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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


package org.wso2.carbon.utils.deployment;

import org.apache.axis2.engine.AxisConfiguration;

/**
 * Common interface for all ghost meta artifact loaders (Service, Webapps)
 */
public interface GhostMetaArtifactsLoader {
    /**
     * This method will be used by all the ghost meta artifact loaders to define their logic
     * @param axisConfiguration - current tenant axis configuration
     * @param tenantDomian - current tenant domain
     */
    public void loadArtifacts(AxisConfiguration axisConfiguration, String tenantDomian);
}
