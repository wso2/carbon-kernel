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

package org.apache.axis2.schema.populate.simple;

public class SimpleTypeBase64BinPopulateTest extends AbstractSimplePopulater{
    private String xmlString[] = {
            "<base64BinParam xmlns=\"http://soapinterop.org/xsd\">abcdABCD</base64BinParam>",
            "<base64BinParam xmlns=\"http://soapinterop.org/xsd\">abcdABCD09rT</base64BinParam>",
    };

     protected void setUp() throws Exception {
        className = "org.soapinterop.xsd.Base64BinParam";
        propertyClass = byte[].class;
    }
    // force others to implement this method
    public void testPopulate() throws Exception {
        process(xmlString[0],"org.soapinterop.xsd.Base64BinParam");
        process(xmlString[1],"org.soapinterop.xsd.Base64BinParam");
    }
}
