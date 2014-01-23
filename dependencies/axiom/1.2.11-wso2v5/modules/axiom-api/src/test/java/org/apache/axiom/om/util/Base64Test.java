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

package org.apache.axiom.om.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.apache.axiom.util.base64.Base64Utils;

public class Base64Test extends TestCase {

    Object expectedObject;

    ByteArrayInputStream byteStream;

    /*
     * Class under test for String encode(byte[])
     */

    public void testEncodebyteArray() throws Exception {
        Object actualObject;
        String expectedBase64;
        expectedObject = new String("Lanka Software Foundation");
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutStream = new ObjectOutputStream(byteStream);
        objectOutStream.writeObject(expectedObject);
        expectedBase64 = Base64Utils.encode(byteStream.toByteArray());
        byte[] tempa = Base64Utils.decode(expectedBase64);
        ObjectInputStream objectInStream = new ObjectInputStream(
                new ByteArrayInputStream(tempa));
        actualObject = objectInStream.readObject();
        assertEquals("Base64 Encoding Check", expectedObject, actualObject);
    }
}