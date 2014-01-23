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

package org.apache.axis2.generics;

import org.apache.axis2.integration.RPCLocalTestCase;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import java.util.ArrayList;

public class GenericServiceTest extends RPCLocalTestCase {

//    private SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    //  0123456789 0 123456789

    protected boolean finish = false;

    protected void setUp() throws Exception {
        super.setUp();
        deployClassAsService("GenericService", GenericService.class);
    }

    public void testProcessStringList() throws AxisFault {
        RPCServiceClient sender = getRPCClient("GenericService", "processStringList");
        ArrayList<StringArray> args = new ArrayList<StringArray>();
        args.add(new StringArray());

        Object[] value = sender.invokeBlocking(new QName("http://generics.axis2.apache.org", "processStringList", "req"), args.toArray(),
                new Class[]{String.class});
        assertEquals(value[0], "Test1");
    }

    public void testGetStringList() throws AxisFault {
        RPCServiceClient sender = getRPCClient("GenericService", "getStringList");
        ArrayList<StringArray> args = new ArrayList<StringArray>();
        args.add(new StringArray());

        ArrayList<Class<String>> resobj = new ArrayList<Class<String>>();
        resobj.add(String.class);
        resobj.add(String.class);
        resobj.add(String.class);


        Object[] value = sender.invokeBlocking(new QName("http://generics.axis2.apache.org", "getStringList", "req"), args.toArray(),
                (Class[]) resobj.toArray(new Class[resobj.size()]));
        assertEquals(value[0], "test1");
        assertEquals(value[1], "test2");
        assertEquals(value[2], "test3");
    }


    public void testProcessPersonList() throws AxisFault {
        RPCServiceClient sender = getRPCClient("GenericService", "processPersonList");
        ArrayList<PersonArray> args = new ArrayList<PersonArray>();
        args.add(new PersonArray());

        Object[] value = sender.invokeBlocking(new QName("http://generics.axis2.apache.org", "processPersonList", "req"), args.toArray(),
                new Class[]{Person.class});
        Person person = (Person) value[0];
        assertNotNull(person);
        assertEquals(person.getAge(), 10);
    }

    class StringArray {
        private ArrayList<String> values;

        StringArray() {
            values = new ArrayList<String>();
            values.add("Test1");
            values.add("Test2");
            values.add("Test3");
            values.add("Test4");

        }

        public ArrayList<String> getValues() {
            return values;
        }

        public void setValues(ArrayList<String> values) {
            this.values = values;
        }
    }

    class PersonArray {
        private ArrayList<Person> values;

        PersonArray() {
            values = new ArrayList<Person>();
            values.add(new Person("P1", 10));
            values.add(new Person("P2", 20));
            values.add(new Person("P3", 30));

        }

        public ArrayList<Person> getValues() {
            return values;
        }

        public void setValues(ArrayList<Person> values) {
            this.values = values;
        }
    }
}
