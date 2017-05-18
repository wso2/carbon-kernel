/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.launcher;

/**
 * Carbon constants.
 *
 * @since 5.0.0
 */
public class Constants {
    public static final String LAUNCH_PROPERTIES_FILE = "launch.properties";

    public static final String CARBON_HOME = "carbon.home";
    public static final String RUNTIME_PATH = "wso2.runtime.path";
    static final String CARBON_START_TIME = "carbon.start.time";

    public static final String OSGI_REPOSITORY = "wso2/lib";
    public static final String LAUNCH_CONF_DIRECTORY = "conf/osgi";
    public static final String PROFILE_REPOSITORY = "wso2";
    public static final String PROFILE = "wso2.runtime";
    public static final String DEFAULT_PROFILE = "default";
    public static final String OSGI_LIB = "lib";
    public static final String PLUGINS = "plugins";
    public static final String BUNDLES_INFO = "bundles.info";

    public static final String CARBON_OSGI_REPOSITORY = "carbon.osgi.repository";
    public static final String CARBON_PROFILE_REPOSITORY = "carbon.runtime.repository";
    public static final String CARBON_OSGI_FRAMEWORK = "carbon.osgi.framework";
    public static final String CARBON_INITIAL_OSGI_BUNDLES = "carbon.initial.osgi.bundles";
    public static final String CARBON_SERVER_LISTENERS = "carbon.server.listeners";

    public static final String OSGI_INSTALL_AREA = "osgi.install.area";
    public static final String OSGI_CONFIG_AREA = "osgi.configuration.area";
    public static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
    public static final String ECLIPSE_P2_DATA_AREA = "eclipse.p2.data.area";

    public static final String PAX_LOGGING_PROPERTY_FILE_KEY = "org.ops4j.pax.logging.property.file";
    public static final String PAX_LOGGING_PROPERTIES_FILE = "pax-logging.properties";
    public static final String PAX_DEFAULT_SERVICE_LOG_LEVEL = "org.ops4j.pax.logging.DefaultServiceLog.level";
    static final String PAX_LOG_SERVICE_RANKING_LEVEL = "org.ops4j.pax.logging.ranking";

    static final String EQUINOX_SIMPLE_CONFIGURATOR_EXCLUSIVE_INSTALLATION =
            "org.eclipse.equinox.simpleconfigurator.exclusiveInstallation";

    static final String START_TIME = "carbon.start.time";

    //  Constants relevant to log level.
    public static final String LOG_LEVEL_WARN = "WARN";

    public static final String CARBON_LOG_FILE_NAME = "carbon.log";

    public static final String OS_NAME = "os.name";
    public static final String WINDOWS = "windows";

    /**
     * Prevent instantiating the Constants class.
     */
    private Constants() {
    }

    /**
     * Carbon server process exit codes.
     */
    static class ExitCodes {
        static final int SUCCESSFUL_TERMINATION = 0;
        static final int UNSUCCESSFUL_TERMINATION = -1;
        static final int RESTART_ACTION = 121;
    }
}
