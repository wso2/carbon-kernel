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

package org.apache.axis2.jaxws.sample.addressbook;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.axis2.jaxws.sample.addressbook.data.AddressBookEntry;

@WebService(name = "AddressBook", 
            targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook")
public interface AddressBook {


    /**
     * 
     * @param entry
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(name = "status", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook")
    @RequestWrapper(localName = "addEntry", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook", className = "org.apache.axis2.jaxws.sample.addressbook.data.AddEntry")
    @ResponseWrapper(localName = "addEntryResponse", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook", className = "org.apache.axis2.jaxws.sample.addressbook.data.AddEntryResponse")
    public boolean addEntry(
        @WebParam(name = "entry", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook")
        AddressBookEntry entry);

    /**
     * 
     * @param firstname
     * @param lastname
     * @return
     *     returns org.apache.axis2.jaxws.sample.addressbook.AddressBookEntry
     */
    @WebMethod
    @WebResult(name = "entry", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook")
    @RequestWrapper(localName = "findEntryByName", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook", className = "org.apache.axis2.jaxws.sample.addressbook.data.FindEntryByName")
    @ResponseWrapper(localName = "findEntryByNameResponse", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook", className = "org.apache.axis2.jaxws.sample.addressbook.data.FindEntryByNameResponse")
    public AddressBookEntry findEntryByName(
        @WebParam(name = "firstname", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook")
        String firstname,
        @WebParam(name = "lastname", targetNamespace = "http://org/apache/axis2/jaxws/sample/addressbook")
        String lastname);

}
