/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.sample.module;

import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.modules.Module;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

public class LogginModule implements Module {
    // initialize the module
    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
       
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean canSupportAssertion(Assertion assertion) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }// shutdown the module
    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
