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

package org.tempuri;

import org.apache.axis2.integration.TestingUtils;
import org.apache.ws.java2wsdl.Java2WSDLBuilder;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.tempuri.elementQualifier.WSDLElementQualifier;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.StringReader;

public class BaseDataTypesTest extends XMLTestCase {

    private String wsdlLocation = TestingUtils.prefixBaseDirectory("test-resources/BaseDataTypes.wsdl");

    public void test1() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Java2WSDLBuilder builder = new Java2WSDLBuilder(out, BaseDataTypes.class.getName(), BaseDataTypes.class.getClassLoader());
            builder.generateWSDL();
            FileReader control = new FileReader(wsdlLocation);
            StringReader test = new StringReader(new String(out.toByteArray()));
            Diff myDiff = new Diff(XMLUnit.buildDocument(XMLUnit.getControlParser(), control), 
		               XMLUnit.buildDocument(XMLUnit.getControlParser(), test), 
		               (DifferenceEngine) null, new WSDLElementQualifier());
            if (!myDiff.similar()) 
	            fail(myDiff.toString()); 
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }
}
