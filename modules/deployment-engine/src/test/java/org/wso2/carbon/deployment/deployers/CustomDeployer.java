package org.wso2.carbon.deployment.deployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.spi.Artifact;
import org.wso2.carbon.deployment.spi.Deployer;

import java.io.FileInputStream;
import java.io.IOException;

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
     * Set to true if "XML2" has been deployed
     */
    public static boolean sample2Deployed;

    private String directory = "text-files";
    private String type = "txt";

    public void init() {
        initCalled = true;
    }

    public void deploy(Artifact artifact) throws CarbonDeploymentException {
        log.info("Deploying : " + artifact.getName());
        try {
            FileInputStream fis = new FileInputStream(artifact.getFile());
            int x = fis.available();
            byte b[] = new byte[x];
            fis.read(b);
            String content = new String(b);
            if (content.contains("sample1")) {
                sample1Deployed = true;
            }
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error while deploying : " + artifact.getName(), e);
        }
    }

    public void undeploy(Artifact artifact) throws CarbonDeploymentException {
        log.info("Un Deploying : " + artifact.getName());
        try {
            FileInputStream fis = new FileInputStream(artifact.getFile());
            int x = fis.available();
            byte b[] = new byte[x];
            fis.read(b);
            String content = new String(b);
            if (content.contains("sample1")) {
                sample1Deployed = false;
            }
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + artifact.getName(), e);
        }
    }


    public String getDirectory() {
        return directory;
    }

    @Override
    public String getType() {
        return type;
    }
}
