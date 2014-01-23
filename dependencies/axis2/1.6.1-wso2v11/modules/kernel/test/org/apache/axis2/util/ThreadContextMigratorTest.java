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

package org.apache.axis2.util;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;

public class ThreadContextMigratorTest extends TestCase {
    private static String TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID
            = "Test-ThreadContextMigrator-List";
    private MessageContext messageContext;

    public void setUp() {
        messageContext = new ConfigurationContext(new AxisConfiguration()).createMessageContext();
    }

    public void testEmptyMigratorStructure()
            throws Exception {
        ThreadContextMigratorUtil
                .performMigrationToThread(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
        ThreadContextMigratorUtil
                .performMigrationToContext(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
        ThreadContextMigratorUtil
                .performThreadCleanup(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
        ThreadContextMigratorUtil
                .performContextCleanup(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
    }

    public void testMigration()
            throws Exception {
        TestMigrator testMigrator1 = new TestMigrator();
        TestMigrator testMigrator2 = new TestMigrator();
        ThreadContextMigratorUtil.addThreadContextMigrator(messageContext.getConfigurationContext(),
                                                           TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID,
                                                           testMigrator1);
        ThreadContextMigratorUtil.addThreadContextMigrator(messageContext.getConfigurationContext(),
                                                           TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID,
                                                           testMigrator2);
        ThreadContextMigratorUtil
                .performMigrationToThread(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
        assertTrue(testMigrator1.migratedToThread);
        assertTrue(testMigrator2.migratedToThread);
        ThreadContextMigratorUtil
                .performMigrationToContext(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
        assertTrue(testMigrator1.migratedToContext);
        assertTrue(testMigrator2.migratedToContext);
        ThreadContextMigratorUtil
                .performThreadCleanup(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
        assertTrue(testMigrator1.cleanedThread);
        assertTrue(testMigrator2.cleanedThread);
        ThreadContextMigratorUtil
                .performContextCleanup(TEST_THREAD_CONTEXT_MIGRATOR_LIST_ID, messageContext);
        assertTrue(testMigrator1.cleanedContext);
        assertTrue(testMigrator2.cleanedContext);
    }

    class TestMigrator implements ThreadContextMigrator {
        boolean migratedToThread;
        boolean cleanedThread;
        boolean migratedToContext;
        boolean cleanedContext;

        public void migrateContextToThread(MessageContext messageContext) throws AxisFault {
            migratedToThread = true;
        }

        public void cleanupThread(MessageContext messageContext) {
            cleanedThread = true;
        }

        public void migrateThreadToContext(MessageContext messageContext) throws AxisFault {
            migratedToContext = true;
        }

        public void cleanupContext(MessageContext messageContext) {
            cleanedContext = true;
        }

    }
}
