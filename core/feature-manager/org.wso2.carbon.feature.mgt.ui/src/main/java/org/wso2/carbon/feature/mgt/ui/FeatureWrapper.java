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
package org.wso2.carbon.feature.mgt.ui;


import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;

import java.io.Serializable;
import java.util.Arrays;

/**
 * wrapper class for code-generated feature classes. This gives better handle to us, such
 * as defining a similarity policy
 */

public class FeatureWrapper implements Serializable {
    private int height;
    private boolean hasChildren;
    private Feature wrappedFeature;
    private String parentElementID;
    private boolean isParentComposite;
    private boolean hiddenRow;
    private boolean isNote;

	private FeatureWrapper[] requiredFeatures = new FeatureWrapper[]{};
    public static final String CATEGORY_FEATURE_TYPE = "org.eclipse.equinox.p2.type.category";
    public static final String COMPOSITE_FEATURE_TYPE = "composite";
    public static final String MANDATORY_FEATURE_TYPE = "mandatory";

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean isHiddenRow() {
        return hiddenRow;
    }

    public void setHiddenRow(boolean hideRow) {
        this.hiddenRow = hideRow;
    }

    public FeatureWrapper[] getRequiredFeatures() {
        return requiredFeatures;
    }

    public void setRequiredFeatures(FeatureWrapper[] requiredFeatures) {
        this.requiredFeatures = Arrays.copyOf(requiredFeatures, requiredFeatures.length);
    }

    public Feature getWrappedFeature() {
        return wrappedFeature;
    }

    public void setWrappedFeature(Feature wrappedFeature) {
        this.wrappedFeature = wrappedFeature;
    }

    public String getParentElementID() {
        return parentElementID;
    }

    public void setParentElementID(String parentElementID) {
        this.parentElementID = parentElementID;
    }

    public boolean isNote() {
        return isNote;
    }

    public void setNote(boolean note) {
        isNote = note;
    }

    /**
     * @return indicates whether the parent of this feature is 
     * a product-specific composite feature
     */
    public boolean isParentComposite() {
    	return isParentComposite;
    }

	public void setParentComposite(boolean isParentComposite) {
    	this.isParentComposite = isParentComposite;
    }

    /**
     * define feature equality based on their feature Id values
     * @param ob object that we are comparing with
     * @return  true if the feature Id is equal;  false otherwise
     */
    @Override
    public boolean equals(Object ob){
        if(!(ob instanceof FeatureWrapper)){
            return false;
}
        boolean isFeatureIdMatching = wrappedFeature.getFeatureID().equalsIgnoreCase(((FeatureWrapper)ob).getWrappedFeature().getFeatureID());
        boolean isFeatureVersionMatching = wrappedFeature.getFeatureVersion().equalsIgnoreCase(((FeatureWrapper)ob).getWrappedFeature().getFeatureVersion());

        return  (isFeatureIdMatching && isFeatureVersionMatching);
    }


    /**
     *
     * @return  hashcode of the feature based on the featureId value
     */
    @Override
    public int hashCode() {
        return wrappedFeature.getFeatureID().hashCode();
    }
}
