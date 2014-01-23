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


package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.DescriptionTestUtils;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;

import javax.xml.namespace.QName;

import javax.jws.WebService;
import javax.wsdl.Definition;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

/**
 * This test validates the error checking and internal functioning of the OperationDescription class
 * and serves as a blueprint for how the code should function.
 * Direct tests of the functionality of an OperationDescription and other
 * Description classes is done in WSDLDescriptionTests.
 */
public class OperationDescriptionImplTests extends TestCase
{
  public void testDetermineOperationQName()
  {
    String wsdlRelativeLocation = "test-resources/wsdl/";
    String wsdlFileName = "BindingNamespaceDefaults.wsdl";
    String targetNamespace = "http://nonanonymous.complextype.test.org";
    String wsdlLocation = wsdlRelativeLocation + wsdlFileName;

    // Build up a DBC, including the WSDL Definition and the annotation information for 
    // the impl class.
    DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();

    URL wsdlURL = DescriptionTestUtils.getWSDLURL(wsdlFileName);
    Definition wsdlDefn = DescriptionTestUtils.createWSDLDefinition(wsdlURL);
    assertNotNull(wsdlDefn);

    WebServiceAnnot webServiceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
    assertNotNull(webServiceAnnot);
    webServiceAnnot.setWsdlLocation(wsdlLocation);
    webServiceAnnot.setTargetNamespace(targetNamespace);
    webServiceAnnot.setServiceName("EchoMessageService");
    webServiceAnnot.setPortName("EchoMessagePort");

    MethodDescriptionComposite mdc = new MethodDescriptionComposite();
    mdc.setMethodName("echoMessage");
    mdc.setReturnType("java.lang.String");

    ParameterDescriptionComposite pdc1 = new ParameterDescriptionComposite();
    pdc1.setParameterType("java.lang.String");

    mdc.addParameterDescriptionComposite(pdc1);

    dbc.addMethodDescriptionComposite(mdc);
    dbc.setWebServiceAnnot(webServiceAnnot);
    dbc.setClassName(BindingNSImpl.class.getName());
    dbc.setWsdlDefinition(wsdlDefn);
    dbc.setwsdlURL(wsdlURL);
    HashMap<String, DescriptionBuilderComposite> dbcMap =
            new HashMap<String, DescriptionBuilderComposite>();
    dbcMap.put(dbc.getClassName(), dbc);
    List<ServiceDescription> serviceDescList =
            DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
    ServiceDescription sd = serviceDescList.get(0);
    EndpointInterfaceDescription eid = new EndpointInterfaceDescriptionImpl(dbc, new EndpointDescriptionImpl(null, new QName(targetNamespace,"EchoMessagePort"), (ServiceDescriptionImpl)sd));
      
    assertEquals(new QName(targetNamespace, "echoMessage"), OperationDescriptionImpl.determineOperationQName(eid, mdc));

  }
  
  @WebService(serviceName = "EchoMessageService", portName = "EchoMessagePort", targetNamespace = "http://nonanonymous.complextype.test.org", wsdlLocation = "test-resources/wsdl/BindingNamespace.wsdl")
  public class BindingNSImpl {
      public String echoMessage(String arg) {
          return arg;
      }
  }
  
}
