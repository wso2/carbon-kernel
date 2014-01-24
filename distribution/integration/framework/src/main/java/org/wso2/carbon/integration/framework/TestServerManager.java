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
package org.wso2.carbon.integration.framework;

import org.wso2.carbon.integration.framework.utils.CodeCoverageUtils;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.framework.utils.ServerUtils;
import org.wso2.carbon.integration.framework.utils.TestUtil;

import java.io.File;
import java.io.IOException;

/**
 * A TestServerManager is responsible for preparing a Carbon server for test executions,
 * and shuts down the server after test executions.
 * <p/>
 * All test suites which require starting of a server should extend this class
 */
public abstract class TestServerManager {

    private ServerUtils serverUtils = new ServerUtils();
    private String carbonZip;
    private int portOffset;

    protected TestServerManager() {
    }

    protected TestServerManager(String carbonZip) {
        this.carbonZip = carbonZip;
    }

    protected TestServerManager(int portOffset) {
        this.portOffset = portOffset;
    }

    protected TestServerManager(String carbonZip, int portOffset) {
        this.carbonZip = carbonZip;
        this.portOffset = portOffset;
    }

    public String getCarbonZip() {
        return carbonZip;
    }

    /**
     * This method is called for starting a Carbon server in preparation for execution of a
     * TestSuite
     * <p/>
     * Add the @BeforeSuite TestNG annotation in the method overriding this method
     *
     * @return The CARBON_HOME
     * @throws IOException If an error occurs while copying the deployment artifacts into the
     *                     Carbon server
     */
    protected String startServer() throws IOException {
        if (carbonZip == null) {
            carbonZip = System.getProperty("carbon.zip");
        }
        if (carbonZip == null) {
            throw new IllegalArgumentException("carbon zip file is null");
        }
        String carbonHome = serverUtils.setUpCarbonHome(carbonZip);
        TestUtil.copySecurityVerificationService(carbonHome);
        copyArtifacts(carbonHome);
        serverUtils.startServerUsingCarbonHome(carbonHome, portOffset);
        FrameworkSettings.init();
        return carbonHome;
    }
    
    /**
     * This method is called for starting a Carbon server in preparation for execution of a
     * TestSuite
     * <p/>
     * Add the @BeforeSuite TestNG annotation in the method overriding this method
     * @param carbonManagementContext context of the application
     * @param scriptName script name to be executed
     * @return The CARBON_HOME
     * @throws IOException If an error occurs while copying the deployment artifacts into the
     *                     Carbon server
     */
    protected String startServerInCarbonFolder(String carbonManagementContext, String scriptName) throws IOException {
        if (carbonZip == null) {
            carbonZip = System.getProperty("carbon.zip");
        }
        if (carbonZip == null) {
            throw new IllegalArgumentException("carbon zip file is null");
        }
        String carbonHome = serverUtils.setUpCarbonHome(carbonZip);
        String carbonFolder = "";
    	if(carbonHome != null) {
    		carbonFolder = carbonHome + File.separator + "carbon";
    	}
        TestUtil.copySecurityVerificationService(carbonFolder);
        copyArtifacts(carbonHome);
        serverUtils.startServerUsingCarbonHome(carbonHome, carbonFolder, scriptName, portOffset, carbonManagementContext);
        FrameworkSettings.init();
        return carbonHome;
    }

    /**
     * This method is called for stopping a Carbon server
     * <p/>
     * Add the @AfterSuite annotation in the method overriding this method
     *
     * @throws Exception If an error occurs while shutting down the server
     */
    protected void stopServer() throws Exception {
        serverUtils.shutdown(portOffset);
        CodeCoverageUtils.generateReports();
    }
    
    /**
     * This method is called for stopping a Carbon server
     * <p/>
     * Add the @AfterSuite annotation in the method overriding this method
     * @param carbonManagementContext context of the application
     * @throws Exception If an error occurs while shutting down the server
     */
    protected void stopServer(String carbonManagementContext) throws Exception {
        serverUtils.shutdown(portOffset, carbonManagementContext);
        CodeCoverageUtils.generateReports();
    }

    /**
     * Copy all the artifacts necessary for running a TestSuite
     *
     * @param carbonHome The CARBON_HOME of the relevant server
     * @throws IOException If an error occurs while copying artifacts
     */
    protected abstract void copyArtifacts(String carbonHome) throws IOException;
}
