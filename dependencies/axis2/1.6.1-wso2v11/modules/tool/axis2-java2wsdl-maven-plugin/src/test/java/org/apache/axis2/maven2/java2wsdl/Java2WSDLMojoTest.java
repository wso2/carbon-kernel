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

package org.apache.axis2.maven2.java2wsdl;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.io.File;


/**
 * Test case for running the java2wsdl goal.
 */
public class Java2WSDLMojoTest extends AbstractMojoTestCase {
    private static final String WSDL_FILE = "target/generated-sources/java2wsdl/service.xml";

    /**
     * Tests running the WSDL generator.
     */
    public void testJava() throws Exception {
        final String dir = "src/test/test1";
        runTest(dir , "java2wsdl" );
        assertTrue( new File(dir, WSDL_FILE).exists());
    }

    protected Java2WSDLMojo newMojo( String pDir, String pGoal ) throws Exception
    {
        final File baseDir = new File(new File(getBasedir()), pDir);
        File testPom = new File( baseDir, "pom.xml" );
        Java2WSDLMojo mojo = (Java2WSDLMojo) lookupMojo( pGoal, testPom );
        MavenProjectStub project = new MavenProjectStub(){
            public File getBasedir() { return baseDir; }
        };
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "outputFileName", WSDL_FILE);
        return mojo;
    }

    protected void runTest( String pDir, String pGoal )
        throws Exception
    {
        newMojo( pDir, pGoal ).execute();
    }
}
