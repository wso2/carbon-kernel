/*
 * Copyright 2005,2015 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.osgi.util;


import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    /**
     * setting the maven local repo system property, important when running in jenkins
     */
    public static void setupMavenLocalRepo() {
        String localRepo = System.getProperty("maven.repo.local");
        if (localRepo != null && !localRepo.equals("")) {
            System.setProperty("org.ops4j.pax.url.mvn.localRepository", localRepo);
        }
    }

    /**
     * Set the carbon home for execute tests.
     * Carbon home is set to /carbon-kernel/tests/osgi-tests/target/carbon-home
     */
    public static void setCarbonHome() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");
        System.setProperty("carbon.home", carbonHome.toString());
    }
}
