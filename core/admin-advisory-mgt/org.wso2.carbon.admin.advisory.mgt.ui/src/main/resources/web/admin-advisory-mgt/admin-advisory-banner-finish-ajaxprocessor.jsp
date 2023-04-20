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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.admin.advisory.mgt.stub.dto.AdminAdvisoryBannerDTO"%>
<%@ page import="org.wso2.carbon.admin.advisory.mgt.ui.AdminAdvisoryBannerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String enableBanner = request.getParameter("enableBanner");
    String bannerContent = request.getParameter("bannerContent").trim();

    AdminAdvisoryBannerDTO adminAdvisoryBannerConfig = new AdminAdvisoryBannerDTO();

    adminAdvisoryBannerConfig.setEnableBanner(Boolean.parseBoolean(enableBanner));
    if (bannerContent != null && bannerContent.length() > 0) {
        adminAdvisoryBannerConfig.setBannerContent(bannerContent);
    }
	
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        AdminAdvisoryBannerClient configClient =
                            new AdminAdvisoryBannerClient(cookie, backendServerURL, configContext);

        // Save the new configuration.
        configClient.saveBannerConfig(adminAdvisoryBannerConfig);

%>
    <script type="text/javascript">
        location.href = "admin-advisory-banner.jsp";
    </script>
<%
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
%>
    <script type="text/javascript">
        location.href = "admin-advisory-banner.jsp";
    </script>
<%
        return;
    }
%>
