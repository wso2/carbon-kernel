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

/**
 * ComplexDataTypesComplexDataTypesSOAP11Test.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT May 30, 2007 (11:56:02 EDT)
 */
package org.apache.axis2.rpc.complex;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.tempuri.complex.data.arrays.xsd.ArrayOfArrayOfstring;
import org.tempuri.complex.data.arrays.xsd.ArrayOfNullableOfdateTime;
import org.tempuri.complex.data.arrays.xsd.ArrayOfNullableOfdecimal;
import org.tempuri.complex.data.arrays.xsd.ArrayOfPerson;
import org.tempuri.complex.data.arrays.xsd.ArrayOfanyType;
import org.tempuri.complex.data.arrays.xsd.ArrayOfint;
import org.tempuri.complex.data.arrays.xsd.ArrayOfshort;
import org.tempuri.complex.data.arrays.xsd.ArrayOfstring;
import org.tempuri.complex.data.xsd.*;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;



public class ComplexDataTypesComplexDataTypesSOAP11Test extends UtilServerBasedTestCase {

 protected EndpointReference targetEPR;
    protected QName serviceName = new QName("ComplexDataTypes");

    protected AxisConfiguration axisConfiguration;

    protected boolean finish = false;
    org.tempuri.complex.ComplexDataTypesComplexDataTypesHttpSoap11EndpointStub stub;

    public ComplexDataTypesComplexDataTypesSOAP11Test() {
        super(ComplexDataTypesComplexDataTypesSOAP11Test.class.getName());
    }

    public ComplexDataTypesComplexDataTypesSOAP11Test(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(ComplexDataTypesComplexDataTypesSOAP11Test.class));
    }


    protected void setUp() throws Exception {
        String className = "org.tempuri.complex.ComplexDataTypes";
        UtilServer.start();
        AxisService   service = AxisService.createService(
                className, UtilServer.getConfigurationContext().getAxisConfiguration());
        service.setElementFormDefault(true);
        service.setName("ComplexDataTypes");
        service.setClassLoader(Thread.currentThread().getContextClassLoader());

        UtilServer.deployService(service);
         targetEPR =
                new EndpointReference("http://127.0.0.1:"
                        + (UtilServer.TESTING_PORT)
                        + "/axis2/services/ComplexDataTypes");
        stub  = new org.tempuri.complex.ComplexDataTypesComplexDataTypesHttpSoap11EndpointStub(null,targetEPR.getAddress());
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testretArrayInt1D() throws java.lang.Exception {


        assertNull(stub.retArrayInt1D(null));
        stub._getServiceClient().cleanupTransport();
        assertNotNull(stub.retArrayInt1D(new ArrayOfint()));
        stub._getServiceClient().cleanupTransport();
        ArrayOfint input = new ArrayOfint();
        input.set_int(new int[]{0, 1, 2});
        ArrayOfint ret = stub.retArrayInt1D(input);
        stub._getServiceClient().cleanupTransport();
        assertNotNull(ret);
        assertNotNull(ret.get_int());
        assertEquals(ret.get_int().length, 3);
    }

    /**
     * Auto generated test method
     */
    public void testretStructSNSAS() throws java.lang.Exception {
        assertNull(stub.retStructSNSAS(null));
        assertNotNull(stub.retStructSNSAS(new Group()));
        Group input = new Group();
        input.setName("xyz");
        input.setMembers(new ArrayOfPerson());
        assertNotNull(stub.retStructSNSAS(input));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayDateTime1D() throws java.lang.Exception {

        assertNull(stub.retArrayDateTime1D(null));

        assertNotNull(stub.retArrayDateTime1D(new ArrayOfNullableOfdateTime()));

        ArrayOfNullableOfdateTime input = new ArrayOfNullableOfdateTime();
        input.setDateTime(new Calendar[]{Calendar.getInstance(), Calendar.getInstance()});
        assertNotNull(stub.retArrayDateTime1D(input));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayString2D() throws java.lang.Exception {

        assertNull(stub.retArrayString2D(null));
        assertNotNull(stub.retArrayString2D(new ArrayOfArrayOfstring()));

        ArrayOfArrayOfstring input = new ArrayOfArrayOfstring();
        ArrayOfstring a2 = new ArrayOfstring();
        ArrayOfstring a1 = new ArrayOfstring();
        a1.setString(new String[]{"foo", "bar"});
        input.setArrayOfstring(new ArrayOfstring[]{a1, a2});
        assertNotNull(stub.retArrayString2D(input));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayDecimal1D() throws java.lang.Exception {

        assertNull(stub.retArrayDecimal1D(null));
        assertNotNull(stub.retArrayDecimal1D(new ArrayOfNullableOfdecimal()));

        ArrayOfNullableOfdecimal input = new ArrayOfNullableOfdecimal();
        input.setDecimal(new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
        assertNotNull(stub.retArrayDecimal1D(input));
    }

    /**
     * Auto generated test method
     */
    public void testretStructSNSA() throws java.lang.Exception {

        assertNull(stub.retStructSNSA(null));
        assertNotNull(stub.retStructSNSA(new Employee()));
        Employee input = new Employee();
        input.setJobID(34);
        input.setBaseDetails(new Person());
        input.setNumbers(new ArrayOfshort());
        input.setHireDate(Calendar.getInstance());
        assertNotNull(stub.retStructSNSA(input));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayAnyType1D() throws java.lang.Exception {

        assertNull(stub.retArrayAnyType1D(null));
        assertNotNull(stub.retArrayAnyType1D(new ArrayOfanyType()));

        ArrayOfanyType input = new ArrayOfanyType();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        // lets create the namespace object of the Article element
        OMNamespace ns = factory.createOMNamespace("http://www.serverside.com/articles/introducingAxiom", "article");
        // now create the Article element with the above namespace
        OMElement articleElement = factory.createOMElement("Article", ns);

       // comment out test case since now adb uses an object to represent an any type
       // input.setAnyType(new OMElement[]{articleElement});
       // assertNotNull(stub.retArrayAnyType1D(input));
    }

    /**
     * Auto generated test method
     */
    public void testretDerivedClass2() throws java.lang.Exception {

        assertNull(stub.retDerivedClass2(null));
        assertNotNull(stub.retDerivedClass2(new Table()));

        Table input = new Table();
        input.setSeatingCapacity(50);
        input.setColor("sdfsd");
        input.setPrice(45);
        assertNotNull(stub.retDerivedClass2(input));
    }

    /**
     * Auto generated test method
     */
    public void testretStructSN() throws java.lang.Exception {

        assertNull(stub.retStructSN(null));
        assertNotNull(stub.retStructSN(new Person()));

        Person input = new Person();
        input.setAge(23);
        input.setId(345);
        input.setMale(false);
        input.setName("Why?");
        assertNotNull(stub.retStructSN(input));
    }

    /**
     * Auto generated test method
     */
    public void testretArray1DSN() throws java.lang.Exception {

        assertNull(stub.retArray1DSN(null));
        assertNotNull(stub.retArray1DSN(new ArrayOfPerson()));

        ArrayOfPerson input = new ArrayOfPerson();
        Person p1 = new Person();
        p1.setAge(34);
        p1.setId(2345);
        p1.setMale(true);
        p1.setName("HJHJH");
        input.setPerson(new Person[]{p1});
        assertNotNull(stub.retArray1DSN(input));
    }

    /**
     * Auto generated test method
     */
    public void testretDerivedClass() throws java.lang.Exception {


        assertNull(stub.retDerivedClass(null));
        assertNotNull(stub.retDerivedClass(new Furniture()));

        Furniture input = new Furniture();
        input.setColor("white");
        input.setPrice(67);
        assertNotNull(stub.retDerivedClass(input));
        assertTrue(input instanceof Furniture);

        input = new Table();
        input.setColor("black");
        input.setPrice(89);
        ((Table) input).setSeatingCapacity(100);
        assertTrue(input instanceof Table);
    }

// TODO: We need to figure out how to deal with ENUM's. Please don't remove this section.    
//    /**
//     * Auto generated test method
//     */
    public void testretEnumInt() throws java.lang.Exception {


        assertNull(stub.retEnumInt(null));
        String input = "";
        assertNotNull(stub.retEnumInt(new String()));
    }

// TODO: We need to figure out how to deal with ENUM's. Please don't remove this section.
//    /**
//     * Auto generated test method
//     */
//    public void testretEnumString() throws java.lang.Exception {
//
//        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
//                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
//
//        assertNull(stub.retEnumString(null));
//        BitMask input = new BitMask();
//        assertNull(stub.retEnumString(new BitMask()));
//    }


    /**
     * Auto generated test method
     */
    public void testretStructS1() throws java.lang.Exception {

        assertNull(stub.retStructS1(null));
        assertNotNull(stub.retStructS1(new Name()));
        Name input = new Name();
        input.setName("ewrterty");
        assertNotNull(stub.retStructS1(input));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayString1D() throws java.lang.Exception {


        assertNull(stub.retArrayString1D(null));
        assertNotNull(stub.retArrayString1D(new ArrayOfstring()));
        ArrayOfstring input = new ArrayOfstring();
        input.setString(new String[]{"foo", "bar"});
        ArrayOfstring ret = stub.retArrayString1D(input);
        assertNotNull(ret);
        assertNotNull(ret.getString());
        assertEquals(ret.getString().length, 2);
    }


    /**
     * Auto generated test method
     */
    public void testretSingle() throws java.lang.Exception {
        float ret = stub.retSingle(43.0f);
        assertTrue(ret==43.0f);
    }

    private SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Auto generated test method
     */
    public void testretDateTime() throws java.lang.Exception {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar input = Calendar.getInstance();
        Calendar ret = stub.retDateTime(input);
        assertNotNull(ret);
        assertEquals(zulu.format(input.getTime()), zulu.format(ret.getTime()));
    }

    /**
     * Auto generated test method
     */
    public void testretGuid() throws java.lang.Exception {

        String input = "12345";
        String ret = stub.retGuid(input);
        assertEquals(ret, input);
    }

    /**
     * Auto generated test method
     */
    public void testretByteArray() throws java.lang.Exception {


        byte[] input = new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        DataHandler ret = stub.retByteArray(new DataHandler(new ByteArrayDataSource(input)));
        byte[] bytes = IOUtils.getStreamAsByteArray(ret.getInputStream());
        assertTrue(Arrays.equals(bytes, input));
    }

    /**
     * Auto generated test method
     */
    public void testretUri() throws java.lang.Exception {


        assertNotNull(stub.retUri("124"));
    }

    /**
     * Auto generated test method
     */
    public void testretQName() throws java.lang.Exception {


        //FIXME: Why is QName being mapped to OMElement?
        assertNull(stub.retQName(null));
    }

// TODO: FIXME: Need to figure out how to do enum's. Please don't remove this following section
//    /**
//     * Auto generated test method
//     */
//    public void testretEnumInt() throws java.lang.Exception {
//
//        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
//                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
//
//        org.tempuri.complex.xsd.xsd.RetEnumInt retEnumInt126 =
//                (org.tempuri.complex.xsd.xsd.RetEnumInt) getTestObject(org.tempuri.complex.xsd.xsd.RetEnumInt.class);
//        // todo Fill in the retEnumInt126 here
//
//        assertNotNull(stub.retEnumInt(
//                getParam0(retEnumInt126)
//        ));
//
//
//    }

    public void testretLong() throws java.lang.Exception {
        long ret = stub.retLong(34);
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretUShort() throws java.lang.Exception {
        int ret = stub.retUShort(34);
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretUInt() throws java.lang.Exception {
        long ret = stub.retUInt(34);
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretByte() throws java.lang.Exception {
        short ret = stub.retByte((short)34);
        assertEquals((short)34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretSByte() throws java.lang.Exception {
        byte ret = stub.retSByte((byte)34);
        assertEquals((byte)34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretShort() throws java.lang.Exception {
        short ret = stub.retShort((short)34);
        assertEquals((short)34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretObject() throws java.lang.Exception {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        // lets create the namespace object of the Article element
        OMNamespace ns = factory.createOMNamespace("http://www.serverside.com/articles/introducingAxiom", "article");
        // now create the Article element with the above namespace
        OMElement articleElement = factory.createOMElement("Article", ns);

        // comment out this test case since adb now uses an object to represents the any type
        // OMElement ret = stub.retObject(articleElement);
        // assertNotNull(ret);
        // assertEquals(ret.toString(), articleElement.toString());
    }

    /**
     * Auto generated test method
     */
    public void testretFloat() throws java.lang.Exception {

        float ret = stub.retFloat((float)34);
        assertTrue(ret==34);
    }

    /**
     * Auto generated test method
     */
    public void testretDouble() throws java.lang.Exception {

        double ret = stub.retDouble((double)34);
        assertTrue(ret==34);
    }

    /**
     * Auto generated test method
     */
    public void testretBool() throws java.lang.Exception {

        boolean ret = stub.retBool(true);
        assertTrue(ret);
    }

    /**
     * Auto generated test method
     */
    public void testretDecimal() throws java.lang.Exception {

        BigDecimal input = new BigDecimal(12334);
        BigDecimal ret = stub.retDecimal(input);
        assertNotNull(ret);
        assertEquals(ret, input);
    }

// TODO: FIXME: Need to figure out how to do enum's. Please don't remove this following section
//    /**
//     * Auto generated test method
//     */
//    public void testretEnumString() throws java.lang.Exception {
//
//        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
//                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
//
//        org.tempuri.complex.xsd.xsd.RetEnumString retEnumString198 =
//                (org.tempuri.complex.xsd.xsd.RetEnumString) getTestObject(org.tempuri.complex.xsd.xsd.RetEnumString.class);
//        // todo Fill in the retEnumString198 here
//
//        assertNotNull(stub.retEnumString(
//                getParam0(retEnumString198)
//        ));
//
//
//    }

    /**
     * Auto generated test method
     */
    public void testretInt() throws java.lang.Exception {
        int ret = stub.retInt((int)34);
        assertEquals((int)34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretInts() throws java.lang.Exception {

        int[] input = new int[]{34, 45};
        int ret[] = stub.retInts(input);
        assertTrue(Arrays.equals(input, ret));
    }

    /**
     * Auto generated test method
     */
    public void testretChar() throws java.lang.Exception {

        int ret = stub.retChar(34);
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretString() throws java.lang.Exception {

        String input = "Abracadabra";
        String ret = stub.retString(input);
        assertNotNull(ret);
        assertEquals(input, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretStrings() throws java.lang.Exception {

String[] ret;
        String[] input = new String[]{"Abracadabra"};
        ret = stub.retStrings(null);
        assertNull(ret);
        ret = stub.retStrings(input);
        assertNotNull(ret);
        assertTrue(Arrays.equals(input, ret));
        input = new String[]{"Abracadabra", null, "abc"};
        ret = stub.retStrings(input);
        assertNotNull(ret);
        assertTrue(Arrays.equals(input, ret));

        input = new String[]{};
        ret = stub.retStrings(input);
        assertNull(ret);
    }

    /**
     * Auto generated test method
     */
    public void testretULong() throws java.lang.Exception {

        BigInteger input = new BigInteger("34");
        BigInteger ret = stub.retULong(input);
        assertEquals(input, ret);
    }
}
    