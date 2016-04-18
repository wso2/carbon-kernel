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
package org.wso2.carbon.launcher.extensions;

import org.wso2.carbon.launcher.extensions.model.BundleInfo;
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
 * This class defines the utility functions used when implementing the dropins deployment capability.
 *
 * @since 5.1.0
 */
public class DropinsBundleDeployerUtils {
    private static final Logger logger = Logger.getLogger(DropinsBundleDeployerUtils.class.getName());
    private static final String dropinsDirectory = "dropins";
    private static final String addOnsDirectory = "osgi";

    /**
     * Updates the specified Carbon profile's bundles.info file based on the OSGi bundles deployed in the dropins
     * directory. The OSGi bundle information in the bundles.info file in a Carbon profile is used to install and
     * start the bundles at the server startup for the particular profile.
     * <p>
     * The mechanism used in updating the bundles.info file is as follows:
     * 1. The new OSGi bundle information from the bundles currently existing within the dropins folder are obtained.
     * 2. The existing OSGi dropins bundle information are compared with the newly retrieved bundle information and
     * the bundles.info file is updated only if the new bundle information are different from the existing.
     * 3. The new OSGi bundle information replace the existing dropins bundle information from the bundles.info file.
     * The OSGi bundle information of the non-dropins bundles, retrieved from the bundles.info file and the new dropins
     * OSGi bundle information are merged together.
     * 4. Updates the bundles.info file with the OSGi bundle information retrieved in step 3.
     *
     * @param carbonHome    the {@link String} representation of carbon.home
     * @param carbonProfile the bundles.info file to be updated
     * @throws IOException if an I/O error occurs
     */
    public static void executeDropinsCapability(String carbonHome, String carbonProfile) throws IOException {
        Path dropinsDirectoryPath = Paths.get(carbonHome, addOnsDirectory, dropinsDirectory);
        Path bundlesInfoFile = Paths.get(carbonHome, "osgi", "profiles", carbonProfile, "configuration",
                "org.eclipse.equinox.simpleconfigurator", "bundles.info");

        List<BundleInfo> newBundleInfo = getNewBundlesInfo(dropinsDirectoryPath);
        if (hasToUpdateBundlesInfoFile(newBundleInfo, bundlesInfoFile)) {
            List<BundleInfo> effectiveNewBundleInfo = mergeDropinsBundleInfo(newBundleInfo, bundlesInfoFile);
            updateBundlesInfo(effectiveNewBundleInfo, bundlesInfoFile);
            logger.log(Level.INFO, "Successfully updated the " + carbonProfile + "'s bundles.info file");
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
    public static List<BundleInfo> getNewBundlesInfo(Path sourceDirectory) throws IOException {
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
            throw new IOException("Invalid OSGi bundle source directory: " + sourceDirectory);
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
                                return Optional.empty();
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
            throw new IOException("Invalid OSGi bundle path: " + bundlePath);
        }
    }

    /**
     * Returns true if the specified bundles.info file requires to be updated, else false.
     * <p>
     * The OSGi bundle information specified are compared with the OSGi bundle information specified in the
     * existing bundles.info file, specified. If the OSGi bundle information of dropins bundles are matching,
     * there is no requirement to update the bundles.info file, again.
     *
     * @param newBundleInfo          the new OSGi bundle information
     * @param existingBundleInfoFile the existing bundles.info file to be checked
     * @return true if the specified bundles.info file requires to be updated, else false
     * @throws IOException if an I/O error occurs
     */
    public static boolean hasToUpdateBundlesInfoFile(List<BundleInfo> newBundleInfo, Path existingBundleInfoFile)
            throws IOException {
        if ((existingBundleInfoFile != null) && (Files.exists(existingBundleInfoFile))) {
            List<BundleInfo> existingBundlesInfo = new ArrayList<>();
            Files.readAllLines(existingBundleInfoFile).stream().filter(line -> !line.startsWith("#")).
                    map(BundleInfo::getInstance).
                    filter(BundleInfo::isFromDropins).forEach(existingBundlesInfo::add);

            long newBundleInfoCount = Optional.ofNullable(newBundleInfo).orElse(new ArrayList<>()).size();
            if (existingBundlesInfo.size() == newBundleInfoCount) {
                long nonMatchingBundleInfoCount = Optional.ofNullable(newBundleInfo).orElse(new ArrayList<>()).stream().
                        filter(info -> existingBundlesInfo.stream().filter(existingInfo -> existingInfo.equals(info)).
                                count() == 0).count();
                return nonMatchingBundleInfoCount > 0;
            } else {
                return true;
            }
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
    public static List<BundleInfo> mergeDropinsBundleInfo(List<BundleInfo> newBundleInfo, Path bundlesInfoFilePath)
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
     * Updates the specified bundles.info file with the specified OSGi bundle information.
     *
     * @param info                the OSGi bundle information instances
     * @param bundlesInfoFilePath the bundles.info file path, to be updated
     * @throws IOException if an I/O error occurs
     */
    private static void updateBundlesInfo(List<BundleInfo> info, Path bundlesInfoFilePath) throws IOException {
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

    /**
     * Returns a list of WSO2 Carbon Profile names.
     *
     * @param carbonHome the WSO2 Carbon home
     * @return a list of WSO2 Carbon Profile names
     * @throws IOException if an I/O error occurs
     */
    public static List<String> getCarbonProfiles(String carbonHome) throws IOException {
        Path carbonProfilesHome = Paths.get(carbonHome, addOnsDirectory, "profiles");
        if (Files.exists(carbonProfilesHome)) {
            Stream<Path> profiles = Files.list(carbonProfilesHome);
            List<String> profileNames = new ArrayList<>();

            profiles.parallel().forEach(profile -> {
                Path profileName = profile.getFileName();
                Optional.ofNullable(profileName).ifPresent(name -> profileNames.add(name.toString()));
            });

            return profileNames;
        } else {
            throw new IOException("The " + carbonHome + "/" + addOnsDirectory + "/profiles directory does not exist");
        }
    }

    /**
     * Returns a list of WSO2 Carbon Profile names.
     * <p>
     * This method can be used when the Carbon server is running along with the carbon.home system property set.
     *
     * @return a list of WSO2 Carbon Profile names
     * @throws IOException if an I/O error occurs
     */
    static List<String> getCarbonProfiles() throws IOException {
        return getCarbonProfiles(Utils.getCarbonHomeDirectory().toString());
    }
}
