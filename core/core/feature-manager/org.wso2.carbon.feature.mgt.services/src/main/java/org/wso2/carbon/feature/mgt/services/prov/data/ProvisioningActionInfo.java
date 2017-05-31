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
package org.wso2.carbon.feature.mgt.services.prov.data;

import java.util.Arrays;

public class ProvisioningActionInfo {

    private String actionType;
    private FeatureInfo[] featuresToInstall = new FeatureInfo[]{};
    private FeatureInfo[] featuresToUninstall = new FeatureInfo[]{};
    private long timestamp;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public FeatureInfo[] getFeaturesToInstall() {
        return Arrays.copyOf(featuresToInstall, featuresToInstall.length);
    }

    public void setFeaturesToInstall(FeatureInfo[] featuresToInstall) {
        this.featuresToInstall = Arrays.copyOf(featuresToInstall, featuresToInstall.length);
    }

    public FeatureInfo[] getFeaturesToUninstall() {
        return Arrays.copyOf(featuresToUninstall, featuresToUninstall.length);
    	
    }

    public void setFeaturesToUninstall(FeatureInfo[] featuresToUninstall) {
        this.featuresToUninstall = Arrays.copyOf(featuresToUninstall, featuresToUninstall.length);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
