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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests TenantIdConverter class.
 */
public class TenantIdConverterTest {

    private TenantIdConverter tenantIdConverter;
    private LogEvent logEvent;
    private static final String TENANT_ID = "tenantId";

    /**
     * Creates a log event to test appending the tenantId.
     */
    @BeforeMethod
    public void setUp() {

        tenantIdConverter = TenantIdConverter.newInstance(null);
        Message msg = new SimpleMessage("Test logging");
        logEvent = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(msg).build();
    }

    /**
     * Tests appending the tenantId into the log message.
     */
    @Test
    public void testFormat() {

        final StringBuilder sb = new StringBuilder();
        String tenantId = "-1";
        ThreadContext.put(TENANT_ID, tenantId);
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("TenantIdTestLogger")
                .setLevel(Level.INFO)
                .setMessage(null).build();

        tenantIdConverter.format(event, sb);
        assertEquals(tenantId, sb.toString());
        ThreadContext.remove(TENANT_ID);
    }
}
