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

package org.apache.axis2.description;

public interface WSDL2Constants {

    String WSDL_NAMESPACE = "http://www.w3.org/ns/wsdl";
    String DEFAULT_WSDL_NAMESPACE_PREFIX = "wsdl2";
    String DESCRIPTION = "description";
    String URI_WSDL2_SOAP = "http://www.w3.org/ns/wsdl/soap";
    String URI_WSDL2_HTTP = "http://www.w3.org/ns/wsdl/http";
    String URI_WSDL2_EXTENSIONS = "http://www.w3.org/ns/wsdl-extensions";
    String URI_WSDL2_RPC = "http://www.w3.org/ns/wsdl/rpc";
    String SOAP_PREFIX = "wsoap";
    String HTTP_PREFIX = "whttp";
    String WSDL_EXTENTION_PREFIX = "wsdlx";
    String WSDL_RPC_PREFIX = "wrpc";
    String SOAP_ENV_PREFIX = "soap";
    String DEFAULT_TARGET_NAMESPACE_PREFIX = "axis2";
    String DOCUMENTATION = "documentation";

    String DEFAULT_SOAP11_ENDPOINT_NAME = "SOAP11Endpoint";
    String DEFAULT_SOAP12_ENDPOINT_NAME = "SOAP12Endpoint";
    String DEFAULT_HTTP_ENDPOINT_NAME = "HTTPEndpoint";
    String DEFAULT_HTTPS_PREFIX = "Secure";
    String DEFAULT_INTERFACE_NAME = "ServiceInterface";
    String TYPES_LOCAL_NALE = "types";
    String INTERFACE_LOCAL_NAME = "interface";
    String INTERFACE_PREFIX = "Interface";
    String OPERATION_LOCAL_NAME = "operation";
    String ATTRIBUTE_NAME = "name";
    String TARGET_NAMESPACE= "targetNamespace";
    String ATTRIBUTE_REF = "ref";
    String ATTRIBUTE_LOCATION = "location";
    String ATTRIBUTE_CONTENT_ENCODING_DEFAULT = "contentEncodingDefault";
    String ATTRIBUTE_CONTENT_ENCODING = "contentEncoding";
    String ATTRIBUTE_QUERY_PARAMETER_SEPERATOR = "queryParameterSeparator";
    String ATTRIBUTE_QUERY_PARAMETER_SEPERATOR_DEFAULT = "queryParameterSeparatorDefault";
    String ATTRIBUTE_ACTION = "action";
    String ATTRIBUTE_MEP = "mep";
    String ATTRIBUTE_MEP_DEFAULT = "mepDefault";
    String ATTRIBUTE_METHOD = "method";
    String ATTRIBUTE_METHOD_DEFAULT = "methodDefault";
    String ATTRIBUTE_MODULE = "module";
    String ATTRIBUTE_IGNORE_UNCITED = "ignoreUncited";
    String ATTRIBUTE_INPUT_SERIALIZATION = "inputSerialization";
    String ATTRIBUTE_OUTPUT_SERIALIZATION = "outputSerialization";
    String ATTRIBUTE_FAULT_SERIALIZATION = "faultSerialization";
    String ATTRIBUTE_CODE = "code";
    String ATTRIBUTE_SUBCODES = "subcodes";
    String ATTRIBUTE_HEADER = "header";
    String ATTRIBUTE_TYPE = "type";
    String ATTRIBUTE_REQUIRED = "required";
    String ATTRIBUTE_MUST_UNDERSTAND = "mustUnderstand";
    String ATTRIBUTE_VERSION = "version";
    String ATTRIBUTE_PROTOCOL = "protocol";
    String ATTRIBUTE_SAFE = "safe";
    String ATTRIBUTE_ADDRESS = "address";
    String ATTRIBUTE_AUTHENTICATION_TYPE = "authenticationType";
    String ATTRIBUTE_AUTHENTICATION_REALM = "authenticationRealm";
    String ATTRIBUTE_STYLE = "style";
    String ATTRIBUTE_SIGNATURE = "signature";
    String OPERATION_STYLE = "operationStyle";
    String IN_PUT_LOCAL_NAME = "input";
    String OUT_PUT_LOCAL_NAME = "output";
    String OUT_FAULT_LOCAL_NAME = "outfault";
    String IN_FAULT_LOCAL_NAME = "infault";
    String FAULT_LOCAL_NAME = "fault";
    String ATTRIBUTE_NAME_PATTERN = "pattern";
    String MESSAGE_LABEL = "messageLabel";
    String ATTRIBUTE_ELEMENT = "element";

    String BINDING_LOCAL_NAME = "binding";
    String ENDPOINT_LOCAL_NAME = "endpoint";
    String SOAP_BINDING_PREFIX = "SOAPBinding";
    String HTTP_PROTOCAL = "http://www.w3.org/2003/05/soap/bindings/HTTP";
    String SERVICE_LOCAL_NAME = "service";

    String URI_WSOAP_MEP = "http://www.w3.org/2003/05/soap/mep/soap-response/";

    String ATTR_WSOAP_PROTOCOL = "wsoap:protocol";
    String ATTR_WSOAP_VERSION = "wsoap:version";
    String ATTR_WSOAP_CODE = "wsoap:code";
    String ATTR_WSOAP_MEP = "wsoap:mep";
    String ATTR_WSOAP_MODULE = "wsoap:module";
    String ATTR_WSOAP_SUBCODES = "wsoap:subcodes";
    String ATTR_WSOAP_HEADER = "wsoap:header";
    String ATTR_WSOAP_ACTION = "wsoap:action";
    String ATTR_WSOAP_ADDRESS = "wsoap:address";

    String ATTR_WHTTP_CONTENT_ENCODING = "whttp:contentEncoding";
    String ATTR_WHTTP_LOCATION = "whttp:location";
    String ATTR_WHTTP_HEADER = "whttp:header";
    String ATTR_WHTTP_METHOD = "whttp:method";
    String ATTR_WHTTP_CODE = "whttp:code";
    String ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR = "whttp:queryParameterSeparator";
    String ATTR_WHTTP_IGNORE_UNCITED = "whttp:ignoreUncited";
    String ATTR_WHTTP_INPUT_SERIALIZATION = "whttp:inputSerialization";
    String ATTR_WHTTP_OUTPUT_SERIALIZATION = "whttp:outputSerialization";
    String ATTR_WHTTP_FAULT_SERIALIZATION = "whttp:faultSerialization";
    String ATTR_WHTTP_AUTHENTICATION_TYPE = "whttp:authenticationType";
    String ATTR_WHTTP_AUTHENTICATION_REALM = "whttp:authenticationRealm";

    String ATTR_WSDLX_SAFE = "wsdlx:safe";

    String ATTR_WRPC_SIGNATURE = "wrpc:signature";

    String SOAP_VERSION_1_1 = "1.1";
    String SOAP_VERSION_1_2 = "1.2";

    String MESSAGE_LABEL_IN = "In";
    String MESSAGE_LABEL_OUT = "Out";

    String RPC_IN = "#in";
    String RPC_RETURN = "#return";
    String RPC_INOUT = "#inout";

    String HTTP_LOCATION_TABLE = "HTTPLocationTable";
    /* HTTP_LOCATION_TABLE_FOR_RESOURCE is to keep axis operation for compiled regex of
    resource url. This is for dispatching multiple resources with same
    resource name*/
    String HTTP_LOCATION_TABLE_FOR_RESOURCE = "HTTPLocationTableForResource";

    // This was taken from thye resolution of CR117 (WSDL 2.0 working group)
    // http://www.w3.org/2002/ws/desc/5/cr-issues/issues.html?view=normal#CR117
    // http://lists.w3.org/Archives/Public/www-ws-desc/2007Feb/0039.html
    String LEGAL_CHARACTERS_IN_URL = "-._~!$&()*+,;=:@?/%";
    String LEGAL_CHARACTERS_IN_PATH = "-._~!$'()*+,;=:@";
    String LEGAL_CHARACTERS_IN_QUERY = "-._~!$'()*+,;=:@/?";
    String TEMPLATE_ENCODE_ESCAPING_CHARACTER = "!";

    public String MEP_URI_IN_ONLY = "http://www.w3.org/ns/wsdl/in-only";
    public String MEP_URI_ROBUST_IN_ONLY = "http://www.w3.org/ns/wsdl/robust-in-only";
    public String MEP_URI_IN_OUT = "http://www.w3.org/ns/wsdl/in-out";
    public String MEP_URI_IN_OPTIONAL_OUT = "http://www.w3.org/ns/wsdl/in-opt-out";
    public String MEP_URI_OUT_ONLY = "http://www.w3.org/ns/wsdl/out-only";
    public String MEP_URI_ROBUST_OUT_ONLY = "http://www.w3.org/ns/wsdl/robust-out-only";
    public String MEP_URI_OUT_IN = "http://www.w3.org/ns/wsdl/out-in";
    public String MEP_URI_OUT_OPTIONAL_IN = "http://www.w3.org/ns/wsdl/out-opt-in";

    public String STYLE_IRI = "http://www.w3.org/ns/wsdl/style/iri";
    public String STYLE_RPC = "http://www.w3.org/ns/wsdl/style/rpc";
    public String STYLE_MULTIPART = "http://www.w3.org/ns/wsdl/style/multipart";

    public String NMTOKEN_ANY = "#any";
    public String NMTOKEN_NONE = "#none";
    public String NMTOKEN_OTHER = "#other";
    public String NMTOKEN_ELEMENT = "#element";
}
