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

    <xsl:template match="javaConstruct">package <xsl:value-of select="@package"/>;
<xsl:for-each select="importList/import">
import <xsl:value-of select="@value"/>;</xsl:for-each>
<xsl:text>
</xsl:text>
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
public class <xsl:value-of select="@name"/> extends Exception{

    /**
     * Java type that goes as soapenv:Fault detail element.
     */
    private <xsl:value-of select="@shortType"/> faultInfo;

    /**
     * @param faultInfo
     * @param message
     */
    public <xsl:value-of select="@name"/>(String message, <xsl:value-of select="@shortType"/> faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * @param faultInfo
     * @param message
     * @param cause
     */
    public <xsl:value-of select="@name"/>(String message, <xsl:value-of select="@shortType"/> faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * @return
     *   returns fault bean: <xsl:value-of select="@shortType"/>
     */
    public <xsl:value-of select="@shortType"/> getFaultInfo() {
        return faultInfo;
    }
}
    </xsl:template>

   <!-- <xsl:template match="method">
    <xsl:variable name="outparamcount" select="count(output/param)"/>
    @javax.jws.WebMethod
    <xsl:choose>
        <xsl:when test="$outparamcount = 0">@javax.jws.Oneway</xsl:when>
        <xsl:otherwise>@javax.jws.WebResult(targetNamespace = "<xsl:value-of select="$targetNs"/>")</xsl:otherwise>
    </xsl:choose>
    public <xsl:choose>
                <xsl:when test="$outparamcount = 0">void </xsl:when>
                <xsl:otherwise><xsl:value-of select="output/param/@type"/><xsl:text> </xsl:text></xsl:otherwise>
           </xsl:choose>
    <xsl:value-of select="@name"/>(<xsl:variable name="inparamcount" select="count(input/param)"/>
    <xsl:for-each select="input/param">
        @javax.jws.WebParam(name = "<xsl:value-of select="@name"/>", targetNamespace = "<xsl:value-of select="$targetNs"/>")
        <xsl:choose>
            <xsl:when test="$inparamcount = position()">
                <xsl:value-of select="@type"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="@name"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@type"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="@name"/>,<xsl:text> </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>)--><!--<xsl:for-each select="fault/param[@type!='']">
               <xsl:if test="position()=1">
        throws </xsl:if>
               <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/>
           </xsl:for-each>--><!--;
    </xsl:template>-->

</xsl:stylesheet>
