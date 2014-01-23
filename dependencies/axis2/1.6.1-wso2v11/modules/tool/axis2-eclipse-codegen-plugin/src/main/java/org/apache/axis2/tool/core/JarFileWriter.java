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

package org.apache.axis2.tool.core;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;

import java.io.File;
import java.io.IOException;

public class JarFileWriter extends Jar{


    public JarFileWriter() {
        this.setProject(new Project());
        this.getProject().init();
        this.setTaskType("jar");
        this.setTaskName("jar");
        this.setOwningTarget(new org.apache.tools.ant.Target());
    }

    public void writeJarFile(File outputFolder,String outputFileName,File inputFileFolder) throws IOException,Exception {

        if (!outputFolder.exists()){
            outputFolder.mkdir(); //create the output path
        }else{
            if (!outputFolder.isDirectory()){
                return;
            }
        }

        File targetFile = new File(outputFolder,outputFileName);
        this.setBasedir(inputFileFolder);
        this.setDestFile(targetFile);

        //run the task
        this.perform();


    }


}
