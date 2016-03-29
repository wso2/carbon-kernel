<!--
~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.security.ui.client.SecurityAdminClient"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="java.text.MessageFormat"%>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%
    String forwardTo = null;
    String serviceName = (String) session.getAttribute("serviceName");
    String specificPath = (String) session.getAttribute("returToPath");
    if (specificPath==null) {
    	specificPath = (String) session.getAttribute("returnToPath");
    }
    String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        SecurityAdminClient client = new SecurityAdminClient(cookie, backendServerURL, configContext);

        String scenarioId = request.getParameter("scenarioId");

        String securityCategory = request.getParameter("org.wso2.security.category");
        if (securityCategory != null && securityCategory.equals("kerberos")) {
            String servicePrincipalName = request.getParameter("org.wso2.kerberos.service.principal.name");
            String servicePrincipalPassword = request.getParameter("org.wso2.kerberos.service.principal.password");

            client.applyKerberosSecurity(serviceName, scenarioId, servicePrincipalName,
                    servicePrincipalPassword);

        } else {

            String policyPath = request.getParameter("policyPath");
            ArrayList<String> userGroupsList = new ArrayList<String>();

            Map<String, Boolean> checkBoxMap = (Map<String, Boolean>) session.getAttribute("checkedRolesMap");

            for (Map.Entry<String, Boolean> entry : checkBoxMap.entrySet()) {
                if (entry.getValue().equals(Boolean.TRUE)) {
                    userGroupsList.add(entry.getKey());
                }
            }
            String[] userGroups = new String[userGroupsList.size()];
            userGroups = userGroupsList.toArray(userGroups);
            String privateStore = request.getParameter("privateStore");
            String[] trustedStores = request.getParameterValues("trustStore");
            client.applySecurity(serviceName, scenarioId, policyPath, trustedStores, privateStore, userGroups);

        }

        String message = resourceBundle.getString("security.add");
        forwardTo = "../service-mgt/service_info.jsp?serviceName=" + serviceName;

        if (specificPath!=null && specificPath.trim().length()>0){
        	forwardTo = specificPath +"?serviceName=" + Encode.forUriComponent(serviceName);
        	session.removeAttribute("returToPath");
        }

        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
	    String message = MessageFormat.format(resourceBundle.getString("security.cannot.add"),
                new Object[]{e.getMessage()});
        forwardTo = "index.jsp?ordinal=2&serviceName=" + Encode.forUriComponent(serviceName);
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }

%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
