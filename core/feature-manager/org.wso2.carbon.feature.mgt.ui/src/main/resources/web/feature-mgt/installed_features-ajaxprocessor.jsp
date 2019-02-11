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
<%@ page import="org.wso2.carbon.feature.mgt.ui.FeatureWrapper" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.ProvisioningAdminClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.Feature" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.util.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Stack" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%

    String httpMethod = request.getMethod().toLowerCase();

    if (!"post".equals(httpMethod)) {
        response.sendError(405);
        return;
    }
    
    String queryType = CharacterEncoder.getSafeText(request.getParameter("queryType"));
    String filterStr = CharacterEncoder.getSafeText(request.getParameter("filterStr"));
    String filterType=CharacterEncoder.getSafeText(request.getParameter("filterType"));  // filter Type : ALL / FRONT_END or BACK-END
    
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProvisioningAdminClient provAdminClient;
    FeatureWrapper[] featureWrappers = null;
    int maxheight;

    try {
         if("searchQuery".equals(queryType)){
            featureWrappers = (FeatureWrapper[])request.getSession(false).getAttribute(ProvisioningAdminClient.INSTALLED_FEATURES);
            if(featureWrappers != null)
                featureWrappers = Utils.filterFeatures(featureWrappers, filterStr);
         }

        if(featureWrappers == null){
            provAdminClient = new ProvisioningAdminClient(cookie, backendServerURL, configContext, request.getLocale());
            featureWrappers = provAdminClient.getInstalledFeatures();
            request.getSession(true).setAttribute(ProvisioningAdminClient.INSTALLED_FEATURES, featureWrappers);
        }

        maxheight = Utils.getMaxHeight(featureWrappers, 0);

     } catch (Exception e) {
%>
<p id="compMgtErrorMsg"><%=e.getMessage()%></p>
<%
        return;
    }

if("FRONT_END".equals(filterType)){
%>
<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">

<carbon:itemGroupSelector selectAllInPageFunction=""
                          selectAllFunction=""
                          selectNoneFunction=""
                          addRemoveFunction= "doUninstall()"
                          addRemoveButtonId= "uninstall1"
                          resourceBundle="org.wso2.carbon.feature.mgt.ui.i18n.Resources"
                          selectAllInPageKey="select.all.in.page"
                          selectAllKey="select.all"
                          selectNoneKey="select.none"
                          addRemoveKey= "uninstall"/>

<table class="styledLeft" cellspacing="1" width="100%" id="_table_installed_features_list_main"
                               style="margin-left: 0px;">
                            <tbody>

                            <tr>
                                <td style="padding:0px !important;">
                                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_installed_child_ui_features"
                                           style="margin-left: 0px;">
                                        <thead>
                                                <tr>
                                                    <th><fmt:message key="features"/></th>
                                                    <th><fmt:message key="version"/></th>
                                                    <th><fmt:message key="actions"/></th>
                                                </tr>
                                                </thead>
                                                <tbody id="_table_installed_child_features_ui_body">
                                    <%
                                        if(featureWrappers == null || featureWrappers.length == 0){
                                    %>
                                            <tr>
                                                <td colspan="0"><fmt:message key="no.features.availables"/></td>
                                            </tr>
                                    <%
                                        } else {
                                            FeatureWrapper[] uniqueFeatureArray = Utils.getUniqueFeatureList(featureWrappers);
                                            for (FeatureWrapper fw : uniqueFeatureArray) {
                                                Feature currentFeature = fw.getWrappedFeature();
                                                if ("console".equalsIgnoreCase(currentFeature.getFeatureType())){
                                    %>
                                    <tr>
                                        <td>
                                            <input class="checkbox-select" type="checkbox"
                                                   name="chkSelectFeaturesToUninstall"
                                                   disabled="true"
                                                   checked="true"
                                                   value="<%=currentFeature.getFeatureID()%>::<%=currentFeature.getFeatureVersion()%>"/>

                                            <%=currentFeature.getFeatureName()%>
                                        </td>
                                        <td><%=currentFeature.getFeatureVersion()%>
                                        </td>
                                        <td>
                                            <a class="icon-link" style="background-image: url(images/more-info.gif);"
                                               href="#" onclick=""><fmt:message key="more.info"/></a>
                                        </td>
                                    </tr>

                                    <%
                                                }
                                            }
                                        }
                                    %>
                                            </tbody>
                                        </table>
                                    </td>
                                </tr>

                                </tbody>
                            </table>

<carbon:itemGroupSelector selectAllInPageFunction=""
                          selectAllFunction=""
                          selectNoneFunction=""
                          addRemoveFunction="doUninstall()"
                          addRemoveButtonId= "uninstall2"
                          resourceBundle="org.wso2.carbon.feature.mgt.ui.i18n.Resources"
                          selectAllInPageKey="select.all.in.page"
                          selectAllKey="select.all"
                          selectNoneKey="select.none"
                          addRemoveKey= "uninstall"/>


</fmt:bundle>

<%
}else if("BACK_END".equals(filterType)){
%>

<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">

<carbon:itemGroupSelector selectAllInPageFunction=""
                          selectAllFunction=""
                          selectNoneFunction=""
                          addRemoveFunction= "doUninstall()"
                          addRemoveButtonId= "uninstall1"
                          resourceBundle="org.wso2.carbon.feature.mgt.ui.i18n.Resources"
                          selectAllInPageKey="select.all.in.page"
                          selectAllKey="select.all"
                          selectNoneKey="select.none"
                          addRemoveKey= "uninstall"/>

<table class="styledLeft" cellspacing="1" width="100%" id="_table_installed_features_list_main"
                               style="margin-left: 0px;">
                            <tbody>

                            <tr>
                                <td style="padding:0px !important;">
                                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_installed_child_Server_features"
                                           style="margin-left: 0px;">
                                        <thead>
                                                <tr>
                                                    <th><fmt:message key="features"/></th>
                                                    <th><fmt:message key="version"/></th>
                                                    <th><fmt:message key="actions"/></th>
                                                </tr>
                                                </thead>
                                                <tbody id="_table_installed_child_features_server_body">
                                    <%
                                        if(featureWrappers == null || featureWrappers.length == 0){
                                    %>
                                            <tr>
                                                <td colspan="0"><fmt:message key="no.features.availables"/></td>
                                            </tr>
                                    <%
                                        } else {
                                            FeatureWrapper[] uniqueFeatureArray = Utils.getUniqueFeatureList(featureWrappers);
                                            for (FeatureWrapper fw : uniqueFeatureArray) {
                                                Feature currentFeature = fw.getWrappedFeature();
                                                if ("server".equalsIgnoreCase(currentFeature.getFeatureType())){
                                    %>
                                    <tr>
                                        <td>
                                            <input class="checkbox-select" type="checkbox"
                                                   name="chkSelectFeaturesToUninstall"
                                                   disabled="true"
                                                   checked="true"
                                                   value="<%=currentFeature.getFeatureID()%>::<%=currentFeature.getFeatureVersion()%>"/>

                                            <%=currentFeature.getFeatureName()%>
                                        </td>
                                        <td><%=currentFeature.getFeatureVersion()%>
                                        </td>
                                        <td>
                                            <a class="icon-link" style="background-image: url(images/more-info.gif);"
                                               href="#" onclick=""><fmt:message key="more.info"/></a>
                                        </td>
                                    </tr>

                                    <%
                                                }
                                            }
                                        }
                                    %>
                                            </tbody>
                                        </table>
                                    </td>
                                </tr>

                                </tbody>
                            </table>

<carbon:itemGroupSelector selectAllInPageFunction=""
                          selectAllFunction=""
                          selectNoneFunction=""
                          addRemoveFunction="doUninstall()"
                          addRemoveButtonId= "uninstall2"
                          resourceBundle="org.wso2.carbon.feature.mgt.ui.i18n.Resources"
                          selectAllInPageKey="select.all.in.page"
                          selectAllKey="select.all"
                          selectNoneKey="select.none"
                          addRemoveKey= "uninstall"/>


</fmt:bundle>



<%
}else{
%>

<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">

<carbon:itemGroupSelector selectAllInPageFunction="selecteAllUninstallableFeatures(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selecteAllUninstallableFeatures(false)"
                          addRemoveFunction= "doUninstall()"
                          addRemoveButtonId= "uninstall1"
                          resourceBundle="org.wso2.carbon.feature.mgt.ui.i18n.Resources"
                          selectAllInPageKey="select.all.in.page"
                          selectAllKey="select.all"
                          selectNoneKey="select.none"
                          addRemoveKey= "uninstall"/>   

<table class="styledLeft" cellspacing="1" width="100%" id="_table_installed_features_list_main"
                               style="margin-left: 0px;">
                            <tbody>
                            
                            <tr>
                                <td style="padding:0px !important;">
                                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_installed_features_list"
                                           style="margin-left: 0px;">
                                        <thead>
                                                <tr>
                                                    <th><fmt:message key="features"/></th>
                                                    <th><fmt:message key="version"/></th>
                                                    <th><fmt:message key="actions"/></th>
                                                </tr>
                                                </thead>
                                                <tbody id="_table_installed_features_list_body">
                                    <%
                                        if(featureWrappers == null || featureWrappers.length == 0){
                                    %>
                                            <tr>
                                                <td colspan="0"><fmt:message key="no.features.availables"/></td>
                                            </tr>
                                    <%
                                        } else {
                                            featureWrappers=Utils.truncatePrefixAndSuffixOfFeature(featureWrappers);
                	                        Utils.sortAscendingByFeatureName(featureWrappers);
                                            for (FeatureWrapper featureWrapper : featureWrappers) {
                                                Stack<FeatureWrapper> stack = new Stack<FeatureWrapper>();
                                                stack.add(featureWrapper);
                                                while (!stack.isEmpty()) {
                                                    FeatureWrapper popedFeature = stack.pop();
                                                    Feature feature = popedFeature.getWrappedFeature();
                                                    String featureVersion = feature.getFeatureVersion();
                                                    String featureID = feature.getFeatureID();
                                                    int height = popedFeature.getHeight();
                                                    boolean hasChildren = popedFeature.hasChildren();
                                                    if(height == 0){
                                     %>
                                            <tr>
                                     <%

                                                    } else {
                                      %>
                                            <tr style="display:none; background-color:#fff;">
                                     <%
                                                    }
                                     %>
	                                     <td class="featureNameCol" abbr="<%=((maxheight + 3) - (height + 2))%>"><%--featureNameCol is not for styling it's to get the elements from client side--%>
                                     <%
                                     		int spaceWidth = height*16;
                                     		if(!hasChildren){
                                     			spaceWidth += 16;
                                     		}
				     %>                                                    
                                                <img src="images/spacer.png" style="height:1px;width:<%=spaceWidth%>px;"  />
                                     <%
                                                    if(hasChildren){
                                     %>
                                                <img src="images/plus.gif" style="cursor:pointer;" style="cursor:pointer" onclick="collapseTree(this)" class="ui-plusMinusIcon" />
                                     <%
                                          	}          
	
                                                    if(height != 0){
                                     %>
                                                <img src="images/spacer.png" style="height:1px;width:10px;"  />

                                     <%
                                                   } else {
                                     %>

                                                
                                                    <input class="checkbox-select" type="checkbox"
                                                           name="chkSelectFeaturesToUninstall"
                                                           <%=(height != 0 || ("org.wso2.carbon.core.feature.group").equals(feature.getFeatureID()))?"disabled='disabled'":""%>
                                                           value="<%=feature.getFeatureID()%>::<%=feature.getFeatureVersion()%>"
                                                           onclick="checkBoxSelectedUninstall(this);"/>
                                     <%
                                                    }
                                     %>
                                                <span class="featureDiscriptionOfInstalledFeatures" id="<%=feature.getFeatureID().replace('.','-')%>">
                                                <%=feature.getFeatureName()%></span></td>   
                                                <td><%=featureVersion%></td>
                                                <td>
                                                    <a class="icon-link" style="background-image: url(images/more-info.gif);"
                                                           href="#" onclick="loadSelectedFeatureDetails('<%=featureID%>','<%=featureVersion%>', true, '_div_tabs02-FD', '_div_tabs02-IF', 2);"><fmt:message key="more.info"/></a>
                                                </td>
                                            </tr>
                                    <%
                                                    FeatureWrapper[] requiredFeatures = popedFeature.getRequiredFeatures();
                                                    if(requiredFeatures != null && requiredFeatures.length > 0){
                                                        requiredFeatures=Utils.truncatePrefixAndSuffixOfFeature(requiredFeatures);
                            	                        Utils.sortDescendingByFeatureName(requiredFeatures);
                                                        for(FeatureWrapper requiredFeature: requiredFeatures){
                                                            stack.push(requiredFeature);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    %>
                                            </tbody>
                                        </table>
                                    </td>
                                </tr>
                                
                                </tbody>
                            </table>
    
<carbon:itemGroupSelector selectAllInPageFunction="selecteAllUninstallableFeatures(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selecteAllUninstallableFeatures(false)"
                          addRemoveFunction="doUninstall()"
                          addRemoveButtonId= "uninstall2"
                          resourceBundle="org.wso2.carbon.feature.mgt.ui.i18n.Resources"
                          selectAllInPageKey="select.all.in.page"
                          selectAllKey="select.all"
                          selectNoneKey="select.none"
                          addRemoveKey= "uninstall"/>


</fmt:bundle>

<%
    }
%>

<script type="text/javascript">
    YAHOO.util.Event.onDOMReady(function() {
        customAlternateTableRows('_table_installed_features_list', 'tableEvenRow', 'tableOddRow');
    });
</script>
