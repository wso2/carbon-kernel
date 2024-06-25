<%--
 Copyright (c) 2005-2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.

 WSO2 LLC. licenses this file to you under the Apache License,
 Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 --%>

<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.utils.CarbonUtils"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.admin.advisory.mgt.stub.dto.AdminAdvisoryBannerDTO"%>
<%@ page import="org.wso2.carbon.admin.advisory.mgt.ui.AdminAdvisoryBannerClient"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%!
    private Log log = LogFactory.getLog(this.getClass());
%>
<%!
    private AdminAdvisoryBannerDTO getAdminBannerConfig(String backendServerURL, ConfigurationContext configContext) {

        AdminAdvisoryBannerDTO adminAdvisoryBannerConfig = new AdminAdvisoryBannerDTO();
        try {
            AdminAdvisoryBannerClient client = new AdminAdvisoryBannerClient(backendServerURL, configContext);
            adminAdvisoryBannerConfig = client.loadBannerConfig();
        } catch (Exception e) {
            log.error("Error in displaying admin advisory banner", e);
        }
        return adminAdvisoryBannerConfig;
    }
%>
<%
String userForumURL =
        (String) config.getServletContext().getAttribute(CarbonConstants.PRODUCT_XML_WSO2CARBON +
                                                         CarbonConstants.PRODUCT_XML_USERFORUM);
String userGuideURL =
        (String) config.getServletContext().getAttribute(CarbonConstants.PRODUCT_XML_WSO2CARBON +
                                                         CarbonConstants.PRODUCT_XML_USERGUIDE);
String mailinglistURL =
        (String) config.getServletContext().getAttribute(CarbonConstants.PRODUCT_XML_WSO2CARBON +
                                                         CarbonConstants.PRODUCT_XML_MAILINGLIST);
String issuetrackerURL =
        (String) config.getServletContext().getAttribute(CarbonConstants.PRODUCT_XML_WSO2CARBON +
                                                         CarbonConstants.PRODUCT_XML_ISSUETRACKER);
if(userForumURL == null){
	userForumURL = "#";
}
if(userGuideURL == null){
	userGuideURL = "#";
}
if(mailinglistURL == null){
	mailinglistURL = "#";
}
if(issuetrackerURL == null){
	issuetrackerURL = "#";
}

if (CharacterEncoder.getSafeText(request.getParameter("skipLoginPage"))!=null){
	response.sendRedirect("../admin/login_action.jsp");
	return;
}
String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
AdminAdvisoryBannerDTO adminConfig = getAdminBannerConfig(backendServerURL, configContext);
Boolean enableBanner = adminConfig.getEnableBanner();
String bannerContent = adminConfig.getBannerContent();
%>

<fmt:bundle basename="org.wso2.carbon.i18n.Resources">

     <script type="text/javascript">

        function doValidation() {
            var reason = "";

            var userNameEmpty = isEmptyCheckWithoutTrim("username");
            var passwordEmpty = isEmptyCheckWithoutTrim("password");

            if (userNameEmpty || passwordEmpty) {
                CARBON.showWarningDialog('<fmt:message key="empty.credentials"/>');
                document.getElementById('txtUserName').focus();
                return false;
            }

            return true;
        }

    </script>

    <%
        String loginStatus = CharacterEncoder.getSafeText(request.getParameter("loginStatus"));
        String errorCode = CharacterEncoder.getSafeText(request.getParameter("errorCode"));
        String BUNDLE = "org.wso2.carbon.i18n.Resources";

        if (loginStatus != null && "false".equalsIgnoreCase(loginStatus)) {
            if (StringUtils.isBlank(errorCode) ||
                    errorCode.equalsIgnoreCase(CarbonUIUtil.geti18nString(errorCode, BUNDLE, request.getLocale()))) {
                errorCode = "login.fail.message";
            }
    %>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog('<fmt:message key="<%=Encode.forJavaScript(errorCode)%>"/>');
        });
    </script>
    <%
        }

        if (loginStatus != null && "failed".equalsIgnoreCase(loginStatus)) {
            if (StringUtils.isBlank(errorCode) ||
                    errorCode.equalsIgnoreCase(CarbonUIUtil.geti18nString(errorCode, BUNDLE, request.getLocale()))) {
                errorCode = "login.fail.message1";
            }
     %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog('<fmt:message key="<%=Encode.forJavaScript(errorCode)%>"/>');
        });
    </script>
    <%
        }
        String backendURL = CharacterEncoder.getSafeText(CarbonUIUtil.getServerURL(config.getServletContext(), session));
    %>
     <script type="text/javascript">
    	function getSafeText(text){
    		text = text.replace(/</g,'&lt;');
    		return text.replace(/>/g,'&gt');
    	}
    
        function checkInputs(){
        	var loginForm = document.getElementById('loginForm');
        	var backendUrl = document.getElementById("txtbackendURL");
        	var username = document.getElementById("txtUserName");
        	
        	backendUrl.value = getSafeText(backendUrl.value);
        	username.value = getSafeText(username.value);
        	loginForm.submit();
        }
    </script>
    <div id="middle">
        <table cellspacing="0" width="100%">
            <tr>
                <td>
                    <div id="features">
                        <table cellspacing="0">
                            <tr class="feature feature-top">
                                <td>
                                    <a target="_blank" href="<%=userGuideURL %>" rel="noopener noreferrer"><img
                                            src="../admin/images/user-guide.gif"/></a>
                                </td>
                                <td>
                                    <h3><a target="_blank" href="<%=userGuideURL %>"
                                           rel="noopener noreferrer"><fmt:message key="user.guide"/></a>
                                    </h3>

                                    <p><fmt:message key="user.guide.text"/></p>
                                </td>
                            </tr>
                            <tr class="feature">
                                <td>
                                    <a target="_blank" href="<%=userForumURL %>" rel="noopener noreferrer"><img
                                            src="../admin/images/forum.gif"/></a>
                                </td>
                                <td>
                                    <h3><a target="_blank" href="<%=userForumURL %>"
                                           rel="noopener noreferrer"><fmt:message
                                            key="forum"/></a>
                                    </h3>

                                    <p><fmt:message key="forum.text"/></p>
                                </td>
                            </tr>
                            <tr class="feature">
                                <td>
                                    <a target="_blank"
                                       href="<%=issuetrackerURL %>" rel="noopener noreferrer"><img
                                            src="../admin/images/issue-tracker.gif"/></a>
                                </td>
                                <td>
                                    <h3><a target="_blank"
                                           href="<%=issuetrackerURL %>" rel="noopener noreferrer">
                                        <fmt:message key="issue.tracker"/></a></h3>

                                    <p><fmt:message key="issue.tracker.text"/></p>

                                </td>
                            </tr>
                            <tr class="feature">
                                <td>
                                    <a target="_blank" href="<%=mailinglistURL %>" rel="noopener noreferrer"><img
                                            src="../admin/images/mailing-list.gif"/></a>
                                </td>
                                <td>
                                    <h3><a target="_blank" href="<%=mailinglistURL %>" rel="noopener noreferrer">
                                        <fmt:message key="mailing.list"/></a></h3>

                                    <p><fmt:message key="mailing.list.text"/></p>
                                </td>
                            </tr>
                        </table>
                    </div>
                </td>
                <td width="20%">
                    <div id="loginbox">
                        <h2><fmt:message key="sign.in"/></h2>

                        <form action='../admin/login_action.jsp' method="POST" onsubmit="return doValidation();" target="_self" onsubmit="checkInputs()">
                            <table>
                                <%if (enableBanner) { %>
                                <tr>
                                    <td colspan="2">
                                        <div style='background-color: #fff5e8; text-align: justify; padding: 10px'>
                                            <%=Encode.forHtmlAttribute(bannerContent)%>
                                        </div>
                                    </td>
                                </tr>
                                <% } %>
                                 <%if(!CarbonUtils.isRunningOnLocalTransportMode()) { %>
                                <tr>
                                    <td>
                                        <nobr><label for="txtUserName"><fmt:message
                                                key="backendURL"/></label></nobr>
                                    </td>
                                    <td>
                                        <input type="text" id="txtbackendURL" name="backendURL"
                                               class="user" tabindex="1" value="<%=backendURL%>"  autocomplete="off" />
                                    </td>
                                </tr>
                                <% } %>
                                <tr>
                                    <td>
                                        <label for="txtUserName"><fmt:message
                                                key="username"/></label>
                                    </td>
                                    <td>
                                        <input type="text" id="txtUserName" name="username"
                                               class="user" tabindex="1"  autocomplete="off" />
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <label for="txtPassword"><fmt:message
                                                key="password"/></label>
                                    </td>
                                    <td>
                                        <input type="password" id="txtPassword" name="password"
                                               class="password" tabindex="2"  autocomplete="off"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        
                                    </td>
                                    <td>
                                    	<input type="checkbox" name="rememberMe" 
                                        				value="rememberMe" tabindex="3"/>
                                        <label for="txtRememberMe"><fmt:message
                                                key="rememberMe"/></label>
                                    </td>
                                </tr>
                                <tr>
                                    <td>&nbsp;</td>
                                    <td>
                                        <input type="submit" value="<fmt:message key="sign.in"/>"
                                               class="button" tabindex="3"/>
                                    </td>
                                </tr>
                            </table>
                        </form>
                        <br/>
			            <a target="_blank" href="../docs/signin_userguide.html" tabindex="4">
                            <fmt:message key="sign.in.help"/>
                        </a>
                    </div>
                </td>
            </tr>
        </table>
    </div>
    <script type="text/javascript">
        function init(loginStatus) {
            // intialize the code and call to the back end
            /*wso2.wsf.Util.initURLs();*/
            /*Initialize the XSLT cache*/
            /*wso2.wsf.XSLTHelper.init();*/

            if (loginStatus == 'true') {
            } else if (loginStatus == 'null') {
            } else if (loginStatus == 'false') {
                wso2.wsf.Util.alertWarning("Login failed. Please recheck the user name and password and try again")
            } 
        }
        document.getElementById('txtUserName').focus();
    </script>

</fmt:bundle>
