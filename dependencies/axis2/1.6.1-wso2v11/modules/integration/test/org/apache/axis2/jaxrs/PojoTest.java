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
package org.apache.axis2.jaxrs;


import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.Constants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class PojoTest extends UtilServerBasedTestCase{

    private static final Log log = LogFactory.getLog(PojoTest.class);
         private AxisService axisService;


        public PojoTest(){
            super(PojoTest.class.getName());

        }

        public PojoTest(String name) {
            super(name);

        }

         public static Test suite() {
        return getTestSetup2(new TestSuite(PojoTest.class),
                             TestingUtils.prefixBaseDirectory(
                                     Constants.TESTING_PATH + "jaxrs-repository"));
        }


    public void testAddDataFromURL() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/add/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_POST);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<addDataFromURL xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1></addDataFromURL>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account created", returnElem.getText() );

          }

          public void testAddDataFromURLandBody() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/getFromBody/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_POST);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<addDataFromURLandBody xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1><data2>password</data2></addDataFromURLandBody>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account and password created", returnElem.getText() );

          }


          public void testUpdateDataFromURL() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/update/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_PUT);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<addDataFromURL xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1></addDataFromURL>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account updated", returnElem.getText() );

          }

          public void testUpdateDataFromURLandBody() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/getFromBody/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_PUT);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<addDataFromURLandBody xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1><data2>password</data2></addDataFromURLandBody>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account and password updated", returnElem.getText() );

          }

    public void testGetDataFromURL() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/get/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_GET);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<getDataFromURL xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1></getDataFromURL>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account read", returnElem.getText() );

          }

          public void testGetDataFromURLandBody() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/getFromBody/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_GET);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<addDataFromURLandBody xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1><data2>password</data2></addDataFromURLandBody>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account and password read", returnElem.getText() );

          }



         public void testDeleteDataFromURL() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/delete/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_DELETE);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<deleteDataFromURL xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1></deleteDataFromURL>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account deleted", returnElem.getText() );

          }

          public void testDeleteDataFromURLandBody() throws Exception{

            Options options= TestUtil.getPojoTestOptions();
            // setting test case dependent settings
            options.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,"testroot/getFromBody/{data1}");
            options.setProperty(Constants.Configuration.HTTP_METHOD,Constants.Configuration.HTTP_METHOD_DELETE);
            options.setProperty(Constants.Configuration.CONTENT_TYPE, Constants.MIME_CT_TEXT_XML);

            ServiceClient sender= TestUtil.getPojoTestServiceClient(options);
            String stringPayload = "<addDataFromURLandBody xmlns=\"http://ws.apache.org/axis2\"><data1>account</data1><data2>password</data2></addDataFromURLandBody>";
            OMElement payload= AXIOMUtil.stringToOM(stringPayload);
            OMElement respond=sender.sendReceive(payload);
            OMElement returnElem=respond.getFirstChildWithName(new QName("return"));
            assertEquals("account and password deleted", returnElem.getText() );

          }

}
