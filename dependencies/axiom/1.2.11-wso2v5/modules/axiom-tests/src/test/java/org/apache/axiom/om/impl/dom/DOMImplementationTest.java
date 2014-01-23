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

package org.apache.axiom.om.impl.dom;

import junit.framework.TestCase;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderImpl;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DOMImplementationTest extends TestCase {
    public void testDOMImpl() throws Exception {
        try {
//			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",DocumentBuilderFactoryImpl.class.getName());

            DocumentBuilderFactoryImpl.setDOOMRequired(true);

            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fac.newDocumentBuilder();
            Document doc = builder.newDocument();

            assertEquals("Incorrect DocumentBuilderFactory instance",
                         DocumentBuilderFactoryImpl.class.getName(), fac.getClass().getName());
            assertEquals("Incorrect DocumentBuilder instance", DocumentBuilderImpl.class.getName(),
                         builder.getClass().getName());
            assertEquals("Incorrect Document instance", DocumentImpl.class.getName(),
                         doc.getClass().getName());

        } finally {
            DocumentBuilderFactoryImpl.setDOOMRequired(false);
        }
    }
}
