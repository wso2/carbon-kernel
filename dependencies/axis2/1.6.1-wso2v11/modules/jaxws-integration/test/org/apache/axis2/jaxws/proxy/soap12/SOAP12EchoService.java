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

package org.apache.axis2.jaxws.proxy.soap12;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@WebServiceClient(name = "SOAP12EchoService", 
        targetNamespace = "http://jaxws.axis2.apache.org/proxy/soap12", 
        wsdlLocation = "SOAP12Echo.wsdl")
public class SOAP12EchoService
    extends Service
{

    
    private final static URL SOAP12ECHOSERVICE_WSDL_LOCATION;
    private static String wsdlLocation = "/test/org/apache/axis2/jaxws/proxy/soap12/server/META-INF/SOAP12Echo.wsdl";
    static {
        URL url = null;
        try {
            try{
                String baseDir  = new File(System.getProperty("basedir",".")).getCanonicalPath();
                wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
            }catch(Exception e){
                
            }
            File file = new File(wsdlLocation);
            url = file.toURL();
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        SOAP12ECHOSERVICE_WSDL_LOCATION = url;
    }

    public SOAP12EchoService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SOAP12EchoService() {
        super(SOAP12ECHOSERVICE_WSDL_LOCATION, new QName("http://jaxws.axis2.apache.org/proxy/soap12", "SOAP12EchoService"));
    }

    @WebEndpoint(name = "EchoPort")
    public Echo getEchoPort() {
        return (Echo)super.getPort(new QName("http://jaxws.axis2.apache.org/proxy/soap12", "EchoPort"), Echo.class);
    }

}
