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

package org.apache.axis2.java.security.action;

import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.java.security.driver.Java2SecTest;
import org.apache.axis2.java.security.interf.Actor;

import java.io.CharArrayWriter;
import java.io.FileReader;
import java.io.IOException;


/**
 * Action reads the data from an input file
 * and then saves the file input to Java2SecTest class
 */

public class Action implements Actor {

    private String fileName;
    private FileReader fileReader;

    // Constructor
    public Action(String fileName) {
        this.fileName = fileName;
    }

    // Implementing Actor's takeAction method
    public void takeAction() {
        try {
            // Print out maven's base,build, and test direcotories
            String baseDir = AbstractTestCase.basedir;
            System.out.println("basedir => " + baseDir);

            String buildDir = System.getProperty("maven_build_dir");
            System.out.println("buildDir => " + buildDir);

            String testDir = System.getProperty("maven_test_dest");
            System.out.println("testDir => " + testDir);

            // Convert the \ (back slash) to / (forward slash)
            String baseDirM = baseDir.replace('\\', '/');
            System.out.println("baseDirM => " + baseDirM);

            String fs = "/";

            // Build the file URL
            String fileURL = baseDirM + fs + "test-resources" + fs + "java2sec" + fs + fileName;
            System.out.println("File URL => " + fileURL);

            if (fileName != null)
                fileReader = new FileReader(fileURL);
            else
                fileReader = new FileReader("public.txt");

            try {
                CharArrayWriter caw = new CharArrayWriter();
                int c;
                while ((c = fileReader.read()) != -1) {
                    caw.write(c);
                }
                // Set/save the file input as test result onto Java2SecTest
                Java2SecTest.testResult = caw.toString();
            }
            catch (IOException e) {
                e.printStackTrace(System.out);
            }
            finally {
                try {
                    fileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
