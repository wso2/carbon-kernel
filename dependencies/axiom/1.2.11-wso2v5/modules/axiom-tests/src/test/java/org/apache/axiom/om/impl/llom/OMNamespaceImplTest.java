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

package org.apache.axiom.om.impl.llom;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.axiom.om.impl.OMNamespaceImpl;

import java.lang.reflect.Field;

/** This class will test the methods and fields of the OMNamespaceImpl class. */
public class OMNamespaceImplTest extends TestCase {

    /**
     * The main method.
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * A one arg constructor to allow specifying a single test method to run.
     *
     * @param name The name of the test to run
     */
    public OMNamespaceImplTest(String name) {
        super(name);
    }

    /**
     * This method returns a test suite of tests to run.
     *
     * @return A test suite to be run
     */
    public static Test suite() {
        return new TestSuite(OMNamespaceImplTest.class);
    }

    public void testGetNamespaceURI() {
        OMNamespaceImpl omNsImpl =
                new OMNamespaceImpl("http://www.w3.org/XML/1998/namespace",
                                    "xml");
        assertEquals("http://www.w3.org/XML/1998/namespace",
                     omNsImpl.getNamespaceURI());
    }

    public void testGetPrefix() {
        OMNamespaceImpl omNsImpl =
                new OMNamespaceImpl("http://www.w3.org/XML/1998/namespace",
                                    "xml");
        assertEquals("xml", omNsImpl.getPrefix());
        omNsImpl = new OMNamespaceImpl("", null);
        assertNull(omNsImpl.getPrefix());
    }

    /** This method wil test the equals method. */
    public void testEquals() {
        OMNamespaceImpl omNsImpl =
                new OMNamespaceImpl("http://www.w3.org/XML/1998/namespace",
                                    null);
        assertTrue(omNsImpl.equals("http://www.w3.org/XML/1998/namespace",
                                   null));

        omNsImpl =
                new OMNamespaceImpl("http://www.w3.org/XML/1998/namespace",
                                    "xml");
        assertTrue(omNsImpl.equals("http://www.w3.org/XML/1998/namespace",
                                   "xml"));
        assertTrue(!omNsImpl.equals("http://www.w3.org/XML/1998/namespace",
                                    "xml2"));
        assertTrue(!omNsImpl.equals("http://www.w3.org/XML/1998/namespace",
                                    null));
        assertTrue(!omNsImpl.equals("http://www.w3.org/XML/2001/namespace",
                                    "xml"));
        assertTrue(!omNsImpl.equals(null,
                                    "xml"));
    }

    /**
     * This method will insure that the OMNamespaceImpl class remains immutable.
     *
     * @throws Exception Any kind of exception
     */
    public void testImmutable() throws Exception {

        Field[] clsField = OMNamespaceImpl.class.getDeclaredFields();
        for (int i = 0; i < clsField.length; i++) {
            String fieldData = clsField[i].toString();
            if ((fieldData.indexOf("prefix") != -1)
                    || (fieldData.indexOf("uri") != -1)) {
                assertTrue("The field should have been private, "
                        + " but instead contained: " + fieldData + ".",
                           fieldData.indexOf("private") != -1);
            }
        }

        String[] method = new String[] { "setPrefix", "setUri", "setName" };
        for (int i = 0; i < method.length; i++) {
            try {
                OMNamespaceImpl.class.getMethod(method[i],
                                                new Class[] { String.class });
                fail("A NoSuchMethodException should have been thrown"
                        + " when trying to get method \"" + method[i]
                        + "\".");
            } catch (NoSuchMethodException e) {
                // These methods should not exist, so this is
                // expected.
            }
        }

    }

}
