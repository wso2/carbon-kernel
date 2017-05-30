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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.wso2.carbon.application.deployer.internal.ApplicationManager;

/**
 * This class implements BundleListener and listens for new bundle additions. If app artifacts
 * are found, deploy them 
 */
public class AppDeployerBundleListener implements BundleListener {

    private static Log log = LogFactory.getLog(AppDeployerBundleListener.class);

    private static AppDeployerBundleListener instance = null;

    private ApplicationManager appManager;

    private AppDeployerBundleListener(ApplicationManager appManager) {
        this.appManager = appManager;
    }

    public static AppDeployerBundleListener getInstance(ApplicationManager appManager) {
        if (instance == null) {
            instance = new AppDeployerBundleListener(appManager);
        }
        return instance;
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        try {
            Bundle currentBundle = bundleEvent.getBundle();
            String appName = AppDeployerUtils.getProjectArtifactName(currentBundle);
            if (bundleEvent.getType() == BundleEvent.RESOLVED && appName != null) {
//                appManager.deployCarbonApp(AppDeployerUtils
//                        .getArchivePathFromBundle(currentBundle));
            }
        } catch (Exception e) {
            log.error("Couldn't deploy Carbon App properly", e);
        }
    }

}
