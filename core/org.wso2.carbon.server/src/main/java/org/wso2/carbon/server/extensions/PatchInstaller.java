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
        File prePatchedDirFile = new File(PatchUtils.getMetaDirectory(), LauncherConstants.PRE_PATCHED_DIR_FILE);


        try {
            PatchInfo patchInfo = PatchUtils.processPatches(prePatchedDirFile , servicepackDir, patchesDir);
            boolean patchesChanged = patchInfo.isPatchesChanged();
            Map<String, JarInfo>  latestPatchedJar = PatchUtils.getMostLatestJarsInServicepackAndPatches(servicepackDir, patchesDir);
            if(!patchesChanged) {
                //check updated jars in already applied patches.
                patchesChanged = PatchUtils.checkUpdatedJars(latestPatchedJar);
            }
            if (patchesChanged) {
                log.info("Patch changes detected ");
                PatchUtils.applyServicepacksAndPatches(servicepackDir , patchesDir, plugins);
            }
            // performs md5sum of latestPatchedJars against jars in plugin directory
            PatchUtils.checkMD5Checksum(latestPatchedJar, plugins, patchesChanged);
        } catch (IOException e) {
            log.error("Error occurred while applying patches", e);
        } catch (Exception e) {
            log.error("Error occurred while verifying md5 checksum of patched jars", e);
        }
    }
}
