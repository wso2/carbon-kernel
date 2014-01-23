/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.wsdl.WSDLConstants;

public class MDQConstants {

    public static final String WSDL_SERVICE_QNAME = "WSDL_SERVICE_QNAME";
    public static final String WSDL_PORT = "WSDL_PORT";
    public static final String WSDL_DEFINITION = WSDLConstants.WSDL_4_J_DEFINITION;
    public static final String WSDL_LOCATION = "WSDL_LOCATION";
    public static final String SERVICE_CLASS = "ServiceClass";
    public static final String WSDL_PORTTYPE_NAME = "WSDL_PORTTYPE_NAME";
    public static final String USE_GENERATED_WSDL = "useGeneratedWSDLinJAXWS";
    public static final String USED_ANNOTATIONS_ONLY = "usedAnnotationsOnly";

    public static final String OBJECT_CLASS_NAME = "java.lang.Object";

    public static final String PROVIDER_SOURCE =
            "javax.xml.ws.Provider<javax.xml.transform.Source>";
    public static final String PROVIDER_SOAP = "javax.xml.ws.Provider<javax.xml.soap.SOAPMessage>";
    public static final String PROVIDER_DATASOURCE =
            "javax.xml.ws.Provider<javax.activation.DataSource>";
    public static final String PROVIDER_STRING = "javax.xml.ws.Provider<java.lang.String>";
    public static final String PROVIDER_OMELEMENT = "javax.xml.ws.Provider<org.apache.axiom.om.OMElement>";

    public static final String WSDL_FILE_NAME = "WSDL_FILE_NAME";
    public static final String SCHEMA_DOCS = "SCHEMA_DOCS";
    public static final String WSDL_COMPOSITE = "WSDL_COMPOSITE";

    // Java string that represents a class constructor
    public static final String CONSTRUCTOR_METHOD = "<init>";

    public static final String RETURN_TYPE_FUTURE = "java.util.concurrent.Future";
    public static final String RETURN_TYPE_RESPONSE = "javax.xml.ws.Response";
    
    public static final String CLIENT_SERVICE_CLASS = "CLIENT_SERVICE_CLASS";
    public static final String CLIENT_SEI_CLASS = "CLIENT_SEI_CLASS";
    
    public static final String HANDLER_CHAIN_DECLARING_CLASS = "HANDLER_CHAIN_DECLARING_CLASS";
    /**
     * Indicates if MTOM is enabled for specific ports (indexed by the SEI class name) under a service on the
     * client side.
     * @deprecated Replaced by SEI_FEATURES_MAP with a MTOMAnnot to indicate if MTOM is enabled.
     */
    public static final String SEI_MTOM_ENABLEMENT_MAP = "org.apache.axis2.jaxws.description.builder.SEI_MTOM_ENABLEMENT_MAP";
    /**
     * Sets the Web Service Features (as Annotation instances) for specific ports under a service on the 
     * client side.  The value associated with this property is:
     *     Map<String, List<java.lang.annotation.Annotation>> 
     * Where:
     *     String: SEI Class name (i.e. the port name)
     *     Annotation: The list of WebServiceFeatures expressed as the corresponding Annotation related to that Port
     */
    public static final String SEI_FEATURES_MAP = "org.apache.axis2.jaxws.description.builder.SEI_FEATURES_MAP";
    
    public static final String BINDING_PROPS_MAP = "org.apache.axis2.jaxws.description.builder.BINDING_PROPS_MAP";    
    
    /**
     * Property indicating a Service Reference name.  This can be used to differentiate between two occurences
     * of the same WSDL Service, for example to attach different policy configurations to each one.f
     */
    public static final String SERVICE_REF_NAME = "org.apache.axis2.jaxws.description.builder.SERVICE_REF_NAME";
    
    // Represent SOAP/JMS Bindings
    // Note that currently there is only a single namespace defined for the SOAP JMS binding in the JMS spec; there is no
    // differentiation between JMS SOAP11 or SOAP12.  For a WSDL-based client or service, the SOAP level is
    // determine by the SOAP namespace used on the binding.  For a WSDL-less client or service, there is currently
    // no way to identify SOAP11 vs SOAP12, so we will default to SOAP11.  See modules/jaxws/src/org/apache/axis2/jaxws/message/Protocol.java
    // and Jira AXIS2-4855 for more information.
    public static final String SOAP11JMS_BINDING = "http://www.w3.org/2010/soapjms/";
    public static final String SOAP12JMS_BINDING = SOAP11JMS_BINDING;
    public static final String SOAP11JMS_MTOM_BINDING = "http://www.w3.org/2010/soapjms/?mtom=true";
    public static final String SOAP12JMS_MTOM_BINDING = SOAP11JMS_MTOM_BINDING;
    public static final String SOAP_HTTP_BINDING ="SOAP_HTTP_BINDING";
    
    public static final String USE_LEGACY_WEB_METHOD_RULES_SUN = "com.sun.xml.ws.model.RuntimeModeler.legacyWebMethod";
    public static final String USE_LEGACY_WEB_METHOD_RULES = "jaxws.runtime.legacyWebMethod";
    public static final String USE_MANIFEST_LEGACY_WEB_METHOD_RULES = "LegacyWebMethod";
    public static final String SUN_WEB_METHOD_BEHAVIOR_CHANGE_VERSION = "2.1.6";

}
