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

package org.apache.axis2.phaserule;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PhaseRuleHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(PhaseRuleHandler.class);
    private String name;

    public PhaseRuleHandler() {
    }

    public PhaseRuleHandler(String name) {
        this.name = name;
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        log.info("I am " + name + " Handler Running :)");
        return InvocationResponse.CONTINUE;
    }

    public void revoke(MessageContext msgContext) {
        log.info("I am " + name + " Handler Running :)");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void init(HandlerDescription handlerdesc) {
        super.init(handlerdesc);
    }
}
