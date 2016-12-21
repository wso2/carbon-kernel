/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.extensions.touchpoint.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.wso2.carbon.extensions.touchpoint.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Copies from PARM_COPY_SOURCE to PARAM_COPY_TARGET replaces ${runtime} with the Profile.
 * The ${runtime} in both PARAM_COPY_SOURCE and PARA_COPY_TARGET is replaced with the Profile.
 * The optional parameter PARAM_COPY_OVERWRITE overwrites and existing file if set to true, else
 * existing file with the same name returns an error. The default is false.
 * If the source is a directory, a merge copy to the target is performed.
 * Copy will copy files and directories (recursively).
 *
 * @since 5.2.0
 */
public class CopyAction extends ProvisioningAction {

    @Override
    public IStatus execute(Map<String, Object> parameters) {

        String target = (String) parameters.get(Constants.PARM_COPY_TARGET);
        String source = (String) parameters.get(Constants.PARM_COPY_SOURCE);
        String overwrite = parameters.get(Constants.PARM_COPY_OVERWRITE).toString();
        String profile = parameters.get(Constants.PROFILE).toString();

        if (target == null) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Target is not defined");
        }
        if (source == null) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Source is not defined");
        } else if (!Files.exists(Paths.get(source))) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Source: " + source + "does not exists");
        }
        String runtime = profile.substring(profile.indexOf(Constants.PROFILE_END_CHAR) + 1, profile.length() - 1);
        target = target.replaceAll(Constants.RUNTIME_KEY, runtime);

        try {
            copy(new File(source), new File(target), Boolean.parseBoolean(overwrite));
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Failed to copy from source: " + source +
                    " to target: " + target, e);
        }
        return Status.OK_STATUS;
    }

    @Override
    public IStatus undo(Map<String, Object> parameters) {
        return new Status(IStatus.CANCEL, Constants.PLUGIN_ID, "Copy touchpoint does not support 'undo'");
    }

    /**
     * Recursively copies files and folder from source to target.
     *
     * @param source        Location from which files / folder should be copied from.
     * @param target        Location to which files / folder should be copied to.
     * @param overwrite     If true will overwrite the file (if it exists). If false will throw an exception if file
     *                      exists at target location
     * @throws IOException
     */
    private static void copy(File source, File target, boolean overwrite) throws IOException {
        if (source.isDirectory()) {
            if (target.exists() && target.isFile()) {
                if (!overwrite) {
                    throw new IOException("Target: " + target + " already exists");
                }

                if (!target.delete()) {
                    throw new IOException("Failed to delete target: " + target.toString());
                }
            }
            if (!target.exists() && !target.mkdirs()) {
                throw new IOException("Unable to create Target: " + target.toString());
            }
            File[] children = source.listFiles();
            if (children == null) {
                throw new IOException("Error while retrieving children of directory: " + source);
            }
            for (File child: children) {
                copy(child, new File(target, child.getName()), overwrite);
            }
            return;
        }

        if (target.exists() && !overwrite) {
            throw new IOException("Target: " + target + " already exists");
        }
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            throw new IOException("Target: Path " + target.getParent() + " could not be created");
        }

        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Error while copying:" + source.getAbsolutePath(), e);
        }
    }
}
