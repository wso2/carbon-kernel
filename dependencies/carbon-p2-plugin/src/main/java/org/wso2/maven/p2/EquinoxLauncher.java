/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.maven.p2;

import java.util.ArrayList;

import org.wso2.maven.p2.generate.utils.P2Constants;

public class EquinoxLauncher {

    /**
     * Name of the launcher jar
     *
     * @parameter
     */
    private String launcherJar;

    /**
     * launcherFiles
     *
     * @parameter
     */
    private ArrayList launcherFiles;

    public EquinoxLauncher(){

    }

    public String getLauncherJar() {
    	if (launcherJar==null)
    		launcherJar="org.eclipse.equinox.launcher";
        return launcherJar;
    }

    public void setLauncherJar(String launcherJar) {
        this.launcherJar = launcherJar;
    }

    public ArrayList getLauncherFiles() {
    	if (launcherFiles==null){
    		launcherFiles=new ArrayList();
			for(String p2File:P2Constants.OSGI_FILES){
				launcherFiles.add(p2File);
			}
    	}
        return launcherFiles;
    }

    public void setLauncherFiles(ArrayList launcherFiles) {
        this.launcherFiles = launcherFiles;
    }
}
