/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.tests.integration;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.utils.FileManipulator;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

/**
 * A test case which verifies that all admin services deployed on this Carbon server are properly
 * secured
 */
public class SecurityVerificationTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(SecurityVerificationTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    @Test(description = "Ensures that all Admin services are exposed only via HTTPS")
    public void verifyAdminServiceSecurity() throws IOException, XPathExpressionException, InterruptedException {

        copySecurityVerificationService(CarbonBaseUtils.getCarbonHome());


        ServiceClient client = new ServiceClient(null, null);
        Options opts = new Options();

        EndpointReference epr =
                new EndpointReference("http://" + automationContext.getInstance().getHosts().get("default") + ":" +
                        FrameworkConstants.SERVER_DEFAULT_HTTP_PORT
                        + "/services/SecurityVerifierService");

        String address = epr.getAddress();
        assert isWebAppDeployed("SecurityVerifierService", address) : address + "does not exist";
        opts.setTo(epr);

        client.setOptions(opts);
        client.sendRobust(createPayLoad());   // robust send. Will get reply only if there is a fault
        log.info("sent the message");
    }

    private OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs =
                fac.createOMNamespace("http://secverifier.tests.integration.carbon.wso2.org", "ns");
        return fac.createOMElement("verifyAdminServices", omNs);
    }

    private void copySecurityVerificationService(String carbonHome) throws IOException, InterruptedException {
        String secVerifierDir = System.getProperty("sec.verifier.dir");
        if (secVerifierDir == null || !(new File(secVerifierDir)).exists()) {
            log.warn("Security verification test not enabled");
            return;
        }
        assert carbonHome != null : "carbonHome cannot be null";
        //File srcFile = new File(secVerifierDir + "SecVerifier.aar");
        Path srcFile = Paths.get(secVerifierDir, "SecVerifier.aar");
       // assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";
        assert srcFile.toFile().exists() : srcFile.toFile().getAbsolutePath() + " does not exist";

        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                + "deployment" + File.separator + "server" + File.separator + "axis2services";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }
        //File dstFile = new File(depFile.getAbsolutePath() + File.separator + "SecVerifier.aar");
        Path dstFile = Paths.get(depFile.getAbsolutePath(), "SecVerifier.aar");
        //log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
        log.info("Copying " + srcFile.toFile().getAbsolutePath() + " => " + dstFile.toFile().getAbsolutePath());
        //FileManipulator.copyFile(srcFile, dstFile);
        //Thread.sleep(20000);
        FileManipulator.copyFile1(srcFile, dstFile);

        assert isFileCopied(dstFile) : dstFile.toFile().getAbsolutePath() + "has not been copied";
    }

    private static boolean isFileCopied(Path path) {
        Calendar startTime = Calendar.getInstance();
        while (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis() < 90000)
            ;

        if (Files.exists(path)) {
            return true;
        }
        return false;
    }

    private static boolean isWebAppDeployed(String webAppName, String endpoint) {
        log.info("waiting " + 90000 + " millis for webApp undeployment " + webAppName);
        HttpResponse response;

        Calendar startTime = Calendar.getInstance();
        while ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) < 90000) {
            try {
                response = HttpRequestUtil.sendGetRequest(endpoint, null);
                if (response != null && !response.getData().isEmpty()) {
                    return true;
                }
            } catch (IOException e) {
                //Ignore IOExceptions
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {

            }

        }
        return false;
    }
}
