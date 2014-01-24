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
package org.wso2.carbon.integration.framework.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * A collection of utilities to facilitate tests
 */
public class TestUtil {

    private static final Log log = LogFactory.getLog(TestUtil.class);

    /**
     * Copy the SecVerifier.aar file into the Carbon server's AAR service deployment directory
     * @param carbonHome CARBON_HOME of the target Carbon server
     * @throws IOException If an error occurs while copying the SecVerifier.aa
     */
    public static void copySecurityVerificationService(String carbonHome) throws IOException {
        String secVerifierDir = System.getProperty("sec.verifier.dir");
        if(secVerifierDir == null){
            log.warn("Security verification test not enabled");
            return;
        }
        assert carbonHome != null : "carbonHome cannot be null";
        File srcFile = new File(secVerifierDir  + "SecVerifier.aar");
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";

        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                                + "deployment" + File.separator + "server" + File.separator + "axis2services";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }
        File dstFile = new File(depFile.getAbsolutePath() + File.separator + "SecVerifier.aar");
        log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
        FileManipulator.copyFile(srcFile, dstFile);
    }
}
