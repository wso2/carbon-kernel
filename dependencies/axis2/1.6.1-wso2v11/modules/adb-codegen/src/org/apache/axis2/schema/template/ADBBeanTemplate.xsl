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

    <!-- cater for the multiple classes - wrappped mode -->
    <xsl:template match="/beans">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
/**
 * <xsl:value-of select="$name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

        <xsl:if test="string-length(normalize-space(@package)) > 0">
            package <xsl:value-of select="@package"/>;
        </xsl:if>

        /**
        *  <xsl:value-of select="$name"/> wrapped bean classes
        */
        @SuppressWarnings({"unchecked","unused"})
        public class <xsl:value-of select="$name"/>{

        <xsl:apply-templates/>

        }
    </xsl:template>

    <!--cater for the multiple classes - unwrappped mode -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>



<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->
<!-- $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$-->

    <xsl:include href="ADBBeanTemplate-bean.xsl"/>
    <xsl:include href="ADBBeanTemplate-helpermode.xsl"/>

    <!-- this is the common template -->
  <xsl:template match="mapper">
        <xsl:variable name="name" select="@name"/>
        <xsl:variable name="helperMode" select="@helpermode"/>

         <xsl:if test="not(not(@unwrapped) or (@skip-write))">
/**
 * <xsl:value-of select="$name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

        <xsl:if test="string-length(normalize-space(@package)) > 0">
            package <xsl:value-of select="@package"/>;
        </xsl:if>
            /**
            *  <xsl:value-of select="$name"/> class
            */
            @SuppressWarnings({"unchecked","unused"})
        </xsl:if>
        public <xsl:if test="not(@unwrapped) or (@skip-write)">static</xsl:if> class <xsl:value-of select="$name"/>{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              <xsl:for-each select="type">
                  if (
                  "<xsl:value-of select="@nsuri"/>".equals(namespaceURI) &amp;&amp;
                  "<xsl:value-of select="@shortname"/>".equals(typeName)){
                   <xsl:choose>
                       <xsl:when test="$helperMode">
                           return  <xsl:value-of select="@classname"/>Helper.INSTANCE.parse(reader);
                       </xsl:when>
                        <xsl:otherwise>
                            return  <xsl:value-of select="@classname"/>.Factory.parse(reader);
                        </xsl:otherwise>
                   </xsl:choose>

                  }

              </xsl:for-each>
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    </xsl:template>
</xsl:stylesheet>
