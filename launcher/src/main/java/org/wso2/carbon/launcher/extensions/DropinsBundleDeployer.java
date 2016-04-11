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
import org.wso2.carbon.launcher.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
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
            try {
                Optional<String> carbonProfile = Optional.ofNullable(System.getProperty("carbon.profile"));
                if (carbonProfile.isPresent()) {
                    DropinsBundleDeployerUtils.executeDropinsCapability(carbonHome.toString(), carbonProfile.get());
                } else {
                    List<String> profileNames = DropinsBundleDeployerUtils.getCarbonProfiles();
                    for (String profileName : profileNames) {
                        DropinsBundleDeployerUtils.executeDropinsCapability(carbonHome.toString(), profileName);
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "An error has occurred when updating the bundles.info using the OSGi bundle information", e);
            }
        }
    }
}
