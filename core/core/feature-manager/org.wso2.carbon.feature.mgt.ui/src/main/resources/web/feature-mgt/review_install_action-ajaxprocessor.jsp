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
<%@ page import="org.eclipse.equinox.p2.metadata.Version" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.ProvisioningAdminClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.FeatureInfo" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.ProvisioningActionResultInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String httpMethod = request.getMethod().toLowerCase();

    if (!"post".equals(httpMethod)) {
        response.sendError(405);
        return;
    }

    String[] selectedFeatures = request.getParameterValues("selectedFeatures");
    HashMap<String, FeatureInfo> featuresMap = new HashMap<String, FeatureInfo>();
    FeatureInfo[] features = null;

    if(selectedFeatures != null){
        for (String selectedFeature : selectedFeatures) {
            String[] splittedStrings = selectedFeature.split("::");
            String featureID = splittedStrings[0];
            String featureVersion = splittedStrings[1];
            
            //resolve all the unique features using featureId & feature version
            FeatureInfo addedFeture = featuresMap.get(featureID);
            if (addedFeture == null) {
                FeatureInfo feature = new FeatureInfo();
                feature.setFeatureID(featureID);
                feature.setFeatureVersion(featureVersion);
                featuresMap.put(featureID, feature);
            } else {
                Version addedFeatureVersion = Version.create(addedFeture.getFeatureVersion());
                Version selectedFeatureVersion = Version.create(featureVersion);
                if(addedFeatureVersion.compareTo(selectedFeatureVersion) < 0){
                    addedFeture.setFeatureVersion(featureVersion);    
                }                
            }
        }
        features = featuresMap.values().toArray(new FeatureInfo[0]);
    }
    
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProvisioningAdminClient provAdminClient;

    ProvisioningActionResultInfo installActionResult;
    boolean proceedToNextStep;
    try {
        provAdminClient = new ProvisioningAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        installActionResult = provAdminClient.reviewInstallFeaturesAction(features);
        if(installActionResult == null){
            throw new Exception("Failed to review the installation plan");
        }
        proceedToNextStep = installActionResult.getProceedWithInstallation();
     } catch (Exception e) {
%>
<p id="compMgtErrorMsg"><%=e.getMessage()%></p>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">
<table class="styledLeft" cellspacing="1" width="100%" id="_table_af_review_licenses_list"
           style="margin-left: 0px;">
    <thead>
        <tr>
            <th><H4><strong><fmt:message key="install.details"/></strong></H4></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>
                <table class="normal" cellspacing="1" width="100%" id="_table_add_repository_link"
                   style="margin-left: 0px;">
                    <tbody>
                        <tr><td><font color="#707277"><i><fmt:message key="install.details.description"/></i></font></td></tr>
                        <tr>
                            <td>
                                <table class="styledLeft" cellspacing="1" width="100%" id="_table_review_features_list"
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
                                    FeatureInfo[] reviewedFeatures = installActionResult.getReviewedInstallableFeatures();
                                    if(reviewedFeatures == null || reviewedFeatures.length == 0){
                                %>
                                        <tr>
                                            <td colspan="0"><fmt:message key="no.features.to.be.installed"/>.</td>
                                        </tr>
                                	</tbody>
                                </table>
                            </td>
                        </tr>
                                        
                                <%
                                    } else {
                                        for(FeatureInfo feature: reviewedFeatures){
                                            String featureID = feature.getFeatureID();
                                            String featureVersion = feature.getFeatureVersion();
                                 %>
                                        <tr>
                                            <td><%=feature.getFeatureName()%></td>
                                            <td><%=featureVersion%></td>
                                            <td><%=feature.getFeatureID()%></td>
                                            <td><%=feature.getProvider()%></td>
                                        </tr>
                                <%
                                        }
                                %>
									</tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key="size.of.the.installation"/>: <%=installActionResult.getSize()%>
                            </td>
                        </tr>                                
                                <%       
                                    }
                                %>
                                   

                    <%
                        String description = installActionResult.getDetailedDescription();
                        if(description != null && !description.equals("")){
                    %>
                        <tr>
                            <td>
                                 <table class="styledLeft" cellspacing="1" width="100%" id="_table_review_features_description"
                                   style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th><fmt:message key="summary"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><%=description%></td>
                                    </tr>
                                    </tbody>
                                </table>
                             </td>
                        </tr>
                    <%
                        }
                    %>

                    </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input value="<fmt:message key="next.button"/>" tabindex="11" type="button"
                       class="button" <%=(proceedToNextStep)?"":"disabled='true'"%>
                       onclick="doNext('RF-RL')"
                       id="_btn_next_review_features"/>
                <input value="<fmt:message key="cancel.button"/>" tabindex="11" type="button"
                       class="button"
                       onclick="doBack('RF-AF')"
                       id="_btn_cancel_review_features"/>
            </td>
        </tr>
    </tbody>
</table>
</fmt:bundle>
<script type="text/javascript">
    alternateTableRows('_table_review_features_list', 'tableEvenRow', 'tableOddRow');
</script>