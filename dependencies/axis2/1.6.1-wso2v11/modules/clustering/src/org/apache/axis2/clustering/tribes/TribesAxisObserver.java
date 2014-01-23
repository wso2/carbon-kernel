/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.axis2.clustering.tribes;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;

import java.util.ArrayList;

/**
 * AxisObserver which specifically handles setting of service & module classloaders for
 * message deserialization by Tribes
 */
public class TribesAxisObserver implements AxisObserver {
    public void init(AxisConfiguration axisConfiguration) {
        //Nothing to do
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        //Nothing to do
    }

    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {
        if (axisEvent.getEventType() == AxisEvent.SERVICE_DEPLOY) {
            ClassLoaderUtil.addServiceGroupClassLoader(axisServiceGroup);
        } else if (axisEvent.getEventType() == AxisEvent.SERVICE_REMOVE) {
            ClassLoaderUtil.removeServiceGroupClassLoader(axisServiceGroup);
        }
    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {
        if (axisEvent.getEventType() == AxisEvent.MODULE_DEPLOY) {
            ClassLoaderUtil.addModuleClassLoader(axisModule);
        } else if (axisEvent.getEventType() == AxisEvent.MODULE_DEPLOY) {
            ClassLoaderUtil.removeModuleClassLoader(axisModule);
        }
    }

    public void addParameter(Parameter parameter) throws AxisFault {
        //Nothing to do
    }

    public void removeParameter(Parameter parameter) throws AxisFault {
        //Nothing to do
    }

    public void deserializeParameters(OMElement omElement) throws AxisFault {
        //Nothing to do
    }

    public Parameter getParameter(String carbonHome) {
        return null;          //Nothing to do
    }

    public ArrayList<Parameter> getParameters() {
        return null;          //Nothing to do
    }

    public boolean isParameterLocked(String carbonHome) {
        return false;         //Nothing to do
    }
}
