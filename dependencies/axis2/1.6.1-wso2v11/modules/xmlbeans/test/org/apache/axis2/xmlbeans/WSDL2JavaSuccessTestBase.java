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

package org.apache.axis2.xmlbeans;

import junit.framework.TestCase;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public abstract class WSDL2JavaSuccessTestBase extends TestCase {

    public static final String OUTPUT_LOCATION_BASE =
            System.getProperty("basedir", ".") + "/out_put_classes";
    public static final String OUTPUT_LOCATION_PREFIX = "/test";
    protected static int folderCount = 0;
    public static final String WSDL_BASE_DIR =
            System.getProperty("basedir", ".") + "/test-resources/";
    public static final String CLASSES_DIR =
            System.getProperty("basedir", ".") + "/target/classes/";
    private String[] moduleNames = { "xml", "common", "core" };
    private static final String MODULE_PATH_PREFIX = "../modules/";
    private static final String COMPILE_TARGET_NAME = "compile";

    protected String wsdlFileName;

    /**
     * Make the root output directory
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()) {
            deleteDir(outputFile);
            outputFile.mkdir();
        } else {
            outputFile.mkdir();
        }
    }

    /**
     * Remove the root output directory
     *
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()) {
            deleteDir(outputFile);
        }
    }

    /**
     * Deletes all files and subdirectories under dir. Returns true if all deletions were
     * successful. If a deletion fails, the method stops attempting to delete and returns false.
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }


    public void testWSDLFile() {
        try {
            generateAndCompile(wsdlFileName,
                               OUTPUT_LOCATION_BASE + OUTPUT_LOCATION_PREFIX + folderCount++);
        } catch (CodeGenerationException e) {
            e.printStackTrace();
            fail("Exception while code generation test! " + wsdlFileName + e.getMessage());
        }

    }

//    /**
//     * Test for the sales rank and price!
//     */
//    public void testCodeGenerationSalesRankNPrice(){
//        try {
//            generateAndCompile("SalesRankNPrice.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
//        } catch (CodeGenerationException e) {
//            fail("Exception while code generation test!"+ e.getMessage());
//        }
//    }

//     /**
//     * Test for the dime doc
//     */
//    public void testCodeGenerationDimeDoc(){
//
//        try {
//            generateAndCompile("dime-doc.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
//        } catch (CodeGenerationException e) {
//            fail("Exception while code generation test!"+ e.getMessage());
//        }
//    }

    /**
     * Test for the modified ping wsdl. it will be the test for the detached schema with only an import
     * statement
     */
//    public void testCodeGenerationPingModified(){
//
//        try {
//            generateAndCompile("ping-modified.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
//        } catch (CodeGenerationException e) {
//            fail("Exception while code generation test!"+ e.getMessage());
//        }
//    }


    /**
     * @param wsdlName
     * @param outputLocation
     * @throws CodeGenerationException
     */
    protected void generateAndCompile(String wsdlName, String outputLocation)
            throws CodeGenerationException {
        codeGenerate(WSDL_BASE_DIR + wsdlName, outputLocation);
        compile(outputLocation);
    }

    /**
     * @param wsdlFile
     * @param outputLocation
     * @throws CodeGenerationException
     */
    private void codeGenerate(String wsdlFile, String outputLocation)
            throws CodeGenerationException {
        //create the option map
        Map optionMap = fillOptionMap(wsdlFile, outputLocation);
        CommandLineOptionParser parser =
                new CommandLineOptionParser(optionMap);
        new CodeGenerationEngine(parser).generate();
    }

    /** @param outputLocation  */
    private void compile(String outputLocation) {
        String cp = null;
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(System.getProperty("basedir", ".") + "/target/cp.txt"));
            cp = br.readLine();
        } catch (Exception e) {
            // Don't care
        }
        if (cp == null) {
            cp = "";
        }

        //using the ant javac task for compilation
        Javac javaCompiler = new Javac();
        Project codeGenProject = new Project();
        Target compileTarget = new Target();

        compileTarget.setName(COMPILE_TARGET_NAME);
        compileTarget.addTask(javaCompiler);
        codeGenProject.addTarget(compileTarget);
        codeGenProject.setSystemProperties();
        javaCompiler.setProject(codeGenProject);
        javaCompiler.setIncludejavaruntime(true);
        javaCompiler.setIncludeantruntime(true);

        /*
          This harmless looking setFork is actually very important. unless the compiler is
          forked it wont work!
        */
        javaCompiler.setFork(true);

        //Create classpath - The generated output directories also become part of the classpath
        //reason for this is that some codegenerators(XMLBeans) produce compiled classes as part of
        //generated artifacts
        File outputLocationFile = new File(outputLocation);
        Path classPath = new Path(codeGenProject, outputLocation);
        classPath.addExisting(classPath.concatSystemClasspath(), false);
        for (int i = 0; i < moduleNames.length; i++) {
            classPath.add(new Path(codeGenProject,
                                   MODULE_PATH_PREFIX + moduleNames[i] + CLASSES_DIR));
        }

        classPath.add(new Path(codeGenProject, cp));

        javaCompiler.setClasspath(classPath);

        //set sourcePath - The generated output directories also become part of the sourcepath
        Path sourcePath = new Path(codeGenProject, outputLocation);
        sourcePath.setLocation(outputLocationFile);
        javaCompiler.setSrcdir(sourcePath);

        //output the classes into the output dir as well
        javaCompiler.setDestdir(outputLocationFile);
        javaCompiler.setVerbose(true);
        javaCompiler.execute();

    }

    /**
     *
     */
    private Map fillOptionMap(String wsdlFileName, String outputLocation) {
        Map optionMap = new HashMap();
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                        new String[] { wsdlFileName }));

        //use default sync option - No option is given
        //use default async option - No option is given
        //use default language option - No option is given
        //output location - code_gen_output

        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                        new String[] { outputLocation }));
        //server side option is on
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
                        new String[0]));
        // descriptor option is on
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                        new String[0]));
        // db is xmlbeans option is on
        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                        new String[] { TestConstants.Databinding.XML_BEANS }));

        optionMap.put(
                CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_ALL_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_ALL_OPTION,
                        new String[0]));

        //todo Make this work
        //test case option is on
//        optionMap.put(
//                CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                new CommandLineOption(
//                        CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                        new String[0]));
        //databinding is default

        return optionMap;
    }


}
