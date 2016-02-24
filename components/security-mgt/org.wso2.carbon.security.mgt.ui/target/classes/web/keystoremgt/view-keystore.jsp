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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData" %>
<%@ page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedCertData" %>
<%@page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedKeyStoreData" %>
<%@page import="org.wso2.carbon.security.ui.SecurityUIConstants" %>
<%@page import="org.wso2.carbon.security.ui.Util" %>
<%@page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="view.keystore"
                   resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
<%
    String filter = request.getParameter(SecurityUIConstants.KEYSTORE_CERT_LIST_FILTER);
    PaginatedCertData paginatedCertData = null;
    CertData[] certData = new CertData[0];
    PaginatedKeyStoreData keyStoreData = (PaginatedKeyStoreData) session.getAttribute(SecurityUIConstants.PAGINATED_KEY_STORE_DATA);
    String keyStore = request.getParameter("keyStore");
    String paginationValue = "keyStore=" + keyStore;
    int startingPage = 0;
    if (keyStoreData != null) {
        startingPage = (Integer) session.getAttribute(SecurityUIConstants.STARTING_CERT_DATA_PAGE);
    }

    int numberOfPages = 0;
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }
    
    if (filter == null || filter.trim().length() == 0) {
        filter = (String) session.getAttribute(SecurityUIConstants.KEYSTORE_CERT_LIST_FILTER);
        if (filter == null || filter.trim().length() == 0) {
            filter = "*";
        }
    }
    filter = filter.trim();
    session.setAttribute(SecurityUIConstants.KEYSTORE_CERT_LIST_FILTER, filter);

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ServletContext servletContext = session.getServletContext();
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);

        if (keyStoreData == null || startingPage + SecurityUIConstants.CACHING_PAGE_SIZE < pageNumberInt || pageNumberInt < startingPage) {


            keyStoreData = client.getPaginatedKeystoreInfo(keyStore, pageNumberInt);
            session.setAttribute(SecurityUIConstants.PAGINATED_KEY_STORE_DATA, keyStoreData);
            session.setAttribute(SecurityUIConstants.STARTING_CERT_DATA_PAGE, pageNumberInt);
            startingPage = pageNumberInt;

        }
        paginatedCertData = keyStoreData.getPaginatedCertData();

        if (paginatedCertData != null) {
            CertData[] filteredCerts = Util.doFilter(filter, paginatedCertData.getCertDataSet());
            certData = Util.doPaging(pageNumberInt - startingPage, filteredCerts);
            numberOfPages = (int) Math.ceil((double) filteredCerts.length / SecurityUIConstants.DEFAULT_ITEMS_PER_PAGE);
        }


    } catch (Exception e) {

%>
<strong>An error occurred!</strong>

<p>Error message is : <%=Encode.forHtmlContent(e.getMessage())%>
</p>
<%

    }

%>

<script type="text/javascript">

    function deleteCert(alias, keystore) {

        function doDelete() {
            var certAlias = alias;
            var keystoreName = keystore;
            location.href = 'delete-cert.jsp?alias=' + certAlias + '&keystore=' + keystoreName;
        }

        CARBON.showConfirmationDialog('<fmt:message key="do.you.want.to.delete.the.certificate"/> ' + alias + '?', doDelete, null);
    }


</script>

<div id="middle">
    <h2><fmt:message key="view.keystore"/></h2>

    <div id="workArea">
        <h3><fmt:message key="certificate.of.the.private.key"/></h3>
        <table class="styledLeft">
            <thead>
	            <tr>
	                <th><fmt:message key="alias"/></th>
	                <th><fmt:message key="issuerdn"/></th>
	                <th><fmt:message key="notafter"/></th>
	                <th><fmt:message key="notbefore"/></th>
	                <th><fmt:message key="serialnumber"/></th>
	                <th><fmt:message key="subjectdn"/></th>
	                <th colspan="2"><fmt:message key="version"/></th>
	            </tr>
            </thead>
            <tbody>
	            <%
	                if (keyStoreData != null && keyStoreData.getKey() != null) {
	                    CertData cdata = keyStoreData.getKey();
	            %>
	            <tr>
	                <td><%=Encode.forHtmlContent(cdata.getAlias())%>
	                </td>
	                <td><%=Encode.forHtmlContent(cdata.getIssuerDN())%>
	                </td>
	                <td><%=Encode.forHtmlContent(cdata.getNotAfter())%>
	                </td>
	                <td><%=Encode.forHtmlContent(cdata.getNotBefore())%>
	                </td>
	                <td><%=cdata.getSerialNumber()%>
	                </td>
	                <td><%=Encode.forHtmlContent(cdata.getSubjectDN())%>
	                </td>
	                <td colspan="2"><%=cdata.getVersion()%>
	                </td>
	            </tr>
	            <%
	                }
	            %>
	            <tr>
	                <td class="buttonRow" colspan="8">
	                    <form>
	                        <input value="<fmt:message key="import.cert"/>" type="button"
	                               class="button"
	                               onclick="location.href
                                           ='import-cert.jsp?keyStore=<%=Encode.forUriComponent(keyStore)%>'"/>
	                        <input value="<fmt:message key="finish"/>" type="button" class="button"
	                               onclick="location.href ='keystore-mgt.jsp?region=region1&item=keystores_menu'"/>
	                    </form>
	                </td>
	            </tr>
            </tbody>
        </table>
        <p>&nbsp;</p>
        <h3><fmt:message key="available.certificates"/></h3>
        <form name="filterForm" method="post" action="view-keystore.jsp">
            <table class="styledLeft noBorders">
                <thead>
                 <tr>
                     <th colspan="2"><fmt:message key="filter.keystore.cert.search"/></th>
                 </tr>
                </thead>
                <tbody>
                 <tr>
                     <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
                             key="filter.keystore.cert.label"/></td>
                     <td>
                         <input type="text" name="<%=SecurityUIConstants.KEYSTORE_CERT_LIST_FILTER%>"
                                value="<%=Encode.forHtmlAttribute(filter)%>"/>
 
                         <input class="button" type="submit"
                                value="<fmt:message key="filter.keystore.cert.search"/>"/>
                     </td>
                 </tr>
                </tbody>
            </table>
        </form>
        <p>&nbsp;</p>
        <table class="styledLeft">
	        <thead>
		        <tr>
		            <th><fmt:message key="alias"/></th>
		            <th><fmt:message key="issuerdn"/></th>
		            <th><fmt:message key="notafter"/></th>
		            <th><fmt:message key="notbefore"/></th>
		            <th><fmt:message key="serialnumber"/></th>
		            <th><fmt:message key="subjectdn"/></th>
		            <th><fmt:message key="version"/></th>
		            <th><fmt:message key="actions"/></th>
		        </tr>
	        </thead>
			<tbody>
				<%
				    if (certData != null && certData.length > 0) {
				        for (CertData cert : certData) {
				            if (cert != null) {
				%>
				<tr>
				    <td><%=Encode.forHtmlContent(cert.getAlias())%>
				    </td>
				    <td><%=Encode.forHtmlContent(cert.getIssuerDN())%>
				    </td>
				    <td><%=Encode.forHtmlContent(cert.getNotAfter())%>
				    </td>
				    <td><%=Encode.forHtmlContent(cert.getNotBefore())%>
				    </td>
				    <td><%=cert.getSerialNumber()%>
				    </td>
				    <td><%=Encode.forHtmlContent(cert.getSubjectDN())%>
				    </td>
				    <td><%=cert.getVersion()%>
				    </td>
				    <td><a href="#"
				           onclick="deleteCert('<%=Encode.forJavaScriptAttribute(cert.getAlias())%>',
				                   '<%=Encode.forJavaScriptAttribute(keyStoreData.getKeyStoreName())%>')"
				           class="icon-link"
				           style="background-image:url(images/delete.gif);">Delete</a>
				    </td>
				</tr>
				<%
				            }
				        }
				    }
				%>
			</tbody>
        </table>
        <carbon:paginator pageNumber="<%=pageNumberInt%>"
                          numberOfPages="<%=numberOfPages%>"
                          page="view-keystore.jsp"
                          pageNumberParameterName="pageNumber"
                          parameters="<%=paginationValue%>"
                          resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"/>
    </div>
</div>
</fmt:bundle>