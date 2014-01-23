<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
    <xsl:template match="/interface">
    <xsl:variable name="svc_name"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:variable name="method-prefix"><xsl:value-of select="@prefix"/></xsl:variable>
    <xsl:variable name="qname"><xsl:value-of select="@qname"/></xsl:variable>
    <xsl:variable name="generateMsgCtx"><xsl:value-of select="@generateMsgCtx"/></xsl:variable>

    /**
     * <xsl:value-of select="@name"/>.c
     *
     * This file was auto-generated from WSDL for "<xsl:value-of select="$qname"/>" service
     * by the Apache Axis2/C version: #axisVersion# #today#
     * <xsl:value-of select="@name"/> Axis2/C skeleton for the axisService
     */

     #include "<xsl:value-of select="@name"/>.h"

     <xsl:for-each select="method">
         <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
         <xsl:variable name="count"><xsl:value-of select="count(output/param)"/></xsl:variable>

         <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
         <xsl:variable name="outputtype">
            <xsl:choose>
                <xsl:when test="$isUnwrapParameters">
                    <xsl:value-of select="output/param/param/@type"/>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise>
            </xsl:choose>
         </xsl:variable>

		 <!-- regardless of the sync or async status, the generated method signature would be just a usual
	           c function-->
        /**
         * auto generated function definition signature
         * for "<xsl:value-of select="@qname"/>" operation.
         * @param env environment ( mandatory)<xsl:if test="$generateMsgCtx='1'">
         <xsl:text>* @param MessageContext the outmessage context</xsl:text></xsl:if><xsl:if test="not($isUnwrapParameters)"><xsl:for-each select="input/param[@type!='']"><xsl:text>
         </xsl:text>* @param _<xsl:value-of select="@name"/> of the <xsl:value-of select="@type"/></xsl:for-each></xsl:if>
         <xsl:if test="$isUnwrapParameters"><xsl:for-each select="input/param/param[@type!='']"><xsl:text>
         </xsl:text>* @param _<xsl:value-of select="@name"/> of the <xsl:value-of select="@type"/></xsl:for-each></xsl:if>
         *<xsl:for-each select="output/param[@location='soap_header']"><xsl:text>
         </xsl:text>* @param dp_<xsl:value-of select="@name"/> - output header</xsl:for-each>
         * @return <xsl:value-of select="$outputtype"/><xsl:text>
         */
        </xsl:text>
         
        <xsl:variable name="inputparams">
            <xsl:choose>
            <xsl:when test="$isUnwrapParameters">
                                          <xsl:for-each select="input/param/param[@type!='']">,
                                              <xsl:value-of select="@type"/><xsl:text> _</xsl:text><xsl:value-of select="@name"/>
                                          </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                                          <xsl:for-each select="input/param[@type!='']">,
                                              <xsl:value-of select="@type"/><xsl:text> _</xsl:text><xsl:value-of select="@name"/>
                                          </xsl:for-each>
            </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
        <xsl:when test="$outputtype=''">axis2_status_t </xsl:when>
        <xsl:when test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:when>
        </xsl:choose>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/><xsl:text>(const axutil_env_t *env </xsl:text>
                                          <xsl:if test="$generateMsgCtx='1'"><xsl:text>, axis2_msg_ctx_t *msg_ctx</xsl:text></xsl:if>
                                          <xsl:value-of select="$inputparams"/><xsl:for-each select="output/param[@location='soap_header']">,
                                            <xsl:variable name="outputtype"><xsl:value-of select="@type"/><xsl:if test="@ours">*</xsl:if></xsl:variable>
                                            <xsl:value-of select="$outputtype"/><xsl:text> dp_</xsl:text><xsl:value-of select="@name"/><xsl:text> /* output header double ptr*/</xsl:text>
                                          </xsl:for-each><xsl:if test="count(fault/*)">,
                                          <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/><xsl:text>_fault</xsl:text> *fault</xsl:if> )
        {
          /* TODO fill this with the necessary business logic */
          <xsl:if test="$outputtype!=''">return (<xsl:value-of select="$outputtype"/>)NULL;</xsl:if>
          <xsl:if test="$outputtype=''">return AXIS2_SUCCESS;</xsl:if>
        }
     </xsl:for-each>



    </xsl:template>
 </xsl:stylesheet>
