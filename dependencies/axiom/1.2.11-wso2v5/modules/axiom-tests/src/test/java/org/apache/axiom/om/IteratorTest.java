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

package org.apache.axiom.om;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import java.util.Iterator;

public class IteratorTest extends AbstractTestCase {
    /** This will test the errrors mentioned in @link http://issues.apache.org/jira/browse/WSCOMMONS-12 */
    public void testScenariosInJIRA() throws Exception {
        OMElement mtomSampleElement = createSampleXMLForTesting();
        testScenarioOne(mtomSampleElement);
        testScenarioTwo(mtomSampleElement);
    }

    private OMElement createSampleXMLForTesting() throws Exception {
        String imageSource = "mtom/img/test.jpg";
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");

        OMElement data = fac.createOMElement("mtomSample", omNs);
        OMElement image = fac.createOMElement("image", omNs);

        DataSource dataSource = getTestResourceDataSource(imageSource);
        DataHandler expectedDH = new DataHandler(dataSource);
        OMText textData = fac.createOMText(expectedDH, true);
        image.addChild(textData);

        OMElement imageName = fac.createOMElement("fileName", omNs);
        imageName.setText(imageSource);
        data.addChild(image);
        data.addChild(imageName);

        return data;
    }

    private void testScenarioOne(OMElement mtomSampleElement) {
        Iterator imageElementIter = mtomSampleElement.getChildrenWithName(new QName("image"));
        // do something with the iterator
        while (imageElementIter.hasNext()) {
            imageElementIter.next();
            // do nothing
        }

        Iterator fileNameElementIter = mtomSampleElement.getChildrenWithName(new QName("fileName"));
        // do something with the iterator
        while (fileNameElementIter.hasNext()) {
            OMNode omNode = (OMNode) fileNameElementIter.next();
            if (omNode instanceof OMElement) {
                OMElement fileNameElement = (OMElement) omNode;
                assertTrue("fileName".equalsIgnoreCase(fileNameElement.getLocalName()));
            }
        }
    }

    private void testScenarioTwo(OMElement mtomSampleElement) {
        Iterator childElementsIter = mtomSampleElement.getChildElements();

        boolean imageElementFound = false;
        boolean fileNameElementFound = false;

        while (childElementsIter.hasNext()) {
            OMNode omNode = (OMNode) childElementsIter.next();
            if (omNode instanceof OMElement) {
                OMElement omElement = (OMElement) omNode;
                if (omElement.getLocalName().equals("fileName")) {
                    fileNameElementFound = true;
                } else if (omElement.getLocalName().equals("image")) {
                    imageElementFound = true;
                }
            }
        }

        assertTrue(fileNameElementFound && imageElementFound);
    }


}
