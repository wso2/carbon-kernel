/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.feature.mgt.services.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.wso2.carbon.feature.mgt.core.ResolutionResult;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.core.operations.ProfileChangeOperation;
import org.wso2.carbon.feature.mgt.core.util.ProvisioningUtils;
import org.wso2.carbon.feature.mgt.services.prov.ProvisioningAdminService;
import org.wso2.carbon.feature.mgt.services.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.services.prov.data.ProfileHistory;
import org.wso2.carbon.feature.mgt.services.prov.data.ProvisioningActionResultInfo;
import org.wso2.carbon.feature.mgt.services.prov.utils.ProvWSUtils;

public class FeatureManagerCommandProvider implements CommandProvider {

    public void _getInstallationHistory(CommandInterpreter ci) throws Exception {
        String arg = ci.nextArgument();

        if (arg == null) {
            ProvisioningAdminService provisioningAdminService = new ProvisioningAdminService();
            ProfileHistory[] profileHistoryArray = provisioningAdminService.getProfileHistory();

            for(int i = 0; i < profileHistoryArray.length; i++){
                if( i == profileHistoryArray.length-1 ){
                    System.out.println("Current state" + " " + profileHistoryArray[i].getSummary());
                } else {
                    System.out.println(profileHistoryArray[i].getTimestamp() + " " + profileHistoryArray[i].getSummary());
                }
            }
            return;
        }

        ProvisioningActionResultInfo resolutionResult = ProvWSUtils.wrapResolutionResult(
                getResolutionResult(Long.parseLong(arg)));
        System.out.println("-- Installed features in this configuration");
        for (FeatureInfo featureInfo : resolutionResult.getReviewedUninstallableFeatures()) {
            System.out.println(featureInfo.getFeatureName() + " " + featureInfo.getFeatureVersion());
        }

        System.out.println();
        System.out.println("-- Uninstalled features in this configuration");
        for (FeatureInfo featureInfo : resolutionResult.getReviewedInstallableFeatures()) {
            System.out.println(featureInfo.getFeatureName() + " " + featureInfo.getFeatureVersion());
        }
    }

    public void _revert(CommandInterpreter ci) throws Exception {
        String arg = ci.nextArgument();
        if (arg == null) {
            throw new Exception("timestamp argument is missing");
        }
        ResolutionResult resolutionResult = getResolutionResult((Long.parseLong(arg)));
        ProvisioningUtils.performProvisioningAction(resolutionResult);
        System.out.println("Successfully reverted to " + arg);
        System.out.println("Changes will get applied once you restart the server.");
    }

    private ResolutionResult getResolutionResult(long timestamp) throws Exception {
        ProfileChangeOperation profileChangeOperation =
                OperationFactory.getProfileChangeOperation(OperationFactory.REVERT_ACTION);
        profileChangeOperation.setTimestamp(timestamp);
        ResolutionResult resolutionResult = profileChangeOperation.reviewProfileChangeAction(
                ProvisioningUtils.getProfile());

        return resolutionResult;
    }

    public String getHelp() {
        return "---Feature Manager (WSO2 Carbon)---\n" +
                "\tgetInstallationHistory [<timestamp>]- List all installation history, or list history based on " +
                "the specified timestamp\n" +
                "\trevert <timestamp> - Revert to a previous configuration\n";
    }
}
