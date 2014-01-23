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

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @version $Rev: 704201 $ $Date: 2008-10-14 00:22:25 +0530 (Tue, 14 Oct 2008) $
 */
public class DirectoryResourceHandle extends AbstractResourceHandle {
    private final String name;
    private final File file;
    private final Manifest manifest;
    private final URL url;
    private final URL codeSource;

    public DirectoryResourceHandle(String name, File file, File codeSource, Manifest manifest) throws MalformedURLException {
        this.name = name;
        this.file = file;
        this.codeSource = codeSource.toURL();
        this.manifest = manifest;
        url = file.toURL();
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public URL getCodeSourceUrl() {
        return codeSource;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public InputStream getInputStream() throws IOException {
        if (file.isDirectory()) {
            return new IoUtil.EmptyInputStream();
        }
        return new FileInputStream(file);
    }

    public int getContentLength() {
        if (file.isDirectory() || file.length() > Integer.MAX_VALUE) {
            return -1;
        } else {
            return (int) file.length();
        }
    }

    public Manifest getManifest() throws IOException {
        return manifest;
    }

    public Attributes getAttributes() throws IOException {
        if (manifest == null) {
            return null;
        }
        return manifest.getAttributes(getName());
    }

    /**
     * Always return null.  This could be implementd by verifing the signatures
     * in the manifest file against the actual file, but we don't need this right now.
     * @return null
     */
    public Certificate[] getCertificates() {
        return null;
    }
}
