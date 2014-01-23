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

    <!-- include the databind supporters  -->
    <xsl:include href="databindsupporter"/>
    <!-- import the other templates for databinding
           Note  -  these names would be handled by a special
           URI resolver during the xslt transformations
       -->
    <xsl:include href="externalTemplate"/>


    <!--Template for in out message receiver -->
    <xsl:template match="/interface[@basereceiver='org.apache.axis2.receivers.AbstractInOutMessageReceiver']">

        <xsl:variable name="skeletonname"><xsl:value-of select="@skeletonInterfaceName"/></xsl:variable>
        <xsl:variable name="isbackcompatible" select="@isbackcompatible"/>
/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */
        package <xsl:value-of select="@package"/>;

        /**
        *  <xsl:value-of select="@name"/> message receiver
        */

        public class <xsl:value-of select="@name"/> extends <xsl:value-of select="@basereceiver"/>{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        <xsl:value-of select="$skeletonname"/> skel = (<xsl:value-of select="$skeletonname"/>)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) &amp;&amp; ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava<xsl:if test="@isLowerCaseMethodName">Identifier</xsl:if>(op.getName().getLocalPart())) != null)){


        <xsl:for-each select="method">
            <xsl:if test="position() > 1">} else </xsl:if>

            if("<xsl:value-of select="@name"/>".equals(methodName)){
                <!-- If usedbmethod attribute present, gives name of method to call for implementation -->
                <xsl:variable name="usedbmethod"><xsl:value-of select="@usedbmethod"/></xsl:variable>
                <xsl:choose>
                    <xsl:when test="string-length(normalize-space($usedbmethod))=0">

                        <xsl:variable name="namespace"><xsl:value-of select="@namespace"/></xsl:variable>

                        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
                        <xsl:variable name="style"><xsl:value-of select="@style"/></xsl:variable>

                        <xsl:variable name="returntype" select="output/param[@location='body']/@type"/>
                        <xsl:variable name="returnvariable" select="output/param[@location='body']/@name"/>
                        <xsl:variable name="returncomplextype"><xsl:value-of select="output/param[@location='body']/@complextype"/></xsl:variable>
                        <xsl:variable name="returnparamcount"><xsl:value-of select="count(output/param[@location='body']/param)"/></xsl:variable>
                        <xsl:variable name="returnshorttype"><xsl:value-of select="output/param[@location='body']/@shorttype"/></xsl:variable>
                        <xsl:variable name="returnpartname"><xsl:value-of select="output/param[@location='body']/param/@partname"/></xsl:variable>

						<xsl:choose>
	                        <xsl:when test="$returntype = 'byte' or $returntype = 'short' or $returntype = 'int' or $returntype = 'long' or $returntype = 'float' or $returntype = 'double'">
	                            <xsl:value-of select="$returntype"/>
	                            <xsl:text> </xsl:text>
	                            <xsl:value-of select="$returnvariable"/> = 0;
	                        </xsl:when>	
	                        
	                        <xsl:when test="$returntype = 'boolean'">
	                            <xsl:value-of select="$returntype"/>
	                            <xsl:text> </xsl:text>
	                            <xsl:value-of select="$returnvariable"/> = false;
	                        </xsl:when>	                        					

	                        <xsl:when test="$returntype = 'char'">
	                            <xsl:value-of select="$returntype"/>
	                            <xsl:text> </xsl:text>
	                            <xsl:value-of select="$returnvariable"/> = '\u0000';
	                        </xsl:when>	
	                        						
	                        <xsl:when test="string-length(normalize-space($returntype)) &gt; 0">
	                            <xsl:value-of select="$returntype"/>
	                            <xsl:text> </xsl:text>
	                            <xsl:value-of select="$returnvariable"/> = null;
	                        </xsl:when>
						
						</xsl:choose>

                        <xsl:choose>
                            <!-- We really don't need to make a difference between these-->
                            <xsl:when test="$style='document' or $style='rpc'">

                                <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                                <xsl:variable name="inputtype" select="input/param[@location='body' and @type!='']/@type"/>
                                <xsl:variable name="inputComplexType" select="input/param[@location='body' and @type!='']/@complextype"/>
                                <xsl:variable name="operationName" select="input/param[@location='body' and @type!='']/@opname"/>

                                <xsl:choose>
                                    <xsl:when test="$isbackcompatible = 'true'">
                                        <xsl:choose>
                                               <xsl:when test="$inputcount=1">
                                                     <xsl:value-of select="$inputtype"/> wrappedParam =
                                                                 (<xsl:value-of select="$inputtype"/>)fromOM(
                                                        msgContext.getEnvelope().getBody().getFirstElement(),
                                                        <xsl:value-of select="$inputtype"/>.class,
                                                        getEnvelopeNamespaces(msgContext.getEnvelope()));

                                                        <xsl:if test="string-length(normalize-space($returntype)) > 0"><xsl:value-of select="$returnvariable"/> =</xsl:if>
                                                        <xsl:choose>
                                                            <xsl:when test="(string-length(normalize-space($inputComplexType)) > 0) and (string-length(normalize-space($returncomplextype)) > 0)">
                                                                   wrap<xsl:value-of select="$operationName"/>(skel.<xsl:value-of select="@name"/>(
                                                                      get<xsl:value-of select="$operationName"/>(wrappedParam)));
                                                            </xsl:when>
                                                            <xsl:when test="(string-length(normalize-space($inputComplexType)) > 0)  and (string-length(normalize-space($returncomplextype)) &lt; 1)">
                                                                   skel.<xsl:value-of select="@name"/>(
                                                                      get<xsl:value-of select="$operationName"/>(wrappedParam));
                                                            </xsl:when>
                                                            <xsl:when test="(string-length(normalize-space($inputComplexType)) &lt; 1)  and (string-length(normalize-space($returncomplextype)) > 0)">
                                                                   wrap<xsl:value-of select="$operationName"/>(skel.<xsl:value-of select="@name"/>(wrappedParam));
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                 skel.<xsl:value-of select="@name"/>(wrappedParam);
                                                            </xsl:otherwise>
                                                         </xsl:choose>

                                                </xsl:when>
                                                <xsl:otherwise>
                                                     <xsl:if test="string-length(normalize-space($returntype)) &gt; 0"><xsl:value-of select="$returnvariable"/> =</xsl:if>
                                                     <xsl:choose>
                                                         <xsl:when test="string-length(normalize-space($returncomplextype)) > 0">
                                                             wrap<xsl:value-of select="$operationName"/>(skel.<xsl:value-of select="@name"/>());
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                             skel.<xsl:value-of select="@name"/>();
                                                         </xsl:otherwise>
                                                     </xsl:choose>
                                                </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>

                                         <xsl:choose>
                                            <xsl:when test="$inputcount=1">
                                                 <xsl:value-of select="$inputtype"/> wrappedParam =
                                                             (<xsl:value-of select="$inputtype"/>)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    <xsl:value-of select="$inputtype"/>.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                <!-- Even when the parameters are 1 we have to see whether we have the
                                              wrapped parameters -->
                                               <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
                                               <xsl:if test="string-length(normalize-space($returntype)) &gt; 0"><xsl:value-of select="$returnvariable"/> =
                                                   <!-- set the response wrappers if unwrapping on -->
                                                   <xsl:choose>
                                                       <xsl:when test="$returnparamcount=1">
                                                           wrap<xsl:value-of select="$returnshorttype"/><xsl:value-of
                                                               select="$returnpartname"/>(
                                                       </xsl:when>
                                                       <xsl:when test="string-length(normalize-space($returncomplextype)) &gt; 0">
                                                           wrap<xsl:value-of select="$operationName"/>(
                                                       </xsl:when>
                                                       <xsl:when test="($returnparamcount=0) and ($isUnwrapParameters)">
                                                           wrap<xsl:value-of select="$operationName"/>();
                                                       </xsl:when>
                                                   </xsl:choose>
                                               </xsl:if>

                                                <xsl:choose>
                                                    <xsl:when test="($isUnwrapParameters)">
                                                        <!-- generate the references. the getters need to be
                                                            generated by the databinding-->

                                                        <!-- wrap it if it in unwarping mode -->
                                                       skel.<xsl:value-of select="@name"/>(
                                                            <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                                                <xsl:if test="position() &gt; 1">,</xsl:if>
                                                                get<xsl:value-of select="@partname"/>(wrappedParam)
                                                            </xsl:for-each>)
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                         skel.<xsl:value-of select="@name"/>(wrappedParam)
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="string-length(normalize-space($returntype)) &gt; 0">
                                                     <xsl:if test="($returnparamcount=1) or (string-length(normalize-space($returncomplextype)) &gt; 0)">
                                                         )
                                                     </xsl:if>
                                                 </xsl:if>;
                                            </xsl:when>
                                            <xsl:otherwise>
                                                 <xsl:if test="string-length(normalize-space($returntype)) &gt; 0"><xsl:value-of select="$returnvariable"/> =
                                                     <xsl:choose>
                                                       <xsl:when test="$returnparamcount=1">
                                                           wrap<xsl:value-of select="$returnshorttype"/><xsl:value-of
                                                               select="$returnpartname"/>(
                                                       </xsl:when>
                                                       <xsl:when test="string-length(normalize-space($returncomplextype)) &gt; 0">
                                                           wrap<xsl:value-of select="$operationName"/>(
                                                       </xsl:when>
                                                   </xsl:choose>
                                                 </xsl:if>
                                                 skel.<xsl:value-of select="@name"/>()
                                                <xsl:if test="string-length(normalize-space($returntype)) &gt; 0">
                                                     <xsl:if test="($returnparamcount=1) or (string-length(normalize-space($returncomplextype)) &gt; 0)">
                                                         )
                                                     </xsl:if>
                                                 </xsl:if>;
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:otherwise>
                                </xsl:choose>


                                <xsl:choose>
                                    <xsl:when test="string-length(normalize-space($returntype)) &gt; 0">
                                        envelope = toEnvelope(getSOAPFactory(msgContext), <xsl:value-of select="$returnvariable"/>, false, new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                    "<xsl:value-of select="$name"/>"));
                                    </xsl:when>
                                    <xsl:otherwise>
                                        envelope = getSOAPFactory(msgContext).getDefaultEnvelope();
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>

                            <xsl:otherwise>
                                //Unknown style!! No code is generated
                                throw new java.lang.UnsupportedOperationException("Unknown Style");
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>

                    <xsl:otherwise>
                        envelope = <xsl:value-of select="$usedbmethod"/>(msgContext.getEnvelope().getBody().getFirstElement(), skel, getSOAPFactory(msgContext));
                    </xsl:otherwise>

                </xsl:choose>

        </xsl:for-each>
        <xsl:if test="method">
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        </xsl:if>

        newMsgContext.setEnvelope(envelope);
        }
        <xsl:for-each select="fault-list/fault">
            <xsl:if test="position()=1">}</xsl:if> catch (<xsl:value-of select="@name"/> e) {

            msgContext.setProperty(org.apache.axis2.Constants.FAULT_NAME,"<xsl:value-of select="@localname"/>");
            org.apache.axis2.AxisFault f = createAxisFault(e);
            if (e.getFaultMessage() != null){
                f.setDetail(toOM(e.getFaultMessage(),false));
            }
            throw f;
            }
        </xsl:for-each>
        <!-- put the extra bracket-->
        <xsl:if test="count(fault-list/fault)=0">}</xsl:if>
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        <!-- Call templates recursively-->
        //<xsl:apply-templates><xsl:with-param name="context">message-receiver</xsl:with-param></xsl:apply-templates>

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    </xsl:template>
    <!-- end of template for in-out message receiver -->

    <!-- start of in-only -->
    <xsl:template match="/interface[@basereceiver='org.apache.axis2.receivers.AbstractInMessageReceiver']">
        <xsl:variable name="skeletonname"><xsl:value-of select="@skeletonInterfaceName"/></xsl:variable>

/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */
        package <xsl:value-of select="@package"/>;

        /**
        *  <xsl:value-of select="@name"/> message receiver
        */

        public class <xsl:value-of select="@name"/> extends <xsl:value-of select="@basereceiver"/>{

        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext inMessage) throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(inMessage);

        <xsl:value-of select="$skeletonname"/> skel = (<xsl:value-of select="$skeletonname"/>)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = inMessage.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) &amp;&amp; ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava<xsl:if test="@isLowerCaseMethodName">Identifier</xsl:if>(op.getName().getLocalPart())) != null)){

        <xsl:for-each select="method">

            <xsl:variable name="style"><xsl:value-of select="@style"/></xsl:variable>

            <xsl:if test="position() > 1">} else </xsl:if>
            if("<xsl:value-of select="@name"/>".equals(methodName)){
            <!-- If usedbmethod attribute present, gives name of method to call for implementation -->
            <xsl:variable name="usedbmethod"><xsl:value-of select="@usedbmethod"/></xsl:variable>
            <xsl:choose>
                <xsl:when test="string-length(normalize-space($usedbmethod))=0">

                    <xsl:choose>
                        <!-- style does not matter since we create the message formats correctly -->
                        <xsl:when test="$style='rpc' or $style='document'">

                            <xsl:variable name="paramCount" select="count(input/param[@location='body' and @type!=''])"/>
                            <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
                            <xsl:choose>
                                <xsl:when test="$paramCount =1">
                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/> wrappedParam = (<xsl:value-of select="input/param[@location='body' and @type!='']/@type"/>)fromOM(
                                                        inMessage.getEnvelope().getBody().getFirstElement(),
                                                        <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/>.class,
                                                        getEnvelopeNamespaces(inMessage.getEnvelope()));
                                            <xsl:choose>
                                                <xsl:when test="($isUnwrapParameters)">
                                                    <!-- generate the references. the getters need to be
                                                        generated by the databinding-->

                                                    <!-- wrap it if it in unwarping mode -->
                                                   skel.<xsl:value-of select="@name"/>(
                                                        <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                                            <xsl:if test="position() &gt; 1">,</xsl:if>
                                                            get<xsl:value-of select="@partname"/>(wrappedParam)
                                                        </xsl:for-each>);
                                                </xsl:when>
                                                <xsl:otherwise>
                                                     skel.<xsl:value-of select="@name"/>(wrappedParam);
                                                </xsl:otherwise>
                                            </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    skel.<xsl:value-of select="@name"/>();
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>

                        <xsl:otherwise>
                            //Unknown style!! No code is generated
                            throw new java.lang.UnsupportedOperationException("Unknown Style");
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:when>

                <xsl:otherwise>
                    <xsl:value-of select="$usedbmethod"/>(inMessage.getEnvelope().getBody().getFirstElement(), skel, null);
                </xsl:otherwise>

            </xsl:choose>
        </xsl:for-each>
            <xsl:if test="method">
                } else {
                  throw new java.lang.RuntimeException("method not found");
                }
            </xsl:if>

        }
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }


        <!-- Call templates recursively-->
        //<xsl:apply-templates><xsl:with-param name="context">message-receiver</xsl:with-param></xsl:apply-templates>



        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }



        }//end of class

    </xsl:template>

    <!--Template for robust in message receiver -->
    <xsl:template match="/interface[@basereceiver='org.apache.axis2.receivers.AbstractMessageReceiver']">

        <xsl:variable name="skeletonname"><xsl:value-of select="@skeletonInterfaceName"/></xsl:variable>
        <xsl:variable name="isbackcompatible" select="@isbackcompatible"/>

/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */
        package <xsl:value-of select="@package"/>;

        /**
        *  <xsl:value-of select="@name"/> message receiver
        */

        public class <xsl:value-of select="@name"/> extends <xsl:value-of select="@basereceiver"/>{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        <xsl:value-of select="$skeletonname"/> skel = (<xsl:value-of select="$skeletonname"/>)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) &amp;&amp; ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava<xsl:if test="@isLowerCaseMethodName">Identifier</xsl:if>(op.getName().getLocalPart())) != null)){
     
        <xsl:for-each select="method">
            <xsl:if test="position() > 1">} else </xsl:if>
            if("<xsl:value-of select="@name"/>".equals(methodName)){
            <!-- If usedbmethod attribute present, gives name of method to call for implementation -->
            <xsl:variable name="usedbmethod"><xsl:value-of select="@usedbmethod"/></xsl:variable>
            <xsl:choose>
                <xsl:when test="string-length(normalize-space($usedbmethod))=0">

                    <xsl:variable name="style"><xsl:value-of select="@style"/></xsl:variable>

                    <xsl:choose>
                        <!-- We really don't need to make a difference between these-->
                        <xsl:when test="$style='document' or $style='rpc'">
                            //doc style
                            <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                            <xsl:variable name="inputtype" select="input/param[@location='body' and @type!='']/@type"/>
                            <xsl:variable name="operationName" select="input/param[@location='body' and @type!='']/@opname"/>

                            <xsl:choose>
                                <xsl:when test="$isbackcompatible = 'true'">
                                     <xsl:choose>
                                        <xsl:when test="$inputcount=1">
                                             <xsl:value-of select="$inputtype"/> wrappedParam =
                                                         (<xsl:value-of select="$inputtype"/>)fromOM(
                                                        msgContext.getEnvelope().getBody().getFirstElement(),
                                                        <xsl:value-of select="$inputtype"/>.class,
                                                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                            <!-- Even when the parameters are 1 we have to see whether we have the
                                          wrapped parameters -->
                                           <xsl:variable name="inputComplexType" select="count(input/param[@location='body' and @type!='']/param)"/>
                                           <xsl:variable name="operationName" select="input/param[@location='body' and @type!='']/@opname"/>
                                            <xsl:choose>
                                                <xsl:when test="string-length(normalize-space($inputComplexType)) > 0">
                                                   skel.<xsl:value-of select="@name"/>(
                                                        get<xsl:value-of select="$operationName"/>(wrappedParam)
                                                    );
                                                </xsl:when>
                                                <xsl:otherwise>
                                                     skel.<xsl:value-of select="@name"/>(wrappedParam) ;
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                             skel.<xsl:value-of select="@name"/>();
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                     <xsl:choose>
                                        <xsl:when test="$inputcount=1">
                                             <xsl:value-of select="$inputtype"/> wrappedParam =
                                                         (<xsl:value-of select="$inputtype"/>)fromOM(
                                                        msgContext.getEnvelope().getBody().getFirstElement(),
                                                        <xsl:value-of select="$inputtype"/>.class,
                                                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                            <!-- Even when the parameters are 1 we have to see whether we have the
                                          wrapped parameters -->
                                           <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
                                            <xsl:choose>
                                                <xsl:when test="$isUnwrapParameters">
                                               skel.<xsl:value-of select="@name"/>(
                                                    <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                                        <xsl:if test="position() &gt; 1">,</xsl:if>
                                                        get<xsl:value-of select="@partname"/>(wrappedParam)
                                                    </xsl:for-each>

                                                );
                                                </xsl:when>
                                                <xsl:otherwise>
                                                     skel.<xsl:value-of select="@name"/>(wrappedParam) ;
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                             skel.<xsl:value-of select="@name"/>();
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>

                             envelope = getSOAPFactory(msgContext).getDefaultEnvelope();
                        </xsl:when>

                        <xsl:otherwise>
                            //Unknown style!! No code is generated
                            throw new java.lang.UnsupportedOperationException("Unknown Style");
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <xsl:otherwise>
                    envelope = <xsl:value-of select="$usedbmethod"/>(msgContext.getEnvelope().getBody().getFirstElement(), skel, getSOAPFactory(msgContext));
                </xsl:otherwise>

            </xsl:choose>
        </xsl:for-each>
             <xsl:if test="method">
                } else {
                  throw new java.lang.RuntimeException("method not found");
                }
            </xsl:if>

        }
        <xsl:for-each select="fault-list/fault">
            <xsl:if test="position()=1">}</xsl:if> catch (<xsl:value-of select="@name"/> e) {
            msgContext.setProperty(org.apache.axis2.Constants.FAULT_NAME,"<xsl:value-of select="@localname"/>");
            org.apache.axis2.AxisFault f = createAxisFault(e);
            if (e.getFaultMessage() != null){
                f.setDetail(toOM(e.getFaultMessage(),false));
            }
            throw f;
            }
        </xsl:for-each>
        <!-- put the extra bracket-->
        <xsl:if test="count(fault-list/fault)=0">}</xsl:if>
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        <!-- Call templates recursively-->
        //<xsl:apply-templates><xsl:with-param name="context">message-receiver</xsl:with-param></xsl:apply-templates>

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    </xsl:template>
    <!-- end of template for robust in message receiver -->

</xsl:stylesheet>
