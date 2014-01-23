/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.feature.mgt.core;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.wso2.carbon.feature.mgt.core.util.ProvisioningUtils;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.core.operations.ProfileChangeOperation;


public class ProvCommandProviderExt implements CommandProvider{

    /**
     * Summary of the given profile configuration.
     *
     * @param ci CommandInterpreter
     * @throws Exception
     */
    public void _provlconfigurationhistory(CommandInterpreter ci) throws Exception {

        long timestamp;
        try {
            timestamp = Long.parseLong(ci.nextArgument());
        }catch (NumberFormatException e){
            System.out.println("Invalid timestamp.");
            return;
        }

        System.out.println("Following is a comparison of this configuration with the current configuration.");
        System.out.println("Reverting to this Configuration, would results in performing following actions on the Current Configuration.");
        System.out.println();

        ProfileChangeOperation profileChangeOperation =
                OperationFactory.getProfileChangeOperation(OperationFactory.REVERT_ACTION);

        //This is required for revert operations.
        profileChangeOperation.setTimestamp(timestamp);

        ResolutionResult resolutionResult = profileChangeOperation.reviewProfileChangeAction(
                    ProvisioningUtils.getProfile());

        IInstallableUnit[] installableUnits = resolutionResult.getReviewedInstallableUnits();
        if(installableUnits.length > 0) {
            System.out.println("Following features will be installed.");
            System.out.println("-------------------------------------");

            for(IInstallableUnit iInstallableUnit: installableUnits){
                System.out.println(iInstallableUnit.getId() + " " + iInstallableUnit.getVersion());
            }
            System.out.println();

        }

        IInstallableUnit[] uninstallableUnits = resolutionResult.getReviewedUninstallableUnits();
        if(uninstallableUnits.length > 0) {
            System.out.println("Following features will be uninstalled.");
            System.out.println("---------------------------------------");

            for(IInstallableUnit uninstallableUnit: uninstallableUnits){
                System.out.println(uninstallableUnit.getId() + " " + uninstallableUnit.getVersion());
            }
        }
    }

    public String getHelp() {
        return "---Provisioning Commands Ext---\n" +
               "\tprovlconfigurationhistory <timestamp> - Summary of the specified profile configuration.\n";
    }
}
