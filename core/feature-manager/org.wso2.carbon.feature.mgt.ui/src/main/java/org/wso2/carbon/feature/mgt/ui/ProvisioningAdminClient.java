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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.feature.mgt.core.operations.OperationFactory;
import org.wso2.carbon.feature.mgt.stub.ProvisioningAdminServiceCallbackHandler;
import org.wso2.carbon.feature.mgt.stub.ProvisioningAdminServiceStub;
import org.wso2.carbon.feature.mgt.stub.prov.data.*;
import org.wso2.carbon.feature.mgt.ui.util.Utils;
import org.wso2.carbon.utils.CarbonUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * WS client which consumes the ComponentManager admin service
 */
public class ProvisioningAdminClient {

    private static final Log log = LogFactory.getLog(ProvisioningAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.feature.mgt.ui.i18n.Resources";
    private ResourceBundle bundle;

    public static String INSTALLED_FEATURES = "installed.features";

    public static String ENABLED = "Enabled";
    public static String DISABLED = "Disabled";

    private boolean isComplete = false;
    private boolean isError = false;
    private Exception exception;

    private ProfileHistory[] profileHistories = new ProfileHistory[]{};

    public ProvisioningAdminServiceStub provAdminStub;

    public ProvisioningAdminClient(String cookie, String backendServerURL,
                                   ConfigurationContext configContext,
                                   Locale locale) throws Exception {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE, locale);
            String serviceURL = backendServerURL + "ProvisioningAdminService";
            provAdminStub = new ProvisioningAdminServiceStub(configContext, serviceURL);
            ServiceClient client = provAdminStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public ProvisioningActionResultInfo reviewInstallFeaturesAction(FeatureInfo[] features) throws Exception {
        ProvisioningActionResultInfo provisioningActionResultInfo = null;
        try {
            ProvisioningActionInfo provisioningActionInfo = new ProvisioningActionInfo();
            provisioningActionInfo.setFeaturesToInstall(features);
            provisioningActionInfo.setActionType(OperationFactory.INSTALL_ACTION);
            provisioningActionResultInfo = provAdminStub.reviewProvisioningAction(provisioningActionInfo);
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.review.prov.action"), e);
            }
        }
        return provisioningActionResultInfo;
    }

    public FeatureInfo getInstalledFeatureDetails(String featureID, String featureVersion) throws Exception {
        FeatureInfo featureInfo = null;
        try {
            featureInfo = provAdminStub.getInstalledFeatureInfo(featureID, featureVersion);
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(MessageFormat.format(bundle.getString(e.getFaultCode().getLocalPart()),
                        featureID, featureVersion), e);
            } else {
                handleException(MessageFormat.format(bundle.getString("failed.get.feature.information"),
                        featureID, featureVersion), e);
            }
        }
        return featureInfo;
    }

    public LicenseFeatureHolder[] getLicensingInformation() throws Exception {
        LicenseFeatureHolder[] licenseFeatureHolders = null;
        try {
	        licenseFeatureHolders = provAdminStub.getFeatureLicenseInfo();
	        Arrays.sort(licenseFeatureHolders, new Comparator<LicenseFeatureHolder>() {
		        @Override
		        public int compare(LicenseFeatureHolder o1, LicenseFeatureHolder o2) {
			        if (o1.getLicenseInfo() == null && o2.getLicenseInfo() == null) {
				        return 0;
			        } else if (o2.getLicenseInfo() == null) {
				        return 1;
			        } else {
				        return -1;
			        }
		        }
	        });
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.get.license.info"), e);
            }
        }
        return licenseFeatureHolders;
    }

    public void performInstallation(String actionType) throws Exception {
        try {
        	if (CarbonUtils.isRunningOnLocalTransportMode()) {
        		provAdminStub.performProvisioningAction(actionType);
        	} else {
        		ServiceClient client = provAdminStub._getServiceClient();
                client.engageModule("addressing"); // IMPORTANT
                Options options = client.getOptions();
                options.setUseSeparateListener(true);
                options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
                provAdminStub.startperformProvisioningAction(actionType, callback);
                handleCallback();
        	}        
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.perform.prov.action"), e);
            }
        }
    }

    public FeatureWrapper[] getInstalledFeatures() throws Exception {
        FeatureWrapper[] featureWrappers = null;
        try {
            Feature[] features = provAdminStub.getAllInstalledFeatures();
            featureWrappers = Utils.processFeatureTree(features, false, 0);
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.get.installed.features"), e);
            }
        }
        return featureWrappers;
    }

    public FeatureInfo[] getInstalledFeaturesWithProperty(String key, String value) throws Exception {
        FeatureInfo[] featureInfos = null;
        try {
            featureInfos = provAdminStub.getInstalledFeaturesWithProperty(key, value);
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(MessageFormat.format(bundle.getString(e.getFaultCode().getLocalPart()),
                        key, value), e);
            } else {
                handleException(MessageFormat.format(bundle.getString("failed.get.installed.feature.with.prop"),
                        key, value), e);
            }
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
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.review.prov.action"), e);
            }
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
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.get.profile.history"), e);
            }
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
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.review.prov.action"), e);
            }
        }
        return provisioningActionResultInfo;
    }

    public void removeAllFeaturesWithProperty(String key, String value) throws Exception {
        try {
        	if (CarbonUtils.isRunningOnLocalTransportMode()) {
        		if ("org.wso2.carbon.p2.category.type".equals(key) && "server".equalsIgnoreCase(value)) {
                    provAdminStub.removeAllServerFeatures();
                } else if ("org.wso2.carbon.p2.category.type".equals(key) && "console".equalsIgnoreCase(value)) {
                    provAdminStub.removeAllConsoleFeatures();
                }
        	} else {
        		 ServiceClient client = provAdminStub._getServiceClient();
                 client.engageModule("addressing"); // IMPORTANT
                 Options options = client.getOptions();
                 options.setUseSeparateListener(true);
                 options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

                 if ("org.wso2.carbon.p2.category.type".equals(key) && "server".equalsIgnoreCase(value)) {
                     provAdminStub.startremoveAllServerFeatures(callback);
                     handleCallback();

                 } else if ("org.wso2.carbon.p2.category.type".equals(key) && "console".equalsIgnoreCase(value)) {
                     provAdminStub.startremoveAllConsoleFeatures(callback);
                     handleCallback();
                 }
        	}
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.remove.server.console.features"), e);
            }
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
