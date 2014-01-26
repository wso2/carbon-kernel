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


import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.wso2.maven.p2.generate.feature.Bundle;

public class CatFeature {
    
    /**
     * Id of the feature
     *
     * @parameter
     * @required
     */
    private String id;

    /**
     * version of the feature
     *
     * @parameter
     * @required
     */
    private String version;
    
    /**
     * @parameter default-value="${project}"
     */
    private MavenProject project;
    
    private boolean versionReplaced = false;

    public CatFeature(){
        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() throws MojoExecutionException {
    	if (!versionReplaced) {
    		replaceProjectKeysInVersion(project);
    	}
        return Bundle.getOSGIVersion(version);
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
	public void replaceProjectKeysInVersion(MavenProject project) throws MojoExecutionException{
		if (version == null) {
			throw new MojoExecutionException("Could not find the version for featureId: " + getId());
		}
		Properties properties = project.getProperties();
		for(Object key:properties.keySet()){
			version=version.replaceAll(Pattern.quote("${"+key+"}"), properties.get(key).toString());
		}
		versionReplaced = true;
	}
    
}
