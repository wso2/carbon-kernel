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
package org.wso2.carbon.core.multitenancy.transports;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;

/**
 * Tenant transport IN description
 */
public class TenantTransportInDescription extends TransportInDescription {
    private TransportInDescription mainTransportInDescription;

    public void setMainTransportInDescription(TransportInDescription mainTransportInDescription) {
        this.mainTransportInDescription = mainTransportInDescription;
    }

    public TenantTransportInDescription(String name) {
        super(name);
    }

    @Override
    public Parameter getParameter(String name) {
        return mainTransportInDescription.getParameter(name);
    }
}
