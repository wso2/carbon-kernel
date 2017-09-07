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
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.logging.CircularBuffer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests MemoryAppender class.
 */
public class MemoryAppenderTest {
    private MemoryAppender memoryAppender;
    private LogEvent logEvent;

    /**
     * Tests if appender is created when only the name is provided.
     *
     * The appender should apply default values for layout and ignore options.
     */
    @Test
    public void testCreateAppender() {
        memoryAppender = MemoryAppender
                .createAppender("CarbonTestAppender", null, null, null);
        assertNotNull(memoryAppender);
        assertNotNull(memoryAppender.getLayout());
        assertTrue(memoryAppender.ignoreExceptions());
    }

    /**
     * Tests if appender returns a null when the name is not provided.
     */
    @Test
    public void testCreateAppenderWithoutName() {
        assertNull(MemoryAppender.createAppender(
                null, null, null, null));
    }

    /**
     * Tests if append method adds the log event to the circular buffer.
     * The length of the circular queue object array should be incremented.
     */
    @Test
    public void testAppend() {
        memoryAppender = MemoryAppender
                .createAppender("CarbonTestAppender", null, null, null);
        Message msg = new SimpleMessage("Test logging");
        logEvent = Log4jLogEvent.newBuilder()
                .setLoggerName("TestLogger")
                .setLevel(Level.INFO)
                .setMessage(msg).build();

        memoryAppender.setBufferSize(10);
        memoryAppender.activateOptions();
        assertEquals(memoryAppender.getCircularQueue().
                getObjects(memoryAppender.getCircularQueue().getSize()).length,
                0);
        memoryAppender.append(logEvent);
        assertEquals(memoryAppender.getCircularQueue().
                        getObjects(memoryAppender.getCircularQueue().getSize()).length,
                1);
    }

    /**
     * Tests is the Circular buffer is set for given values or
     * applies a default size if not.
     */
    @Test
    public void testSetCircularBuffer() {
        memoryAppender = MemoryAppender
                .createAppender("CarbonTestAppender", null, null, null);
        memoryAppender.activateOptions();
        assertEquals(memoryAppender.getCircularQueue().getSize(), 10000);
        memoryAppender.setCircularBuffer(new CircularBuffer<LogEvent>(100));
        memoryAppender.setBufferSize(10);
        memoryAppender.activateOptions();
        assertEquals(memoryAppender.getBufferSize(), 10);
    }
}
