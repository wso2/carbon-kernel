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

package org.apache.axiom.om.util;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;

import java.util.List;

public class XPathEvaluator {

    public List evaluateXpath(String xpathExpression, Object element, String nsURI)
            throws Exception {
        AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
        if (nsURI != null) {
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
            nsContext.addNamespace(null, nsURI);
            xpath.setNamespaceContext(nsContext);
        }
        return xpath.selectNodes(element);
    }


}
