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
<script type="text/javascript" src="../securityconfig/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="add.new.keystore"
		resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <script type="text/javascript">


        function doValidation() {
            var reason = "";
            reason = validateEmpty("keystoreFile");
            if (reason != "") {
                CARBON.showWarningDialog("<fmt:message key="enter.a.keystore.file"/>");
                return false;
            }

            reason = validateEmpty("ksPassword");
            if (reason != "") {
                CARBON.showWarningDialog("<fmt:message key="enter.keystore.password"/>");
                return false;
            }

        }


        function doCancel() {
            location.href = 'keystore-mgt.jsp?region=region1&item=keystores_menu';
        }
    </script>
    <jsp:include page="../dialog/display_messages.jsp"/>
    <div id="middle">
        <h2><fmt:message key="add.new.keystore"/></h2>

        <div id="workArea">
            <form method="post" name="keysotoreupload" action="add-keystore-step2.jsp"
                  enctype="multipart/form-data" onsubmit="return doValidation();">

                <h3><fmt:message key="step.1.upload.keystore.file"/></h3>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="key.store.file"/></th>
                    </tr>
                    </thead>
<tr>            
<td class="formRow">
<table class="normal">
                    <tr>
                        <td><fmt:message key="keystore.file"/><font color="red">*</font></td>
                        <td>
                            <input name="keystoreFile" id="keystoreFile" size="50" type="file"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="keystore.password"/><font color="red">*</font>
                        </td>
                        <td>
                            <input name="ksPassword" type="password" value=""/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="provider"/></td>
                        <td>
                            <input name="provider" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="keystore.type"/></td>
                        <td>
                            <select name="keystoreType">
                                <option value="jks">JKS</option>
                                <option value="pkcs12">PKCS12</option>
                            </select>
                        </td>
</table>
</td>
</tr>                        
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            	<input class="button" value="<fmt:message key="next"/> &gt;" type="submit"/>
				<input class="button" value="<fmt:message key="cancel"/>" type="button" onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
                <br/>
        </div>

    </div>
</fmt:bundle>