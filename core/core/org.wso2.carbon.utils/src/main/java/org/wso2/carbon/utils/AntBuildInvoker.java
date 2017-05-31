/*                                                                             
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.utils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;

/**
 * The class <code>AntBuildInvoker</code> provides a Java utility engine to
 * invoke <code>ANT</code> targets of a given build file.
 */
public class AntBuildInvoker {
    // The ANT build file
    private File buildFile;

    /**
     * The constructor
     * @param buildFile the <code>ANT</code> build file
     */
    public AntBuildInvoker(File buildFile) {
        this.buildFile = buildFile;
    }


    /**
     * Invokes the default target of the build file. Runs in silent mode
     * @throws AntBuildException if invoking the target failed
     */
    public void invokeDefaultTarget() throws AntBuildException {
        invokeTarget(null, false);
    }

    /**
     * Invokes the default target of the build file. Runs in verbose mode
     * @throws AntBuildException if invoking the target failed
     */
    public void invokeDefaultTarget(boolean showOutput) throws AntBuildException {
        invokeTarget(null, showOutput);
    }

    /**
     * Invokes the given target of the build file. Runs in silent mode
     * @param target the target to be executed
     * @throws AntBuildException if invoking the target failed
     */
    public void invokeTarget(String target) throws AntBuildException {
        invokeTarget(target, false);
    }

    /**
     * Invokes the given target of the build file under the given mode (silent/verbose)
     * @param target the target to be executed
     * @throws AntBuildException if invoking the target failed
     */
    public void invokeTarget(String target, boolean showOutput) throws AntBuildException {
        Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        if (showOutput) {
            DefaultLogger consoleLogger = new DefaultLogger();
            consoleLogger.setErrorPrintStream(System.err);
            consoleLogger.setOutputPrintStream(System.out);
            consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
            p.addBuildListener(consoleLogger);
        }

        try {
            p.fireBuildStarted();
            p.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            p.addReference("ant.projectHelper", helper);
            helper.parse(p, buildFile);
            if (target == null || "".equals(target)) {
                p.executeTarget(p.getDefaultTarget());
            } else {
                p.executeTarget(target);
            }
            p.fireBuildFinished(null);
        } catch (BuildException e) {
            p.fireBuildFinished(e);
            throw new AntBuildException(e);
        }
    }
}
