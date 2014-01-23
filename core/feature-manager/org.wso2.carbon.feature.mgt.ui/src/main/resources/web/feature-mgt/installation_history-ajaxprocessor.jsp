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
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.ProfileHistory" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<script src="js/tableTree.js" type="text/javascript"></script>

<script type="text/javascript" src="../admin/js/main.js"></script>


<%
    ProvisioningAdminClient provAdminClient;
    ProfileHistory[] profHistoryConf = null;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    try {
        provAdminClient = new ProvisioningAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        profHistoryConf = provAdminClient.getProfileHistory();
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
<H4><strong><fmt:message key="installation.history"/></strong></H4>
<p><font color="#707277"><i><fmt:message key="installation.history.description"/></i></font></p>
<br/>
<div id="_div_previous_configurations_list">
        <table class="styledLeft" cellspacing="1" width="100%" id="_table_previous_configurations_list"
               style="margin-left: 0px;">
            <thead>
            <tr>
                <th><fmt:message key="previous.configurations"/></th>
            </tr>
            </thead>
            <tbody>

<%
    if(profHistoryConf == null || profHistoryConf.length == 0){
%>
        <tr>
            <td colspan="0"><fmt:message key="no.previous.configurations"/></td>
        </tr>
<%
    } else {
        for(int index = profHistoryConf.length-1 ; index >= 0; index--){
            String summary;
            ProfileHistory profileHistory = profHistoryConf[index];
            long timestamp = profileHistory.getTimestamp();
            if(index == profHistoryConf.length -1){
                summary = "Current Configuration";
 %>
        <tr>
            <td><%=summary%></td>
        </tr>
<%
            } else {
                summary = profileHistory.getSummary();
%>
        <tr>
            <td><a href="#"  onclick="getRevertPlan('<%=timestamp%>');"><%=summary%></a></td>
        </tr>
<%
            }
        }
    }
%>
            </tbody>
        </table>
</div>
<script type="text/javascript">
    alternateTableRows('_table_previous_configurations_list', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>