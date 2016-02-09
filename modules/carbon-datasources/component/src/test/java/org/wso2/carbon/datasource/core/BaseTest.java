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
package org.wso2.carbon.datasource.core;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base test class for unit testing. Common methods for unit tests reside here.
 */
public class BaseTest {

    protected void setEnv() {
        Path carbonHomePath = Paths.get("target", "carbonHome");
        System.setProperty("carbon.home", carbonHomePath.toFile().getAbsolutePath());

        Path configFilePath = Paths.get("src", "test", "resources", "conf", "datasources", "master-datasources.xml");
        Path configPathCopyLocation = Paths.get("target", "carbonHome", "conf", "datasources",
                "master-datasources.xml");
        Utils.copy(configFilePath.toFile().getAbsolutePath(), configPathCopyLocation.toFile().getAbsolutePath());
    }

    protected void clearEnv() {
        System.clearProperty("carbon.home");
    }

}
