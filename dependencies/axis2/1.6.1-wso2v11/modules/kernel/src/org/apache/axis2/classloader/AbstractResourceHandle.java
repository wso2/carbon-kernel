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
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @version $Rev: 704201 $ $Date: 2008-10-14 00:22:25 +0530 (Tue, 14 Oct 2008) $
 */
public abstract class AbstractResourceHandle implements ResourceHandle {
    public byte[] getBytes() throws IOException {
        InputStream in = getInputStream();
        try {
            byte[] bytes = IoUtil.getBytes(in);
            return bytes;
        } finally {
            IoUtil.close(in);
        }
    }

    public Manifest getManifest() throws IOException {
        return null;
    }

    public Certificate[] getCertificates() {
        return null;
    }

    public Attributes getAttributes() throws IOException {
        Manifest m = getManifest();
        if (m == null) {
            return null;
        }

        String entry = getUrl().getFile();
        return m.getAttributes(entry);
    }

    public void close() {
    }

    public String toString() {
        return "[" + getName() + ": " + getUrl() + "; code source: " + getCodeSourceUrl() + "]";
    }
}
