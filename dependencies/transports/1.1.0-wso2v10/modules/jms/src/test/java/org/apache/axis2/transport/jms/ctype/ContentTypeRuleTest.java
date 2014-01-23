/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.axis2.transport.jms.ctype;

import java.io.InputStream;

import javax.jms.Message;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.mockejb.jms.BytesMessageImpl;
import org.mockejb.jms.ObjectMessageImpl;
import org.mockejb.jms.TextMessageImpl;

public class ContentTypeRuleTest extends TestCase {
    private ContentTypeRuleSet ruleSet;
    
    @Override
    public void setUp() throws Exception {
        AxisConfiguration axisCfg = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisCfg);
        AxisService service = new AxisService();
        
        InputStream in = ContentTypeRuleTest.class.getResourceAsStream(getName() + ".xml");
        try {
            OMElement element = new StAXOMBuilder(in).getDocumentElement();
            new ServiceBuilder(cfgCtx, service).populateService(element);
        } finally {
            in.close();
        }
        
        ruleSet = ContentTypeRuleFactory.parse(service.getParameter("test"));
    }
    
    private void assertContentTypeInfo(String propertyName, String contentType, Message message)
            throws Exception {
        
        ContentTypeInfo contentTypeInfo = ruleSet.getContentTypeInfo(message);
        assertEquals(propertyName, contentTypeInfo.getPropertyName());
        assertEquals(contentType, contentTypeInfo.getContentType());
    }
    
    public void test1() throws Exception {
        Message message = new BytesMessageImpl();
        message.setStringProperty("contentType", "application/xml");
        assertContentTypeInfo("contentType", "application/xml", message);
        
        assertContentTypeInfo(null, "text/plain", new TextMessageImpl());
        assertContentTypeInfo(null, "application/octet-stream", new BytesMessageImpl());
        assertEquals(null, ruleSet.getContentTypeInfo(new ObjectMessageImpl()));
    }
    
    public void test2() throws Exception {
        Message message = new BytesMessageImpl();
        message.setStringProperty("contentType", "application/xml");
        assertContentTypeInfo("contentType", "application/xml", message);
        
        message = new TextMessageImpl();
        message.setStringProperty("ctype", "application/xml");
        assertContentTypeInfo("ctype", "application/xml", message);

        assertContentTypeInfo(null, "text/xml", new TextMessageImpl());
        assertContentTypeInfo(null, "text/xml", new BytesMessageImpl());
    }
}
