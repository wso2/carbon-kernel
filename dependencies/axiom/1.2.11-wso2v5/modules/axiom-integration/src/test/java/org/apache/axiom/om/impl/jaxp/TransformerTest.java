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

import static org.custommonkey.xmlunit.XMLAssert.assertXMLIdentical;
import static org.custommonkey.xmlunit.XMLUnit.compareXML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TransformerTest {
    private final TransformerFactory factory;
    
    @Parameters
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { org.apache.xalan.processor.TransformerFactoryImpl.class },
                { net.sf.saxon.TransformerFactoryImpl.class }
        });
    }
    
    public TransformerTest(Class<? extends TransformerFactory> factoryClass) throws Exception {
        this.factory = factoryClass.newInstance();
    }

    private InputStream getInput() {
        return TransformerTest.class.getResourceAsStream("test.xml");
    }
    
    @Test
    public void testIdentity() throws Exception {
        Transformer transformer = factory.newTransformer();
        
        OMElement element = new StAXOMBuilder(getInput()).getDocumentElement();
        OMSource omSource = new OMSource(element);
        OMResult omResult = new OMResult();
        transformer.transform(omSource, omResult);
        
        StreamSource streamSource = new StreamSource(getInput());
        StringWriter out = new StringWriter();
        StreamResult streamResult = new StreamResult(out);
        transformer.transform(streamSource, streamResult);
        
        assertXMLIdentical(compareXML(out.toString(), omResult.getRootElement().toString()), true);
        
        element.close(false);
    }
    
    /**
     * Test that all namespace mappings in scope of the source element are available on the result.
     * This checks for an issue that may arise under the following circumstances:
     * <ol>
     *   <li>The source element, i.e. the element passed as argument to
     *   {@link OMSource#OMSource(OMElement)} is not the root element of the document.</li>
     *   <li>One of the ancestors declares a namespace mapping.</li>
     *   <li>The namespace mapping is not used in the name of the source element or any of its
     *   descendant elements or attributes (but may be used in the value of an attribute).</li>   
     * </ol>
     * Example:
     * <pre>&lt;root xmlns:ns="urn:ns">&lt;element attr="ns:someThing"/>&lt;root></pre>
     * In that case, when constructing an {@link OMSource} from the child element, the namespace
     * mapping for the <tt>ns</tt> prefix should be visible to the consumer. Otherwise it would not
     * be able to interpret the attribute value correctly. This is relevant e.g. when validating
     * a part of a document against an XML schema (see SYNAPSE-501).
     * 
     * @throws Exception
     */
    @Test
    public void testNamespaceMappingsOnFragment() throws Exception {
        Transformer transformer = factory.newTransformer();
        
        OMElement element = new StAXOMBuilder(getInput()).getDocumentElement().getFirstElement();
        OMSource omSource = new OMSource(element);
        OMResult omResult = new OMResult();
        transformer.transform(omSource, omResult);
        
        OMNamespace ns = omResult.getRootElement().findNamespaceURI("p");
        assertNotNull(ns);
        assertEquals("urn:some:namespace", ns.getNamespaceURI());
        
        element.close(false);
    }
}
