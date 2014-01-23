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

    <!-- import the databinding template-->
    <xsl:include href="databindsupporter"/>
    <!-- import the other templates for databinding
         Note  -  these names would be handled by a special
         URI resolver during the xslt transformations
     -->
    <xsl:include href="externalTemplate"/>
    
    
    <xsl:include href="policyExtensionTemplate"/>

    <xsl:template match="/class">
        <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
        <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
        <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
        <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
        <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
        <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
        <xsl:variable name="isbackcompatible" select="@isbackcompatible"/>
/**
 * <xsl:value-of select="@name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */
        package <xsl:value-of select="$package"/>;

        <!-- Put the MTOM enable flag -->

        /*
        *  <xsl:value-of select="@name"/> java implementation
        */

        <xsl:variable name="fullyQualifiedClassName"><xsl:value-of select="$package"/>.<xsl:value-of select="@name"/></xsl:variable>
        public class <xsl:value-of select="@name"/> extends org.apache.axis2.client.Stub
        <xsl:if test="not(@wrapped)">implements <xsl:value-of select="$interfaceName"/></xsl:if>{
        protected org.apache.axis2.description.AxisOperation[] _operations;

        //hashmaps to keep the fault mapping
        private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
        private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
        private java.util.HashMap faultMessageMap = new java.util.HashMap();

        private static int counter = 0;

        private static synchronized java.lang.String getUniqueSuffix(){
            // reset the counter if it is greater than 99999
            if (counter > 99999){
                counter = 0;
            }
            counter = counter + 1; 
            return java.lang.Long.toString(java.lang.System.currentTimeMillis()) + "_" + counter;
        }

    
    private void populateAxisService() throws org.apache.axis2.AxisFault {

     //creating the Service with a unique name
     _service = new org.apache.axis2.description.AxisService("<xsl:value-of select="@servicename"/>" + getUniqueSuffix());
     addAnonymousOperations();

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[<xsl:value-of select="count(method)"/>];
        <xsl:for-each select="method">
            <xsl:choose>
                <xsl:when test="@mep='10'">
                    __operation = new org.apache.axis2.description.OutOnlyAxisOperation();
                </xsl:when>
                <xsl:when test="@mep='11'">
                    __operation = new org.apache.axis2.description.RobustOutOnlyAxisOperation();
                </xsl:when>
                <xsl:otherwise>
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                </xsl:otherwise>
            </xsl:choose>

            __operation.setName(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>", "<xsl:value-of select="@name"/>"));
	    _service.addOperation(__operation);
	    

	    <xsl:if test="input/@policy">
	    (__operation).getMessage(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getPolicySubject().attachPolicy(getPolicy("<xsl:value-of select="input/@policy"/>"));
	    </xsl:if>
	    
	    <xsl:if test="output/@policy">
	    (__operation).getMessage(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE).getPolicySubject().attachPolicy(getPolicy("<xsl:value-of select="output/@policy"/>"));
	    </xsl:if>
	    
            _operations[<xsl:value-of select="position()-1"/>]=__operation;
            
        </xsl:for-each>
        }

    //populates the faults
    private void populateFaults(){
         <xsl:for-each select="method">
           <xsl:for-each select="fault/param">
              faultExceptionNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>","<xsl:value-of select="@localname"/>"), "<xsl:value-of select="@operationName"/>"),"<xsl:value-of select="@name"/>");
              faultExceptionClassNameMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>","<xsl:value-of select="@localname"/>"), "<xsl:value-of select="@operationName"/>"),"<xsl:value-of select="@name"/>");
              faultMessageMap.put(new org.apache.axis2.client.FaultMapKey(new javax.xml.namespace.QName("<xsl:value-of select="@namespace"/>","<xsl:value-of select="@localname"/>"), "<xsl:value-of select="@operationName"/>"),"<xsl:value-of select="@instantiatableType"/>");
           </xsl:for-each>
        </xsl:for-each>


    }

    /**
      *Constructor that takes in a configContext
      */

    public <xsl:value-of select="@name"/>(org.apache.axis2.context.ConfigurationContext configurationContext,
       java.lang.String targetEndpoint)
       throws org.apache.axis2.AxisFault {
         this(configurationContext,targetEndpoint,false);
   }


   /**
     * Constructor that takes in a configContext  and useseperate listner
     */
   public <xsl:value-of select="@name"/>(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint, boolean useSeparateListener)
        throws org.apache.axis2.AxisFault {
         //To populate AxisService
         populateAxisService();
         populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);
        <xsl:if test="//@policy">
        _service.applyPolicy();
        </xsl:if>
	
        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
        <xsl:if test="starts-with($soapVersion,'http://www.w3.org/2003/05/soap-envelope')">
            //Set the soap version
            _serviceClient.getOptions().setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        </xsl:if>
    
    }

    /**
     * Default Constructor
     */
    public <xsl:value-of select="@name"/>(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {
        <xsl:for-each select="endpoint">
            <xsl:choose>
                <xsl:when test="position()=1">
                    this(configurationContext,"<xsl:value-of select="."/>" );
                </xsl:when>
                <xsl:otherwise>
                    //this(configurationContext,"<xsl:value-of select="."/>" );
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    }

    /**
     * Default Constructor
     */
    public <xsl:value-of select="@name"/>() throws org.apache.axis2.AxisFault {
        <xsl:for-each select="endpoint">
            <xsl:choose>
                <xsl:when test="position()=1">
                    this("<xsl:value-of select="."/>" );
                </xsl:when>
                <xsl:otherwise>
                    //this("<xsl:value-of select="."/>" );
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    }

    /**
     * Constructor taking the target endpoint
     */
    public <xsl:value-of select="@name"/>(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }



        <xsl:for-each select="method">
                        
          <!-- If usedbmethod attribute present, gives name of method to call for implementation -->
          <xsl:variable name="usedbimpl"><xsl:value-of select="@usdbimpl"/></xsl:variable>
          <xsl:if test="$usedbimpl!='true'">
          
            <xsl:variable name="outputtype"><xsl:value-of select="output/param[@location='body']/@type"/></xsl:variable>
            <xsl:variable name="outputcomplextype"><xsl:value-of select="output/param[@location='body']/@complextype"/></xsl:variable>
            <xsl:variable name="outputopname"><xsl:value-of select="output/param[@location='body']/@opname"/></xsl:variable>
            <xsl:variable name="style"><xsl:value-of select="@style"></xsl:value-of></xsl:variable>
            <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>
            <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
            <xsl:variable name="outputparamcount"><xsl:value-of select="count(output/param[@location='body']/param)"/></xsl:variable>
            <xsl:variable name="outputparamshorttype"><xsl:value-of select="output/param[@location='body']/@shorttype"/></xsl:variable>
            <xsl:variable name="outputparampartname"><xsl:value-of select="output/param[@location='body']/param/@partname"/></xsl:variable>
            <xsl:variable name="isUnwrapParameters" select="input/param[@location='body' and @type!='']/@unwrappParameters"/>
        <!-- MTOM -->
        <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>
        <!-- MTOM -->

            <!-- Code generation for the in-out mep -->
            <xsl:if test="$mep='12'">  <!-- These constants can be found in org.apache.axis2.wsdl.WSDLConstants -->
                <xsl:if test="$isSync='1'">
                    /**
                     * Auto generated method signature
                     * <xsl:value-of select="@comment"/>
                     * @see <xsl:value-of select="$package"/>.<xsl:value-of select="$interfaceName"/>#<xsl:value-of select="@name"/>
                    <xsl:for-each select="input/param[@type!='']">
                     * @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
                    </xsl:text></xsl:for-each>
                    <xsl:for-each select="fault/param[@type!='']">
                     * @throws <xsl:value-of select="@name"/> : <xsl:value-of select="@comment"/>
                    </xsl:for-each>
                     */

                    <xsl:choose>
                        <!-- if -b flag is on then we have to unwrap the request and response messages. -->
                        <xsl:when test="$isbackcompatible = 'true'">
                            public <xsl:choose><xsl:when test="$outputtype=''">void</xsl:when>
                            <xsl:when test="string-length(normalize-space($outputcomplextype)) > 0"><xsl:value-of select="$outputcomplextype"/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise></xsl:choose>
                            <xsl:text> </xsl:text><xsl:value-of select="@name"/>(

                            <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                            <xsl:choose>
                                <xsl:when test="$inputcount=1">
                                    <xsl:variable name="inputComplexType" select="input/param[@location='body' and @type!='']/@complextype"/>
                                    <xsl:choose>
                                        <xsl:when test="string-length(normalize-space($inputComplexType)) > 0">
                                           <xsl:value-of select="$inputComplexType"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise><!-- Just leave it - nothing we can do here --></xsl:otherwise>
                            </xsl:choose>

                            <xsl:if test="$inputcount=1 and input/param[not(@location='body') and @type!='']">,</xsl:if>
                            <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                                <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                            </xsl:for-each>)
                        </xsl:when>
                        <xsl:otherwise>

                            public  <xsl:choose>
                            <xsl:when test="$outputtype=''">void</xsl:when>
                            <xsl:when test="$outputparamcount=1"><xsl:value-of select="output/param[@location='body']/param/@type"/></xsl:when>
                            <xsl:when test="string-length(normalize-space($outputcomplextype)) > 0"><xsl:value-of select="$outputcomplextype"/></xsl:when>
                            <xsl:when test="($outputparamcount=0) and ($isUnwrapParameters)">void</xsl:when>
                            <xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise>
                            </xsl:choose>
                            <xsl:text> </xsl:text><xsl:value-of select="@name"/>(

                            <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                            <xsl:variable name="inputParamCount" select="count(input/param[@location='body' and @type!='']/param)"/>

                            <xsl:choose>
                                <xsl:when test="$inputcount=1">
                                    <!-- Even when the parameters are 1 we have to see whether we have the
                                  wrapped parameters -->
                                    <xsl:choose>
                                        <xsl:when test="$isUnwrapParameters">
                                           <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                                <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise><!-- Just leave it - nothing we can do here --></xsl:otherwise>
                            </xsl:choose>

                            <xsl:if test="($inputcount=1 and input/param[not(@location='body') and @type!='']) and
                                not($isUnwrapParameters and $inputParamCount=0)">,</xsl:if>
                            <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                                <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                            </xsl:for-each>)
                        </xsl:otherwise>
                    </xsl:choose>

                    throws java.rmi.RemoteException
                    <!--add the faults-->
                    <xsl:for-each select="fault/param[@type!='']">
                        ,<xsl:value-of select="@name"/>
                    </xsl:for-each>{
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[<xsl:value-of select="position()-1"/>].getName());
              _operationClient.getOptions().setAction("<xsl:value-of select="$soapAction"/>");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              <!-- add the other parameter options to operational client -->
              <xsl:for-each select="optionParam">
                  addPropertyToOperationClient(_operationClient,<xsl:value-of select="@name"/>,<xsl:value-of select="@value"/>);
              </xsl:for-each>

              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();

              <!--todo if the stub was generated with unwrapping, wrap all parameters into a single element-->

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    <xsl:variable name="count" select="count(input/param[@type!=''])"/>
                    <xsl:choose>
                        <!-- test the number of input parameters
                        If the number of parameter is more then just run the normal test-->
                        <xsl:when test="$count &gt; 0">
                            <xsl:choose>
                                <!-- style being doclit or rpc does not matter -->
                                <xsl:when test="$style='rpc' or $style='document'">
                                    <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                                    <xsl:choose>
                                        <xsl:when test="$inputcount=1">
                                            <!-- Even when the parameters are 1 we have to see whether we have the
                                                wrapped parameters -->
                                            <!-- unwrapping takes place only if the back word compatiblity is off. if -b on
                                             then we do not unwrapp and only remove the top element -->
                                           <xsl:variable name="inputElementType" select="input/param[@location='body' and @type!='']/@type"></xsl:variable>
                                           <xsl:variable name="inputElementComplexType" select="input/param[@location='body' and @type!='']/@complextype"></xsl:variable>
                                           <xsl:variable name="opName" select="input/param[@location='body' and @type!='']/@opname"></xsl:variable>

                                            <xsl:choose>
                                                <xsl:when test="(($isbackcompatible='true') and (string-length(normalize-space($inputElementComplexType)) > 0))">
                                                     <!-- there are no unwrapped parameters - go ahead and use the normal wrapped codegen-->
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    wrap<xsl:value-of select="$opName"/>(<xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>),
                                                    optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                    "<xsl:value-of select="$method-name"/>")));
                                                </xsl:when>
                                                <xsl:when test="($isUnwrapParameters) and not($isbackcompatible='true')">
                                                    <xsl:value-of select="$inputElementType"/><xsl:text> </xsl:text>dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                                        <xsl:value-of select="@name"/>,
                                                    </xsl:for-each>dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                    "<xsl:value-of select="$method-name"/>")));
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <!-- there are no unwrapped parameters - go ahead and use the normal wrapped codegen-->
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>,
                                                    optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                    "<xsl:value-of select="$method-name"/>")), new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                    "<xsl:value-of select="$method-name"/>"));
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                              env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()));
                                        </xsl:otherwise>
                                    </xsl:choose>

                                   <xsl:if test="count(input/param[@location='soap_header']) &gt; 0">
                                               env.build();
                                    </xsl:if>
                                    <xsl:for-each select="input/param[@location='soap_header']">
                                        // add the children only if the parameter is not null
                                        if (<xsl:value-of select="@name"/>!=null){
                                            <xsl:choose>
                                                <xsl:when test="@mustUnderstand = 'true'">
                                                    org.apache.axiom.om.OMElement omElement<xsl:value-of select="@name"/> = toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                                    addHeader(omElement<xsl:value-of select="@name"/>,env,true);
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    org.apache.axiom.om.OMElement omElement<xsl:value-of select="@name"/> = toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                                    addHeader(omElement<xsl:value-of select="@name"/>,env);
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        }
                                    </xsl:for-each>

                                    <xsl:for-each select="input/param[@location='http_header']">
                                        // add the children only if the parameter is not null
                                        if (<xsl:value-of select="@name"/>!=null){
                                            addHttpHeader(_messageContext,"<xsl:value-of select="@headername"/>",<xsl:value-of select="@name"/>);
                                        }
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    //Unknown style detected !! No code is generated
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <!-- No input parameters present. So generate assuming no input parameters-->
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="$style='rpc'">
                                    //Style is taken to be "rpc". No input parameters
                                    org.apache.axiom.soap.SOAPFactory factory = getFactory(_operationClient.getOptions().getSoapVersionURI());
                                    env = factory.getDefaultEnvelope();
                                    env.getBody().addChild(factory.createOMElement("<xsl:value-of select="$method-name"/>", "<xsl:value-of select="$method-ns"/>", ""));
                                </xsl:when>
                                <xsl:when test="$style='document'">
                                    //Style is taken to be "document". No input parameters
                                    // according to the WS-Basic profile in this case we have to send an empty soap message
                                    org.apache.axiom.soap.SOAPFactory factory = getFactory(_operationClient.getOptions().getSoapVersionURI());
                                    env = factory.getDefaultEnvelope();
                                </xsl:when>
                                <xsl:otherwise>
                                     //Unknown style detected !! No code is generated
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         <xsl:choose>
            <xsl:when test="$outputtype=''">
                return;
            </xsl:when>
            <xsl:otherwise>
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                <!-- todo need to change this to cater for unwrapped messages (multiple parts) -->
                <xsl:choose>
                    <xsl:when test="$style='document' or $style='rpc'">
                        <xsl:choose>
                            <xsl:when test="$outputtype='byte'">
                                return toByte(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>
                            <xsl:when test="$outputtype='char'">
                                return toChar(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>
                            <xsl:when test="$outputtype='double'">
                                return toDouble(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>
                            <xsl:when test="$outputtype='float'">
                                return toFloat(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>
                            <xsl:when test="$outputtype='int'">
                                return toInt(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>
                            <xsl:when test="$outputtype='long'">
                                return toLong(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>
                            <xsl:when test="$outputtype='short'">
                                return toShort(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>
                            <xsl:when test="$outputtype='boolean'">
                                return toBoolean(_returnEnv.getBody().getFirstElement(),
                                                                 getEnvelopeNamespaces(_returnEnv));
                            </xsl:when>                            
                            <xsl:otherwise>
                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             <xsl:value-of select="$outputtype"/>.class,
                                              getEnvelopeNamespaces(_returnEnv));

                               <xsl:choose>
                                   <xsl:when test="$outputparamcount=1">
                                        return get<xsl:value-of select="$outputparamshorttype"/><xsl:value-of
                                           select="$outputparampartname"/>((<xsl:value-of select="$outputtype"/>)object);
                                   </xsl:when>
                                   <!-- this covers both back compatibility and normal unwrapping -->
                                   <xsl:when test="(string-length(normalize-space($outputcomplextype)) > 0)">
                                        return get<xsl:value-of select="$outputopname"/>((<xsl:value-of select="$outputtype"/>)object);
                                   </xsl:when>
                                   <xsl:when test="($outputparamcount=0) and ($isUnwrapParameters)">
                                        return;
                                   </xsl:when>
                                   <xsl:otherwise>
                                        return (<xsl:value-of select="$outputtype"/>)object;
                                   </xsl:otherwise>
                               </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                         //Unknown style detected !! No code is generated
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"))){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"));
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"));
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        <xsl:for-each select="fault/param">
                        if (ex instanceof <xsl:value-of select="@name"/>){
                          throw (<xsl:value-of select="@name"/>)ex;
                        }
                        </xsl:for-each>

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
            }
        }
            </xsl:if>
            <!-- Async method generation -->
            <xsl:if test="$isAsync='1'">
                /**
                * Auto generated method signature for Asynchronous Invocations
                * <xsl:value-of select="@comment"/>
                * @see <xsl:value-of select="$package"/>.<xsl:value-of select="$interfaceName"/>#start<xsl:value-of select="@name"/>
                <xsl:for-each select="input/param[@type!='']">
                    * @param <xsl:value-of select="@name"></xsl:value-of><xsl:text>
                </xsl:text></xsl:for-each>
                */
                public  void start<xsl:value-of select="@name"/>(

                 <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                 <xsl:variable name="inputWrappedCount" select="count(input/param[@location='body' and @type!='']/param)"/>

                    <xsl:choose>
                        <xsl:when test="$inputcount=1">
                            <!-- Even when the parameters are 1 we have to see whether we have the
                          wrapped parameters -->
                                                       <xsl:choose>
                                <xsl:when test="$isUnwrapParameters">
                                   <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                        <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise><!-- Just leave it - nothing we can do here --></xsl:otherwise>
                    </xsl:choose>                                                
                    <xsl:if test="($inputcount=1) and not($isUnwrapParameters and $inputWrappedCount=0)">,</xsl:if>
                    <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                       <xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>,
                    </xsl:for-each>

                  final <xsl:value-of select="$package"/>.<xsl:value-of select="$callbackname"/> callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[<xsl:value-of select="position()-1"/>].getName());
             _operationClient.getOptions().setAction("<xsl:value-of select="$soapAction"/>");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              <!-- add the other parameter options to operational client -->
              <xsl:for-each select="optionParam">
                  addPropertyToOperationClient(_operationClient,<xsl:value-of select="@name"/>,<xsl:value-of select="@value"/>);
              </xsl:for-each>


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    <xsl:variable name="count" select="count(input/param[@type!=''])"/>
                    <xsl:choose>
                        <!-- test the number of input parameters
                        If the number of parameter is more then just run the normal test-->
                        <xsl:when test="$count &gt; 0">
                            <xsl:choose>
                                <xsl:when test="$style='document' or $style='rpc'">
                                    //Style is Doc.
                                    <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                                    <xsl:choose>
                                        <xsl:when test="$inputcount=1">
                                            <!-- Even when the parameters are 1 we have to see whether we have the
                                                wrapped parameters -->
                                            <xsl:variable name="inputElementType" select="input/param[@location='body' and @type!='']/@type"></xsl:variable>

                                            <xsl:choose>
                                                <xsl:when test="$isUnwrapParameters">
                                                    <xsl:value-of select="$inputElementType"/><xsl:text> </xsl:text>dummyWrappedType = null;
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                                        <xsl:value-of select="@name"/>,
                                                    </xsl:for-each> dummyWrappedType,
                                                    optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                    "<xsl:value-of select="$method-name"/>")));
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <!-- there are no unwrapped parameters - go ahead and use the normal wrapped codegen-->
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>,
                                                    optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                    "<xsl:value-of select="$method-name"/>")), new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                    "<xsl:value-of select="$method-name"/>"));
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                              env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()));
                                        </xsl:otherwise>
                                    </xsl:choose>

                                    <xsl:for-each select="input/param[@location='soap_header']">
                                         // add the soap_headers only if they are not null
                                        if (<xsl:value-of select="@name"/>!=null){
                                           <xsl:choose>
                                                <xsl:when test="@mustUnderstand = 'true'">
                                                    org.apache.axiom.om.OMElement omElement<xsl:value-of select="@name"/> = toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                                    addHeader(omElement<xsl:value-of select="@name"/>,env,true);
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    org.apache.axiom.om.OMElement omElement<xsl:value-of select="@name"/> = toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                                    addHeader(omElement<xsl:value-of select="@name"/>,env);
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        }
                                    </xsl:for-each>
                                     <xsl:for-each select="input/param[@location='http_header']">
                                        // add the children only if the parameter is not null
                                        if (<xsl:value-of select="@name"/>!=null){
                                            addHttpHeader(_messageContext,"<xsl:value-of select="@headername"/>",<xsl:value-of select="@name"/>);
                                        }
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    //Unknown style detected !! No code is generated
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <!-- No input parameters present. So generate assuming no input parameters-->
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="$style='rpc'">
                                    //Style is Doc. No input parameters
                                    org.apache.axiom.soap.SOAPFactory factory = getFactory(_operationClient.getOptions().getSoapVersionURI());
                                    env = factory.getDefaultEnvelope();
                                    env.getBody().addChild(factory.createOMElement("<xsl:value-of select="$method-name"/>", "<xsl:value-of select="$method-ns"/>", ""));
                                </xsl:when>
                                <xsl:when test="$style='document'">
                                    //Style is taken to be "document". No input parameters
                                    // according to the WS-Basic profile in this case we have to send an empty soap message
                                    org.apache.axiom.soap.SOAPFactory factory = getFactory(_operationClient.getOptions().getSoapVersionURI());
                                    env = factory.getDefaultEnvelope();
                                </xsl:when>
                                <xsl:otherwise>
                                    //Unknown style detected !! No code is generated
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    <xsl:choose>
                        <xsl:when test="$outputtype=''">
                            // Nothing to pass as the callback!!!
                        </xsl:when>
                        <xsl:otherwise>
                        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                            try {
                                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                <xsl:choose>
                                    <xsl:when test="$outputtype='byte'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toByte(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>
                                    <xsl:when test="$outputtype='char'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toChar(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>
                                    <xsl:when test="$outputtype='double'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toDouble(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>
                                    <xsl:when test="$outputtype='float'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toFloat(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>
                                    <xsl:when test="$outputtype='int'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toInt(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>
                                    <xsl:when test="$outputtype='long'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toLong(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>
                                    <xsl:when test="$outputtype='short'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toShort(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>
                                    <xsl:when test="$outputtype='boolean'">
                                        callback.receiveResult<xsl:value-of select="@name"/>(toBoolean(resultEnv.getBody().getFirstElement(),
                                                                         getEnvelopeNamespaces(resultEnv)));
                                    </xsl:when>                                    
                                    <xsl:otherwise>
                                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                         <xsl:value-of select="$outputtype"/>.class,
                                                                         getEnvelopeNamespaces(resultEnv));
                                        callback.receiveResult<xsl:value-of select="@name"/>(<xsl:choose>
                                        <xsl:when test="$outputtype=''">);</xsl:when>
                                        <xsl:when test="$outputparamcount=1">
                                            get<xsl:value-of select="$outputparamshorttype"/><xsl:value-of
                                              select="$outputparampartname"/>((<xsl:value-of select="$outputtype"/>)object));
                                        </xsl:when>
                                        <xsl:when test="string-length(normalize-space($outputcomplextype)) > 0">
                                            (<xsl:value-of select="$outputcomplextype"/>)object);
                                        </xsl:when>
                                        <xsl:when test="($outputparamcount=0) and ($isUnwrapParameters)">);</xsl:when>
                                        <xsl:otherwise>
                                        (<xsl:value-of select="$outputtype"/>)object);
                                        </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:otherwise>
                            </xsl:choose>
                            } catch (org.apache.axis2.AxisFault e) {
                                callback.receiveError<xsl:value-of select="@name"/>(e);
                            }
                            }

                            public void onError(java.lang.Exception error) {
								if (error instanceof org.apache.axis2.AxisFault) {
									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
									org.apache.axiom.om.OMElement faultElt = f.getDetail();
									if (faultElt!=null){
										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"))){
											//make the fault by reflection
											try{
													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"));
													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
													java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
													//message class
													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"));
														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
															new java.lang.Class[]{messageClass});
													m.invoke(ex,new java.lang.Object[]{messageObject});
													<xsl:for-each select="fault/param">
													if (ex instanceof <xsl:value-of select="@name"/>){
														callback.receiveError<xsl:value-of select="$method-name"/>((<xsl:value-of select="@name"/>)ex);
											            return;
										            }
										            </xsl:for-each>
					
										            callback.receiveError<xsl:value-of select="@name"/>(new java.rmi.RemoteException(ex.getMessage(), ex));
                                            } catch(java.lang.ClassCastException e){
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveError<xsl:value-of select="@name"/>(f);
                                            } catch (java.lang.ClassNotFoundException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveError<xsl:value-of select="@name"/>(f);
                                            } catch (java.lang.NoSuchMethodException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveError<xsl:value-of select="@name"/>(f);
                                            } catch (java.lang.reflect.InvocationTargetException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveError<xsl:value-of select="@name"/>(f);
                                            } catch (java.lang.IllegalAccessException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveError<xsl:value-of select="@name"/>(f);
                                            } catch (java.lang.InstantiationException e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveError<xsl:value-of select="@name"/>(f);
                                            } catch (org.apache.axis2.AxisFault e) {
                                                // we cannot intantiate the class - throw the original Axis fault
                                                callback.receiveError<xsl:value-of select="@name"/>(f);
                                            }
									    } else {
										    callback.receiveError<xsl:value-of select="@name"/>(f);
									    }
									} else {
									    callback.receiveError<xsl:value-of select="@name"/>(f);
									}
								} else {
								    callback.receiveError<xsl:value-of select="@name"/>(error);
								}
                            }

                            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                onError(fault);
                            }

                            public void onComplete() {
                                try {
                                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                } catch (org.apache.axis2.AxisFault axisFault) {
                                    callback.receiveError<xsl:value-of select="@name"/>(axisFault);
                                }
                            }
                });
                        </xsl:otherwise>
                    </xsl:choose>

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[<xsl:value-of select="position()-1"/>].getMessageReceiver()==null &amp;&amp;  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[<xsl:value-of select="position()-1"/>].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                </xsl:if>
                <!-- End of in-out mep -->
            </xsl:if>




            <!-- Start of in only mep-->
            <xsl:if test="$mep='10' or $mep='11'"> <!-- These constants can be found in org.apache.axis2.wsdl.WSDLConstants -->
                <!-- for the in only mep there is no notion of sync or async. And there is no return type also -->
                /**
                  * Auto generated method signature
                  * <xsl:value-of select="@comment"/>
                 <xsl:if test="$mep='11'">
                   <xsl:for-each select="fault/param[@type!='']">
                     * @throws <xsl:value-of select="@name"/> : <xsl:value-of select="@comment"/>
                    </xsl:for-each>
                  </xsl:if>
                  */
                public void <xsl:text> </xsl:text><xsl:value-of select="@name"/>(
                 <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                 <xsl:variable name="inputParamCount" select="count(input/param[@location='body' and @type!='']/param)"/>
                
                    <xsl:choose>
                        <xsl:when test="$inputcount=1">
                            <!-- Even when the parameters are 1 we have to see whether we have the
                          wrapped parameters -->
                            <xsl:choose>
                                <xsl:when test="$isUnwrapParameters">
                                   <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                        <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@type"/><xsl:text> </xsl:text><xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise><!-- Just leave it - nothing we can do here --></xsl:otherwise>
                    </xsl:choose>

                   <xsl:if test="($inputcount=1 and input/param[not(@location='body') and @type!='']) and
                                not($isUnwrapParameters and $inputParamCount=0)">,</xsl:if>
                    <xsl:for-each select="input/param[not(@location='body') and @type!='']">
                        <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="@type"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                    </xsl:for-each>

                ) throws java.rmi.RemoteException
                <!--add the faults-->
                <xsl:if test="$mep='11'">
                    <xsl:for-each select="fault/param[@type!='']">
                        ,<xsl:value-of select="@name"/>
                    </xsl:for-each>
                </xsl:if>
                {
                org.apache.axis2.context.MessageContext _messageContext = null;

                <xsl:if test="$mep='11'">try {</xsl:if>
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[<xsl:value-of select="position()-1"/>].getName());
                _operationClient.getOptions().setAction("<xsl:value-of select="$soapAction"/>");
                _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

                <!-- add the other parameter options to operational client -->
              <xsl:for-each select="optionParam">
                  addPropertyToOperationClient(_operationClient,<xsl:value-of select="@name"/>,<xsl:value-of select="@value"/>);
              </xsl:for-each>

                <xsl:for-each select="input/param[@Action!='']">_operationClient.getOptions().setAction("<xsl:value-of select="@Action"/>");</xsl:for-each>
                org.apache.axiom.soap.SOAPEnvelope env = null;
                 _messageContext = new org.apache.axis2.context.MessageContext();

                <xsl:variable name="count" select="count(input/param[@type!=''])"/>
                                    <xsl:choose>
                                        <!-- test the number of input parameters
                                        If the number of parameter is more then just run the normal test-->
                                        <xsl:when test="$count &gt; 0">
                                            <xsl:choose>
                                                <!-- style being doclit or rpc does not matter -->
                                                <xsl:when test="$style='rpc' or $style='document'">
                                                    //Style is Doc.
                                                    <xsl:variable name="inputcount" select="count(input/param[@location='body' and @type!=''])"/>
                                                    <xsl:choose>
                                                        <xsl:when test="$inputcount=1">
                                                            <!-- Even when the parameters are 1 we have to see whether we have the
                                                                wrapped parameters -->
                                                            <xsl:variable name="inputElementType" select="input/param[@location='body' and @type!='']/@type"></xsl:variable>

                                                            <xsl:choose>
                                                                <xsl:when test="$isUnwrapParameters">
                                                                    <xsl:value-of select="$inputElementType"/><xsl:text> </xsl:text>dummyWrappedType = null;
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    <xsl:for-each select="input/param[@location='body' and @type!='']/param">
                                                                        <xsl:value-of select="@name"/>,
                                                                    </xsl:for-each>dummyWrappedType,
                                                                    optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                                    "<xsl:value-of select="$method-name"/>")));
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <!-- there are no unwrapped parameters - go ahead and use the normal wrapped codegen-->
                                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                    <xsl:value-of select="input/param[@location='body' and @type!='']/@name"/>,
                                                                    optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                                    "<xsl:value-of select="$method-name"/>")),new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>",
                                                                    "<xsl:value-of select="$method-name"/>"));
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                              env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()));
                                                        </xsl:otherwise>
                                                  </xsl:choose>

                                                    <xsl:for-each select="input/param[@location='soap_header']">
                                                        // add the children only if the parameter is not null
                                                        if (<xsl:value-of select="@name"/>!=null){
                                                            <xsl:choose>
                                                                <xsl:when test="@mustUnderstand = 'true'">
                                                                    org.apache.axiom.om.OMElement omElement<xsl:value-of select="@name"/> = toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                                                    addHeader(omElement<xsl:value-of select="@name"/>,env,true);
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    org.apache.axiom.om.OMElement omElement<xsl:value-of select="@name"/> = toOM(<xsl:value-of select="@name"/>, optimizeContent(new javax.xml.namespace.QName("<xsl:value-of select="$method-ns"/>", "<xsl:value-of select="$method-name"/>")));
                                                                    addHeader(omElement<xsl:value-of select="@name"/>,env);
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        }
                                                    </xsl:for-each>
                                                     <xsl:for-each select="input/param[@location='http_header']">
                                                        // add the children only if the parameter is not null
                                                        if (<xsl:value-of select="@name"/>!=null){
                                                            addHttpHeader(_messageContext,"<xsl:value-of select="@headername"/>",<xsl:value-of select="@name"/>);
                                                        }
                                                    </xsl:for-each>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    //Unknown style detected !! No code is generated
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <!-- No input parameters present. So generate assuming no input parameters-->
                                        <xsl:otherwise>
                                            <xsl:choose>
                                                <xsl:when test="$style='rpc'">
                                                    //Style is taken to be "document". No input parameters
                                                    org.apache.axiom.soap.SOAPFactory factory = getFactory(_operationClient.getOptions().getSoapVersionURI());
                                                    env = factory.getDefaultEnvelope();
                                                    env.getBody().addChild(factory.createOMElement("<xsl:value-of select="$method-name"/>", "<xsl:value-of select="$method-ns"/>", ""));
                                                </xsl:when>
                                                <xsl:when test="$style='document'">
                                                    //Style is taken to be "document". No input parameters
                                                    // according to the WS-Basic profile in this case we have to send an empty soap message
                                                    org.apache.axiom.soap.SOAPFactory factory = getFactory(_operationClient.getOptions().getSoapVersionURI());
                                                    env = factory.getDefaultEnvelope();
                                                </xsl:when>
                                                <xsl:otherwise>
                                                     //Unknown style detected !! No code is generated
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:otherwise>
                                    </xsl:choose>

              //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
                // create message context with that soap envelope

            _messageContext.setEnvelope(env);

            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);

             _operationClient.execute(true);

           <xsl:if test="$mep='11'">
               }catch(org.apache.axis2.AxisFault f){
                  org.apache.axiom.om.OMElement faultElt = f.getDetail();
                  if (faultElt!=null){
                      if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"))){
                          //make the fault by reflection
                          try{
                              java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"));
                              java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                              java.lang.Exception ex=
                                      (java.lang.Exception) exceptionClass.newInstance();
                              //message class
                              java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"<xsl:value-of select="@originalName"/>"));
                              java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                              java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                              java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                         new java.lang.Class[]{messageClass});
                              m.invoke(ex,new java.lang.Object[]{messageObject});
                              <xsl:for-each select="fault/param">
                              if (ex instanceof <xsl:value-of select="@name"/>){
                                throw (<xsl:value-of select="@name"/>)ex;
                              }
                              </xsl:for-each>

                              throw new java.rmi.RemoteException(ex.getMessage(), ex);
                          }catch(java.lang.ClassCastException e){
                             // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          } catch (java.lang.ClassNotFoundException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }catch (java.lang.NoSuchMethodException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          } catch (java.lang.reflect.InvocationTargetException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }  catch (java.lang.IllegalAccessException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }   catch (java.lang.InstantiationException e) {
                              // we cannot intantiate the class - throw the original Axis fault
                              throw f;
                          }
                      }else{
                          throw f;
                      }
                  }else{
                      throw f;
                  }
              } finally {
                if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                }
              }
           </xsl:if>
           <xsl:if test="not($mep='11')">
              if (_messageContext.getTransportOut() != null) {
                      _messageContext.getTransportOut().getSender().cleanup(_messageContext);
              }
           </xsl:if>
             return;
           }
            </xsl:if>
          </xsl:if>
        </xsl:for-each>


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

    <xsl:if test="//@policy">
    ////////////////////////////////////////////////////////////////////////
    
    private static org.apache.neethi.Policy getPolicy (java.lang.String policyString) {
    	java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(policyString.getBytes());
    	return org.apache.neethi.PolicyEngine.getPolicy(bais);
    }
    
    /////////////////////////////////////////////////////////////////////////

    </xsl:if>
    
    private javax.xml.namespace.QName[] opNameArray = null;
    private boolean optimizeContent(javax.xml.namespace.QName opName) {
        <xsl:if test="stubMethods">
            setOpNameArray();
        </xsl:if>

        if (opNameArray == null) {
            return false;
        }
        for (int i = 0; i &lt; opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;   
            }
        }
        return false;
    }
     //<xsl:apply-templates><xsl:with-param name="context">interface-implementation</xsl:with-param></xsl:apply-templates>
   }
   </xsl:template>
</xsl:stylesheet>
