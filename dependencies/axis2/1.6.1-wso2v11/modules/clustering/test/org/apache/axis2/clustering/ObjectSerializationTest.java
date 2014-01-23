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

package org.apache.axis2.clustering;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 */
public class ObjectSerializationTest extends TestCase {

    public void testSerialization() throws IOException, ClassNotFoundException {
        TestDO testDO = new TestDO("name", "value");
        TestDO testDO2 = (TestDO) copy(testDO);

        assertNotNull(testDO2);
        assertFalse(testDO.equals(testDO2));
        assertEquals(testDO.getName(), testDO2.getName());
        assertEquals(testDO.getValue(), testDO2.getValue());
    }

    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public Object copy(Object orig) throws ClassNotFoundException, IOException {
        Object obj = null;
        // Write the object out to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(orig);
        out.flush();
        out.close();

        // Make an input stream from the byte array and read
        // a copy of the object back in.
        ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        obj = in.readObject();
        return obj;
    }
}
