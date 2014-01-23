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

package org.temp.doclitbare;

import org.apache.axis2.jaxbri.JaxbSchemaGenerator;
import org.apache.ws.java2wsdl.Java2WSDLBuilder;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class DocLitBareWSDLTest extends XMLTestCase {
    private String wsdlLocation = System.getProperty("basedir", ".") + "/" + "test-resources/wsdl/DocLitBareService.wsdl";

    public void testVersion() {
        XMLUnit.setIgnoreWhitespace(true);
        File testResourceFile = new File(wsdlLocation);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Java2WSDLBuilder builder = new Java2WSDLBuilder(baos, DocLitBareService.class.getName(),
                    Thread.currentThread().getContextClassLoader());
            builder.setSchemaGenClassName(JaxbSchemaGenerator.class.getName());
            builder.generateWSDL();
            //System.out.println(new String(baos.toByteArray()));
            //assertXMLEqual(new FileReader(testResourceFile), new StringReader(new String(baos.toByteArray())));
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
            fail("Caught exception " + e.toString());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }

}
