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
<%@ page import="org.wso2.carbon.feature.mgt.stub.prov.data.LicenseInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    //TODO null check
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProvisioningAdminClient provAdminClient;

    LicenseInfo[] licenses;

    try {
        provAdminClient = new ProvisioningAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        licenses = provAdminClient.getLicensingInformation();
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
            <th><H4><strong><fmt:message key="licence.agreements"/></strong></H4></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>
                <table class="normal" cellspacing="1" width="100%" id="_table_add_repository_link"
                   style="margin-left: 0px;">
                    <tbody>
                        <tr><td><font color="#707277"><i><fmt:message key="licence.agreements.description"/></i></font></td></tr>
<%
    if(licenses != null) {
        for(LicenseInfo license: licenses){
%>
                    <tr>
                        <td>
                            <textarea name="sdfdsf" cols="80" rows="20"
                                      width="100%" readonly="true" class="myformat">
<%=license.getBody()%></textarea>
                        </td>
                    </tr>
<%
        }
%>
                    <tr>
                        <td>
                            <div>
                            <br/>
                            <input id="_radio_af_accept_licenses" value="accept" name="accept-decline-af"
                                   class="toolsClass" type="radio"
                                   onclick="doAcceptLicenses()"/>&nbsp;&nbsp;&nbsp;<fmt:message key="licence.accept.text"/>
                            <br/>
                            <input id="_radio_af_decline_licenses" value="decline" name="accept-decline-af"
                                   class="toolsClass" checked="checked" type="radio"
                                    onclick="doDeclineLicenses()"/>&nbsp;&nbsp;&nbsp;<fmt:message key="licence.decline.text"/>
                            </div>
                        </td>
                    </tr>
<%
    } else {
        //TODO if there are no license spefied
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
                       onclick="doBack('RL-RF')"
                       id="_btn_back_af_review_licenses"/>
                <input value="<fmt:message key="next.button"/>" tabindex="11" type="button"
                       class="button" disabled="true"
                       onclick="doFinish('IF')"
                       id="_btn_finish_af_review_licenses">
                <input value="<fmt:message key="cancel.button"/>" tabindex="11" type="button"
                       class="button"
                       onclick="doBack('RL-AF')"
                       id="_btn_cancel_af_review_licenses"/>
                <input id="_hidden_RL_actionType" value="<%=provAdminClient.getInstallActionType()%>" type="hidden" />
            </td>
        </tr>
    </tbody>
</table>
</fmt:bundle>