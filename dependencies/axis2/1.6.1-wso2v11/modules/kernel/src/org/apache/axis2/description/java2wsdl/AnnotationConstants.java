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

public interface AnnotationConstants {
    String WEB_SERVICE = "javax.jws.WebService";
    String WEB_METHOD = "javax.jws.WebMethod";
    String WEB_PARAM = "javax.jws.WebParam";
    String WEB_RESULT = "javax.jws.WebResult";
    String WEB_SERVICE_PROVIDER = "javax.xml.ws.WebServiceProvider";
    String TARGETNAMESPACE = "targetNamespace";
    String NAME = "name";
    String SERVICE_NAME = "serviceName";
    String EXCLUDE = "exclude";
    String ACTION = "action";
    String WSDL_LOCATION = "wsdlLocation";
    String OPERATION_NAME ="operationName";
}
