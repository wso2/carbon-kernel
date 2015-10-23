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
package org.wso2.carbon.launcher.bootstrap.logging;

import java.util.logging.Logger;

/**
 * Logger class correspond to bootstrap logging.
 */
public class BootstrapLogger extends Logger {
    private static Logger bootstrapLogger = new BootstrapLogger(null, null);

    /**
     * Protected method to construct a logger for a named subsystem.
     * The logger will be initially configured with a null Level
     * and with useParentHandlers true.
     *
     * @param resourceBundleName name of ResourceBundle to be used for localizing
     *                           messages for this logger.  May be null if none
     *                           of the messages require localization.
     * @param name               A name for the logger.  This should
     *                           be a dot-separated name and should normally
     *                           be based on the package name or class name
     *                           of the subsystem, such as java.net
     *                           or javax.swing.  It may be null for anonymous Loggers.
     * @throws java.util.MissingResourceException if the ResourceBundleName is non-null and
     *                                            no corresponding resource can be found.
     */
    protected BootstrapLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    public static Logger getBootstrapLogger() {
        return bootstrapLogger;
    }
}
