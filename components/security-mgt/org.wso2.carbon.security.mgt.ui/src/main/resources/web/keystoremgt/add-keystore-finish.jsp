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
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.security.ui.SecurityUIConstants" %>
<%@page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    if (request.getParameter("addKeystore") != null) {
        String keyStoreName = request.getParameter("keyStoreName");
        try {
            String password = request.getParameter("keyPass");
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);

            byte[] content = (byte[]) session.getAttribute("org.wso2.carbon.security.content");
            String fileName = (String) session.getAttribute("org.wso2.carbon.security.fileName");
            String ksPassword = (String) session.getAttribute("org.wso2.carbon.security.ksPassword");
            String provider = (String) session.getAttribute("org.wso2.carbon.security.provider");
            String keystoreType = (String) session.getAttribute("org.wso2.carbon.security.keystoreType");

            client.addKeyStore(content, fileName, ksPassword, provider, keystoreType, password);

            String message = resourceBundle.getString("keystore.add");
            forwardTo = "keystore-mgt.jsp?region=region1&item=keystores_menu";
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
            
            session.setAttribute(SecurityUIConstants.RE_FETCH_KEYSTORES, Boolean.TRUE);

        } catch (Exception e) {
            String message = MessageFormat.format(resourceBundle.getString("keystore.cannot.add"),
                    new Object[]{e.getMessage()});
            forwardTo = "add-keystore-step1.jsp?ordinal=1";
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        } finally {

            session.removeAttribute("org.wso2.carbon.security.content");
            session.removeAttribute("org.wso2.carbon.security.fileName");
            session.removeAttribute("org.wso2.carbon.security.ksPassword");
            session.removeAttribute("org.wso2.carbon.security.provider");
            session.removeAttribute("org.wso2.carbon.security.keystoreType");
        }
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
