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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.feature.mgt.stub.RepositoryAdminServiceStub;
import org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo;
import org.wso2.carbon.integration.framework.utils.AuthenticateStubUtil;

import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.net.URISyntaxException;

public class RepositoryAdminClient {

    private static final Log log = LogFactory.getLog(RepositoryAdminClient.class);

    private String serviceName = "RepositoryAdminService";
    private RepositoryAdminServiceStub repositoryAdminServiceStub;

    public RepositoryAdminClient(String backendURL, AutomationContext automationContext) throws AxisFault,
            XPathExpressionException {
        String endPoint = backendURL + serviceName;
        repositoryAdminServiceStub = new RepositoryAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword(), repositoryAdminServiceStub);
    }

    public void addRepository(String repoURL, String nickName, boolean localRepo) throws Exception {
        //validating inputs
        if (nickName == null || nickName.length() == 0) {


            throw new Exception("missing.repo.name");
        }

        if (repoURL == null || repoURL.length() == 0) {
            throw new Exception("missing.repo.location");
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
                    throw new Exception("invalid.url.prot" +
                            "ocol");
                }
            } catch (URISyntaxException e) {
                handleException("invalid.repo.location", e);
            }
        }

        try {
            repositoryAdminServiceStub.addRepository(repoURL, nickName);
        } catch (AxisFault e) {
            handleException("failed.add.repository", e);
        }
    }

    public RepositoryInfo[] getAllRepositories() throws Exception {
        RepositoryInfo[] repositoryInfo = null;
        try {
            repositoryInfo = repositoryAdminServiceStub.getAllRepositories();
        } catch (AxisFault e) {
            handleException("failed.get.repositories", e);
        }
        return repositoryInfo;
    }

    public RepositoryInfo[] getEnabledRepositories() throws Exception {
        RepositoryInfo[] repositoryInfo = null;
        try {
            return repositoryAdminServiceStub.getEnabledRepositories();
        } catch (AxisFault e) {
            handleException("failed.get.repositories", e);
        }
        return repositoryInfo;
    }

    public void updateRepository(String prevLocation, String prevNickName, String updatedLocation, String updatedNickName) throws Exception {
        try {
            repositoryAdminServiceStub.updateRepository(prevLocation, prevNickName, updatedLocation, updatedNickName);
        } catch (AxisFault e) {
            handleException("failed.update.repository", e);
        }
    }

    public void removeRepository(String location) throws Exception {
        try {
            repositoryAdminServiceStub.removeRepository(location);
        } catch (AxisFault e) {
            handleException("failed.remove.repository", e);
        }
    }

    public void enableRepository(String location, String enabled) throws Exception {
        try {
            boolean isEnabled = false;
            if (Boolean.parseBoolean(enabled)) {
                isEnabled = true;
            }
            repositoryAdminServiceStub.enableRepository(location, isEnabled);
        } catch (AxisFault e) {
            handleException("failed.enable.repository", e);
        }
    }

    private void handleException(String msg, Exception e) throws Exception {
        log.error(msg, e);
        throw new Exception(msg, e);
    }

}
