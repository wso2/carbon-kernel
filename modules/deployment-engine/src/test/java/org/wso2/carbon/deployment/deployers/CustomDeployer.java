/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment.deployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.ArtifactType;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.Artifact;
import org.wso2.carbon.deployment.spi.Deployer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CustomDeployer implements Deployer {
    private static Log log = LogFactory.getLog(CustomDeployer.class);

    /**
     * Has init() been called?
     */
    public static boolean initCalled;
    /**
     * Set to true if "XML1" has been deployed
     */
    public static boolean sample1Deployed;
    /**
     * Set to true if "XML1" has been updated
     */
    public static boolean sample1Updated;

    private String directory = "text-files";
    private URL directoryLocation;
    private ArtifactType artifactType;
    private String testDir = "src" + File.separator + "test" + File.separator  + "resources" +
                             File.separator + "carbon-repo" + File.separator + directory;

    public CustomDeployer() {
        artifactType = new ArtifactType("txt");
        try {
            directoryLocation = new URL("file:text-files");
        } catch (MalformedURLException e) {
            log.error(e);
        }
    }

    public void init() {
        log.info("Initializing Deployer");
        initCalled = true;
    }

    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        log.info("Deploying : " + artifact.getName());
        String key = null;
        try {
            FileInputStream fis = new FileInputStream(artifact.getFile());
            int x = fis.available();
            byte b[] = new byte[x];
            fis.read(b);
            String content = new String(b);
            if (content.contains("sample1")) {
                sample1Deployed = true;
                key = artifact.getName();
            }
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error while deploying : " + artifact.getName(), e);
        }
        return key;
    }

    public void undeploy(Object key) throws CarbonDeploymentException {
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key +
                                                "is not a String value");
        }
        log.info("Undeploying : " + key);
        try {
            File fileToUndeploy = new File(testDir + File.separator + key);
            log.info("File to undeploy : " + fileToUndeploy.getAbsolutePath());
            FileInputStream fis = new FileInputStream(fileToUndeploy);
            int x = fis.available();
            byte b[] = new byte[x];
            fis.read(b);
            String content = new String(b);
            if (content.contains("sample1")) {
                sample1Deployed = false;
            }
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key, e);
        }
    }

    public String update(Artifact artifact) throws CarbonDeploymentException {
        log.info("Updating : " + artifact.getName());
        sample1Updated = true;
        return  artifact.getName();
    }


    public URL getLocation() {
        return directoryLocation;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }
}
