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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.MessageSender;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultNodeManager implements NodeManager {
    private static final Log log = LogFactory.getLog(DefaultNodeManager.class);

    private MessageSender sender;
    private ConfigurationContext configurationContext;
    private final Map<String, Parameter> parameters = new HashMap<String, Parameter>();

    public DefaultNodeManager() {
    }

    public void commit() throws ClusteringFault {
    }

    public void exceptionOccurred(Throwable throwable) throws ClusteringFault {
    }

    public void prepare() throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: DefaultNodeManager::prepare");
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: DefaultNodeManager::prepare");
        }
    }
    public void rollback() throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: DefaultNodeManager::rollback");
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: DefaultNodeManager::rollback");
        }
    }

    protected void send(Throwable throwable) throws ClusteringFault {
//        sender.sendToGroup(new ExceptionCommand(throwable));
    }

    public void sendMessage(NodeManagementCommand command) throws ClusteringFault {
        sender.sendToGroup(command);
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void addParameter(Parameter param) throws AxisFault {
        parameters.put(param.getName(), param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList<Parameter> list = new ArrayList<Parameter>();
        for (Iterator iter = parameters.keySet().iterator(); iter.hasNext();) {
            list.add(parameters.get(iter.next()));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        return getParameter(parameterName).isLocked();
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        throw new UnsupportedOperationException();
    }
}
