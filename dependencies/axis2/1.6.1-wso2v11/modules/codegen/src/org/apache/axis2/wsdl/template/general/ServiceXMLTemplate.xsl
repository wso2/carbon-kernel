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
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:template match="/">
        <xsl:comment> This file was auto-generated from WSDL </xsl:comment>
        <xsl:comment> by the Apache Axis2 version: #axisVersion# #today# </xsl:comment>
        <serviceGroup>
            <xsl:apply-templates/>
        </serviceGroup>
    </xsl:template>

    <xsl:template match="interfaces/interface">
        <xsl:variable name="package"><xsl:value-of select="@classpackage"/></xsl:variable>

        <service>
            <xsl:attribute name="name"><xsl:value-of select="@servicename"/></xsl:attribute>
            <messageReceivers>
                <xsl:for-each select="messagereceiver">
                    <xsl:if test=".">
                        <messageReceiver>
                            <xsl:attribute name="mep"><xsl:value-of select="@mepURI"/></xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="$package=''">
                                    <xsl:attribute name="class"><xsl:value-of select="."/></xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class"><xsl:value-of select="$package"/>.<xsl:value-of select="."/></xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                        </messageReceiver>
                    </xsl:if>
                </xsl:for-each>
             </messageReceivers>

            <parameter name="ServiceClass">
                <xsl:choose>
                    <xsl:when test="$package=''">
                        <xsl:value-of select="@name"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$package"/>.<xsl:value-of select="@name"/>
                    </xsl:otherwise>
                </xsl:choose>
            </parameter>
            <parameter name="useOriginalwsdl">true</parameter>
            <parameter name="modifyUserWSDLPortAddress">true</parameter>
            <xsl:for-each select="method">
				<operation>
					<xsl:attribute name="name"><xsl:value-of select="@originalName"/></xsl:attribute>
					<xsl:attribute name="mep"><xsl:value-of select="@mepURI"/></xsl:attribute>
					<xsl:attribute name="namespace"><xsl:value-of select="@namespace"/></xsl:attribute>
					<actionMapping>
						<xsl:value-of select="@soapaction"/>
					</actionMapping>
					<xsl:for-each select="outputActionMapping">
					<outputActionMapping>
						<xsl:value-of select="@Action"/>
					</outputActionMapping>
					</xsl:for-each>
					<xsl:for-each select="faultActionMapping">
					<faultActionMapping>
						<xsl:attribute name="faultName"><xsl:value-of select="@faultName"/></xsl:attribute>
						<xsl:value-of select="@Action"/>
					</faultActionMapping>
					</xsl:for-each>
				</operation>
			</xsl:for-each>
        </service>
    </xsl:template>
</xsl:stylesheet>