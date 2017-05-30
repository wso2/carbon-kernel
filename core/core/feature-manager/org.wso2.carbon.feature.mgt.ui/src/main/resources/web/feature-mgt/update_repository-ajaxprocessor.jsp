<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.RepositoryAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    String httpMethod = request.getMethod().toLowerCase();

    if (!"post".equals(httpMethod)) {
        response.sendError(405);
        return;
    }

    String repoUpdateAction = request.getParameter("action");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RepositoryAdminServiceClient repositoryAdminClient;

    if(repoUpdateAction == null){

%>
Repository Update Action cannot be null
<%
        return;
    }

    String repoURL = CharacterEncoder.getSafeText(request.getParameter("repoURL"));
    String nickName = CharacterEncoder.getSafeText(request.getParameter("nickName"));
    boolean local = Boolean.parseBoolean(CharacterEncoder.getSafeText(request.getParameter("local")));
    String enabled = CharacterEncoder.getSafeText(request.getParameter("enabled"));
    String updatedRepoURL = CharacterEncoder.getSafeText(request.getParameter("updatedRepoURL"));
    String updatedNickName = CharacterEncoder.getSafeText(request.getParameter("updatedNickName"));

    try {
        repositoryAdminClient = new RepositoryAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());

        if(repoUpdateAction.equals("editRepo")){
            repositoryAdminClient.updateRepository(repoURL, nickName, updatedRepoURL, updatedNickName);

%>
Repository is Updated Successfully
<%
        } else if(repoUpdateAction.equals("removeRepo")){
            repositoryAdminClient.removeRepository(repoURL);

%>
Repository is Removed Successfully
<%
        } else if(repoUpdateAction.equals("addRepo")){
            repositoryAdminClient.addRepository(repoURL, nickName, local);

%>
Repository is Added Successfully
<%
        } else if(repoUpdateAction.equals("enableRepo")){
            repositoryAdminClient.enableRepository(repoURL, enabled);

%>
Repository is <%=enabled%> Successfully
<%
        }
        return;
    } catch (Exception e){
%>
<p id="compMgtErrorMsg"><%=e.getMessage()%></p>
<%
        return;
    }
%>