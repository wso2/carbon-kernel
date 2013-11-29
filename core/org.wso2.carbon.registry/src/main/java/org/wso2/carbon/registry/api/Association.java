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
package org.wso2.carbon.registry.api;

/**
 * This is a representation of a uni-directional association on the registry. An association can be
 * created from any resource (or collection) on the registry to another resource (or collection) on
 * the registry or an external resource which can be referred to by a URL.
 * <p/>
 * While the source should be a resource existing on the registry, the destination resource can be
 * anything for which a URL can be given.
 */
public class Association {

    /**
     * Identifies a dependency to a resources.
     */
    public static final String DEPENDS = "depends";

    /**
     * Identifies a usage of a resource.
     */
    @SuppressWarnings("unused")
    public static final String USED_BY = "usedBy";

    private String associationType;
    private String sourcePath;
    private String destinationPath;

    /**
     * Default constructor for the Association Class. Just create an empty association.
     */
    public Association() {
        this(null, null, null);
    }

    /**
     * Construct an association by providing the source, target and the association type.
     *
     * @param sourcePath      the source of the association.
     * @param destinationPath the destination of the association.
     * @param associationType the type of the association.
     */
    public Association(String sourcePath, String destinationPath, String associationType) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.associationType = associationType;
    }

    /**
     * Method to get the source path.
     *
     * @return the source path.
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Method to set the source path.
     *
     * @param sourcePath the source path.
     */
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * Method to get the destination path.
     *
     * @return the destination path.
     */
    public String getDestinationPath() {
        return destinationPath;
    }

    /**
     * Method to set the destination path.
     *
     * @param destinationPath the destination path.
     */
    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    /**
     * Method to get the association type.
     *
     * @return the association type.
     */
    public String getAssociationType() {
        return associationType;
    }

    /**
     * Method to get the association type.
     *
     * @param associationType the association type.
     */
    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

}
