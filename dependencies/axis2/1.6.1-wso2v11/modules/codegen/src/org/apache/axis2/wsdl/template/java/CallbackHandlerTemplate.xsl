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
    <xsl:template match="/callback">
/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

    package <xsl:value-of select="@package"/>;

    /**
     *  <xsl:value-of select="@name"/> Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class <xsl:value-of select="@name"/>{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public <xsl:value-of select="@name"/>(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public <xsl:value-of select="@name"/>(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        <xsl:for-each select="method">
            <xsl:variable name="outParamType" select="output/param[@location='body']/@type"></xsl:variable>
            <xsl:variable name="outParamName" select="output/param/@name"></xsl:variable>
            <xsl:variable name="outParamComplexType" select="output/param[@location='body']/@complextype"></xsl:variable>
            <xsl:variable name="outParamCount" select="count(output/param[@location='body']/param)"></xsl:variable>
            <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
            <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
            <xsl:choose>
                <!-- Code generation for in-out only. Need to consider the other meps also
                    They should be parts of this xsl:choose loop -->
                <xsl:when test="$mep='12'">
           /**
            * auto generated Axis2 call back method for <xsl:value-of select="@name"/> method
            * override this method for handling normal response from <xsl:value-of select="@name"/> operation
            */
           public void receiveResult<xsl:value-of select="@name"/>(
                    <xsl:choose>
                        <xsl:when test="$outParamCount=1">
                             <xsl:value-of select="output/param[@location='body']/param/@type"/><xsl:text> </xsl:text>result
                        </xsl:when>
                        <xsl:when test="string-length(normalize-space($outParamComplexType)) > 0">
                            <xsl:value-of select="$outParamComplexType"/><xsl:text> </xsl:text>result
                        </xsl:when>
                        <xsl:when test="($outParamCount=0) and ($isUnwrapParameters)">
                        </xsl:when>
                        <xsl:when test="string-length(normalize-space($outParamType)) > 0">
                            <xsl:value-of select="$outParamType"/><xsl:text> </xsl:text>result
                        </xsl:when>
                    </xsl:choose>) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from <xsl:value-of select="@name"/> operation
           */
            public void receiveError<xsl:value-of select="@name"/>(java.lang.Exception e) {
            }
                </xsl:when>
                <xsl:otherwise>
               // No methods generated for meps other than in-out
                </xsl:otherwise>
            </xsl:choose>


        </xsl:for-each>


    }
    </xsl:template>
</xsl:stylesheet>
