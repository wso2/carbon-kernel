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
package org.wso2.carbon.feature.mgt.services.prov.utils;

import org.apache.axis2.context.MessageContext;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.metadata.*;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepository;
import org.wso2.carbon.feature.mgt.core.ProvisioningException;
import org.wso2.carbon.feature.mgt.core.ResolutionResult;
import org.wso2.carbon.feature.mgt.core.util.IUPropertyUtils;
import org.wso2.carbon.feature.mgt.core.util.ProvisioningUtils;
import org.wso2.carbon.feature.mgt.core.util.RepositoryUtils;
import org.wso2.carbon.feature.mgt.services.prov.data.*;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProvWSUtils {

    public static RepositoryInfo[] wrapURIsAsRepositories(URI[] uriArray) throws ProvisioningException {
        if (uriArray == null) {
            return new RepositoryInfo[0];
        }

        RepositoryInfo[] repositories = new RepositoryInfo[uriArray.length];

        for (int index = 0; index < uriArray.length; index++) {
            URI location = uriArray[index];
            boolean enabled = RepositoryUtils.isRepositoryEnabled(location);
            String nickName = RepositoryUtils.getMetadataRepositoryProperty(location, IRepository.PROP_NICKNAME);
            if (nickName == null) {
                nickName = RepositoryUtils.getMetadataRepositoryProperty(location, IRepository.PROP_NAME);
            }
            if (nickName == null) {
                nickName = " ";
            }
            RepositoryInfo repository = new RepositoryInfo(location.toString(), nickName, enabled);
            repositories[index] = repository;
        }
        return repositories;
    }

    public static URI[] mergeURIArrays(URI[] array1, URI[] array2) {
        int arrayLength = array1.length + array2.length;
        URI[] mergedArray = new URI[arrayLength];
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        return mergedArray;
    }

    public static FeatureInfo[] wrapIUsAsFeatures(IInstallableUnit[] installableUnits) {
        if (installableUnits == null) {
            return new FeatureInfo[0];
        }

        FeatureInfo[] features = new FeatureInfo[installableUnits.length];
        for (int index = 0; index < installableUnits.length; index++) {
            FeatureInfo feature = new FeatureInfo();
            IInstallableUnit installableUnit = installableUnits[index];
            features[index] = feature;

            feature.setFeatureID(installableUnit.getId());
            feature.setFeatureVersion(installableUnit.getVersion().toString());
            feature.setFeatureName(IUPropertyUtils.getIUProperty(installableUnit, IInstallableUnit.PROP_NAME));

            String provider = IUPropertyUtils.getIUProperty(installableUnit, IInstallableUnit.PROP_PROVIDER);
            if (provider == null) {
                provider = " ";
            }
            feature.setProvider(provider);

            String description = IUPropertyUtils.getIUProperty(installableUnit, IInstallableUnit.PROP_DESCRIPTION);
            if (description == null) {
                description = "No description provided";
            }
            feature.setDescription(description);
        }
        return features;
    }

    public static Feature[] wrapInstalledFeatures(IInstallableUnit[] installableUnits, IQueryable queryable) {
        if (installableUnits == null) {
            return new Feature[0];
        }

        HashMap<String, IInstallableUnit> installedFeaturesMap = getInstalledUnitsMap(installableUnits);

        ArrayList<Feature> featuresList = new ArrayList<Feature>();
        for (IInstallableUnit iu : installableUnits) {
            if (!iu.getId().startsWith("org.eclipse.equinox")&& !iu.getId().startsWith("carbon.product.id")) {
                featuresList.add(processFeature(iu, queryable, installedFeaturesMap));
            }
        }
        return featuresList.toArray(new Feature[featuresList.size()]);
    }

    public static Feature[] wrapAvailableFeatures(IInstallableUnit[] availableInstallableUnits,
                                                  IInstallableUnit[] installedInstallableUnits, IQueryable queryable) {
        if (availableInstallableUnits == null) {
            return new Feature[0];
        }

        HashMap<String, IInstallableUnit> installedFeaturesMap = getInstalledUnitsMap(installedInstallableUnits);

        ArrayList<Feature> featuresList = new ArrayList<Feature>();
        for (IInstallableUnit iu : availableInstallableUnits) {
            if (iu.getId().startsWith("org.eclipse.equinox") || ProvisioningUtils.isIUInstalled(iu, installedFeaturesMap)) {
                continue;
            }
            featuresList.add(processFeature(iu, queryable, installedFeaturesMap));
        }
        return featuresList.toArray(new Feature[featuresList.size()]);
    }

    public static Feature processFeature(IInstallableUnit iu, IQueryable<IInstallableUnit> queryable, HashMap<String, IInstallableUnit> installedFeaturesMap) {
        Stack<IInstallableUnit> iuStack;
        Stack<Feature> featureStack;
        Feature groupFeature = wrapIU(iu);
        iuStack = new Stack<IInstallableUnit>();
        featureStack = new Stack<Feature>();
        iuStack.add(iu);
        featureStack.add(groupFeature);

        while (!iuStack.isEmpty() && !featureStack.isEmpty()) {
            IInstallableUnit popedIU = iuStack.pop();
            Feature popedFeature = featureStack.pop();

            if (popedFeature.isInstalled()) {
                popedFeature.setRequiredFeatures(new Feature[0]);
                continue;
            }

            //Processing required Features.
            ArrayList<Feature> requiredFeaturesList = new ArrayList<Feature>();
            Collection<IRequirement> requiredCapabilities = popedIU.getRequirements();
            for (IRequirement requiredCapability : requiredCapabilities) {
                IMatchExpression<IInstallableUnit> matchExpression = requiredCapability.getMatches();
                IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery(IInstallableUnit.class, matchExpression, new Object[0]);
                IInstallableUnit[] requiredInstallableUnits = queryable.query(query,
                        new NullProgressMonitor()).toArray(IInstallableUnit.class);
                //sorting here to get the first element from the search
                Arrays.sort(requiredInstallableUnits);
                for (IInstallableUnit installableUnit : requiredInstallableUnits) {
                    if (installableUnit.getId().endsWith("feature.group") &&
                            !installableUnit.getId().startsWith("org.eclipse.equinox") &&
                            "org.eclipse.equinox.p2.type.category".equals(popedFeature.getFeatureType())) {
                        IInstallableUnit requiredIU = (requiredInstallableUnits.length == 0) ? null : installableUnit;
                        if (requiredIU != null) {
                            Feature requiredFeature = wrapIU(requiredIU);
                            if (!ProvisioningUtils.isIUInstalled(requiredIU, installedFeaturesMap)) {
                                requiredFeature.setRequired(true);
                                iuStack.push(requiredIU);
                                featureStack.add(requiredFeature);
                                requiredFeaturesList.add(requiredFeature);
                            }
                            // Break the loop if we are processing a nested.category because we only need the element
                            // from the search for a nested category. This is due to having multiple features in
                            // requiredInstallableUnits, when they are defined without any "match" at importFeatureDef.
                            // This check can be removed once all the features for a nested.category is properly
                            // configured using "perfect" match. See : CARBON-15127
                            if (popedIU.getId().contains("category.feature")) {
                                break;
                            }
                        }
                    }
                }
            }
            popedFeature.setRequiredFeatures(requiredFeaturesList.toArray(
                    new Feature[requiredFeaturesList.size()]));
        }
        return groupFeature;
    }

    public static Feature wrapIU(IInstallableUnit iu) {
        Feature feature = new Feature();
        feature.setFeatureID(iu.getId());
        feature.setFeatureVersion(iu.getVersion().toString());
        feature.setFeatureName(IUPropertyUtils.getIUProperty(iu, IInstallableUnit.PROP_NAME));

        String provider = IUPropertyUtils.getIUProperty(iu, IInstallableUnit.PROP_PROVIDER);
        if (provider == null) {
            provider = " ";
        }
        feature.setProvider(provider);

//        String featureType = iu.getProperty("org.wso2.carbon.p2.category.type");
//        if(featureType == null){
//            //not a carbon.p2.category type, hence check if it is a feature category
//        	if(iu.getProperty("org.eclipse.equinox.p2.type.category") != null && Boolean.parseBoolean(iu.getProperty("org.eclipse.equinox.p2.type.category"))){
//        		//feature type is featureCategory
//        		featureType = "org.eclipse.equinox.p2.type.category";
//        	}
//        	else{
//        		featureType="";
//        	}
//
//        }

        String featureType = iu.getProperty("org.eclipse.equinox.p2.type.category");
        if (featureType == null) {
            // This feature is not a category
            featureType = iu.getProperty("org.wso2.carbon.p2.category.type");
            if (featureType == null) {
                featureType = "";
            }
        } else {
            featureType = "org.eclipse.equinox.p2.type.category";
        }

        feature.setFeatureType(featureType);

        String featureDescription = IUPropertyUtils.getIUProperty(iu, IInstallableUnit.PROP_DESCRIPTION);
        if (featureDescription == null) {
            featureDescription = ""; // setting the empty String if the property is null
        }
        feature.setFeatureDescription(featureDescription);

        return feature;
    }

    public static IInstallableUnit[] getRequiredFeatureIUs(IInstallableUnit featureIU, IQueryable queryable) {
        ArrayList<IInstallableUnit> requiredIUList = new ArrayList<IInstallableUnit>();
        Collection<IRequirement> requiredCapabilities = featureIU.getRequirements();
        for (IRequirement requiredCapability : requiredCapabilities) {
            IMatchExpression<IInstallableUnit> matchExpression = requiredCapability.getMatches();
            IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery(matchExpression, new Object[0]);
            Collection<IInstallableUnit> requiredInstallableUnits = queryable.query(query,
                    new NullProgressMonitor()).toUnmodifiableSet();
            for (IInstallableUnit installableUnit : requiredInstallableUnits) {
                if (IInstallableUnit.NAMESPACE_IU_ID.equals(installableUnit.getId()) && installableUnit.getId().endsWith("feature.group")
                        && !installableUnit.getId().startsWith("org.eclipse.equinox")) {
                    requiredIUList.add(installableUnit);
                }
            }
        }
        IInstallableUnit[] requiredIUs = new IInstallableUnit[requiredIUList.size()];
        return requiredIUList.toArray(requiredIUs);
    }

    public static FeatureInfo wrapIUsAsFeaturesWithDetails(IInstallableUnit installableUnit, ILicense license, ICopyright copyright) {
        if (installableUnit == null) {
            return new FeatureInfo();
        }

        FeatureInfo feature = new FeatureInfo();
        feature.setFeatureID(installableUnit.getId());
        feature.setFeatureVersion(installableUnit.getVersion().toString());
        feature.setFeatureName(IUPropertyUtils.getIUProperty(installableUnit, IInstallableUnit.PROP_NAME));

        String provider = IUPropertyUtils.getIUProperty(installableUnit, IInstallableUnit.PROP_PROVIDER);
        if (provider == null) {
            provider = " ";
        }
        feature.setProvider(provider);

        String description = IUPropertyUtils.getIUProperty(installableUnit, IInstallableUnit.PROP_DESCRIPTION);
        if (description == null) {
            description = "No description provided";
        }
        feature.setDescription(description);
        if (license != null) {
        	feature.setLicenseInfo(wrapP2LicensesAsLicenses(new ILicense[]{license})[0]);
        }
        if (copyright != null){
        	feature.setCopyrightInfo(wrapICopyrightAsCopyrightInfo(copyright));
        }
        return feature;
    }

    public static IInstallableUnit[] deriveRepositoryIUsFromFeatures(FeatureInfo[] features) throws ProvisioningException {
        if (features == null) {
            return new IInstallableUnit[0];
        }
        HashMap<String, IInstallableUnit> iuMap = new HashMap<String, IInstallableUnit>();

        for (FeatureInfo featureInfo : features) {
            IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(featureInfo.getFeatureID(),
                    new VersionRange(Version.create(featureInfo.getFeatureVersion()),
                            true, Version.create(featureInfo.getFeatureVersion()), true));
            IQueryable<IInstallableUnit> queryable = RepositoryUtils.getQuerybleRepositoryManager(null);
        IInstallableUnit[] installableUnits = queryable.query(query,new NullProgressMonitor()).toArray(IInstallableUnit.class);

            if (installableUnits[0] == null) {
                continue;
            }

            addIUtoMap(iuMap, installableUnits[0]);
        }
        return iuMap.values().toArray(new IInstallableUnit[iuMap.values().size()]);
    }

    private static void addIUtoMap(HashMap<String, IInstallableUnit> iuMap, IInstallableUnit iu) {
        IInstallableUnit addedIU = iuMap.get(iu.getId());
        if (addedIU == null) {
            iuMap.put(iu.getId(), iu);
        } else if (addedIU.getVersion().compareTo(iu.getVersion()) < 0) {
            iuMap.put(iu.getId(), iu);
        }
    }

    public static IInstallableUnit[] deriveProfileIUsFromFeatures(FeatureInfo[] features) throws ProvisioningException {
        if (features == null) {
            return new IInstallableUnit[0];
        }
        IInstallableUnit[] ius = new IInstallableUnit[features.length];

        for (int i = 0; i < features.length; i++) {
            FeatureInfo feature = features[i];
            IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(feature.getFeatureID(),
                    new VersionRange(feature.getFeatureVersion()));
            IInstallableUnit[] installableUnits = ProvisioningUtils.getProfile().query(query,
                    new NullProgressMonitor()).toArray(IInstallableUnit.class);
            ius[i] = installableUnits[0];
        }
        return ius;
    }

    public static ProvisioningActionResultInfo wrapResolutionResult(ResolutionResult resolutionResult) {
        ProvisioningActionResultInfo resultInfo = new ProvisioningActionResultInfo();
        resultInfo.setReviewedInstallableFeatures(wrapIUsAsFeatures(resolutionResult.getReviewedInstallableUnits()));
        resultInfo.setReviewedUninstallableFeatures(wrapIUsAsFeatures(resolutionResult.getReviewedUninstallableUnits()));
        resultInfo.setFailedinstallableFeatures(wrapIUsAsFeatures(resolutionResult.getFailedInstallableUnits()));
        resultInfo.setFailedUninstallableFeatures(wrapIUsAsFeatures(resolutionResult.getFailedUninstallableUnits()));
        resultInfo.setDetailedDescription(resolutionResult.getSummaryReport());
        resultInfo.setSummary(resolutionResult.getSummaryReport());
        resultInfo.setSize(resolutionResult.getInstallationSize());
        return resultInfo;
    }

    public static void saveResolutionResult(String actionType, ResolutionResult resolutionResult,
                                            MessageContext messageContext) throws Exception {
        HttpSession session = getHttpSession(messageContext);
        if (session != null) {
            session.setAttribute(actionType, resolutionResult);
        } else {
            throw new Exception("HttpSession is null");
        }
    }

    public static ResolutionResult getResolutionResult(String actionType,
                                                       MessageContext messageContext) throws Exception {
        HttpSession session = getHttpSession(messageContext);
        if (session != null) {
            return (ResolutionResult) session.getAttribute(actionType);
        } else {
            throw new Exception("HttpSession is null");
        }
    }

    public static LicenseInfo[] wrapP2LicensesAsLicenses(ILicense[] p2Licenses) {
        if (p2Licenses == null) {
            return new LicenseInfo[0];
        }

        LicenseInfo[] licenses = new LicenseInfo[p2Licenses.length];
        for (int index = 0; index < licenses.length; index++) {
            LicenseInfo license = wrapP2LicenseAsLicense(p2Licenses[index]);
            licenses[index] = license;
        }
        return licenses;
    }

    public static LicenseInfo wrapP2LicenseAsLicense(ILicense p2License) {
        if (p2License == null) {
            return new LicenseInfo();
        }
        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setBody((p2License.getBody() == null) ? "" : p2License.getBody());
        licenseInfo.setURL((p2License.getLocation() == null) ? "" : p2License.getLocation().toString());
        return licenseInfo;
    }

	public static LicenseFeatureHolder[] wrapP2LicensesAsLicenses(
			Map<ILicense, List<IInstallableUnit>> licenseFeatureMap) {
		if (licenseFeatureMap == null) {
			return new LicenseFeatureHolder[0];
		}
		List<LicenseFeatureHolder> licenseFeatureHolders = new ArrayList<LicenseFeatureHolder>();
		List<IInstallableUnit> iInstallableUnits = null;
		for (ILicense iLicense : licenseFeatureMap.keySet()) {
			LicenseFeatureHolder licenseFeatureHolder = new LicenseFeatureHolder();
			if (iLicense == null) {
				licenseFeatureHolder.setLicenseInfo(null);
			} else {
				licenseFeatureHolder.setLicenseInfo(wrapP2LicenseAsLicense(iLicense));
			}
			iInstallableUnits = licenseFeatureMap.get(iLicense);
			licenseFeatureHolder.setFeatureInfo(
					wrapIUsAsFeatures(iInstallableUnits.toArray(new IInstallableUnit[iInstallableUnits.size()])));
			licenseFeatureHolders.add(licenseFeatureHolder);
		}
		return licenseFeatureHolders.toArray(new LicenseFeatureHolder[licenseFeatureHolders.size()]);
	}

    public static CopyrightInfo wrapICopyrightAsCopyrightInfo(ICopyright iCopyright) {
        if (iCopyright == null) {
            return new CopyrightInfo();
        }
        CopyrightInfo copyrightInfo = new CopyrightInfo();
        copyrightInfo.setBody(iCopyright.getBody());
        copyrightInfo.setURL((iCopyright.getLocation() == null) ? "" : iCopyright.getLocation().toString());
        return copyrightInfo;
    }

    public static ProfileHistory getProfileHistoryFromTimestamp(long timestamp) {
        ProfileHistory profileHistory = new ProfileHistory();
        profileHistory.setTimestamp(timestamp);
        SimpleDateFormat monthDayYearformatter = new SimpleDateFormat("MMMMM dd, yyyy 'at' HH:mm:ss z");
        Date date = new Date(timestamp);
        profileHistory.setSummary(monthDayYearformatter.format(date));
        return profileHistory;
    }

    public static HttpSession getHttpSession(MessageContext messageContext) {
        return (HttpSession) messageContext.getProperty("comp.mgt.servlet.session");
    }


    public static HashMap<String, IInstallableUnit> getInstalledUnitsMap(IInstallableUnit[] installedUnits) {
        HashMap<String, IInstallableUnit> installedUnitsMap = new HashMap<String, IInstallableUnit>(installedUnits.length);
        for (IInstallableUnit iu : installedUnits) {
            installedUnitsMap.put(iu.getId(), iu);
        }
        return installedUnitsMap;
    }


}
