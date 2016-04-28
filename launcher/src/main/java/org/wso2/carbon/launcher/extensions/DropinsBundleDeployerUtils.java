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

import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.model.BundleInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class defines the utility functions used when implementing the dropins deployment capability.
 *
 * @since 5.1.0
 */
public class DropinsBundleDeployerUtils {
    private static final Logger logger = Logger.getLogger(DropinsBundleDeployerUtils.class.getName());

    private static List<BundleInfo> newBundlesInfo;

    /**
     * Updates the specified Carbon profile's bundles.info file based on the OSGi bundles deployed in the dropins
     * directory. The OSGi bundle information in the bundles.info file in a Carbon profile is used to install and
     * start the bundles at the server startup for the particular profile.
     * <p>
     * The mechanism used in updating the bundles.info file is as follows:
     * 1. The new OSGi bundle information from the bundles currently existing within the dropins folder are obtained.
     * The new OSGi bundle information are read only once for updating one or more Carbon profiles.
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
    public static synchronized void executeDropinsCapability(String carbonHome, String carbonProfile)
            throws IOException {
        Path dropinsDirectoryPath = Paths.get(carbonHome, Constants.OSGI_REPOSITORY, Constants.DROPINS);
        Path bundlesInfoFile = Paths.
                get(carbonHome, Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH, carbonProfile, "configuration",
                        "org.eclipse.equinox.simpleconfigurator", Constants.BUNDLES_INFO);

        if (newBundlesInfo == null) {
            logger.log(Level.FINE, "Loading the new OSGi bundle information from " + Constants.DROPINS + " folder...");
            newBundlesInfo = getNewBundlesInfo(dropinsDirectoryPath);
            logger.log(Level.FINE, "Successfully loaded the new OSGi bundle information from " + Constants.DROPINS +
                    " folder");
        } else {
            logger.log(Level.FINE, "The OSGi bundle information from " + Constants.DROPINS + " folder are " +
                    "already loaded");
        }

        if (hasToUpdateBundlesInfoFile(newBundlesInfo, bundlesInfoFile)) {
            logger.log(Level.INFO, "New file changes detected in " + Constants.DROPINS + " folder");

            List<BundleInfo> effectiveNewBundleInfo = mergeDropinsBundleInfo(newBundlesInfo, bundlesInfoFile);

            logger.log(Level.INFO, "Updating the OSGi bundle information of Carbon Profile: " + carbonProfile + "...");
            updateBundlesInfo(effectiveNewBundleInfo, bundlesInfoFile);
            logger.log(Level.INFO,
                    "Successfully updated the OSGi bundle information of Carbon Profile: " + carbonProfile);
        } else {
            logger.log(Level.INFO, "No changes detected in the dropins directory in comparison with the profile, " +
                    "skipped the OSGi bundle information update for Carbon Profile: " + carbonProfile);
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
            children
                    .parallel()
                    .forEach(child -> {
                        try {
                            logger.log(Level.FINE, "Loading OSGi bundle information from " + child + "...");
                            getNewBundleInfo(child).ifPresent(bundleInfo -> {
                                if (!bundleInfoExists(bundleInfo, newBundleInfoLines)) {
                                    newBundleInfoLines.add(bundleInfo);
                                }
                            });
                            logger.log(Level.FINE, "Successfully loaded OSGi bundle information from " + child);
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "Error when loading the OSGi bundle information from " + child,
                                    e);
                        }
                    });
        } else {
            throw new IOException("Invalid or non-existent OSGi bundle source directory: " + sourceDirectory);
        }

        return newBundleInfoLines;
    }

    /**
     * Constructs a {@code BundleInfo} instance out of the OSGi bundle file path specified.
     * <p>
     * If the specified file path refers to a non-Java Archive (JAR) file, no {@code BundleInfo} instance will be
     * created.
     *
     * @param bundlePath path to the OSGi bundle from which the {@link BundleInfo} is to be generated
     * @return a {@link BundleInfo} instance
     * @throws IOException if an I/O error occurs or if an invalid {@code bundlePath} is found
     */
    private static Optional<BundleInfo> getNewBundleInfo(Path bundlePath) throws IOException {
        if ((bundlePath != null) && (Files.exists(bundlePath))) {
            Path bundleFileName = bundlePath.getFileName();
            if (bundleFileName == null) {
                throw new IOException("Specified OSGi bundle file name is null: " + bundlePath);
            } else {
                String fileName = bundleFileName.toString();
                if (fileName.endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(bundlePath.toString())) {
                        Manifest manifest = jarFile.getManifest();
                        if ((manifest == null) || (manifest.getMainAttributes() == null)) {
                            throw new IOException("Invalid OSGi bundle found in the " + Constants.DROPINS + " folder");
                        } else {
                            String bundleSymbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
                            String bundleVersion = manifest.getMainAttributes().getValue("Bundle-Version");

                            if (bundleSymbolicName == null || bundleVersion == null) {
                                throw new IOException("Required bundle manifest headers do not exist");
                            } else {
                                if (bundleSymbolicName.contains(";")) {
                                    bundleSymbolicName = bundleSymbolicName.split(";")[0];
                                }
                            }

                            //  checks whether this bundle is a fragment or not
                            boolean isFragment = (manifest.getMainAttributes().getValue("Fragment-Host") != null);
                            int defaultBundleStartLevel = 4;
                            BundleInfo generated = new BundleInfo(bundleSymbolicName, bundleVersion,
                                    "../../" + Constants.DROPINS + "/" + fileName, defaultBundleStartLevel, isFragment);
                            return Optional.of(generated);
                        }
                    }
                } else {
                    return Optional.empty();
                }
            }
        } else {
            throw new IOException("Invalid or non-existent OSGi bundle path: " + bundlePath);
        }
    }

    /**
     * Returns true if the specified bundles.info file requires to be updated, else false.
     * <p>
     * The OSGi bundle information specified are compared with the OSGi bundle information specified in the
     * existing bundles.info file, specified. If the OSGi bundle information of dropins bundles are matching,
     * there is no requirement to update the bundles.info file, again.
     *
     * @param newBundlesInfo          the new OSGi bundle information
     * @param existingBundlesInfoFile the existing bundles.info file to be checked
     * @return true if the specified bundles.info file requires to be updated, else false
     * @throws IOException if an I/O error occurs
     */
    public static boolean hasToUpdateBundlesInfoFile(List<BundleInfo> newBundlesInfo, Path existingBundlesInfoFile)
            throws IOException {
        if ((existingBundlesInfoFile != null) && (Files.exists(existingBundlesInfoFile))) {
            List<BundleInfo> existingDropinsBundleInfo = Files.readAllLines(existingBundlesInfoFile)
                    .stream()
                    .filter(line -> !line.startsWith("#"))
                    .map(BundleInfo::getInstance)
                    .filter(BundleInfo::isFromDropins)
                    .collect(Collectors.toList());

            List<BundleInfo> newBundleInfoList = Optional.ofNullable(newBundlesInfo).orElse(new ArrayList<>());
            if (existingDropinsBundleInfo.size() == newBundleInfoList.size()) {
                long nonMatchingBundleInfoCount = newBundleInfoList
                        .stream()
                        .filter(bundleInfo -> !bundleInfoExists(bundleInfo, existingDropinsBundleInfo))
                        .count();
                return nonMatchingBundleInfoCount > 0;
            } else {
                return true;
            }
        } else {
            throw new IOException("Invalid or non-existent file path: " + existingBundlesInfoFile);
        }
    }

    /**
     * Merges the information on the current set of OSGi bundle(s) that may reside within the dropins folder.
     * <p>
     * Each new OSGi bundle is checked if it is already defined within the specified, existing bundles.info
     * file from any other bundle locations (non-dropins)
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
            List<BundleInfo> effectiveBundleInfo = Files.readAllLines(bundlesInfoFilePath)
                    .stream()
                    .filter(line -> !line.startsWith("#"))
                    .map(BundleInfo::getInstance)
                    //  filter all non-dropins, OSGi bundle information
                    .filter(info -> !info.isFromDropins())
                    .collect(Collectors.toList());

            newBundleInfo
                    .stream()
                    //  filter OSGi bundles of dropins directory, which are not equal to any non-dropins, OSGi bundles
                    .filter(bundle -> !bundleInfoExists(bundle, effectiveBundleInfo))
                    .forEach(effectiveBundleInfo::add);

            return effectiveBundleInfo;
        } else {
            throw new IOException("Invalid or non-existing file path: " + bundlesInfoFilePath);
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
                info.stream()
                        .forEach(information -> bundleInfoLines.add(information.toString()));
                Files.write(bundlesInfoFilePath, bundleInfoLines);
            }
        } else {
            throw new IOException("Invalid or non-existing file path: " + bundlesInfoFilePath);
        }
    }

    /**
     * Returns whether the specified OSGi bundle information matches with any of the OSGi bundle information
     * within the specified list.
     *
     * @param bundleInfo  the OSGi bundle of which the information are to be compared
     * @param bundlesInfo a list of OSGi bundle information
     * @return true if the specified OSGi bundle information matches with any of the OSGi bundle information
     * within the specified list, else false
     */
    public static boolean bundleInfoExists(BundleInfo bundleInfo, List<BundleInfo> bundlesInfo) {
        return (bundleInfo != null) &&
                Optional.ofNullable(bundlesInfo).orElse(new ArrayList<>())
                        .stream()
                        .anyMatch(bundle -> (bundleInfo.equals(bundle)));
    }

    /**
     * Returns a list of WSO2 Carbon Profile names.
     *
     * @param carbonHome the WSO2 Carbon home
     * @return a list of WSO2 Carbon Profile names
     * @throws IOException if an I/O error occurs
     */
    public static List<String> getCarbonProfiles(String carbonHome) throws IOException {
        Path carbonProfilesHome = Paths.get(carbonHome, Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH);
        if (Files.exists(carbonProfilesHome)) {
            Stream<Path> profiles = Files.list(carbonProfilesHome);
            List<String> profileNames = new ArrayList<>();

            profiles
                    .parallel()
                    .forEach(profile -> Optional.ofNullable(profile.getFileName())
                            .ifPresent(name -> profileNames.add(name.toString())));

            return profileNames;
        } else {
            throw new IOException("The " + carbonHome + "/" + Constants.OSGI_REPOSITORY + "/" + Constants.PROFILE_PATH +
                    " directory does not exist");
        }
    }
}
