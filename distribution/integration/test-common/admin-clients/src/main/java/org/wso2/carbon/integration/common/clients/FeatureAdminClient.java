/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.stub.ProvisioningAdminServiceStub;
import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.LicenseInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProfileHistory;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionResultInfo;
import org.wso2.carbon.feature.mgt.ui.FeatureWrapper;
import org.wso2.carbon.feature.mgt.ui.util.Utils;
import org.wso2.carbon.integration.framework.utils.AuthenticateStubUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

public class FeatureAdminClient {

    private static final Log log = LogFactory.getLog(FeatureAdminClient.class);

    private String serviceName = "ProvisioningAdminService";
    private org.wso2.carbon.feature.mgt.stub.ProvisioningAdminServiceStub provAdminStub;

    private boolean isComplete = false;
    private boolean isError = false;
    private Exception exception;

    private ProfileHistory[] profileHistories = new ProfileHistory[]{};

    public FeatureAdminClient(String backendURL, String sessionCookie) throws AxisFault,
                                                                              XPathExpressionException {
        String endPoint = backendURL + serviceName;
        provAdminStub = new ProvisioningAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, provAdminStub);
    }

    public ProvisioningActionResultInfo reviewInstallFeaturesAction(FeatureInfo[] features)
            throws RemoteException {
        ProvisioningActionResultInfo provisioningActionResultInfo = null;
        ProvisioningActionInfo provisioningActionInfo = new ProvisioningActionInfo();
        provisioningActionInfo.setFeaturesToInstall(features);
        provisioningActionInfo.setActionType(OperationFactory.INSTALL_ACTION);
        provisioningActionResultInfo = provAdminStub.reviewProvisioningAction(provisioningActionInfo);
        return provisioningActionResultInfo;
    }

    public FeatureInfo getInstalledFeatureDetails(String featureID, String featureVersion)
            throws RemoteException {
        return provAdminStub.getInstalledFeatureInfo(featureID, featureVersion);
    }

    public LicenseInfo[] getLicensingInformation() throws Exception {
        return provAdminStub.getLicensingInformation();
    }

    public void performInstallation(String actionType) throws Exception {
            provAdminStub.performProvisioningAction(actionType);
    }

    public FeatureWrapper[] getInstalledFeatures() throws Exception {
            Feature[] features = provAdminStub.getAllInstalledFeatures();
        return Utils.processFeatureTree(features, false, 0);
    }

    public FeatureInfo[] getInstalledFeaturesWithProperty(String key, String value)
            throws RemoteException {
        return provAdminStub.getInstalledFeaturesWithProperty(key, value);
    }

    public ProvisioningActionResultInfo reviewUninstallFeaturesAction(FeatureInfo[] features)
            throws RemoteException {
        ProvisioningActionInfo provisioningActionInfo = new ProvisioningActionInfo();
        provisioningActionInfo.setFeaturesToUninstall(features);
        provisioningActionInfo.setActionType(OperationFactory.UNINSTALL_ACTION);
        return provAdminStub.reviewProvisioningAction(provisioningActionInfo);
    }

    public ProfileHistory[] getProfileHistory() throws Exception {
            if (CarbonUtils.isRunningOnLocalTransportMode()) {
                profileHistories = provAdminStub.getProfileHistory();
            }
        return profileHistories;
    }

    public ProvisioningActionResultInfo reviewRevertPlan(String timestampString) throws Exception {
        ProvisioningActionInfo provisioningActionInfo = new ProvisioningActionInfo();
        provisioningActionInfo.setTimestamp(Long.parseLong(timestampString));
        provisioningActionInfo.setActionType(OperationFactory.REVERT_ACTION);
        return provAdminStub.reviewProvisioningAction(provisioningActionInfo);
    }

    public void removeAllFeaturesWithProperty(String key, String value) throws Exception {
            if ("org.wso2.carbon.p2.category.type".equals(key) && "server".equalsIgnoreCase(value)) {
                provAdminStub.removeAllServerFeatures();
            } else if ("org.wso2.carbon.p2.category.type".equals(key) && "console".equalsIgnoreCase(value)) {
                provAdminStub.removeAllConsoleFeatures();
            }
    }

    public String getInstallActionType() {
        return OperationFactory.INSTALL_ACTION;
    }

    public String getUninstallActionType() {
        return OperationFactory.UNINSTALL_ACTION;
    }

    public String getRevertActionType() {
        return OperationFactory.REVERT_ACTION;
    }

    private void handleCallback() throws Exception {
        int i = 0;
        while (!isComplete && !isError) {
            Thread.sleep(500);
            i++;
            if (i > 120 * 2400) {
                throw new Exception("Response not received within 4 hours");
            }
        }

        if (isError) {
            isError = false;
            throw exception;
        } else {
            isComplete = false;
        }
    }

}
