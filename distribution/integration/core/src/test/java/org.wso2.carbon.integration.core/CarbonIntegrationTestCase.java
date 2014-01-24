/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.core;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * This is the base class of all JUnit TestCases which require starting of a Carbon server
 * instance
 * <p/>
 * The carbon.zip System property should provide the location of the Carbon server zip file that
 * needs to be extracted and run
 * <p/>
 * This can be specified in the Maven2 pom.xml file as follows
 * <p/>
 * <pre>
 *      &lt;plugins>
 *          &lt;plugin>
 *              &lt;artifactId>maven-surefire-plugin&lt;/artifactId>
 *              &lt;inherited>false&lt;/inherited>
 *              &lt;configuration>
 *                  &lt;systemProperties>
 *                      &lt;property>
 *                          &lt;name>carbon.zip&lt;/name>
 *                          &lt;value>
 *                              ${basedir}/../../kernel/target/wso2carbon-core-${project.version}.zip
 *                          &lt;/value>
 *                      &lt;/property>
 *                  &lt;/systemProperties>
 *              &lt;/configuration>
 *          &lt;/plugin>
 *      &lt;/plugins>
 * </pre>
 */
public class CarbonIntegrationTestCase extends TestCase {

    protected String carbonHome;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String carbonZip = System.getProperty("carbon.zip");
        if (carbonZip == null) {
            throw new IllegalArgumentException("carbon zip file is null");
        }
        carbonHome = ServerUtils.setUpCarbonHome(carbonZip);
        if (carbonHome == null) {
            return;
        }
        copyArtifacts();

        ServerUtils.startServerUsingCarbonHome(carbonHome);
        //ServerUtils.startServerUsingCarbonZip(carbonZip);
    }

    /**
     * Copy any artifacts that may be needed before starting the server
     *
     * @throws java.io.IOException If an error occurs while copying artifacts
     */
    protected void copyArtifacts() throws IOException {

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        ServerUtils.shutdown();
    }
}
