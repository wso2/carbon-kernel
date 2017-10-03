/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.transport.statistics;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.logging.Logger;

/**
 * TransportStatisticsEntryTest includes test scenarios for
 * [1] functions, getContext () and getTenantName () of TransportStatisticsEntry.
 * [2] properties, requestSize, responseSize and requestUrl of TransportStatisticsEntry.
 * @since 4.4.19
 */
public class TransportStatisticsEntryTest {

    private static final Logger log = Logger.getLogger("TransportStatisticsEntryTest");

    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport.statistics"},
            description = "Testing getters and setters for requestSize, responseSize and requestUrl.")
    public void testTransportStatisticsEntry () {
        log.info("Testing getters and setters for TransportStatisticsEntry" +
                " requestSize, responseSize and requestUrl");
        TransportStatisticsEntry transportStatisticsEntry = new TransportStatisticsEntry
                (146515L, 162315L, "http://example.com/services/t/abc.com/echo");
        Assert.assertTrue(transportStatisticsEntry.getRequestSize() == 146515L,
                "Returned byte size does not match to set size");
        Assert.assertTrue(transportStatisticsEntry.getResponseSize() == 162315L,
                "Returned byte size does not match to set size");
        Assert.assertTrue("http://example.com/services/t/abc.com/echo".equals(transportStatisticsEntry
                .getRequestUrl()), "Returned URL does not match to set URL");
        // update transportStatisticsEntry with new request URL
        transportStatisticsEntry.setRequestUrl("http://example.com/services/t/abc.com/foo");
        // check if transportStatisticsEntry was updated with the new request URL
        Assert.assertTrue("http://example.com/services/t/abc.com/foo".equals(transportStatisticsEntry.getRequestUrl()),
                "Returned URL does not match to set URL");
    }

    /**
     * Checks getTenantName functionality for services.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport.statistics"})
    public void testGetTenantName () {
        log.info("Testing getTenantName () functionality for services");
        TransportStatisticsEntry transportStatisticsEntry = new TransportStatisticsEntry
                (146515L, 162315L, "http://example.com/services/t/abc.com/echo");
        Assert.assertTrue("abc.com".equals(transportStatisticsEntry.getTenantName()),
                "Returned tenant domain does not match with expected");
    }

    /**
     * Checks getContext functionality for services.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport.statistics"})
    public void testGetContext () {
        log.info("Testing getContext () functionality for services");
        TransportStatisticsEntry transportStatisticsEntry = new TransportStatisticsEntry
                (146515L, 162315L, "http://example.com/services/t/abc.com/echo");
        Assert.assertTrue("services".equals(transportStatisticsEntry.getContext()),
                "Returned application context does not match with expected");
    }
}
