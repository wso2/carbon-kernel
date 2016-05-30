package org.wso2.carbon.osgi.test.util.container;

import org.apache.commons.io.FileUtils;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.options.extra.RepositoryOption;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.osgi.test.util.container.options.CarbonDistributionConfigurationFileReplacementOption;
import org.wso2.carbon.osgi.test.util.container.options.CarbonDistributionConfigurationOption;
import org.wso2.carbon.osgi.test.util.container.options.KeepRuntimeDirectory;
import org.wso2.carbon.osgi.test.util.container.runner.Runner;

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

public class CarbonTestContainer implements TestContainer {

    private static final Logger logger = LoggerFactory.getLogger(CarbonTestContainer.class);

    private static final String CARBON_TEST_CONTAINER = "CarbonTestContainer.start";
    private static final String EXAM_INJECT_PROPERTY = "pax.exam.inject";

    private final Runner runner;
    private final ExamSystem system;
    private CarbonDistributionConfigurationOption carbonDistributionConfigurationOption;
    private boolean started;
    private RBCRemoteTarget target;
    private Path targetDirectory;
    private Registry rgstry;

    public CarbonTestContainer(ExamSystem system,
            CarbonDistributionConfigurationOption carbonDistributionConfigurationOption, Runner runner) {
        this.carbonDistributionConfigurationOption = carbonDistributionConfigurationOption;
        this.system = system;
        this.runner = runner;
    }

    public synchronized TestContainer start() {

        if (carbonDistributionConfigurationOption.getDistributionDirectoryPath() == null
                && carbonDistributionConfigurationOption.getDistributionMavenURL() == null &&
                carbonDistributionConfigurationOption.getDistributionZipPath() == null) {
            throw new IllegalStateException("Either distributionURL or distributionUrlReference need to be set.");
        }

        try {
            String name = system.createID(CARBON_TEST_CONTAINER);

            FreePort freePort = new FreePort(21000, 21099);
            int port = freePort.getPort();

            logger.info("using RMI registry at port {}", port);

            rgstry = LocateRegistry.createRegistry(port);

            String host = InetAddress.getLocalHost().getHostName();

            //setting RMI related properties
            ExamSystem subsystem = system.fork(options(systemProperty(RMI_HOST_PROPERTY).value(host),
                    systemProperty(RMI_PORT_PROPERTY).value(Integer.toString(port)),
                    systemProperty(RMI_NAME_PROPERTY).value(name), systemProperty(EXAM_INJECT_PROPERTY).value("true")
                    //                    systemProperty("jline.shutdownhook").value("true"))
            ));

            target = new RBCRemoteTarget(name, port, subsystem.getTimeout());

            System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");

            RepositoryOption[] repositories = system.getOptions(RepositoryOption.class);
            if (repositories.length != 0) {
                System.setProperty("org.ops4j.pax.url.mvn.repositories", buildString(repositories));
            }

            if (carbonDistributionConfigurationOption.getDistributionDirectoryPath() == null) {
                targetDirectory = retrieveFinalTargetDirectory();

                if (carbonDistributionConfigurationOption.getDistributionMavenURL() != null) {
                    URL sourceDistribution = new URL(
                            carbonDistributionConfigurationOption.getDistributionMavenURL().getURL());
                    ArchiveExtractor.extract(sourceDistribution, targetDirectory.toFile());
                }
                if (carbonDistributionConfigurationOption.getDistributionZipPath() != null) {
                    Path sourceDistribution = carbonDistributionConfigurationOption.getDistributionZipPath();
                    ArchiveExtractor.extract(sourceDistribution, targetDirectory.toFile());
                }

            } else {
                targetDirectory = carbonDistributionConfigurationOption.getDistributionDirectoryPath();
            }

            Path carbonHome = targetDirectory;

            //            copyPaxBundlesToDropins(carbonHome);
            copyReferencedBundles(carbonHome, subsystem);
            copyConfigurationFiles(carbonHome);

            startCarbon(subsystem, carbonHome);
            started = true;
        } catch (IOException e) {
            throw new RuntimeException("Problem starting container", e);
        }
        return this;
    }

    private File createUnique(String url, File deploy) {
        String prefix = UUID.randomUUID().toString();
        String fileName = new File(url).getName();
        return new File(deploy, prefix + "_" + fileName + ".jar");
    }

    private void copyPaxBundlesToDropins(Path carbonHome) {
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base-lang/1.5.0", carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base-monitors/1.5.0",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base-net/1.5.0", carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base-store/1.5.0",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base-io/1.5.0", carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base-spi/1.5.0", carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base-util-property/1.5.0",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.swissbox/pax-swissbox-core/1.8.2",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.swissbox/pax-swissbox-extender/1.8.2",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.swissbox/pax-swissbox-lifecycle/1.8.2",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.swissbox/pax-swissbox-tracker/1.8.2",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.swissbox/pax-swissbox-framework/1.8.2",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.exam/pax-exam/4.10.0-SNAPSHOT",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.exam/pax-exam-extender-service/4.10.0-SNAPSHOT",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.exam/pax-exam-container-rbc/4.10.0-SNAPSHOT",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.exam/pax-exam-inject/4.10.0-SNAPSHOT",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.hamcrest.core/1.3.0.1",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.junit/4.12.0.1",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.apache.geronimo.specs/geronimo-atinject_1.0_spec/1.0",
                carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.ops4j.base/ops4j-base/1.5.0", carbonHome + "/osgi/dropins");
        copyReferencedArtifactsToDeployDirectory("mvn:org.testng/testng/6.8.17", carbonHome + "/osgi/dropins");
    }

    private void copyReferencedArtifactsToDeployDirectory(String url, String targetDirectory) {
        File target = createUnique(url, new File(targetDirectory));
        try {
            FileUtils.copyURLToFile(new URL(url), target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy dependencies specified as ProvisionOption in system to the dropins Directory
     */
    private void copyReferencedBundles(Path carbonHome, ExamSystem system) {
        String targetDirectory = carbonHome + "/osgi/dropins";

        Arrays.asList(system.getOptions(ProvisionOption.class)).forEach(
                provisionOption -> copyReferencedArtifactsToDeployDirectory(provisionOption.getURL(), targetDirectory));
    }

    private void copyConfigurationFiles(Path carbonHome){
        Arrays.asList(system.getOptions(CarbonDistributionConfigurationFileReplacementOption.class)).forEach(option->{
            try {
                Files.copy(option.getSourcePath(),carbonHome.resolve(option.getDestinationPath()), StandardCopyOption
                        .REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void startCarbon(ExamSystem subsystem, Path carbonHome) throws IOException {
        long startedAt = System.currentTimeMillis();
        Path carbonBin = carbonHome.resolve("bin");
        makeScriptsInBinExec(carbonBin.toFile());
        ArrayList<String> javaOpts = new ArrayList<>();
        String[] environment = new String[]{};
        setupSystemProperties(javaOpts, subsystem);
        runner.exec(environment, carbonHome, javaOpts);
        waitForState(0, Bundle.ACTIVE, subsystem.getTimeout());

        logger.info("Test Container started in " + (System.currentTimeMillis() - startedAt) + " millis");
        logger.info("Wait for test container to finish its initialization " + subsystem.getTimeout());
    }

    private void setupSystemProperties(List<String> javaOpts, ExamSystem subsystem) throws IOException {
        Arrays.asList(subsystem.getOptions(SystemPropertyOption.class)).forEach(systemPropertyOption -> {
            String property = String.format("-D%s=%s", systemPropertyOption.getKey(), systemPropertyOption.getValue());
            javaOpts.add(property);
        });

        Arrays.asList(subsystem.getOptions(VMOption.class)).forEach(vmOption -> javaOpts.add(vmOption.getOption()));
    }

    private void makeScriptsInBinExec(File carbonBin) {
        if (!carbonBin.exists()) {
            return;
        }
        Arrays.asList(carbonBin.listFiles()).forEach(file -> file.setExecutable(true));
    }

    private Path retrieveFinalTargetDirectory() {
        Path unpackDirectory = carbonDistributionConfigurationOption.getUnpackDirectory();
        if (unpackDirectory == null) {
            unpackDirectory = Paths.get("target", UUID.randomUUID().toString());
        }
        unpackDirectory.toFile().mkdir();
        return unpackDirectory;
    }

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
        for (String a : prepend) {
            builder.append(a);
            builder.append(",");
        }
        for (ValueOption<?> option : options) {
            builder.append(option.getValue());
            builder.append(",");
        }
        for (String a : append) {
            builder.append(a);
            builder.append(",");
        }
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
                if (runner != null) {
                    runner.shutdown();
                }
                try {
                    UnicastRemoteObject.unexportObject(rgstry, true);
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

    private void waitForState(final long bundleId, final int state, final RelativeTimeout timeout) {
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
    public String toString() {
        return "CarbonTestContainer{" + carbonDistributionConfigurationOption.getDistributionDirectoryPath() + "}";
    }

    @Override
    public long installProbe(InputStream stream) {
        return target.installProbe(stream);
    }

    @Override
    public void uninstallProbe() {
        target.uninstallProbe();
    }
}
