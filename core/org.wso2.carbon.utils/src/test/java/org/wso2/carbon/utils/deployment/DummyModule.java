/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.utils.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.modules.Module;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * Dummy axis2 module used with Axis2ModuleRegistry.
 */
public class DummyModule implements Module {
    @Override
    public void init(ConfigurationContext configurationContext, AxisModule axisModule) throws AxisFault {

    }

    @Override
    public void engageNotify(AxisDescription axisDescription) throws AxisFault {

    }

    @Override
    public boolean canSupportAssertion(Assertion assertion) {
        return false;
    }

    @Override
    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {

    }

    @Override
    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {

    }
}
