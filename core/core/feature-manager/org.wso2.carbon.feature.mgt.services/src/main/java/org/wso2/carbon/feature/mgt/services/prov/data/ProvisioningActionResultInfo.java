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

public class ProvisioningActionResultInfo {
    private String provActionID;
    private String detailedDescription;
    private String summary;
    private FeatureInfo[] reviewedInstallableFeatures = new FeatureInfo[]{};
    private FeatureInfo[] reviewedUninstallableFeatures = new FeatureInfo[]{};
    private FeatureInfo[] failedinstallableFeatures = new FeatureInfo[]{};
    private FeatureInfo[] failedUninstallableFeatures = new FeatureInfo[]{};

    private boolean proceedWithInstallation;
    private String size;

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public boolean isProceedWithInstallation() {
        return proceedWithInstallation;
    }

    public void setProceedWithInstallation(boolean proceedWithInstallation) {
        this.proceedWithInstallation = proceedWithInstallation;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getProvActionID() {
        return provActionID;
    }

    public void setProvActionID(String provActionID) {
        this.provActionID = provActionID;
    }

    public FeatureInfo[] getReviewedInstallableFeatures() {
        return Arrays.copyOf(reviewedInstallableFeatures, reviewedInstallableFeatures.length);
    }

    public void setReviewedInstallableFeatures(FeatureInfo[] reviewedInstallableFeatures) {
        this.reviewedInstallableFeatures = Arrays.copyOf(reviewedInstallableFeatures,
                                                         reviewedInstallableFeatures.length);
    }

    public FeatureInfo[] getReviewedUninstallableFeatures() {
        return Arrays.copyOf(reviewedUninstallableFeatures, reviewedUninstallableFeatures.length);
    }

    public void setReviewedUninstallableFeatures(FeatureInfo[] reviewedUninstallableFeatures) {
        this.reviewedUninstallableFeatures = Arrays.copyOf(reviewedUninstallableFeatures,
                                                           reviewedUninstallableFeatures.length);
    }

    public FeatureInfo[] getFailedinstallableFeatures() {
        return Arrays.copyOf(failedinstallableFeatures, failedinstallableFeatures.length);
    }

    public void setFailedinstallableFeatures(FeatureInfo[] failedinstallableFeatures) {
        this.failedinstallableFeatures = Arrays.copyOf(failedinstallableFeatures,
                                                       failedinstallableFeatures.length);
    }

    public FeatureInfo[] getFailedUninstallableFeatures() {
        return Arrays.copyOf(failedUninstallableFeatures,
                             failedUninstallableFeatures.length);
    }

    public void setFailedUninstallableFeatures(FeatureInfo[] failedUninstallableFeatures) {
        this.failedUninstallableFeatures = Arrays.copyOf(failedUninstallableFeatures,
                                                         failedUninstallableFeatures.length);
    }
}
