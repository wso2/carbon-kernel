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
<%@ page import="org.wso2.carbon.feature.mgt.ui.ProvisioningAdminClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionResultInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="java.util.Date" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String timestamp = CharacterEncoder.getSafeText(request.getParameter("timestamp"));
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProvisioningAdminClient provAdminClient;
    ProvisioningActionResultInfo provActionResultInfo;

    SimpleDateFormat monthDayYearformatter = new SimpleDateFormat("MMMMM dd, yyyy 'at' HH:mm:ss z");
	Date date=new Date(Long.parseLong(timestamp));
    String summary = monthDayYearformatter.format(date);

    try {
        provAdminClient = new ProvisioningAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        provActionResultInfo = provAdminClient.reviewRevertPlan(timestamp);
     } catch (Exception e) {
%>
<p id="compMgtErrorMsg"><%=e.getMessage()%></p>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">
<table class="styledLeft" cellspacing="1" width="100%" id="_table_revert_plan"
           style="margin-left: 0px;">
    <thead>
        <tr>
            <th><H4><strong><fmt:message key="reverting.to.previous.configuration"/> - <%=summary%></strong></H4></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>
                <table class="normal" cellspacing="1" width="100%" id="_table_revert_plan_normal"
                   style="margin-left: 0px;">
                    <tbody>
                        <tr><td><font color="#707277"><i><fmt:message key="reverting.description"/>.</i></font></td></tr>
                        <%
                            FeatureInfo[] installedFeatures = provActionResultInfo.getReviewedInstallableFeatures();
                            if(installedFeatures != null && installedFeatures.length != 0){
                        %>
                        <tr>
                           <td><fmt:message key="to.be.installed.features"/></td>
                        </tr>
                        <tr>
                            <td>
                                <table class="styledLeft" cellspacing="1" width="100%" id="_table_features_to_install"
                                       style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="name"/></th>
                                        <th><fmt:message key="version1"/></th>
                                        <th><fmt:message key="id"/></th>
                                        <th><fmt:message key="provider"/></th>
                                        <%--<th><fmt:message key="actions"/></th>--%>
                                    </tr>
                                    </thead>
                                    <tbody>
                        <%
                                for(FeatureInfo feature: installedFeatures){
                                    String featureID = feature.getFeatureID();
                                    String featureVersion = feature.getFeatureVersion();
                         %>
                                <tr>
                                    <td><%=feature.getFeatureName()%></td>
                                    <td><%=featureVersion%></td>
                                    <td><%=featureID%></td>
                                    <td><%=feature.getProvider()%></td>
                                    <%--<td>--%>
                                        <%--<a class="icon-link" style="background-image: none;"--%>
                                               <%--href="#" onclick="loadSelectedFeatureDetails('<%=featureID%>','<%=featureVersion%>', true, '_div_tabs03-FD', '_div_tabs03-RP', 3);"><fmt:message key="more.info1"/>.</a>--%>
                                    <%--</td>--%>
                                </tr>
                        <%
                                }
                        %>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <script type="text/javascript">
                            alternateTableRows('_table_features_to_install', 'tableEvenRow', 'tableOddRow');
                        </script>
                        <%
                            }

                            FeatureInfo[] uninstalledFeatures = provActionResultInfo.getReviewedUninstallableFeatures();
                            if(uninstalledFeatures != null && uninstalledFeatures.length != 0){
                        %>
                        <tr>
                           <td><fmt:message key="uninstalled.features"/></td>
                        </tr>
                        <tr>
                            <td>
                                <table class="styledLeft" cellspacing="1" width="100%" id="_table_features_to_uninstall"
                                       style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="name"/></th>
                                        <th><fmt:message key="version1"/></th>
                                        <th><fmt:message key="id"/></th>
                                        <th><fmt:message key="provider"/></th>
                                        <%--<th><fmt:message key="actions"/></th>--%>
                                    </tr>
                                    </thead>
                                    <tbody>
                        <%
                                for(FeatureInfo feature: uninstalledFeatures){
                                    String featureID = feature.getFeatureID();
                                    String featureVersion = feature.getFeatureVersion();
                         %>
                                <tr>
                                    <td><%=feature.getFeatureName()%></td>
                                    <td><%=featureVersion%></td>
                                    <td><%=featureID%></td>
                                    <td><%=feature.getProvider()%></td>
                                    <%--<td>--%>
                                        <%--<a class="icon-link" style="background-image: none;"--%>
                                               <%--href="#" onclick="loadSelectedFeatureDetails('<%=featureID%>','<%=featureVersion%>', true, '_div_tabs03-FD', '_div_tabs03-RP', 3);"><fmt:message key="more.info1"/>.</a>--%>
                                    <%--</td>--%>
                                </tr>
                        <%
                                }
                        %>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <script type="text/javascript">
                            alternateTableRows('_table_features_to_uninstall', 'tableEvenRow', 'tableOddRow');
                        </script>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input value="<fmt:message key="back.button"/>" tabindex="11" type="button"
                       class="button"
                       onclick="doBack('RP-IH')"
                       id="_btn_back_revert_plan">
                <input value="<fmt:message key="revert.button"/>" tabindex="11" type="button"
                       class="button" onclick="doRevert();"
                       id="_btn_revert_revert_plan">
                <input id="_hidden_RP_actionType" value="<%=provAdminClient.getRevertActionType()%>" type="hidden" />            
            </td>
        </tr>
    </tbody>
</table>
</fmt:bundle>