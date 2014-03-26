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

package org.wso2.carbon.kernel;

import java.io.File;

public class CarbonConstants {
    public static final String CARBON_HOME = "carbon.home";
    public static final String CARBON_CONF_REPO = "carbon.conf.repo";
    public static final String CARBON_OSGI_REPO = "carbon.osgi.repo";

    public static final String CARBON_CONFIG_XML = "carbon.xml";

    public static final String CARBON_REPO_DIR = "repository";
    public static final String CONF_REPO_DIR = CARBON_REPO_DIR + File.separator + "conf";
    public static final String OSGI_REPO_DIR = CARBON_REPO_DIR + File.separator + "components";
    public static final String DATA_REPO_DIR = CARBON_REPO_DIR + File.separator + "data";
}
