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
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="interface">
        <xsl:variable name="servicename">
            <xsl:value-of select="translate(@servicename,':.-','___')"/>
        </xsl:variable>

        <service>
            <xsl:attribute name="name"><xsl:value-of select="$servicename"/></xsl:attribute>

            <parameter name="ServiceClass">
                        <xsl:value-of select="$servicename"/>
            </parameter>
            <description>
              <xsl:value-of select="@servicename"/> Service
            </description>
			<xsl:for-each select="method">
				<operation>
					<xsl:attribute name="name"><xsl:value-of select="@localpart"/></xsl:attribute>
                    <xsl:attribute name="mep"><xsl:value-of select="@mepURI"/></xsl:attribute>
            <xsl:if test="@soapaction">
              <parameter>
                <xsl:attribute name="name"><xsl:text>wsamapping</xsl:text></xsl:attribute>
                <xsl:value-of select="@soapaction"/>
              </parameter>
            </xsl:if>
				</operation>
			</xsl:for-each>
        </service>
    </xsl:template>
</xsl:stylesheet>
