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
package org.apache.axiom.om.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactory;

import junit.framework.TestSuite;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.util.XMLStreamWriterRemoveIllegalChars;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

public class XMLStreamWriterFilterTestCase extends AbstractTestCase {
	
	private char ILLEGAL_CHAR = 0x15;
	private String ILLEGAL_ENTITY = "&#x15;";
	
	private char NULL_CHAR = 0x00;
	private String NULL_ENTITY = "&#x00;";
	
	private final OMMetaFactory omMetaFactory;
    
    protected XMLStreamWriterFilterTestCase(OMMetaFactory omMetaFactory) {
        this.omMetaFactory = omMetaFactory;
    }
	
	public void test01() throws Exception {
		char[] chars = new char[] {ILLEGAL_CHAR};
		String insert = new String(chars);
		testInsert(insert);
	}
	
	public void test02() throws Exception {
		char[] chars = new char[] {NULL_CHAR};
		String insert = new String(chars);
		testInsert(insert);
	}
	
	public void test03() throws Exception {
		
		testInsert(ILLEGAL_ENTITY);
	}
	
	public void test04() throws Exception {
		
		testInsert(NULL_ENTITY);
	}
	
	
	
	private void testInsert(String insert) throws Exception {
		
		// Read XML
		InputStream is = getTestResource(TestConstants.TEST);
		
		// Build SOAP OM
		SOAPEnvelope env1 = createEnvelope(is);
		
		// Add illegal character
		SOAPBody body = env1.getBody();
		OMElement omElement = body.getFirstElement();
		String text = omElement.getText();
		text = text + "[" + insert + "]";
		System.out.println("New Text = " + text);
		omElement.setText(text);
		
		// Serialize
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputFormat omFormat = new OMOutputFormat();
		omFormat.setXmlStreamWriterFilter(new XMLStreamWriterRemoveIllegalChars());
		env1.serialize(baos, omFormat);
		
		String xmlText = baos.toString();
		System.out.println("Serialized Text = " + xmlText);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(xmlText.getBytes("UTF-8"));
		
		SOAPEnvelope env2 = createEnvelope(bais);
		env2.build();
	}
	
    /**
     * Create SOAPEnvelope from the test in the indicated file
     * @param input stream
     * @return
     * @throws Exception
     */
    protected SOAPEnvelope createEnvelope(InputStream is) throws Exception {
        XMLStreamReader parser =
            XMLInputFactory.newInstance().createXMLStreamReader(is);
        OMXMLParserWrapper builder = new StAXSOAPModelBuilder(omMetaFactory, parser, null);
        SOAPEnvelope sourceEnv = (SOAPEnvelope) builder.getDocumentElement();
        return sourceEnv;
    }
    
}
