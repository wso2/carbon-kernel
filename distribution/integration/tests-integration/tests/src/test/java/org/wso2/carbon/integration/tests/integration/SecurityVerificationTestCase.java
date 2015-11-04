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

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
        assert isWebServiceDeployed("SecurityVerifierService", address) : address + "does not exist";
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

        Path srcFile = Paths.get(secVerifierDir, "SecVerifier.aar");

        assert Files.exists(srcFile) : srcFile.toFile().getAbsolutePath() + " does not exist";

        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                + "deployment" + File.separator + "server" + File.separator + "axis2services";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }

        Path dstFile = Paths.get(depFile.getAbsolutePath(), "SecVerifier.aar");
        log.info("Copying " + srcFile.toFile().getAbsolutePath() + " => " + dstFile.toFile().getAbsolutePath());

        Files.copy(srcFile, dstFile, StandardCopyOption.REPLACE_EXISTING);

        assert Files.exists(dstFile) : dstFile.toFile().getAbsolutePath() + "has not been copied";
    }

    private boolean isWebServiceDeployed(String webServiceName, String endpoint) {
        log.info("waiting " + 90000 + " millis for web service deployment " + webServiceName);
        HttpResponse response;

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 90000) {
            try {
                response = HttpRequestUtil.sendGetRequest(endpoint, null);
                if (!response.getData().isEmpty()) {
                    return true;
                }
            } catch (IOException ignored) {
                //Ignore IOExceptions as this is simply checking the availability of the given webservice continuously
                //until a positive response is received within a time limit. An IOException could occur during the
                //connection establishment but failures in connection establishment shouldn't affect the busy waiting
                //for a positive response and it doesn't need to be specifically handled
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                //Here sleep is used just to reduce the frequency of the while loop since time gap between a
                //web service's undeployed and deployed status is higher than the time for one cycle in while loop.
                //Therefore an interruption is not a concern hence ignored
            }
        }
        return false;
    }
}
