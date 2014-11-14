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
package org.wso2.carbon.integration.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.stub.ProvisioningAdminServiceCallbackHandler;
import org.wso2.carbon.feature.mgt.stub.ProvisioningAdminServiceStub;
import org.wso2.carbon.feature.mgt.stub.prov.data.*;
import org.wso2.carbon.feature.mgt.ui.FeatureWrapper;
import org.wso2.carbon.feature.mgt.ui.util.Utils;
import org.wso2.carbon.integration.framework.utils.AuthenticateStubUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.xpath.XPathExpressionException;
import java.util.Arrays;

public class FeatureAdminClient {

    private static final Log log = LogFactory.getLog(FeatureAdminClient.class);

    private String serviceName = "ProvisioningAdminService";
    private org.wso2.carbon.feature.mgt.stub.ProvisioningAdminServiceStub provAdminStub;

    private boolean isComplete = false;
    private boolean isError = false;
    private Exception exception;

    private ProfileHistory[] profileHistories = new ProfileHistory[]{};

    public FeatureAdminClient(String backendURL, AutomationContext automationContext, String sessionCookie) throws AxisFault,
            XPathExpressionException {
        String endPoint = backendURL + serviceName;
        provAdminStub = new ProvisioningAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, provAdminStub);
    }

    public ProvisioningActionResultInfo reviewInstallFeaturesAction(FeatureInfo[] features) throws Exception {
        ProvisioningActionResultInfo provisioningActionResultInfo = null;
        try {
            ProvisioningActionInfo provisioningActionInfo = new ProvisioningActionInfo();
            provisioningActionInfo.setFeaturesToInstall(features);
            provisioningActionInfo.setActionType(OperationFactory.INSTALL_ACTION);
            provisioningActionResultInfo = provAdminStub.reviewProvisioningAction(provisioningActionInfo);
        } catch (AxisFault e) {
            handleException("failed.review.prov.action", e);
        }
        return provisioningActionResultInfo;
    }

    public FeatureInfo getInstalledFeatureDetails(String featureID, String featureVersion) throws Exception {
        FeatureInfo featureInfo = null;
        try {
            featureInfo = provAdminStub.getInstalledFeatureInfo(featureID, featureVersion);
        } catch (AxisFault e) {
            handleException("failed.get.feature.information", e);
        }
        return featureInfo;
    }

    public LicenseInfo[] getLicensingInformation() throws Exception {
        LicenseInfo[] licenseInfo = null;
        try {
            licenseInfo = provAdminStub.getLicensingInformation();
        } catch (AxisFault e) {
            handleException("failed.get.license.info", e);
        }
        return licenseInfo;
    }

    public void performInstallation(String actionType) throws Exception {
        try {
            provAdminStub.performProvisioningAction(actionType);

        } catch (AxisFault e) {
            handleException("failed.perform.prov.action", e);
        }
    }

    public FeatureWrapper[] getInstalledFeatures() throws Exception {
        FeatureWrapper[] featureWrappers = null;
        try {
            Feature[] features = provAdminStub.getAllInstalledFeatures();
            featureWrappers = Utils.processFeatureTree(features, false, 0);
        } catch (AxisFault e) {
            handleException("failed.get.installed.features", e);
        }
        return featureWrappers;
    }

    public FeatureInfo[] getInstalledFeaturesWithProperty(String key, String value) throws Exception {
        FeatureInfo[] featureInfos = null;
        try {
            featureInfos = provAdminStub.getInstalledFeaturesWithProperty(key, value);
        } catch (AxisFault e) {
            handleException("failed.get.installed.feature.with.prop", e);
        }
        return featureInfos;
    }

    public ProvisioningActionResultInfo reviewUninstallFeaturesAction(FeatureInfo[] features) throws Exception {
        ProvisioningActionResultInfo provisioningActionResultInfo = null;
        try {
            ProvisioningActionInfo provisioningActionInfo = new ProvisioningActionInfo();
            provisioningActionInfo.setFeaturesToUninstall(features);
            provisioningActionInfo.setActionType(OperationFactory.UNINSTALL_ACTION);
            return provAdminStub.reviewProvisioningAction(provisioningActionInfo);
        } catch (AxisFault e) {
            handleException("failed.review.prov.action", e);
        }
        return provisioningActionResultInfo;
    }

    public ProfileHistory[] getProfileHistory() throws Exception {
        try {
            if (CarbonUtils.isRunningOnLocalTransportMode()) {
                profileHistories = provAdminStub.getProfileHistory();
            } else {
                ServiceClient client = provAdminStub._getServiceClient();
                client.engageModule("addressing"); // IMPORTANT
                Options options = client.getOptions();
                options.setUseSeparateListener(true);
                options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
                provAdminStub.startgetProfileHistory(callback);
                handleCallback();
            }
        } catch (AxisFault e) {
            handleException("failed.get.profile.history", e);
        }
        return profileHistories;
    }

    public ProvisioningActionResultInfo reviewRevertPlan(String timestampString) throws Exception {
        ProvisioningActionResultInfo provisioningActionResultInfo = null;
        try {
            ProvisioningActionInfo provisioningActionInfo = new ProvisioningActionInfo();
            provisioningActionInfo.setTimestamp(Long.parseLong(timestampString));
            provisioningActionInfo.setActionType(OperationFactory.REVERT_ACTION);
            provisioningActionResultInfo = provAdminStub.reviewProvisioningAction(provisioningActionInfo);
        } catch (AxisFault e) {
            handleException("failed.review.prov.action", e);
        }
        return provisioningActionResultInfo;
    }

    public void removeAllFeaturesWithProperty(String key, String value) throws Exception {
        try {
            if ("org.wso2.carbon.p2.category.type".equals(key) && "server".equalsIgnoreCase(value)) {
                provAdminStub.removeAllServerFeatures();
            } else if ("org.wso2.carbon.p2.category.type".equals(key) && "console".equalsIgnoreCase(value)) {
                provAdminStub.removeAllConsoleFeatures();
            }
        } catch (AxisFault e) {
            handleException("failed.remove.server.console.features", e);
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

    ProvisioningAdminServiceCallbackHandler callback = new ProvisioningAdminServiceCallbackHandler() {
        @Override
        public void receiveResultperformProvisioningAction(boolean result) {
            isComplete = true;
        }

        @Override
        public void receiveErrorperformProvisioningAction(Exception e) {
            isError = true;
            exception = e;
        }

        @Override
        public void receiveResultremoveAllConsoleFeatures(boolean result) {
            isComplete = true;
        }

        @Override
        public void receiveErrorremoveAllConsoleFeatures(Exception e) {
            isError = true;
            exception = e;
        }

        @Override
        public void receiveResultremoveAllServerFeatures(boolean result) {
            isComplete = true;
        }

        @Override
        public void receiveErrorremoveAllServerFeatures(Exception e) {
            isError = true;
            exception = e;
        }

        @Override
        public void receiveResultgetProfileHistory(ProfileHistory[] result) {
            profileHistories = Arrays.copyOf(result, result.length);
            isComplete = true;
        }

        @Override
        public void receiveErrorgetProfileHistory(Exception e) {
            isError = true;
            exception = e;
        }
    };

    private void handleException(String msg, Exception e) throws Exception {
        log.error(msg, e);
        throw new Exception(msg, e);
    }

}
