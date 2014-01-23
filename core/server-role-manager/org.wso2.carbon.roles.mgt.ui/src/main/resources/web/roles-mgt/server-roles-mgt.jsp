<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.roles.mgt.ui.ServerRoleManagerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServerRoleManagerClient client;

    String[] defaultServerRoles;
    String[] customServerRoles;

    try {
        client = new ServerRoleManagerClient(configContext, serverURL, cookie);
        defaultServerRoles = client.getServerRoles("Default");
        customServerRoles = client.getServerRoles("Custom");

    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.roles.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="server-roles"
                   resourceBundle="org.wso2.carbon.roles.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>

<script type="text/javascript">

    function deleteServerRole(serverRole, serverRoleType) {
        var serverRoleName = serverRole;
        CARBON.showConfirmationDialog('<fmt:message key="confirm.delete.server-role"/>  \'' +
                                      serverRoleName + '\' ?', function () {
            location.href = 'delete-server-role.jsp?serverRoleName=' + serverRoleName +
                            '&serverRoleType=' + serverRoleType;
        }, null);
    }

    function addServerRole() {
        var serverRoleName = document.getElementById('_serverRoleName');
        var serverRoleType = '<fmt:message key="server-role.type.custom"/>';

        if (validateInput(serverRoleName.value, serverRoleType)) {
            location.href = 'add-server-role.jsp?serverRoleName=' + serverRoleName.value +
                            "&serverRoleType="
                    + serverRoleType;
        } else {

        }
        return true;
    }

    function validateInput(serverRoleName, serverRolesType) {
//        serverRoleName = stripWhitespace(serverRoleName);
        if (!(serverRoleName.search(/^[A-Za-z0-9 _]{1,30}$/) != -1)){
            CARBON.showWarningDialog('<fmt:message key="warn.wrong.server-role.name.format"/>',
                                     null, null);
            return false;
        }

        if (serverRoleName == null || serverRoleName == "") {
            CARBON.showWarningDialog('<fmt:message key="warn.empty.server-role.name"/> ',
                                     null, null);
            return false;
        }

        var isExistingBool1 = isExisting(serverRoleName,
                                         '<fmt:message key = "server-role.type.default"/>');
        var isExistingBool2 = isExisting(serverRoleName,
                                         '<fmt:message key="server-role.type.custom"/>');
        if (isExistingBool1 || isExistingBool2) {
            CARBON.showWarningDialog('<fmt:message key="warn.existing.server-role.name"/>',
                                     null, null, null);
            return false;
        }

        return true;
    }

    function isExisting(serverRoleName, serverRolesType) {
        var status = false;
    <%int j = 0;
   String[] array=null;%>
        if ('<fmt:message key="server-role.type.default"/>' == serverRolesType) {
        <%if(defaultServerRoles != null) {
            array = defaultServerRoles;
        }%>
        } else if ('<fmt:message key="server-role.type.custom"/>' == serverRolesType) {
        <%if(customServerRoles != null) {
            array = customServerRoles;
        }%>
        }

    <%if(array != null) {
 if (array.length != 0){%>
        for (var i = 0; i <= <%=array.length%>; i++) {
            if ('<%=array[j]%>' == serverRoleName) {
                return true;
            }
        <%j++;%>
        }
    <%}
    }%>
        return status;
    }

    function showProperties() {
        if ($('propertiesIconExpanded').style.display == "none") {
//	We have to expand all and hide sum
            $('propertiesIconExpanded').style.display = "";
            $('propertiesIconMinimized').style.display = "none";
            $('propertiesExpanded').style.display = "";
            $('propertiesMinimized').style.display = "none";
        }
        else {
            $('propertiesIconExpanded').style.display = "none";
            $('propertiesIconMinimized').style.display = "";
            $('propertiesExpanded').style.display = "none";
            $('propertiesMinimized').style.display = "";
        }
    }

    // removes any whitespace from the string and returns the result
    // the value of "replacement" will be used to replace the whitespace (optional)
    function stripWhitespace(str, replacement) {
        if (replacement == null) replacement = '';
        var result = str;
        var re = new RegExp(/\s/g);
        if (str.search(re) != -1) {
            result = str.replace(re, replacement);
        }
        return result;
    }

    function showHideCommon(divId) {
        var theDiv = document.getElementById(divId);
        if (theDiv.style.display == "none") {
            theDiv.style.display = "";
        } else {
            theDiv.style.display = "none";
        }
    }

    jQuery(document).ready(function() {
        //Hide (Collapse) the toggle containers on load
        jQuery(".toggle_container").hide();
        jQuery("h2.trigger").click(function() {
            jQuery(this).toggleClass("active").next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        });
    });
</script>


<div id="middle">
    <h2><fmt:message key="server-roles"/></h2>

    <div id="workArea">

        <% if ((defaultServerRoles != null) && (defaultServerRoles.length != 0) ||
                (customServerRoles != null) && (customServerRoles.length != 0)) { %>

        <table class="styledLeft" id="roleTable">
            <thead>
            <tr>
                <th width="30%"><fmt:message key="name"/></th>
                <th width="30%"><fmt:message key="type"/></th>
                <th width="40%"><fmt:message key="actions"/></th>
            </tr>
            </thead>
            <tbody>
            <%
                if (defaultServerRoles != null && defaultServerRoles.length != 0) {
                    for (String defaultServerRole : defaultServerRoles) {%>
            <tr>
                <td><%=defaultServerRole%>
                </td>
                <td><fmt:message key="server-role.type.default"/></td>
                <td>
                    <a href="#"
                       onclick="deleteServerRole('<%=defaultServerRole %>',
                       '<fmt:message key="server-role.type.default"/>')"
                       class="icon-link"
                       style="background-image:url(../roles-mgt/images/delete.gif);"><fmt:message
                            key="delete"/></a>
                    <%}%>
                </td>
            </tr>
            <%
                }
                if (customServerRoles != null && customServerRoles.length != 0) {
                    for (String customServerRole : customServerRoles) {
            %>
            <tr>
                <td><%=customServerRole%>
                </td>
                <td><fmt:message key="server-role.type.custom"/></td>
                <td>
                    <a href="#"
                       onclick="deleteServerRole('<%=customServerRole%>',
                       '<fmt:message key="server-role.type.custom"/>')"
                       class="icon-link"
                       style="background-image:url(../roles-mgt/images/delete.gif);"><fmt:message
                            key="delete"/></a>
                    <%}%>
                </td>
            </tr>
            <%}%>
            </tbody>
        </table>

        <% } %>

        <div class="icon-link-ouside registryWriteOperation">
            <a href="#" class="icon-link"
               style="background-image:url(../roles-mgt/images/add.gif);"
               onclick="showHideCommon('propertiesAddDiv');if($('propertiesAddDiv').
               style.display!='none')$('_serverRoleName').focus();">
                <fmt:message key="add.new.server-role"/>
            </a>

            <div style="clear:both"></div>
        </div>

        <div class="registryWriteOperation" id="propertiesAddDiv" style="display:none;">
            <form onsubmit="return addServerRole();">
                <table cellpadding="0" cellspacing="0" border="0"
                       class="styledLeft noBorders">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="add.server-role"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table class="normal">
                                <tbody>
                                <tr>
                                    <td id="propertySizer"><fmt:message key="name"/><font
                                            color="red">*</font></td>
                                    <td><input type="text" id="_serverRoleName"/></td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addServerRole();"/>
                            <input
                                    style="margin-left:5px;" type="button"
                                    class="button"
                                    value="<fmt:message key="cancel"/>"
                                    onclick="showHideCommon('propertiesAddDiv');"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</div>


<script type="text/javascript">
    alternateTableRows('roleTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>