/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.annotationprocessor;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests the annotation processor logic in carbon core/annotations.
 *
 * @since 5.2.0
 */
public class AnnotationProcessorTest {

    private static JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private DiagnosticCollector<JavaFileObject> collector;
    private String[] classesToCompile;
    private String packagePath;

    @BeforeClass
    public void initClass() {
        //get the java compiler.
        compiler = ToolProvider.getSystemJavaCompiler();
        //configure the diagnostics collector.
        collector = new DiagnosticCollector<>();
        fileManager = compiler.getStandardFileManager(collector, Locale.US, Charset.forName("UTF-8"));
        packagePath = Paths.get("src", "test", "java", "org", "wso2", "carbon", "kernel", "annotationprocessor")
                .toString();
        classesToCompile =  new String[] {
                    Paths.get(packagePath, "Configurations.java").toString()};
    }

    @Test
    public void testCompilation() {
            try (ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
            OutputStreamWriter stdout = new OutputStreamWriter(stdoutStream)) {
                JavaCompiler.CompilationTask task = compiler.getTask(stdout, fileManager, collector, null, null,
                        fileManager.getJavaFileObjects(classesToCompile));
                Boolean result = task.call();
                //perform the verifications.
                verifyCompilationErrors(collector.getDiagnostics(), result);
                verifyTempConfigFile();
            } catch (IOException e) {
                Assert.fail("error while creating output stream.", e);
            }
    }

    private void verifyCompilationErrors(List<Diagnostic<? extends JavaFileObject>> diagnostics, Boolean result) {
        //no mandatory warnings or compilation errors should be found.
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            if (diagnostic.getKind() == Diagnostic.Kind.MANDATORY_WARNING ||
                    diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                fail("Failed with message: " + diagnostic.getMessage(null));
            }
        }
        assertEquals("Files should have no compilation errors", Boolean.TRUE, result);
    }

    private void verifyTempConfigFile() {
        File file = Paths.get(System.getProperty("user.dir"), "temp_config_classnames.txt").toFile();
        try {
            List<String> lines = FileUtils.readLines(file, Charset.forName("UTF-8"));
            Assert.assertFalse(lines.isEmpty(), "temp config classes file cannot be empty");

            boolean classFound = false;
            for (String line : lines) {
                if (line.contains("org.wso2.carbon.kernel.annotationprocessor.Configurations")) {
                    classFound = true;
                }
            }
            Assert.assertTrue(classFound, "expected configuration class doesnot exists");
        } catch (IOException e) {
            Assert.fail("error while reading temp file.", e);
        }
    }

    @AfterClass
    public void cleanOutputs() throws IOException {
        File file = Paths.get(System.getProperty("user.dir"), "temp_config_classnames.txt").toFile();
        FileUtils.forceDeleteOnExit(file);
        File classFile = Paths.get(System.getProperty("user.dir"), packagePath, "Configurations.class").toFile();
        FileUtils.forceDeleteOnExit(classFile);
        classFile = Paths.get(System.getProperty("user.dir"), packagePath, "Transport.class").toFile();
        FileUtils.forceDeleteOnExit(classFile);
        classFile = Paths.get(System.getProperty("user.dir"), packagePath, "Transports.class").toFile();
        FileUtils.forceDeleteOnExit(classFile);
    }
}
