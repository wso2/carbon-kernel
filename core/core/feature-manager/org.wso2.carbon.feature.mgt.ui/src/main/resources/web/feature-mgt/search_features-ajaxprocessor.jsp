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
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../admin/js/main.js"></script>
<%
    String httpMethod = request.getMethod().toLowerCase();

    if (!"post".equals(httpMethod)) {
        response.sendError(405);
        return;
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RepositoryAdminServiceClient repositoryAdminClient;
    RepositoryInfo[] repositories;
    boolean disableNext = true;
    try {
        repositoryAdminClient = new RepositoryAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());
        repositories = repositoryAdminClient.getEnabledRepositories();
     } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
       location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">
                    <H4><strong><fmt:message key="available.features"/></strong></H4>
                    <p><font color="#707277"><i><fmt:message key="available.features.description"/></i></font></p>
                    <br/>
                    <div id="_div_filter_repositories">
                        <table class="styledLeft" width="100%">
                            <thead>
                                 <tr>
                                     <th/>
                                 </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td class="formRaw">
                                    <table class="normal">
                                        <tr>
                                            <td>
                                                <fmt:message key="repository"/>:
                                                </td>
                                            <td>
                                                    <select id="_select_repositoryCombo"
                                                            tabindex="5" style="width: 500px">
                                                        <% if (repositories == null) {
                                                        %>
                                                        <option value="NO_REPO" selected="true">
                                                            <fmt:message
                                                                    key="no.repositories.found"/>
                                                        </option>
                                                        <%
                                                        } else if (repositories.length == 1) { // if there is only one repository we show it
                                                            disableNext = false;
                                                            String nickName = repositories[0].getNickName();
                                                            String location = repositories[0].getLocation();
                                                        %>
                                                        <option value="<%=location%>"
                                                                selected="true">
                                                            <%=nickName%> - <%=location%>
                                                        </option>

                                                        <%
                                                        } else {
                                                            disableNext = false;
                                                        %>
                                                        <option value="ALL_REPOS" selected="true">
                                                            All Available
                                                            Repositories
                                                        </option>
                                                        <%
                                                            for (RepositoryInfo repository : repositories) {
                                                                String nickName = repository.getNickName();
                                                                String location = repository.getLocation();
                                                        %>
                                                        <option value="<%=location%>"><%=nickName%>
                                                            - <%=location%>
                                                        </option>
                                                        <%
                                                                }
                                                            }
                                                        %>
                                                    </select>&nbsp;&nbsp;<a href="#"
                                                                            class="icon-link"
                                                                            style="background-image: url(images/add.gif);float:none;"
                                                                            onclick="$myTabs.tabs('select', 3);swapVisiblility('_div_tabs04-MR', '_div_tabs04-AR', 4); addRepositoryOnCompleteReturnTabID='AF';"><fmt:message
                                                            key="add.new.repository"/></a></nobr>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <fmt:message key="filter.by.feature.name"/>
                                            </td>
                                            <td>
                                                <input id="_txt_AF_filterString"
                                                       class="log-select"
                                                       type="text"
                                                       size="40"
                                                       onkeypress="submitenter(event)"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <input id="_chk_show_latest" class="toolsClass" type="checkbox"/>&nbsp;&nbsp;&nbsp;<fmt:message key="latest.version"/>
                                            </td>
					    <td>
                                                <input id="_chk_groupBy_category" name="categoryBox" class="toolsClass" type="checkbox" checked="yes"/>&nbsp;&nbsp;&nbsp;<fmt:message key="groupBy.category"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td class="buttonRow">
                                    <input value="<fmt:message key="find.features"/>"
                                           tabindex="11"
                                           type="button"
                                           class="button" <%=(disableNext)?"disabled='true'":""%>
                                           onclick="CARBON.showInfoDialog('<fmt:message key="loading.features"/>....');doNext('FR-AF');"
                                           id="_btn_next_filter_repositories">
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        </div>

                    <div id="_div_tabs01-step-01-AF" style="display:none">
                    </div>

</fmt:bundle>
