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

package org.apache.axis2.clustering.management;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.ParameterInclude;

/**
 * <p>
 * This interface is responsible for handling configuration management. Configuraion changes include
 * <p/>
 * <ul>
 * <li>Rebooting an entire cluster, in which case, all nodes have to load the new Axis2 configuration
 * in a consistent manner
 * </li>
 * <li>
 * Deploying a new service to a cluster or undeploying a service from a cluster
 * </li>
 * <li>
 * Changing the policies of a service deployed on the cluster
 * </li>
 * </ul>
 * </p>
 * <p>
 * It is not mandatory to have a NodeManager in a node. In which case the cluster may be
 * used only for <a href="http://blog.afkham.org/2008/05/definition-of-high-availability.html">
 * High Availability</a> through context replication. However, it is difficult to imagine that
 * a cluster will be deployed in production with only context replication but without cluster
 * configuration management.
 * </p>
 * <p>
 * The implementation of this interface is set by the
 * {@link org.apache.axis2.deployment.ClusterBuilder}, by
 * reading the  "configurationManager" element in the axis2.xml
 * <p/>
 * e.g.
 * <code>
 * <b>
 * <configurationManager class="org.apache.axis2.cluster.configuration.TribesConfigurationManager">
 * </b>
 * </code>
 * </p>
 */
public interface NodeManager extends ParameterInclude {

    // ###################### Configuration management methods ##########################
    /**//**
     * Load a set of service groups
     *
     * @param serviceGroupNames The set of service groups to be loaded
     * @throws ClusteringFault If an error occurs while loading service groups
     *//*
    void loadServiceGroups(String[] serviceGroupNames) throws ClusteringFault;

    *//**
     * Unload a set of service groups
     *
     * @param serviceGroupNames The set of service groups to be unloaded
     * @throws ClusteringFault If an error occurs while unloading service groups
     *//*
    void unloadServiceGroups(String[] serviceGroupNames) throws ClusteringFault;

    *//**
     * Apply a policy to a service
     *
     * @param serviceName The name of the service to which this policy needs to be applied
     * @param policy      The serialized policy to be applied to the service
     * @throws ClusteringFault If an error occurs while applying service policies
     *//*
    void applyPolicy(String serviceName, String policy) throws ClusteringFault;

    *//**
     * Reload the entire configuration of an Axis2 Node
     *
     * @throws ClusteringFault If an error occurs while reinitializing Axis2
     *//*
    void reloadConfiguration() throws ClusteringFault;*/

    // ###################### Transaction management methods ##########################

    /**
     * First phase of the 2-phase commit protocol.
     * Notifies a node that it needs to prepare to switch to a new configuration.
     *
     * @throws ClusteringFault If an error occurs while preparing to commit
     */
    void prepare() throws ClusteringFault;

    /**
     * Rollback whatever was done
     *
     * @throws ClusteringFault If an error occurs while rolling back a cluster configuration
     *                         transaction
     */
    void rollback() throws ClusteringFault;

    /**
     * Second phase of the 2-phase commit protocol.
     * Notifies a node that it needs to switch to a new configuration.
     *
     * @throws ClusteringFault If an error occurs while committing a cluster configuration
     *                         transaction
     */
    void commit() throws ClusteringFault;

    // ######################## General management methods ############################
    /**
     * To notify other nodes that an Exception occurred, during the processing
     * of a {@link NodeManagementCommand}
     *
     * @param throwable The throwable which has to be propogated to other nodes
     * @throws org.apache.axis2.clustering.ClusteringFault
     *          If an error occurs while processing the
     *          exception message
     */
    void exceptionOccurred(Throwable throwable) throws ClusteringFault;

    /**
     * Set the system's configuration context. This will be used by the clustering implementations
     * to get information about the Axis2 environment and to correspond with the Axis2 environment
     *
     * @param configurationContext The configuration context
     */
    void setConfigurationContext(ConfigurationContext configurationContext);

    /**
     * Execute a NodeManagementCommand
     * 
     * @param command  The command to be executed
     * @throws ClusteringFault If an error occurs while sending the message
     */
    void sendMessage(NodeManagementCommand command) throws ClusteringFault;
}