/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.core.multitenancy.transports;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TenantTransportSenderTest {
    private static final String DELETE_REQ_WITH_PAYLOAD = "DELETE_REQUEST_WITH_PAYLOAD";

    @Test
    public void testSetDeleteRequestWithPlayLoadWithTrue() {
        TenantTransportSender tenantTransportSender = new TenantTransportSender(
                new ConfigurationContext(new AxisConfiguration()));
        MessageContext superTenantOutMessageContext = new MessageContext();
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(DELETE_REQ_WITH_PAYLOAD, true);
        tenantTransportSender.setDeleteRequestWithPayloadProperty(superTenantOutMessageContext, messageContext);
        Assert.assertNotNull(superTenantOutMessageContext.getProperty(DELETE_REQ_WITH_PAYLOAD));
        Assert.assertEquals(superTenantOutMessageContext.getProperty(DELETE_REQ_WITH_PAYLOAD), true);
    }

    @Test
    public void testSetDeleteRequestWithPlayLoadWithFalse() {
        TenantTransportSender tenantTransportSender = new TenantTransportSender(
                new ConfigurationContext(new AxisConfiguration()));
        MessageContext superTenantOutMessageContext = new MessageContext();
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty(DELETE_REQ_WITH_PAYLOAD, false);
        tenantTransportSender.setDeleteRequestWithPayloadProperty(superTenantOutMessageContext, messageContext);
        Assert.assertNotNull(superTenantOutMessageContext.getProperty(DELETE_REQ_WITH_PAYLOAD));
        Assert.assertEquals(superTenantOutMessageContext.getProperty(DELETE_REQ_WITH_PAYLOAD), false);
    }

    @Test
    public void testSetDeleteRequestWithPlayLoadWithNull() {
        TenantTransportSender tenantTransportSender = new TenantTransportSender(
                new ConfigurationContext(new AxisConfiguration()));
        MessageContext superTenantOutMessageContext = new MessageContext();
        MessageContext messageContext = new MessageContext();
        tenantTransportSender.setDeleteRequestWithPayloadProperty(superTenantOutMessageContext, messageContext);
        Assert.assertNull(superTenantOutMessageContext.getProperty(DELETE_REQ_WITH_PAYLOAD));
    }
}
