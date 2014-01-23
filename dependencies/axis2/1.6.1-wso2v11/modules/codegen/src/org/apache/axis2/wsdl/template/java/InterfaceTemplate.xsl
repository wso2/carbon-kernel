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

     <!-- import the databinding template-->
    <xsl:include href="databindsupporter"/>
    <!-- import the other templates for databinding
         Note  -  these names would be handled by a special
         URI resolver during the xslt transformations
     -->
    <xsl:include href="externalTemplate"/>


    <xsl:template match="/interface">
    <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
    <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
    <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
    <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
    <xsl:variable name="isbackcompatible" select="@isbackcompatible"/>

/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

    package <xsl:value-of select="$package"/>;

    /*
     *  <xsl:value-of select="@name"/> java interface
     */

    public interface <xsl:value-of select="@name"></xsl:value-of> {
          <xsl:for-each select="method">
            <!-- Code for in-out mep -->
         <xsl:if test="@mep='12'">
         <xsl:variable name="outputtype"><xsl:value-of select="output/param[@location='body']/@type"></xsl:value-of></xsl:variable>
         <xsl:variable name="outputcomplextype"><xsl:value-of select="output/param[@location='body']/@complextype"></xsl:value-of></xsl:variable>
         <xsl:variable name="outputparamcount"><xsl:value-of select="count(output/param[@location='body']/param)"></xsl:value-of></xsl:variable>
         <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>

        <!-- start of the sync block -->
         <xsl:if test="$isSync='1'">

        /**
          * Auto generated method signature
          * <xsl:value-of select="@comment"/>
                <xsl:for-each select="input/param[@type!='']">
                    * @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
                </xsl:text></xsl:for-each>
             <xsl:for-each select="fault/param[@type!='']">
             * @throws <xsl:value-of select="@name"/> : <xsl:value-of select="@comment"/>
             </xsl:for-each>
         */

         <xsl:choose>
             <xsl:when test="$isbackcompatible='true'">
                    public <xsl:choose><xsl:when test="$outputtype=''">void</xsl:when>
                      <xsl:when test="string-length(normalize-space($outputcomplextype)) > 0"><xsl:value-of select="$outputcomplextype"/></xsl:when>
                      <xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise></xsl:choose>
                        <xsl:text> </xsl:text><xsl:value-of select="@name"/>(

                        <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                        <xsl:choose>
                            <xsl:when test="$inputcount=1">
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

                        <xsl:if test="$inputcount=1 and input/param[not(@location='body') and @type!='']">,</xsl:if>
                        <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                    <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                        </xsl:for-each>)
                        throws java.rmi.RemoteException
             </xsl:when>
             <xsl:otherwise>
                     public <xsl:choose>
                    <xsl:when test="$outputtype=''">void</xsl:when>
                    <xsl:when test="$outputparamcount=1"><xsl:value-of select="output/param[@location='body']/param/@type"/></xsl:when>
                    <xsl:when test="string-length(normalize-space($outputcomplextype)) > 0"><xsl:value-of
                            select="$outputcomplextype"/></xsl:when>
                    <xsl:when test="($outputparamcount=0) and ($isUnwrapParameters)">void</xsl:when>
                    <xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise>
                    </xsl:choose>
                        <xsl:text> </xsl:text><xsl:value-of select="@name"/>(

                        <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                        <xsl:variable name="inputParamCount" select="count(input/param[@location='body' and @type!='']/param)"/>
                 
                        <xsl:choose>
                            <xsl:when test="$inputcount=1">
                                <!-- Even when the parameters are 1 we have to see whether we have the
                              wrapped parameters -->
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

                        <xsl:if test="($inputcount=1 and input/param[not(@location='body') and @type!='']) and
                                not($isUnwrapParameters and $inputParamCount=0)">,</xsl:if>
                        <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                    <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                        </xsl:for-each>)
                        throws java.rmi.RemoteException
             </xsl:otherwise>
         </xsl:choose>

          <!--add the faults-->
          <xsl:for-each select="fault/param[@type!='']">
          ,<xsl:value-of select="@name"/>
                </xsl:for-each>;

        </xsl:if>

       <!-- start of the async block -->
        <xsl:if test="$isAsync='1'">
         /**
            * Auto generated method signature for Asynchronous Invocations
            * <xsl:value-of select="@comment"/>
            <xsl:for-each select="input/param[@type!='']">
                * @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
            </xsl:text></xsl:for-each>
          */
        public void start<xsl:value-of select="@name"/>(

            <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
            <xsl:variable name="inputWrappedCount" select="count(input/param[@location='body' and @type!='']/param)"/>
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
                <xsl:if test="($inputcount=1) and not($isUnwrapParameters and $inputWrappedCount=0)">,</xsl:if>
                <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                   <xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>,
                </xsl:for-each>

            final <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/> callback)

            throws java.rmi.RemoteException;

     </xsl:if>
     </xsl:if>
        <!-- Code for in-only mep -->
       <xsl:if test="@mep='10' or @mep='11'">
       <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
       /**
         * Auto generated method signature for Asynchronous Invocations
         * <xsl:value-of select="@comment"/>
           <xsl:if test="$mep='11'">
               <xsl:for-each select="fault/param[@type!='']">
                 * @throws <xsl:value-of select="@name"/> : <xsl:value-of select="@comment"/>
               </xsl:for-each>
           </xsl:if>
         */
        public void <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
         <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
         <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
         <xsl:variable name="inputParamCount" select="count(input/param[@location='body' and @type!='']/param)"/>

         <xsl:choose>

                <xsl:when test="$inputcount=1">
                    <!-- Even when the parameters are 1 we have to see whether we have the
                  wrapped parameters -->

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

           <xsl:if test="($inputcount=1 and input/param[not(@location='body') and @type!='']) and
                                not($isUnwrapParameters and $inputParamCount=0)">,</xsl:if>
            <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
            </xsl:for-each>

        ) throws java.rmi.RemoteException
        <!--add the faults-->
        <xsl:if test="$mep='11'">
               <xsl:for-each select="fault/param[@type!='']">
               ,<xsl:value-of select="@name"/>
               </xsl:for-each>
        </xsl:if>;

        </xsl:if>

      </xsl:for-each>

       <!-- Apply other templates --> 
       //<xsl:apply-templates/>
       }
    </xsl:template>
   </xsl:stylesheet>