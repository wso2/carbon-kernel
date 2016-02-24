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
<%@page import="org.apache.commons.fileupload.disk.DiskFileItem" %>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@page import="org.apache.commons.fileupload.servlet.ServletRequestContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>
<%@page import="org.wso2.carbon.security.ui.jsp.SecurityUIUtil" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="java.text.MessageFormat" %>
<%@page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    String forwardTo = null;
    String keyStore = null;
    String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        if (ServletFileUpload.isMultipartContent(request)) {
            List items = SecurityUIUtil.parseRequest(new ServletRequestContext(request));
            byte[] content = null;
            String fileName = null;
            for (Object item : items) {
                DiskFileItem diskFileItem = (DiskFileItem) item;
                String name = diskFileItem.getFieldName();
                if (name.equals("certFile")) {
                    content = diskFileItem.get();
                    fileName = diskFileItem.getName();
                    int index = fileName.lastIndexOf("\\");
                    fileName = fileName.substring(index+1);
                } else if (name.equals("keyStore")) {
                    keyStore = new String(diskFileItem.get());
                }
            }

            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);
            client.importCertToStore(fileName, content, keyStore);
            String message = resourceBundle.getString("cert.import");
            forwardTo = "view-keystore.jsp?keyStore=" + Encode.forUriComponent(keyStore);
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        }
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("cert.cannot.import"),
                new Object[]{e.getMessage()});
        forwardTo = "import-cert.jsp?keyStore=" + Encode.forUriComponent(keyStore);
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
