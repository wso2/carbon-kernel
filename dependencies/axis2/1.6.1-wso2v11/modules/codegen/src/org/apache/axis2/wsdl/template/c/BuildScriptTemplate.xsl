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
<xsl:template match="interface">

<xsl:variable name="servicename"><xsl:value-of select="@servicename"/></xsl:variable>
<xsl:variable name="option"><xsl:value-of select="@option"/></xsl:variable>
<xsl:variable name="outputlocation"><xsl:value-of select="@outputlocation"/></xsl:variable>
<xsl:variable name="targetsourcelocation"><xsl:value-of select="@targetsourcelocation"/></xsl:variable>
<xsl:choose>
<xsl:when test="$option = 1">
gcc -g -shared -olib<xsl:value-of select="$servicename"/>.so -I $AXIS2C_HOME/include/axis2-1.7.0/ -I<xsl:value-of select="$targetsourcelocation"/> -L$AXIS2C_HOME/lib \
    -laxutil \
    -laxis2_axiom \
    -laxis2_engine \
    -laxis2_parser \
    -lpthread \
    -laxis2_http_sender \
    -laxis2_http_receiver \
    -lguththila \
    *.c <xsl:value-of select="@targetsourcelocation"/>/*.c
</xsl:when>
<xsl:otherwise>
gcc -g -shared -olib<xsl:value-of select="$servicename"/>.so -I $AXIS2C_HOME/include/axis2-1.7.0/  -L$AXIS2C_HOME/lib \
    -laxutil \
    -laxis2_axiom \
    -laxis2_engine \
    -laxis2_parser \
    -lpthread \
    -laxis2_http_sender \
    -laxis2_http_receiver \
    -lguththila \
    *.c 
</xsl:otherwise>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>
