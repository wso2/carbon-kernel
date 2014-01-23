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
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="/ant">
        <xsl:variable name="package" select="@package"/>
        <xsl:variable name="src" select="@src"/>
        <xsl:variable name="resource" select="@resource"/>

        <project basedir=".">
             <xsl:choose>
                <xsl:when test="@isserverside">
                   <xsl:attribute name="default">jar.server</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="default">jar.client</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:comment>Auto generated ant build file</xsl:comment>
            <property environment="env"/>
                        <property name="axis2.home">
                            <xsl:attribute name="value">${env.AXIS2_HOME}</xsl:attribute>
                        </property>
            <property name="project.base.dir">
                <xsl:attribute name="value">.</xsl:attribute>
            </property>
            <property name="src">
                <xsl:attribute name="value">${project.base.dir}/<xsl:value-of select="$src"/></xsl:attribute>
            </property>
            <property name="test">
                <xsl:attribute name="value">${project.base.dir}/test</xsl:attribute>
            </property>
            <property name="build">
                <xsl:attribute name="value">${project.base.dir}/build</xsl:attribute>
            </property>
            <property name="classes">
                <xsl:attribute name="value">${build}/classes</xsl:attribute>
            </property>
            <property name="lib">
                <xsl:attribute name="value">${build}/lib</xsl:attribute>
            </property>
             <property name="resources">
                <xsl:attribute name="value">${project.base.dir}/<xsl:value-of select="$resource"/></xsl:attribute>
            </property>
            <property name="name">
                <xsl:attribute name="value"><xsl:value-of select="@artifactname"/></xsl:attribute>
            </property>

            <property name="jars.ok" value=""></property>

            <path id="axis2.class.path">
                <pathelement>
                    <xsl:attribute name="path">${java.class.path}</xsl:attribute>
                </pathelement>
                <pathelement>
                    <xsl:attribute name="path">${maven.class.path}</xsl:attribute>
                </pathelement>
                <fileset>
                    <xsl:attribute name="dir">${axis2.home}</xsl:attribute>
                    <include>
                        <xsl:attribute name="name">lib/*.jar</xsl:attribute>
                    </include>
                </fileset>
            </path>

            <target name="init">
                <mkdir>
                    <xsl:attribute name="dir">${build}</xsl:attribute>
                </mkdir>
                <mkdir>
                    <xsl:attribute name="dir">${classes}</xsl:attribute>
                </mkdir>
                <mkdir>
                    <xsl:attribute name="dir">${lib}</xsl:attribute>
                </mkdir>
                <xsl:if test="not(@testOmit)">
                    <mkdir>
                        <xsl:attribute name="dir">${test}</xsl:attribute>
                    </mkdir>
                </xsl:if>
            </target>

            <target name="pre.compile.test" depends="init">
                <xsl:comment>Test the classpath for the availability of necesary classes</xsl:comment>

                <available classname="javax.xml.stream.XMLStreamReader" property="stax.available"/>
                <available classname="org.apache.axis2.engine.AxisEngine" property="axis2.available"/>
                <condition property="jars.ok" >
                    <and>
                        <isset property="stax.available"/>
                        <isset property="axis2.available"/>
                    </and>
                </condition>

                <xsl:comment>Print out the availabilities</xsl:comment>
                <echo>
                    <xsl:attribute name="message">Stax Availability= ${stax.available}</xsl:attribute>
                </echo>
                <echo>
                    <xsl:attribute name="message">Axis2 Availability= ${axis2.available}</xsl:attribute>
                </echo>

            </target>

            <target name="compile.src" depends="echo.classpath.problem">
                <xsl:attribute name="if">jars.ok</xsl:attribute>
                <javac fork="true" memoryInitialSize="256m" memoryMaximumSize="256m" debug="on">
                    <xsl:attribute name="destdir">${classes}</xsl:attribute>
                    <xsl:attribute name="srcdir">${src}</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="refid">axis2.class.path</xsl:attribute>
                    </classpath>
                </javac>
            </target>

            <target name="compile.test" depends="compile.src">
                <xsl:attribute name="if">jars.ok</xsl:attribute>
                <javac fork="true" memoryInitialSize="256m" memoryMaximumSize="256m" debug="on">
                    <xsl:attribute name="destdir">${classes}</xsl:attribute>
                    <xsl:attribute name="srcdir">${test}</xsl:attribute>
                    <classpath>
                        <xsl:attribute name="refid">axis2.class.path</xsl:attribute>
                    </classpath>
                </javac>
            </target>

            <target name="echo.classpath.problem" depends="pre.compile.test">
                <xsl:attribute name="unless">jars.ok</xsl:attribute>
                <echo message="The class path is not set right!
                               Please make sure the following classes are in the classpath
                               1. Stax
                               2. Axis2
                "></echo>
            </target>


            <target name="jar.client"  if="jars.ok">
                 <xsl:choose>
                    <xsl:when test="@testOmit">
                        <xsl:attribute name="depends">compile.src</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="depends">compile.test</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <jar>
                    <xsl:attribute name="destfile">${lib}/${name}-client.jar</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${classes}</xsl:attribute>
                        <exclude>
                            <xsl:attribute name="name">**/META-INF/*.*</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/lib/*.*</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/*MessageReceiver.class</xsl:attribute>
                        </exclude>
                        <exclude>
                            <xsl:attribute name="name">**/*Skeleton.class</xsl:attribute>
                        </exclude>
                    </fileset>
                </jar>
            </target>





            <target name="jar.server" depends="compile.src,echo.classpath.problem">
                <xsl:attribute name="if">jars.ok</xsl:attribute>
                <copy>
                    <xsl:attribute name="toDir">${classes}/META-INF</xsl:attribute>
                    <xsl:attribute name="failonerror">false</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="dir">${resources}</xsl:attribute>
                        <include>
                            <xsl:attribute name="name">*.xml</xsl:attribute>
                        </include>
                        <include>
                            <xsl:attribute name="name">*.wsdl</xsl:attribute>
                        </include>
                        <include>
                            <xsl:attribute name="name">*.xsd</xsl:attribute>
                        </include>
                    </fileset>
                </copy>
                <jar>
                    <xsl:attribute name="destfile">${lib}/${name}.aar</xsl:attribute>
                    <fileset>
                        <xsl:attribute name="excludes">**/Test.class</xsl:attribute>
                        <xsl:attribute name="dir">${classes}</xsl:attribute>
                    </fileset>
                </jar>
            </target>

            <target name="jar.all" depends="jar.server,jar.client"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
