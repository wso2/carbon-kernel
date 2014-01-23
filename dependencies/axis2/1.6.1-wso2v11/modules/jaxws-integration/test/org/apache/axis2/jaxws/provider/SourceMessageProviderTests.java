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

package org.apache.axis2.jaxws.provider;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SourceMessageProviderTests extends ProviderTestCase {

    private String endpointUrl = "http://localhost:6060/axis2/services/SourceMessageProviderService.SourceMessageProviderPort";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "SourceMessageProviderService");
    private String xmlDir = "xml";

    public SourceMessageProviderTests() {
        super();
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(SourceMessageProviderTests.class));
    }

    public void testProviderSource(){
        try{
            String resourceDir = new File(providerResourceDir, xmlDir).getAbsolutePath();
            String fileName = resourceDir+File.separator+"web.xml";

            File file = new File(fileName);
            InputStream inputStream = new FileInputStream(file);
            StreamSource xmlStreamSource = new StreamSource(inputStream);

            Service svc = Service.create(serviceName);
            svc.addPort(portName,null, endpointUrl);
            Dispatch<Source> dispatch = svc.createDispatch(portName, Source.class, null);
            TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
            Source response = dispatch.invoke(xmlStreamSource);

            TestLogger.logger.debug(">> Response [" + response.toString() + "]");
            
            // Try again to verify
            TestLogger.logger.debug(">> Invoking SourceMessageProviderDispatch");
            inputStream = new FileInputStream(file);
            xmlStreamSource = new StreamSource(inputStream);
            response = dispatch.invoke(xmlStreamSource);

            TestLogger.logger.debug(">> Response [" + response.toString() + "]");

        }catch(Exception e){
        	e.printStackTrace();
            fail("Caught exception " + e);
        }
        
    }
}
