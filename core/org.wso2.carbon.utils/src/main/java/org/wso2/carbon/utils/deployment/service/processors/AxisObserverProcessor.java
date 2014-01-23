/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
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
import org.apache.axis2.engine.AxisObserver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *  Dynamically add deployers to the AxisConfiguration
 */
public class AxisObserverProcessor extends ConfigurationServiceProcessor {
    
    public AxisObserverProcessor(AxisConfiguration axisConfig, BundleContext bundleContext) {
        super(axisConfig, bundleContext);
    }

    public void processConfigurationService(ServiceReference sr, int action) throws AxisFault {
        ((AxisObserver) bundleContext.getService(sr)).init(axisConfig);
        axisConfig.addObservers((AxisObserver) bundleContext.getService(sr));
    }
}
