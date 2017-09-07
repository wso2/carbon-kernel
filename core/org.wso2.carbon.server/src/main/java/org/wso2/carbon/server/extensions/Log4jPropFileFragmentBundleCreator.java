/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.server.extensions;

import org.wso2.carbon.server.LauncherConstants;
import org.wso2.carbon.server.util.Utils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @deprecated log4j2.properties is wired through pax-logging.properties
 */
@Deprecated
public class Log4jPropFileFragmentBundleCreator extends FragmentBundleCreator {
    private static String LOG4J_PROP_FILE_NAME = "log4j.properties";
    private static String FRAGMENT_BUNDLE_NAME = "org.wso2.carbon.logging.propfile";
    private static String FRAGMENT_HOST_BUNDLE_NAME = "org.wso2.carbon.logging";

    @Override
    protected String getFragmentBundleName(File dirName) {
        return FRAGMENT_BUNDLE_NAME;
    }

    @Override
    protected String getFragmentHostBundleName(File dirName) {
        return FRAGMENT_HOST_BUNDLE_NAME;
    }

    @Override
    protected File[] getBundleConfigs() {
        String confPath = System.getProperty(LauncherConstants.CARBON_CONFIG_DIR_PATH);
        File confFolder;
        if (confPath == null) {
            confFolder = new File(Utils.getCarbonComponentRepo(), Paths.get("..", "conf").toString());
        } else {
            confFolder = new File(confPath);
        }

        String loggingPropFilePath = confFolder.getAbsolutePath() + File.separator +
                LOG4J_PROP_FILE_NAME;
        Collection<File> fileList = new ArrayList<File>();
        fileList.add(new File(loggingPropFilePath));
        return fileList.toArray(new File[0]);
    }
}
