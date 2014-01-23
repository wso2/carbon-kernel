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

package org.apache.axis2.jaxws.client.soapaction;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@WebServiceClient(name = "BookStoreService", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", wsdlLocation = "SOAPActionTest.wsdl")
public class BookStoreService
    extends Service
{

    private static URL BOOKSTORESERVICE_WSDL_LOCATION;
    private static String wsdlLocation="/test/org/apache/axis2/jaxws/client/soapaction/server/META-INF/SOAPActionTest.wsdl";
    static {
        URL url = null;
        try {
            try{
                String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
                wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
            }catch(Exception e){
                e.printStackTrace();
            }
            File file = new File(wsdlLocation);
            url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        BOOKSTORESERVICE_WSDL_LOCATION = url;
    }
    
    public BookStoreService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public BookStoreService() {
        super(BOOKSTORESERVICE_WSDL_LOCATION, new QName("http://jaxws.axis2.apache.org/client/soapaction", "BookStoreService"));
    }

    /**
     * 
     * @return
     *     returns BookStore
     */
    @WebEndpoint(name = "BookStorePort")
    public BookStore getBookStorePort() {
        return (BookStore)super.getPort(new QName("http://jaxws.axis2.apache.org/client/soapaction", "BookStorePort"), BookStore.class);
    }

}
