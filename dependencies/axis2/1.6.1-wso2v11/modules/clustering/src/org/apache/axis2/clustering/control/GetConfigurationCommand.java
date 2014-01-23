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

package org.apache.axis2.clustering.control;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class GetConfigurationCommand extends ControlCommand {

    private String[] serviceGroupNames;

    public void execute(ConfigurationContext configCtx) throws ClusteringFault {

        List serviceGroupNames = new ArrayList();
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        for (Iterator iter = axisConfig.getServiceGroups(); iter.hasNext();) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup) iter.next();
            boolean excludeSG = false;
            for (Iterator serviceIter = serviceGroup.getServices(); serviceIter.hasNext();) {
                AxisService service = (AxisService) serviceIter.next();
                if (service.getParameter(AxisModule.MODULE_SERVICE) != null ||
                    service.isClientSide()) { // No need to send services deployed through modules or client side services
                    excludeSG = true;
                    break;
                }
            }

            //TODO: Exclude all services loaded from modules. How to handle data services etc.?
            if (!excludeSG) {
                serviceGroupNames.add(serviceGroup.getServiceGroupName());
            }
        }
        this.serviceGroupNames =
                (String[]) serviceGroupNames.toArray(new String[serviceGroupNames.size()]);
    }

    public String[] getServiceGroupNames() {
        return serviceGroupNames;
    }

    public String toString() {
        return "GetConfigurationCommand";
    }
}
