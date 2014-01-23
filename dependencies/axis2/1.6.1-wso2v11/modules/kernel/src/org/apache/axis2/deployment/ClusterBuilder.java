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


package org.apache.axis2.deployment;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.clustering.management.NodeManager;
import org.apache.axis2.clustering.state.StateManager;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Builds the cluster configuration from the axis2.xml file
 */
public class ClusterBuilder extends DescriptionBuilder {

    private static final Log log = LogFactory.getLog(ClusterBuilder.class);

    public ClusterBuilder(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    /**
     * Build the cluster configuration
     *
     * @param clusterElement Cluster element
     * @throws DeploymentException If an error occurs while building the cluster configuration
     */
    public void buildCluster(OMElement clusterElement) throws DeploymentException {

        if (!isEnabled(clusterElement)) {
            log.info("Clustering has been disabled");
            return;
        }
        log.info("Clustering has been enabled");

        OMAttribute classNameAttr = clusterElement.getAttribute(new QName(TAG_CLASS_NAME));
        if (classNameAttr == null) {
            throw new DeploymentException(Messages.getMessage("classAttributeNotFound",
                                                              TAG_CLUSTER));
        }

        String className = classNameAttr.getAttributeValue();
        ClusteringAgent clusteringAgent;
        try {
            Class clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                  className));
            }
            clusteringAgent = (ClusteringAgent) clazz.newInstance();

            clusteringAgent.setConfigurationContext(configCtx);

            //loading the parameters.
            processParameters(clusterElement.getChildrenWithName(new QName(TAG_PARAMETER)),
                              clusteringAgent,
                              null);

            // loading the application domains
            loadGroupManagement(clusteringAgent, clusterElement);

            // loading the members
            loadWellKnownMembers(clusteringAgent, clusterElement);

            //loading the NodeManager
            loadNodeManager(clusterElement, clusteringAgent);

            // loading the StateManager
            loadStateManager(clusterElement, clusteringAgent);

            axisConfig.setClusteringAgent(clusteringAgent);
        } catch (InstantiationException e) {
            throw new DeploymentException(Messages.getMessage("cannotLoadClusterImpl"));
        } catch (IllegalAccessException e) {
            throw new DeploymentException(e);
        }
    }

    private boolean isEnabled(OMElement element) {
        boolean enabled = true;
        OMAttribute enableAttr = element.getAttribute(new QName("enable"));
        if (enableAttr != null) {
            enabled = Boolean.parseBoolean(enableAttr.getAttributeValue().trim());
        }
        return enabled;
    }

    private void loadGroupManagement(ClusteringAgent clusteringAgent,
                                     OMElement clusterElement) throws DeploymentException {
        OMElement lbEle = clusterElement.getFirstChildWithName(new QName("groupManagement"));
        if (lbEle != null) {
            if (isEnabled(lbEle)) {
                log.info("Running in group management mode");
            } else {
                log.info("Running in application mode");
                return;
            }

            for (Iterator iter = lbEle.getChildrenWithName(new QName("applicationDomain"));
                 iter.hasNext();) {
                OMElement omElement = (OMElement) iter.next();
                String domainName = omElement.getAttributeValue(new QName("domain"));
                if (domainName != null) {
                    domainName = domainName.trim();
                }
                String name = omElement.getAttributeValue(new QName("name"));
                if (name != null) {
                    domainName = name.trim();
                }
                String subDomainName = omElement.getAttributeValue(new QName("subDomain"));
                if (subDomainName != null) {
                    subDomainName = subDomainName.trim();
                }
                String handlerClass = omElement.getAttributeValue(new QName("agent")).trim();
                String descAttrib = omElement.getAttributeValue(new QName("description"));
                String description = "Description not found";
                if (descAttrib != null) {
                    description = descAttrib.trim();
                }
                int groupMgtPort = -1;
                String groupMgtPortAttrib = omElement.getAttributeValue(new QName("port"));
                if (groupMgtPortAttrib != null) {
                    groupMgtPort = Integer.parseInt(groupMgtPortAttrib.trim());
                }
                GroupManagementAgent groupManagementAgent;
                try {
                    groupManagementAgent = (GroupManagementAgent) Class.forName(handlerClass).newInstance();
                    groupManagementAgent.setDescription(description);
                    groupManagementAgent.setDomain(domainName);
                    groupManagementAgent.setSubDomain(subDomainName);
                    if (groupMgtPort != -1) {
                        groupManagementAgent.setGroupMgtPort(groupMgtPort);
                    }
                } catch (Exception e) {
                    String msg = "Could not instantiate GroupManagementAgent " + handlerClass +
                                 " for domain " + domainName;
                    log.error(msg, e);
                    throw new DeploymentException(msg, e);
                }
                clusteringAgent.addGroupManagementAgent(groupManagementAgent,
                                                        domainName, subDomainName, groupMgtPort);
            }
        }
    }

    private void loadWellKnownMembers(ClusteringAgent clusteringAgent, OMElement clusterElement) {
        clusteringAgent.setMembers(new ArrayList<Member>());
        Parameter membershipSchemeParam = clusteringAgent.getParameter("membershipScheme");
        if (membershipSchemeParam != null) {
            String membershipScheme = ((String) membershipSchemeParam.getValue()).trim();
            if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
                List<Member> members = new ArrayList<Member>();
                OMElement membersEle =
                        clusterElement.getFirstChildWithName(new QName("members"));
                if (membersEle != null) {
                    for (Iterator iter = membersEle.getChildrenWithLocalName("member"); iter.hasNext();) {
                        OMElement memberEle = (OMElement) iter.next();
                        String hostName =
                                memberEle.getFirstChildWithName(new QName("hostName")).getText().trim();
                        String port =
                                memberEle.getFirstChildWithName(new QName("port")).getText().trim();
                        members.add(new Member(replaceVariables(hostName),
                                               Integer.parseInt(replaceVariables(port))));
                    }
                }
                clusteringAgent.setMembers(members);
            }
        }
    }

    private String replaceVariables(String text) {
        int indexOfStartingChars;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        if ((indexOfStartingChars = text.indexOf("${")) != -1 &&
            (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String var = text.substring(indexOfStartingChars + 2,
                                        indexOfClosingBrace);

            String propValue = System.getProperty(var);
            if (propValue == null) {
                propValue = System.getenv(var);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }

    private void loadStateManager(OMElement clusterElement,
                                    ClusteringAgent clusteringAgent) throws DeploymentException,
                                                                          InstantiationException,
                                                                          IllegalAccessException {
        OMElement contextManagerEle =
                clusterElement.getFirstChildWithName(new QName(TAG_STATE_MANAGER));
        if (contextManagerEle != null) {
            if (!isEnabled(contextManagerEle)) {
                log.info("Clustering state management has been disabled");
                return;
            }
            log.info("Clustering state management has been enabled");

            // Load & set the StateManager class
            OMAttribute classNameAttr =
                    contextManagerEle.getAttribute(new QName(ATTRIBUTE_CLASS));
            if (classNameAttr == null) {
                throw new DeploymentException(Messages.getMessage("classAttributeNotFound",
                                                                  TAG_STATE_MANAGER));
            }

            String className = classNameAttr.getAttributeValue();

            Class clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                  className));
            }
            StateManager stateManager = (StateManager) clazz.newInstance();
            clusteringAgent.setStateManager(stateManager);

            //loading the parameters.
            processParameters(contextManagerEle.getChildrenWithName(new QName(TAG_PARAMETER)),
                              stateManager,
                              null);

            // Load the replication patterns to be excluded. We load the following structure.
            /*<replication>
                <defaults>
                    <exclude name="foo.bar.*"/>
                </defaults>
                <context class="org.apache.axis2.context.ConfigurationContext">
                    <exclude name="my.sandesha.*"/>
                </context>
                <context class="org.apache.axis2.context.ServiceGroupContext">
                    <exclude name="my.sandesha.*"/>
                </context>
                <context class="org.apache.axis2.context.ServiceContext">
                    <exclude name="my.sandesha.*"/>
                </context>
            </replication>*/
            OMElement replicationEle =
                    contextManagerEle.getFirstChildWithName(new QName(TAG_REPLICATION));
            if (replicationEle != null) {
                // Process defaults
                OMElement defaultsEle =
                        replicationEle.getFirstChildWithName(new QName(TAG_DEFAULTS));
                if (defaultsEle != null) {
                    List<String> defaults = new ArrayList<String>();
                    for (Iterator iter = defaultsEle.getChildrenWithName(new QName(TAG_EXCLUDE));
                         iter.hasNext();) {
                        OMElement excludeEle = (OMElement) iter.next();
                        OMAttribute nameAtt = excludeEle.getAttribute(new QName(ATTRIBUTE_NAME));
                        defaults.add(nameAtt.getAttributeValue());
                    }
                    stateManager.setReplicationExcludePatterns(TAG_DEFAULTS, defaults);
                }

                // Process specifics
                for (Iterator iter = replicationEle.getChildrenWithName(new QName(TAG_CONTEXT));
                     iter.hasNext();) {
                    OMElement contextEle = (OMElement) iter.next();
                    String ctxClassName =
                            contextEle.getAttribute(new QName(ATTRIBUTE_CLASS)).getAttributeValue();
                    List<String> excludes = new ArrayList<String>();
                    for (Iterator iter2 = contextEle.getChildrenWithName(new QName(TAG_EXCLUDE));
                         iter2.hasNext();) {
                        OMElement excludeEle = (OMElement) iter2.next();
                        OMAttribute nameAtt = excludeEle.getAttribute(new QName(ATTRIBUTE_NAME));
                        excludes.add(nameAtt.getAttributeValue());
                    }
                    stateManager.setReplicationExcludePatterns(ctxClassName, excludes);
                }
            }
        }
    }

    private void loadNodeManager(OMElement clusterElement,
                                   ClusteringAgent clusteringAgent) throws DeploymentException,
                                                                         InstantiationException,
                                                                         IllegalAccessException {
        OMElement configManagerEle =
                clusterElement.getFirstChildWithName(new QName(TAG_NODE_MANAGER));
        if (configManagerEle != null) {
            if (!isEnabled(configManagerEle)) {
                log.info("Clustering configuration management has been disabled");
                return;
            }
            log.info("Clustering configuration management has been enabled");

            OMAttribute classNameAttr = configManagerEle.getAttribute(new QName(ATTRIBUTE_CLASS));
            if (classNameAttr == null) {
                throw new DeploymentException(Messages.getMessage("classAttributeNotFound",
                                                                  TAG_NODE_MANAGER));
            }

            String className = classNameAttr.getAttributeValue();
            Class clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                  className));
            }

            NodeManager nodeManager = (NodeManager) clazz.newInstance();
            clusteringAgent.setNodeManager(nodeManager);

            //updating the NodeManager with the new ConfigurationContext
            nodeManager.setConfigurationContext(configCtx);

            //loading the parameters.
            processParameters(configManagerEle.getChildrenWithName(new QName(TAG_PARAMETER)),
                              nodeManager,
                              null);
        }
    }
}
