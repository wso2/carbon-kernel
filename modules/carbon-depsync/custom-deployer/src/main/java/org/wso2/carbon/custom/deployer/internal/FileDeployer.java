package org.wso2.carbon.custom.deployer.internal;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.deployment.Artifact;
import org.wso2.carbon.kernel.deployment.ArtifactType;
import org.wso2.carbon.kernel.deployment.Deployer;
import org.wso2.carbon.kernel.deployment.exception.CarbonDeploymentException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Custom deployer fo deploying text files
 */
@Component(
        name = "org.wso2.carbon.custom.deployer.internal.FileDeployer",
        service = Deployer.class,
        immediate = true
)
public class FileDeployer implements Deployer {
    private static final Logger log = LoggerFactory.getLogger(FileDeployer.class);

    private URL directoryLocation;
    private ArtifactType artifactType;
    private final String DEPLOYMENTPATH = "file:text-files";

    public FileDeployer() {
        artifactType = new ArtifactType<String>("txt");
        try {
            directoryLocation = new URL(DEPLOYMENTPATH);
        } catch (MalformedURLException e) {
            log.error("Error while initializing directoryLocation", e);
        }
    }

    @Override
    public void init() {
        log.info("Initializing deployer");
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        log.info("Deploying : " + artifact.getName());
        String key = null;
        try {
            FileInputStream fis = new FileInputStream(artifact.getFile());
            int x = fis.available();
            byte b[] = new byte[x];
            fis.read(b);
            String content = new String(b);
            if (content != null) {
                key = artifact.getName();
                //Logging a message with the artifact name to confirm deployment
                log.info("Deployed artifact :  " + key + " successfully.");
            }  else {
                throw new IOException();
            }
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error while deploying : " + artifact.getName(), e);
        }
        return key;
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Undploying : " + key +
                    "is not a String value");
        }
        log.info("Undeploying : " + key);
        try {
            log.info("File to undeploy : " + key);
            log.info("Undeployed artifact : " + key + "successfully.");
        } catch (Exception e) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key, e);
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        log.info("Updating artifact " + artifact);
        return artifact.getFile().getAbsolutePath();
    }

    @Override
    public URL getLocation() {
        return directoryLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }
}
