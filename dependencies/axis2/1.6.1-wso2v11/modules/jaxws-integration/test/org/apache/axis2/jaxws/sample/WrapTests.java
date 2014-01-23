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
 * 
 */
package org.apache.axis2.jaxws.sample;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.datasource.jaxb.JAXBCustomBuilderMonitor;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrapService;
import org.test.sample.wrap.Header;
import org.test.sample.wrap.HeaderPart0;
import org.test.sample.wrap.HeaderPart1;
import org.test.sample.wrap.HeaderResponse;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

public class WrapTests extends AbstractTestCase {

    String axisEndpoint = "http://localhost:6060/axis2/services/DocLitWrapService.DocLitWrapImplPort";
	
    // String containing some characters that require XML encoding
    private static String XMLCHARS = "<<<3>>>3>>>3";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(WrapTests.class));
    }

        /**
         * Get theDocLitWrap Prxoy
         * @return DocLitWrapProxy
         */
        private DocLitWrap getProxy() {
            DocLitWrapService service = new DocLitWrapService();
            DocLitWrap proxy = service.getDocLitWrapPort();
            BindingProvider p = (BindingProvider) proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
            return proxy;
        }
	
        public void testTwoWaySync(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String reqString = "Test twoWay Sync";
                DocLitWrap proxy = getProxy();

                String response = proxy.twoWay(reqString);
                TestLogger.logger.debug("Sync Response =" + response);
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        public void testOneWayVoidWithNoInputParams(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{

                DocLitWrapService service = new DocLitWrapService();
                DocLitWrap proxy = getProxy();
                proxy.oneWayVoid();
                
                // Repeat to ensure correct behavior
                proxy.oneWayVoid();

                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        public void testTwoWayHolder(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String holderString = new String("Test twoWay Sync");
                Integer holderInteger = new Integer(0);
                Holder<String> strHolder = new Holder<String>(holderString);
                Holder<Integer> intHolder = new Holder<Integer>(holderInteger);
                DocLitWrap proxy = getProxy();
                proxy.twoWayHolder(strHolder, intHolder);
                TestLogger.logger.debug("Holder Response String =" + strHolder.value);;
                TestLogger.logger.debug("Holder Response Integer =" + intHolder.value);
                
                // Repeat to ensure correct behavior
                proxy.twoWayHolder(strHolder, intHolder);
                TestLogger.logger.debug("Holder Response String =" + strHolder.value);;
                TestLogger.logger.debug("Holder Response Integer =" + intHolder.value);
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        public void testTwoWayWithHeadersAndHolders(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                Header header = new Header();
                header.setOut(0);
                HeaderPart0 hp0= new HeaderPart0();
                hp0.setHeaderType("Client setup Header Type for HeaderPart0");
                HeaderPart1 hp1 = new HeaderPart1();
                hp1.setHeaderType("Client setup Header Type for HeaderPart0");
                Holder<HeaderPart0> holder = new Holder<HeaderPart0>(hp0);
                DocLitWrap proxy = getProxy();
                HeaderResponse hr = proxy.header(header, holder, hp1);
                hp0=holder.value;
                TestLogger.logger.debug("Holder Response String =" + hp0.getHeaderType());
                TestLogger.logger.debug("Header Response Long =" + hr.getOut());
                
                // Repeat to ensure correct behavior
                hr = proxy.header(header, holder, hp1);
                hp0=holder.value;
                TestLogger.logger.debug("Holder Response String =" + hp0.getHeaderType());
                TestLogger.logger.debug("Header Response Long =" + hr.getOut());
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        public void testTwoWayHolderAsync(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String holderString = new String("Test twoWay Sync");
                Integer holderInteger = new Integer(0);
                Holder<String> strHolder = new Holder<String>(holderString);
                Holder<Integer> intHolder = new Holder<Integer>(holderInteger);
                DocLitWrap proxy = getProxy();
                proxy.twoWayHolder(strHolder, intHolder);
                TestLogger.logger.debug("Holder Response String =" + strHolder.value);;
                TestLogger.logger.debug("Holder Response Integer =" + intHolder.value);
                
                // Repeat 
                proxy.twoWayHolder(strHolder, intHolder);
                TestLogger.logger.debug("Holder Response String =" + strHolder.value);;
                TestLogger.logger.debug("Holder Response Integer =" + intHolder.value);
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        /**
         * This is a test of a doc/lit echo test
         */
        public void testEchoString() {
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String request = "hello world";

                DocLitWrap proxy = getProxy();
                String response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                
                // Repeat
                response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }
        
        /**
         * This is a test of a doc/lit method that passes the 
         * request in a header.  This can only be reproduced via
         * annotations and WSGEN.  WSImport will not allow this.
         */
        public void testEchoStringWSGEN1() {
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String request = "hello world";

                DocLitWrap proxy = getProxy();
                String response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                
                // Repeat
                response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        /**
         * This is a test of a doc/lit method that passes the 
         * response in a header.  This can only be reproduced via
         * annotations and WSGEN.  WSImport will not allow this.
         */

        public void testEchoStringWSGEN2() {
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String request = "hello world 2";

                DocLitWrap proxy = getProxy();
                String response = proxy.echoStringWSGEN2(request);
                assertTrue(response.equals(request));
                
                // Repeat
                response = proxy.echoStringWSGEN2(request);
                assertTrue(response.equals(request));
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }
        
        /**
         * This is a test of a doc/lit echo test with xml chars.
         */
        public void testEchoString_xmlchars() {
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String request = XMLCHARS;

                DocLitWrap proxy = getProxy();
                String response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                
                // Repeat
                response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }
        
        /**
         * This is a test of a doc/lit method that passes the 
         * request in a header.  This can only be reproduced via
         * annotations and WSGEN.  WSImport will not allow this.
         */
        public void testEchoStringWSGEN1_xmlchars() {
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String request = XMLCHARS;

                DocLitWrap proxy = getProxy();
                String response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                
                // Repeat
                response = proxy.echoStringWSGEN1(request);
                assertTrue(response.equals(request));
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }

        /**
         * This is a test of a doc/lit method that passes the 
         * response in a header.  This can only be reproduced via
         * annotations and WSGEN.  WSImport will not allow this.
         */

        public void testEchoStringWSGEN2_xmlchars() {
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test : " + getName());
            try{
                String request = XMLCHARS;

                DocLitWrap proxy = getProxy();
                String response = proxy.echoStringWSGEN2(request);
                assertTrue(response.equals(request));
                
                // Repeat
                response = proxy.echoStringWSGEN2(request);
                assertTrue(response.equals(request));
                TestLogger.logger.debug("------------------------------");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }
        /**
         * Test to validate whether a JAXBCustomBuilder is plugged in
         * on the server.
         */
        public void testJAXBCB_Server1(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test  : " + getName());
            try{
                String reqString = "JAXBCustomBuilderServer1";
                DocLitWrap proxy = getProxy();
                
                // Start Monitoring
                proxy.twoWay("JAXBCustomBuilderMonitorStart");
                
                String response = proxy.twoWay(reqString);
                // The returned response will contain the number of JAXBCustomBuilders
                // for the server this could be any number 0 or greater.
                TestLogger.logger.debug("Response 1 =" + response);
                String response2 = proxy.twoWay(reqString);
                TestLogger.logger.debug("Response 2 =" + response2);
                // The returned response will contain the number of JAXBCustomBuilders
                // this could be any number 1 or greater.  The assumption is that
                // the JAXBCustomBuilder will be installed on the second invoke
                Integer r = Integer.parseInt(response2);
                assertTrue(r.intValue() >= 1);
                TestLogger.logger.debug("------------------------------");
                
                // End Monitoring
                proxy.twoWay("JAXBCustomBuilderMonitorEnd");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }
        
        /**
         * Test to validate whether a JAXBCustomBuilder is plugged in
         * and used on the server.
         */
        public void testJAXBCB_Server2(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test  : " + getName());
            try{
                String reqString = "JAXBCustomBuilderServer2";
                DocLitWrap proxy = getProxy();
                
                // Start Monitoring
                proxy.twoWay("JAXBCustomBuilderMonitorStart");
                
                String response = proxy.twoWay(reqString);
                // The returned response will contain the number of JAXBCustomBuilders
                // usages.
                TestLogger.logger.debug("Response 1 =" + response);
                Integer r1 = Integer.parseInt(response);
                String response2 = proxy.twoWay(reqString);
                TestLogger.logger.debug("Response 2 =" + response2);
                // The returned response will contain the number of JAXBCustomBuilders
                // usages.  This should be greater than the first response
                Integer r2 = Integer.parseInt(response2);
                assertTrue(r2.intValue() > r1.intValue());
                TestLogger.logger.debug("------------------------------");
                

                // End Monitoring
                proxy.twoWay("JAXBCustomBuilderMonitorEnd");
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
        }
        
        /**
         * Test to validate whether a JAXBCustomBuilder is plugged and used
         * on the client
         */
        public void testJAXBCB_Client(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test  : " + getName());
            try{
                String reqString = "JAXBCustomBuilderClient";
                DocLitWrap proxy = getProxy();
                
                // Start Monitoring
                JAXBCustomBuilderMonitor.setMonitoring(true);
                JAXBCustomBuilderMonitor.clear();
                
                // Invoke the web services
                proxy.twoWay(reqString);
                
                // The second invoke should trigger the fast
                // unmarshalling of the response
                proxy.twoWay(reqString);
                
                
                // The returned response unmarshalling should try
                // the JAXBCustomBuilder
                int totalBuilders = JAXBCustomBuilderMonitor.getTotalBuilders();
                assertTrue(totalBuilders >= 1);
                int totalCreates = JAXBCustomBuilderMonitor.getTotalCreates();
                assertTrue(totalCreates >= 1);
                
                TestLogger.logger.debug("------------------------------");
                
            }catch(Exception e){
                e.printStackTrace();
                fail();
            } finally {
                JAXBCustomBuilderMonitor.setMonitoring(false);
            }
        }
        
        /**
         * Test to validate whether a JAXBCustomBuilder is plugged and used
         * on the client
         */
        public void testJAXBCB_Client_withHighFidelity(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test  : " + getName());
            try{
                String reqString = "JAXBCustomBuilderClient";
                DocLitWrap proxy = getProxy();
                
                BindingProvider p = (BindingProvider) proxy;
                p.getRequestContext().put(org.apache.axis2.jaxws.Constants.JAXWS_PAYLOAD_HIGH_FIDELITY, Boolean.TRUE);
                
                // Start Monitoring
                JAXBCustomBuilderMonitor.setMonitoring(true);
                JAXBCustomBuilderMonitor.clear();
                
                // Invoke the web services
                proxy.twoWay(reqString);
                
                // The second invoke should trigger the fast
                // unmarshalling of the response
                proxy.twoWay(reqString);
                
                
                // The returned response unmarshalling should try
                // the JAXBCustomBuilder
                int totalBuilders = JAXBCustomBuilderMonitor.getTotalBuilders();
                assertTrue(totalBuilders >= 1);
                int totalCreates = JAXBCustomBuilderMonitor.getTotalCreates();
                assertTrue("Expected 0, but received " + totalCreates, totalCreates == 0);
                
                TestLogger.logger.debug("------------------------------");
                
            }catch(Exception e){
                e.printStackTrace();
                fail();
            } finally {
                JAXBCustomBuilderMonitor.setMonitoring(false);
            }
        }
        
        /**
         * Test to validate whether a JAXBCustomBuilder is plugged in
         * on the client.  Also makes sure that the JAXBCustomBuilder
         * falls back to normal processing when faults are thrown.
         */
        public void testJAXBCB_Fault(){
            TestLogger.logger.debug("------------------------------");
            TestLogger.logger.debug("Test  : " + getName());
            try{
                String reqNormalString = "JAXBCustomBuilderClient";
                String reqFaultString = "JAXBCustomBuilderFault";
                DocLitWrap proxy = getProxy();
                
                // Start Monitoring
                JAXBCustomBuilderMonitor.setMonitoring(true);
                JAXBCustomBuilderMonitor.clear();
                
                try {
                    // Invoke the web services
                    proxy.twoWay(reqNormalString);
                    
                    // This second invoke will cause
                    // an exception to be thrown.
                    proxy.twoWay(reqFaultString);
                    
                    // An exception was expected
                    assertTrue(false);
                } catch (WebServiceException wse) {
                    // An exception is expected
                    // The returned response unmarshalling should try
                    // the JAXBCustomBuilder but fallback to normal unmarshalling
                    // due to the presense of a SOAPFault
                    int totalBuilders = JAXBCustomBuilderMonitor.getTotalBuilders();
                    assertTrue(totalBuilders >= 1);
                    int totalCreates = JAXBCustomBuilderMonitor.getTotalCreates();
                    assertTrue(totalCreates == 0);
                    int totalFailedCreates = 
                            JAXBCustomBuilderMonitor.getTotalFailedCreates();
                    assertTrue(totalFailedCreates >= 1);
                     
                } 
                TestLogger.logger.debug("------------------------------");
                
            } catch(Exception e){
                e.printStackTrace();
                fail();
            } finally {
                JAXBCustomBuilderMonitor.setMonitoring(false);
            }
        }
    
}
