/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.utils.deployment.service.processors;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.utils.deployment.service.Axis2ConfigParameterProvider;

import java.util.Iterator;
import java.util.Map;

public class Axis2ConfigParameterProcessor extends ConfigurationServiceProcessor {

    public Axis2ConfigParameterProcessor(AxisConfiguration axisConfig, BundleContext bundleContext) {
        super(axisConfig, bundleContext);
    }

    public void processConfigurationService(ServiceReference sr, int action) throws AxisFault {
        Axis2ConfigParameterProvider parameterService = (Axis2ConfigParameterProvider) bundleContext.getService(sr);
        lock.lock();
        try {
            Map<String, Object> paramMap = parameterService.getAxis2ConfigParameterMap();
            for(Iterator<Map.Entry<String,Object>> paremItr = paramMap.entrySet().iterator(); paremItr.hasNext();){
                Map.Entry<String, Object> paramEntry = paremItr.next();
                axisConfig.addParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        } finally {
            lock.unlock();
        }
    }
}