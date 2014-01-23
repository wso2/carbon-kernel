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
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.ParameterInclude;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * This interface is responsible for handling context replication. The property changes in the
 * <a href="http://www.ibm.com/developerworks/webservices/library/ws-apacheaxis2/">
 * Axis2 context hierarchy
 * </a> in this node, are propagated to all other nodes in the cluster.
 * </p>
 * <p>
 * It is not mandatory to have a StateManager in a node. If we are not interested in
 * <a href="http://blog.afkham.org/2008/05/definition-of-high-availability.html">
 * High Availability</a>, we may disable context replication by commenting out the "contextManager"
 * section in the axis2.xml cluster configuration section. In such a scenatio, the cluster will be
 * used only for the purpose of
 * <a href="http://blog.afkham.org/2008/05/definition-of-scalability.html">Scalability</a>
 * </p>
 * <p>
 * The implementation of this interface is set by the
 * {@link org.apache.axis2.deployment.ClusterBuilder}, by
 * reading the  "contextManager" element in the axis2.xml
 * <p/>
 * e.g.
 * <code>
 * <b>
 * <contextManager class="org.apache.axis2.cluster.configuration.TribesContextManager">
 * </b>
 * </code>
 * </p>
 */
public interface StateManager extends ParameterInclude {

    /**
     * This method is called when properties in an {@link AbstractContext} are updated.
     * This could be addition of new properties, modifications of existing properties or
     * removal of properties.
     *
     * @param context The context to be replicated
     * @throws ClusteringFault If replication fails
     */
    void updateContext(AbstractContext context) throws ClusteringFault;

    /**
     * This method is called when one need to update/replicate only certains properties in the
     * specified <code>context</code>
     *
     * @param context       The AbstractContext containing the properties to be replicated
     * @param propertyNames The names of the specific properties that should be replicated
     * @throws ClusteringFault If replication fails
     */
    void updateContext(AbstractContext context, String[] propertyNames) throws ClusteringFault;

    /**
     * This method is called when properties in a collection of {@link AbstractContext}s are updated.
     * This could be addition of new properties, modifications of existing properties or
     * removal of properties.
     *
     * @param contexts The AbstractContexts containing the properties to be replicated
     * @throws ClusteringFault If replication fails
     */
    void updateContexts(AbstractContext[] contexts) throws ClusteringFault;

    /**
     * Replicate state using a custom StateClusteringCommand
     *
     * @param command The custom StateClusteringCommand which can be used for replicating state
     * @throws ClusteringFault If replication fails
     */
    void replicateState(StateClusteringCommand command) throws ClusteringFault;

    /**
     * This method is called when {@link AbstractContext} is removed from the system
     *
     * @param context The AbstractContext to be removed
     * @throws ClusteringFault If context removal fails
     */
    void removeContext(AbstractContext context) throws ClusteringFault;

    /**
     * This is a check to see whether the properties in an instance of {@link AbstractContext}
     * should be replicated. This allows an implementer to dissallow the replication of properties
     * stored in a certain type of context
     *
     * @param context The instance of AbstractContext under consideration
     * @return True - if the provided {@link AbstractContext}  is clusterable
     */
    boolean isContextClusterable(AbstractContext context);

    /**
     * Set the system's configuration context. This will be used by the clustering implementations
     * to get information about the Axis2 environment and to correspond with the Axis2 environment
     *
     * @param configurationContext The configuration context
     */
    void setConfigurationContext(ConfigurationContext configurationContext);

    /**
     * <p>
     * All properties in the context with type <code>contextType</code> which have
     * names that match the specified pattern will be excluded from replication.
     * </p>
     * <p/>
     * <p>
     * Only prefixes and suffixes are allowed. e.g. the local_* pattern indicates that
     * all property names starting with local_ should be omitted from replication. *_local pattern
     * indicated that all property names ending with _local should be omitted from replication.
     * * pattern indicates that all properties should be excluded.
     * </p>
     * <p>
     * Generally, we can use the context class name as the context type.
     * </p>
     *
     * @param contextType The type of the context such as
     *                    org.apache.axis2.context.ConfigurationContext,
     *                    org.apache.axis2.context.ServiceGroupContext &
     *                    org.apache.axis2.context.ServiceContext.
     *                    Also "defaults" is a special type, which will apply to all contexts
     * @param patterns    The patterns
     */
    void setReplicationExcludePatterns(String contextType, List patterns);

    /**
     * Get all the excluded context property name patterns
     *
     * @return All the excluded pattern of all the contexts. The key of the Map is the
     *         the <code>contextType</code>. See {@link #setReplicationExcludePatterns(String,List)}.
     *         The values are of type {@link List} of {@link String} Objects,
     *         which are a collection of patterns to be excluded.
     * @see #setReplicationExcludePatterns(String, java.util.List)
     */
    Map getReplicationExcludePatterns();
}
