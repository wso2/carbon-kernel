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
    <xsl:template match="/fault">
/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

package <xsl:value-of select="@package"/>;

public class <xsl:value-of select="@shortName"/> extends <xsl:value-of select="@exceptionBaseClass"/>{

    private static final long serialVersionUID = <xsl:value-of select="@serialVersionUID"/>L;
    
    private <xsl:value-of select="@type"/> faultMessage;

    <xsl:variable name="classShortName" select="@shortName"/>
    <xsl:if test="count(constructor) > 0">
        <xsl:for-each select="constructor">
             public <xsl:value-of select="$classShortName"/>(<xsl:for-each select="param"><xsl:if test="position() > 1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/></xsl:for-each>) {
                super(<xsl:for-each select="param"><xsl:if test="position() > 1">,</xsl:if><xsl:value-of select="@name"/></xsl:for-each>);
            }
        </xsl:for-each>
    </xsl:if>
    <xsl:if test="count(constructor) = 0">
        public <xsl:value-of select="@shortName"/>() {
            super("<xsl:value-of select="@shortName"/>");
        }

        public <xsl:value-of select="@shortName"/>(java.lang.String s) {
           super(s);
        }

        public <xsl:value-of select="@shortName"/>(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public <xsl:value-of select="@shortName"/>(java.lang.Throwable cause) {
            super(cause);
        }
    </xsl:if>

    public void setFaultMessage(<xsl:value-of select="@type"/> msg){
       faultMessage = msg;
    }
    
    public <xsl:value-of select="@type"/> getFaultMessage(){
       return faultMessage;
    }
}
    </xsl:template>
</xsl:stylesheet>