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
/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */
    package <xsl:value-of select="@package"/>;
    /**
     *  <xsl:value-of select="@name"/> java skeleton interface for the axisService
     */
    public interface <xsl:value-of select="@name"></xsl:value-of> {
     <xsl:variable name="isbackcompatible" select="@isbackcompatible"/>
     <xsl:for-each select="method">
         <xsl:variable name="count"><xsl:value-of select="count(output/param)"/></xsl:variable>
         <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"/></xsl:variable>
         <xsl:variable name="outputcomplextype"><xsl:value-of select="output/param/@complextype"/></xsl:variable>
         <xsl:variable name="outputparamcount"><xsl:value-of select="count(output/param[@location='body']/param)"/></xsl:variable>
         <!-- regardless of the sync or async status, the generated method signature would be just a usual
               java method -->
        /**
         * Auto generated method signature
         * <xsl:value-of select="@comment"/>
         
         <xsl:choose>
            <xsl:when test="$isbackcompatible = 'true'">
                <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                <xsl:choose>
                    <xsl:when test="$inputcount=1">
                        <!-- should provide the inter complex type to method signature if is available -->
                        <xsl:variable name="inputComplexType" select="input/param[@location='body' and @type!='']/@complextype"/>
                        <xsl:choose>
                            <xsl:when test="string-length(normalize-space($inputComplexType)) > 0">
                               * @param<xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                            </xsl:when>
                            <xsl:otherwise>
                               * @param<xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                  <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                    <xsl:choose>
                        <xsl:when test="$inputcount=1">
                            <!-- Even when the parameters are 1 we have to see whether we have the
                          wrapped parameters -->
                            <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
                            <xsl:choose>
                                <xsl:when test="$isUnwrapParameters">
                                   <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                        * @param<xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    * @param<xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                    </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
         <xsl:for-each select="fault/param[@type!='']">
             * @throws <xsl:value-of select="@name"/> : <xsl:value-of select="@comment"/>
         </xsl:for-each>
         */

        <xsl:choose>
            <xsl:when test="$isbackcompatible = 'true'">
                public <xsl:choose><xsl:when test="$count=0 or $outputtype=''">void</xsl:when>
                    <xsl:when test="string-length(normalize-space($outputcomplextype)) > 0"><xsl:value-of select="$outputcomplextype"/></xsl:when>
                    <xsl:when test="$outputtype!=''"><xsl:value-of select="$outputtype"/></xsl:when>
                </xsl:choose><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                (
                  <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                        <xsl:choose>
                            <xsl:when test="$inputcount=1">
                                <!-- should provide the inter complex type to method signature if is available -->
                                <xsl:variable name="inputComplexType" select="input/param[@location='body' and @type!='']/@complextype"/>
                                <xsl:choose>
                                    <xsl:when test="string-length(normalize-space($inputComplexType)) > 0">
                                       <xsl:value-of select="$inputComplexType"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise><!-- Just leave it - nothing we can do here --></xsl:otherwise>
                        </xsl:choose>
                 )
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
                public <xsl:choose>
                    <xsl:when test="$count=0 or $outputtype=''">void</xsl:when>
                    <xsl:when test="$outputparamcount=1"><xsl:value-of select="output/param[@location='body']/param/@type"/></xsl:when>
                    <xsl:when test="string-length(normalize-space($outputcomplextype)) > 0"><xsl:value-of select="$outputcomplextype"/></xsl:when>
                    <xsl:when test="($outputparamcount=0) and ($isUnwrapParameters)">void</xsl:when>
                    <xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise></xsl:choose>
                <xsl:text> </xsl:text><xsl:value-of select="@name"/>
                (
                  <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                    <xsl:choose>
                        <xsl:when test="$inputcount=1">
                            <!-- Even when the parameters are 1 we have to see whether we have the
                          wrapped parameters -->
                            <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
                            <xsl:choose>
                                <xsl:when test="$isUnwrapParameters">
                                   <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                        <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise><!-- Just leave it - nothing we can do here --></xsl:otherwise>
                    </xsl:choose>
                 )
            </xsl:otherwise>
        </xsl:choose>
         <!--add the faults-->
           <xsl:for-each select="fault/param[@type!='']">
               <xsl:if test="position()=1">throws </xsl:if>
               <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@name"/>
           </xsl:for-each>;
        </xsl:for-each>
         }
    </xsl:template>
 </xsl:stylesheet>