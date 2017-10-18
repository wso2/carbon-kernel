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

import static org.mockito.Mockito.mock;

/**
 * TransportStatisticsContainerTest includes test scenarios for
 * [1] property, transportStatistics of TransportStatisticsContainer.
 * @since 4.4.19
 */
public class TransportStatisticsContainerTest {

    private static final Logger log = Logger.getLogger("TransportStatisticsContainerTest");

    @Test(groups = {"org.wso2.carbon.tomcat.ext.transport.statistics"},
            description = "Testing getters and setters for transportStatistics.")
    public void testTransportStatistics () {
        // mocking inputs
        TransportStatisticsEntry transportStatisticsEntry1 = mock(TransportStatisticsEntry.class);
        TransportStatisticsEntry transportStatisticsEntry2 = mock(TransportStatisticsEntry.class);
        // calling set method
        TransportStatisticsContainer.addTransportStatisticsEntry(transportStatisticsEntry1);
        TransportStatisticsContainer.addTransportStatisticsEntry(transportStatisticsEntry2);
        // checking transportStatistics queue size
        log.info("Testing getters and setters for transportStatistics");
        Assert.assertEquals(TransportStatisticsContainer.getTransportStatistics().size(), 2,
                "Retrieved queue size did not match with set queue size");
    }
}
