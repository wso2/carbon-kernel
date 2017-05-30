<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.roles.mgt.ui.ServerRoleManagerClient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>

<%
    String httpMethod = request.getMethod().toLowerCase();

    if (!"post".equals(httpMethod)) {
        response.sendError(405);
        return;
    }

    String forwardTo;
    String BUNDLE = "org.wso2.carbon.roles.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    ServerRoleManagerClient client;
    String serverRoleName = CharacterEncoder.getSafeText(request.getParameter("serverRoleName"));
    String serverRoleType = CharacterEncoder.getSafeText(request.getParameter("serverRoleType"));

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);

        client = new ServerRoleManagerClient(configContext, backendServerURL, cookie);
        client.deleteServerRoles(new String[]{serverRoleName}, serverRoleType);

        forwardTo = "server-roles-mgt.jsp";
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("server-role.cannot.delete"),
                serverRoleName, e.getMessage());
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "server-roles-mgt.jsp";
    }
%>

<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>

