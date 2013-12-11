/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.launcher.utils;

import java.io.File;

public class Constants {

    public static final String LAUNCH_PROPERTIES_FILE = "launch.properties";

    public static final String CARBON_HOME = "carbon.home";

    public static final String PROFILE = "profile";
    public static final String DEFAULT_PROFILE = "default";

    public static final String CARBON_OSGI_REPOSITORY = "carbon.osgi.repository";
    public static final String CARBON_OSGI_FRAMEWORK = "carbon.osgi.framework";
    public static final String CARBON_INITIAL_OSGI_BUNDLES = "carbon.initial.osgi.bundles";

    public static final String OSGI_INSTALL_AREA = "osgi.install.area";
    public static final String OSGI_CONFIG_AREA = "osgi.configuration.area";
    public static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
    public static final String ECLIPSE_P2_DATA_AREA = "eclipse.p2.data.area";

    public static final String REPOSITORY_CONF_DIR_PATH = "repository" + File.separator + "conf";

    public static final int DEFAULT_BUNDLE_START_LEVEL = 4;

    public static final String LOGGING_DEFAULT_SERVICE_NAME = "org.ops4j.pax.logging.DefaultServiceLog.level";
    public static final String PAX_LOGGING_LEVEL = "WARN";
    public static final String BUNDLE_CONFIG_LOCATION = "bundles.configuration.location";
}
