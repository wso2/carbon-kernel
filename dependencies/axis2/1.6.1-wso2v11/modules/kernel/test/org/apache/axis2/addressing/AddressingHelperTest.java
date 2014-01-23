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

package org.apache.axis2.addressing;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;

public class AddressingHelperTest extends TestCase {

    public void testIsReplyRedirectedNoReplyTo() {
        MessageContext mc = new MessageContext();
        assertFalse(AddressingHelper.isReplyRedirected(mc));
    }

    public void testIsReplyRedirectedAnonReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        assertFalse(AddressingHelper.isReplyRedirected(mc));
    }

    public void testIsReplyRedirectedNonAnonReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference("http://ws.apache.org/axis2"));
        assertTrue(AddressingHelper.isReplyRedirected(mc));
    }

    public void testIsReplyRedirectedNoneReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));
        assertTrue(AddressingHelper.isReplyRedirected(mc));
    }

    public void testIsFaultRedirectedNoFaultToOrReplyTo() {
        MessageContext mc = new MessageContext();
        assertFalse(AddressingHelper.isFaultRedirected(mc));
    }

    public void testIsFaultRedirectedNoFaultTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference("http://ws.apache.org/axis2"));
        assertTrue(AddressingHelper.isFaultRedirected(mc));
    }

    public void testIsFaultRedirectedAnonFaultTo() {
        MessageContext mc = new MessageContext();
        mc.setFaultTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        assertFalse(AddressingHelper.isFaultRedirected(mc));
    }

    public void testIsFaultRedirectedNonAnonFaultTo() {
        MessageContext mc = new MessageContext();
        mc.setFaultTo(new EndpointReference("http://ws.apache.org/axis2"));
        assertTrue(AddressingHelper.isFaultRedirected(mc));
    }

    public void testIsFaultRedirectedNoneReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));
        assertTrue(AddressingHelper.isFaultRedirected(mc));
    }

    public void testIsSyncReplyAllowedNoReplyTo() {
        MessageContext mc = new MessageContext();
        assertTrue(AddressingHelper.isSyncReplyAllowed(mc));
    }

    public void testIsSyncReplyAllowedAnonReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        assertTrue(AddressingHelper.isSyncReplyAllowed(mc));
    }

    public void testIsSyncReplyAllowedNonAnonReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference("http://ws.apache.org/axis2"));
        assertFalse(AddressingHelper.isSyncReplyAllowed(mc));
    }

    public void testIsSyncReplyAllowedNoneReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));
        assertTrue(AddressingHelper.isSyncReplyAllowed(mc));
    }

    public void testIsSyncFaultAllowedNoFaultToOrReplyTo() {
        MessageContext mc = new MessageContext();
        assertTrue(AddressingHelper.isSyncFaultAllowed(mc));
    }

    public void testIsSyncFaultAllowedNoFaultTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference("http://ws.apache.org/axis2"));
        assertFalse(AddressingHelper.isSyncFaultAllowed(mc));
    }

    public void testIsSyncFaultAllowedAnonFaultTo() {
        MessageContext mc = new MessageContext();
        mc.setFaultTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        assertTrue(AddressingHelper.isSyncFaultAllowed(mc));
    }

    public void testIsSyncFaultAllowedNonAnonFaultTo() {
        MessageContext mc = new MessageContext();
        mc.setFaultTo(new EndpointReference("http://ws.apache.org/axis2"));
        assertFalse(AddressingHelper.isSyncFaultAllowed(mc));
    }

    public void testIsSyncFaultAllowedNoneReplyTo() {
        MessageContext mc = new MessageContext();
        mc.setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_NONE_URI));
        assertTrue(AddressingHelper.isSyncFaultAllowed(mc));
    }
    
    public void testGetInvocationPatternParameterValueFromAxisOperation() throws Exception {
        AxisService axisService = new AxisService();
        AxisOperation axisOperation = new InOutAxisOperation();
        axisService.addOperation(axisOperation);
        
        // Set invocation pattern on AxisOperation only 
        AddressingHelper.setInvocationPatternParameterValue(axisOperation,
                AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS);
        
        String value = AddressingHelper
                .getInvocationPatternParameterValue(axisOperation);
        assertEquals(value, AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS);
    }
    
    public void testGetInvocationPatternParameterValueFromAxisService() throws Exception {
        AxisService axisService = new AxisService();
        AxisOperation axisOperation = new InOutAxisOperation();
        axisService.addOperation(axisOperation);

        // Set invocation pattern on AxisService only
        axisService.addParameter(new Parameter(
                AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME,
                AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS));

        String value = AddressingHelper
                .getInvocationPatternParameterValue(axisOperation);
        assertEquals(value, AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS);
    }
    
    public void testGetInvocationPatternParameterValueFromBoth() throws Exception {
        AxisService axisService = new AxisService();
        AxisOperation axisOperation = new InOutAxisOperation();
        axisService.addOperation(axisOperation);

        // Set invocation pattern on AxisOperation and AxisService 
        AddressingHelper.setInvocationPatternParameterValue(axisOperation,
                AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS);
        axisService.addParameter(new Parameter(
                AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME,
                AddressingConstants.WSAM_INVOCATION_PATTERN_SYNCHRONOUS));

        // Check that the AxisOperation value has precedence over the AxisService value
        String value = AddressingHelper
                .getInvocationPatternParameterValue(axisOperation);
        assertEquals(value, AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS);
    }
}
