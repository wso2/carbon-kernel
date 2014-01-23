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

package org.apache.axis2.jaxws.description.builder.converter;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.log4j.BasicConfigurator;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class ReflectiveConverterTests extends TestCase {
    static {
        // Note you will probably need to increase the java heap size, for example
        // -Xmx512m.  This can be done by setting maven.junit.jvmargs in project.properties.
        // To change the settings, edit the log4j.property file
        // in the test-resources directory.
        BasicConfigurator.configure();
    }

    private static DescriptionBuilderComposite implDBC;
    private static DescriptionBuilderComposite seiDBC;

    public void setUp() {
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(SimpleServiceImpl.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        assertNotNull(dbcMap);
        implDBC = dbcMap.get(
                "org.apache.axis2.jaxws.description.builder.converter.SimpleServiceImpl");
        seiDBC = dbcMap.get(
                "org.apache.axis2.jaxws.description.builder.converter.SimpleService");
    }

    public static void testCreateImplDBC() {
        assertNotNull(implDBC);
        WebService wsAnnot = implDBC.getWebServiceAnnot();
        assertNotNull(wsAnnot);
        assertEquals("SimpleService", wsAnnot.serviceName());
    }

    public static void testImplMethods() {
        assertNotNull(implDBC);
        List<MethodDescriptionComposite> mdcList = sortList(implDBC.getMethodDescriptionsList());
        sortList(mdcList);
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 5);
        MethodDescriptionComposite mdc = mdcList.get(0);
        assertNotNull(mdc);
        assertEquals("<init>", mdc.getMethodName());
        mdc = mdcList.get(1);
        assertNotNull(mdc);
        assertEquals("invoke", mdc.getMethodName());
        assertEquals("java.lang.String", mdc.getReturnType());
        mdc = mdcList.get(2);
        assertNotNull(mdc);
        assertEquals("invoke2", mdc.getMethodName());
        assertEquals("int", mdc.getReturnType());
        mdc = mdcList.get(3);
        assertNotNull(mdc);
        assertTrue("invoke3 is static operation and should return true for static check", mdc.isStatic());
        mdc = mdcList.get(4);
        assertNotNull(mdc);
        assertTrue("invoke4 is final operation and should return true for static check", mdc.isFinal());
        
    }

    public static void testImplParams() {
        assertNotNull(implDBC);
        List<MethodDescriptionComposite> mdcList = sortList(implDBC.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 5);
        MethodDescriptionComposite mdc = mdcList.get(0);
        assertNotNull(mdc);
        List<ParameterDescriptionComposite> pdcList = mdc.getParameterDescriptionCompositeList();
        assertNotNull(pdcList);
        assertEquals(0, pdcList.size());
        mdc = mdcList.get(1);
        assertNotNull(mdc);
        pdcList = mdc.getParameterDescriptionCompositeList();
        assertNotNull(pdcList);
        assertEquals(pdcList.size(), 1);
        ParameterDescriptionComposite pdc = pdcList.get(0);
        assertEquals("java.util.List<java.lang.String>", pdc.getParameterType());
        mdc = mdcList.get(2);
        pdcList = mdc.getParameterDescriptionCompositeList();
        assertNotNull(pdcList);
        assertEquals(pdcList.size(), 2);
        pdc = pdcList.get(0);
        assertEquals("int", pdc.getParameterType());
        pdc = pdcList.get(1);
        assertNotNull(pdc);
        assertEquals("int", pdc.getParameterType());
    }

    public static void testCreateSEIDBC() {
        assertNotNull(seiDBC);
        WebService wsAnnot = seiDBC.getWebServiceAnnot();
        assertNotNull(wsAnnot);
        assertEquals("SimpleServicePort", wsAnnot.name());
    }

    public static void testSEIMethods() {
        assertNotNull(seiDBC);
        List<MethodDescriptionComposite> mdcList = sortList(seiDBC.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 2);
        MethodDescriptionComposite mdc = mdcList.get(0);
        assertEquals("invoke", mdc.getMethodName());
        assertEquals("java.lang.String", mdc.getReturnType());
        assertNotNull(mdc.getWebMethodAnnot());
        WebMethodAnnot wmAnnot = mdc.getWebMethodAnnot();
        assertEquals("invoke", wmAnnot.operationName());
        mdc = mdcList.get(1);
        assertEquals("invoke2", mdc.getMethodName());
        assertEquals("int", mdc.getReturnType());
    }

    public static void testSEIParams() {
        assertNotNull(seiDBC);
        List<MethodDescriptionComposite> mdcList = sortList(seiDBC.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 2);
        MethodDescriptionComposite mdc = mdcList.get(0);
        assertNotNull(mdc);
        List<ParameterDescriptionComposite> pdcList = mdc.getParameterDescriptionCompositeList();
        assertNotNull(pdcList);
        assertEquals(pdcList.size(), 1);
        ParameterDescriptionComposite pdc = pdcList.get(0);
        assertNotNull(pdc);
        assertEquals("java.util.List<java.lang.String>", pdc.getParameterType());
        WebParamAnnot wpAnnot = pdc.getWebParamAnnot();
        assertNotNull(wpAnnot);
        assertEquals("echoString", wpAnnot.name());
        mdc = mdcList.get(1);
        assertNotNull(mdc);
        pdcList = mdc.getParameterDescriptionCompositeList();
        assertNotNull(pdcList);
        assertEquals(pdcList.size(), 2);
        pdc = pdcList.get(0);
        assertNotNull(pdc);
        assertEquals("int", pdc.getParameterType());
        assertNull(pdc.getWebParamAnnot());
        pdc = pdcList.get(1);
        assertNotNull(pdc);
        assertEquals("int", pdc.getParameterType());
        assertNull(pdc.getWebParamAnnot());
    }

    public void testDBCHierarchy() {
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(ChildClass.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        DescriptionBuilderComposite dbc =
                dbcMap.get("org.apache.axis2.jaxws.description.builder.converter.ChildClass");
        assertNotNull(dbc);
        List<MethodDescriptionComposite> mdcList = sortList(dbc.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 3);
        assertEquals("<init>", mdcList.get(0).getMethodName());
        assertEquals("doAbstract", mdcList.get(1).getMethodName());
        assertEquals("extraMethod", mdcList.get(2).getMethodName());
        dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter.ParentClass");
        assertNotNull(dbc);
        mdcList = sortList(dbc.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 2);
        assertEquals("<init>", mdcList.get(0).getMethodName());
        assertEquals("doParentAbstract", mdcList.get(1).getMethodName());
        dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter.ServiceInterface");
        assertNotNull(dbc);
        mdcList = sortList(dbc.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 1);
        assertEquals("doAbstract", mdcList.get(0).getMethodName());
        dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter.CommonService");
        assertNotNull(dbc);
        mdcList = sortList(dbc.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 1);
        assertEquals("extraMethod", mdcList.get(0).getMethodName());
        dbc = dbcMap.get(
                "org.apache.axis2.jaxws.description.builder.converter.ParentServiceInterface");
        assertNotNull(dbc);
        mdcList = sortList(dbc.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 1);
        assertEquals("doParentAbstract", mdcList.get(0).getMethodName());
        dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter.AbstractService");
        assertNotNull(dbc);
        mdcList = sortList(dbc.getMethodDescriptionsList());
        assertNotNull(mdcList);
        assertEquals(mdcList.size(), 2);
        assertEquals("<init>", mdcList.get(0).getMethodName());
        assertEquals("someAbstractMethod", mdcList.get(1).getMethodName());

    }

    private static List<MethodDescriptionComposite> sortList(List<MethodDescriptionComposite> mdc) {
        Comparator<MethodDescriptionComposite> c = new Comparator<MethodDescriptionComposite>() {
            public int compare(MethodDescriptionComposite mdc1, MethodDescriptionComposite o2) {
                return mdc1.getMethodName().compareTo(o2.getMethodName());
            }
        };
        Collections.sort(mdc, c);
        return mdc;
    }
}

@WebService(serviceName = "SimpleService", endpointInterface = "org.apache.axis2.jaxws." +
        "description.builder.converter.SimpleService")
class SimpleServiceImpl {
    public SimpleServiceImpl() {
    }

    ;

    public String invoke(List<String> myParam) {
        return myParam.get(0);
    }

    public int invoke2(int num1, int num2) {
        return num1 + num2;
    }
    
    public static String invoke3(){
        return "static";
    }
    
    public final String invoke4(){
        return "final";
    }
}

@WebService(name = "SimpleServicePort")
interface SimpleService {
    @WebMethod(operationName = "invoke")
    public String invoke(@WebParam(name = "echoString") List<String> arg1);

    public int invoke2(int arg1, int arg2);
}

@WebService(serviceName = "InheritanceTestChild")
class ChildClass extends ParentClass implements ServiceInterface, CommonService {
    public ChildClass() {
    }

    public void doAbstract() {
    }

    public void extraMethod() {
    }
    
    protected void protectedChildMethod() {        
    }
    
    private void privateChildMethod() {        
    }
}

@WebService(serviceName = "InhertianceTestParent")
class ParentClass extends AbstractService implements ParentServiceInterface {
    public ParentClass() {
    }

    public void doParentAbstract() {
    }

    protected void protectedParentMethod() {        
    }
    
    private void privateParentMethod() {        
    }
}

interface ServiceInterface {
    public void doAbstract();
}

interface CommonService {
    public void extraMethod();
}

interface ParentServiceInterface {
    public void doParentAbstract();
}

class AbstractService {
    public void someAbstractMethod() {
    }
}

