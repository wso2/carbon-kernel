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

package org.apache.axis2.description;

import junit.framework.TestCase;
import org.apache.ws.java2wsdl.Java2WSDLBuilder;
import org.xml.sax.InputSource;

import javax.wsdl.Definition;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.factory.WSDLFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class Java2WSDLTest extends TestCase {
    public void test1() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Java2WSDLBuilder builder = new Java2WSDLBuilder(out, CalculatorService.class.getName(), CalculatorService.class.getClassLoader(), new HashMap());
        builder.generateWSDL();
        InputSource inputSource = new InputSource(new ByteArrayInputStream(out.toByteArray()));
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        Definition definition = wsdlReader.readWSDL(null, inputSource);
        assertNotNull(definition);
    }
}
