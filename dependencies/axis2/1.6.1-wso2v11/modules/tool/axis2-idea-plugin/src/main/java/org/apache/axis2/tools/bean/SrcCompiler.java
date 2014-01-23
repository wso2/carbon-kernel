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

package org.apache.axis2.tools.bean;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: keith chapman
 * Date: 28/09/2006
 * Time: 16:35:06
 */
public class SrcCompiler extends Javac {

    Project project;

    public SrcCompiler() {
        project = new Project();
        this.setProject(project);
        project.init();

    }

    public void compileSource(String tempPath) {

          Path path;
          File destDir = new File(tempPath + File.separator + "classes");
          destDir.mkdir();

          Path srcPath = new Path(project, tempPath + File.separator + "src");
          this.setSrcdir(srcPath);
          this.setDestdir(destDir);
          this.setIncludes("**" + File.separator + "*.java, *.java");
          URL url = getClass().getResource("/icons");
          File lib = new File(url.getPath() + File.separator + ".." + File.separator + ".." + File.separator + "lib");
          File[] files;
          files = lib.listFiles();

          Path classpath = new Path(project);
          for (int count = 0; count < files.length; count++) {
              path = new Path(project, files[count].getAbsolutePath());
              classpath.add(path);
          }
          this.setClasspath(classpath);
         this.setFork(true);
          this.perform();

      }

}
