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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a utility class to make it easier/cleaner for user programming
 * model-level implementations (e.g. the Axis2 JAX-WS code) to invoke the
 * ThreadContextMigrators.
 */
public class ThreadContextMigratorUtil {
    /**
     * Register a new ThreadContextMigrator.
     *
     * @param configurationContext
     * @param threadContextMigratorListID The name of the parameter in the
     *                                    AxisConfiguration that contains
     *                                    the list of migrators.
     * @param migrator
     */
    public static void addThreadContextMigrator(ConfigurationContext configurationContext,
                                                String threadContextMigratorListID,
                                                ThreadContextMigrator migrator)
    throws AxisFault {
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        addThreadContextMigrator(axisConfiguration, threadContextMigratorListID, migrator);
    }

    /**
     * Register a new ThreadContextMigrator.
     *
     * @param axisConfiguration
     * @param threadContextMigratorListID The name of the parameter in the
     *                                    AxisConfiguration that contains
     *                                    the list of migrators.
     * @param migrator
     */
    public static void addThreadContextMigrator(AxisConfiguration axisConfiguration,
                                                String threadContextMigratorListID,
                                                ThreadContextMigrator migrator)
    throws AxisFault {
        Parameter param = axisConfiguration.getParameter(threadContextMigratorListID);
        
        if (param == null) {
            param = new Parameter(threadContextMigratorListID, new LinkedList());
            axisConfiguration.addParameter(param);
        }

        List migratorList = (List) param.getValue();
        migratorList.add(migrator);
    }

    /**
     * Activate any registered ThreadContextMigrators to move context info
     * to the thread of execution.
     *
     * @param threadContextMigratorListID The name of the parameter in the
     *                                    AxisConfiguration that contains
     *                                    the list of migrators.
     * @param msgContext
     * @throws AxisFault
     */
    public static void performMigrationToThread(String threadContextMigratorListID,
                                                MessageContext msgContext)
    throws AxisFault {
        if (msgContext == null) {
            return;
        }

        AxisConfiguration axisConfiguration = 
            msgContext.getConfigurationContext().getAxisConfiguration();
        Parameter param = axisConfiguration.getParameter(threadContextMigratorListID);

        if (param != null) {
            List migratorList = (List) param.getValue();
            int size = migratorList.size();
            for (int i=0; i<size;i++) {
                ((ThreadContextMigrator) migratorList.get(i))
                .migrateContextToThread(msgContext);
            }
        }
    }

    /**
     * Activate any registered ThreadContextMigrators to remove information
     * from the thread of execution if necessary.
     *
     * @param threadContextMigratorListID The name of the parameter in the
     *                                    AxisConfiguration that contains
     *                                    the list of migrators.
     * @param msgContext
     */
    public static void performThreadCleanup(String threadContextMigratorListID,
                                            MessageContext msgContext) {
        if (msgContext == null) {
            return;
        }

        AxisConfiguration axisConfiguration = 
            msgContext.getConfigurationContext().getAxisConfiguration();
        Parameter param = axisConfiguration.getParameter(threadContextMigratorListID);

        if (param != null) {
            List migratorList = (List) param.getValue();
            int size = migratorList.size();
            for (int i=0; i<size;i++) {
                ((ThreadContextMigrator) migratorList.get(i)).cleanupThread(msgContext);
            }
        }
    }

    /**
     * Activate any registered ThreadContextMigrators to move info from the
     * thread of execution into the context.
     *
     * @param threadContextMigratorListID The name of the parameter in the
     *                                    AxisConfiguration that contains
     *                                    the list of migrators.
     * @param msgContext
     * @throws AxisFault
     */
    public static void performMigrationToContext(String threadContextMigratorListID,
                                                 MessageContext msgContext)
    throws AxisFault {
        if (msgContext == null) {
            return;
        }

        AxisConfiguration axisConfiguration = 
            msgContext.getConfigurationContext().getAxisConfiguration();
        Parameter param = axisConfiguration.getParameter(threadContextMigratorListID);

        if (param != null) {
            List migratorList = (List) param.getValue();
            int size = migratorList.size();
            for (int i=0; i<size;i++) {
                ((ThreadContextMigrator) migratorList.get(i))
                        .migrateThreadToContext(msgContext);
            }
        }
    }

    /**
     * Activate any registered ThreadContextMigrators to remove information from
     * the context if necessary.
     *
     * @param threadContextMigratorListID The name of the parameter in the
     *                                    AxisConfiguration that contains
     *                                    the list of migrators.
     * @param msgContext
     */
    public static void performContextCleanup(String threadContextMigratorListID,
                                             MessageContext msgContext) {
        if (msgContext == null) {
            return;
        }

        AxisConfiguration axisConfiguration = 
            msgContext.getConfigurationContext().getAxisConfiguration();
        Parameter param = axisConfiguration.getParameter(threadContextMigratorListID);

        if (param != null) {
            List migratorList = (List) param.getValue();
            int size = migratorList.size();
            for (int i=0; i<size;i++) {
                ((ThreadContextMigrator) migratorList.get(i)).cleanupContext(msgContext);
            }
        }
  }
}
