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
package org.apache.axiom.om.impl.jaxp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestSuite;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.util.stax.dialect.StAXDialect;
import org.apache.axiom.util.stax.dialect.StAXDialectDetector;

public class OMSourceToStreamResultTestCase extends AbstractTestCase {
    private final OMMetaFactory omMetaFactory;
    private final TransformerFactory transformerFactory;
    private final String file;
    
    private OMSourceToStreamResultTestCase(OMMetaFactory omMetaFactory,
            TransformerFactory transformerFactory, String name, String file) {
        super(name);
        this.omMetaFactory = omMetaFactory;
        this.transformerFactory = transformerFactory;
        this.file = file;
    }

    protected void runTest() throws Throwable {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        StAXDialect dialect = StAXDialectDetector.getDialect(inputFactory.getClass());
        inputFactory = dialect.normalize(inputFactory);
        // Make sure CDATA sections are reported by the StAX parser
        inputFactory = dialect.enableCDataReporting(inputFactory);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(getTestResource(file));
        StAXOMBuilder builder = new StAXOMBuilder(omMetaFactory.getOMFactory(), reader);
        OMSource source = new OMSource(builder.getDocumentElement());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(out);
        transformerFactory.newTransformer().transform(source, result);
        assertXMLIdentical(compareXML(
                toDocumentWithoutDTD(getTestResource(file)),
                toDocumentWithoutDTD(new ByteArrayInputStream(out.toByteArray()))), true);
    }
    
    public static TestSuite suite(OMMetaFactory omMetaFactory,
            TransformerFactory transformerFactory) throws Exception {
        TestSuite suite = new TestSuite();
        String[] files = getConformanceTestFiles();
        for (int i=0; i<files.length; i++) {
            String file = files[i];
            int idx = file.lastIndexOf('/');
            String name = file.substring(idx+1);
            suite.addTest(new OMSourceToStreamResultTestCase(omMetaFactory, transformerFactory,
                    name, file));
        }
        return suite;
    }
}
