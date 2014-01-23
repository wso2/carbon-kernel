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

package org.apache.axis2.jibx;

import junit.framework.TestCase;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.extension.JiBXExtension;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Full code generation and runtime test for JiBX data binding extension. This is based on the
 * XMLBeans test code.
 */
public class Test extends TestCase {
    private static final String TEST_CLASSES_DIR =
            System.getProperty("basedir", ".") + "/target/test-classes";
    private static final String OUTPUT_LOCATION_BASE =
            System.getProperty("basedir", ".") + "/target/gen";
    private static final String OUTPUT_LOCATION_PREFIX = "/test";
    private static final String WSDL_BASE_DIR =
            System.getProperty("basedir", ".") + "/test-resources/wsdl/";
    private static final String BINDING_BASE_DIR =
            System.getProperty("basedir", ".") + "/test-resources/binding/";
    private static final String REPOSITORY_DIR =
            System.getProperty("basedir", ".") + "/test-resources/repo/";
    private static final String CLASSES_DIR =
            System.getProperty("basedir", ".") + "/target/classes/";
    private static final String[] moduleNames = { "kernel", "core" };
    private static final String MODULE_PATH_PREFIX = "../modules/";
    private static final String COMPILE_TARGET_NAME = "compile";
    private static final String STUB_CLASS =
            "org.apache.ws.axis2.jibx.customer.wsdl.EchoCustomerServiceStub";

    public static final QName serviceName = new QName("EchoCustomerService");
    public static final QName operationName = new QName("echo");

    private AxisService service;

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

    private void startServer() throws Exception {
        service = Utils.createSimpleService(serviceName,
                                            Echo.class.getName(), operationName);
        UtilServer.start(REPOSITORY_DIR);
        UtilServer.deployService(service);
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

    private void stopServer() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
/*        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
        }   */
    }

    /**
     * Handle linkage code generation.
     *
     * @param wsdl
     * @param binding
     * @param outdir
     * @param unwrap
     * @throws CodeGenerationException
     */
    private void codeGenerate(String wsdl, String binding, String outdir,
                              boolean unwrap) throws CodeGenerationException {

        // create the option map
        Map optionMap = new HashMap();
        optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                      new CommandLineOption(
                              CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
                              new String[] { wsdl }));

        //use default sync option - No option is given
        //use default async option - No option is given
        //use default language option - No option is given

        // output location
        optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                      new CommandLineOption(
                              CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
                              new String[] { outdir }));

        // db is JiBX
        optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                      new CommandLineOption(
                              CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
                              new String[] { "jibx" }));

        // unwrap if requested
        if (unwrap) {
            optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS,
                          new CommandLineOption(
                                  CommandLineOptionConstants.WSDL2JavaConstants.UNWRAP_PARAMETERS,
                                  new String[0]));
        }

        // binding definition is supplied
        String option = CommandLineOptionConstants.WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX +
                JiBXExtension.BINDING_PATH_OPTION;
        optionMap.put(option, new CommandLineOption(option, new String[] { binding }));

        //TODO: Make this work
        //test case option is on
//        optionMap.put(
//                CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                new CommandLineOption(
//                        CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                        new String[0]));
        CommandLineOptionParser parser = new CommandLineOptionParser(optionMap);
        new CodeGenerationEngine(parser).generate();
    }

    /**
     * Compile generated code.
     *
     * @param outdir
     */
    private void compile(String outdir) throws Exception {
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
        String classdir = outdir + "/classes";
        File outputLocationFile = new File(classdir);
        outputLocationFile.mkdir();
        Path classPath = new Path(codeGenProject, classdir);
        classPath.add(new Path(codeGenProject, TEST_CLASSES_DIR));
        classPath.addExisting(classPath.concatSystemClasspath(), false);
        for (int i = 0; i < moduleNames.length; i++) {
            classPath.add(new Path(codeGenProject,
                                   MODULE_PATH_PREFIX + moduleNames[i] + CLASSES_DIR));
        }
        classPath.add(new Path(codeGenProject, cp));

        javaCompiler.setClasspath(classPath);

        //set sourcePath - The generated output directories also become part of the sourcepath
        Path sourcePath = new Path(codeGenProject, outdir);
        sourcePath.setLocation(outputLocationFile);
        javaCompiler.setSrcdir(sourcePath);

        //output the classes into the output dir as well
        javaCompiler.setDestdir(outputLocationFile);
        javaCompiler.setDebug(true);
        javaCompiler.setVerbose(true);
        javaCompiler.execute();
//        codeGenProject.executeTarget(COMPILE_TARGET_NAME);
    }

    public void testBuildAndRun() throws Exception {
        startServer();

        // start by generating and compiling the Axis2 interface code
        String outdir =
                OUTPUT_LOCATION_BASE + OUTPUT_LOCATION_PREFIX;
        codeGenerate(WSDL_BASE_DIR + "customer-echo.wsdl",
                     BINDING_BASE_DIR + "customer-binding.xml", outdir, false);
        compile(outdir);

//         finish by testing a roundtrip call to the echo server
        File classesdir = new File(outdir + "/classes/");
        URLClassLoader loader = new URLClassLoader(new URL[] { classesdir.toURL() },
                                                   this.getClass().getClassLoader());
        Class stub = loader.loadClass(STUB_CLASS);
        Person person = new Person(42, "John", "Smith");
        Customer customer = new Customer("Redmond", person, "+14258858080",
                                         "WA", "14619 NE 80th Pl.", new Integer(98052));
        Constructor constructor = stub.getConstructor(new Class[] {
                "http://127.0.0.1:5555/axis2/services/EchoCustomerService/echo".getClass() });
        Object inst = constructor.newInstance(
                new Object[] { "http://127.0.0.1:5555/axis2/services/EchoCustomerService/echo" });
        Method method = stub.getMethod("echo", new Class[] { Customer.class });
        Object result = method.invoke(inst, new Object[] { customer });
        stopServer();
        assertEquals("Result object does not match request object",
                     customer, result);
    }

    public void testCompileWrapped() throws Exception {

        // generate and compile the Axis2 interface code
        String outdir = OUTPUT_LOCATION_BASE + OUTPUT_LOCATION_PREFIX;
        codeGenerate(WSDL_BASE_DIR + "library.wsdl",
                     BINDING_BASE_DIR + "library-binding.xml", outdir, false);
        compile(outdir);
    }

    public void testCompileUnwrapped() throws Exception {

        // generate and compile the Axis2 interface code
        String outdir = OUTPUT_LOCATION_BASE + OUTPUT_LOCATION_PREFIX;
        codeGenerate(WSDL_BASE_DIR + "library.wsdl",
                     BINDING_BASE_DIR + "library-binding.xml", outdir, true);
        compile(outdir);
    }

}

