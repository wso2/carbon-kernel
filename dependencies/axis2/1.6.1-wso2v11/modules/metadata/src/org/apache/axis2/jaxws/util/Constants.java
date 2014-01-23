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

package org.apache.axis2.jaxws.util;

public class Constants {
    public static final String URI_WSDL_SOAP11 = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String URI_WSDL_SOAP11_BODY = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12_BODY = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String URI_WSDL_SOAP11_HEADER = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12_HEADER = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String URI_WSDL_SOAP11_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String URI_WSDL_SOAP12_BINDING = "http://schemas.xmlsoap.org/wsdl/soap12/";

    public static final String POLICY = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public static final String POLICY_REFERENCE = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    public static final String SCHEMA = "http://www.w3.org/2001/XMLSchema";

    public static String AXIS2_REPO_PATH = "org.apache.axis2.jaxws.repo.path";
    public static String AXIS2_CONFIG_PATH = "org.apache.axis2.jaxws.config.path";
    public static String USE_ASYNC_MEP = "org.apache.axis2.jaxws.use.async.mep";

    public static final String THREAD_CONTEXT_MIGRATOR_LIST_ID = "JAXWS-ThreadContextMigrator-List";
    public static final String WSDL_EXTENSION_VALIDATOR_LIST_ID = "JAXWS-WSDLExtensionValidator-List";
    
    public static final String INVOCATION_PATTERN = "org.apache.axis2.jaxws.invocation.pattern";
    
    public static final String METADATA_REGISTRY_CONFIG_FILE = 
        "META-INF/services/org.apache.axis2.metadata.registry.MetadataFactoryRegistry";
    
}
