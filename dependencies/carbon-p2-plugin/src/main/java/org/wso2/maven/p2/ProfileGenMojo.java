/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.maven.p2;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.tycho.p2.facade.internal.P2ApplicationLauncher;
import org.wso2.maven.p2.generate.utils.FileManagementUtil;
import org.wso2.maven.p2.generate.utils.P2Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Write environment information for the current build to file.
 *
 * @goal p2-profile-gen
 * @phase package
 */
public class ProfileGenMojo extends AbstractMojo {


    private final String STREAM_TYPE_IN = "inputStream";
    private final String STREAM_TYPE_ERROR = "errorStream";
    /**
     * Destination to which the features should be installed
     *
     * @parameter
     * @required
     */
    private String destination;
    /**
     * target profile
     *
     * @parameter
     * @required
     */
    private String profile;
    /**
     * URL of the Metadata Repository
     *
     * @parameter
     */
    private URL metadataRepository;
    /**
     * URL of the Artifact Repository
     *
     * @parameter
     */
    private URL artifactRepository;
    /**
     * List of features
     *
     * @parameter
     * @required
     */
    private ArrayList features;
    /**
     * Flag to indicate whether to delete old profile files
     *
     * @parameter default-value="true"
     */
    private boolean deleteOldProfileFiles = true;
    /**
     * Location of the p2 repository
     *
     * @parameter
     */
    private P2Repository p2Repository;
    /**
     * @parameter default-value="${project}"
     */
    private MavenProject project;
    /**
     * @component
     */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;
    /**
     * @component
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;
    /**
     * @parameter default-value="${localRepository}"
     */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;
    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     */
    private java.util.List remoteRepositories;
    /**
     * Equinox p2 configuration path
     *
     * @parameter
     */
    private P2Profile p2Profile;
    /**
     * Maven ProjectHelper.
     *
     * @component
     */
    private MavenProjectHelper projectHelper;
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
    private File FOLDER_TARGET;
    private File FOLDER_TEMP;
    private File FOLDER_TEMP_REPO_GEN;
    private File FILE_FEATURE_PROFILE;
    private File p2AgentDir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (profile == null) {
                profile = P2Constants.DEFAULT_PROFILE_ID;
            }
            createAndSetupPaths();
            rewriteEclipseIni();
//          	verifySetupP2RepositoryURL();
            this.getLog().info("Running Equinox P2 Director Application");
            installFeatures(getIUsToInstall());
            //updating profile's config.ini p2.data.area property using relative path
            File profileConfigIni = FileManagementUtil.getProfileConfigIniFile(destination, profile);
            FileManagementUtil.
                    changeConfigIniProperty(profileConfigIni, "eclipse.p2.data.area", "@config.dir/../../p2/");

            //deleting old profile files, if specified
            if (deleteOldProfileFiles) {
                deleteOldProfiles();
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
//        createArchive();
//        deployArtifact();
        performMopUp();
    }

    private String getIUsToInstall() throws MojoExecutionException {
        String installUIs = "";
        for (Object featureObj : features) {
            Feature f;
            if (featureObj instanceof Feature) {
                f = (Feature) featureObj;
            } else if (featureObj instanceof String) {
                f = Feature.getFeature(featureObj.toString());
            } else {
                f = (Feature) featureObj;
            }
            installUIs = installUIs + f.getId().trim() + "/" + f.getVersion().trim() + ",";
        }

        if (installUIs.length() == 0) {
            installUIs = installUIs.substring(0, installUIs.length() - 1);
        }
        return installUIs;
    }

    private String getPublisherApplication() {
        return "org.eclipse.equinox.p2.director";
    }

    private void installFeatures(String installUIs) throws Exception {
        P2ApplicationLauncher launcher = this.launcher;

        launcher.setWorkingDirectory(project.getBasedir());
        launcher.setApplicationName(getPublisherApplication());

        addArguments(launcher, installUIs);


        int result = launcher.execute(forkedProcessTimeoutInSeconds);
        if (result != 0) {
            throw new MojoFailureException("P2 publisher return code was " + result);
        }
    }

    private void addArguments(P2ApplicationLauncher launcher, String installUIs)
            throws IOException, MalformedURLException {
        launcher.addArguments(
                "-metadataRepository", metadataRepository.toExternalForm(), //
                "-artifactRepository", artifactRepository.toExternalForm(), //
                "-profileProperties", "org.eclipse.update.install.features=true",
                "-installIU", installUIs,
                "-bundlepool", destination,
                //to support shared installation in carbon
                "-shared", destination + File.separator + "p2",
                //target is set to a separate directory per Profile
                "-destination", destination + File.separator + profile,
                "-profile", profile.toString(),
                "-roaming"
        );
    }

    private void createAndSetupPaths() throws Exception {
        FOLDER_TARGET = new File(project.getBasedir(), "target");
        String timestampVal = String.valueOf((new Date()).getTime());
        FOLDER_TEMP = new File(FOLDER_TARGET, "tmp." + timestampVal);
        FOLDER_TEMP_REPO_GEN = new File(FOLDER_TEMP, "temp_repo");
        FILE_FEATURE_PROFILE = new File(FOLDER_TARGET, project.getArtifactId() + "-" + project.getVersion() + ".zip");


    }

    private void deleteOldProfiles() {
        if (!destination.endsWith("/")) {
            destination = destination + "/";
        }
        String profileFolderName = destination +
                "p2/org.eclipse.equinox.p2.engine/profileRegistry/" + profile + ".profile";

        File profileFolder = new File(profileFolderName);
        if (profileFolder.isDirectory()) {
            String[] profileFileList = profileFolder.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".profile");
                }
            });

            Arrays.sort(profileFileList);

            //deleting old profile files
            for (int i = 0; i < (profileFileList.length - 1); i++) {
                File profileFile = new File(profileFolderName, profileFileList[i]);
                profileFile.delete();
            }
        }
    }

    private void rewriteEclipseIni() {
        File eclipseIni = null;
        String profileLocation = destination + File.separator + profile;
        // getting the file null.ini
        eclipseIni = new File(profileLocation + File.separator + "null.ini");
        if (eclipseIni.exists()) {
            rewriteFile(eclipseIni, profileLocation);
            return;
        }
        // null.ini does not exist. trying with eclipse.ini
        eclipseIni = new File(profileLocation + File.separator + "eclipse.ini");
        if (eclipseIni.exists()) {
            rewriteFile(eclipseIni, profileLocation);
            return;
        }
    }

    private void rewriteFile(File file, String profileLocation) {
        file.delete();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file));
            pw.write("-install\n");
            pw.write(profileLocation);
            pw.flush();
        } catch (IOException e) {
            this.getLog().debug("Error while writing to file " + file.getName());
            e.printStackTrace();
        } finally {
            pw.close();
        }
    }

    private void deployArtifact() {
        if (FILE_FEATURE_PROFILE != null && FILE_FEATURE_PROFILE.exists()) {
            project.getArtifact().setFile(FILE_FEATURE_PROFILE);
            projectHelper.attachArtifact(project, "zip", null, FILE_FEATURE_PROFILE);
        }
    }

    private void performMopUp() {
        try {
            FileUtils.deleteDirectory(FOLDER_TEMP);
        } catch (Exception e) {
            getLog().warn(new MojoExecutionException("Unable complete mop up operation", e));
        }
    }

    public class InputStreamHandler implements Runnable {
        String streamType;
        InputStream inputStream;

        public InputStreamHandler(String name, InputStream is) {
            this.streamType = name;
            this.inputStream = is;
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.start();
        }

        public void run() {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                while (true) {
                    String s = bufferedReader.readLine();
                    if (s == null) {
                        break;
                    }
                    if (STREAM_TYPE_IN.equals(streamType)) {
                        getLog().info(s);
                    } else if (STREAM_TYPE_ERROR.equals(streamType)) {
                        getLog().error(s);
                    }
                }
                inputStream.close();
            } catch (Exception ex) {
                getLog().error("Problem reading the " + streamType + ".", ex);
            }
        }

    }
}
