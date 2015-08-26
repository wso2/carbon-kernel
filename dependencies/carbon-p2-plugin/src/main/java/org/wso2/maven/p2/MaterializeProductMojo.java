package org.wso2.maven.p2;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.tycho.model.ProductConfiguration;
import org.eclipse.tycho.p2.facade.internal.P2ApplicationLauncher;
import org.wso2.maven.p2.generate.utils.FileManagementUtil;
import org.wso2.maven.p2.generate.utils.P2Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Maven goal: materialize-product
 */
public class MaterializeProductMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;
    /**
     * Metadata repository name
     *
     * @parameter
     */
    private URL metadataRepository;
    /**
     * Artifact repository name
     *
     * @parameter
     */
    private URL artifactRepository;


    /**
     * The product configuration, a .product file. This file manages all aspects
     * of a product definition from its constituent plug-ins to configuration
     * files to branding.
     *
     * @parameter expression="${productConfiguration}"
     */
    private File productConfigurationFile;

    /**
     * @parameter
     */
    private URL targetPath;
    /**
     * Parsed product configuration file
     */
    private ProductConfiguration productConfiguration;

    /**
     * The new profile to be created during p2 Director install &
     * the default profile for the the application which is set in config.ini
     *
     * @parameter expression="${profile}"
     */
    private String profile;


    /**
     * @component
     */
    private P2ApplicationLauncher launcher;

    /**
     * Kill the forked test process after a certain number of seconds. If set to 0, wait forever for
     * the process, never timing out.
     *
     * @parameter expression="${p2.timeout}"
     */
    private int forkedProcessTimeoutInSeconds;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (profile == null) {
                profile = P2Constants.DEFAULT_PROFILE_ID;
            }
            deployRepository();
            //updating profile's config.ini p2.data.area property using relative path
            File profileConfigIni = FileManagementUtil.getProfileConfigIniFile(targetPath.getPath(), profile);
            FileManagementUtil.
                    changeConfigIniProperty(profileConfigIni, "eclipse.p2.data.area", "@config.dir/../../p2/");
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot generate P2 metadata", e);
        }
    }

    private void regenerateCUs()
            throws MojoExecutionException, MojoFailureException {
        getLog().debug("Regenerating config.ini");
        Properties props = new Properties();
        String id = productConfiguration.getId();

        setPropertyIfNotNull(props, "osgi.bundles", getFeaturesOsgiBundles());
        setPropertyIfNotNull(props, "osgi.bundles.defaultStartLevel", "4");
        if (profile == null) {
            profile = "profile";
        }
        setPropertyIfNotNull(props, "eclipse.p2.profile", profile);
        setPropertyIfNotNull(props, "eclipse.product", id);
        setPropertyIfNotNull(props, "eclipse.p2.data.area", "@config.dir/../p2/");
        setPropertyIfNotNull(props, "eclipse.application", productConfiguration.getApplication());


        File configsFolder = new File(targetPath.toString(), "configuration");
        configsFolder.mkdirs();

        File configIni = new File(configsFolder, "config.ini");
        try {
            FileOutputStream fos = new FileOutputStream(configIni);
            props.store(fos, "Product Runtime Configuration File");
            fos.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating .eclipseproduct file.", e);
        }

    }

    private String getFeaturesOsgiBundles() {
        String bundles = "org.eclipse.equinox.common@2:start," +
                "org.eclipse.update.configurator@3:start," +
                "org.eclipse.core.runtime@start,org.eclipse.equinox.ds@1:start," +
                "org.eclipse.equinox.simpleconfigurator@1:start";
        return bundles;
    }

    private void deployRepository() throws Exception {
        productConfiguration = ProductConfiguration.read(productConfigurationFile);
        P2ApplicationLauncher launcher = this.launcher;

        launcher.setWorkingDirectory(project.getBasedir());
        launcher.setApplicationName("org.eclipse.equinox.p2.director");

        launcher.addArguments(
                "-metadataRepository", metadataRepository.toExternalForm(),
                "-artifactRepository", metadataRepository.toExternalForm(),
                "-installIU", productConfiguration.getId(),
                "-profileProperties", "org.eclipse.update.install.features=true",
                "-profile", profile.toString(),
                "-bundlepool", targetPath.toExternalForm(),
                //to support shared installation in carbon
                "-shared", targetPath.toExternalForm() + File.separator + "p2",
                //target is set to a separate directory per Profile
                "-destination", targetPath.toExternalForm() + File.separator + profile,
                "-p2.os", "linux",
                "-p2.ws", "gtk",
                "-p2.arch", "x86",
                "-roaming"
        );

        int result = launcher.execute(forkedProcessTimeoutInSeconds);

        if (result != 0) {
            throw new MojoFailureException("P2 publisher return code was " + result);
        }

    }

    private void setPropertyIfNotNull(Properties properties, String key, String value) {
        if (value != null) {
            properties.setProperty(key, value);
        }
    }

}
