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


package org.apache.axis2.jaxws.description;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

public interface OperationDescriptionJava {

    public WebResult getAnnoWebResult();

    public boolean isWebResultAnnotationSpecified();

    public boolean getAnnoWebResultHeader();

    public String getAnnoWebResultName();

    public String getAnnoWebResultPartName();

    public String getAnnoWebResultTargetNamespace();

    public RequestWrapper getAnnoRequestWrapper();

    /**
     * @return the specified value of @RequestWrapper className or null NOTE: There is no default
     *         for the classname
     */
    public String getAnnoRequestWrapperClassName();

    public String getAnnoRequestWrapperLocalName();

    public String getAnnoRequestWrapperTargetNamespace();

    public ResponseWrapper getAnnoResponseWrapper();

    /**
     * @return the specified value of @ResponseWrapper className or null NOTE: There is no default
     *         for the classname
     */
    public String getAnnoResponseWrapperClassName();

    public String getAnnoResponseWrapperLocalName();

    public String getAnnoResponseWrapperTargetNamespace();

    public SOAPBinding getAnnoSoapBinding();

    public javax.jws.soap.SOAPBinding.ParameterStyle getAnnoSoapBindingParameterStyle();

    public javax.jws.soap.SOAPBinding.Style getAnnoSoapBindingStyle();

    public javax.jws.soap.SOAPBinding.Use getAnnoSoapBindingUse();

    public WebMethod getAnnoWebMethod();

    public String getAnnoWebMethodAction();

    public boolean getAnnoWebMethodExclude();

    public String getAnnoWebMethodOperationName();

    // Note that the WebParam annotation is handled by ParameterDescription.  These
    // WebParam-related methods are simply convenience methods.
    public Mode[] getAnnoWebParamModes();

    public String[] getAnnoWebParamNames();

    public String getAnnoWebParamTargetNamespace(String name);

    public String[] getAnnoWebParamTargetNamespaces();

    public boolean isAnnoWebParamHeader(String name);

    public Oneway getAnnoOneway();

    public boolean isAnnoOneWay();
    
    public Action getAnnoAction();

}