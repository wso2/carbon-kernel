/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.core.session;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.Manager;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;

/**
 * The class extends tomcat's SimpleTcpCluster. The main use of this class is to have our
 * implementation of DeltaManger(ClusterManager) to be used when standardContext class creates
 * managers for contexts
 */
public class CarbonTomcatSimpleTcpCluster extends SimpleTcpCluster{
    protected ClusterManager managerTemplate = new CarbonTomcatClusterableSessionManager();

    @Override
    public void setManagerTemplate(ClusterManager managerTemplate) {
        this.managerTemplate = managerTemplate;
    }

    @Override
    public ClusterManager getManagerTemplate() {
        return managerTemplate;
    }
    /**
     * Create new Manager without add to cluster (comes with start the manager)
     *
     * @param name
     *            Context Name of this manager
     * @see org.apache.catalina.Cluster#createManager(java.lang.String)
     * @see org.apache.catalina.ha.session.DeltaManager#start()
     */
    @Override
    public synchronized Manager createManager(String name) {
        if (log.isDebugEnabled()) {
            log.debug("Creating ClusterManager for context " + name +
                    " using class " + getManagerTemplate().getClass().getName());
        }
        Manager manager = null;
        try {
            manager = managerTemplate.cloneFromTemplate();
            ((ClusterManager)manager).setName(name);

            // TODO The following is the correct place to set sessionReplication is enabled, but CC is not yet ready
            /*ConfigurationContext configurationContext = CarbonCoreDataHolder.getInstance().getMainServerConfigContext();
            if (configurationContext != null) {
                configurationContext.setProperty(SessionConstants.SESSION_REPLICATION_INITIALIZED, true);
            }*/
        } catch (Exception x) {
            log.error("Unable to clone cluster manager, " +
                      "defaulting to org.apache.catalina.ha.session.DeltaManager", x);
            manager = new org.apache.catalina.ha.session.DeltaManager();
        } finally {
            if ( manager != null && (manager instanceof ClusterManager))
                ((ClusterManager)manager).setCluster(this);
        }
        return manager;
    }
}
