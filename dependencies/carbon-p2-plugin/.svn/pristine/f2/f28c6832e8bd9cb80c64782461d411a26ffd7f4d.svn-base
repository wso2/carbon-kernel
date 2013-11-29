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

import org.apache.maven.plugin.MojoExecutionException;
import org.wso2.maven.p2.generate.feature.Bundle;
import org.wso2.maven.p2.generate.feature.ImportFeature;

public class Feature {
    
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

    public Feature(){
        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return Bundle.getOSGIVersion(version);
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
	protected static Feature getFeature(String bundleDefinition) throws MojoExecutionException{
		String[] split = bundleDefinition.split(":");
		if (split.length>1){
			Feature feature=new Feature();
			feature.setId(split[0]);
			feature.setVersion(split[1]);
			return feature;
		}
		throw new MojoExecutionException("Insufficient feature information provided to determine the feature: "+bundleDefinition) ; 
	}
}
