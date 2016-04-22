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
package org.wso2.carbon.tools;

/**
 * A Java class which defines tool specific constants.
 *
 * @since 5.0.0
 */
public class Constants {
    //  Carbon tool constants
    public static final String CARBON_TOOL_SYSTEM_PROPERTY = "wso2.carbon.tool";

    //  OSGi Bundle manifest constants
    public static final String MANIFEST_VERSION = "Manifest-Version";
    public static final String BUNDLE_MANIFEST_VERSION = "Bundle-ManifestVersion";
    public static final String BUNDLE_NAME = "Bundle-Name";
    public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    public static final String BUNDLE_VERSION = "Bundle-Version";
    public static final String EXPORT_PACKAGE = "Export-Package";
    public static final String BUNDLE_CLASSPATH = "Bundle-ClassPath";
    public static final String DYNAMIC_IMPORT_PACKAGE = "DynamicImport-Package";

    //  file path name and extension constants
    public static final String JAR_TO_BUNDLE_TEMP_DIRECTORY_NAME = "temp";
    public static final String JAR_MANIFEST_FOLDER = "META-INF";
    public static final String MANIFEST_FILE_NAME = "MANIFEST.MF";
    public static final String P2_INF_FILE_NAME = "p2";
    public static final String P2_INF_FILE_EXTENSION = ".inf";
    public static final String JAR_FILE_EXTENSION = ".jar";
    public static final String ZIP_FILE_EXTENSION = ".zip";

    //  create zip file system properties
    public static final String CREATE_NEW_ZIP_FILE_PROPERTY = "create";
    public static final String ENCODING_TYPE_PROPERTY = "encoding";

    /**
     * Prevents instantiating this class.
     */
    private Constants() {
    }
}
