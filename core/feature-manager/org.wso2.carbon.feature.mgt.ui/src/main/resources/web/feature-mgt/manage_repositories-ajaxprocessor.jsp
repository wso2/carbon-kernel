<%--
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
 --%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.RepositoryAdminServiceClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo" %>
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
    
    String repositoryURL = CharacterEncoder.getSafeText(request.getParameter("repoURL"));
    String nickName = CharacterEncoder.getSafeText(request.getParameter("nickName"));

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RepositoryAdminServiceClient repositoryAdminClient;
    RepositoryInfo[] repositories;
    
    try {
        repositoryAdminClient = new RepositoryAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());
        repositories = repositoryAdminClient.getAllRepositories();        
     } catch (Exception e){
%>
<p id="compMgtErrorMsg"><%=e.getMessage()%></p>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">
<table class="styledLeft" cellspacing="1" width="100%" id="_table_repository_list"
                                       style="margin-left: 0px;">
    <thead>
    <tr>
        <th><fmt:message key="name"/></th>
        <th><fmt:message key="location"/></th>
        <th><fmt:message key="enabled"/></th>
        <th colspan="4"><fmt:message key="actions"/></th>
    </tr>
    </thead>
    <tbody>

    <%
        if(repositories == null || repositories.length == 0){
    %>
    <tr>
        <td colspan="0"><fmt:message key="no.available.repositories"/>.</td>
    </tr>
    <%
        } else {
            for(RepositoryInfo repository:repositories){
                String location = repository.getLocation();
                String repoNickName = repository.getNickName();
                boolean enabled = repository.getEnabled();
                String status;
                if(enabled){
                    status =  RepositoryAdminServiceClient.ENABLED;
                } else {
                    status = RepositoryAdminServiceClient.DISABLED;
                }

                String oppositeStatus = (RepositoryAdminServiceClient.ENABLED.equals(
                        status))?RepositoryAdminServiceClient.DISABLED:RepositoryAdminServiceClient.ENABLED;

                String statusText = oppositeStatus.substring(0, oppositeStatus.length() -1);

    %>
    <tr>
        <td><%=repoNickName%></td>
        <td><%=location%></td>
        <td><%=status%></td>
        <td>
            <a class="icon-link" style="background-image: url(../admin/images/edit.gif);"
               href="#" onclick="setSelectedRepoProperties('<%=location%>', '<%=repoNickName%>', null);
               swapVisiblility('_div_tabs04-MR', '_div_tabs04-ER', 4);fillEditRepoTextBoxes();return false;"><fmt:message key="edit.button"/></a>
        </td>
        <td>
            <a class="icon-link" style="background-image: url(../admin/images/delete.gif);"
               href="#" onclick="setSelectedRepoProperties('<%=location%>', null, null);
               showConfirmationDialogBox('<fmt:message key="confirm.removing.repo"/>',
               removeRepository);return false;"><fmt:message key="remove"/></a>
        </td>
        <td>
            <a class="icon-link"
               <%=statusText.equals("Enable")?"style='background-image: url(images/activate.gif)';":"style='background-image: url(images/deactivate.gif)';"%>
               href="#" onclick="setSelectedRepoProperties('<%=location%>', null, '<%=oppositeStatus%>');
               showConfirmationDialogBox('<fmt:message key="confirm.enabling.repo.part1"/> '+ '<%=statusText.toLowerCase()%>' +' <fmt:message key="confirm.enabling.repo.part2"/>',
               enableRepository);return false;">
                <%
                    if(statusText.equals("Enable")){
                %>
                    <fmt:message key="enable"/>
                <%
                    } else {
                 %>
                    <fmt:message key="disable"/>
                <%
                    }
                %>
               </a>
        </td>
    </tr>
    <%
            }
        }
    %>
    </tbody>
    </table>
</fmt:bundle>
<script type="text/javascript">
    alternateTableRows('_table_repository_list', 'tableEvenRow', 'tableOddRow');
</script>
