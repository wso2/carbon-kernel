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
<%@ page import="org.wso2.carbon.feature.mgt.ui.FeatureWrapper" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.ProvisioningAdminClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.RepositoryAdminServiceClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.RepositoryInfo" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.util.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Stack" %>
<%@ page import="org.wso2.carbon.base.ServerConfiguration" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<link type="text/css" href="css/ui.all.css" rel="stylesheet" />
<link rel="stylesheet" href="css/jquery.cluetip.css" type="text/css" />
<!--Yahoo includes for dom event handling-->
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" ></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<script type="text/javascript" src="js/tableTree.js" ></script>
<script type="text/javascript" src="js/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.hoverIntent.js"></script>
<script type="text/javascript" src="js/jquery.cluetip.js"></script>
<script type="text/javascript" src="js/comp-mgt-utils.js"></script>
<%
	int maxheight;
    boolean disableNext = true;
    String defaultRepoURL = null;
    RepositoryAdminServiceClient repositoryAdminClient;
    ProvisioningAdminClient provAdminClient;
    RepositoryInfo[] allRepositories = null;
    FeatureWrapper[] featureWrappers = null;
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    try {
        repositoryAdminClient = new RepositoryAdminServiceClient(cookie, backendServerURL, configContext, request.getLocale());

        ServerConfiguration serverConfiguration =
            (ServerConfiguration) config.getServletContext().getAttribute(CarbonConstants.SERVER_CONFIGURATION);

        if(serverConfiguration != null ){
            defaultRepoURL = serverConfiguration.getFirstProperty(CarbonConstants.FEATURE_REPO_URL);
        }
      	
        allRepositories = repositoryAdminClient.getAllRepositories();
        provAdminClient = new ProvisioningAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        featureWrappers = provAdminClient.getInstalledFeatures();
        request.getSession(true).setAttribute(ProvisioningAdminClient.INSTALLED_FEATURES, featureWrappers);
        maxheight = Utils.getMaxHeight(featureWrappers, 0);
     } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
       location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.feature.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="comp.management"
        resourceBundle="org.wso2.carbon.feature.mgt.ui.i18n.Resources"
        topPage="true" request="<%=request%>" />
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.server.admin.ui.i18n.JSResources"
        request="<%=request%>" />

<link type="text/css" href="css/ui.all.css" rel="stylesheet" />
<script type="text/javascript" src="js/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.hoverIntent.js"></script>
<script type="text/javascript" src="js/jquery.cluetip.js"></script>
<script type="text/javascript" src="js/comp-mgt-utils.js"></script>
<div id="middle">
    <h2><fmt:message key="comp.management"/></h2>
    <div id="workArea">
        <div id="tabs">

			<ul>
				<li><a href="#tabs-1"><fmt:message key="available.features"/></a></li>
				<li><a href="#tabs-2"><fmt:message key="installed.features"/></a></li>
                <li><a href="#tabs-3"><fmt:message key="installation.history"/></a></li>
                <li><a href="#tabs-4"><fmt:message key="repository.management"/></a></li>
			</ul>

            <div id="tabs-1">

                <div id="_div_tabs01-step-00-FR">
                    <jsp:include page="search_features-ajaxprocessor.jsp"/>
                </div>

                <div id="_div_tabs01-step-02-RF" style="display:none">
                </div>

                <div id="_div_tabs01-step-03-RL" style="display:none">
                </div>

                <div id="_div_tabs01-step-04-INSTALLING" style="display:none">
                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_installing"
                               style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><H4><strong><fmt:message key="installing"/></strong></H4></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><fmt:message key="installing.description"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div id="_div_tabs01-step-05-IC" style="display:none">
                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_Installation_Complete"
                               style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><H4><strong><fmt:message key="installation.complete"/></strong></H4></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <table class="normal" cellspacing="1" width="100%" id="_table_Installation_Complete_details"
                                       style="margin-left: 0px;">
                                        <tbody>
                                            <tr>
                                                <td><fmt:message key="succesfully.installed.description"/></td>
                                            </tr>
                                            <tr>
                                                <td><fmt:message key="restart.the.server"/></td>
                                            </tr>
                                        </tbody>
                                     </table>
                                </td>
                            </tr>
                        <tr>
                             <td class="buttonRow">
                                <input value="<fmt:message key="restart.now.button"/>" tabindex="11" type="button"
                                    class="button"
                                    onclick="doBack('IC-AF'); restartServerGracefully();"
                                    id="_btn_ic_restart_now"/>
                                <input value="<fmt:message key="restart.later.button"/>" tabindex="12" type="button"
                                    class="button"
                                    onclick="doBack('IC-AF')"
                                    id="_btn_ic_restart_later"/>
                             </td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <div id="_div_tabs01-FD" style="display:none">
                </div>

            </div>

			<div id="tabs-2">

                <div id="_div_tabs02-IF">
                    <H4><strong><fmt:message key="installed.features"/></strong></H4>
                    <p><font color="#707277"><i><fmt:message key="installed.features.description"/></i></font></p>
                    <br/>

                    <div id="_div_search_installed_features">
                        <table style="border:0; !important">
                            <tbody>
                            <tr style="border:0; !important">
                                <td style="border:0; !important">
                                    <nobr>
                                        <fmt:message key="filtered.by"/>
                                        <select id="_select_feature_type_top" tabindex="5"
                                                style="width: 120px" onchange="featureTypeOnChange(this)">
                                            <option value="ALL" selected="true"><fmt:message
                                                    key="all"/>
                                            </option>
                                            <option value="BACK_END"><fmt:message key="back.end"/>
                                            </option>
                                            <option value="FRONT_END"><fmt:message key="front.end"/>
                                            </option>
                                        </select>
                                        &nbsp;&nbsp;&nbsp;
                                        <fmt:message key="name"/>
                                        <input id="_txt_IF_filterString" class="log-select"
                                               type="text" size="40" value=""
                                               onkeypress="submitenter(event, true)"/>&nbsp;
                                    </nobr>
                                </td>
                                <td style="border:0; !important">
                                    <a id="_icon_IF_filterString" class="icon-link" href="#"
                                       style="background-image: url(images/search.gif);"
                                       onclick="searchInstalledFeatures(); return false;"
                                       alt="<fmt:message key="search.button"/>"></a>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                    </div>
                    <div id="_div_tabs02_loading_IF">
                        <table class="styledLeft" cellspacing="1" width="100%" id="_table_loading-IF"
                               style="margin-left: 0px;">
                            <tbody>
                            <tr>
                                <td><fmt:message key="loading"/>...</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div id="_div_installed_features_list" style="display:none"></div>
                </div>

                <div id="_div_tabs02-RUF" style="display:none">
                </div>

                <div id="_div_tabs02-FD" style="display:none">
                </div>

                <div id="_div_tabs02-UNINSTALLING" style="display:none">
                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_uninstalling"
                               style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><H4><strong><fmt:message key="uninstalling"/></strong></H4></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><fmt:message key="uninstalling.description"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div id="_div_tabs02-UC" style="display:none">
                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_Uninstallation_Complete"
                               style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><H4><strong><fmt:message key="uninstallation.complete"/></strong></H4></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <table class="normal" cellspacing="1" width="100%" id="_table_Unnstallation_Complete_details"
                                       style="margin-left: 0px;">
                                        <tbody>
                                            <tr>
                                                <td><fmt:message key="uninstalled.message"/></td>
                                            </tr>
                                            <tr>
                                                <td><fmt:message key="restart.the.server"/></td>
                                            </tr>
                                        </tbody>
                                     </table>
                                </td>
                            </tr>
                            <tr>
                                <td class="buttonRow">
                                    <input value="<fmt:message key="restart.now.button"/>" tabindex="11" type="button"
                                        class="button"
                                        onclick="doBack('UC-IF'); restartServerGracefully();"
                                        id="_btn_ic_restart_now"/>
                                    <input value="<fmt:message key="restart.later.button"/>" tabindex="12" type="button"
                                        class="button"
                                        onclick="doBack('UC-IF')"
                                        id="_btn_ic_restart_later"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

            </div>

            <dir id="tabs-3">

                <div id="_div_tabs03-IH" style="display:none"></div>

                <div id="_div_tabs03-Loading-IH" >
                    <H4><strong><fmt:message key="installation.history"/></strong></H4>
                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_loading-IH"
                               style="margin-left: 0px;">

                        <tbody>
                            <tr>
                                <td><fmt:message key="loading"/>...</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div id="_div_tabs03-RP" style="display:none" >
                </div>

                <div id="_div_tabs03-FD" style="display:none">
                </div>

                <div id="_div_tabs03-RC" style="display:none">
                    <table class="styledLeft" cellspacing="1" width="100%" id="_table_revert_Complete"
                               style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><H4><strong><fmt:message key="reverting.is.completed"/></strong></H4></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    <table class="normal" cellspacing="1" width="100%" id="_table_revert_Complete_details"
                                       style="margin-left: 0px;">
                                        <tbody>
                                            <tr>
                                                <td><fmt:message key="reverting.completed.description"/></td>
                                            </tr>
                                            <tr>
                                                <td><fmt:message key="restart.the.server"/></td>
                                            </tr>
                                        </tbody>
                                     </table>
                                </td>
                            </tr>
                        <tr>
                             <td class="buttonRow">
                                <input value="<fmt:message key="finish.button"/>" tabindex="11" type="button"
                                       class="button"
                                       onclick="doBack('RC-IH')"
                                       id="_btn_rc_finish"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>

            </dir>

            <div id="tabs-4">



                <div id="_div_tabs04-MR">
                    <table class="styledLeft" cellspacing="1" width="100%"
                               id="_table_manage_repositories" style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><fmt:message key="manage.repositories"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><table class="normal" cellspacing="1" width="100%" id="_table_add_repository_link"
                                       style="margin-left: 0px;">
                                    <tbody>
                                    <tr><td><font color="#707277"><i><fmt:message key="manage.repositories.description"/></i></font></td></tr>
                                    <br/>
                                    <tr>
                                        <td><a class="icon-link" href="#" onclick="swapVisiblility('_div_tabs04-MR', '_div_tabs04-AR', 4);addRepositoryOnCompleteReturnTabID='SETTINGS';"
                                               style="background-image: url(images/add.gif);"><fmt:message key="add.new.repository"/></a></td>
                                    </tr>
                                    <tr><td><fmt:message key="available.repositories"/></td></tr>
                                    <tr><td>
                                        <div id="_div_repository_list">
                                            <table class="styledLeft" cellspacing="1" width="100%" id="_table_repository_list"
                                                                                   style="margin-left: 0px;">
                                                <thead>
                                                <tr>
                                                    <th><fmt:message key="name"/></th>
                                                    <th><fmt:message key="location"/></th>
                                                    <th><fmt:message key="enabled"/></th>
                                                    <th colspan="4"><fmt:message key="actions"/></th>
                                                </tr>
                                                </thead>
                                                <tbody>

                                                <%
                                                    if(allRepositories == null || allRepositories.length == 0){
                                                %>
                                                <tr>
                                                    <td colspan="0"><fmt:message key="no.available.repositories"/>.</td>
                                                </tr>
                                                <%
                                                    } else {
                                                        for(RepositoryInfo repository:allRepositories){
                                                            String location = repository.getLocation();
                                                            String repoNickName = repository.getNickName();
                                                            boolean enabled = repository.getEnabled();
                                                            String status;
                                                            if(enabled){
                                                                status =  RepositoryAdminServiceClient.ENABLED;
                                                            } else {
                                                                status = RepositoryAdminServiceClient.DISABLED;
                                                            }

                                                            String oppositeStatus = (RepositoryAdminServiceClient.ENABLED.equals(
                                                                    status))?RepositoryAdminServiceClient.DISABLED:RepositoryAdminServiceClient.ENABLED;

                                                            String statusText = oppositeStatus.substring(0, oppositeStatus.length() -1);

                                                %>
                                                <tr>
                                                    <td><%=repoNickName%></td>
                                                    <td><%=location%></td>
                                                    <td><%=status%></td>
                                                    <td>
                                                        <a class="icon-link" style="background-image: url(../admin/images/edit.gif);"
                                                           href="#" onclick="setSelectedRepoProperties('<%=location%>', '<%=repoNickName%>', null);
                                                           swapVisiblility('_div_tabs04-MR', '_div_tabs04-ER', 4);fillEditRepoTextBoxes();return false;"><fmt:message key="edit.button"/></a>
                                                    </td>
                                                    <td>
                                                        <a class="icon-link" style="background-image: url(../admin/images/delete.gif);"
                                                           href="#" onclick="setSelectedRepoProperties('<%=location%>', null, null);
                                                           showConfirmationDialogBox('<fmt:message key="confirm.removing.repo"/>',
                                                           removeRepository);return false;"><fmt:message key="remove"/></a>
                                                    </td>
                                                    <td>
                                                        <a class="icon-link"
                                                           <%=statusText.equals("Enable")?"style='background-image: url(images/activate.gif)';":"style='background-image: url(images/deactivate.gif)';"%>
                                                           href="#" onclick="setSelectedRepoProperties('<%=location%>', null, '<%=oppositeStatus%>');
                                                           showConfirmationDialogBox('<fmt:message key="confirm.enabling.repo.part1"/> '+ '<%=statusText%>' +' <fmt:message key="confirm.enabling.repo.part2"/>',
                                                           enableRepository);return false;">
                                                            <%
                                                                if(statusText.equals("Enable")){
                                                            %>
                                                                <fmt:message key="enable"/>
                                                            <%
                                                                } else {
                                                             %>
                                                                <fmt:message key="disable"/>
                                                            <%
                                                                }
                                                            %>
                                                           </a>
                                                    </td>
                                                </tr>
                                                <%
                                                        }
                                                    }
                                                %>
                                                </tbody>
                                                </table>
                                                <script type="text/javascript">
                                                    alternateTableRows('_table_repository_list', 'tableEvenRow', 'tableOddRow');
                                                </script>
                                        </div>
                                    </td></tr>
                                    </tbody>
                                </table>  </td>
                            </tr>

                        </tbody>
                     </table>
                </div>

                <div id="_div_tabs04-AR" style="display:none">
                    <table class="styledLeft" cellspacing="1" width="100%"
                               id="_table_add_repositories" style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><fmt:message key="add.new.repository"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><table class="normal" cellspacing="1" width="100%" id="_table_add_repository"
                                       style="margin-left: 0px;">
                                    <tbody>
                                    <tr><td><font color="#707277"><i><fmt:message key="add.repo.desciption"/></i></font></td></tr>
                                    <br/>
                                    <tr>
                                        <td>
                                            <table class="normal" width="100%">
                                                <tbody>
                                                <tr>
                                                    <td><fmt:message key="name"/>:&nbsp;<font class="required">*</font></td>
                                                    <td colspan="3">
                                                        <input id="_txt_repository_name" style="width: 390px;" class="toolsClass" type="text"/>
                                                    </td>
                                                    <td width="100%"></td>
                                                </tr>
                                                <tr>
                                                    <td><fmt:message key="location"/>:&nbsp;</td>
                                                    <td><input id="_chk_repository_location_url"  class="toolsClass"
                                                               name="repository_location" type="radio"
                                                               checked="checked" onchange="document.getElementById('_txt_repository_location_file').disabled = true;document.getElementById('_txt_repository_location_url').disabled = false;"></td>
                                                    <td><fmt:message key="url"/></td>
                                                    <td><input id="_txt_repository_location_url" style="width: 300px;" class="toolsClass" type="text" value="http://"/></td>
                                                    <td><font color="#707277"><i>
                                                    <%
                                                    	if (defaultRepoURL != null) {
                                                    %>
                                                    	e.g. <%=defaultRepoURL%>
                                                    <%		
                                                    	}
                                                    %>
                                                    </i></font></td>
                                                </tr>
                                                <tr>
                                                    <td>&nbsp;</td>
                                                    <td><input id="_chk_repository_location_file"  class="toolsClass" name="repository_location" type="radio"
                                                            onchange="document.getElementById('_txt_repository_location_url').disabled = true;document.getElementById('_txt_repository_location_file').disabled = false;"></td>
                                                    <td><fmt:message key="file"/></td>
                                                    <td><input id="_txt_repository_location_file" style="width: 300px;"
                                                               class="toolsClass" type="text" disabled="disabled"/></td>
                                                    <td><font color="#707277"><i><fmt:message key="sample.local.repo.desciption"/></i></font></td>

                                                </tr>
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>  </td>
                            </tr>
                            <tr>
                                <td class="buttonRow">
                                    <input value="<fmt:message key="add.button"/>" tabindex="11" type="button"
                                           class="button"
                                           onclick="addRepository('<fmt:message key="adding.repository"/>....');"
                                           id="_btn_add_repository">
                                    <input value="<fmt:message key="cancel.button"/>" tabindex="11" type="button"
                                           class="button"
                                           onclick="doBack('AR-MR')"
                                           id="_btn_cancel_add_repository">
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div id="_div_tabs04-ER" style="display:none">
                    <table class="styledLeft" cellspacing="1" width="100%"
                               id="_table_edit_repositories" style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th><fmt:message key="edit.existing.repository"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><table class="normal" cellspacing="1" width="100%" id="_table_edit_repository_normal"
                                       style="margin-left: 0px;">
                                    <tbody>
                                    <tr><td><font color="#707277"><i><fmt:message key="edit.repo.description"/></i></font></td></tr>
                                    <br/>
                                    <tr>
                                        <td>
                                            <table class="normal">
                                                <tbody>
                                                <tr>
                                                    <td><fmt:message key="name"/>:&nbsp;</td>
                                                    <td>
                                                        <input id="_txt_edit_repository_name" style="width: 300px;" class="toolsClass" type="text"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><fmt:message key="location"/>:&nbsp;</td>
                                                    <td>
                                                        <input id="_txt_edit_repository_location" style="width: 300px;" class="toolsClass" disabled="disabled" type="text"/>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>  </td>
                            </tr>
                            <tr>
                                <td class="buttonRow">
                                    <input value="<fmt:message key="save.button"/>" tabindex="11" type="button"
                                           class="button"
                                           onclick="editRepository();"
                                           id="_btn_Finish_edit_repository">
                                    <input value="<fmt:message key="cancel.button"/>" tabindex="11" type="button"
                                           class="button"
                                           onclick="doBack('ER-MR')"
                                           id="_btn_cancel_edit_repository">
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

		</div>
    &nbsp;</div>
</div>
<script type="text/javascript">
    YAHOO.util.Event.onDOMReady(function() {
        customAlternateTableRows('_table_installed_features_list', 'tableEvenRow', 'tableOddRow');
    });
</script>
</fmt:bundle>
