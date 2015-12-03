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
package org.wso2.carbon.launcher.extensions;

/**
 * A Java class which defines an instance holding an OSGi bundle's information.
 *
 * @since 5.0.0
 */
public class BundleInfo {
    private String bundleSymbolicName;
    private String bundleVersion;
    private String bundlePath;
    private int startLevel;
    private boolean isFragment;
    private boolean isFromDropins;

    public BundleInfo(String bundleSymbolicName, String bundleVersion, String bundlePath, int startLevel,
            boolean isFragment) {
        this.bundleSymbolicName = bundleSymbolicName;
        this.bundleVersion = bundleVersion;
        this.bundlePath = bundlePath;
        this.startLevel = startLevel;
        this.isFragment = isFragment;
        this.isFromDropins = bundlePath.contains("dropins/");
    }

    public String getBundleSymbolicName() {
        return bundleSymbolicName;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public String getBundlePath() {
        return bundlePath;
    }

    public boolean isFragment() {
        return isFragment;
    }

    public boolean isFromDropins() {
        return isFromDropins;
    }

    public static BundleInfo getInstance(String bundleInfoLineStr) throws Exception {
        String[] parts = bundleInfoLineStr.split(",");
        if (parts.length != 5) {
            throw new RuntimeException("Invalid line in the bundles.info file: " + bundleInfoLineStr);
        }

        return new BundleInfo(parts[0].trim(), parts[1].trim(), parts[2].trim(), Integer.parseInt(parts[3].trim()),
                !Boolean.parseBoolean(parts[4].trim()));
    }

    public String toString() {
        return bundleSymbolicName + "," + bundleVersion + "," +
                bundlePath + "," + startLevel + "," + Boolean.toString(!isFragment);
    }
}
