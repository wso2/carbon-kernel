/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.impl;

import java.net.URL;

import junit.framework.TestCase;

import org.wso2.carbon.metrics.manager.Level;

/**
 * Test Cases for {@link MetricsConfiguration}
 */
public class MetricsConfigurationTest extends TestCase {

    private static final String LEVEL = "Level";
    private static final String CSV_REPORTING_LOCATION = "Reporting.CSV.Location";

    protected void setUp() throws Exception {
        System.setProperty("carbon.home", "/wso2/carbon");

        URL file = getClass().getResource("/metrics.xml");
        String filePath = file.getPath();
        MetricsConfiguration configuration = MetricsConfiguration.getInstance();
        configuration.load(filePath);
    }

    public void testConfigLoad() {
        String configLevel = MetricsConfiguration.getInstance().getFirstProperty(LEVEL);
        assertEquals("Level should be OFF", Level.OFF.name(), configLevel);

        String csvLocation = MetricsConfiguration.getInstance().getFirstProperty(CSV_REPORTING_LOCATION);
        assertEquals("/wso2/carbon/repository/logs/metrics/", csvLocation);
    }

    public void testSystemPropertiesReplacements() {
        System.setProperty("user.home", "/home/test");
        assertEquals("/home/test/file", MetricsConfiguration.replaceSystemProperties("${user.home}/file"));
        assertEquals("/home/test/file/wso2/carbon",
                MetricsConfiguration.replaceSystemProperties("${user.home}/file${carbon.home}"));
    }
}
