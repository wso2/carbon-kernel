/*
 * Copyright (c) 2005, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This is registered as an implementation of the OSGi service Axis2ConfigurationContextObserver.
 * This service will be notified when AxisConfigurationContexts are created and destroyed.
 */
public class SecurityAxis2ConfigurationContextObserver extends
        AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(SecurityAxis2ConfigurationContextObserver.class);

    @Override
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        AxisModule poxSecModule =
                axisConfig.getModule("POXSecurityModule");
        if (poxSecModule != null) {
            try {
                axisConfig.engageModule(poxSecModule);
            } catch (AxisFault e) {
                log.error("Cannot globally engage POX Security module", e);
            }
        }
    }

}
