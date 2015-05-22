/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.jira.issues.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.utils.FileManipulator;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import static org.testng.Assert.assertTrue;

public class CARBON15203GSONBuilderOrderTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(CARBON15203GSONBuilderOrderTestCase.class);
    private static int portOffset = 28;
    private static final long TIMEOUT = 5 * 60000;
    private CarbonTestServerManager serverManager;
    private AutomationContext context;
    private String carbonHome;

    @BeforeClass(groups = { "carbon.integration.jira" })
    public void initialize() throws Exception {
        HashMap<String, String> startUpParameterMap = new HashMap<String, String>();
        startUpParameterMap.put("-DportOffset", String.valueOf(portOffset));
        context = new AutomationContext();
        serverManager = new CarbonTestServerManager(context, System.getProperty("carbon.zip"), startUpParameterMap);
        serverManager.startServer();
        carbonHome = serverManager.getCarbonHome();
        changeConfigFiles();
        restartServer();
    }

    @AfterClass(groups = { "carbon.integration.jira" })
    public void destroy() throws Exception {
        if (serverManager != null) {
            serverManager.stopServer();
        }
    }

    @Test(description = "Verify GSON Builder for DSS JSON")
    public void dssGSONBuilderTest() throws Exception {
        uploadApp();
        String serviceEndpoint = "http://" + context.getInstance().getHosts().get("default") + ":" +
                                 (Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTP_PORT) + portOffset) +
                                 "/services/DssVerifierService/";
        String endpoint = "verifyJsonRequest";
        String contentType = "application/json";
        String jsonRequest1 = "{\"" + endpoint + "\":{\"id\":291, \"name\":\"La Rochelle Gifts\", " +
                              "\"lastName\":\"Labrune\", \"firstName\":\"Janine\", \"age\":28, " +
                              "\"country\":\"France\"}}";
        String jsonResponse1 = "{\"verifyJsonRequestResponse\":{\"return\":\"Success\"}}";
        HttpResponse response1 = this.getHttpResponse(serviceEndpoint + endpoint, contentType, jsonRequest1);
        assertTrue(response1.getData().equalsIgnoreCase(jsonResponse1));

        String jsonRequest2 = "{\"" + endpoint + "\":{\"age\":28, \"name\":\"La Rochelle Gifts\", " +
                              "\"lastName\":\"Labrune\", \"firstName\":\"Janine\", \"id\":291, " +
                              "\"country\":\"France\"}}";
        String jsonResponse2 = "{\"Fault\":{\"faultcode\":\"soapenv:Server\",\"faultstring\":\"6\",\"detail\":\"\"}}";
        HttpResponse response2 = this.getHttpResponse(serviceEndpoint + endpoint, contentType, jsonRequest2);
        assertTrue(response2.getData().equalsIgnoreCase(jsonResponse2));
    }

    private void changeConfigFiles() throws Exception {
        String axis2Artifact =
                System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION) + File.separator +
                "axis2config" + File.separator + "CARBON15203" + File.separator + "axis2.xml";
        File axis2SrcFile = new File(axis2Artifact);
        String axis2ConfigPath =
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "axis2";
        File axis2File = new File(axis2ConfigPath + File.separator + "axis2.xml");
        FileManipulator.copyFile(axis2SrcFile, axis2File);
    }

    private void restartServer() throws Exception {
        Thread.sleep(5000);
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset,
                                         context.getInstance().getHosts().get("default"));
        ServerAdminClient serverAdminClient = new ServerAdminClient(
                "https://" + context.getInstance().getHosts().get("default") + ":" +
                (Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset) +
                "/services/ServerAdmin/", context.getSuperTenant().getTenantAdmin().getUserName(),
                context.getSuperTenant().getTenantAdmin().getPassword());
        serverAdminClient.restartGracefully();
        Thread.sleep(5000);
        ClientConnectionUtil
                .waitForPort(Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset, TIMEOUT, true,
                             context.getInstance().getHosts().get("default"));
        Thread.sleep(5000);
    }

    private void uploadApp() throws Exception {
        String aarServiceFile = "Axis2SampleService.aar";
        String axis2SampleServiceDir = System.getProperty("axis2.sample.service.dir");
        if (axis2SampleServiceDir == null || !(new File(axis2SampleServiceDir)).exists()) {
            log.warn("DSS JSON verification test not enabled");
            return;
        }
        assert carbonHome != null : "carbonHome cannot be null";
        File srcFile = new File(axis2SampleServiceDir + aarServiceFile);
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";
        String deploymentPath =
                carbonHome + File.separator + "repository" + File.separator + "deployment" + File.separator + "server" +
                File.separator + "axis2services";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }
        File dstFile = new File(depFile.getAbsoluteFile() + File.separator + aarServiceFile);
        log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
        FileManipulator.copyFile(srcFile, dstFile);
        Thread.sleep(20000);
    }

    HttpResponse getHttpResponse(String endpoint, String contentType, String data) throws Exception {
        if (endpoint.startsWith("http://")) {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Accept", contentType);
            conn.setRequestProperty("charset", "UTF-8");
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.close();
            conn.connect();
            // Get the response
            StringBuilder sb = new StringBuilder();
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException ignored) {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
            return new HttpResponse(sb.toString(), conn.getResponseCode());
        }
        return null;
    }

}
