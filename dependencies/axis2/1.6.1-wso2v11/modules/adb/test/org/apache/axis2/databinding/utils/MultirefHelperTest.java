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

package org.apache.axis2.databinding.utils;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;

import javax.xml.stream.XMLStreamException;
import java.util.Iterator;


public class MultirefHelperTest extends TestCase {
     public void testProcessHrefAttributes1(){
        String bodyElement = "<soap:body xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <operation>\n" +
                "        <arg1 href=\"#obj1\"/>\n" +
                "    </operation>\n" +
                "    <multiref id=\"obj1\">\n" +
                "        <name>the real argument</name>\n" +
                "        <color>blue</color>\n" +
                "    </multiref>\n" +
                "</soap:body>";
        testProcessHrefAttributes(bodyElement);
    }

    public void testProcessHrefAttribute2(){
        String bodyElement = "<soapenv:Body xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <ns1:operation xmlns:ns1=\"http:temp1.org\">\n" +
                "        <ns1:arg1 href=\"#obj1\"/>\n" +
                "    </ns1:operation>\n" +
                "    <ns2:multiref id=\"obj1\"  xmlns:ns2=\"http:temp1.org\">\n" +
                "        <ns2:name>the real argument</ns2:name>\n" +
                "        <ns2:color>blue</ns2:color>\n" +
                "    </ns2:multiref>\n" +
                "</soapenv:Body>";
        testProcessHrefAttributes(bodyElement);
    }

    public void testProcessHrefAttribute3(){
        String bodyElement = "<soapenv:Body xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "        <ns1:operation xmlns:ns1=\"http:temp1.org\">\n" +
                "            <ns1:arg1 href=\"#obj1\"/>\n" +
                "            <ns1:operation2>\n" +
                "                <ns1:arg1 href=\"#obj1\"/>\n" +
                "            </ns1:operation2>\n" +
                "        </ns1:operation>\n" +
                "        <ns2:multiref xmlns:ns2=\"http:temp1.org\" id=\"obj1\">\n" +
                "            <ns2:name>the real argument</ns2:name>\n" +
                "            <ns2:color>blue</ns2:color>\n" +
                "        </ns2:multiref>\n" +
                "    </soapenv:Body>";
        testProcessHrefAttributes(bodyElement);
    }

    public void testProcessHrefAttribute4(){
        String bodyElement = "<soapenv:Body xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <ns1:operation xmlns:ns1=\"http:temp1.org\">\n" +
                "        <ns1:arg1 href=\"#obj1\"/>\n" +
                "        <ns1:operation2>\n" +
                "            <ns1:arg1 href=\"#obj1\"/>\n" +
                "            <ns1:test href=\"#obj2\"/>\n" +
                "        </ns1:operation2>\n" +
                "    </ns1:operation>\n" +
                "    <ns2:multiref xmlns:ns2=\"http:temp1.org\" id=\"obj1\">\n" +
                "        <ns2:name>the real argument</ns2:name>\n" +
                "        <ns2:color>blue</ns2:color>\n" +
                "        <ns2:person href=\"#obj2\"/>\n" +
                "    </ns2:multiref>\n" +
                "    <multiref id=\"obj2\">\n" +
                "        <age>23</age>\n" +
                "        <name>amila</name>\n" +
                "    </multiref>\n" +
                "</soapenv:Body>";
        testProcessHrefAttributes(bodyElement);
    }

    private void testProcessHrefAttributes(String bodyElement){

        try {
            OMElement generatedElement = AXIOMUtil.stringToOM(bodyElement);
            SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope soapEnvelope =  soapFactory.getDefaultEnvelope();
            OMElement omElement = null;
            for (Iterator iter = generatedElement.getChildElements();iter.hasNext();){
                omElement = (OMElement) iter.next();
                soapEnvelope.getBody().addChild(omElement);
            }

            String omElementString;
            omElementString = soapEnvelope.toString();
            System.out.println("OM Element before processing ==> " + omElementString);
            MultirefHelper.processHrefAttributes(soapEnvelope);
            omElementString = soapEnvelope.toString();
            System.out.println("OM Element after processing ==> " + omElementString);


        } catch (XMLStreamException e) {
            fail();
        } catch (AxisFault axisFault) {
            fail();
        }

    }
}
