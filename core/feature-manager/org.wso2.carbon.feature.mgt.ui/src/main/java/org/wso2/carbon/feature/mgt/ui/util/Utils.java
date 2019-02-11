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
package org.wso2.carbon.feature.mgt.ui.util;

import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;
import org.wso2.carbon.feature.mgt.ui.FeatureWrapper;

import java.util.*;

public class Utils {

    public static int getMaxHeight(FeatureWrapper[] featureWrappers, int maxheight) {
        for (FeatureWrapper featureWrapper : featureWrappers) {
            if (maxheight < featureWrapper.getHeight()) {
                maxheight = featureWrapper.getHeight();
            }

            if (featureWrapper.getRequiredFeatures() != null && featureWrapper.getRequiredFeatures().length > 0) {
                maxheight = getMaxHeight(featureWrapper.getRequiredFeatures(), maxheight);
            }
        }
        return maxheight;
    }

    /**
     * Recursive algorithm to filter our features and their required features to make them ready to display in the UI.
     * @param features features list
     * @param groupByCategory indicate whether we need to group by category or not.
     * @param height depth of the tree
     * @return the filtered list of the features.
     */
    public static FeatureWrapper[] processFeatureTree(Feature[] features, boolean groupByCategory, int height) {
        List<FeatureWrapper> featureWrappers = new ArrayList<FeatureWrapper>(features.length);

        for (Feature feature : features) {
            if(!groupByCategory && FeatureWrapper.CATEGORY_FEATURE_TYPE.equals(feature.getFeatureType())){
                // If group by category is disabled then filter out all the category features as well.
                continue;
            }

            FeatureWrapper featureWrapper = new FeatureWrapper();
            featureWrappers.add(featureWrapper);
            featureWrapper.setWrappedFeature(feature);
            featureWrapper.setHeight(height);
            featureWrapper.setHiddenRow(true);

            if (feature.getRequiredFeatures() != null && feature.getRequiredFeatures().length > 0) {
                featureWrapper.setHasChildren(true);
                FeatureWrapper[] requiredFeatureWrappers = processFeatureTree(
                        feature.getRequiredFeatures(), groupByCategory, height + 1);
                featureWrapper.setRequiredFeatures(requiredFeatureWrappers);

            } else {
                if (FeatureWrapper.CATEGORY_FEATURE_TYPE.equals(feature.getFeatureType())) {
                    //Creating a dummy FeatureWrapper to show that all the features listed in a category are installed.
                    FeatureWrapper[] requiredFeatureWrappers = new FeatureWrapper[1];
                    requiredFeatureWrappers[0] = new FeatureWrapper();
                    requiredFeatureWrappers[0].setHeight(height + 1);
                    requiredFeatureWrappers[0].setHiddenRow(true);
                    requiredFeatureWrappers[0].setNote(true);

                    Feature wrappedFeature = new Feature();
                    wrappedFeature.setFeatureID(feature.getFeatureID() + "_Note");
                    wrappedFeature.setFeatureName("All features are installed");
                    wrappedFeature.setFeatureVersion(feature.getFeatureVersion());
                    requiredFeatureWrappers[0].setWrappedFeature(wrappedFeature);

                    featureWrapper.setRequiredFeatures(requiredFeatureWrappers);
                    featureWrapper.setHasChildren(true);
                } else {
                    featureWrapper.setHasChildren(false);
                }
            }
        }
        return featureWrappers.toArray(new FeatureWrapper[featureWrappers.size()]);
    }

    /**
     * @param originalFeatures
     * @param filterStr
     * @return filtered feature array
     */
    public static FeatureWrapper[] filterFeatures(FeatureWrapper[] originalFeatures, String filterStr) {
        try {
            List<FeatureWrapper> originalFeatureList = Arrays.asList(originalFeatures);
            ArrayList<FeatureWrapper> matchedFeatureList = new ArrayList<FeatureWrapper>();
            for (FeatureWrapper featureWrapper : originalFeatureList) {
                Feature feature = featureWrapper.getWrappedFeature();
                String featureName = feature.getFeatureName();
                if (featureName != null && featureName.toLowerCase().contains(filterStr.toLowerCase())) {
                    matchedFeatureList.add(featureWrapper);
                }
            }
            FeatureWrapper[] matchedFeatures = new FeatureWrapper[matchedFeatureList.size()];
            return matchedFeatureList.toArray(matchedFeatures);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FeatureWrapper[0];
    }


    /**
     * Given a feature wrapper list, the utility method truncates the prefix & the suffix of the contained features.
     *
     * @param featureList
     * @return prefix & suffix truncated feature wrapper list
     */
    public static FeatureWrapper[] truncatePrefixAndSuffixOfFeature(FeatureWrapper[] featureList){
    	String prefix1="wso2 carbon";
    	String prefix2="wso2carbon";
        String prefix3= "wso2";
    	String suffix="feature";


    	// here we use the actual name & the temporary name which is converted to lower case for comparison
    	//purposes. We are doing the same operations to both  the Strings.
    	for(FeatureWrapper featureWrapper : featureList){
    		 Feature currentFeature=featureWrapper.getWrappedFeature();
    		 String featureName=currentFeature.getFeatureName();
    		 String tempStr=currentFeature.getFeatureName().toLowerCase();
    		 if(tempStr.startsWith(prefix1)){   // if the name starts with the wso2; we test for minor changes
    			 tempStr=tempStr.substring(prefix1.length()).trim();
    			 featureName=featureName.substring(prefix1.length()).trim();
    		 }
    		 else if(tempStr.startsWith(prefix2)){
    			 tempStr= tempStr.substring(prefix2.length()).trim();
    			 featureName=featureName.substring(prefix2.length()).trim();
    		 }else if(tempStr.startsWith(prefix3)){
    			 tempStr= tempStr.substring(prefix3.length()).trim();
    			 featureName=featureName.substring(prefix3.length()).trim();
    		 }
    		 // eliminating the first character if it is a '-'
    		 if(tempStr.startsWith("-")){
    			 tempStr=tempStr.substring(1).trim();
    			 featureName=featureName.substring(1).trim();
    		 }
    		 if(tempStr.endsWith(suffix)){
                 int tempIndex=tempStr.lastIndexOf(suffix);
    			 tempStr=tempStr.substring(0,tempIndex);
    			 featureName=featureName.substring(0,tempIndex);
    		 }

    		 currentFeature.setFeatureName(featureName.trim());

    	}
		return featureList;

    }

    /**
     * sorts the given wrapped features list according to their alphabetical  order (name)
     *
     * @param featureList
     */
    public static void sortAscendingByFeatureName(FeatureWrapper[] featureList) {

        Arrays.sort(featureList, new Comparator() {
            public int compare(Object ob1, Object ob2) {
                String featureName1 = ((FeatureWrapper) ob1).getWrappedFeature().getFeatureName();
                String featureName2 = ((FeatureWrapper) ob2).getWrappedFeature().getFeatureName();

                return featureName1.compareToIgnoreCase(featureName2);
            }
        });
    }

    /**
     * sorts the given wrapped features list according to their counter-alphabetical  order (name)
     *
     * @param featureList
     */
    public static void sortDescendingByFeatureName(FeatureWrapper[] featureList) {
        Arrays.sort(featureList, Collections.reverseOrder(new Comparator() {

            public int compare(Object ob1, Object ob2) {
                String featureName1 = ((FeatureWrapper) ob1).getWrappedFeature().getFeatureName();
                String featureName2 = ((FeatureWrapper) ob2).getWrappedFeature().getFeatureName();

                return featureName1.compareToIgnoreCase(featureName2);
            }


        }));
    }

    /**
     * We are traversing the feature list in order to get the description of a given feature
     *
     * @param featureWrappers
     * @param featureId
     * @return the description of the feature
     */
    public static String getDescriptionOfFeature(FeatureWrapper[] featureWrappers, String featureId) {
        String featureDescription = null;
        for (FeatureWrapper fw : featureWrappers) {
            if (fw.getWrappedFeature().getFeatureID().equals(featureId)) {
                featureDescription = fw.getWrappedFeature().getFeatureDescription();
                break;
            }
        }
        return (featureDescription != null && !featureDescription.equals("")) ? featureDescription : "no description available";
    }

     /**
     * Return a collection of unique featureWrapper array(  after resolving their require features) , for a given set of featureWrappers.
     * The intuitive approach would be to implement this as a recursive method. But cant implement this feature without using
     * instance variables. Therefore we are simulating the recursive calls using stacks.
     *
     * @param featureWrappers
     * @return returns an array of FeatureWrapper objects.
     */
    public static FeatureWrapper[] getUniqueFeatureList(FeatureWrapper[] featureWrappers) {
        Set<FeatureWrapper> featureWrapperSet = new HashSet<FeatureWrapper>();
        for (FeatureWrapper fw : featureWrappers) {
            Stack<FeatureWrapper> featureWrapperStack = new Stack<FeatureWrapper>();
            featureWrapperStack.add(fw);
            while (!featureWrapperStack.isEmpty()) {
                FeatureWrapper popedFeatureWrapper = featureWrapperStack.pop();
                featureWrapperSet.add(popedFeatureWrapper);
                FeatureWrapper[] requireFeatures = popedFeatureWrapper.getRequiredFeatures();
                if (requireFeatures != null && requireFeatures.length > 0) {
                    for (FeatureWrapper rfw : requireFeatures) {
                        featureWrapperStack.push(rfw);
                    }
                }
            }
        }
        return featureWrapperSet.toArray(new FeatureWrapper[0]);
    }
}