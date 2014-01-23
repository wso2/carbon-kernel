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

package org.apache.axis2.clustering.state;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.commands.DeleteServiceGroupStateCommand;
import org.apache.axis2.clustering.state.commands.StateClusteringCommandCollection;
import org.apache.axis2.clustering.state.commands.UpdateConfigurationStateCommand;
import org.apache.axis2.clustering.state.commands.UpdateServiceGroupStateCommand;
import org.apache.axis2.clustering.state.commands.UpdateServiceStateCommand;
import org.apache.axis2.clustering.state.commands.UpdateStateCommand;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.PropertyDifference;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public final class StateClusteringCommandFactory {

    private static final Log log = LogFactory.getLog(StateClusteringCommandFactory.class);

    public static StateClusteringCommandCollection
    getCommandCollection(AbstractContext[] contexts,
                         Map excludedReplicationPatterns) {

        ArrayList<StateClusteringCommand> commands = new ArrayList<StateClusteringCommand>(contexts.length);
        StateClusteringCommandCollection collection =
                new StateClusteringCommandCollection(commands);
        for (AbstractContext context : contexts) {
            StateClusteringCommand cmd = getUpdateCommand(context,
                                                          excludedReplicationPatterns,
                                                          false);
            if (cmd != null) {
                commands.add(cmd);
            }
        }
        return collection;
    }

    /**
     * @param context                  The context
     * @param excludedPropertyPatterns The property patterns to be excluded
     * @param includeAllProperties     True - Include all properties,
     *                                 False - Include only property differences
     * @return ContextClusteringCommand
     */
    public static StateClusteringCommand getUpdateCommand(AbstractContext context,
                                                          Map excludedPropertyPatterns,
                                                          boolean includeAllProperties) {

        UpdateStateCommand cmd = toUpdateContextCommand(context);
        if (cmd != null) {
            fillProperties(cmd,
                           context,
                           excludedPropertyPatterns,
                           includeAllProperties);
            if (cmd.isPropertiesEmpty()) {
                cmd = null;
            }
        }
        return cmd;
    }


    public static StateClusteringCommand getUpdateCommand(AbstractContext context,
                                                          String[] propertyNames)
            throws ClusteringFault {

        UpdateStateCommand cmd = toUpdateContextCommand(context);
        if (cmd != null) {
            fillProperties(cmd, context, propertyNames);
            if (cmd.isPropertiesEmpty()) {
                cmd = null;
            }
        }
        return cmd;
    }

    private static UpdateStateCommand toUpdateContextCommand(AbstractContext context) {
        UpdateStateCommand cmd = null;
        if (context instanceof ConfigurationContext) {
            cmd = new UpdateConfigurationStateCommand();
        } else if (context instanceof ServiceGroupContext) {
            ServiceGroupContext sgCtx = (ServiceGroupContext) context;
            cmd = new UpdateServiceGroupStateCommand();
            UpdateServiceGroupStateCommand updateSgCmd = (UpdateServiceGroupStateCommand) cmd;
            updateSgCmd.setServiceGroupName(sgCtx.getDescription().getServiceGroupName());
            updateSgCmd.setServiceGroupContextId(sgCtx.getId());
        } else if (context instanceof ServiceContext) {
            ServiceContext serviceCtx = (ServiceContext) context;
            cmd = new UpdateServiceStateCommand();
            UpdateServiceStateCommand updateServiceCmd = (UpdateServiceStateCommand) cmd;
            String sgName =
                    serviceCtx.getServiceGroupContext().getDescription().getServiceGroupName();
            updateServiceCmd.setServiceGroupName(sgName);
            updateServiceCmd.setServiceGroupContextId(serviceCtx.getServiceGroupContext().getId());
            updateServiceCmd.setServiceName(serviceCtx.getAxisService().getName());
        }
        return cmd;
    }

    /**
     * @param updateCmd                The command
     * @param context                  The context
     * @param excludedPropertyPatterns The property patterns to be excluded from replication
     * @param includeAllProperties     True - Include all properties,
     *                                 False - Include only property differences
     */
    private static void fillProperties(UpdateStateCommand updateCmd,
                                       AbstractContext context,
                                       Map excludedPropertyPatterns,
                                       boolean includeAllProperties) {
        if (!includeAllProperties) {
            synchronized (context) {
                Map diffs = context.getPropertyDifferences();
                for (Object o : diffs.keySet()) {
                    String key = (String) o;
                    PropertyDifference diff = (PropertyDifference) diffs.get(key);
                    Object value = diff.getValue();
                    if (isSerializable(value)) {

                        // Next check whether it matches an excluded pattern
                        if (!isExcluded(key,
                                        context.getClass().getName(),
                                        excludedPropertyPatterns)) {
                            if (log.isDebugEnabled()) {
                                log.debug("sending property =" + key + "-" + value);
                            }
                            updateCmd.addProperty(diff);
                        }
                    }
                }
            }
        } else {
            synchronized (context) {
                for (Iterator iter = context.getPropertyNames(); iter.hasNext();) {
                    String key = (String) iter.next();
                    Object value = context.getPropertyNonReplicable(key);
                    if (isSerializable(value)) {

                        // Next check whether it matches an excluded pattern
                        if (!isExcluded(key, context.getClass().getName(), excludedPropertyPatterns)) {
                            if (log.isDebugEnabled()) {
                                log.debug("sending property =" + key + "-" + value);
                            }
                            PropertyDifference diff = new PropertyDifference(key, value, false);
                            updateCmd.addProperty(diff);
                        }
                    }
                }
            }
        }
    }

    private static void fillProperties(UpdateStateCommand updateCmd,
                                       AbstractContext context,
                                       String[] propertyNames) throws ClusteringFault {
        Map diffs = context.getPropertyDifferences();
        for (String key : propertyNames) {
            Object prop = context.getPropertyNonReplicable(key);

            // First check whether it is serializable
            if (isSerializable(prop)) {
                if (log.isDebugEnabled()) {
                    log.debug("sending property =" + key + "-" + prop);
                }
                PropertyDifference diff = (PropertyDifference) diffs.get(key);
                if (diff != null) {
                    diff.setValue(prop);
                    updateCmd.addProperty(diff);

                    // Remove the diff?
                    diffs.remove(key);
                }
            } else {
                String msg =
                        "Trying to replicate non-serializable property " + key +
                        " in context " + context;
                throw new ClusteringFault(msg);
            }
        }
    }

    private static boolean isExcluded(String propertyName,
                                      String ctxClassName,
                                      Map excludedPropertyPatterns) {

        // Check in the excludes list specific to the context
        List specificExcludes =
                (List) excludedPropertyPatterns.get(ctxClassName);
        boolean isExcluded = false;
        if (specificExcludes != null) {
            isExcluded = isExcluded(specificExcludes, propertyName);
        }
        if (!isExcluded) {
            // check in the default excludes
            List defaultExcludes =
                    (List) excludedPropertyPatterns.get(DeploymentConstants.TAG_DEFAULTS);
            if (defaultExcludes != null) {
                isExcluded = isExcluded(defaultExcludes, propertyName);
            }
        }
        return isExcluded;
    }

    private static boolean isExcluded(List list, String propertyName) {
        for (Object aList : list) {
            String pattern = (String) aList;
            if (pattern.startsWith("*")) {
                pattern = pattern.replaceAll("\\*", "");
                if (propertyName.endsWith(pattern)) {
                    return true;
                }
            } else if (pattern.endsWith("*")) {
                pattern = pattern.replaceAll("\\*", "");
                if (propertyName.startsWith(pattern)) {
                    return true;
                }
            } else if (pattern.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public static StateClusteringCommand getRemoveCommand(AbstractContext abstractContext) {
        if (abstractContext instanceof ServiceGroupContext) {
            ServiceGroupContext sgCtx = (ServiceGroupContext) abstractContext;
            DeleteServiceGroupStateCommand cmd = new DeleteServiceGroupStateCommand();
            cmd.setServiceGroupContextId(sgCtx.getId());

            return cmd;
        }
        return null;
    }

    private static boolean isSerializable(Object obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(obj);
            oos.close();
            return out.toByteArray().length > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
