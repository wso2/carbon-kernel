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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData" %>
<%@page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.KeyStoreData" %>
<%@page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.owasp.encoder.Encode" %>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="import.certificates.to"
		resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <%
        String[] aliasSet = null;
        String keyStore = request.getParameter("keyStore");
		 KeyStoreData keyStoreData = null; 
		 CertData[] certData = new CertData[0];
        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            ServletContext servletContext = session.getServletContext();
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);
            keyStoreData = client.getKeystoreInfo(keyStore);
            certData = keyStoreData.getCerts();
        } catch (Exception e) {
            String message = "Problem while retrieving key store entries";
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    %>
    <jsp:forward page="../admin/error.jsp"/>

    <%
            return;
        }
    %>

    <script type="text/javascript">

        function verify() {
            var txtField = document.getElementById("browseField");
            if (txtField.value == "") {
                CARBON.showWarningDialog('Please specify a path before importing certificate');
            } else {
                document.certForm.submit();
            }
        }
    </script>


    <div id="middle">
        <h2><fmt:message key="import.certificates.to"/><%= " " + Encode.forHtml(keyStore) %></h2>
        <div id="workArea">
            <form method="post" name="certForm" enctype="multipart/form-data"
                  action="import-cert-finish.jsp">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="import.certificate"/></th>
                    </tr>
                    </thead>
                    <tbody>
<tr>            
<td class="formRow">
<table class="normal">                    
                    <tr>
                        <td><fmt:message key="certificate"/> <font color="red">*</font>
                        <input id="browseField" type="file" name="certFile" size="50"/></td>
                    </tr>
</table>
</td>
</tr>
                    <tr>
                        <td class="buttonRow"> 
                            <input type="hidden" name="keyStore" id="keyStore" value="<%=Encode.forHtmlAttribute(keyStore)%>"/>
                            <input class="button" type="button" value="<fmt:message key="import"/>" onclick="verify();"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>" onclick="location.href ='keystore-mgt.jsp?region=region1&item=keystores_menu'"/>
                            <input class="button" type="button" value="<fmt:message key="view.keystore"/>" onclick="location.href='view-keystore.jsp?keyStore=<%=Encode.forUriComponent(keyStore) %>'"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>

            
        </div>
    </div>

</fmt:bundle>