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
<%@ page import="org.wso2.carbon.feature.mgt.ui.ProvisioningAdminClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.RepositoryAdminServiceClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.CopyrightInfo" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.LicenseInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%

    String httpMethod = request.getMethod().toLowerCase();

    if (!"post".equals(httpMethod)) {
        response.sendError(405);
        return;
    }

    String featureID = CharacterEncoder.getSafeText(request.getParameter("featureID"));
    String featureVersion = CharacterEncoder.getSafeText(request.getParameter("featureVersion"));
    String divIDToShow = CharacterEncoder.getSafeText(request.getParameter("divIDToShow"));
    String divIDToHide = CharacterEncoder.getSafeText(request.getParameter("divIDToHide"));
    String tabNumber = CharacterEncoder.getSafeText(request.getParameter("tabNumber"));
    boolean isInstalledFeature = Boolean.parseBoolean(CharacterEncoder.getSafeText(request.getParameter("isInstalledFeature")));

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    FeatureInfo featureInfo;
    LicenseInfo licenseInfo;
    CopyrightInfo copyrightInfo;

    try {
        if(isInstalledFeature){
            ProvisioningAdminClient provAdminClient = new ProvisioningAdminClient(cookie, backendServerURL, configContext, request.getLocale());
            featureInfo = provAdminClient.getInstalledFeatureDetails(featureID, featureVersion);
        } else {
            RepositoryAdminServiceClient repositoryAdminServiceClient = new RepositoryAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());
            featureInfo = repositoryAdminServiceClient.getInstallableFeatureDetails(featureID, featureVersion);    
        }

        licenseInfo = featureInfo.getLicenseInfo();
        copyrightInfo = featureInfo.getCopyrightInfo();
     } catch (Exception e) {
%>
<p id="compMgtErrorMsg"><%=e.getMessage()%></p>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">
<H4><strong><fmt:message key="feature.information"/></strong></H4>
<%--<p><font color="#707277"><i>Following licenses must be reviewed and accepted before installing features.</i></font></p>--%>
<br/>
<div id="_div_feature_details">
    <table class="styledLeft" cellspacing="1" width="100%" id="_table_feature_details"
           style="margin-left: 0px;">
        <tbody>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="name"/></td>
                        <td><%=featureInfo.getFeatureName()%></td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="identifier"/></td>
                        <td><%=featureInfo.getFeatureID()%></td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="version1"/></td>
                        <td><%=featureInfo.getFeatureVersion()%></td>
                    </tr>
                    <tr class="tableOddRow">
                    	<td><fmt:message key="provider"/></td>
                    	<td><%=featureInfo.getProvider() %>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="description"/></td>
                        <td><%=featureInfo.getDescription()%></td>
                    </tr>
<%
    if(copyrightInfo != null) {
%>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="copyright"/></td>
                        <td>
                            <textarea name="copyright_text" cols="80" rows="10"
                                      width="100%" readonly="true" class="myformat">
<%=copyrightInfo.getBody()%></textarea>
                        </td>
                    </tr>
<%
        }
        
    if(licenseInfo != null) {
%>
                    <tr class="tableOddRow">
                        <td><fmt:message key="license.agreement"/></td>
                        <td>
                            <textarea name="license_text" cols="80" rows="20"
                                      width="100%" readonly="true" class="myformat">
<%=licenseInfo.getBody()%></textarea>
                        </td>
                    </tr>
<%
        }
%>
        </tbody>
    </table>
</div>
<br/>
<div id="_div_feature_details_button_panel">
    <table class="styledLeft" cellspacing="1" width="100%"
           id="_table_feature_details_button_panel" style="margin-left: 0px;">
        <tbody>
        <tr>
            <td class="buttonRow">
                <input value="<fmt:message key="back.button"/>" tabindex="11" type="button"
                       class="button"
                       onclick="swapVisiblility('<%=divIDToShow%>', '<%=divIDToHide%>', <%=tabNumber%>);"
                       id="_btn_back__feature_details"/>
            </td>
        </tr>
        </tbody>
    </table>
</div>
</fmt:bundle>
