/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.axis2.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.jar.Manifest;

/**
 * @version $Rev: 757306 $ $Date: 2009-03-23 08:47:07 +0530 (Mon, 23 Mar 2009) $
 */
public class DirectoryResourceLocation extends AbstractUrlResourceLocation {
    private final File baseDir;
    private boolean manifestLoaded = false;
    private Manifest manifest;

    public DirectoryResourceLocation(File baseDir) throws MalformedURLException {
        super(baseDir.toURL());
        this.baseDir = baseDir;
    }

    public ResourceHandle getResourceHandle(String resourceName) {
        File file = new File(baseDir, resourceName);
        if (!file.exists() || !isLocal(file)) {
            return null;
        }

        try {
            ResourceHandle resourceHandle = new DirectoryResourceHandle(resourceName, file, baseDir, getManifestSafe());
            return resourceHandle;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean isLocal(File file) {
        try {
            String base = baseDir.getCanonicalPath();
            String relative = file.getCanonicalPath();
            return (relative.startsWith(base));
        } catch (IOException e) {
            return false;
        }
    }
    
    public Manifest getManifest() throws IOException {
        if (!manifestLoaded) {
            File manifestFile = new File(baseDir, "META-INF/MANIFEST.MF");

            if (manifestFile.isFile() && manifestFile.canRead()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(manifestFile);
                    manifest = new Manifest(in);
                } finally {
                    IoUtil.close(in);
                }
            }
            manifestLoaded = true;
        }
        return manifest;
    }

    private Manifest getManifestSafe() {
        Manifest manifest = null;
        try {
            manifest = getManifest();
        } catch (IOException e) {
            // ignore
        }
        return manifest;
    }
}
