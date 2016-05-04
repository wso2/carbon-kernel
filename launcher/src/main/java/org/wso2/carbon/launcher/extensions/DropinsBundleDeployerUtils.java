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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    //  TODO: update comments

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
    public static synchronized void installDropins(String carbonHome, String carbonProfile,
            List<BundleInfo> bundlesInfo) throws IOException {
        //  validate the arguments provided
        if ((carbonHome == null) || (carbonHome.isEmpty())) {
            throw new IllegalArgumentException("Carbon home specified is invalid");
        }

        if ((carbonProfile == null) || (carbonProfile.isEmpty())) {
            throw new IllegalArgumentException("Carbon Profile specified is invalid");
        }

        if (bundlesInfo == null) {
            throw new IllegalArgumentException("No new OSGi bundle information specified, for updating the " +
                    "Carbon Profile: " + carbonProfile);
        }

        Path bundlesInfoFile = Paths.get(carbonHome, Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH,
                carbonProfile, "configuration", "org.eclipse.equinox.simpleconfigurator", Constants.BUNDLES_INFO);
        //  retrieves the OSGi bundle information defined in the existing bundles.info file
        List<BundleInfo> existingBundlesInfo = Files.readAllLines(bundlesInfoFile)
                .stream()
                .filter(line -> !line.startsWith("#"))
                .map(BundleInfo::getInstance)
                .collect(Collectors.toList());


        Map<BundleInfo, Boolean> updatableBundles = getUpdatableBundles(bundlesInfo, existingBundlesInfo);

        if (updatableBundles.size() > 0) {
            logger.log(Level.FINE, "New file changes detected in " + Constants.DROPINS + " folder");

            List<BundleInfo> effectiveNewBundleInfo = mergeDropinsBundleInfo(updatableBundles, existingBundlesInfo);

            logger.log(Level.FINE, "Updating the OSGi bundle information of Carbon Profile: " + carbonProfile + "...");
            updateBundlesInfo(effectiveNewBundleInfo, bundlesInfoFile);
            logger.log(Level.INFO,
                    "Successfully updated the OSGi bundle information of Carbon Profile: " + carbonProfile);
        } else {
            logger.log(Level.FINE, "No changes detected in the dropins directory in comparison with the profile, " +
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
    public static List<BundleInfo> getBundlesInfo(Path sourceDirectory) throws IOException {
        if ((sourceDirectory == null) || (!Files.exists(sourceDirectory))) {
            throw new IOException("Invalid OSGi bundle source directory. The specified path may not exist or " +
                    "user may not have required file permissions for the specified path: " + sourceDirectory);
        }

        return Files.list(sourceDirectory)
                .map(child -> {
                    BundleInfo bundleInfo = null;
                    try {
                        bundleInfo = getBundleInfo(child).orElse(null);
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Error when loading the OSGi bundle information from " + child, e);
                    }
                    return bundleInfo;
                })
                .distinct()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Constructs a {@code BundleInfo} instance out of the OSGi bundle file path specified.
     * <p>
     * If the specified file path does not satisfy the requirements of an OSGi bundle, no {@code BundleInfo} instance
     * will be created.
     *
     * @param bundlePath path to the OSGi bundle from which the {@link BundleInfo} is to be generated
     * @return a {@link BundleInfo} instance
     * @throws IOException if an I/O error occurs or if an invalid {@code bundlePath} is found
     */
    private static Optional<BundleInfo> getBundleInfo(Path bundlePath) throws IOException {
        if ((bundlePath == null) || (!Files.exists(bundlePath))) {
            throw new IOException("Invalid OSGi bundle path. The specified path may not exist or " +
                    "user may not have required file permissions for the specified path");
        }

        Path bundleFileName = bundlePath.getFileName();
        if (bundleFileName == null) {
            throw new IOException("Specified OSGi bundle file name is null: " + bundlePath);
        }

        String fileName = bundleFileName.toString();
        if (!fileName.endsWith(".jar")) {
            return Optional.empty();
        }

        try (JarFile jarFile = new JarFile(bundlePath.toString())) {
            Manifest manifest = jarFile.getManifest();

            if ((manifest == null) || (manifest.getMainAttributes() == null)) {
                throw new IOException("Invalid OSGi bundle found in the " + Constants.DROPINS + " folder");
            }

            String bundleSymbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
            String bundleVersion = manifest.getMainAttributes().getValue("Bundle-Version");

            if (bundleSymbolicName == null || bundleVersion == null) {
                throw new IOException("Required bundle manifest headers do not exist");
            }

            logger.log(Level.FINE,
                    "Loading information from OSGi bundle: " + bundleSymbolicName + ":" + bundleVersion + "...");

            if (bundleSymbolicName.contains(";")) {
                bundleSymbolicName = bundleSymbolicName.split(";")[0];
            }

            //  checks whether this bundle is a fragment or not
            boolean isFragment = (manifest.getMainAttributes().getValue("Fragment-Host") != null);
            int defaultBundleStartLevel = 4;
            BundleInfo generated = new BundleInfo(bundleSymbolicName, bundleVersion,
                    "../../" + Constants.DROPINS + "/" + fileName, defaultBundleStartLevel, isFragment);
            logger.log(Level.FINE,
                    "Successfully loaded information from OSGi bundle: " + bundleSymbolicName + ":" + bundleVersion);
            return Optional.of(generated);
        }
    }

    /**
     * Returns the OSGi bundles information which are to be either added or removed from the existing set of bundle
     * information, in order to bring the existing bundle information up-to-date with new bundle information.
     * <p>
     * This method returns a group of key-value pairs - the key indicates the OSGi bundle and the corresponding value
     * indicates whether to add (true) or remove (false) the bundle information.
     *
     * @param newBundlesInfo     the new OSGi bundle information
     * @param existingBundleInfo the existing OSGi bundle information
     * @return a group of key-value pairs - the key indicates the updatable OSGi bundle and the corresponding value
     * indicates whether to add (true) or remove (false) the bundle information
     */
    public static Map<BundleInfo, Boolean> getUpdatableBundles(List<BundleInfo> newBundlesInfo,
            List<BundleInfo> existingBundleInfo) {
        if ((newBundlesInfo == null) || (existingBundleInfo == null)) {
            throw new IllegalArgumentException("The list of OSGi bundle information cannot be null");
        }

        Map<BundleInfo, Boolean> updatableBundles = new HashMap<>();

        //  gets existing dropins OSGi bundle information
        List<BundleInfo> existingDropinsBundleInfo = existingBundleInfo
                .stream()
                .filter(BundleInfo::isFromDropins)
                .collect(Collectors.toList());

        //  retrieves new OSGi bundle information absent in the existing bundles.info file
        newBundlesInfo
                .stream()
                .filter(bundleInfo -> !existingBundleInfo.contains(bundleInfo))
                .forEach(bundleInfo -> updatableBundles.put(bundleInfo, true));

        //  retrieves the information of OSGi bundles to be removed from the bundles.info file
        existingDropinsBundleInfo
                .stream()
                .filter(bundleInfo -> !newBundlesInfo.contains(bundleInfo))
                .forEach(bundleInfo -> updatableBundles.put(bundleInfo, false));

        return updatableBundles;
    }

    //  TODO: update the Javadoc comments
    public static List<BundleInfo> mergeDropinsBundleInfo(Map<BundleInfo, Boolean> updatableBundleInfo,
            List<BundleInfo> existingBundleInfo) throws IOException {
        if ((updatableBundleInfo == null) || (existingBundleInfo == null)) {
            throw new IllegalArgumentException("The list of updatable OSGi bundle information cannot be null");
        }

        //  adds the newly added OSGi bundle information
        List<BundleInfo> effectiveBundlesInfo = updatableBundleInfo.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(true))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        //  removes the existing OSGi bundle information of bundles which have been removed
        updatableBundleInfo.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(false))
                .map(Map.Entry::getKey)
                .forEach(existingBundleInfo::remove);

        effectiveBundlesInfo.addAll(existingBundleInfo);

        return effectiveBundlesInfo;
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
            throw new IOException("Invalid file path. The specified path may not exist or " +
                    "user may not have required file permissions for the specified path: " + bundlesInfoFilePath);
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
