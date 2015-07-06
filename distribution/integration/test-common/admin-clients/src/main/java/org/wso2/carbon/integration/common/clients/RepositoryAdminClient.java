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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.feature.mgt.stub.RepositoryAdminServiceStub;
import org.wso2.carbon.feature.mgt.stub.prov.data.Feature;
import org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo;
import org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo;
import org.wso2.carbon.integration.framework.utils.AuthenticateStubUtil;

import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

public class RepositoryAdminClient {

    private static final Log log = LogFactory.getLog(RepositoryAdminClient.class);

    private String serviceName = "RepositoryAdminService";
    private RepositoryAdminServiceStub repositoryAdminServiceStub;

    public RepositoryAdminClient(String backendURL, AutomationContext automationContext)
            throws AxisFault,
                   XPathExpressionException {
        String endPoint = backendURL + serviceName;
        repositoryAdminServiceStub = new RepositoryAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(automationContext.getContextTenant().getContextUser().getUserName(),
                                              automationContext.getContextTenant().getContextUser().getPassword(), repositoryAdminServiceStub);
    }

    public void addRepository(String repoURL, String nickName, boolean localRepo)
            throws RemoteException, URISyntaxException {

        repoURL = repoURL.trim();

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
            uri = new URI(repoURL);
            String scheme = uri.getScheme();

        }
        repositoryAdminServiceStub.addRepository(repoURL, nickName);

    }

    public RepositoryInfo[] getAllRepositories() throws Exception {
        return repositoryAdminServiceStub.getAllRepositories();
    }

    public RepositoryInfo[] getEnabledRepositories() throws Exception {
        return repositoryAdminServiceStub.getEnabledRepositories();
    }

    public void updateRepository(String prevLocation, String prevNickName, String updatedLocation,
                                 String updatedNickName)
            throws RemoteException {
        repositoryAdminServiceStub.updateRepository(prevLocation, prevNickName, updatedLocation, updatedNickName);
    }

    public void removeRepository(String location) throws RemoteException {
        repositoryAdminServiceStub.removeRepository(location);
    }

    public void enableRepository(String location, String enabled) throws RemoteException {
        boolean isEnabled = false;
        if (Boolean.parseBoolean(enabled)) {
            isEnabled = true;
        }
        repositoryAdminServiceStub.enableRepository(location, isEnabled);
    }

    public Feature[] getInstallableFeatures(String location, boolean groupByCategory, boolean hideInstalledFeatures,
                                            boolean showOnlyTheLatestFeatures) throws RemoteException {
        return repositoryAdminServiceStub.getInstallableFeatures(location, groupByCategory, hideInstalledFeatures, showOnlyTheLatestFeatures);
    }

    public FeatureInfo getInstallableFeatureInfo(String featureId, String featureVersion) throws RemoteException {
        return repositoryAdminServiceStub.getInstallableFeatureInfo(featureId, featureVersion);
    }
}
