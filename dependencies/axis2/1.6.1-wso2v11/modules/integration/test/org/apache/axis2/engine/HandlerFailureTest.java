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

package org.apache.axis2.engine;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;


public class HandlerFailureTest extends LocalTestCase {
    private static final Log log = LogFactory.getLog(HandlerFailureTest.class);

    private Handler culprit = new AbstractHandler() {
        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            throw new AxisFault(UtilServer.FAILURE_MESSAGE);
        }
    };
    
    public void setUp() throws Exception{
    	super.setUp();
    	AxisService as = deployClassAsService(Echo.SERVICE_NAME, Echo.class);
    	AxisOperation operation = as.getOperation(new QName(Echo.ECHO_OM_ELEMENT_OP_NAME));
    	
    	ArrayList phasec = new ArrayList();
        phasec.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(phasec);
        ArrayList phase = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phase.size(); i++) {
            Phase phase1 = (Phase)phase.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION.equals(phase1.getPhaseName())) {
                phase1.addHandler(culprit);
            }
        }
    }

    public void testFailureAtServerRequestFlow() throws Exception {
        try {
            ServiceClient sender = getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
            
            OMElement result = sender.sendReceive(TestingUtils.createDummyOMElement());
            result.serializeAndConsume(StAXUtils.createXMLStreamWriter(System.out));
            fail("the test must fail due to the intentional failure of the \"culprit\" handler");
        } catch (AxisFault e) {
            log.info(e.getMessage());
            String message = e.getMessage();
            assertTrue((message.indexOf(UtilServer.FAILURE_MESSAGE)) >= 0);
        }
    }
}

