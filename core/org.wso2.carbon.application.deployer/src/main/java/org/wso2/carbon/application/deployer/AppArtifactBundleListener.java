/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.application.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleListener;
import org.wso2.carbon.application.deployer.internal.ApplicationManager;

public class AppArtifactBundleListener implements BundleListener {

    private static Log log = LogFactory.getLog(AppArtifactBundleListener.class);

    private static AppArtifactBundleListener instance;

    private ApplicationManager appManager;

    private AppArtifactBundleListener(ApplicationManager appManager) {
        this.appManager = appManager;
    }

    public static AppArtifactBundleListener getInstance(ApplicationManager appManager) {
        if (instance == null) {
            instance = new AppArtifactBundleListener(appManager);
        }
        return instance;
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        try {
            Bundle currentBundle = bundleEvent.getBundle();
            String parentAppName = AppDeployerUtils.getParentAppName(currentBundle);
            if (bundleEvent.getType() == BundleEvent.RESOLVED && parentAppName != null) {
//                appManager.deployAppArtifact(AppDeployerUtils
//                        .getArchivePathFromBundle(currentBundle), parentAppName);
            }
        } catch (Exception e) {
            log.error("Couldn't deploy Carbon App artifact properly", e);
        }
    }

}
