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
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * Test for starting a Carbon server instance
 */
public class StartCarbonServerTest extends CarbonIntegrationTestCase {

    @Override
    protected void copyArtifacts() throws IOException {
        File srcFile = new File(System.getProperty("basedir") + File.separator + ".." +
                                        File.separator + "security-verifier" + File.separator +
                                        "target" + File.separator + "SecVerifier.aar");

        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                + "deployment" + File.separator + "server" + File.separator + "axis2services";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            System.err.println("Error while creating the deployment folder : " + deploymentPath);
        }


        File dstFile = new File(depFile.getAbsolutePath() + File.separator + "SecVerifier.aar");
        FileManipulator.copyFile(srcFile, dstFile);
    }

    /**
     * Just a simple test which will make the Carbon server start.
     *
     *  @see org.wso2.carbon.integration.core.CarbonIntegrationTestCase#setUp()
     */
    public void test1(){}

    /**
     * Yet another simple test which will make the Carbon server start.
     *
     *  @see org.wso2.carbon.integration.core.CarbonIntegrationTestCase#setUp()
     */
    public void test2(){}

    public void testSecurity() throws AxisFault {
        final ServiceClient client = new ServiceClient(null, null);
        Options opts = new Options();

        EndpointReference epr =
                new EndpointReference("http://localhost:9763/services/SecurityVerifierService");
        opts.setTo(epr);

        client.setOptions(opts);
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    client.sendRobust(createPayLoad());   // robust send. Will get reply only if there is a fault
                } catch (AxisFault axisFault) {
                    axisFault.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
        System.out.println("sent the message");
    }

    public static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs =
                fac.createOMNamespace("http://secverifier.integration.carbon.wso2.org", "ns");
        return fac.createOMElement("verifyAdminServices", omNs);
    }
}
