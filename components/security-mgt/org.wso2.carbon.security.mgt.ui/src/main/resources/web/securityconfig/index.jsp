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
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page
        import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioData" %>
<%@page import="org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioDataWrapper" %>
<%@page import="org.wso2.carbon.security.ui.client.SecurityAdminClient" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>


<%@page import="java.util.ResourceBundle"%>
<%@ page import="org.owasp.encoder.Encode" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../resources/js/registry-browser.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
	String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String[] options = new String[]{"Yes", "No"};
    String[] optionsValues = new String[]{resourceBundle.getString("yes"), resourceBundle.getString("no")};
    String backLink = (String)session.getAttribute("backLink");
%>
<%!
    String info = null;
    SecurityScenarioData[] scenarios = null;
%>
<%!
    String showOption = null;
    String displayStyle = null;
    int[] optionsOrder = null;

%>

<!--%!  This method causes issues when we add a new scenario and when the scenario id is not a sequentially incremented
       one
    private void checkScenarioID(int scenarioIdDisplay, SecurityScenarioData scenario) {
        if(!("scenario" + scenarioIdDisplay).equals(scenario.getScenarioId())){
            throw new RuntimeException("ScenarioID & generated ID for scenario " +
                                       scenario.getSummary() + " do not match. Scenario ID: " +
                                       scenario.getScenarioId() +", generated ID:" + scenarioIdDisplay);
        }
    }
+% -->
<%
	SecurityScenarioData currentScenario;
    String serviceName;
    String policyPath = "";
    serviceName = request.getParameter("serviceName");
    if (serviceName != null) {
        serviceName = serviceName.trim();
        if (serviceName.length() > 0) {
            session.setAttribute("serviceName", serviceName);
        }
    } else {
        serviceName = (String) session.getAttribute("serviceName");
    }

    if (serviceName == null) {
        String message = resourceBundle.getString("cannot.proceed.please.select.a.service.to.enable.security");
		CarbonUIMessage uiMsg = new CarbonUIMessage(message, CarbonUIMessage.ERROR, null);
		session.setAttribute(CarbonUIMessage.ID, uiMsg);
	%>
	<jsp:include page="../admin/error.jsp"/>
	<%
		return;
    }
    
    if (backLink==null){
    	backLink = "../service-mgt/service_info.jsp?serviceName="+ Encode.forUriComponent(serviceName);
    }
    
	try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        SecurityAdminClient client = new SecurityAdminClient(cookie, backendServerURL, configContext);
        SecurityScenarioDataWrapper scenarioDataWrapper = client.getScenarios(serviceName);
        scenarios = scenarioDataWrapper.getScenarios();
        currentScenario = scenarioDataWrapper.getCurrentScenario();

        if (currentScenario == null) {
            info = MessageFormat.format(resourceBundle.getString("service.not.secured"),
                                        serviceName);
            optionsOrder = new int[]{1, 0};
            displayStyle = "display: none;";
        } else {
            if (currentScenario.getPolicyRegistryPath() != null) {
                policyPath = currentScenario.getPolicyRegistryPath();
                info = MessageFormat.format(resourceBundle.getString("service.secured.using.policy.from.registry"),
                        serviceName);
            } else {
                info = MessageFormat.format(resourceBundle.getString("service.secured.using"),
                        serviceName, currentScenario.getSummary());
            }
            optionsOrder = new int[]{0, 1};
            displayStyle = "display: inline;";
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="security.for.the.service"
                   resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
                   <script type="text/javascript">


    function doValidation() {
        var checkedOption = null;
        checkedOption = isAtleastOneChecked("scenarioId");
        
        var isPolicyPathSet = true;
        if (document.getElementById("secPolicyRegText").value == "") {
            isPolicyPathSet = false;
        }
        if (checkedOption == null) {
            CARBON.showWarningDialog('<fmt:message key="please.select.a.security.scenario"/>');
            return false;
        }
        if (checkedOption == "policyFromRegistry" && !isPolicyPathSet) {
            CARBON.showWarningDialog('<fmt:message key="please.set.registry.path"/>');
            return false;
        }
        return true;
    }

    function disablePolicyPath() {
        document.getElementById("secPolicyRegText").disabled = true;
    }

    function enablePolicyPath() {
        document.getElementById("secPolicyRegText").disabled = false;
    }
    
    function setUnsetBackButtion(){
          if (document.getElementById("securityConfigAction").value == "Yes") {
   	           document.getElementById('backButton').style.display = 'none';
          } else if (document.getElementById("securityConfigAction").value == "No") {
   	           document.getElementById('backButton').style.display = 'block';
         }       
   }

    function showOptions() {
        var temp = document.getElementById("securityConfiguration");
        if (document.getElementById("securityConfigAction").value == "Yes") {
        	setUnsetBackButtion();
            temp.style.display = "inline";
        } else if (document.getElementById("securityConfigAction").value == "No") {
            CARBON.showConfirmationDialog("<fmt:message key="disable.security.confirm"/>",
                    function() {
                        document.deleteSecurity.submit();
                    },
                    function() {
                        document.getElementById("securityConfigAction").value = "Yes";
                        temp.style.display = "inline";
                    });
            setUnsetBackButtion();
        }
    }


</script>
<div id="middle">
    <h2><fmt:message key="security.for.the.service"/></h2>

    <div id="workArea">
        <p><%=Encode.forHtmlContent(info)%></p>

        <p>&nbsp;</p>

        <table>
            <tr>
                <td width="">
                    <label for="securityConfigAction">
                        <fmt:message key="enable.security"/>?&nbsp;&nbsp;
                    </label>
                </td>
                <td>
                    <select onchange="showOptions();" id="securityConfigAction" name="enable">
                        <%
                            for (int optionOder : optionsOrder) {
                        %>
                        <option value="<%=Encode.forHtmlAttribute(options[optionOder])%>">
                            <%=Encode.forHtmlContent(optionsValues[optionOder])%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </td>
            </tr>
        </table>
        <p>&nbsp;</p>
        
        <div id="backButton">
           <table class="styledLeft">
               <tr>
                   <td class="buttonRow">
                               <input type="button" class="button" value="< <fmt:message key="back"/>" onclick="location.href = '<%=backLink%>'">
                   </td>
               </tr>
           </table>
           <p>&nbsp;</p>
       </div>

        <div id="securityConfiguration" style="<%=displayStyle%>">
            <form id="secConfigForm" name="securityForm" method="post" action="ut-ks-advance.jsp?serviceName=<%=Encode.forUriComponent(serviceName)%>"
                  onsubmit="return doValidation();">    
                  
                  <script type="text/javascript">
                       setUnsetBackButtion();
                  </script>  

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="5"><fmt:message key="basic.scenarios"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        int scenarioIdDisplay = 1;
                        for (SecurityScenarioData scenario : scenarios) {
                            if (scenario != null) {
                                String id = scenario.getScenarioId();
                                if (!scenario.getType().equals("basic")){
                                continue;
                            }
                            //checkScenarioID(scenarioIdDisplay, scenario);
                    %>
                    <tr>
                        <td><%= scenarioIdDisplay++ %>.</td>
                        <td>
                            <%
                                int helpId = scenarioIdDisplay - 1;
                                if (currentScenario != null && "".equals(policyPath) &&
                                    currentScenario.getScenarioId().equals(id)) {
                            %><input type="radio" name="scenarioId" id="option_<%=Encode.forHtmlAttribute(id)%>"
                                     value="<%=Encode.forHtmlAttribute(id)%>"
                                     onclick="disablePolicyPath();" checked="checked"/><%
                        } else {
                        %><input type="radio" name="scenarioId"  id="option_<%=Encode.forHtmlAttribute(id)%>"
                                 value="<%=Encode.forHtmlAttribute(id)%>"
                                 onclick="disablePolicyPath();"/><%
                            }
                        %>
                        </td>
                        <td>
                            <label for="option_<%=Encode.forHtmlAttribute(id)%>">
                                <%= Encode.forHtmlContent(scenario.getSummary()) %></label>
                        </td>
                        <td>
                            <a onmouseover="showTooltip(this,'View scenario <%=helpId%> in detail')" class='icon-link'
                               target="_blank"
                               href="view-scenario-detail_ajaxprocessor.jsp?scenarioId=<%=helpId%>&scenarioSummary
                               =<%=Encode.forUriComponent(scenario.getSummary())%>"
                               style='background-image:url(images/view.png);float:none;'></a>
                        </td>
                        <td><%=Encode.forHtmlContent(scenario.getDescription())%>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    <tr><td colspan="5">&nbsp;</td></tr>
                    <tr>
                        <td colspan="5" class="sub-header"><fmt:message
                                key="advanced.scenarios"/></td>
                    </tr>
                    <%
                       for (SecurityScenarioData scenario: scenarios) {
                           if (scenario != null) {
                               String id = scenario.getScenarioId();
                               if (!scenario.getType().equals("advanced")) {
                                   continue;
                            }
                           //checkScenarioID(scenarioIdDisplay, scenario);
                    %>
                    <tr>
                        <td><%= scenarioIdDisplay++ %>.</td>
                        <td>
                            <%
                                int helpId = scenarioIdDisplay - 1;
                                if (currentScenario != null && "".equals(policyPath) &&
                                    currentScenario.getScenarioId().equals(id)) {
                            %><input type="radio" name="scenarioId"  id="option_<%=Encode.forHtmlAttribute(id)%>"
                                     value="<%=Encode.forHtmlAttribute(id)%>"
                                     onclick="disablePolicyPath();" checked="checked"/><%
                        } else {
                        %><input type="radio" name="scenarioId" id="option_<%=Encode.forHtmlAttribute(id)%>"
                                 value="<%=Encode.forHtmlAttribute(id)%>"
                                 onclick="disablePolicyPath();"/><%
                            }
                        %>
                        </td>
                        <td><label
                                for="option_<%=Encode.forHtmlAttribute(id)%>">
                            <%=Encode.forHtmlContent(scenario.getSummary())%></label>
                        </td>
                        <td>
                            <a onmouseover="showTooltip(this,'View scenario <%=helpId%> in detail')" class='icon-link'
                               target="_blank"
                               href="view-scenario-detail_ajaxprocessor.jsp?scenarioId=<%=helpId%>&scenarioSummary=<%=Encode.forUriComponent(scenario.getSummary())%>"
                               style='background-image:url(images/view.png);float:none;'></a>
                        </td>
                        <td><%=Encode.forHtmlContent(scenario.getDescription())%>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    <tr><td colspan="5">&nbsp;</td></tr>
                    <tr>
                        <td colspan="5" class="sub-header"><fmt:message
                                key="sec.policy.from.registry"/></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <% if (!"".equals(policyPath)) { %>
                                <input type="radio" name="scenarioId" id="policyFromRegistry"
                                       value="policyFromRegistry" onclick="enablePolicyPath();" checked="checked"/>
                            <% } else { %>
                                <input type="radio" name="scenarioId" id="policyFromRegistry"
                                       value="policyFromRegistry" onclick="enablePolicyPath();"/>
                            <% } %>
                        </td>
                        <td colspan="3">
                            <table class="noBorders" style="border:none">
                                <td><label for="policyFromRegistry"><fmt:message key="sec.policy.path"/></label></td>
                                <td>
                                    <table cellspacing="0">
                                        <tr>
                                            <td class="nopadding" style="border:none !important">
                                                <% if (!"".equals(policyPath)) { %>
                                                    <input type="text" name="secPolicyRegText" id="secPolicyRegText"
                                                           value="<%= Encode.forHtmlAttribute(policyPath)%>" size="60"
                                                           readonly="readonly"/>
                                                <% } else { %>
                                                    <input type="text" name="secPolicyRegText" id="secPolicyRegText"
                                                           value="<%= Encode.forHtmlAttribute(policyPath)%>" size="60"
                                                           readonly="readonly" disabled="disabled"/>
                                                <% } %>
                                            </td>
                                            <td class="nopadding" style="border:none !important">
                                                <label for="policyFromRegistry">
                                                    <a class="registry-picker-icon-link"
                                                       style="padding-left:30px;cursor:pointer;color:#386698"
                                                       onclick="showRegistryBrowser('secPolicyRegText','/_system/config');"
                                                            ><fmt:message key="conf.registry"/>
                                                    </a>
                                                </label>
                                            </td>
                                            <td class="nopadding" style="border:none !important">
                                                <label for="policyFromRegistry">
                                                    <a class="registry-picker-icon-link"
                                                       style="padding-left:30px;cursor:pointer;color:#386698"
                                                       onclick="showRegistryBrowser('secPolicyRegText','/_system/governance');">
                                                        <fmt:message key="gov.registry"/>
                                                    </a>
                                                </label>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td class="buttonRow" colspan="5">
                            <input type="button" class="button" value="< <fmt:message key="back"/>"
                                   onclick="location.href = '<%= Encode.forJavaScriptBlock(backLink)%>'">
                            <input class="button" type="submit"
                                   value="<fmt:message key="next"/> >"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
        <div id="divDeleteSecurity" style="display: none">
            <form name="deleteSecurity" action="remove-security.jsp?serviceName=<%=
            Encode.forUriComponent(serviceName)%>">
                <input type="submit" value="<fmt:message key="delete"/>"/>
            </form>
        </div>

    </div>
</div>
</fmt:bundle>