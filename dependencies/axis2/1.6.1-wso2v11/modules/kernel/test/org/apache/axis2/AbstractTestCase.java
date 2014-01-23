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

package org.apache.axis2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Abstract base class for test cases.
 */
public abstract class AbstractTestCase extends TestCase {

    protected String testDir = "test" + File.separator;
    protected String sampleDir = "samples" + File.separator;
    protected String outDir = "target" + File.separator + "generated" +
        File.separator + "samples" + File.separator;
    protected String tempDir = "target" + File.separator + "generated" +
        File.separator + "temp";
    protected String testResourceDir = "test-resources";

    /**
     * Basedir for all file I/O. Important when running tests from the reactor.
     * Note that anyone can use this statically.
     */
    public static String basedir;

    static {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
    }

    /**
     * @param testName
     */
    public AbstractTestCase(String testName) {
        super(testName);
        testDir = new File(basedir, testDir).getAbsolutePath();
        sampleDir = new File(basedir, sampleDir).getAbsolutePath();
        outDir = new File(basedir, outDir).getAbsolutePath();
        tempDir = new File(basedir, tempDir).getAbsolutePath();
        testResourceDir = new File(basedir, testResourceDir).getAbsolutePath();
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    public InputStream getTestResource(String relativePath) {
        File testResource = getTestResourceFile(relativePath);
        try {
            return new FileInputStream(testResource);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The '" + testResource.getAbsolutePath() +
                "' file does not exist. Verify that the 'basedir' System property " +
                "is pointing to the root of the project", e);
        }
    }
}
