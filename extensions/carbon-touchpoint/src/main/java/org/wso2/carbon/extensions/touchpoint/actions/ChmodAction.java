/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.extensions.touchpoint.actions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.wso2.carbon.extensions.touchpoint.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

/**
 * Set the permission of the resource file in the target directory.
 * The {runtime} placeholder in the target directory path is replaced with the profile,
 * This is basically a copy of natives ChmodAction class(@see org.eclipse.equinox.internal.p2.touchpoint.natives
 * .actions.ChmodAction) with {runtime} placeholder support.
 *
 * @since 5.2.0
 */
public class ChmodAction extends ProvisioningAction {
    private static final boolean WINDOWS = java.io.File.separatorChar == '\\';

    /**
     * {@inheritDoc}
     * @param parameters
     * @return
     */
    @Override
    public IStatus execute(Map<String, Object> parameters) {
        Object absoluteFiles = parameters.get(Constants.PARM_ABSOLUTE_FILES); //String or String[]
        String targetDir = (String) parameters.get(Constants.PARM_TARGET_DIR);
        String targetFile = (String) parameters.get(Constants.PARM_TARGET_FILE);
        // Add new variable to keep the profile.
        String profile = parameters.get(Constants.PROFILE).toString();

        if (targetFile != null && absoluteFiles != null) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Both Target file and absolute files can't be set");
        }
        if (targetDir != null && targetFile == null) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Target file is not set");
        }
        if (targetDir == null && targetFile != null) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Target directory is not set");
        }

        String permissions = (String) parameters.get(Constants.PARM_PERMISSIONS);
        if (permissions == null) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Permission is not set");
        }

        // custom code starts here
        // replace the {runtime} placeholder in target directory path with the profile.
        String runtime = profile.substring(profile.indexOf(Constants.PROFILE_END_CHAR) + 1, profile.length() - 1);
        targetDir = targetDir.replaceAll(Constants.RUNTIME_KEY, runtime);
        targetFile = targetFile.replaceAll(Constants.RUNTIME_KEY, runtime);
        // custom code ends here

        String optionsString = (String) parameters.get(Constants.PARM_OPTIONS);

        String[] filesToProcess = absoluteFiles != null ? ((absoluteFiles instanceof String) ? new String[] {
                ((String) absoluteFiles).replaceAll(Constants.RUNTIME_KEY, runtime) } : (String[]) absoluteFiles) :
                                  makeFilesAbsolute(targetDir, targetFile);
        for (String fileToChmod : filesToProcess) {
            // Check that file exist
            File probe = new File(fileToChmod);
            if (!probe.exists()) {
                return new Status(IStatus.ERROR, Constants.PLUGIN_ID, probe.toString() + " File doesn't exist");
            }
            doChmod(fileToChmod, permissions, optionsString);
        }

        return Status.OK_STATUS;
    }

    private String[] makeFilesAbsolute(String targetDir, String targetFile) {
        return new String[] { targetDir + IPath.SEPARATOR + targetFile };
    }

    private void doChmod(String fileToChmod, String permissions, String optionsString) {
        String options[] = null;
        if (optionsString != null) {
            ArrayList<String> collect = new ArrayList<>();
            String r = optionsString.trim();
            while (r.length() > 0) {
                int spaceIdx = r.indexOf(' ');
                if (spaceIdx < 0) {
                    collect.add(r);
                    r = ""; //$NON-NLS-1$
                } else {
                    collect.add(r.substring(0, spaceIdx));
                    r = r.substring(spaceIdx + 1);
                    r = r.trim();
                }
            }
            if (collect.size() > 0) {
                options = new String[collect.size()];
                collect.toArray(options);
            }
        }

        chmod(fileToChmod, permissions, options);
    }

    public IStatus undo(Map<String, Object> parameters) {
        //TODO: implement undo ??
        return Status.OK_STATUS;
    }

    private void chmod(String fileToChmod, String perms, String[] options) {
        if (WINDOWS) {
            return;
        }
        Runtime r = Runtime.getRuntime();
        try {
            // Note: 3 is from chmod, permissions, and target
            String[] args = new String[3 + (options == null ? 0 : options.length)];
            int i = 0;
            args[i++] = "chmod"; //$NON-NLS-1$
            if (options != null) {
                for (String option : options) {
                    args[i++] = option;
                }
            }
            args[i++] = perms;
            args[i] = fileToChmod;
            Process process = r.exec(args);
            readOffStream(process.getErrorStream());
            readOffStream(process.getInputStream());
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                // mark thread interrupted and continue
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private void readOffStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            while (reader.skip(Long.MAX_VALUE) == Long.MAX_VALUE) {
                // do nothing
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
