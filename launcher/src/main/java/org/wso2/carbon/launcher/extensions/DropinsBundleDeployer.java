/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.launcher.extensions;

import org.wso2.carbon.launcher.CarbonServerEvent;
import org.wso2.carbon.launcher.CarbonServerListener;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.model.BundleInfo;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class deploys the OSGi bundles in CARBON_HOME/osgi/dropins directory in the Carbon Server.
 * <p>
 * For this purpose, the OSGi bundle information retrieved from the dropins directory bundles are updated in
 * the bundles.info file of each and every, existing Carbon profile, along with the bundle startup information
 * of each bundle.
 *
 * @since 5.1.0
 */
public class DropinsBundleDeployer implements CarbonServerListener {
    private static final Logger logger = Logger.getLogger(DropinsBundleDeployer.class.getName());

    @Override
    public void notify(CarbonServerEvent event) {
        if (event.getType() == CarbonServerEvent.STARTING) {
            Path carbonHome = Utils.getCarbonHomeDirectory();
            Path dropinsDirectoryPath = Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, Constants.DROPINS);
            String profile = Optional.ofNullable(System.getProperty(Constants.PROFILE))
                    .orElse(Constants.DEFAULT_PROFILE);

            try {
                logger.log(Level.FINE,
                        "Loading the new OSGi bundle information from " + Constants.DROPINS + " folder...");
                List<BundleInfo> newBundlesInfo = DropinsBundleDeployerUtils.getBundlesInfo(dropinsDirectoryPath);
                logger.log(Level.FINE, "Successfully loaded the new OSGi bundle information from " + Constants.DROPINS +
                        " folder");

                DropinsBundleDeployerUtils.installDropins(carbonHome.toString(), profile, newBundlesInfo);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to update the OSGi bundle information of Carbon Profile: " + profile,
                        e);
            }
        }
    }
}
