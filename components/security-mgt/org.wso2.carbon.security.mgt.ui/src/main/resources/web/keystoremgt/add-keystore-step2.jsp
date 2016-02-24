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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page
        import="org.apache.commons.fileupload.disk.DiskFileItem" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletRequestContext" %>
<%@ page
        import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>

<%@page import="org.wso2.carbon.security.ui.jsp.SecurityUIUtil" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<script type="text/javascript" src="../securityconfig/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:useBean id="ksBean" type="org.wso2.carbon.security.ui.client.KeyStoreBean" class="org.wso2.carbon.security.ui.client.KeyStoreBean" scope="session"/>
<jsp:setProperty name="ksBean" property="*" />

 <%
        String forwardTo = null;
        boolean isGetPrivateKey = false;
        String fileName = null;
        String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        try {

            if (ServletFileUpload.isMultipartContent(request)) {
            	ServletRequestContext servletContext = new ServletRequestContext(request);
                List items = SecurityUIUtil.parseRequest(servletContext);
                String ksPassword = null;
                String provider = null;
                String keystoreType = null;
                byte[] content = null;

                for (Object item : items) {
                    DiskFileItem diskFileItem = (DiskFileItem) item;
                    String name = diskFileItem.getFieldName();
                    if (name.equals("keystoreFile")) {
                        FileItem fileItem = (FileItem) diskFileItem;
                        fileName = fileItem.getName();
                        int index = fileName.lastIndexOf("\\");
                        fileName = fileName.substring(index+1);
                        content = fileItem.get();
                    } else if (name.equals("ksPassword")) {
                        ksPassword = new String(diskFileItem.get());
                    } else if (name.equals("provider")) {
                        provider = new String(diskFileItem.get());
                    } else if (name.equals("keystoreType")) {
                        keystoreType = new String(diskFileItem.get());
                    }
                }

                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

                KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);
                //client.addKeyStore(content, fileName, ksPassword, provider, keystoreType);
                if (client.isPrivateKeyStore(content, ksPassword, keystoreType)) {
                    isGetPrivateKey = true;
                }

                session.setAttribute("org.wso2.carbon.security.content", content);
                session.setAttribute("org.wso2.carbon.security.fileName", fileName);
                session.setAttribute("org.wso2.carbon.security.ksPassword", ksPassword);
                session.setAttribute("org.wso2.carbon.security.provider", provider);
                session.setAttribute("org.wso2.carbon.security.keystoreType", keystoreType);

                if (!isGetPrivateKey) {
                    String message = resourceBundle.getString("keystore.doesnt.contain.private.key");
                    // We are not gonna allow users to
                    // upload keystores that do not contain private key
                    forwardTo = "add-keystore-step1.jsp?ordinal=1";
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.WARNING, request);
                    session.setAttribute("add-keystore-error", "true");
                }

            }
        } catch (Exception e) {
            forwardTo = "add-keystore-step1.jsp?ordinal=1";
            CarbonUIMessage.sendCarbonUIMessage("Error when uploading the KeyStore : " + e.getMessage(),
                    CarbonUIMessage.ERROR, request);
            session.setAttribute("add-keystore-error", "true");
        }
    %>



<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="private.key.password"
		resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
    <script type="text/javascript">

        function doValidation() {

            reason = validateEmpty("keyPass");
            if (reason != "") {
                CARBON.showWarningDialog("<fmt:message key="enter.private.key.password"/>");
                return false;
            }
            else {
                document.forms.keystoreFinish.submit();
            }

        }

        function doCancel() {
            location.href = 'keystore-mgt.jsp';
        }
    </script>
   
    <%
        if (isGetPrivateKey) {
    %>
    <jsp:include page="../dialog/display_messages.jsp"/>
    <div id="middle">
        <h2><fmt:message key="add.new.keystore"/></h2>
        <div id="workArea">
            <form method="post" action="add-keystore-finish.jsp" name="keystoreFinish">
                <h3><fmt:message key="step.2.specify.private.key.password"/></h3>
                <input type="hidden" name="keyStoreName" value="<%=Encode.forHtmlAttribute(fileName)%>"/>
                <input type="hidden" name="addKeystore" value="true"/>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="enter.private.key.password1"/></th>
                    </tr>
                    </thead>
<tr>            
<td class="formRow">
<table class="normal">                                        
                    <tr>
                        <td><fmt:message key="private.key.password"/><font color="red">*</font></td>
                        <td>
                            <input type="password" name="keyPass" value=""/>
                        </td>
                    </tr>
</table>
</td>
</tr>
                    <tr>
                        <td class="buttonRow">
                            <input class="button" type="button" value="&lt; <fmt:message key="back"/>" onclick="history.back()"/>
                            <input class="button" type="button" value="<fmt:message key="finish"/>" onclick="doValidation();"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>" onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
          </div>
     </div>
    <%
        }else{
    %>
                <script type="text/javascript">
                    function forward() {
                        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
                    }
                </script>

                <script type="text/javascript">
                    forward();
                </script>

    <%
        }
    %>
</fmt:bundle>