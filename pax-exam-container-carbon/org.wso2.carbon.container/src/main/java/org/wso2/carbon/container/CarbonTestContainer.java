package org.wso2.carbon.container;

import org.apache.commons.io.FileUtils;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.options.extra.RepositoryOption;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.container.options.CarbonDropinsBundleOption;
import org.wso2.carbon.container.options.CarbonFileCopyOption;
import org.wso2.carbon.container.options.CarbonHomeOption;
import org.wso2.carbon.container.options.DebugConfigurationOption;
import org.wso2.carbon.container.options.KeepRuntimeDirectory;
import org.wso2.carbon.container.runner.CarbonRunner;
import org.wso2.carbon.container.runner.Runner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.rbc.Constants.RMI_HOST_PROPERTY;
import static org.ops4j.pax.exam.rbc.Constants.RMI_NAME_PROPERTY;
import static org.ops4j.pax.exam.rbc.Constants.RMI_PORT_PROPERTY;

/**
 * Test Container class to configure the distribution and start the server.
 */
public class CarbonTestContainer implements TestContainer {

    private static final Logger logger = LoggerFactory.getLogger(CarbonTestContainer.class);

    private static final String CARBON_TEST_CONTAINER = "CarbonTestContainer";
    private static final String EXAM_INJECT_PROPERTY = "pax.exam.inject";

    private final Runner runner;
    private final ExamSystem system;
    private CarbonHomeOption carbonHomeDirectoryOption;
    private boolean started;
    private RBCRemoteTarget target;
    private Path targetDirectory;
    private Registry registry;

    public CarbonTestContainer(ExamSystem system, CarbonHomeOption carbonHomeDirectoryOption) {
        this.carbonHomeDirectoryOption = carbonHomeDirectoryOption;
        this.system = system;
        this.runner = new CarbonRunner();
    }

    public synchronized TestContainer start() {

        if (carbonHomeDirectoryOption.getDistributionDirectoryPath() == null
                && carbonHomeDirectoryOption.getDistributionMavenURL() == null &&
                carbonHomeDirectoryOption.getDistributionZipPath() == null) {
            throw new IllegalStateException("Distribution path need to be set.");
        }

        try {
            String name = system.createID(CARBON_TEST_CONTAINER);
            FreePort freePort = new FreePort(21000, 21099);
            int port = freePort.getPort();
            logger.info("using RMI registry at port {}" + name, port);
            registry = LocateRegistry.createRegistry(port);
            String host = InetAddress.getLocalHost().getHostName();

            //Setting RMI related properties
            ExamSystem subsystem = system.fork(options(systemProperty(RMI_HOST_PROPERTY).value(host),
                    systemProperty(RMI_PORT_PROPERTY).value(Integer.toString(port)),
                    systemProperty(RMI_NAME_PROPERTY).value(name), systemProperty(EXAM_INJECT_PROPERTY).value("true")));

            target = new RBCRemoteTarget(name, port, subsystem.getTimeout());
            System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");

            addRepositories();
            targetDirectory = retrieveFinalTargetDirectory();

            if (carbonHomeDirectoryOption.getDistributionMavenURL() != null) {
                URL sourceDistribution = new URL(carbonHomeDirectoryOption.getDistributionMavenURL().getURL());
                ArchiveExtractor.extract(sourceDistribution, targetDirectory.toFile());
            } else if (carbonHomeDirectoryOption.getDistributionZipPath() != null) {
                Path sourceDistribution = carbonHomeDirectoryOption.getDistributionZipPath();
                ArchiveExtractor.extract(sourceDistribution, targetDirectory.toFile());
            } else if (carbonHomeDirectoryOption.getDistributionDirectoryPath() != null) {
                Path sourceDirectory = carbonHomeDirectoryOption.getDistributionDirectoryPath();
                FileUtils.copyDirectory(sourceDirectory.toFile(), targetDirectory.toFile());
            }

            copyReferencedBundles(targetDirectory, subsystem);
            copyFiles(targetDirectory);

            Path carbonBin = targetDirectory.resolve("bin");
            makeScriptsInBinExec(carbonBin.toFile());
            ArrayList<String> options = new ArrayList<>();
            String[] environment = new String[] {};
            setupSystemProperties(options, subsystem);

            DebugConfigurationOption[] debugConfigurationOptions = system.getOptions(DebugConfigurationOption.class);

            if (debugConfigurationOptions.length > 0) {
                options.add(debugConfigurationOptions[debugConfigurationOptions.length - 1].getDebugConfiguration());
            }

            runner.exec(environment, targetDirectory, options);
            logger.info("Wait for test container to finish its initialization " + subsystem.getTimeout());
            waitForState(0, Bundle.ACTIVE, subsystem.getTimeout());
            started = true;
        } catch (IOException e) {
            throw new RuntimeException("Problem starting container", e);
        }
        return this;
    }

    /**
     * Set repositories specified in the Repository Option.
     */
    private void addRepositories() {
        RepositoryOption[] repositories = system.getOptions(RepositoryOption.class);
        if (repositories.length != 0) {
            System.setProperty("org.ops4j.pax.url.mvn.repositories", buildString(repositories));
        }
    }

    /**
     * Copy dependencies specified as carbon dropins bundle option in system to the dropins Directory
     */
    private void copyReferencedBundles(Path carbonHome, ExamSystem system) {
        Path targetDirectory = carbonHome.resolve("osgi").resolve("dropins");

        Arrays.asList(system.getOptions(CarbonDropinsBundleOption.class)).forEach(
                option -> copyReferencedArtifactsToDeployDirectory(option.getMavenArtifactUrlReference().getURL(),
                        targetDirectory));
    }

    /**
     * Helper method to copy artifacts to the dropins.
     *
     * @param url             url of the artifact
     * @param targetDirectory target directory
     */
    private void copyReferencedArtifactsToDeployDirectory(String url, Path targetDirectory) {
        File target = createUnique(url, targetDirectory.toFile());
        try {
            FileUtils.copyURLToFile(new URL(url), target);
        } catch (IOException e) {
            logger.error("Error while copying Artifacts", e);
        }
    }

    /**
     * Create unique id to the file
     *
     * @param url    url of the artifact
     * @param deploy deploy directory
     * @return file
     */
    private File createUnique(String url, File deploy) {
        String prefix = UUID.randomUUID().toString();
        String fileName = new File(url).getName();
        return new File(deploy, prefix + "_" + fileName + ".jar");
    }

    /**
     * Copy files specified in the carbon file copy option to the destination path.
     *
     * @param carbonHome carbon home
     */
    private void copyFiles(Path carbonHome) {
        Arrays.asList(system.getOptions(CarbonFileCopyOption.class)).forEach(option -> {
            try {
                Files.copy(option.getSourcePath(), carbonHome.resolve(option.getDestinationPath()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Error while copying configuration files", e);
            }
        });
    }

    /**
     * Setup system properties.
     * @param options
     * @param subsystem
     * @throws IOException
     */
    private void setupSystemProperties(List<String> options, ExamSystem subsystem) throws IOException {
        Arrays.asList(subsystem.getOptions(SystemPropertyOption.class)).forEach(systemPropertyOption -> {
            String property = String.format("-D%s=%s", systemPropertyOption.getKey(), systemPropertyOption.getValue());
            options.add(property);
        });
    }

    /**
     * Make all the files in the bin directory to be executable.
     *
     * @param carbonBin carbonBin
     */
    private void makeScriptsInBinExec(File carbonBin) {
        if (!carbonBin.exists()) {
            return;
        }
        File[] files = carbonBin.listFiles();
        if (files != null) {
            Arrays.asList(files).forEach(file -> file.setExecutable(true));
        }
    }

    /**
     * Generate a random path if the unpack directory is not specified.
     *
     * @return the path to the carbon home directory
     * @throws IOException
     */
    private Path retrieveFinalTargetDirectory() throws IOException {
        Path unpackDirectory = carbonHomeDirectoryOption.getUnpackDirectory();
        if (unpackDirectory == null) {
            unpackDirectory = Paths.get("target", UUID.randomUUID().toString());
        }

        boolean isCreated = true;
        if (!unpackDirectory.toFile().exists()) {
            isCreated = unpackDirectory.toFile().mkdir();
        }

        if (!isCreated) {
            throw new IOException("Couldn't create the directory: " + unpackDirectory.toFile().toString());
        }

        return unpackDirectory;
    }

    /**
     * Check whether the container should delete the test directories or not.
     *
     * @return boolean
     */
    private boolean shouldDeleteRuntime() {
        boolean deleteRuntime = true;
        KeepRuntimeDirectory[] keepRuntimeDirectory = system.getOptions(KeepRuntimeDirectory.class);
        if (keepRuntimeDirectory != null && keepRuntimeDirectory.length != 0) {
            deleteRuntime = false;
        }
        return deleteRuntime;
    }

    private String buildString(ValueOption<?>[] options) {
        return buildString(new String[0], options, new String[0]);
    }

    private String buildString(String[] prepend, ValueOption<?>[] options, String[] append) {
        StringBuilder builder = new StringBuilder();

        Arrays.asList(prepend).forEach(s -> {
            builder.append(s);
            builder.append(",");
        });

        Arrays.asList(options).forEach(option -> {
            builder.append(option.getValue());
            builder.append(",");
        });

        Arrays.asList(append).forEach(s -> {
            builder.append(s);
            builder.append(",");
        });

        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        } else {
            return "";
        }
    }

    @Override
    public synchronized TestContainer stop() {
        logger.debug("Shutting down the test container.");
        try {
            if (started) {
                target.stop();
                RemoteBundleContextClient remoteBundleContextClient = target.getClientRBC();
                if (remoteBundleContextClient != null) {
                    remoteBundleContextClient.stop();

                }
                runner.shutdown();
                try {
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (NoSuchObjectException exc) {
                    throw new TestContainerException(exc);
                }

            } else {
                throw new RuntimeException("Container never started.");
            }
        } finally {
            started = false;
            target = null;
            if (shouldDeleteRuntime()) {
                system.clear();
                try {
                    FileUtils.forceDelete(targetDirectory.toFile());
                } catch (IOException e) {
                    forceCleanup();
                }
            }
        }
        return this;
    }

    private void forceCleanup() {
        try {
            FileUtils.forceDeleteOnExit(targetDirectory.toFile());
        } catch (IOException e) {
            logger.error("Error occured when deleting the Directory.", e);
        }
    }

    private synchronized void waitForState(final long bundleId, final int state, final RelativeTimeout timeout) {
        target.getClientRBC().waitForState(bundleId, state, timeout);
    }

    @Override
    public synchronized void call(TestAddress address) {
        target.call(address);
    }

    @Override
    public synchronized long install(InputStream stream) {
        return install("local", stream);
    }

    @Override
    public synchronized long install(String location, InputStream stream) {
        return target.install(location, stream);
    }

    @Override
    public synchronized long installProbe(InputStream stream) {
        return target.installProbe(stream);
    }

    @Override
    public synchronized void uninstallProbe() {
        target.uninstallProbe();
    }

    @Override
    public String toString() {
        return "CarbonTestContainer";
    }
}
