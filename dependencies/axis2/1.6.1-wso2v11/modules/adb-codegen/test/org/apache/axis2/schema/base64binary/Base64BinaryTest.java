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

package org.apache.axis2.schema.base64binary;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.schema.AbstractTestCase;
import org.w3.www._2005._05.xmlmime.*;

import javax.activation.DataHandler;

public class Base64BinaryTest extends AbstractTestCase {

    private void testBase64Binary(DataHandler dataHandler) throws Exception {
        TestBase64Binary testBase64Binary = new TestBase64Binary();
        Base64Binary base64Binary = new Base64Binary();
        testBase64Binary.setTestBase64Binary(base64Binary);

        base64Binary.setBase64Binary(dataHandler);
        ContentType_type0 contentType_type0 = new ContentType_type0();
        contentType_type0.setContentType_type0("test content type");
        base64Binary.setContentType(contentType_type0);

        testSerializeDeserialize(testBase64Binary, false);
    }

    public void testBase64Binary() throws Exception {
        testBase64Binary(new DataHandler(new ByteArrayDataSource("new test string".getBytes())));
    }

    public void testBase64BinaryEmpty() throws Exception {
        testBase64Binary(new DataHandler(new ByteArrayDataSource(new byte[0])));
    }

    public void testHexBinary() throws Exception {
        TestHexBinary testHexBinary = new TestHexBinary();
        org.w3.www._2005._05.xmlmime.HexBinary hexBinary = new org.w3.www._2005._05.xmlmime.HexBinary();
        testHexBinary.setTestHexBinary(hexBinary);

        String testString = "ab";

        org.apache.axis2.databinding.types.HexBinary adbHexBinary =
                new  org.apache.axis2.databinding.types.HexBinary(testString);

        hexBinary.setHexBinary(adbHexBinary);
        ContentType_type0 contentType_type0 = new ContentType_type0();
        contentType_type0.setContentType_type0("test content type");
        hexBinary.setContentType(contentType_type0);

        testSerializeDeserialize(testHexBinary, false);
    }

    public void testBase64MultiElement() throws Exception {

        TestBase64MultiElement testBase64MultiElement = new TestBase64MultiElement();
        String testString = "testing base 64 elements";
        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(testString.getBytes()));
        testBase64MultiElement.setParam1(dataHandler);
        testBase64MultiElement.setParam2("test string");
        testBase64MultiElement.setParam3(5);

        testSerializeDeserialize(testBase64MultiElement, false);
    }
    
    public void testBase64BinaryOnbounded() throws Exception {
        TestBase64BinaryOnbounded bean = new TestBase64BinaryOnbounded();
        bean.setParam(new DataHandler[] {
                new DataHandler("DataHandler 1", "text/plain"),
                new DataHandler("DataHandler 2", "text/plain"),
                new DataHandler("DataHandler 3", "text/plain")
        });
        testSerializeDeserialize(bean, false);
    }
}
