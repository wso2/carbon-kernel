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

package org.apache.axis2.description.java2wsdl;

public interface Java2WSDLConstants {

    String SOAP11_PREFIX = "soap";
    String SOAP12_PREFIX = "soap12";
    String DEFAULT_WSDL_NAMESPACE_PREFIX = "wsdl";
    String DEFAULT_SCHEMA_NAMESPACE_PREFIX = "xs";
    String DEFAULT_TARGET_NAMESPACE_PREFIX = "axis2";
    String TARGETNAMESPACE_PREFIX = "tns";
    String SCHEMA_NAMESPACE_PRFIX = "ns";
    String FORM_DEFAULT_QUALIFIED = "qualified";
    String FORM_DEFAULT_UNQUALIFIED = "unqualified";
    String DOC_LIT_BARE_PARAMETER ="doclitBare";
    

    String DEFAULT_TARGET_NAMESPACE = "http://ws.apache.org/axis2";
    String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    String AXIS2_XSD = "http://org.apache.axis2/xsd";
    String URI_WSDL12_SOAP = "http://schemas.xmlsoap.org/wsdl/soap12/";
    String URI_WSDL11_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
    String TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";
    String DEFAULT_LOCATION_URL = "http://localhost:8080/axis2/services/";
    String WSAD_NS = "http://www.w3.org/2006/05/addressing/wsdl";


    String HTTP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/http/";
    String MIME_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/mime/";
    String HTTP_PREFIX = "http";
    String MIME_PREFIX = "mime";
    String URN_PREFIX = "urn";
    String COLON_SEPARATOR = ":";

    String BINDING_NAME_SUFFIX = "SOAP11Binding";
    String SOAP12BINDING_NAME_SUFFIX = "SOAP12Binding";
    String HTTP_BINDING = "HttpBinding";
    String PORT_TYPE_SUFFIX = "PortType";
    String PORT_NAME_SUFFIX = "Port";
    String MESSAGE_LOCAL_NAME = "message";
    String ATTRIBUTE_NAME = "name";
    String REQUEST_MESSAGE = "RequestMessage";
    String RESPONSE_MESSAGE = "ResponseMessage";
    String FAULT_MESSAGE = "Message";
    String MESSAGE_SUFFIX = "Request";
    String REQUEST = "Request";
    String RESPONSE = "Response";
    String RESULT = "Result";
    String PORT_TYPE_LOCAL_NAME = "portType";
    String OPERATION_LOCAL_NAME = "operation";
    String IN_PUT_LOCAL_NAME = "input";
    String OUT_PUT_LOCAL_NAME = "output";
    String SERVICE_LOCAL_NAME = "service";
    String BINDING_LOCAL_NAME = "binding";
    String PORT = "port";
    String SOAP12PORT = "SOAP12port";
    String SOAP11PORT = "SOAP11port";
    String HTTP_PORT = "Httpport";
    String PART_ATTRIBUTE_NAME = "part";
    String ELEMENT_ATTRIBUTE_NAME = "element";
    String FAULT_LOCAL_NAME = "fault";

    String SOAP_ADDRESS = "address";
    String LOCATION = "location";
    String TRANSPORT = "transport";
    String STYLE = "style";
    String SOAP_ACTION = "soapAction";
    String SOAP_BODY = "body";
    String SOAP_USE = "use";
    String DOCUMENT = "document";
    String LITERAL = "literal";
    //
    // Schema XSD Namespaces
    //
    String URI_2001_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";


    String ATTR_FORM_DEFAULT_OPTION = "afd";
    String ATTR_FORM_DEFAULT_OPTION_LONG = "attributeFormDefault";
    String CLASSNAME_OPTION = "cn";
    String CLASSNAME_OPTION_LONG = "className";
    String CLASSPATH_OPTION = "cp";
    String CLASSPATH_OPTION_LONG = "classPath";
    String DOC_LIT_BARE = "dlb";
    String DOC_LIT_BARE_LONG = "doclitbare";
    String ELEMENT_FORM_DEFAULT_OPTION = "efd";
    String ELEMENT_FORM_DEFAULT_OPTION_LONG = "elementFormDefault";
    String EXTRA_CLASSES_DEFAULT_OPTION = "xc";
    String EXTRA_CLASSES_DEFAULT_OPTION_LONG = "extraClasses";
    String JAVA_PKG_2_NSMAP_OPTION = "p2n";
    String JAVA_PKG_2_NSMAP_OPTION_LONG = "package2Namespace";
    String LOCATION_OPTION = "l";
    String LOCATION_OPTION_LONG = "location";
    String NAMESPACE_GENERATOR_OPTION = "nsg";
    String NAMESPACE_GENERATOR_OPTION_LONG = "namespaceGenerator";
    String OUTPUT_FILENAME_OPTION = "of";
    String OUTPUT_FILENAME_OPTION_LONG = "outputFilename";
    String OUTPUT_LOCATION_OPTION = "o";
    String OUTPUT_LOCATION_OPTION_LONG = "output";
    String SCHEMA_GENERATOR_OPTION = "sg";
    String SCHEMA_GENERATOR_OPTION_LONG = "schemaGenerator";
    String SCHEMA_TARGET_NAMESPACE_OPTION = "stn";
    String SCHEMA_TARGET_NAMESPACE_OPTION_LONG = "schemaTargetnamespace";
    String SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION = "stp";
    String SCHEMA_TARGET_NAMESPACE_PREFIX_OPTION_LONG = "schemaTargetnamespacePrefix";
    String SERVICE_NAME_OPTION = "sn";
    String SERVICE_NAME_OPTION_LONG = "serviceName";
    String STYLE_OPTION = "st";
    String STYLE_OPTION_LONG = "style";
    String TARGET_NAMESPACE_OPTION = "tn";
    String TARGET_NAMESPACE_OPTION_LONG = "targetNamespace";
    String TARGET_NAMESPACE_PREFIX_OPTION = "tp";
    String TARGET_NAMESPACE_PREFIX_OPTION_LONG = "targetNamespacePrefix";
    String USE_OPTION = "u";
    String USE_OPTION_LONG = "use";
    String WSDL_VERSION_OPTION = "wv";
    String WSDL_VERSION_OPTION_LONG = "wsdl-version";
    String CUSTOM_SCHEMA_LOCATION = "csl";
    String CUSTOM_SCHEMA_LOCATION_LONG = "custom-schema-location";
    String SCHEMA_MAPPING_FILE_LOCATION = "mfl";
    String SCHEMA_MAPPING_FILE_LOCATION_LONG = "mapping-file-location";
    String DISABLE_BINDING_SOAP11 = "disableSOAP11";
    String DISABLE_BINDING_SOAP12 = "disableSOAP12";
    String DISABLE_BINDING_REST = "disableREST";

    String PORT_TYPE_NAME_OPTION = "ptn";
    String PORT_TYPE_NAME_OPTION_LONG = "portTypeName";
    String SOAP11_BINDING_NAME_OPTION_LONG = "soap11BindingName";
    String SOAP12_BINDING_NAME_OPTION_LONG = "soap12BindingName";
    String REST_BINDING_NAME_OPTION_LONG = "restBindingName";

    String DISALLOW_NILLABLE_ELEMENTS_OPTION = "dne";
    String DISALLOW_NILLABLE_ELEMENTS_OPTION_LONG = "disallowNillableElements";

    String MESSAGE_PART_NAME_OPTION = "mpn";
    String MESSAGE_PART_NAME_OPTION_LONG = "messagePartName";

    String REQUEST_ELEMENT_SUFFIX_OPTION = "res";
    String REQUEST_ELEMENT_SUFFIX_OPTION_LONG = "requestElementSuffix";

    String DISALLOW_ANON_TYPES_OPTION = "dat";
    String DISALLOW_ANON_TYPES_OPTION_LONG = "disallowAnonymousTypes";

    String AXIS2_NAMESPACE_PREFIX = "ns1";

    public static final String SOLE_INPUT = "SOLE_INPUT";

    String WSDL_VERSION_2 = "2.0";
    String WSDL_VERSION_2_OPTIONAL = "2";
    String WSDL_VERSION_1 = "1.1";
    String PARAMETERS = "parameters";
}
