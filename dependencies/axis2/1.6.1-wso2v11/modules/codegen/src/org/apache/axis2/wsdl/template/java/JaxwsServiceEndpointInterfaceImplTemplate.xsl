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

/**
 * <xsl:value-of select="@name"/>.java
 *
 * This class was auto-generated from WSDL.
 * Apache Axis2 version: #axisVersion# #today#
 */
<xsl:for-each select="annotation">
    <xsl:variable name="annoparamcount" select="count(param)"/>
@<xsl:value-of select="@name"/>(<xsl:for-each select="param">
        <xsl:choose>
            <xsl:when test="$annoparamcount = position()">
                <xsl:value-of select="@type"/>
                <xsl:text> = </xsl:text>"<xsl:value-of select="@value"/>"</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@type"/>
                <xsl:text> = </xsl:text>"<xsl:value-of select="@value"/>",<xsl:text> </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>)</xsl:for-each>
public class <xsl:value-of select="@name"/> {
<xsl:apply-templates/>
}
    </xsl:template>

    <xsl:template match="method">
    <xsl:variable name="outparamcount" select="count(output/param)"/>
    <xsl:variable name="parameterstyle" select="@parameterstyle"/>
    <xsl:variable name="useholder" select="@useholder"/>
    <xsl:variable name="style" select="@style"/>
    <xsl:variable name="inputWrappedCount" select="count(input/param/param)"/>
    <xsl:variable name="inparamcount" select="count(input/param)"/>
    /**<xsl:choose><xsl:when test="$inputWrappedCount &gt; 0"><xsl:for-each select="input/param/param">
     * @param <xsl:value-of select="@name"/></xsl:for-each><xsl:if test="$outparamcount != 0">
     * @return <xsl:value-of select="output/param/@shorttype"/></xsl:if><xsl:for-each select="fault/param[@type!='']">
     * @throws <xsl:value-of select="@name"/></xsl:for-each></xsl:when><xsl:otherwise><xsl:for-each select="input/param">
     * @param <xsl:value-of select="@name"/></xsl:for-each><xsl:if test="$outparamcount != 0">
     * @return <xsl:value-of select="output/param/@shorttype"/></xsl:if><xsl:for-each select="fault/param[@type!='']">
     * @throws <xsl:value-of select="@name"/></xsl:for-each></xsl:otherwise> </xsl:choose>
     */
    public <xsl:choose>
                <xsl:when test="$outparamcount = 0">void </xsl:when>
                <xsl:when test="$useholder = 'true'">void </xsl:when>
                <xsl:otherwise><xsl:value-of select="output/param/@type"/><xsl:text> </xsl:text></xsl:otherwise>
           </xsl:choose>
    <xsl:value-of select="@name"/>(<xsl:choose>
        <xsl:when test="$useholder = 'true'">
    <xsl:for-each select="input/param">
        @javax.jws.WebParam(name = "<xsl:value-of select="@name"/>", targetNamespace = "<xsl:value-of select="$targetNs"/>", mode = javax.jws.WebParam.Mode.INOUT<xsl:choose>
                <xsl:when test="$parameterstyle = 'BARE'">, partName = "<xsl:value-of select="@partname"/>"</xsl:when></xsl:choose>)
        <xsl:choose>
            <xsl:when test="$inparamcount = position()">javax.xml.ws.Holder&lt;<xsl:value-of select="@type"/>
                <xsl:text>&gt; </xsl:text>
                <xsl:value-of select="@name"/>
            </xsl:when>
            <xsl:otherwise>javax.xml.ws.Holder&lt;<xsl:value-of select="@type"/>
                <xsl:text>&gt; </xsl:text>
                <xsl:value-of select="@name"/>,<xsl:text> </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each></xsl:when>
        <xsl:when test="$inputWrappedCount &gt; 0">
            <xsl:for-each select="input/param/param">
        <xsl:choose>
            <xsl:when test="$inputWrappedCount = position()">
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
            </xsl:for-each>
        </xsl:when>
        <xsl:otherwise><xsl:for-each select="input/param">
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
    </xsl:for-each></xsl:otherwise>
    </xsl:choose>)<xsl:for-each select="fault/param[@type!='']">
               <xsl:if test="position()=1">
            throws </xsl:if>
               <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@shortName"/>
           </xsl:for-each> {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName()
                + "#" + this.getClass().getEnclosingMethod().getName());
    }
    </xsl:template>

</xsl:stylesheet>
