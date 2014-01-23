/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.classloader;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

public class JarFileClassLoaderTest extends TestCase {
    private File tmpDir;
    
    @Override
    protected void setUp() throws Exception {
        tmpDir = new File(System.getProperty("java.io.tmpdir"), getClass().getName());
        if (tmpDir.exists()) {
            FileUtils.deleteDirectory(tmpDir);
        }
        // Create the following files in the tmp directory:
        // outside
        // root/a
        // root/dir/b
        FileUtils.touch(new File(tmpDir, "outside"));
        File root = new File(tmpDir, "root");
        root.mkdir();
        FileUtils.touch(new File(root, "a"));
        File dir = new File(root, "dir");
        dir.mkdir();
        FileUtils.touch(new File(dir, "b"));
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir);
    }

    /**
     * Test that if one of the URLs is a directory, the class loader doesn't allow access to files
     * outside of that directory (by using ".." in the resource name). See AXIS2-4282.
     * <p>
     * Note that while
     * {@linkplain http://java.sun.com/j2se/1.4.2/docs/guide/resources/resources.html} suggests
     * that ".." should be prohibited altogether, Sun's URLClassLoader implementation allows this,
     * as long as the resource name doesn't specify a file outside of the directory. E.g.
     * "dir/../a" is an allowed resource name (equivalent to "a").
     * 
     * @throws Exception
     */
    public void testConfinement() throws Exception {
        ClassLoader cl = new JarFileClassLoader(new URL[] { new File(tmpDir, "root").toURL() });
        assertNull(cl.getResource("../outside"));
        assertNotNull(cl.getResource("a"));
        assertNotNull(cl.getResource("dir/b"));
    }
}
