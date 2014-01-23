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
    <xsl:key name="paramsIn" match="//databinders/param[@direction='in']" use="@type"/>
    <xsl:key name="innerParams" match="//databinders/param[@direction='in']/param" use="@partname"/>

    <!-- #################################################################################  -->
    <!-- ############################   xmlbeans template   ##############################  -->
    <xsl:template match="databinders[@dbtype='xmlbeans']">
        //Create the desired XmlObject and provide it as the test object
        public  org.apache.xmlbeans.XmlObject getTestObject(java.lang.Class type) throws java.lang.Exception{
        java.lang.reflect.Method creatorMethod = null;
                if (org.apache.xmlbeans.XmlObject.class.isAssignableFrom(type)){
                    Class[] declaredClasses = type.getDeclaredClasses();
                    for (int i = 0; i &lt; declaredClasses.length; i++) {
                        Class declaredClass = declaredClasses[i];
                        if (declaredClass.getName().endsWith("$Factory")){
                            creatorMethod = declaredClass.getMethod("newInstance",null);
                            break;
                        }

                    }
                }
                if (creatorMethod!=null){
                    return  (org.apache.xmlbeans.XmlObject)creatorMethod.invoke(null,null);
                }else{
                    throw new java.lang.Exception("Creator not found!");
                }

        }

         <xsl:for-each select="opnames/name">

            <xsl:variable name="opname" select="."/>

              <xsl:if test="count(../../param[@type!='' and @direction='in' and @opname=$opname])=1">
                    <!-- generate the get methods -->
                   <xsl:variable name="inputElement" select="../../param[@type!='' and @direction='in' and @opname=$opname]"></xsl:variable>
                   <xsl:variable name="inputElementType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"></xsl:variable>
                   <xsl:variable name="inputElementShortType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@shorttype"></xsl:variable>

                    <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                        <xsl:variable name="paramElement" select="."></xsl:variable>
                        <xsl:variable name="partName" select="@partname"></xsl:variable>

                        <xsl:if test="(generate-id($paramElement) = generate-id(key('innerParams', $partName)[1])) or
                        (generate-id($inputElement) = generate-id(key('paramsIn', $inputElementType)[1]))">

                            private <xsl:value-of select="@type"/> get<xsl:value-of select="@partname"/>(
                            <xsl:value-of select="../@type"/> wrappedType){

                            <!-- there is not difference betwee having a seperate complex type and the
                                inline complex type implementation -->
                            <xsl:choose>
                                 <xsl:when test="@array">
                                     return wrappedType.get<xsl:value-of select="substring-before($inputElementShortType,'Document')"/>().xget<xsl:value-of select="$partName"/>Array();
                                 </xsl:when>
                                 <xsl:otherwise>
                                     return wrappedType.get<xsl:value-of select="substring-before($inputElementShortType,'Document')"/>().xget<xsl:value-of select="$partName"/>();
                                 </xsl:otherwise>
                            </xsl:choose>
                        }
                       </xsl:if>
                    </xsl:for-each>
                </xsl:if>
        </xsl:for-each>

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   ADB template   ###################################  -->
    <xsl:template match="databinders[@dbtype='adb']">
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        <!-- generate the getter methods for each databinders if it is in uwwrapped mode -->
        <xsl:for-each select="opnames/name">
            <xsl:variable name="opname" select="."/>

            <xsl:if test="count(../../param[@type!='' and @direction='in' and @opname=$opname])=1">
                    <!-- generate the get methods -->
                    <xsl:variable name="inputElement" select="../../param[@type!='' and @direction='in' and @opname=$opname]"></xsl:variable>
                    <xsl:variable name="inputElementType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"></xsl:variable>
                    <xsl:variable name="inputElementShortType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@shorttype"></xsl:variable>
                    <xsl:variable name="inputElementComplexType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@complextype"></xsl:variable>

                    <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">

                        <xsl:variable name="paramElement" select="."></xsl:variable>
                        <xsl:variable name="partName" select="@partname"></xsl:variable>

                        <xsl:if test="(generate-id($paramElement) = generate-id(key('innerParams', $partName)[1])) or
                            (generate-id($inputElement) = generate-id(key('paramsIn', $inputElementType)[1]))">

                            private <xsl:value-of select="@type"/> get<xsl:value-of select="@partname"/>(
                            <xsl:value-of select="../@type"/> wrappedType){
                            <xsl:choose>
                                <!--<xsl:when test="$inputElementComplexType != ''">-->
                                <xsl:when test="string-length(normalize-space($inputElementComplexType)) > 0">
                                    return wrappedType.get<xsl:value-of select="$inputElementShortType"/>().get<xsl:value-of select="@partname"/>();
                                </xsl:when>
                                <xsl:otherwise>
                                    return wrappedType.get<xsl:value-of select="@partname"/>();
                                </xsl:otherwise>
                            </xsl:choose>
                            }

                         </xsl:if>
                    </xsl:for-each>
                    <!-- to support for backword compatiblity we have to add and wrapp method-->
                    <xsl:if test="generate-id($inputElement) = generate-id(key('paramsIn', $inputElementType)[1])">
                        <xsl:if test="string-length(normalize-space($inputElementComplexType)) > 0">
                            private <xsl:value-of select="$inputElementComplexType"/> get<xsl:value-of select="$opname"/>(
                            <xsl:value-of select="$inputElementType"/> wrappedType){
                                return wrappedType.get<xsl:value-of select="$inputElementShortType"/>();
                            }
                            private <xsl:value-of select="$inputElementType"/> wrap<xsl:value-of select="$opname"/>(
                            <xsl:value-of select="$inputElementComplexType"/> innerType){
                                <xsl:value-of select="$inputElementType"/> wrappedElement = new <xsl:value-of select="$inputElementType"/>();
                                wrappedElement.set<xsl:value-of select="$inputElementShortType"/>(innerType);
                                return wrappedElement;
                            }
                        </xsl:if>
                    </xsl:if>
                </xsl:if>
        </xsl:for-each>

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   jibx template   ##############################  -->
    <xsl:template match="databinders[@dbtype='jibx']">
        // create the desired object and provide it as the test object
        public java.lang.Object getTestObject(java.lang.Class type) throws java.lang.Exception {
            return type.newInstance();
        }

    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   Jaxbri template   ###################################  -->
    <xsl:template match="databinders[@dbtype='jaxbri']">
        //Create an object and provide it as the test object
        public Object getTestObject(java.lang.Class type) throws java.lang.Exception{
           return type.newInstance();
        }
    </xsl:template>
    <!-- #################################################################################  -->
    <!-- ############################   none template!!!   ###############################  -->
    <xsl:template match="databinders[@dbtype='none']">
        //Create an OMElement and provide it as the test object
        public org.apache.axiom.om.OMElement getTestObject(java.lang.Object dummy){
           org.apache.axiom.om.OMFactory factory = org.apache.axiom.om.OMAbstractFactory.getOMFactory();
           org.apache.axiom.om.OMNamespace defNamespace = factory.createOMNamespace("",null);
           return org.apache.axiom.om.OMAbstractFactory.getOMFactory().createOMElement("test",defNamespace);
        }
    </xsl:template>

</xsl:stylesheet>