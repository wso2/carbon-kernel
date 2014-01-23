
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
    <xsl:key name="paramsOut" match="//databinders/param[@direction='out']" use="@type"/>
    <xsl:key name="innerParams" match="//databinders/param[@direction='in']/param" use="@partname"/>
    <xsl:key name="innerOutParams" match="//databinders/param[@direction='out']/param" use="@partname"/>
    <xsl:key name="outOperationName" match="//databinders/param[@direction='out']" use="@opname"/>
    <xsl:key name="inOperationName" match="//databinders/param[@direction='in']" use="@opname"/>
    <!--<xsl:key name="paramsType" match="//databinders/param[@direction='in']" use="@type"/>-->

    <!-- #################################################################################  -->
    <!-- ############################   ADB template   ##############################  -->
    <xsl:template match="databinders[@dbtype='adb']">
        <xsl:variable name="serverside"  select="@isserverside"></xsl:variable>
        <xsl:variable name="helpermode"  select="extra/@h"></xsl:variable>

        <!--  generate toOM for only non parts and non primitives!!! -->
        <xsl:for-each select="param[not(@type = preceding-sibling::param/@type) and @type!='' and not(@primitive)]">
            private  org.apache.axiom.om.OMElement  toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            <xsl:choose>
                    <xsl:when test="$helpermode">
                         try{
                            return <xsl:value-of select="@type"/>Helper.INSTANCE.getOMElement(
                                        param,
                                        <xsl:value-of select="@type"/>.MY_QNAME,
                                        org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                            } catch(org.apache.axis2.databinding.ADBException e){
                                throw org.apache.axis2.AxisFault.makeFault(e);
                            }
                    </xsl:when>
                    <xsl:when test="@type = 'org.apache.axiom.om.OMElement'">
                     return param; 
                    </xsl:when>
                    <xsl:otherwise>
                        try{
                             return param.getOMElement(<xsl:value-of select="@type"/>.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    </xsl:otherwise>
            </xsl:choose>

            }
        </xsl:for-each>

        <xsl:for-each select="opnames/name">

            <xsl:variable name="opname" select="."/>
            <xsl:variable name="opnsuri" select="@opnsuri"/>
            <xsl:variable name="paramcount" select="count(../../param[@type!='' and @direction='in' and @opname=$opname])"/>

            <xsl:if test="not($serverside)">
                <xsl:choose>
                    <xsl:when test="$paramcount &gt; 0">
                        <xsl:variable name="inputElement" select="../../param[@type!='' and @direction='in' and @opname=$opname]"></xsl:variable>
                        <xsl:variable name="inputElementType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"></xsl:variable>
                        <xsl:variable name="inputElementShortType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@shorttype"></xsl:variable>
                        <xsl:variable name="inputElementComplexType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@complextype"></xsl:variable>
                        <!--<xsl:variable name="wrappedParameterCount" select="count(../../param[@type!='' and @direction='in' and @opname=$opname]/param)"></xsl:variable>-->
                        <xsl:variable name="isUnwrapParameters" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@unwrappParameters"/>
                        <xsl:if test="generate-id($inputElement) = generate-id(key('paramsIn', $inputElementType)[1])">

                         <!-- if the unwrapping mode is on then we have to generate the unwrapped methods -->
                         <xsl:choose>
                                <xsl:when test="$isUnwrapParameters">
                                    <!-- geneate the toEnvelope method-->
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                    <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                                     <xsl:value-of select="@type"/> param<xsl:value-of select="position()"/>,
                                    </xsl:for-each>
                                    <xsl:value-of select="$inputElementType"/> dummyWrappedType,
                                 boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                try{
                                <xsl:value-of select="$inputElementType"/> wrappedType = new <xsl:value-of select="$inputElementType"/>();

                                 <xsl:choose>
                                     <xsl:when test="string-length(normalize-space($inputElementComplexType)) > 0">
                                          <xsl:value-of select="$inputElementComplexType"/> wrappedComplexType = new <xsl:value-of select="$inputElementComplexType"/>();
                                          <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                                              wrappedComplexType.set<xsl:value-of select="@partname"/>(param<xsl:value-of select="position()"/>);
                                         </xsl:for-each>
                                         wrappedType.set<xsl:value-of select="$inputElementShortType"/>(wrappedComplexType);
                                     </xsl:when>
                                     <xsl:otherwise>
                                         <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                                              wrappedType.set<xsl:value-of select="@partname"/>(param<xsl:value-of select="position()"/>);
                                         </xsl:for-each>
                                     </xsl:otherwise>
                                 </xsl:choose>

                               org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                  <xsl:choose>
                                    <xsl:when test="$helpermode">
                                        emptyEnvelope.getBody().addChild(<xsl:value-of select="$inputElementType"/>Helper.INSTANCE.getOMElement(
                                        wrappedType,
                                        <xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                                    </xsl:when>
                                    <xsl:otherwise>
                                        emptyEnvelope.getBody().addChild(wrappedType.getOMElement(<xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                                    </xsl:otherwise>
                                </xsl:choose>

                                return emptyEnvelope;
                               } catch(org.apache.axis2.databinding.ADBException e){
                                    throw org.apache.axis2.AxisFault.makeFault(e);
                               }
                               }



                                </xsl:when>
                                <xsl:otherwise>
                                    <!-- Assumption - the parameter is always an ADB element-->
                                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="$inputElementType"/> param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                        throws org.apache.axis2.AxisFault{

                                             <xsl:choose>
                                                <xsl:when test="$helpermode">
                                                    try{
                                                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                        emptyEnvelope.getBody().addChild(<xsl:value-of select="$inputElementType"/>Helper.INSTANCE.getOMElement(
                                                        param,
                                                        <xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                                                        return emptyEnvelope;
                                                        } catch(org.apache.axis2.databinding.ADBException e){
                                                            throw org.apache.axis2.AxisFault.makeFault(e);
                                                        }
                                                </xsl:when>
                                                <xsl:when test="$inputElementType = 'org.apache.axiom.om.OMElement'">
                                                    org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                    emptyEnvelope.getBody().addChild(param);
                                                    return emptyEnvelope;
                                                 </xsl:when>
                                                <xsl:otherwise>
                                                    try{

                                                            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                            emptyEnvelope.getBody().addChild(param.getOMElement(<xsl:value-of select="$inputElementType"/>.MY_QNAME,factory));
                                                            return emptyEnvelope;
                                                        } catch(org.apache.axis2.databinding.ADBException e){
                                                            throw org.apache.axis2.AxisFault.makeFault(e);
                                                        }
                                                </xsl:otherwise>
                                        </xsl:choose>

                                        }
                                </xsl:otherwise>
                         </xsl:choose>
                             <!-- to support for backword compatiblity we have to add and wrapp method-->
                             /* methods to provide back word compatibility */

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
                   </xsl:when>
                   <xsl:otherwise>
                      <!-- Do nothing here -->
                    </xsl:otherwise>
                </xsl:choose>
                <!-- generate additional getter and setter method to remove top element of the response
                    only usefull when -b option on-->
                <xsl:if test="count(../../param[@type!='' and @direction='out' and @opname=$opname])=1">
                        <!-- generate the get methods -->
                        <xsl:variable name="outputElement" select="../../param[@type!='' and @direction='out' and @opname=$opname]"></xsl:variable>
                        <xsl:variable name="outputElementType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"></xsl:variable>
                        <xsl:variable name="outputElementShortType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@shorttype"></xsl:variable>
                        <xsl:variable name="outputElementComplexType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@complextype"></xsl:variable>

                        <xsl:for-each select="../../param[@type!='' and @direction='out' and @opname=$opname]/param">

                            <xsl:variable name="paramElement" select="."></xsl:variable>
                            <xsl:variable name="partName" select="@partname"></xsl:variable>

                            <xsl:if test="(generate-id($paramElement) = generate-id(key('innerOutParams', $partName)[1])) or
                                (generate-id($outputElement) = generate-id(key('paramsOut', $outputElementType)[1]))">

                                <!-- we put the out element type to the method signature to make it unique -->
                                private <xsl:value-of select="@type"/> get<xsl:value-of select="$outputElementShortType"/><xsl:value-of select="@partname"/>(
                                <xsl:value-of select="$outputElementType"/> wrappedType){
                                <xsl:choose>
                                    <xsl:when test="string-length(normalize-space($outputElementComplexType)) > 0">
                                        return wrappedType.get<xsl:value-of select="$outputElementShortType"/>().get<xsl:value-of select="@partname"/>();
                                    </xsl:when>
                                    <xsl:otherwise>
                                        return wrappedType.get<xsl:value-of select="@partname"/>();
                                    </xsl:otherwise>
                                </xsl:choose>
                                }
                             </xsl:if>
                        </xsl:for-each>

                        <xsl:if test="generate-id($outputElement) = generate-id(key('paramsOut', $outputElementType)[1]) or
                                  generate-id($outputElement) = generate-id(key('outOperationName', $opname)[1])">
                            <xsl:if test="string-length(normalize-space($outputElementComplexType)) > 0">

                                private <xsl:value-of select="$outputElementComplexType"/> get<xsl:value-of select="$opname"/>(
                                <xsl:value-of select="$outputElementType"/> wrappedType){
                                    return wrappedType.get<xsl:value-of select="$outputElementShortType"/>();
                                }

                                <!-- in client side we donot have to wrap the out put messages -->
                                <!--
                                private <xsl:value-of select="$outputElementType"/> wrap<xsl:value-of select="$opname"/>(
                                <xsl:value-of select="$outputElementComplexType"/> innerType){
                                    <xsl:value-of select="$outputElementType"/> wrappedElement = new <xsl:value-of select="$outputElementType"/>();
                                    wrappedElement.set<xsl:value-of select="$outputElementShortType"/>(innerType);
                                    return wrappedElement;
                                } -->
                            </xsl:if>
                        </xsl:if>
                  </xsl:if>
            </xsl:if>
            <!-- this piece of logic needs to be generated only for the server side-->
            <xsl:if test="$serverside">
             <xsl:choose>
                  <xsl:when test="count(../../param[@type!='' and @direction='out' and @opname=$opname])=1">
                  <xsl:variable name="outElement" select="../../param[@type!='' and @direction='out' and @opname=$opname]"></xsl:variable>
                  <xsl:variable name="outElementType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"></xsl:variable>
                    <!-- Assumption - The ADBBean here is always an element based bean -->
                    <xsl:if test="generate-id($outElement) = generate-id(key('paramsOut', $outElementType)[1])">
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/> param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           <xsl:choose>
                                <xsl:when test="$helpermode">
                                    emptyEnvelope.getBody().addChild(
                                    <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/>Helper.INSTANCE.getOMElement(
                                    param,
                                    <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/>.MY_QNAME,factory));
                                </xsl:when>
                                <xsl:otherwise>
                                    emptyEnvelope.getBody().addChild(param.getOMElement(<xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/>.MY_QNAME,factory));
                                </xsl:otherwise>
                        </xsl:choose>

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    </xsl:if>
                </xsl:when>
       </xsl:choose>

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
                <xsl:if test="generate-id($inputElement) = generate-id(key('paramsIn', $inputElementType)[1]) or
                    generate-id($inputElement) = generate-id(key('inOperationName', $opname)[1])">
                    <xsl:if test="string-length(normalize-space($inputElementComplexType)) > 0">
                        private <xsl:value-of select="$inputElementComplexType"/> get<xsl:value-of select="$opname"/>(
                        <xsl:value-of select="$inputElementType"/> wrappedType){
                            return wrappedType.get<xsl:value-of select="$inputElementShortType"/>();
                        }
                        <!-- in server side we do not want to wrap input elements -->
                        <!--
                        private <xsl:value-of select="$inputElementType"/> wrap<xsl:value-of select="$opname"/>(
                        <xsl:value-of select="$inputElementComplexType"/> innerType){
                            <xsl:value-of select="$inputElementType"/> wrappedElement = new <xsl:value-of select="$inputElementType"/>();
                            wrappedElement.set<xsl:value-of select="$inputElementShortType"/>(innerType);
                            return wrappedElement;
                        } -->
                    </xsl:if>
                </xsl:if>
            </xsl:if>

            <xsl:if test="count(../../param[@type!='' and @direction='out' and @opname=$opname])=1">
            <!-- generate the get methods -->
            <xsl:variable name="outputElement" select="../../param[@type!='' and @direction='out' and @opname=$opname]"></xsl:variable>
            <xsl:variable name="outputElementType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"></xsl:variable>
            <xsl:variable name="outputElementShortType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@shorttype"></xsl:variable>
            <xsl:variable name="outputElementComplexType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@complextype"></xsl:variable>

            <xsl:for-each select="../../param[@type!='' and @direction='out' and @opname=$opname]/param">

                    <xsl:variable name="paramElement" select="."></xsl:variable>
                    <xsl:variable name="partName" select="@partname"></xsl:variable>

                    <xsl:if test="(generate-id($paramElement) = generate-id(key('innerOutParams', $partName)[1])) or
                        (generate-id($outputElement) = generate-id(key('paramsOut', $outputElementType)[1]))">

                        <!-- we put the out element type to the method signature to make it unique -->
                        private <xsl:value-of select="$outputElementType"/> wrap<xsl:value-of select="$outputElementShortType"/><xsl:value-of select="@partname"/>(
                        <xsl:value-of select="@type"/> param){
                        <xsl:value-of select="$outputElementType"/> wrappedElement = new <xsl:value-of select="$outputElementType"/>();
                        <xsl:choose>
                            <xsl:when test="string-length(normalize-space($outputElementComplexType)) > 0">
                                <xsl:value-of select="$outputElementComplexType"/> innerType = new <xsl:value-of
                                    select="$outputElementComplexType"/>();
                                innerType.set<xsl:value-of select="@partname"/>(param);
                                wrappedElement.set<xsl:value-of select="$outputElementShortType"/>(innerType);
                            </xsl:when>
                            <xsl:otherwise>
                                wrappedElement.set<xsl:value-of select="@partname"/>(param);
                            </xsl:otherwise>
                        </xsl:choose>
                            return wrappedElement;
                        }
                     </xsl:if>
                </xsl:for-each>

            <xsl:if test="generate-id($outputElement) = generate-id(key('paramsOut', $outputElementType)[1]) or
                    generate-id($outputElement) = generate-id(key('outOperationName', $opname)[1])">

                <xsl:choose>
                    <xsl:when test="string-length(normalize-space($outputElementComplexType)) > 0" >
                         private <xsl:value-of select="$outputElementType"/> wrap<xsl:value-of select="$opname"/>(
                            <xsl:value-of select="$outputElementComplexType"/> innerType){
                                <xsl:value-of select="$outputElementType"/> wrappedElement = new <xsl:value-of select="$outputElementType"/>();
                                wrappedElement.set<xsl:value-of select="$outputElementShortType"/>(innerType);
                                return wrappedElement;
                         }
                    </xsl:when>
                    <xsl:otherwise>
                         private <xsl:value-of select="$outputElementType"/> wrap<xsl:value-of select="$opname"/>(){
                                <xsl:value-of select="$outputElementType"/> wrappedElement = new <xsl:value-of select="$outputElementType"/>();
                                return wrappedElement;
                         }
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:if>
            </xsl:if>
      </xsl:if>
      </xsl:for-each>


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        <xsl:for-each select="param[not(@primitive) and @type!='']">
                if (<xsl:value-of select="@type"/>.class.equals(type)){
                <xsl:choose>
                    <xsl:when test="$helpermode">
                           return <xsl:value-of select="@type"/>Helper.INSTANCE.parse(param.getXMLStreamReaderWithoutCaching());
                    </xsl:when>
                    <xsl:when test="@type = 'org.apache.axiom.om.OMElement'">
                           return param;
                    </xsl:when>
                    <xsl:otherwise>
                           return <xsl:value-of select="@type"/>.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    </xsl:otherwise>
                </xsl:choose>

                }
           </xsl:for-each>
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    </xsl:template>

</xsl:stylesheet>