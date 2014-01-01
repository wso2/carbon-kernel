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
     * Set to true if "XML2" has been deployed
     */
    public static boolean sample2Deployed;

    private String directory = "text-files";
    private URL directoryLocation;
    private ArtifactType artifactType;
    private String testDir = "src" + File.separator + "test" + File.separator  + "resources" +
                             File.separator + "carbon-repo" + File.separator + directory;

    public CustomDeployer() {
        artifactType = new ArtifactType("txt");
        try {
            directoryLocation = new URL(new URL("file:"), "./text-files");
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
                key = "sample1.txt";
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
        log.info("Un Deploying : " + key);
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

    public Object update(Artifact artifact) throws CarbonDeploymentException {
        return null;
    }


    public URL getLocation() {
        return directoryLocation;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }
}
