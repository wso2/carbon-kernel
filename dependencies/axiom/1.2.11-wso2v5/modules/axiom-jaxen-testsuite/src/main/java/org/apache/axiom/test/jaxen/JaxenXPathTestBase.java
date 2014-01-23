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

package org.apache.axiom.test.jaxen;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.test.XPathTestBase;

public abstract class JaxenXPathTestBase extends XPathTestBase {
    static String TESTS_ROOT;
    
    static {
        URL testsXmlUrl = XPathTestBase.class.getClassLoader().getResource("xml/test/tests.xml");
        try {
            TESTS_ROOT = new URL(testsXmlUrl, "../..").toExternalForm();
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    final List documents = new ArrayList();
    
    public JaxenXPathTestBase(String name) {
        super(name);
    }

    protected abstract Object loadDocument(InputStream in) throws Exception;

    protected abstract void releaseDocument(Object document);
    
    protected abstract Navigator createNavigator();
    
    protected void tearDown() throws Exception {
        super.tearDown();
        for (Iterator it = documents.iterator(); it.hasNext(); ) {
            releaseDocument(it.next());
        }
        documents.clear();
    }

    protected final Object getDocument(String url) throws Exception {
        // This method is actually never used in XPathTestBase; it only uses Navigator#getDocument
        return null;
    }

    protected final Navigator getNavigator() {
        return new NavigatorWrapper(createNavigator()) {
            // We need to tweak the getDocument method a bit to load the document from the right
            // place.
            public Object getDocument(String uri) throws FunctionCallException {
                try {
                    URL url = new URL(TESTS_ROOT + uri);
                    Object document = loadDocument(url.openStream());
                    documents.add(document);
                    return document;
                } catch (Exception ex) {
                    throw new FunctionCallException(ex);
                }
            }
        };
    }
}
