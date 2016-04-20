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
import org.wso2.carbon.launcher.utils.Utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * DropinsBundleDeployer deploys the OSGi bundles in CARBON_HOME/osgi/dropins folder by writing
 * the OSGi bundle information to the bundles.info file.
 * <p>
 *
 * @since 5.0.0
 */
public class DropinsBundleDeployer implements CarbonServerListener {

    private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    private static final String BUNDLE_VERSION = "Bundle-Version";
    private static final String FRAGMENT_HOST = "Fragment-Host";
    private static final int DEFAULT_BUNDLE_START_LEVEL = 4;

    private static final Logger logger = Logger.getLogger(DropinsBundleDeployer.class.getName());

    /**
     * Receives notification of a CarbonServerEvent.
     *
     * @param event the CarbonServerEvent instance
     */
    @Override
    public void notify(CarbonServerEvent event) {
        if (event.getType() == CarbonServerEvent.STARTING) {
            String profileName = System.getProperty(Constants.PROFILE, Constants.DEFAULT_PROFILE);
            Path dropinsDirectory = Paths.get(Utils.getCarbonHomeDirectory().toString(), "osgi", "dropins");
            try {
                if (Files.exists(dropinsDirectory)) {
                    List<BundleInfo> newBundleInfoLines = getNewBundleInfoLines(dropinsDirectory);
                    Path bundleInfoDirectory = Paths.
                            get(Utils.getCarbonHomeDirectory().toString(), "osgi", Constants.PROFILE_PATH, profileName,
                                    "configuration", "org.eclipse.equinox.simpleconfigurator");
                    Path bundleInfoFile = Paths.get(bundleInfoDirectory.toString(), "bundles.info");
                    Map<String, List<BundleInfo>> bundleInfoLineMap = processBundleInfoFile(bundleInfoFile,
                            newBundleInfoLines);
                    addNewBundleInfoLines(newBundleInfoLines, bundleInfoLineMap);
                    updateBundlesInfoFile(bundleInfoFile, bundleInfoLineMap);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "An error has occurred when updating the bundles.info using the OSGi " + "bundle information",
                        e);
            }
        }
    }

    /**
     * Scans through the dropins directory and constructs corresponding {@code BundleInfo} instances.
     *
     * @param sourceBundleDirectory the source directory which contains the OSGi bundles
     * @return a {@link List} of {@link BundleInfo} instances
     * @throws IOException if an I/O error occurs
     */
    private static List<BundleInfo> getNewBundleInfoLines(Path sourceBundleDirectory) throws IOException {
        List<BundleInfo> existingBundleInfoLines = new ArrayList<>();
        Stream<Path> children = Files.list(sourceBundleDirectory);

        children.forEach(child -> {
            Path childFileName = child.getFileName();
            if (childFileName != null) {
                if (childFileName.toString().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(child.toString())) {
                        if ((jarFile.getManifest() == null) || (jarFile.getManifest().getMainAttributes() == null)) {
                            logger.log(Level.WARNING,
                                    "Invalid bundle found in the dropins directory: " + jarFile.toString());
                        } else {
                            String bundleSymbolicName = jarFile.getManifest().getMainAttributes().
                                    getValue(BUNDLE_SYMBOLIC_NAME);
                            String bundleVersion = jarFile.getManifest().getMainAttributes().
                                    getValue(BUNDLE_VERSION);
                            if (bundleSymbolicName == null || bundleVersion == null) {
                                logger.log(Level.WARNING,
                                        "Required bundle manifest headers do not exists: " + jarFile.toString());
                            } else {
                                //  BSN can have values like, Bundle-SymbolicName: com.example.acme;singleton:=true
                                //  refer - http://wiki.osgi.org/wiki/Bundle-SymbolicName for more details
                                if (bundleSymbolicName.contains(";")) {
                                    bundleSymbolicName = bundleSymbolicName.split(";")[0];
                                }
                            }
                            //  Checking whether this bundle is a fragment or not.
                            boolean isFragment = jarFile.getManifest().getMainAttributes().
                                    getValue(FRAGMENT_HOST) != null;
                            existingBundleInfoLines.add(new BundleInfo(bundleSymbolicName, bundleVersion,
                                    "../../dropins/" + childFileName.toString(), DEFAULT_BUNDLE_START_LEVEL,
                                    isFragment));
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error getting bundles info lines", e);
                    }
                }
            }
        });
        return existingBundleInfoLines;
    }

    /**
     * Returns a {@code Map} of existing OSGi bundle information by reading the bundles.info. Stale references are
     * removed.
     *
     * @param bundleInfoFile     the bundles.info file to be read
     * @param newBundleInfoLines the {@link List} of {@link BundleInfo} instances corresponding to the new OSGi
     *                           bundles in dropins folder
     * @return a {@link Map} of existing OSGi bundle information with stale references removed
     * @throws Exception if an error occurs when reading the bundles.info file
     */
    private static Map<String, List<BundleInfo>> processBundleInfoFile(Path bundleInfoFile,
            List<BundleInfo> newBundleInfoLines) throws Exception {
        Map<String, List<BundleInfo>> bundleInfoLineMap = new HashMap<>();

        if (Files.exists(bundleInfoFile)) {
            List<String> fileContent = Files.readAllLines(bundleInfoFile, Charset.forName("UTF-8"));
            for (String line : fileContent) {
                if (!line.startsWith("#")) {
                    BundleInfo bundleInfoLine = BundleInfo.getInstance(line);
                    if (bundleInfoLine.isFromDropins()) {
                        //  This bundle is from the dropins directory. We need to check whether this bundle
                        //  is still there in the dropins directory. If it does not exist, we remove it. This
                        //  is how we check whether two bundle lines are identical in this scenario.
                        //  BundleSymbolicNames and BundleVersions should be equal, Fragment-ness should be
                        //  equal.
                        boolean found = false;
                        for (BundleInfo newBundleInfoLine : newBundleInfoLines) {
                            if (newBundleInfoLine.getBundleSymbolicName().equals(bundleInfoLine.getBundleSymbolicName())
                                    && newBundleInfoLine.getBundleVersion().equals(bundleInfoLine.getBundleVersion())) {
                                //  Now the symbolicName and the version is equal. Now we need to check the
                                //  fragment-ness of these bundles.
                                if (!(newBundleInfoLine.isFragment() ^ bundleInfoLine.isFragment())) {
                                    // This means fragment-ness property is equal.
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            //  If this dropins bundle is no longer available in the dropins directory, we remove it.
                            continue;
                        }
                    }
                    List<BundleInfo> bundleInfoLines = bundleInfoLineMap.get(bundleInfoLine.getBundleSymbolicName());
                    if (bundleInfoLines == null) {
                        bundleInfoLines = new ArrayList<>();
                        bundleInfoLines.add(bundleInfoLine);
                        bundleInfoLineMap.put(bundleInfoLine.getBundleSymbolicName(), bundleInfoLines);
                    } else {
                        bundleInfoLines.add(bundleInfoLine);
                    }
                }
            }
        }
        return bundleInfoLineMap;
    }

    /**
     * Adds the new {@code BundleInfo} instance(s) to the existing OSGi bundle information.
     *
     * @param newBundleInfoLines        the list of new {@link BundleInfo} instances available in the dropins
     *                                  directory
     * @param existingBundleInfoLineMap the list of bundles currently available in the system
     */

    private static void addNewBundleInfoLines(List<BundleInfo> newBundleInfoLines,
            Map<String, List<BundleInfo>> existingBundleInfoLineMap) {
        newBundleInfoLines.forEach(newBundleInfoLine -> {
            String symbolicName = newBundleInfoLine.getBundleSymbolicName();
            String version = newBundleInfoLine.getBundleVersion();
            boolean isFragment = newBundleInfoLine.isFragment();

            List<BundleInfo> bundleInfoLineList = existingBundleInfoLineMap.get(symbolicName);

            if (bundleInfoLineList == null) {
                //  Bundle does not exists in the bundles.info line, hence we add it.
                bundleInfoLineList = new ArrayList<>();
                bundleInfoLineList.add(newBundleInfoLine);
                existingBundleInfoLineMap.put(symbolicName, bundleInfoLineList);
                logger.log(Level.INFO, "Deploying bundle: " + newBundleInfoLine.getBundleSymbolicName() + "_" +
                        newBundleInfoLine.getBundleVersion() + ".jar");
            } else {
                //  Bundle symbolic names exists. Now we need to check whether their versions are equal.
                boolean found = false;
                for (BundleInfo existingBundleInfoLine : bundleInfoLineList) {

                    if (existingBundleInfoLine.getBundleVersion().equals(version)) {
                        //  SymbolicName and the version match with an existing bundle.
                        //  Now we need to compare the fragment-ness.
                        if (existingBundleInfoLine.isFragment() ^ isFragment) {
                            //  This means fragment-ness property is not equal.
                            if (!existingBundleInfoLine.getBundlePath().equals(newBundleInfoLine.getBundlePath())) {
                                logger.log(Level.WARNING,
                                        "Ignoring the deployment of bundle: " + newBundleInfoLine.toString() +
                                                ", because it is already available in the system: " +
                                                existingBundleInfoLine.getBundlePath() +
                                                ". Bundle-SymbolicName and Bundle-Version headers are identical.");
                                found = true;
                                break;
                            }
                        } else {
                            //  This means fragment-ness property is equal. Seems like we have a match.
                            //  Now lets check whether their locations are equal. If the locations are equal, we don't
                            //  need to add it again. But if the locations are different we should throw a WARN.
                            if (existingBundleInfoLine.getBundlePath().equals(newBundleInfoLine.getBundlePath())) {
                                //  As we have an exact match, no need to add again.
                                logger.log(Level.FINE, "Deploying bundle: " + newBundleInfoLine.getBundlePath());
                                found = true;
                                break;

                            } else {
                                //  We have an exact match, but their locations are different.
                                logger.log(Level.WARNING,
                                        "Ignoring the deployment of bundle: " + newBundleInfoLine.toString() +
                                                ", because it is already available in the system: " +
                                                existingBundleInfoLine.getBundlePath() +
                                                ". Bundle-SymbolicName and Bundle-Version headers are identical.");
                                found = true;
                                break;
                            }
                        }
                    } else {
                        //  Version property is different. Therefore this new bundle does not exist in the system.
                        //  Therefore 'found' is still false.
                        found = false;
                    }
                }

                if (!found) {
                    //  Dropins bundle is not available in the system. Hence, add it.
                    bundleInfoLineList.add(newBundleInfoLine);
                    logger.log(Level.FINE, "Deploying bundle: ", newBundleInfoLine.getBundlePath());
                }
            }
        });
    }

    /**
     * Updates the bundles.info file with the new OSGi bundle information, if exists.
     *
     * @param bundlesInfoFile   the {@link Path} instance to the existing bundles.info file
     * @param bundleInfoLineMap the {@link Map} containing the complete OSGi bundle information
     * @throws Exception if an error occurs when updating the existing bundles.info file
     */
    private static void updateBundlesInfoFile(Path bundlesInfoFile, Map<String, List<BundleInfo>> bundleInfoLineMap)
            throws Exception {
        //  Generates the new bundles.info file into a temp location.
        String tempDirectory = System.getProperty("java.io.tmpdir");
        if (tempDirectory == null || tempDirectory.length() == 0) {
            throw new Exception("java.io.tmpdir property is null. Cannot proceed.");
        }

        Path tempBundlesInfoDirectory = Paths.get(tempDirectory, "bundles_info_" + UUID.randomUUID().toString());
        Path tempBundlesInfoFilePath = Paths.get(tempBundlesInfoDirectory.toString(), "bundles.info");
        if (!Files.exists(tempBundlesInfoDirectory)) {
            Files.createDirectories(tempBundlesInfoDirectory);
        }

        if (Files.exists(tempBundlesInfoDirectory)) {
            String[] keyArray = bundleInfoLineMap.keySet().toArray(new String[bundleInfoLineMap.keySet().size()]);
            Arrays.sort(keyArray);

            List<String> bundleInfoLines = new ArrayList<>();
            for (String key : keyArray) {
                List<BundleInfo> bundleInfoLineList = bundleInfoLineMap.get(key);
                bundleInfoLineList.forEach(bundleInfoLine -> bundleInfoLines.add(bundleInfoLine.toString()));
            }
            Files.write(tempBundlesInfoFilePath, bundleInfoLines, Charset.forName("UTF-8"));

            if (Files.exists(bundlesInfoFile)) {
                //  Replaces the original one with the new temporary file.
                Files.copy(tempBundlesInfoFilePath, bundlesInfoFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            throw new IOException("Failed to create the directory: " + tempBundlesInfoFilePath);
        }
    }
}
