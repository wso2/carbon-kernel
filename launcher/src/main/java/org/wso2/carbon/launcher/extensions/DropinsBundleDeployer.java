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
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DropinsBundleDeployer deploys the OSGi bundles in CARBON_HOME/osgi/dropins folder by writing
 * the OSGi bundle information to the bundles.info file.
 *
 * @since 5.1.0
 */
public class DropinsBundleDeployer implements CarbonServerListener {
    private static final Logger logger = BootstrapLogger.getCarbonLogger(DropinsBundleDeployer.class.getName());

    @Override
    public void notify(CarbonServerEvent event) {
        if (event.getType() == CarbonServerEvent.STARTING) {
            Path defaultProfileBundlesInfo = Paths.
                    get(Utils.getCarbonHomeDirectory().toString(), "osgi", Constants.DEFAULT_PROFILE,
                            "configuration", "org.eclipse.equinox.simpleconfigurator", "bundles.info");
            try {
                BundleDeployerUtils.executeDropinsCapability(defaultProfileBundlesInfo);
            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "An error has occurred when updating the bundles.info using the OSGi bundle information", e);
            }
        }
    }
}
