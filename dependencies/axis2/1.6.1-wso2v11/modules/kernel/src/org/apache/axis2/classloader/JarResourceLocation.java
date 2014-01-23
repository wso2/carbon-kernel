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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

/**
 * @version $Rev: 704201 $ $Date: 2008-10-14 00:22:25 +0530 (Tue, 14 Oct 2008) $
 */
public class JarResourceLocation extends AbstractUrlResourceLocation {
    private JarFile jarFile;
    private byte content[];

    public JarResourceLocation(URL codeSource, File cacheFile) throws IOException {
        super(codeSource);
        try {
            jarFile = new JarFile(cacheFile);
        } catch (ZipException ze) {
            // We get this exception on windows when the
            // path to the jar file gets too long (Bug ID: 6374379)
            InputStream is = null;
            try {
                is = new FileInputStream(cacheFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int bytesRead = -1;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                this.content = baos.toByteArray();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    public ResourceHandle getResourceHandle(String resourceName) {
        if (jarFile != null) {
            JarEntry jarEntry = jarFile.getJarEntry(resourceName);
            if (jarEntry != null) {
                try {
                    URL url = new URL(getCodeSource(), resourceName);
                    return new JarResourceHandle(jarFile, jarEntry, getCodeSource());
                } catch (MalformedURLException e) {
                }
            }
        } else {
            try {
                final JarInputStream is = new JarInputStream(new ByteArrayInputStream(this.content));
                JarEntry jarEntry = null;
                while ((jarEntry = is.getNextJarEntry()) != null) {
                    if (jarEntry.getName().equals(resourceName)) {
                        try {
                            URL url = new URL(getCodeSource(), resourceName);
                            final JarEntry jarEntry2 = jarEntry;
                            return new JarEntryResourceHandle(jarEntry2, is);
                        } catch (MalformedURLException e) {
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    public Manifest getManifest() throws IOException {
        if (jarFile != null) {
            return jarFile.getManifest();
        } else {
            try {
                JarInputStream is = new JarInputStream(new ByteArrayInputStream(this.content));
                return is.getManifest();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public void close() {
        if (jarFile != null) {
            IoUtil.close(jarFile);
        }
    }

    private class JarEntryResourceHandle extends AbstractResourceHandle {
        private final JarEntry jarEntry2;
        private final JarInputStream is;

        public JarEntryResourceHandle(JarEntry jarEntry2, JarInputStream is) {
            this.jarEntry2 = jarEntry2;
            this.is = is;
        }

        public String getName() {
            return jarEntry2.getName();
        }

        public URL getUrl() {
            try {
                return new URL("jar", "", -1, getCodeSource() + "!/" + jarEntry2.getName());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isDirectory() {
            return jarEntry2.isDirectory();
        }

        public URL getCodeSourceUrl() {
            return getCodeSource();
        }

        public InputStream getInputStream() throws IOException {
            return is;
        }

        public int getContentLength() {
            return (int) jarEntry2.getSize();
        }
    }
}
