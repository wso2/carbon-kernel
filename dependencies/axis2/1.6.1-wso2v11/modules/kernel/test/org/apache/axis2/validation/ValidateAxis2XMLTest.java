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

package org.apache.axis2.validation;

import junit.framework.TestCase;
import org.apache.axis2.AbstractTestCase;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

public class ValidateAxis2XMLTest extends TestCase {


    private static String validationFeature
            = "http://xml.org/sax/features/validation";

    private static String schemaValidationFeature
            = "http://apache.org/xml/features/validation/schema";

    private static String extSchemaProp
            = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    private static boolean validate(File xmlSource, File xsdSource) {

        boolean valid = true;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // configures the xerces parser for schema validation.
            factory.setFeature(validationFeature, true);
            factory.setFeature(schemaValidationFeature, true);

            SAXParser parser = factory.newSAXParser();

            //validate against the given schemaURL
            parser.setProperty(extSchemaProp, xsdSource.toURL().toString());

            // parse (validates) the xml
            parser.parse(xmlSource, new DefaultHandler());

        } catch (SAXNotRecognizedException snre) {
            System.out.println("SAX not recognised " + snre);
            valid = false;
        } catch (SAXException se) {
            System.out.println("SAX parser " + se);
            valid = false;
        } catch (Exception e) {
            System.out.println("error parsing " + e);
            valid = false;
        }

        return valid;
    }

    public void testDefaultAxis2XML() throws Exception {
        String xmlFile =
                AbstractTestCase.basedir + "/conf/axis2.xml";
        String xsdFile =
                AbstractTestCase.basedir + "/resources/axis2.xsd";
        assertTrue(validate(new File(xmlFile), new File(xsdFile)));
    }

    public static void main(String args[]) {
        File f1 = new File(args[0]);
        File f2 = new File(args[1]);
        if (f1.isDirectory()) {
            recurseDirectory(f1, f2);
        } else {
            System.out.println(f1.getAbsolutePath() + " : " + (validate(f1, f2) ? "OK" : "INVALID"));
        }
    }

    private static void recurseDirectory(File f1, File f2) {
        File[] array = f1.listFiles();
        for (int i = 0; i < array.length; i++) {
            File file = array[i];
            if (file.isDirectory()) {
                recurseDirectory(file, f2);
            } else {
                if (file.getName().endsWith("axis2.xml")) {
                    System.out.println(file.getAbsolutePath() + " : " + (validate(file, f2) ? "OK" : "INVALID"));
                }
            }
        }
    }
}
