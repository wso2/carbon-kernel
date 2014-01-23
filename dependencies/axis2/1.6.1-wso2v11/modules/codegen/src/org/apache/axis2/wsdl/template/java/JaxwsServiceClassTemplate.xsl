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

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <xsl:variable name="targetNs" select="/javaConstruct/@targetNamespace"/>
    <xsl:variable name="serviceName" select="/javaConstruct/@name"/>
    <xsl:variable name="capitalizedServiceName" select="/javaConstruct/@capitalizedServiceName"/>
    <xsl:variable name="wsdlLocation" select="/javaConstruct/@wsdlLocation"/>

    <xsl:template match="javaConstruct">package <xsl:value-of select="@package"/>;

import java.net.URL;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import javax.xml.ws.Service;<!--<xsl:for-each select="importList/import">-->
<!--import <xsl:value-of select="@value"/>;</xsl:for-each>-->
/**
 * <xsl:value-of select="@name"/>.java
 *
 * This class was auto-generated from WSDL.
 * Apache Axis2 version: #axisVersion# #today#
 *
 */
<xsl:for-each select="annotation">
    <xsl:variable name="annoparamcount" select="count(param)"/>
@<xsl:value-of select="@name"/>(<xsl:for-each select="param">
        <xsl:choose>
            <xsl:when test="$annoparamcount = position()">
                <xsl:value-of select="@type"/>
                <xsl:text>=</xsl:text>"<xsl:value-of select="@value"/>"</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@type"/>
                <xsl:text>=</xsl:text>"<xsl:value-of select="@value"/>",<xsl:text> </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>)</xsl:for-each>
public class <xsl:value-of select="$serviceName"/> extends Service {

    public final static URL <xsl:value-of select="$capitalizedServiceName"/>_WSDL_LOCATION;

    static {
        URL url = null;

        try {
            url = new URL("<xsl:value-of select="$wsdlLocation"/>");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        <xsl:value-of select="$capitalizedServiceName"/>_WSDL_LOCATION = url;
    }

    public <xsl:value-of select="$serviceName"/>() {
        super(<xsl:value-of select="$capitalizedServiceName"/>_WSDL_LOCATION,
            new QName("<xsl:value-of select="$targetNs"/>","<xsl:value-of select="$serviceName"/>"));
    }

    public <xsl:value-of select="$serviceName"/>(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }
    <xsl:for-each select="/javaConstruct/port">
    /**
     * @return
     *   returns <xsl:value-of select="@portTypeName"/>
     */<xsl:for-each select="annotation">
    <xsl:variable name="annoparamcount" select="count(param)"/>
    @<xsl:value-of select="@name"/>(<xsl:for-each select="param">
        <xsl:choose>
            <xsl:when test="$annoparamcount = position()">
                <xsl:value-of select="@type"/>
                <xsl:text>=</xsl:text>"<xsl:value-of select="@value"/>"</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@type"/>
                <xsl:text>=</xsl:text>"<xsl:value-of select="@value"/>",<xsl:text> </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>)</xsl:for-each>
    public <xsl:value-of select="@portTypeName"/> get<xsl:value-of select="@portName"/>() {
        return (<xsl:value-of select="@portTypeName"/>)super.getPort(new QName("<xsl:value-of select="$targetNs"/>","<xsl:value-of select="@portName"/>"),
                <xsl:value-of select="@portTypeName"/>.class);
    }</xsl:for-each>        
}
    </xsl:template>
</xsl:stylesheet>
