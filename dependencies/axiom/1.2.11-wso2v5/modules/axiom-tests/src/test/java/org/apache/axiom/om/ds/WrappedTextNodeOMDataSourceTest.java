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

package org.apache.axiom.om.ds;

import java.io.StringReader;
import java.util.Random;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.WrappedTextNodeOMDataSourceFromReader;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;

public class WrappedTextNodeOMDataSourceTest extends TestCase {
    public void testFromReader() throws Exception {
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(40000);
        for (int i=0; i<40000; i++) {
            buffer.append((char)(32 + random.nextInt(96)));
        }
        String testData = buffer.toString();
        QName qname = new QName("data");
        OMDataSource ds = new WrappedTextNodeOMDataSourceFromReader(qname, new StringReader(testData));
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMSourcedElement element = new OMSourcedElementImpl(qname, factory, ds);
        assertEquals(testData, element.getText());
    }
}
