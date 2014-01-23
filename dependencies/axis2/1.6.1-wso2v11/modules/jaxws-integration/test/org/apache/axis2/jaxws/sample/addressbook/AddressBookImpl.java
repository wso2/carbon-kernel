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

import org.apache.axis2.jaxws.TestLogger;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.axis2.jaxws.sample.addressbook.data.AddressBookEntry;
import org.apache.axis2.jaxws.sample.addressbook.data.ObjectFactory;

@WebService(serviceName="AddressBookService",
			endpointInterface="org.apache.axis2.jaxws.sample.addressbook.AddressBook")
public class AddressBookImpl implements AddressBook {

    private static ArrayList<AddressBookEntry> data;
    
    static {
        data = new ArrayList<AddressBookEntry>();
        
        ObjectFactory factory = new ObjectFactory();
        AddressBookEntry entry = factory.createAddressBookEntry();
        entry.setFirstName("Joe");
        entry.setLastName("Test");
        entry.setStreet("1214 Test Ln.");
        entry.setCity("Austin");
        entry.setState("TX");
        data.add(entry);
        
        entry = factory.createAddressBookEntry();
        entry.setFirstName("Sue");
        entry.setLastName("Testfield");
        entry.setStreet("780 1st St.");
        entry.setCity("New York");
        entry.setState("NY");
        data.add(entry);
    }
    
    public boolean addEntry(AddressBookEntry entry) {
        if (entry != null) {
            TestLogger.logger.debug("New AddressBookEntry received");
            TestLogger.logger
                    .debug("       [name] " + entry.getLastName() + ", " + entry.getFirstName());
            TestLogger.logger.debug("      [phone] " + entry.getPhone());
            TestLogger.logger.debug("     [street] " + entry.getStreet());
            TestLogger.logger.debug("[city, state] " + entry.getCity() + ", " + entry.getState());
            data.add(entry);
            return true;
        }
        else {
            return false;
        }
    }

    public AddressBookEntry findEntryByName(String firstname, String lastname) {
        TestLogger.logger.debug("New request received.");
        TestLogger.logger.debug("Looking for entry: [" + firstname + "] [" + lastname + "]");
        Iterator<AddressBookEntry> i = data.iterator();
        while (i.hasNext()) {
            AddressBookEntry entry = i.next();
            
            //If they have a firstname and it doesn't match, just go on
            //to the next entry.
            if (firstname != null) {
                if (!firstname.equals(entry.getFirstName()))
                    continue;                    
            }
            
            if (lastname != null) {
                if (lastname.equals(entry.getLastName()))
                    return entry;
            }
        }
        
        return null;
    }
    
}
