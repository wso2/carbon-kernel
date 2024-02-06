<!--
  ~ Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
  ~
  ~  WSO2 LLC. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  -->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.admin.advisory.mgt.stub.dto.AdminAdvisoryBannerDTO"%>
<%@ page import="org.wso2.carbon.admin.advisory.mgt.ui.AdminAdvisoryBannerClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.owasp.encoder.Encode" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />

<%
    String forwardTo = null;
    AdminAdvisoryBannerClient client = null;

    AdminAdvisoryBannerDTO adminAdvisoryBannerConfig = null;
    Boolean enableBanner = null;
    String bannerContent = null;

    String BUNDLE = "org.wso2.carbon.admin.advisory.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        client = new AdminAdvisoryBannerClient(cookie, backendServerURL, configContext);
		adminAdvisoryBannerConfig = client.loadBannerConfig();

    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.loading.admin.advisory.banner.data");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }
%>

<%
    if ( forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
        return;
    }
%>

<script type="text/javascript">
    function setBooleanValueToTextBox(element) {
        document.getElementById(element.value).value = element.checked;
    }
</script>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>

<fmt:bundle basename="org.wso2.carbon.admin.advisory.mgt.ui.i18n.Resources">
	<carbon:breadcrumb label="admin.session.advisory.banner"
		resourceBundle="org.wso2.carbon.admin.advisory.mgt.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

    <div id="middle">
        <h2>
            <fmt:message key="admin.session.advisory.banner.heading" />
        </h2>
        <div id="workArea">
            <form action="admin-advisory-banner-finish-ajaxprocessor.jsp" method="post">
                <div class="sectionSeperator">
                    <fmt:message key="admin.session.advisory.banner.set" />
                </div>
                <div class=”sectionSub”>
                    <table class="carbonFormTable">
                        <%
                        enableBanner = adminAdvisoryBannerConfig.getEnableBanner();
                        bannerContent = adminAdvisoryBannerConfig.getBannerContent();%>
                        <tr>
                            <td style="width: 500px;">
                                <fmt:message key="admin.session.advisory.banner.enable.banner" />
                            </td>
                            <td>
                                <input class="sectionCheckbox" type="checkbox"
                                    onclick="setBooleanValueToTextBox(this)"
                                        <%if (enableBanner) {%> checked="checked" <%}%>
                                        name="enableBanner"
                                        value="true"
                                        />
							
                                <div class="sectionHelp">
                                    <fmt:message key="admin.session.advisory.banner.enable.banner.hint" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="width: 500px;">
                                <fmt:message key="admin.session.advisory.banner.banner.content" />
                            </td>
                            <td>
                                <textarea name="bannerContent" id="bannerContent" class="text-box-big"
                                    style="width: 500px; height: 60px;"><%=Encode.forHtmlContent(bannerContent)%>
                                </textarea>
                                <div class="sectionHelp">
                                    <fmt:message key="admin.session.advisory.banner.banner.content.hint" />
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="buttonRow">
                    <input type="submit" class="button" value="Update"/>
                </div>
            </form>
        </div>
    </div>
</fmt:bundle>
