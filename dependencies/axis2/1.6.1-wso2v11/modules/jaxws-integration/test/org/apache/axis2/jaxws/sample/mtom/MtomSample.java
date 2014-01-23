
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

package org.apache.axis2.jaxws.sample.mtom;

import org.test.mtom.ImageDepot;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "MtomSample",
            targetNamespace = "http://org/apache/axis2/jaxws/sample/mtom", 
            wsdlLocation = "META-INF/ImageDepot.wsdl")
public interface MtomSample {


    /**
     * 
     * @param input
     * @return
     *     returns org.test.mtom.ImageDepot
     */
    @WebMethod
    @WebResult(name = "output", targetNamespace = "urn://mtom.test.org")
    @RequestWrapper(localName = "sendImage", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendImage")
    @ResponseWrapper(localName = "sendImageResponse", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendImageResponse")
    public ImageDepot sendImage(
        @WebParam(name = "input", targetNamespace = "urn://mtom.test.org")
        ImageDepot input);

    /**
     * 
     * @param input
     * @return
     *     returns org.test.mtom.ImageDepot
     */
    @WebMethod
    @WebResult(name = "output", targetNamespace = "urn://mtom.test.org")
    @RequestWrapper(localName = "sendText", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendText")
    @ResponseWrapper(localName = "sendTextResponse", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendTextResponse")
    public ImageDepot sendText(
        @WebParam(name = "input", targetNamespace = "urn://mtom.test.org")
        byte[] input);

}
