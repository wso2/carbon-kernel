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
package org.wso2.carbon.launcher.config;

import java.net.URL;

/**
 * Class for initial bundle that loads while starting the server.
 *
 * @since 5.0.0
 */
public class CarbonInitialBundle {
    public final URL location;
    public final int level;
    public final boolean start;

    /**
     * Initialize bundle.
     *
     * @param location URL for the bundle
     * @param level    bundle start level
     * @param start    true if bundle need to start, false otherwise
     */
    CarbonInitialBundle(URL location, int level, boolean start) {
        this.location = location;
        this.level = level;
        this.start = start;
    }

    /**
     * Get Bundle location.
     *
     * @return location URL
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Get bundle start level.
     *
     * @return bundle start level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @return true if bundle should start, false otherwise
     */
    public boolean shouldStart() {
        return start;
    }
}
