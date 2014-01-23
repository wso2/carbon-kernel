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
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.tempuri.complex.ComplexDataTypesDocLitBareStub;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/*
 *  ComplexDataTypesDocLitBareTest Junit test case
*/

public class ComplexDataTypesDocLitBareTest extends
        UtilServerBasedTestCase {

    protected QName serviceName = new QName("ComplexDataTypesDocLitBare");
    protected AxisConfiguration axisConfiguration;
    protected EndpointReference targetEPR;
    ComplexDataTypesDocLitBareStub stub;

    public ComplexDataTypesDocLitBareTest() {
        super(ComplexDataTypesDocLitBareTest.class.getName());
    }

    public ComplexDataTypesDocLitBareTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(ComplexDataTypesDocLitBareTest.class));
    }

    protected void setUp() throws Exception {
        targetEPR =
                new EndpointReference("http://127.0.0.1:"
                        + (UtilServer.TESTING_PORT)
//                        + 8000
                        + "/axis2/services/ComplexDataTypesDocLitBare");
        stub = new org.tempuri.complex.ComplexDataTypesDocLitBareStub(null, targetEPR.getAddress());
        String className = "org.tempuri.complex.ComplexDataTypesDocLitBare";
        UtilServer.start();
        Parameter generateBare = new Parameter();
        generateBare.setName(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
        generateBare.setValue("true");
        UtilServer.getConfigurationContext().getAxisConfiguration().addParameter(generateBare);
        AxisService service = AxisService.createService(
                className, UtilServer.getConfigurationContext().getAxisConfiguration());
        service.addParameter(generateBare);
        service.setName("ComplexDataTypesDocLitBare");
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testretArrayInt1D() throws java.lang.Exception {
//        assertNull(stub.retArrayInt1D(null));
        stub._getServiceClient().cleanupTransport();
        ComplexDataTypesDocLitBareStub.InArrayInt1D req = new
                ComplexDataTypesDocLitBareStub.InArrayInt1D();
        assertNotNull(stub.retArrayInt1D(req));
        stub._getServiceClient().cleanupTransport();
        ComplexDataTypesDocLitBareStub.ArrayOfint input = new ComplexDataTypesDocLitBareStub.ArrayOfint();
        input.set_int(new int[]{0, 1, 2});
        req.setInArrayInt1D(input);
        ComplexDataTypesDocLitBareStub.RetArrayInt1DResult ret = stub.retArrayInt1D(req);
        stub._getServiceClient().cleanupTransport();
        assertNotNull(ret);
        assertNotNull(ret.getRetArrayInt1DResult().get_int());
        assertEquals(ret.getRetArrayInt1DResult().get_int().length, 3);
    }

    /**
     * Auto generated test method
     */
    public void testretStructSNSAS() throws java.lang.Exception {
        //TODO Codegen issue
//        assertNull(stub.retStructSNSAS(null));
        ComplexDataTypesDocLitBareStub.InStructSNSAS req =
                new ComplexDataTypesDocLitBareStub.InStructSNSAS();
        assertNotNull(stub.retStructSNSAS(req));

        ComplexDataTypesDocLitBareStub.Group input =
                new ComplexDataTypesDocLitBareStub.Group();
        input.setName("xyz");
        input.setMembers(new ComplexDataTypesDocLitBareStub.ArrayOfPerson());
        req.setInStructSNSAS(input);
        assertNotNull(stub.retStructSNSAS(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayDateTime1D() throws java.lang.Exception {
        //TODO , this is a codegen bug
//        assertNull(stub.retArrayDateTime1D(null));
        ComplexDataTypesDocLitBareStub.InArrayDateTime1D req = new ComplexDataTypesDocLitBareStub.InArrayDateTime1D();
        assertNotNull(stub.retArrayDateTime1D(req));
        ComplexDataTypesDocLitBareStub.ArrayOfNullableOfdateTime input = new ComplexDataTypesDocLitBareStub.ArrayOfNullableOfdateTime();
        input.setDateTime(new Calendar[]{Calendar.getInstance(), Calendar.getInstance()});
        req.setInArrayDateTime1D(input);
        assertNotNull(stub.retArrayDateTime1D(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayString2D() throws java.lang.Exception {
        //TODO codegen issue
//        assertNull(stub.retArrayString2D(null));
        ComplexDataTypesDocLitBareStub.InArrayString2D req =
                new ComplexDataTypesDocLitBareStub.InArrayString2D();
        assertNotNull(stub.retArrayString2D(req));

        ComplexDataTypesDocLitBareStub.ArrayOfArrayOfstring input = new ComplexDataTypesDocLitBareStub.ArrayOfArrayOfstring();
        ComplexDataTypesDocLitBareStub.ArrayOfstring a2 = new ComplexDataTypesDocLitBareStub.ArrayOfstring();
        ComplexDataTypesDocLitBareStub.ArrayOfstring a1 = new ComplexDataTypesDocLitBareStub.ArrayOfstring();
        a1.setString(new String[]{"foo", "bar"});
        input.setArrayOfstring(new ComplexDataTypesDocLitBareStub.ArrayOfstring[]{a1, a2});
        req.setInArrayString2D(input);
        assertNotNull(stub.retArrayString2D(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayDecimal1D() throws java.lang.Exception {

//        assertNull(stub.retArrayDecimal1D(null));
        ComplexDataTypesDocLitBareStub.InArrayDecimal1D req =
                new ComplexDataTypesDocLitBareStub.InArrayDecimal1D();
        assertNotNull(stub.retArrayDecimal1D(req));

        ComplexDataTypesDocLitBareStub.ArrayOfNullableOfdecimal input = new ComplexDataTypesDocLitBareStub.ArrayOfNullableOfdecimal();
        input.setDecimal(new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
        req.setInArrayDecimal1D(input);
        assertNotNull(stub.retArrayDecimal1D(req));
    }

    /**
     * Auto generated test method
     */
    public void testretStructSNSA() throws java.lang.Exception {

//        assertNull(stub.retStructSNSA(null));
        ComplexDataTypesDocLitBareStub.InStructSNSA req = new ComplexDataTypesDocLitBareStub.InStructSNSA();
        assertNotNull(stub.retStructSNSA(req));
        ComplexDataTypesDocLitBareStub.Employee input = new ComplexDataTypesDocLitBareStub.Employee();
        input.setJobID(34);
        input.setBaseDetails(new ComplexDataTypesDocLitBareStub.Person());
        input.setNumbers(new ComplexDataTypesDocLitBareStub.ArrayOfshort());
        input.setHireDate(Calendar.getInstance());
        req.setInStructSNSA(input);
        assertNotNull(stub.retStructSNSA(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayAnyType1D() throws java.lang.Exception {

//        assertNull(stub.retArrayAnyType1D(null));
        ComplexDataTypesDocLitBareStub.InArrayAnyType1D req = new ComplexDataTypesDocLitBareStub.InArrayAnyType1D();
        assertNotNull(stub.retArrayAnyType1D(req));
        ComplexDataTypesDocLitBareStub.ArrayOfanyType input = new ComplexDataTypesDocLitBareStub.ArrayOfanyType();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        // lets create the namespace object of the Article element
        OMNamespace ns = factory.createOMNamespace("http://www.ibm.com/developerworks/library/ws-axis2soap/index.html", "article");
        // now create the Article element with the above namespace
        OMElement articleElement = factory.createOMElement("Article", ns);

        // comment out this test case since now adb uses Object to represent any type
        
        // input.setAnyType(new OMElement[]{articleElement});
        // req.setInArrayAnyType1D(input);
        // assertNotNull(stub.retArrayAnyType1D(req));
        //TODOD : Need to fix this , seems like we are not getting the corrcet response
    }


    /**
     * Auto generated test method
     */
    public void testretStructSN() throws java.lang.Exception {

//        assertNull(stub.retStructSN(null));
        ComplexDataTypesDocLitBareStub.InStructSN req = new ComplexDataTypesDocLitBareStub.InStructSN();
        assertNotNull(stub.retStructSN(req));

        ComplexDataTypesDocLitBareStub.Person input = new ComplexDataTypesDocLitBareStub.Person();
        input.setAge(23);
        input.setId(345);
        input.setMale(false);
        input.setName("Why?");
        req.setInStructSN(input);
        assertNotNull(stub.retStructSN(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArray1DSN() throws java.lang.Exception {
//TODO Codegen issue
//        assertNull(stub.retArray1DSN(null));

        ComplexDataTypesDocLitBareStub.InArray1DSN req = new ComplexDataTypesDocLitBareStub.InArray1DSN();
        assertNotNull(stub.retArray1DSN(req));

        ComplexDataTypesDocLitBareStub.ArrayOfPerson input = new ComplexDataTypesDocLitBareStub.ArrayOfPerson();
        ComplexDataTypesDocLitBareStub.Person p1 = new ComplexDataTypesDocLitBareStub.Person();
        p1.setAge(34);
        p1.setId(2345);
        p1.setMale(true);
        p1.setName("HJHJH");
        input.setPerson(new ComplexDataTypesDocLitBareStub.Person[]{p1});
        req.setInArray1DSN(input);
        assertNotNull(stub.retArray1DSN(req));
        //TODO : Need to fix this , we are not gettin corrcet reponse
    }

    /**
     * Auto generated test method
     */
    public void testretDerivedClass() throws java.lang.Exception {
//        assertNull(stub.retDerivedClass(null));
        ComplexDataTypesDocLitBareStub.InDerivedClass req =
                new ComplexDataTypesDocLitBareStub.InDerivedClass();
        assertNotNull(stub.retDerivedClass(req));

        ComplexDataTypesDocLitBareStub.Furniture input = new ComplexDataTypesDocLitBareStub.Furniture();
        input.setColor("white");
        input.setPrice(67);
        req.setInDerivedClass(input);
        assertNotNull(stub.retDerivedClass(req));
        //TODO : Need to fix this too
    }
//
//// TODO: We need to figure out how to deal with ENUM's. Please don't remove this section.
////    /**
////     * Auto generated test method
////     */
//    public void testretEnumInt() throws java.lang.Exception {
//
//
//        assertNull(stub.retEnumInt(null));
//        String input = "";
//        assertNotNull(stub.retEnumInt(new String()));
//    }
//
//// TODO: We need to figure out how to deal with ENUM's. Please don't remove this section.
////    /**
////     * Auto generated test method
////     */
////    public void testretEnumString() throws java.lang.Exception {
////
////        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
////                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
////
////        assertNull(stub.retEnumString(null));
////        BitMask input = new BitMask();
////        assertNull(stub.retEnumString(new BitMask()));
////    }
//

    //

    /**
     * Auto generated test method
     */
    public void testretStructS1() throws java.lang.Exception {
//        assertNull(stub.retStructS1(null));
        ComplexDataTypesDocLitBareStub.InStructS1 req = new ComplexDataTypesDocLitBareStub.InStructS1();
        assertNotNull(stub.retStructS1(req));
        ComplexDataTypesDocLitBareStub.Name input = new ComplexDataTypesDocLitBareStub.Name();
        input.setName("ewrterty");
        req.setInStructS1(input);
        assertNotNull(stub.retStructS1(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayString1D() throws java.lang.Exception {
//        assertNull(stub.retArrayString1D(null));
        ComplexDataTypesDocLitBareStub.InArrayString1D req = new ComplexDataTypesDocLitBareStub.InArrayString1D();
        assertNotNull(stub.retArrayString1D(req));
        ComplexDataTypesDocLitBareStub.ArrayOfstring input = new ComplexDataTypesDocLitBareStub.ArrayOfstring();
        input.setString(new String[]{"foo", "bar"});
        req.setInArrayString1D(input);
        ComplexDataTypesDocLitBareStub.RetArrayString1DResult ret = stub.retArrayString1D(req);
        assertNotNull(ret);
        assertNotNull(ret.getRetArrayString1DResult().getString());
        assertEquals(ret.getRetArrayString1DResult().getString().length, 2);
    }


    /**
     * Auto generated test method
     */
    public void testretSingle() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InSingle req = new ComplexDataTypesDocLitBareStub.InSingle();
        req.setInSingle(43.0f);
        float ret = stub.retSingle(req).getRetSingleResult();
        assertTrue(ret == 43.0f);
    }

    private SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Auto generated test method
     */
    public void testretDateTime() throws java.lang.Exception {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar input = Calendar.getInstance();
        ComplexDataTypesDocLitBareStub.InDateTime req = new ComplexDataTypesDocLitBareStub.InDateTime();
        req.setInDateTime(input);
        Calendar ret = stub.retDateTime(req).getRetDateTimeResult();
        assertNotNull(ret);
        assertEquals(zulu.format(input.getTime()), zulu.format(ret.getTime()));
    }

    /**
     * Auto generated test method
     */
    public void testretGuid() throws java.lang.Exception {

        String input = "12345";
        ComplexDataTypesDocLitBareStub.InGuid req = new ComplexDataTypesDocLitBareStub.InGuid();
        req.setInGuid(input);
        String ret = stub.retGuid(req).getRetGuidResult();
        assertEquals(ret, input);
    }

    /**
     * Auto generated test method
     */
    public void testretByteArray() throws java.lang.Exception {


        byte[] input = new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        ComplexDataTypesDocLitBareStub.RetByteArray req = new ComplexDataTypesDocLitBareStub.RetByteArray();
        req.setInByteArray(new DataHandler(new ByteArrayDataSource(input)));
        DataHandler ret = stub.retByteArray(req).get_return();
        byte[] bytes = IOUtils.getStreamAsByteArray(ret.getInputStream());
        assertTrue(Arrays.equals(bytes, input));
    }

    /**
     * Auto generated test method
     */
    public void testretUri() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InUri req =
                new ComplexDataTypesDocLitBareStub.InUri();
        req.setInUri("124");
        assertNotNull(stub.retUri(req));
    }

    /**
     * Auto generated test method
     */
//    public void testretQName() throws java.lang.Exception {
//        FIXME: Why is QName being mapped to OMElement?
//        assertNull(stub.retQName(null));
//    }

//// TODO: FIXME: Need to figure out how to do enum's. Please don't remove this following section
////    /**
////     * Auto generated test method
////     */
////    public void testretEnumInt() throws java.lang.Exception {
////
////        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
////                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
////
////        org.tempuri.complex.xsd.xsd.RetEnumInt retEnumInt126 =
////                (org.tempuri.complex.xsd.xsd.RetEnumInt) getTestObject(org.tempuri.complex.xsd.xsd.RetEnumInt.class);
////        // todo Fill in the retEnumInt126 here
////
////        assertNotNull(stub.retEnumInt(
////                getParam0(retEnumInt126)
////        ));
////
////
////    }
//
    public void testretLong() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InLong req = new ComplexDataTypesDocLitBareStub.InLong();
        req.setInLong(34);
        long ret = stub.retLong(req).getRetLongResult();
        assertEquals(34, ret);
    }

    //
    /**
     * Auto generated test method
     */
    public void testretUShort() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InUShort req = new ComplexDataTypesDocLitBareStub.InUShort();
        req.setInUShort(34);
        int ret = stub.retUShort(req).getRetUShortResult();
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretUInt() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InUInt req = new ComplexDataTypesDocLitBareStub.InUInt();
        req.setInUInt(34);
        long ret = stub.retUInt(req).getRetUIntResult();
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretByte() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InByte req = new ComplexDataTypesDocLitBareStub.InByte();
        req.setInByte((short) 34);
        short ret = stub.retByte(req).getRetByteResult();
        assertEquals((short) 34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretSByte() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InSByte req = new ComplexDataTypesDocLitBareStub.InSByte();
        req.setInSByte((byte) 34);
        byte ret = stub.retSByte(req).getRetSByteResult();
        assertEquals((byte) 34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretShort() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InShort req = new ComplexDataTypesDocLitBareStub.InShort();
        req.setInShort((short) 34);
        short ret = stub.retShort(req).getRetShortResult();

        assertEquals((short) 34, ret);
    }

    //
    /**
     * Auto generated test method
     */
    public void testretObject() throws java.lang.Exception {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        // lets create the namespace object of the Article element
        OMNamespace ns = factory.createOMNamespace("http://www.ibm.com/developerworks/library/ws-axis2soap/index.html", "article");
        // now create the Article element with the above namespace
        OMElement articleElement = factory.createOMElement("Article", ns);

        // representing the any type with a OMElement is wrong. it should be an Object
        // adb has fixed this now comment this test to fix this for java2wsdl as well

        // ComplexDataTypesDocLitBareStub.InObject req = new ComplexDataTypesDocLitBareStub.InObject();
        // req.setInObject(articleElement);
        // OMElement ret = stub.retObject(req).getRetObjectResult();
        // assertNotNull(ret);
        // assertEquals(ret.toString(), articleElement.toString());
    }

    /**
     * Auto generated test method
     */
    public void testretFloat() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InFloat req = new ComplexDataTypesDocLitBareStub.InFloat();
        req.setInFloat((float) 34);
        float ret = stub.retFloat(req).getRetFloatResult();
        assertTrue(ret == 34);
    }

    /**
     * Auto generated test method
     */
    public void testretDouble() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InDouble req = new ComplexDataTypesDocLitBareStub.InDouble();
        req.setInDouble(34);
        double ret = stub.retDouble(req).getRetDoubleResult();
        assertTrue(ret == 34);
    }

    /**
     * Auto generated test method
     */
    public void testretBool() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InBool req =
                new ComplexDataTypesDocLitBareStub.InBool();
        req.setInBool(true);
        boolean ret = stub.retBool(req).getRetBoolResult();
        assertTrue(ret);
    }

    /**
     * Auto generated test method
     */
    public void testretDecimal() throws java.lang.Exception {

        BigDecimal input = new BigDecimal(12334);
        ComplexDataTypesDocLitBareStub.InDecimal req = new ComplexDataTypesDocLitBareStub.InDecimal();
        req.setInDecimal(input);
        BigDecimal ret = stub.retDecimal(req).getRetDecimalResult();
        assertNotNull(ret);
        assertEquals(ret, input);
    }

// TODO: FIXME: Need to figure out how to do enum's. Please don't remove this following section
////    /**
////     * Auto generated test method
////     */
////    public void testretEnumString() throws java.lang.Exception {
////
////        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
////                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
////
////        org.tempuri.complex.xsd.xsd.RetEnumString retEnumString198 =
////                (org.tempuri.complex.xsd.xsd.RetEnumString) getTestObject(org.tempuri.complex.xsd.xsd.RetEnumString.class);
////        // todo Fill in the retEnumString198 here
////
////        assertNotNull(stub.retEnumString(
////                getParam0(retEnumString198)
////        ));
////
////
////    }

    //
    /**
     * Auto generated test method
     */
    public void testretInt() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InInt req = new ComplexDataTypesDocLitBareStub.InInt();
        req.setInInt(34);
        int ret = stub.retInt(req).getRetIntResult();
        assertEquals((int) 34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretInts() throws java.lang.Exception {

        int[] input = new int[]{34, 45};
        ComplexDataTypesDocLitBareStub.RetInts req = new ComplexDataTypesDocLitBareStub.RetInts();
        req.setInInt(input);
        int ret[] = stub.retInts(req).get_return();
        assertTrue(Arrays.equals(input, ret));
    }

    /**
     * Auto generated test method
     */
    public void testretChar() throws java.lang.Exception {
        ComplexDataTypesDocLitBareStub.InChar req = new ComplexDataTypesDocLitBareStub.InChar();
        req.setInChar(34);
        int ret = stub.retChar(req).getRetCharResult();
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretString() throws java.lang.Exception {
        String input = "Abracadabra";
        ComplexDataTypesDocLitBareStub.InString req = new ComplexDataTypesDocLitBareStub.InString();
        req.setInString(input);
        String ret = stub.retString(req).getRetStringResult();
        assertNotNull(ret);
        assertEquals(input, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretStrings() throws java.lang.Exception {

        String[] ret;
        String[] input = new String[]{"Abracadabra"};
        ComplexDataTypesDocLitBareStub.RetStrings req = new ComplexDataTypesDocLitBareStub.RetStrings();
        req.setInString(input);
        ret = stub.retStrings(req).get_return();
        assertNotNull(ret);
        ret = stub.retStrings(req).get_return();
        assertNotNull(ret);
        assertTrue(Arrays.equals(input, ret));
        input = new String[]{"Abracadabra", null, "abc"};
        req.setInString(input);
        ret = stub.retStrings(req).get_return();
        assertNotNull(ret);
        assertTrue(Arrays.equals(input, ret));

        input = new String[]{};
        req.setInString(input);
        ret = stub.retStrings(req).get_return();
        assertNull(ret);
    }

    /**
     * Auto generated test method
     */
    public void testretULong() throws java.lang.Exception {

        BigInteger input = new BigInteger("34");
        ComplexDataTypesDocLitBareStub.InULong req = new ComplexDataTypesDocLitBareStub.InULong();
        req.setInULong(input);
        BigInteger ret = stub.retULong(req).getRetULongResult();
        assertEquals(input, ret);
    }


}
