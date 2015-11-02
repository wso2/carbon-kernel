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
package org.wso2.carbon.tools.utils;

/**
 * A Java class which defines tool specific constants.
 *
 * @since 5.0.0
 */
public class Constants {

    //  OSGi Bundle manifest constants
    protected static final String MANIFEST_VERSION = "Manifest-Version";
    protected static final String BUNDLE_MANIFEST_VERSION = "Bundle-ManifestVersion";
    protected static final String BUNDLE_NAME = "Bundle-Name";
    protected static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    protected static final String BUNDLE_VERSION = "Bundle-Version";
    protected static final String EXPORT_PACKAGE = "Export-Package";
    protected static final String BUNDLE_CLASSPATH = "Bundle-ClassPath";
    protected static final String DYNAMIC_IMPORT_PACKAGE = "DynamicImport-Package";

    //  File path constants
    protected static final String JAR_TO_BUNDLE_TEMP_DIRECTORY_NAME = "temp";
    protected static final String MANIFEST_FILE_NAME = "MANIFEST.MF";
    protected static final String P2_INF_FILE_NAME = "p2.inf";

    /**
     * A constructor which prevents instantiating the Constants class.
     */
    private Constants() {
    }

}
