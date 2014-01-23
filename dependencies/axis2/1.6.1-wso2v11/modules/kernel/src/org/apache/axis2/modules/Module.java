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


package org.apache.axis2.modules;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * Every module provides an implementation of this class. Modules are in one of
 * two states: "available" or "engaged". All modules that the runtime
 * detects (from the system modules/ directory or from other means) are said to
 * be in the "available" state. If some service indicates a dependency on this
 * module then the module is initialized (once for the life of the system) and
 * the state changes to "initialized".
 * <p/>
 * <p/>Any module which is in the "engaged" state can be engaged as needed
 * by the engine to respond to a message. Currently module engagement is done
 * via deployment (using module.xml). In the future we may engage modules
 * programmatically by introducing an engage() method to this interface, thereby
 * allowing more dynamic scenarios.
 */
public interface Module {

    // initialize the module
    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault;

    /**
     * When engaging this module to some service or operation , module will be notify by calling this
     * method there module author can validate , add policy and do any thing that he want , and he can
     * refuse the engage as well
     *
     * @param axisDescription
     * @throws AxisFault
     */
    void engageNotify(AxisDescription axisDescription) throws AxisFault;

    /**
     * Evaluate whether it can support the specified assertion and returns true if the assertion can
     * be supported.
     *
     * @param assertion the assertion that the module must decide whether it can support or not.
     * @return true if the specified assertion can be supported by the module
     */
    public boolean canSupportAssertion(Assertion assertion);

    /**
     * Evaluates specified policy for the specified AxisDescription. It computes the configuration that
     * is appropriate to support the policy and stores it the appropriate description.
     *
     * @param policy the policy that is applicable for the specified AxisDescription
     * @throws AxisFault if anything goes wrong.
     */
    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault;


    // shutdown the module
    public void shutdown(ConfigurationContext configurationContext) throws AxisFault;
}
