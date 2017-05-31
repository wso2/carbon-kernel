/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.utils;

/**
 * @deprecated use {@link org.wso2.carbon.utils.ServerConstants}
 */
public final class WSO2Constants {
	private WSO2Constants() {
		//disable external instantiation
	}

    /**
     * @deprecated use {@link ServerConstants#FILE_RESOURCE_MAP}
     */
    public static final String FILE_RESOURCE_MAP = ServerConstants.FILE_RESOURCE_MAP;

    /**
     * @deprecated use {@link ServerConstants#WORK_DIR}
     */
    public static final String WORK_DIR = ServerConstants.WORK_DIR;
    public static final String WSO2WSAS_INSTANCE = "WSO2 WSAS";
    public static final String WEB_RESOURCE_LOCATION = "web.location";
    public static final String GENERATED_PAGES = "wso2wsas.generated.pages";
    public static final String CONFIGURATION_CONTEXT = "CONFIGURATION_CONTEXT";

	public static final String HTTP_PORT = "wso2utils.http.port";
	public static final String HTTPS_PORT = "wso2utils.https.port";
    public static final String PROXY_PORT = "proxyPort";

    public static class ContextPaths {
        public static final String UPLOAD_PATH = "/fileupload";
        public static final String DOWNLOAD_PATH = "/filedownload";
        public static final String SERVER_PATH = "/server";
    }

    public static final String LOCAL_REPO_INSTANCE = "WSO2LocalRepository";
    public static final String CONFIG_SYSTEM_REGISTRY_INSTANCE = "WSO2ConfigurationSystemRegistry";
    public static final String CONFIG_USER_REGISTRY_INSTANCE = "WSO2ConfigurationUserRegistry";
    public static final String GOVERNANCE_REGISTRY_INSTANCE = "WSO2GovernanceRegistry";
    public static final String USER_REALM_INSTANCE = "WSO2UserRealm";

    public static final String BUNDLE_ID = "bundleId";

    public static final String PRIMARY_BUNDLE_CONTEXT = "primaryBundleContext";

}
