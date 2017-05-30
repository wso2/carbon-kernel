/*
 * Copyright 2014 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.feature.mgt.services.prov.data;

public class LicenseFeatureHolder {
    private LicenseInfo licenseInfo;
    private FeatureInfo[] featureInfo;

    public LicenseInfo getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(LicenseInfo licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public FeatureInfo[] getFeatureInfo() {
        return featureInfo;
    }

    public void setFeatureInfo(FeatureInfo[] featureInfo) {
        this.featureInfo = featureInfo;
    }
}
