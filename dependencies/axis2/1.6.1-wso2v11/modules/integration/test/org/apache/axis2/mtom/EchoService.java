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

package org.apache.axis2.mtom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;

import java.util.Iterator;


public class EchoService {
    public OMElement mtomSample(OMElement element) throws Exception {
        if (element.getLocalName().equalsIgnoreCase("Data")
                && element.getNamespace().getNamespaceURI().equalsIgnoreCase(
                "http://example.org/mtom/data")) {
            OMText binaryNode = (OMText)element.getFirstOMChild();
            binaryNode.setOptimize(!binaryNode.isOptimized());
        } else if (element.getLocalName().equalsIgnoreCase("EchoTest") && element.getNamespace()
                .getNamespaceURI().equalsIgnoreCase("http://example.org/mtom/data")) {
            Iterator childrenIterator = element.getChildren();
            while (childrenIterator.hasNext()) {
                OMElement dataElement = (OMElement)childrenIterator.next();
                OMText binaryNode = (OMText)dataElement.getFirstOMChild();
                binaryNode.setOptimize(!binaryNode.isOptimized());
            }
        }
        return element;
    }
}
