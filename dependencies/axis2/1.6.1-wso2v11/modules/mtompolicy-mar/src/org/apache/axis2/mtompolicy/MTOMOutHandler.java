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

import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

/**
 * This out handler does a check of the effective policy set. Based on that the corresponding MTOM property </br>
 * value is set.
 */
public class MTOMOutHandler extends AbstractHandler {

	/**
	 * Checks the effective policy set and based on it the <code>enableMTOM</code> is set to the appropriate value.</br>
	 * E.g. if the policy states that MTOM is <code>optional</code> then the <code>enableMTOM</code> is set to this value.  
	 * 
	 * @param msgCtx the {@link MessageContext}
     * 
     * @throws AxisFault
	 */
    public InvocationResponse invoke(MessageContext msgCtx) throws AxisFault {

        Policy policy = msgCtx.getEffectivePolicy();

        if (policy == null) {
            return InvocationResponse.CONTINUE;
        }

        // TODO When we have policy alternatives support we will have to change
        // the implementation.
        List<Assertion> list = (List<Assertion>) policy.getAlternatives().next();

        for (Assertion assertion : list) {
            if (assertion instanceof MTOMAssertion) {
                boolean isOptional = assertion.isOptional();

                if (isOptional) {
                    msgCtx.setProperty(Constants.Configuration.ENABLE_MTOM,
                            Constants.VALUE_OPTIONAL);
                } else {
                    msgCtx.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
                }

            }
        }

        return InvocationResponse.CONTINUE;
    }

}
