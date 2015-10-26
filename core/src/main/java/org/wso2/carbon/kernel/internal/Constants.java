/*
 * Copyright 2015 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.kernel.internal;

/**
 * Carbon internal Constants.
 *
 * @since 5.0.0
 */
public final class Constants {

    //properties relevant to pax logging configuration
    public static final String LOG4J2_CONFIG_FILE_KEY = "org.ops4j.pax.logging.log4j2.config.file";
    public static final String LOG4J2_CONFIG_FILE_NAME = "log4j2.xml";
    public static final String LOGGING_CONFIG_PID = "org.ops4j.pax.logging";
    public static final String SERVICE_PID = "service.pid";

    /**
     * Remove default constructor and make it not available to initialize.
     */
    private Constants() {
        throw new AssertionError("Trying to a instantiate a constant class");
    }
}
