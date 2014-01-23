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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.ds.ParserInputStreamDataSource.Data;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;


public class ParserInputStreamDataSourceTests extends TestCase {
	private StAXSOAPModelBuilder builder = null;
	private XMLStreamReader parser = null;
	private String mockenvelope= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
	"<soapenv:Header/>"+
	"<soapenv:Body>"+
	"<invokeOp>Hello Provider OM</invokeOp>"+
	"</soapenv:Body>"+
	"</soapenv:Envelope>";
	
	private String payloadText = "<my:payload xmlns:my=\"urn://sample\">"+
    // "<my:emptyData/>"+
    "<my:data>"+
    "Hello World"+
    "</my:data>"+
    "</my:payload>";
	
	// Scenarios are 
	// SER: Serialize and cache
	// SER_SER: Serialize and cache called twice
	// SAC: Serialize and consume
	// SER_SAC: Serialize and cache followed by serialize and consume
	// SAC_SAC: Serialize and consume twice...the second may issue an exception 
	//        because it is an intentional misuse
	
	
	
	
	
	
	public void testCreateParserInputStreamDataSource() throws Exception {
		ParserInputStreamDataSource peds = createPeds();
		assertNotNull(peds);
	}
	
	public void testParserInputStreamDataSourceSerialize() throws Exception {
		ParserInputStreamDataSource peds = createPeds();
		//lets test Serialze() call.
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		peds.serialize(output, null);
		String str = new String(output.toByteArray());
		assertNotNull(str);
		assertEquals(str, "<invokeOp>Hello Provider OM</invokeOp>");
	}
	
	public void testParserInputStreamDataSourceSerializeWithWriter() throws Exception {
		ParserInputStreamDataSource peds = createPeds();
		//lets test Serialze() call.
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(output);
		peds.serialize(writer);
		String str = new String(output.toByteArray());
		assertNotNull(str);
		assertEquals(str, "<invokeOp>Hello Provider OM</invokeOp>");
	}
	
	public void testParserInputStreamDataSourceGetXMLBytes() throws Exception {
		ParserInputStreamDataSource peds = createPeds();
		
		//lets test getXMLBytes().
		byte[] bytes = peds.getXMLBytes("UTF-8");
		String str = new String(bytes);
		assertNotNull(bytes);
		assertEquals(str, "<invokeOp>Hello Provider OM</invokeOp>");
	}
	
	private void updatePedsDataWithMockInputStream(ParserInputStreamDataSource peds) throws Exception{
		SOAPEnvelope env = getMockEnvelope();
		SOAPBody body = env.getBody();
		Iterator iter = body.getChildElements();
		InputStream mockInputStream = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		while(iter.hasNext()){
			OMElement om = (OMElement)iter.next();
			om.serialize(os);
			byte[] bArray = os.toByteArray();
			mockInputStream = new ByteArrayInputStream(bArray);
			break;
		}
		((Data)peds.getObject()).setInputStream(mockInputStream);
	}
	
	private ParserInputStreamDataSource createPeds() throws Exception{
        ParserInputStreamDataSource peds= new ParserInputStreamDataSource(null, "UTF-8" );
//              This should fake the inputStream, so we dont rely on parser fetch.
                updatePedsDataWithMockInputStream(peds);
                return peds;
    }
	
        
    private StAXSOAPModelBuilder getOMBuilder() throws Exception {
    	if(builder == null){
	        builder = new StAXSOAPModelBuilder(getParser(), null);
    	}
        return builder;
    }
    
	private SOAPEnvelope getMockEnvelope() throws Exception{
		SOAPEnvelope env = (SOAPEnvelope)getOMBuilder().getDocumentElement();
		return env;
	}
	
    private XMLStreamReader getParser()throws XMLStreamException{
    	if(parser == null){
    		parser =  XMLInputFactory.newInstance()
    		.createXMLStreamReader(
    				new ByteArrayInputStream(mockenvelope.getBytes()));
    	}
    	return parser;

    }
    
    // DataSource Accesses expected
    // Behavior                 SER     SER_SER     SAC    SER_SAC     SAC_SAC
    // DESTRUCTUVE              1       1           1      1           fail
    // NOT_DESTRUCTUVE MARK     1       2           1      2           2 or fail
    // NOT_DESTRUCTUVE COPY     1       2           1      2           2 or fail
    // ONE_USE_UNSAFE           1       fail        1      fail        fail
    
    public void testDestructiveMarkableSER() throws Exception {
        int numReads = _testPEDS(Behavior.DESTRUCTIVE, true, Scenario.SER);
        assertTrue (numReads == 1);
    }
    
    public void testDestructiveMarkableSER_SER() throws Exception {
        int numReads = _testPEDS(Behavior.DESTRUCTIVE, true, Scenario.SER_SER);
        // The om is cached with the first serialization; thus only one 
        // read is expected of the data source
        assertTrue (numReads == 1);
    }
    
    public void testDestructiveMarkableSAC() throws Exception {
        int numReads = _testPEDS(Behavior.DESTRUCTIVE, true, Scenario.SAC);
        assertTrue (numReads == 1);
    }
    
    public void testDestructiveMarkableSER_SAC() throws Exception {
        int numReads = _testPEDS(Behavior.DESTRUCTIVE, true, Scenario.SER_SAC);
        // The om is cached with the first serialization; thus only one 
        // read is expected of the data source
        assertTrue (numReads == 1);
    }
    
    public void testDestructiveMarkableSAC_SAC() throws Exception {
        try {
            int numReads = _testPEDS(Behavior.DESTRUCTIVE, true, Scenario.SAC_SAC);
            fail();
        } catch (OMException e) {
            // OMException is expected..you can't call serialize and consume twice ..ever
        }
    }
    
    public void testNotDestructiveMarkableSER() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, true, Scenario.SER);
        assertTrue (numReads == 1);
    }
    
    public void testNotDestructiveMarkableSER_SER() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, true, Scenario.SER_SER);
        
        // Two serializations, two reads
        assertTrue (numReads == 2);
    }
    
    public void testNotDestructiveMarkableSAC() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, true, Scenario.SAC);
        assertTrue (numReads == 1);
    }
    
    public void testNotDestructiveMarkableSER_SAC() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, true, Scenario.SER_SAC);
        
        // Two serializations, two reads
        assertTrue (numReads == 2);
    }
    
    public void testNotDestructiveMarkableSAC_SAC() throws Exception {
        try {
            int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, true, Scenario.SAC_SAC);
            // If no failure occurs than two reads are expected.
            assertTrue(numReads == 2);
        } catch (OMException e) {
            // OMException is allowed...this is an unsafe series of operations
        }
    }
    
    public void testNotDestructiveNotMarkableSER() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, false, Scenario.SER);
        assertTrue (numReads == 1);
    }
    
    public void testNotDestructiveNotMarkableSER_SER() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, false, Scenario.SER_SER);
        // Two serializations, two reads
        assertTrue (numReads == 2);
    }
    
    public void testNotDestructiveNotMarkableSAC() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, false, Scenario.SAC);
        assertTrue (numReads == 1);
    }
    
    public void testNotDestructiveNotMarkableSER_SAC() throws Exception {
        int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, false, Scenario.SER_SAC);
        // Two serializations, two reads
        assertTrue (numReads == 2);
    }
    
    public void testNotDestructiveNotMarkableSAC_SAC() throws Exception {
        try {
            int numReads = _testPEDS(Behavior.NOT_DESTRUCTIVE, false, Scenario.SAC_SAC);
            // If no failure occurs than two reads are expected.
            assertTrue(numReads == 2);
        } catch (OMException e) {
         // OMException is allowed...this is an unsafe series of operations
        }
    }
    
    public void testOneUseNotMarkableSER() throws Exception {
        int numReads = _testPEDS(Behavior.ONE_USE_UNSAFE, false, Scenario.SER);
        assertTrue (numReads == 1);
    }
    
    public void testOneUseNotMarkableSER_SER() throws Exception {
        try {
            int numReads = _testPEDS(Behavior.ONE_USE_UNSAFE, false, Scenario.SER_SER);
            fail();
        } catch (OMException e) {
            // OMException is expected..you can't serialize twice in this mode
        }
    }
    
    public void testOneUseNotMarkableSAC() throws Exception {
        int numReads = _testPEDS(Behavior.ONE_USE_UNSAFE, false, Scenario.SAC);
        assertTrue (numReads == 1);
    }
    
    public void testOneUseNotMarkableSER_SAC() throws Exception {
        try {
            int numReads = _testPEDS(Behavior.ONE_USE_UNSAFE, false, Scenario.SER_SAC);
            fail();
        } catch (OMException e) {
            // OMException is expected..you can't serialize twice in this mode
        }
    }
    
    public void testOneUseNotMarkableSAC_SAC() throws Exception {
        try {
            int numReads = _testPEDS(Behavior.ONE_USE_UNSAFE, false, Scenario.SAC_SAC);
            fail();
        } catch (OMException e) {
            // OMException is expected..you can't call serialize and consume twice ..ever
        }
    }
    
    /**
     * @param behavior Behavior (DESTRUCTIVE, NOT_DESTRUCTIVE, ONE_USE_UNSAFE)
     * @param markSupported (indicates if InputStream should be markable)
     * @param scnenario Scenario
     * @return numReads
     * @throws Exception
     */
    public int _testPEDS(int behavior, 
            boolean markSupported, 
            int scenario) throws Exception {
        
        // Create an InputStream
        InputStream is = null;
        if (markSupported) {
            is = new ByteArrayInputStream(payloadText.getBytes("UTF-8"));
        } else {
            is = new NotMarkableInputStream(payloadText.getBytes("UTF-8"));
        }
        
        // Create a PEDS with the indicated behavior
        ParserInputStreamDataSource peds = new ParserInputStreamDataSource(is, "UTF-8", behavior);
        
        // Create a OM tree with a root that contains an OMSourcedElement with a PADS
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("urn://sample", "my");
        OMElement om = factory.createOMElement(peds, "payload", ns);
        
        QName rootQName = new QName("urn://root", "root", "pre");
        OMElement root = factory.createOMElement(rootQName);
        root.addChild(om);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Apply the scenario
        if (scenario == Scenario.SER) {
            root.serialize(baos);
            String result = baos.toString("UTF-8");
            assertResult(result);
        } else if (scenario == Scenario.SER_SER) {
            root.serialize(baos);
            String result = baos.toString("UTF-8");
            assertResult(result);
            baos.reset();
            root.serialize(baos);
            result = baos.toString("UTF-8");
            assertResult(result);
        } else if (scenario == Scenario.SAC) {
            root.serializeAndConsume(baos);
            String result = baos.toString("UTF-8");
            assertResult(result);
        } else if (scenario == Scenario.SER_SAC) {
            root.serialize(baos);
            String result = baos.toString("UTF-8");
            assertResult(result);
            baos.reset();
            root.serializeAndConsume(baos);
            result = baos.toString("UTF-8");
            assertResult(result);
        } else if (scenario == Scenario.SAC_SAC) {
            root.serializeAndConsume(baos);
            String result = baos.toString("UTF-8");
            assertResult(result);
            baos.reset();
            root.serializeAndConsume(baos);
            // This second serializeAndConsume is expected to throw an exception.
        }
        
        return peds.numReads();
    }
    
    private void assertResult(String result) throws Exception {
        assertTrue("Result is incorrect:" + result + " payload=" + payloadText, result.contains(payloadText));
    }
    
    /**
     * This InputStream tests functionality when mark is not supported.
     */
    class NotMarkableInputStream extends ByteArrayInputStream {

        public NotMarkableInputStream (byte[] buf) {
            super(buf);
            
        }

        public boolean markSupported() {
            return false;
        }

        
        
        
    }
}
