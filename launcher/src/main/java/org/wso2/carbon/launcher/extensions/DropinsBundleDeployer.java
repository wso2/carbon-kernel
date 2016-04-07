/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.launcher.extensions;

import org.wso2.carbon.launcher.CarbonServerEvent;
import org.wso2.carbon.launcher.CarbonServerListener;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * DropinsBundleDeployer deploys the OSGi bundles in CARBON_HOME/osgi/dropins folder by writing
 * the OSGi bundle information to the bundles.info file.
 *
 * @since 5.1.0
 */
public class DropinsBundleDeployer implements CarbonServerListener {
    private static final Logger logger = BootstrapLogger.getCarbonLogger(DropinsBundleDeployer.class.getName());
    private static final String addOnsDirectory = "osgi";
    private static final String dropinsDirectory = "dropins";

    @Override
    public void notify(CarbonServerEvent event) {
        if (event.getType() == CarbonServerEvent.STARTING) {
            try {
                revampBundlesInfo();
            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "An error has occurred when updating the bundles.info using the OSGi bundle information", e);
            }
        }
    }

    /**
     * Revamps the bundles.info file based on the OSGi bundles deployed in the dropins directory.
     * <p>
     * The mechanism used in revamping the bundles.info file is as follows:
     * 1. The new OSGi bundle information from the bundles currently existing within the dropins folder are obtained.
     * 2. The new OSGi bundle information replace the existing dropins bundle information from the bundles.info file.
     * The OSGi bundle information of the non-dropins bundles, retrieved from the bundles.info file and the new dropins
     * OSGi bundle information are merged together.
     * 3. Updates the bundles.info file with the OSGi bundle information retrieved in step 2.
     *
     * @throws IOException if an I/O error occurs
     */
    public static void revampBundlesInfo() throws IOException {
        Path dropinsDirectoryPath = Paths.
                get(Utils.getCarbonHomeDirectory().toString(), addOnsDirectory, dropinsDirectory);
        String profileName = System.getProperty(Constants.PROFILE, Constants.DEFAULT_PROFILE);
        Path bundlesInfoFilePath = Paths.
                get(Utils.getCarbonHomeDirectory().toString(), addOnsDirectory, profileName, "configuration",
                        "org.eclipse.equinox.simpleconfigurator", "bundles.info");

        List<BundleInfo> newBundleInfo = getNewBundlesInfo(dropinsDirectoryPath);
        if(hasToUpdateBundlesInfoFile(newBundleInfo, bundlesInfoFilePath)) {
            List<BundleInfo> effectiveNewBundleInfo = mergeDropinsBundleInfo(newBundleInfo, bundlesInfoFilePath);
            updateDropinsBundleFile(effectiveNewBundleInfo, bundlesInfoFilePath);
        } else {
            logger.log(Level.INFO, "No changes detected in the dropins directory, skipping the bundles.info update");
        }
    }

    /**
     * Scans through the specified directory and constructs corresponding {@code BundleInfo} instances.
     *
     * @param sourceDirectory the source folder in which the OSGi bundles reside
     * @return the constructed {@link BundleInfo} instances list
     * @throws IOException if an I/O error occurs or if the {@code sourceDirectory} is invalid
     */
    private static List<BundleInfo> getNewBundlesInfo(Path sourceDirectory) throws IOException {
        List<BundleInfo> newBundleInfoLines = new ArrayList<>();
        if ((sourceDirectory != null) && (Files.exists(sourceDirectory))) {
            Stream<Path> children = Files.list(sourceDirectory);
            children.parallel().forEach(child -> {
                try {
                    Optional<BundleInfo> newBundleInfo = getNewBundleInfo(child);
                    if (newBundleInfo.isPresent()) {
                        newBundleInfoLines.add(newBundleInfo.get());
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error when loading OSGi bundle info from " + child.toString(), e);
                }
            });
        } else {
            throw new IOException("Invalid OSGi bundle source directory");
        }

        return newBundleInfoLines;
    }

    /**
     * Constructs a {@code BundleInfo} instance out of the OSGi bundle file path specified.
     *
     * @param bundlePath path to the OSGi bundle from which the {@link BundleInfo} is to be generated
     * @return a {@link BundleInfo} instance
     * @throws IOException if an I/O error occurs or if an invalid {@code bundlePath} is found
     */
    private static Optional<BundleInfo> getNewBundleInfo(Path bundlePath) throws IOException {
        if ((bundlePath != null) && (Files.exists(bundlePath))) {
            Path bundleFileName = bundlePath.getFileName();
            if (bundleFileName == null) {
                return Optional.empty();
            } else {
                String fileName = bundleFileName.toString();
                if (fileName.endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(bundlePath.toString())) {
                        if ((jarFile.getManifest() == null) || (jarFile.getManifest().getMainAttributes() == null)) {
                            throw new IOException("Invalid bundle found in the " + dropinsDirectory + " directory: " +
                                    jarFile.toString());
                        } else {
                            String bundleSymbolicName = jarFile.getManifest().getMainAttributes().
                                    getValue("Bundle-SymbolicName");
                            String bundleVersion = jarFile.getManifest().getMainAttributes().getValue("Bundle-Version");
                            if (bundleSymbolicName == null || bundleVersion == null) {
                                logger.log(Level.WARNING,
                                        "Required bundle manifest headers do not exists: " + jarFile.toString());
                            } else {
                                if (bundleSymbolicName.contains(";")) {
                                    bundleSymbolicName = bundleSymbolicName.split(";")[0];
                                }
                            }
                            //  checks whether this bundle is a fragment or not
                            boolean isFragment = (jarFile.getManifest().getMainAttributes().getValue("Fragment-Host")
                                    != null);
                            int defaultBundleStartLevel = 4;
                            BundleInfo generated = new BundleInfo(bundleSymbolicName, bundleVersion,
                                    "../../" + dropinsDirectory + "/" + fileName, defaultBundleStartLevel, isFragment);
                            return Optional.of(generated);
                        }
                    }
                } else {
                    return Optional.empty();
                }
            }
        } else {
            throw new IOException("Invalid OSGi bundle path");
        }
    }

    private static boolean hasToUpdateBundlesInfoFile(List<BundleInfo> newBundleInfo, Path existingBundleInfoFile)
            throws IOException {
        if ((existingBundleInfoFile != null) && (Files.exists(existingBundleInfoFile))) {
            List<BundleInfo> existingBundlesInfo = new ArrayList<>();
            Files.readAllLines(existingBundleInfoFile).stream().filter(line -> !line.startsWith("#")).
                    map(BundleInfo::getInstance).
                    filter(BundleInfo::isFromDropins).forEach(existingBundlesInfo::add);

            return Optional.ofNullable(newBundleInfo).orElse(new ArrayList<>()).stream().filter(
                    info -> existingBundlesInfo.stream().filter(existingInfo -> existingInfo.equals(info)).count() > 0).count() > 0;
        } else {
            throw new IOException("Invalid file path specified " + existingBundleInfoFile);
        }
    }

    /**
     * Merges the information on the current set of OSGi bundle(s) that may reside within the dropins folder.
     *
     * @param newBundleInfo       the OSGi bundle information on the current set of bundles that reside within the
     *                            dropins folder
     * @param bundlesInfoFilePath the bundles.info file path from which existing OSGi bundle information are to be
     *                            loaded
     * @return the effective group of OSGi bundle information
     * @throws IOException if an I/O error occurs or if the dropins directory does not exist
     */
    private static List<BundleInfo> mergeDropinsBundleInfo(List<BundleInfo> newBundleInfo, Path bundlesInfoFilePath)
            throws IOException {
        if ((bundlesInfoFilePath != null) && (Files.exists(bundlesInfoFilePath))) {
            List<BundleInfo> effectiveBundleInfo = new ArrayList<>();
            Files.readAllLines(bundlesInfoFilePath).stream().filter(line -> !line.startsWith("#")).
                    map(BundleInfo::getInstance).
                    filter(info -> !info.isFromDropins()).forEach(effectiveBundleInfo::add);
            newBundleInfo.stream().forEach(effectiveBundleInfo::add);

            return effectiveBundleInfo;
        } else {
            throw new IOException("Dropins directory does not exist");
        }
    }

    /**
     * Updates the bundles.info file with the specified OSGi bundle information.
     *
     * @param info                the OSGi bundle information instances
     * @param bundlesInfoFilePath the bundles.info file path, to be updated
     * @throws IOException if an I/O error occurs
     */
    private static void updateDropinsBundleFile(List<BundleInfo> info, Path bundlesInfoFilePath) throws IOException {
        if ((bundlesInfoFilePath != null) && (Files.exists(bundlesInfoFilePath))) {
            Files.delete(bundlesInfoFilePath);
            if (info != null) {
                List<String> bundleInfoLines = new ArrayList<>();
                info.stream().forEach(information -> bundleInfoLines.add(information.toString()));
                Files.write(bundlesInfoFilePath, bundleInfoLines);
            }
        } else {
            throw new IOException("Invalid file path " + bundlesInfoFilePath);
        }
    }
}
