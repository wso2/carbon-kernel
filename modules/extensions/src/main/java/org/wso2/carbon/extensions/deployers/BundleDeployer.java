package org.wso2.carbon.extensions.deployers;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.wso2.carbon.deployment.Artifact;
import org.wso2.carbon.deployment.ArtifactType;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.spi.Deployer;
import org.wso2.carbon.extensions.internal.DataHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

@Component(
        name = "org.wso2.carbon.extensions.deployers.BundleDeployerServiceComponent",
        description = "The bundle deployer instance",
        immediate = true
)
@Service
public class BundleDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(BundleDeployer.class);

    private ArtifactType artifactType;
    private URL deploymentLocation;

    @Override
    public void init() {
        artifactType = new ArtifactType(Bundle.class);
        try {
            deploymentLocation = new URL("file:bundles");
        } catch (MalformedURLException e) {
            logger.error("Error while initializing Bundle Deployer instance");
        }
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        Bundle bundle = null;
        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        try {
            logger.info("Deploying bundle  : {}", artifact.getName());
            bundle = bundleContext.installBundle(artifact.getFile().toURI().toURL().toString());
        } catch (BundleException | MalformedURLException e) {
            throw new CarbonDeploymentException("Error while deploying bundle", e);
        }
        return bundle;
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (key instanceof Bundle) {
            Bundle bundle = ((Bundle) key);
            try {
                logger.info("Undeploying bundle  : {}", bundle.getSymbolicName());
                bundle.uninstall();
            } catch (BundleException e) {
                throw new CarbonDeploymentException("Error while undeploying bundle", e);
            }
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        Bundle bundle = null;
        Object key = artifact.getKey();
        if (key instanceof Bundle) {
            bundle = ((Bundle) key);
            try {
                logger.info("Updating bundle  : {}", bundle.getSymbolicName());
                bundle.update(new FileInputStream(artifact.getFile()));
            } catch (BundleException | FileNotFoundException e) {
                throw new CarbonDeploymentException("Error while updating bundle", e);
            }
        }
        return bundle;
    }

    @Override
    public URL getLocation() {
        return deploymentLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }
}
