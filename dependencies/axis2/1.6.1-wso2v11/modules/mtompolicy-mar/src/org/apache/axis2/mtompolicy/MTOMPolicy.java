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

package org.apache.axis2.mtompolicy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.modules.Module;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * The module implementation class for the MTOM module.
 *
 */
public class MTOMPolicy implements Module {

    private static final Log log = LogFactory.getLog(MTOMPolicy.class);

    public void applyPolicy(Policy policy, AxisDescription axisDescription)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("applyPolicy() called on MTOMPolicy module");
        }
        
    }

    public boolean canSupportAssertion(Assertion assertion) {

        if (log.isDebugEnabled()) {
            log.debug("canSupportAssertion called on MTOMPolicy module with "
                    + assertion.getName().toString() + " assertion");
        }

        if (assertion instanceof MTOMAssertion) {
            return true;
        }

        return false;
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("MTOMPolicy module has been engaged to "
                    + axisDescription.getClass().getName());
        }

        AxisService axisService = Utils.locateAxisService(axisDescription);

        if (axisService == null) {
            if (log.isDebugEnabled()) {
                log.debug("MTOMPolicy module couldn't find the Axis Service ");
            }
            return;
        }

        Parameter param = axisService
                .getParameter(Constants.Configuration.ENABLE_MTOM);

        Policy mtomPolicy = Utils.getMTOMPolicy(param);

        if (mtomPolicy != null) {
            Utils.applyPolicyToSOAPBindings(axisService, mtomPolicy);
        }

    }

    public void init(ConfigurationContext configContext, AxisModule module)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("init() called on MTOMPolicy module");
        }

    }

    public void shutdown(ConfigurationContext configurationContext)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("shutdown() called on MTOMPolicy module");
        }

    }

}
