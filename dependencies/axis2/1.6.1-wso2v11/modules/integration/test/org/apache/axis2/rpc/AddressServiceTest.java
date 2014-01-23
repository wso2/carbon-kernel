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

package org.apache.axis2.rpc;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;

import javax.xml.namespace.QName;

public class AddressServiceTest extends LocalTestCase
{
    protected void setUp() throws Exception {
        super.setUp();
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_ONLY,
                                        new RPCInOnlyMessageReceiver());
        serverConfig.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT,
                                        new RPCMessageReceiver());
        deployClassAsService("AddressBookService", AddressBookService.class);
    }
    
    public void testAddAndFetchEntry() throws Exception {
        QName opAddEntry = new QName("http://rpc.axis2.apache.org", "addEntry");

        Entry entry = new Entry();

        entry.setName("Abby Cadabby");
        entry.setStreet("Sesame Street");
        entry.setCity("Sesame City");
        entry.setState("Sesame State");
        entry.setPostalCode("11111");

        // Constructing the arguments array for the method invocation
        Object[] opAddEntryArgs = new Object[] { entry };

        // Invoking the method
        RPCServiceClient serviceClient = getRPCClient();
        Options options = serviceClient.getOptions();
        EndpointReference targetEPR = new EndpointReference(
                "local://services/AddressBookService");
        options.setTo(targetEPR);
        options.setAction("addEntry");

        Object[] ret = serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs, new Class[]{Integer.class});
        assertEquals(ret[0], new Integer(1));

        QName opFindEntry = new QName("http://rpc.axis2.apache.org", "findEntry");
        String name = "Abby Cadabby";

        Object[] opFindEntryArgs = new Object[] { name };
        Class[] returnTypes = new Class[] { Entry.class };

        RPCServiceClient serviceClient2 = getRPCClient();
        Options options2 = serviceClient2.getOptions();
        EndpointReference targetEPR2 = new EndpointReference(
                "local://services/AddressBookService");
        options2.setTo(targetEPR2);
        options2.setAction("findEntry");
        Object[] response = serviceClient2.invokeBlocking(opFindEntry,
                opFindEntryArgs, returnTypes);

        Entry result = (Entry) response[0];
        assertNotNull(result);

        System.out.println("Name   :" + result.getName());
        System.out.println("Street :" + result.getStreet());
        System.out.println("City   :" + result.getCity());
        System.out.println("State  :" + result.getState());
        System.out.println("Postal Code :" + result.getPostalCode());
    }

    public void testEntry1() throws Exception {
        QName opAddEntry = new QName("http://rpc.axis2.apache.org", "getEntries1");

        Object[] opAddEntryArgs = new Object[] { };

        // Invoking the method
        RPCServiceClient serviceClient = getRPCClient();
        Options options = serviceClient.getOptions();
        EndpointReference targetEPR = new EndpointReference(
                "local://services/AddressBookService");
        options.setTo(targetEPR);
        options.setAction("getEntries1");

        Object[] result = serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs, new Class[]{Entry[].class});
        assertNotNull(result);
        Entry[] entries = (Entry[]) result[0];
        assertEquals(entries.length, 2);
    }

    public void testEntry2() throws Exception {
        QName opAddEntry = new QName("http://rpc.axis2.apache.org", "getEntries2");

        Object[] opAddEntryArgs = new Object[] { };

        // Invoking the method
        RPCServiceClient serviceClient = getRPCClient();
        Options options = serviceClient.getOptions();
        EndpointReference targetEPR = new EndpointReference(
                "local://services/AddressBookService");
        options.setTo(targetEPR);
        options.setAction("getEntries2");

        Object[] result = serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs, new Class[]{Entry[].class});
        assertNotNull(result);
        Entry[] entries = (Entry[]) result[0];
        assertEquals(entries.length, 0);
    }

    public void testEntry3() throws Exception {
        QName opAddEntry = new QName("http://rpc.axis2.apache.org", "getEntries3");

        Object[] opAddEntryArgs = new Object[] { };

        // Invoking the method
        RPCServiceClient serviceClient = getRPCClient();
        Options options = serviceClient.getOptions();
        EndpointReference targetEPR = new EndpointReference(
                "local://services/AddressBookService");
        options.setTo(targetEPR);
        options.setAction("getEntries3");

        Object[] result = serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs, new Class[]{Entry[].class});
        assertNotNull(result);
        Entry[] entries = (Entry[]) result[0];
        assertEquals(entries.length, 0);
    }
}
