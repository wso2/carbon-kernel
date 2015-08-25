/*
 * Copyright 2005,2013 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.base;

/**
 * Carbon base Constants
 */
public class Constants {
    public static final String CARBON_REPOSITORY = "carbon.repository";
    public static final String CARBON_REPOSITORY_PATH_ENV = "CARBON_REPOSITORY";
    public static final String CARBON_HOME = "carbon.home";
    public static final String CARBON_HOME_ENV = "CARBON_HOME";

    //    properties relavant to pax logging configuration
    public static final String CONFIG_FILE_NAME = "log4j.properties";
    public static final String LOGGING_CONFIG_PID = "org.ops4j.pax.logging";

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private Constants() {
		throw new AssertionError("Instantiating utility class...");
	}

}
