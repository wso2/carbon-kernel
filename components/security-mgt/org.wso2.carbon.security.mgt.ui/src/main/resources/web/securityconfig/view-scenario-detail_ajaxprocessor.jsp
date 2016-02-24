<%@ page import="org.owasp.encoder.Encode" %>
<!--
~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%
    String scenarioIdNumber = "";
    String scenarioId = request.getParameter("scenarioId");
    String scenarioSummary = request.getParameter("scenarioSummary");
    if(scenarioId != null && !scenarioId.equals("")){
        scenarioIdNumber = "scenario" + scenarioId;
    }
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">

<div id="middle">
    <h2><fmt:message key="scenario"/> <%=Encode.forHtml(scenarioId)%> : <%=Encode.forHtml(scenarioSummary)%></h2>
    <img src="images/<%=scenarioIdNumber%>.png" alt="security scenario description"/>
</div>
</fmt:bundle>