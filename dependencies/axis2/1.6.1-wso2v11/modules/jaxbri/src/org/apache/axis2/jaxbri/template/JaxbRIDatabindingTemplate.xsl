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
<!-- #################################################################################  -->
    <!-- ############################   JAXB-RI template   ##############################  -->
    <xsl:template match="databinders[@dbtype='jaxbri']">

        <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
        <xsl:if test="$base64">
            private static javax.xml.namespace.QName[] qNameArray = {
            <xsl:for-each select="base64Elements/name">
                <xsl:if test="position()>1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
        </xsl:if>

        <xsl:variable name="firstType"><xsl:value-of select="param[1]/@type"/></xsl:variable>
        
        private static final javax.xml.bind.JAXBContext wsContext;
        static {
            javax.xml.bind.JAXBContext jc;
            jc = null;
            try {
				jc = javax.xml.bind.JAXBContext.newInstance(
            <xsl:for-each select="param[not(@type = preceding-sibling::param/@type)]">
                <xsl:if test="@type!=''">
                        <xsl:value-of select="@type"/>.class<xsl:if test="position() != last()">,
                        </xsl:if>
                </xsl:if>
            </xsl:for-each>
				);
            }
            catch ( javax.xml.bind.JAXBException ex ) {
                System.err.println("Unable to create JAXBContext: " + ex.getMessage());
                ex.printStackTrace(System.err);
                Runtime.getRuntime().exit(-1);
            }
            finally {
                wsContext = jc;
			}
        }

        <xsl:for-each select="param[not(@type = preceding-sibling::param/@type)]">
            <xsl:if test="@type!=''">

                private org.apache.axiom.om.OMElement toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                throws org.apache.axis2.AxisFault {
                    try {
                        javax.xml.bind.JAXBContext context = wsContext;
                        javax.xml.bind.Marshaller marshaller = context.createMarshaller();
                        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

                        org.apache.axiom.om.OMFactory factory = org.apache.axiom.om.OMAbstractFactory.getOMFactory();

                        JaxbRIDataSource source = new JaxbRIDataSource( <xsl:value-of select="@type"/>.class,
                                                                        param,
                                                                        marshaller,
                                                                        methodQName.getNamespaceURI(),
                                                                        methodQName.getLocalPart());
                        org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace(methodQName.getNamespaceURI(),
                                                                           null);
                        return factory.createOMElement(source, methodQName.getLocalPart(), namespace);
                    } catch (javax.xml.bind.JAXBException bex){
                        throw org.apache.axis2.AxisFault.makeFault(bex);
                    }
                }

                private org.apache.axiom.om.OMElement toOM(<xsl:value-of select="@type"/> param, boolean optimizeContent)
                    throws org.apache.axis2.AxisFault {
                        try {
                            javax.xml.bind.JAXBContext context = wsContext;
                            javax.xml.bind.Marshaller marshaller = context.createMarshaller();
                            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

                            org.apache.axiom.om.OMFactory factory = org.apache.axiom.om.OMAbstractFactory.getOMFactory();

                            JaxbRIDataSource source = new JaxbRIDataSource( <xsl:value-of select="@type"/>.class,
                                                                            param,
                                                                            marshaller,
                                                                            "<xsl:value-of select="qname/@nsuri"/>",
                                                                            "<xsl:value-of select="qname/@localname"/>");
                            org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace("<xsl:value-of select="qname/@nsuri"/>", null);
                            return factory.createOMElement(source, "<xsl:value-of select="qname/@localname"/>", namespace);
                        } catch (javax.xml.bind.JAXBException bex){
                            throw org.apache.axis2.AxisFault.makeFault(bex);
                        }
                    }

                private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                throws org.apache.axis2.AxisFault {
                    org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                    envelope.getBody().addChild(toOM(param, optimizeContent, methodQName));
                    return envelope;
                }

                <xsl:variable name="propertyType" select="@type"/>
                <xsl:choose>
                    <xsl:when test="$propertyType='byte'">
                        private byte toByte (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Byte ret = (java.lang.Integer)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), byte.class).getValue();
                                return ret.byteValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='char'">
                        private char toChar (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Character ret = (java.lang.Character)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), char.class).getValue();
                                return ret.charValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='double'">
                        private double toDouble (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Double ret = (java.lang.Double)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), double.class).getValue();
                                return ret.doubleValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='float'">
                        private float toFloat (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Float ret = (java.lang.Float)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), float.class).getValue();
                                return ret.floatValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='int'">
                        private int toInt (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Integer ret = (java.lang.Integer)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), int.class).getValue();
                                return ret.intValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='long'">
                        private long toLong (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Long ret = (java.lang.Long)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), long.class).getValue();
                                return ret.longValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='short'">
                        private short toShort (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Short ret = (java.lang.Short)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), short.class).getValue();
                                return ret.shortValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>
                    <xsl:when test="$propertyType='boolean'">
                        private boolean toBoolean (
                            org.apache.axiom.om.OMElement param,
                            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
                            try {
                                javax.xml.bind.JAXBContext context = wsContext;
                                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                                java.lang.Boolean ret = (java.lang.Boolean)unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), boolean.class).getValue();
                                return ret.booleanValue();
                            } catch (javax.xml.bind.JAXBException bex){
                                throw org.apache.axis2.AxisFault.makeFault(bex);
                            }
                        }
                    </xsl:when>                    
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>

        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
            return factory.getDefaultEnvelope();
        }

        private java.lang.Object fromOM (
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
            try {
                javax.xml.bind.JAXBContext context = wsContext;
                javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();

                return unmarshaller.unmarshal(param.getXMLStreamReaderWithoutCaching(), type).getValue();
            } catch (javax.xml.bind.JAXBException bex){
                throw org.apache.axis2.AxisFault.makeFault(bex);
            }
        }

        class JaxbRIDataSource implements org.apache.axiom.om.OMDataSource {
            /**
             * Bound object for output.
             */
            private final Object outObject;

            /**
             * Bound class for output.
             */
            private final Class outClazz;

            /**
             * Marshaller.
             */
            private final javax.xml.bind.Marshaller marshaller;

            /**
             * Namespace
             */
            private String nsuri;

            /**
             * Local name
             */
            private String name;

            /**
             * Constructor from object and marshaller.
             *
             * @param obj
             * @param marshaller
             */
            public JaxbRIDataSource(Class clazz, Object obj, javax.xml.bind.Marshaller marshaller, String nsuri, String name) {
                this.outClazz = clazz;
                this.outObject = obj;
                this.marshaller = marshaller;
                this.nsuri = nsuri;
                this.name = name;
            }

            public void serialize(java.io.OutputStream output, org.apache.axiom.om.OMOutputFormat format) throws javax.xml.stream.XMLStreamException {
                try {
                    marshaller.marshal(new javax.xml.bind.JAXBElement(
                            new javax.xml.namespace.QName(nsuri, name), outObject.getClass(), outObject), output);
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }

            public void serialize(java.io.Writer writer, org.apache.axiom.om.OMOutputFormat format) throws javax.xml.stream.XMLStreamException {
                try {
                    marshaller.marshal(new javax.xml.bind.JAXBElement(
                            new javax.xml.namespace.QName(nsuri, name), outObject.getClass(), outObject), writer);
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }

            public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                try {
                    marshaller.marshal(new javax.xml.bind.JAXBElement(
                            new javax.xml.namespace.QName(nsuri, name), outObject.getClass(), outObject), xmlWriter);
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }

            public javax.xml.stream.XMLStreamReader getReader() throws javax.xml.stream.XMLStreamException {
                try {
                    javax.xml.bind.JAXBContext context = wsContext;
                    org.apache.axiom.om.impl.builder.SAXOMBuilder builder = new org.apache.axiom.om.impl.builder.SAXOMBuilder();
                    javax.xml.bind.Marshaller marshaller = context.createMarshaller();
                    marshaller.marshal(new javax.xml.bind.JAXBElement(
                            new javax.xml.namespace.QName(nsuri, name), outObject.getClass(), outObject), builder);

                    return builder.getRootElement().getXMLStreamReader();
                } catch (javax.xml.bind.JAXBException e) {
                    throw new javax.xml.stream.XMLStreamException("Error in JAXB marshalling", e);
                }
            }
        }
        
    </xsl:template>
    </xsl:stylesheet>
