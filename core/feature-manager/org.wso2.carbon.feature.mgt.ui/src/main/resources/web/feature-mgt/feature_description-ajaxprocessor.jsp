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
<%@page import="org.wso2.carbon.feature.mgt.ui.ProvisioningAdminClient"%>
<%@ page import="org.wso2.carbon.feature.mgt.ui.FeatureWrapper" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.RepositoryAdminServiceClient" %>
<%@ page import="org.wso2.carbon.feature.mgt.ui.util.Utils" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<%

    String httpMethod = request.getMethod().toLowerCase();

    if (!"post".equals(httpMethod)) {
        response.sendError(405);
        return;
    }
    
    FeatureWrapper[] featureWrappers = null;
    String featureId = CharacterEncoder.getSafeText(request.getParameter("featureId"));
    String isInstalledFeature = CharacterEncoder.getSafeText(request.getParameter("isInstalledFeature"));
    if((featureId != null) && ("true".equals(isInstalledFeature))){
    	featureWrappers = (FeatureWrapper[])request.getSession(false).getAttribute(
    			ProvisioningAdminClient.INSTALLED_FEATURES);
    	if(featureWrappers != null){
    		featureWrappers = Utils.getUniqueFeatureList(featureWrappers);
    		String featureDescription = Utils.getDescriptionOfFeature(featureWrappers, featureId.replace('-','.'));
    		%>
            <p><%=featureDescription%></p>
            <%
    	}else{
    		%>
            <p> no description available</p>
            <%
    	}
    }else if((featureId != null) && ("false".equals(isInstalledFeature))){
    	featureWrappers = (FeatureWrapper[]) request.getSession(false).getAttribute(
    			RepositoryAdminServiceClient.AVAILABLE_FEATURES);
    	if(featureWrappers != null){
    		featureWrappers = Utils.getUniqueFeatureList(featureWrappers);
    		String featureDescription = Utils.getDescriptionOfFeature(featureWrappers, featureId.replace('-','.'));
    		%>
            <p><%=featureDescription%></p>
            <%
    	}else{
    		%>
            <p> no description available</p>
            <%
    	}
    }else 
    {
    	%>
        <p> Error while fetching description</p>
        <%
    }
%>
