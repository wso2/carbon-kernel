<?xml version="1.0" encoding="utf-8"?>
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">
    <xsl:import href="urn:docbkx:stylesheet"/>
    <xsl:include href="common.xsl"/>
    
    <!-- Only split chapters, not sections -->
    <xsl:param name="chunk.section.depth">0</xsl:param>
    
    <xsl:param name="img.src.path">images/</xsl:param>
    
    <xsl:param name="admon.graphics.extension">.png</xsl:param>
    <xsl:param name="admon.graphics.path"><xsl:value-of select="$img.src.path"/></xsl:param>
</xsl:stylesheet>