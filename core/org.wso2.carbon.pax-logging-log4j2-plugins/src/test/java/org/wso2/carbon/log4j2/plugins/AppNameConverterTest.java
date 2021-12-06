/*
 * Copyright 2017 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.log4j2.plugins;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.logging.handler.TenantDomainSetter;

import static org.testng.Assert.assertEquals;

/**
 * Tests AppNameConverter class.
 */
@PrepareForTest(TenantDomainSetter.class)
public class AppNameConverterTest extends PowerMockTestCase {

    private AppNameConverter appNameConverter;
    private LogEvent logEvent;
    private static final String APP_NAME = "appName";

    /**
     * Creates a log event to test appending of the AppName.
     */
    @BeforeMethod
    public void setUp() {

        appNameConverter = AppNameConverter.newInstance(null);
        Message msg = new SimpleMessage("Test logging");
        logEvent = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(msg).build();
    }

    /**
     * Tests appending the AppName into the log message.
     */
    @Test
    public void testFormat() {

        final StringBuilder sb = new StringBuilder();
        String appName = "TestApp";
        ThreadContext.put(APP_NAME, appName);
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("AppNameTestLogger")
                .setLevel(Level.INFO)
                .setMessage(null).build();

        appNameConverter.format(event, sb);
        assertEquals(appName, sb.toString());
        ThreadContext.remove(APP_NAME);
    }
}
