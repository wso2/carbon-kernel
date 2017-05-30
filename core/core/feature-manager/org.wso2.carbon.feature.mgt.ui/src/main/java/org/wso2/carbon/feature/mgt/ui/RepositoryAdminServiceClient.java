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
import org.wso2.carbon.feature.mgt.stub.RepositoryAdminServiceCallbackHandler;
import org.wso2.carbon.feature.mgt.stub.RepositoryAdminServiceStub;
import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo;
import org.wso2.carbon.feature.mgt.ui.util.Utils;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class RepositoryAdminServiceClient {
    private static final Log log = LogFactory.getLog(RepositoryAdminServiceClient.class);
    private static final String BUNDLE = "org.wso2.carbon.feature.mgt.ui.i18n.Resources";
    private ResourceBundle bundle;

    public static final String ENABLED = "Enabled";
    public static final String DISABLED = "Disabled";

    public static String AVAILABLE_FEATURES = "available.features";
    public static final String IS_DEFAULT_REPOSITORY_ADDED = "is.default.repositoy.added";
    public static final String DEFAULT_REPOSITORY_URL = "default.repository.url";

    private boolean isComplete = false;
    private boolean isError = false;
    private Exception exception;
    private String defaultRepositoryURL;
    public RepositoryAdminServiceStub repositoryAdminServiceStub;

    public RepositoryAdminServiceClient(String cookie,
                                        String backendServerURL,
                                        ConfigurationContext configContext,
                                        Locale locale) throws Exception {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE, locale);
            String serviceURL = backendServerURL + "RepositoryAdminService";
            repositoryAdminServiceStub = new RepositoryAdminServiceStub(configContext, serviceURL);
            ServiceClient client = repositoryAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void addRepository(String repoURL, String nickName, boolean localRepo) throws Exception {
        //validating inputs
        if (nickName == null || nickName.length() == 0) {


            throw new Exception(bundle.getString("missing.repo.name"));
        }

        if (repoURL == null || repoURL.length() == 0) {
            throw new Exception(bundle.getString("missing.repo.location"));
        } else {
        	repoURL = repoURL.trim();
        }

        URI uri = null;
        if (localRepo) {
            //Removing all whitespaces
            repoURL = repoURL.replaceAll("\\b\\s+\\b", "%20");

            //Replacing all "\" with "/"
            repoURL = repoURL.replace('\\', '/');

            if (!repoURL.startsWith("file:") && repoURL.startsWith("/")) {
                repoURL = "file://" + repoURL;
            } else if (!repoURL.startsWith("file:")) {
                repoURL = "file:///" + repoURL;
            }
        } else {
            try {
                uri = new URI(repoURL);
                String scheme = uri.getScheme();
                if (!scheme.equals("http") && !scheme.equals("https") && !scheme.equals("file")) {
                    throw new Exception(MessageFormat.format(bundle.getString("invalid.url.protocol"), scheme));
                }
            } catch (URISyntaxException e) {
                throw new Exception(MessageFormat.format(bundle.getString("invalid.repo.location"), ""));
            }
        }

        try {
        	if (CarbonUtils.isRunningOnLocalTransportMode()) {
        		repositoryAdminServiceStub.addRepository(repoURL, nickName);	
        	} else {
        		ServiceClient client = repositoryAdminServiceStub._getServiceClient();
                client.engageModule("addressing"); // IMPORTANT
                Options options = client.getOptions();
                options.setUseSeparateListener(true);
                options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
                repositoryAdminServiceStub.startaddRepository(repoURL, nickName, callback);
                handleCallback();
        	}
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(MessageFormat.format(bundle.getString(e.getFaultCode().getLocalPart()),
                        repoURL), e);
            } else {
                handleException(MessageFormat.format(bundle.getString("failed.add.repository"),
                        repoURL), e);
            }
        }
    }

    public RepositoryInfo[] getAllRepositories() throws Exception {
        RepositoryInfo[] repositoryInfo = null;
        try {
            repositoryInfo = repositoryAdminServiceStub.getAllRepositories();
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.get.repositories"), e);
            }
        }
        return repositoryInfo;
    }

    public RepositoryInfo[] getEnabledRepositories() throws Exception {
        RepositoryInfo[] repositoryInfo = null;
        try {
            return repositoryAdminServiceStub.getEnabledRepositories();
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.get.repositories"), e);
            }
        }
        return repositoryInfo;
    }

    public void updateRepository(String prevLocation, String prevNickName, String updatedLocation, String updatedNickName) throws Exception {
        try {
            repositoryAdminServiceStub.updateRepository(prevLocation, prevNickName, updatedLocation, updatedNickName);
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(MessageFormat.format(bundle.getString(e.getFaultCode().getLocalPart()),
                        updatedLocation), e);
            } else {
                handleException(MessageFormat.format(bundle.getString("failed.update.repository"),
                        updatedLocation), e);
            }
        }
    }

    public void removeRepository(String location) throws Exception {
        try {
            repositoryAdminServiceStub.removeRepository(location);
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(MessageFormat.format(bundle.getString(e.getFaultCode().getLocalPart()),
                        location), e);
            } else {
                handleException(MessageFormat.format(bundle.getString("failed.remove.repository"),
                        location), e);
            }
        }
    }

    public void enableRepository(String location, String enabled) throws Exception {
        try {
            boolean isEnabled = false;
            if (ENABLED.equals(enabled)) {
                isEnabled = true;
            }
            repositoryAdminServiceStub.enableRepository(location, isEnabled);
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(MessageFormat.format(bundle.getString(e.getFaultCode().getLocalPart()),
                        location), e);
            } else {
                handleException(MessageFormat.format(bundle.getString("failed.enable.repository"),
                        location), e);
            }
        }
    }

    public FeatureWrapper[] getInstallableFeatures(String repoLocation, boolean groupByCategory, boolean hideInstalledFeatures,
                                                   boolean showOnlyTheLatestFeatures) throws Exception {
        FeatureWrapper[] featureWrappers = null;
        try {
            if ("ALL_REPOS".equals(repoLocation)) {
                repoLocation = null;
            }
            Feature[] features = repositoryAdminServiceStub.getInstallableFeatures(repoLocation, groupByCategory, hideInstalledFeatures, showOnlyTheLatestFeatures);

            if (features == null || features.length == 0) {
                featureWrappers = new FeatureWrapper[0];
            } else {
                featureWrappers = Utils.processFeatureTree(features, groupByCategory, 0);
            }
        } catch (AxisFault e) {
            if (e.getFaultCode() != null) {
                handleException(bundle.getString(e.getFaultCode().getLocalPart()), e);
            } else {
                handleException(bundle.getString("failed.get.installable.features"), e);
            }
        }
        return featureWrappers;
    }

    public FeatureInfo getInstallableFeatureDetails(String featureID, String featureVersion) throws Exception {
        FeatureInfo featureInfo = null;
        try {
            return repositoryAdminServiceStub.getInstallableFeatureInfo(featureID, featureVersion);
        } catch (AxisFault e) {
            handleException(MessageFormat.format(bundle.getString(e.getMessage()),
                    featureID, featureVersion), e);
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

    RepositoryAdminServiceCallbackHandler callback = new RepositoryAdminServiceCallbackHandler() {
        @Override
        public void receiveResultaddRepository(boolean result) {
            isComplete = true;
        }

        @Override
        public void receiveErroraddRepository(Exception e) {
            isError = true;
            exception = e;
        }
        
        @Override
        public void receiveResultaddDefaultRepository(String result) {
        	defaultRepositoryURL = result;
        	isComplete = true;
        }
        
        @Override
        public void receiveErroraddDefaultRepository(Exception e) {
        	isError = true;
        	exception = e;
        }
    };

    private void handleException(String msg, Exception e) throws Exception {
        log.error(msg, e);
        throw new Exception(msg, e);
    }
    
}
