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

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;

/**
 * Defines WSDL Access methods
 *
 */
public interface WSDLWrapper {
    public static final QName POLICY = new QName(Constants.POLICY, "Policy");
    public static final QName POLICY_REFERENCE =
            new QName(Constants.POLICY_REFERENCE, "PolicyReference");
    public static final QName SCHEMA = new QName(Constants.SCHEMA, "schema");
    public static final QName SOAP_11_BINDING =
            new QName(Constants.URI_WSDL_SOAP11_BINDING, "binding");
    public static final QName SOAP_11_BODY = new QName(Constants.URI_WSDL_SOAP11_BODY, "body");
    public static final QName SOAP_11_HEADER =
            new QName(Constants.URI_WSDL_SOAP11_HEADER, "header");
    public static final QName SOAP_11_OPERATION = new QName(Constants.URI_WSDL_SOAP11, "operation");
    public static final QName SOAP_12_BINDING =
            new QName(Constants.URI_WSDL_SOAP12_BINDING, "binding");
    public static final QName SOAP_12_BODY = new QName(Constants.URI_WSDL_SOAP12_BODY, "body");
    public static final QName SOAP_12_HEADER =
            new QName(Constants.URI_WSDL_SOAP12_HEADER, "header");
    public static final QName SOAP_12_OPERATION = new QName(Constants.URI_WSDL_SOAP11, "operation");

    public Object getFirstPortBinding(QName serviceQname);

    public String getOperationName(QName serviceQname, QName portQname);

    public ArrayList getPortBinding(QName serviceQname);

    public String getPortBinding(QName serviceQname, QName portQname);

    public String[] getPorts(QName serviceQname);

    public Object getService(QName serviceQname);

    public String getSOAPAction(QName serviceQname);

    public String getSOAPAction(QName serviceQname, QName portQname);

    public String getSOAPAction(QName serviceQname, QName portQName, QName operationQname);

    public URL getWSDLLocation();

    public String getTargetNamespace();

    public Definition getDefinition();

    public Definition getUnwrappedDefinition();
}