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

package org.apache.axis2.integration;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import java.io.File;
import java.io.IOException;

public class TestingUtils {
    public static OMElement createDummyOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://org.apache.axis2/xsd", "ns1");
        OMElement method = fac.createOMElement("echoOM", omNs);
        OMElement value = fac.createOMElement("omEle", omNs);
        value.addChild(
                fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);
        return method;
    }

    public static OMElement createDummyOMElement(String nameSpace) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(nameSpace, "ns1");
        OMElement method = fac.createOMElement("echoOM", omNs);
        OMElement value = fac.createOMElement("omEle", omNs);
        value.addChild(
                fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);
        return method;
    }


    public static void compareWithCreatedOMElement(OMElement element) {
        OMElement firstChild = element.getFirstElement();
        TestCase.assertNotNull(firstChild);
        String textValue = firstChild.getText();
        TestCase.assertEquals(textValue, "Isaac Asimov, The Foundation Trilogy");
    }

    public static String prefixBaseDirectory(String path) {
        String baseDir;
        try {
            baseDir = new File(System.getProperty("basedir", ".")).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baseDir + "/" + path;
    }
}
