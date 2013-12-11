/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.server.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.server.CarbonLaunchExtension;
import org.wso2.carbon.server.LauncherConstants;
import org.wso2.carbon.server.util.JarInfo;
import org.wso2.carbon.server.util.PatchInfo;
import org.wso2.carbon.server.util.PatchUtils;
import org.wso2.carbon.server.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Copy all the patches found in the patches directory to the plugins directory in a recursive manner.
 */
public class PatchInstaller implements CarbonLaunchExtension {
    private static Log log = LogFactory.getLog(PatchInstaller.class);

    public void perform() {
        File carbonComponentDir = Utils.getCarbonComponentRepo();
        File plugins = new File(carbonComponentDir, LauncherConstants.PLUGINS_DIR);
        File servicepackDir = new File(carbonComponentDir, LauncherConstants.SERVICEPACKS_DIR);
        File patchesDir = new File(carbonComponentDir, LauncherConstants.PARENT_PATCHES_DIR);
        File bundleBackupDir = new File(patchesDir, LauncherConstants.BUNDLE_BACKUP_DIR);
        File prePatchedDirFile = new File(PatchUtils.getMetaDirectory(), LauncherConstants.PRE_PATCHED_DIR_FILE);

        try {
            PatchInfo patchInfo = PatchUtils.processPatches(prePatchedDirFile, servicepackDir, patchesDir);
            boolean changesOnPatches = false;

            // if any patches has being reverted we'll apply all patches
            // if new servicepack added it will considered as a situation where we need to apply all patches
            if (patchInfo.getRemovedPatchesCount() > 0 || patchInfo.isServicepackUpdated()) {
                log.info("Patch changes detected ");
                changesOnPatches = true;
                PatchUtils.applyServicepacksAndPatches(servicepackDir, patchesDir, plugins);
            } else {
                // check for content changes on currently applied patches
                Map<String, JarInfo> currentlyPatchedJars = PatchUtils.getJarsInAppliedServicepackAndPatches(servicepackDir, patchesDir);
                boolean patchUpdated = PatchUtils.checkUpdatedJars(currentlyPatchedJars);
                // if patches has modified we'll apply all the patches
                if (patchUpdated) {
                    log.info("Patch changes detected ");
                    changesOnPatches = true;
                    PatchUtils.applyServicepacksAndPatches(servicepackDir, patchesDir, plugins);
                } else {
                // no revert patches and no content change on previously added patches
                // check for new patches to skip patching process when no changes on patches
                    if (patchInfo.getNewPatchesCount() > 0) {
                        changesOnPatches = true;
                        // only new patches are available
                        log.info("Patch changes detected ");
                        if (!bundleBackupDir.exists()) {
                            // no BUNDLE_BACKUP_DIR we are patching for the first time
                            PatchUtils.applyServicepacksAndPatches(servicepackDir, patchesDir, plugins);
                        } else {
                            // apply remaining patches only from most recently added patch
                            Collections.sort(patchInfo.getNewPatches());
                            PatchUtils.copyNewPatches(servicepackDir, patchesDir, patchInfo.getNewPatches().get(0), plugins);
                        }
                    }
                }
            }
            Map<String, JarInfo> latestPatchedJar = PatchUtils.getMostLatestJarsInServicepackAndPatches(servicepackDir, patchesDir);
            // performs md5sum of latestPatchedJars against jars in plugin directory
            PatchUtils.checkMD5Checksum(latestPatchedJar, plugins, changesOnPatches);
        } catch (IOException e) {
            log.error("Error occurred while applying patches", e);
        } catch (Exception e) {
            log.error("Error occurred while verifying md5 checksum of patched jars", e);
        }
    }
}
