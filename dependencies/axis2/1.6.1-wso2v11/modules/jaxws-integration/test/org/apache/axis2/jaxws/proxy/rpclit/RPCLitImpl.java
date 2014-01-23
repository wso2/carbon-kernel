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

package org.apache.axis2.jaxws.proxy.rpclit;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.proxy.rpclit.sei.RPCFault;
import org.apache.axis2.jaxws.proxy.rpclit.sei.RPCLit;
import org.test.proxy.rpclit.ComplexAll;
import org.test.proxy.rpclit.Enum;

import javax.jws.WebService;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.math.BigInteger;

/**
 * 
 *
 */
@WebService(serviceName="RPCLitService",
		targetNamespace="http://org/apache/axis2/jaxws/proxy/rpclit",
		endpointInterface="org.apache.axis2.jaxws.proxy.rpclit.sei.RPCLit")
public class RPCLitImpl implements RPCLit {

    public static DatatypeFactory df ;
    public static XMLGregorianCalendar bday;
    public static XMLGregorianCalendar holiday;
    public static BigInteger bigInt1 = new BigInteger("1");
    public static BigInteger bigInt2 = new BigInteger("2");
    public static QName qname1 = new QName("urn://sample", "hello" );
    public static QName qname2 = new QName("urn://sample", "world" );
    
    static {
        try {
            df = DatatypeFactory.newInstance();
            bday = df.newXMLGregorianCalendarDate(1964, 12, 3,  DatatypeConstants.FIELD_UNDEFINED);
            holiday = bday = df.newXMLGregorianCalendarDate(2007, 1, 1,  DatatypeConstants.FIELD_UNDEFINED);
        } catch (Exception e) {}
    }
    
    
    
    /**
     * Echo the input
     */
    public String testSimple(String simpleIn) {
        assertTrue(simpleIn != null);  // According to JAX-WS an RPC service should never receive a null
        
        // Test to ensure that returning null causes the proper exception 
        if (simpleIn.contains("returnNull")) {
            return null;
        }
        return simpleIn;
    }
    
    /**
     * Echo the input
     */
    public String testSimple2(String simple2In1, String simple2In2) {
        return simple2In1 + simple2In2;
    }
    
    public QName[] testLists(
            QName[] qNames,
            XMLGregorianCalendar[] calendars,
            String[] texts,
            BigInteger[] bigInts,
            Long[] longs,
            Enum[] enums,
            String[] text2,
            ComplexAll all) {
        assertTrue(qNames.length==2);
        assertTrue(qNames[0].equals(qname1));
        assertTrue(qNames[1].equals(qname2));
        
        return qNames;
    }

    public XMLGregorianCalendar[] testCalendarList1(XMLGregorianCalendar[] cals) {
       assertTrue(cals.length == 2);
       assertTrue(cals[0].compare(bday) == 0);
       assertTrue(cals[1].compare(holiday) == 0);
       return cals;
       
    }

    public String[] testStringList2(String[] arg20) {

        assertTrue(arg20.length==2);
        assertTrue(arg20[0].equals("Hello"));
        assertTrue(arg20[1].equals("World"));
        return arg20;
    }

    public BigInteger[] testBigIntegerList3(BigInteger[] arg30) {
        assertTrue(arg30.length==2);
        assertTrue(arg30[0].compareTo(bigInt1) == 0);
        assertTrue(arg30[1].compareTo(bigInt2) == 0);
        return arg30;
    }

    public Long[] testLongList4(Long[] longs) {
        assertTrue(longs.length==3);
        assertTrue(longs[0] == 0);
        assertTrue(longs[1] == 1);
        assertTrue(longs[2] == 2);
        return longs;
    }

    public Enum[] testEnumList5(Enum[] enums) {
        assertTrue(enums.length==3);
        assertTrue(enums[0] == Enum.ONE);
        assertTrue(enums[1] == Enum.TWO);
        assertTrue(enums[2] == Enum.THREE);
        return enums;
    }

    public ComplexAll testComplexAll6(ComplexAll arg60) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String[] testEnumList7(String[] arg70) {
        assertTrue(arg70.length==2);
        assertTrue(arg70[0].equals("Apple"));
        assertTrue(arg70[0].equals("Orange"));
        return arg70;
    }

    private void assertTrue(boolean value) throws RuntimeException {
        if (!value) {
            RuntimeException re = new RuntimeException();
            TestLogger.logger.debug("Test FAILURE=" + re);
            throw re;
        }
    }

    public String testHeader(String bodyParam, String headerParam) {
        return bodyParam + headerParam;
    }

    public void testFault() throws RPCFault {
        throw new RPCFault("Throw RPCFault", 123);
    }

    public String testSimpleInOut(Holder<String> simpleInOut) {
        return simpleInOut.value;
    }

}
