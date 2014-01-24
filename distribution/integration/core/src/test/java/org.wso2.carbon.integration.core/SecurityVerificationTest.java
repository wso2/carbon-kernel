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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * Test case for verifying server side security
 */
public class SecurityVerificationTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(SecurityVerificationTest.class);

    @Override
    protected void copyArtifacts() throws IOException {
        String secVerifierDir = System.getProperty("sec.verifier.dir");
        File srcFile = new File(secVerifierDir + "target" + File.separator + "SecVerifier.aar");

        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                                + "deployment" + File.separator + "server" + File.separator + "axis2services";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            System.err.println("Error while creating the deployment folder : " + deploymentPath);
        }
        File dstFile = new File(depFile.getAbsolutePath() + File.separator + "SecVerifier.aar");
        try {
            log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
            FileManipulator.copyFile(srcFile, dstFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    public static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs =
                fac.createOMNamespace("http://secverifier.integration.carbon.wso2.org", "ns");
        return fac.createOMElement("verifyAdminServices", omNs);
    }

    @Override
    public void init() {
    }

    @Override
    public void runSuccessCase() {
        try {
            ServiceClient client = new ServiceClient(null, null);
            Options opts = new Options();

            EndpointReference epr =
                    new EndpointReference("http://localhost:9763/services/SecurityVerifierService");
            opts.setTo(epr);

            client.setOptions(opts);
            client.sendRobust(createPayLoad());   // robust send. Will get reply only if there is a fault
            log.info("sent the message");
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void runFailureCase() {
        // Nothing to do
    }

    @Override
    public void cleanup() {
        // Nothing to do
    }
}
