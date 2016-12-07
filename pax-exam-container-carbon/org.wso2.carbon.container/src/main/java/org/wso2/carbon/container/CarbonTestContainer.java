/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
import org.wso2.carbon.container.options.CarbonDistributionBaseOption;
import org.wso2.carbon.container.options.CopyFileOption;
import org.wso2.carbon.container.options.CopyOSGiLibBundleOption;
import org.wso2.carbon.container.options.DebugOption;
import org.wso2.carbon.container.options.KeepDirectoryOption;
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
    private static final String LIB_DIRECTORY = "lib";

    private final Runner runner;
    private final ExamSystem system;
    private CarbonDistributionBaseOption carbonHomeDirectoryOption;
    private RBCRemoteTarget target;
    private Path targetDirectory;
    private Registry registry;
    private boolean started;

    public CarbonTestContainer(ExamSystem system, CarbonDistributionBaseOption carbonHomeDirectoryOption) {
        this.carbonHomeDirectoryOption = carbonHomeDirectoryOption;
        this.system = system;
        this.runner = new CarbonRunner();
    }

    public synchronized TestContainer start() {
        if (carbonHomeDirectoryOption.getDistributionDirectoryPath() == null
                && carbonHomeDirectoryOption.getDistributionMavenURL() == null
                && carbonHomeDirectoryOption.getDistributionZipPath() == null) {
            throw new TestContainerException("Distribution path need to be set.");
        }
        try {
            String name = system.createID(CARBON_TEST_CONTAINER);
            //get a free port to use for rmi
            FreePort freePort = new FreePort(21000, 21099);
            int port = freePort.getPort();
            logger.debug("using RMI registry at port {}" + name, port);
            registry = LocateRegistry.createRegistry(port);
            String host = InetAddress.getLocalHost().getHostName();

            //Setting RMI related properties
            ExamSystem subsystem = system.fork(options(systemProperty(RMI_HOST_PROPERTY).value(host),
                    systemProperty(RMI_PORT_PROPERTY).value(Integer.toString(port)),
                    systemProperty(RMI_NAME_PROPERTY).value(name), systemProperty(EXAM_INJECT_PROPERTY).value("true")));

            target = new RBCRemoteTarget(name, port, subsystem.getTimeout());
            System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");

            //setup repositories if there are any
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

            //install bundles if there are any
            copyOSGiLibBundles(targetDirectory);

            //copy files to the distributions if there are any
            copyFiles(targetDirectory);
            Path carbonBin = targetDirectory.resolve("bin");

            //make the files in the bin directory to be executable
            makeFilesInBinExec(carbonBin.toFile());
            List<String> options = new ArrayList<>();
            String[] environment = new String[] {};

            //set system properties as command line arguments
            setupSystemProperties(options, subsystem);

            //Setup debug configurations if available
            DebugOption debugOption = system.getSingleOption(DebugOption.class);
            if (debugOption != null) {
                options.add(debugOption.getDebugConfiguration());
            }
            runner.exec(environment, targetDirectory, options);
            logger.debug("Wait for test container to finish its initialization " + subsystem.getTimeout());

            //wait for the osgi environment to be active
            waitForState(0, Bundle.ACTIVE, subsystem.getTimeout());
            started = true;
        } catch (IOException e) {
            throw new TestContainerException("Problem starting container", e);
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
     * Copy dependencies specified as carbon OSGi-lib option in system to the LIB_DIRECTORY.
     *
     * @param carbonHome carbon home dir
     */
    private void copyOSGiLibBundles(Path carbonHome) {
        Path targetDirectory = carbonHome.resolve(LIB_DIRECTORY);

        Arrays.asList(system.getOptions(CopyOSGiLibBundleOption.class)).forEach(option -> {
            try {
                copyReferencedArtifactsToDeployDirectory(option.getMavenArtifactUrlReference().getURL(),
                        targetDirectory);
            } catch (IOException e) {
                throw new TestContainerException(String.format("Error while copying artifacts to " + LIB_DIRECTORY), e);
            }
        });
    }

    /**
     * Helper method to copy artifacts to the target directory.
     *
     * @param url             url of the artifact
     * @param targetDirectory target directory
     */
    private void copyReferencedArtifactsToDeployDirectory(String url, Path targetDirectory) throws IOException {
        File target = createUnique(url, targetDirectory.toFile());
        FileUtils.copyURLToFile(new URL(url), target);
    }

    /**
     * Create unique id to the file.
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
        Arrays.asList(system.getOptions(CopyFileOption.class)).forEach(option -> {
            try {
                Files.copy(option.getSourcePath(), carbonHome.resolve(option.getDestinationPath()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new TestContainerException("Error while copying configuration files", e);
            }
        });
    }

    /**
     * Setup system properties.
     *
     * @param options options
     * @throws IOException
     */
    private void setupSystemProperties(List<String> options, ExamSystem examSystem) throws IOException {
        Arrays.asList(examSystem.getOptions(SystemPropertyOption.class)).forEach(systemPropertyOption -> {
            String property = String.format("-D%s=%s", systemPropertyOption.getKey(), systemPropertyOption.getValue());
            options.add(property);
        });
    }

    /**
     * Make all the files in the bin directory to be executable.
     *
     * @param carbonBin carbonBin
     */
    private void makeFilesInBinExec(File carbonBin) {
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
        boolean isCreated = unpackDirectory.toFile().exists() || unpackDirectory.toFile().mkdir();
        if (!isCreated) {
            throw new TestContainerException("Couldn't create the directory: " + unpackDirectory.toFile().toString());
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
        KeepDirectoryOption keepDirectoryOption = system.getSingleOption(KeepDirectoryOption.class);
        if (keepDirectoryOption != null) {
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
                throw new TestContainerException("Container never started.");
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
