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

    <!-- #################################################################################  -->
    <!-- ############################   xmlbeans template   ##############################  -->
    <xsl:template match="databinders[@dbtype='xmlbeans']">
        <xsl:variable name="serverside" select="@isserverside"/>
        <xsl:variable name="base64" select="base64Elements/name"/>
        <xsl:if test="$base64">
            private static javax.xml.namespace.QName[] qNameArray = {
            <xsl:for-each select="base64Elements/name">
                <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
        </xsl:if>

        <xsl:for-each select="param[not(@type = preceding-sibling::param/@type) and @type!='']">

            private  org.apache.axiom.om.OMElement  toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault{

            <xsl:choose>
                <xsl:when test="$base64">
                     if (optimizeContent) {
                        org.apache.axiom.om.impl.builder.StAXOMBuilder builder = new org.apache.axiom.om.impl.builder.StAXOMBuilder
                        (org.apache.axiom.om.OMAbstractFactory.getOMFactory(),new org.apache.axis2.util.StreamWrapper(param.newXMLStreamReader())) ;
                        org.apache.axiom.om.OMElement documentElement = builder.getDocumentElement(true);
                        optimizeContent(documentElement,qNameArray);
                        return documentElement;
                     } else {
                        return toOM(param);
                     }
                </xsl:when>
                <xsl:otherwise>
                    return toOM(param);
                </xsl:otherwise>
            </xsl:choose>

            }

            private org.apache.axiom.om.OMElement toOM(final <xsl:value-of select="@type"/> param)
                    throws org.apache.axis2.AxisFault {

                org.apache.axiom.om.impl.builder.SAXOMBuilder builder = new org.apache.axiom.om.impl.builder.SAXOMBuilder();
                org.apache.xmlbeans.XmlOptions xmlOptions = new org.apache.xmlbeans.XmlOptions();
                xmlOptions.setSaveNoXmlDecl();
                xmlOptions.setSaveAggressiveNamespaces();
                xmlOptions.setSaveNamespacesFirst();
                try {
                   param.save(builder, builder, xmlOptions);
                   org.apache.axiom.om.OMElement element = builder.getRootElement();
                   return element;
                } catch (java.lang.Exception e) {
                    throw org.apache.axis2.AxisFault.makeFault(e);
                }
            }
        </xsl:for-each>


        <xsl:for-each select="opnames/name">

            <xsl:variable name="opname" select="."/>
            <xsl:variable name="opnsuri" select="@opnsuri"/>
            <xsl:variable name="paramcount" select="count(../../param[@type!='' and @direction='in' and @opname=$opname])"/>
             <!-- get the opname capitalized -->
            <xsl:variable name="opnameCapitalized" select="concat(translate(substring($opname, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($opname, 2, string-length($opname)))"></xsl:variable>

            <xsl:if test="not($serverside)">
                <xsl:choose>
                    <xsl:when test="$paramcount &gt; 0">
                        <xsl:variable name="inputElement" select="../../param[@type!='' and @direction='in' and @opname=$opname]"></xsl:variable>
                        <xsl:variable name="inputElementType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@type"></xsl:variable>
                        <xsl:variable name="inputElementShortType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@shorttype"></xsl:variable>
                        <xsl:variable name="inputElementComplexType" select="../../param[@type!='' and @direction='in' and @opname=$opname]/@complextype"></xsl:variable>
                        <xsl:variable name="wrappedParameterCount" select="count(../../param[@type!='' and @direction='in' and @opname=$opname]/param)"></xsl:variable>

                        <xsl:if test="generate-id($inputElement) = generate-id(key('paramsIn', $inputElementType)[1])">
                            <xsl:choose>
                            <xsl:when test="$wrappedParameterCount &gt; 0">
                                <!-- geneate the toEnvelope method-->
                                private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
                                <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                                    <xsl:value-of select="@type"/> param<xsl:value-of select="position()"/>,
                                </xsl:for-each><xsl:value-of select="$inputElementType"/> dummyWrappedType,
                                boolean optimizeContent) throws org.apache.axis2.AxisFault{

                                <xsl:value-of select="$inputElementType"/> wrappedType = <xsl:value-of select="$inputElementType"/>.Factory.newInstance();

                                <xsl:choose>
                                    <xsl:when test="string-length(normalize-space($inputElementComplexType)) > 0">
                                        <xsl:value-of select="$inputElementComplexType"/> innerType = wrappedType.addNew<xsl:value-of select="substring-before($inputElementShortType,'Document')"/>();
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$inputElementType"/>.<xsl:value-of select="substring-before($inputElementShortType,'Document')"/> innerType = wrappedType.addNew<xsl:value-of select="substring-before($inputElementShortType,'Document')"/>();
                                    </xsl:otherwise>
                                </xsl:choose>

                                <xsl:for-each select="../../param[@type!='' and @direction='in' and @opname=$opname]/param">
                                    <xsl:choose>
                                        <xsl:when test="@array">
                                            innerType.xset<xsl:value-of select="@partname"/>Array(param<xsl:value-of select="position()"/>);
                                        </xsl:when>
                                        <xsl:otherwise>
                                            innerType.xset<xsl:value-of select="@partname"/>(param<xsl:value-of select="position()"/>);
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>

                                org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                                envelope.getBody().addChild(toOM(wrappedType, optimizeContent));
                                return envelope;

                                }

                            </xsl:when>
                            <xsl:otherwise>
                                <!-- Assumption - the parameter is always an XMLBeans -->
                                private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="$inputElementType"/> param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                throws org.apache.axis2.AxisFault{
                                org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                                if (param != null){
                                envelope.getBody().addChild(toOM(param, optimizeContent));
                                }
                                return envelope;
                                }
                            </xsl:otherwise>
                        </xsl:choose>
                        </xsl:if>
                    </xsl:when>

                    <xsl:otherwise>
                        <!-- Do nothing here -->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
            <!-- this piece of logic needs to be generated only for the server side-->
            <xsl:if test="$serverside">


                <xsl:choose>
                    <xsl:when test="count(../../param[@type!='' and @direction='out' and @opname=$opname])=1">
                        <xsl:variable name="outElement" select="../../param[@type!='' and @direction='out' and @opname=$opname]"></xsl:variable>
                        <xsl:variable name="outElementType" select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"></xsl:variable>

                        <!-- Assumption - This is an XMLBeans element-->
                        <xsl:if test="generate-id($outElement) = generate-id(key('paramsOut', $outElementType)[1])">
                            private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="../../param[@type!='' and @direction='out' and @opname=$opname]/@type"/> param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                            throws org.apache.axis2.AxisFault {
                            org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                            if (param != null){
                            envelope.getBody().addChild(toOM(param, optimizeContent));
                            }
                            return envelope;
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
            </xsl:if>
        </xsl:for-each>


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }

        public org.apache.xmlbeans.XmlObject fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
        try{
        <xsl:for-each select="param[@type!='' and not(@primitive)]">

            if (<xsl:value-of select="@type"/>.class.equals(type)){
            if (extraNamespaces!=null){
            return <xsl:value-of select="@type"/>.Factory.parse(
            param.getXMLStreamReaderWithoutCaching(),
            new org.apache.xmlbeans.XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));
            }else{
            return <xsl:value-of select="@type"/>.Factory.parse(
            param.getXMLStreamReaderWithoutCaching());
            }
            }

        </xsl:for-each>
        }catch(java.lang.Exception e){
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        return null;
        }

        <!-- Generate the base 64 optimize methods only if the base64 items are present -->
        <xsl:if test="$base64">

            private void optimizeContent(org.apache.axiom.om.OMElement element, javax.xml.namespace.QName[] qNames){
            for (int i = 0; i &lt; qNames.length; i++) {
            markElementsAsOptimized(qNames[i],element);
            }
            }

            private void markElementsAsOptimized(javax.xml.namespace.QName qName,org.apache.axiom.om.OMElement rootElt){
            if (rootElt.getQName().equals(qName)){
            //get the text node and mark it
            org.apache.axiom.om.OMNode node = rootElt.getFirstOMChild();
            if (node.getType()==org.apache.axiom.om.OMNode.TEXT_NODE){
            ((org.apache.axiom.om.OMText)node).setOptimize(true);
            }

            }
            java.util.Iterator childElements = rootElt.getChildElements();
            while (childElements.hasNext()) {
            markElementsAsOptimized(qName,(org.apache.axiom.om.OMElement)childElements.next());
            }
            }
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>