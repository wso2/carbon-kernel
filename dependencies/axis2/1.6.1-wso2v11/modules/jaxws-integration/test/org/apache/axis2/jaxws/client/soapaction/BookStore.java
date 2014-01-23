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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "BookStore", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction")
public interface BookStore {

    /**
     * 
     * @param item
     * @return
     *     returns float
     */
    @WebMethod
    @WebResult(name = "price", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction")
    @RequestWrapper(localName = "getPrice", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", className = "org.apache.axis2.jaxws.client.soapaction.GetPriceType")
    @ResponseWrapper(localName = "getPriceResponse", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", className = "org.apache.axis2.jaxws.client.soapaction.GetPriceResponseType")
    public float getPrice(
        @WebParam(name = "item", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction")
        String item);

    /**
     * 
     * @param item
     * @return
     *     returns float
     */
    @WebMethod(action = "http://jaxws.axis2.apache.org/client/soapaction/getPrice")
    @WebResult(name = "price", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction")
    @RequestWrapper(localName = "getPriceWithAction", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", className = "org.apache.axis2.jaxws.client.soapaction.GetPriceType")
    @ResponseWrapper(localName = "getPriceWithActionResponse", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", className = "org.apache.axis2.jaxws.client.soapaction.GetPriceResponseType")
    public float getPriceWithAction(
        @WebParam(name = "item", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction")
        String item);

    /**
     * 
     * @param item
     * @return
     *     returns int
     */
    @WebMethod
    @WebResult(name = "inventory", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", partName = "inventory")
    @SOAPBinding(parameterStyle = ParameterStyle.BARE)
    public int getInventory(
        @WebParam(name = "item", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", partName = "item")
        String item);

    /**
     * 
     * @param item
     * @return
     *     returns int
     */
    @WebMethod(action = "http://jaxws.axis2.apache.org/client/soapaction/getInventory")
    @WebResult(name = "inventoryWithAction", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", partName = "inventory")
    @SOAPBinding(parameterStyle = ParameterStyle.BARE)
    public int getInventoryWithAction(
        @WebParam(name = "itemWithAction", targetNamespace = "http://jaxws.axis2.apache.org/client/soapaction", partName = "item")
        String item);

}
