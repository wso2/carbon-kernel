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

package org.apache.axis2.corba.deployer;

public interface CorbaConstants {
    // Constants for Service DD XML
    String IDL_FILE = "idlFile";

    // To use a custom ORB
    String ORB_CLASS = "orbClass";
    String ORB_SINGLETON_CLASS = "orbSingletonClass";

    // One of the following is required to locate the CORBA service
    String NAMING_SERVICE_URL = "namingServiceUrl";
    String IOR_FILE_PATH = "iorFilePath";
    String IOR_STRING = "iorString";

    // Required only if namingServiceUrl is provided 
    String OBJECT_NAME = "objectName";

    // Name of the CORBA interface
    String INTERFACE_NAME = "interfaceName";

    // Constants for processing the corba web services
    String ORB_LITERAL = "orb";
    String IDL_LITERAL = "idl";
    String RETURN_WRAPPER = "return";
    String VOID = "void";
    String RESPONSE = "Response";
    String FAULT = "Fault";
    String ARRAY_ITEM = "item";
    String ORG_OMG_CORBA_ORBCLASS = "org.omg.CORBA.ORBClass";
    String DEFAULR_ORB_CLASS = "org.apache.yoko.orb.CORBA.ORB";
    String ORG_OMG_CORBA_ORBSINGLETON_CLASS = "org.omg.CORBA.ORBSingletonClass";
    String DEFAULT_ORBSINGLETON_CLASS = "org.apache.yoko.orb.CORBA.ORBSingleton";

    String NAME_SPACE_PREFIX = "ax2"; // axis2 name space
    String HTTP = "http://";
    char PACKAGE_CLASS_DELIMITER = '.';
    String SCHEMA_NAMESPACE_EXTN = "/xsd";
    String DEFAULT_SCHEMA_NAMESPACE_PREFIX = "xs";
    String URI_2001_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";
    String FORM_DEFAULT_UNQUALIFIED = "unqualified";
}
