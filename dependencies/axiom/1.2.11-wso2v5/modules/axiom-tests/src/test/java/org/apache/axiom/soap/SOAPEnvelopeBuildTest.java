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
package org.apache.axiom.soap;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.util.CommonUtils;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;

/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
public class SOAPEnvelopeBuildTest extends TestCase {


	String testMessage="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
    "<soapenv:Header><x:Header xmlns:x=\"http://test/\">Hello</x:Header></soapenv:Header>"+
    "<soapenv:Body><x:Content xmlns:x=\"http://test/\">Hello</x:Content></soapenv:Body>"+
    	"</soapenv:Envelope>";

	public void testBodyPreservedSerialize() throws Exception{
		XMLStreamReader parser = StAXUtils.createXMLStreamReader(new StringReader(testMessage));
		StAXSOAPModelBuilder sob = new StAXSOAPModelBuilder(parser, null);
		SOAPEnvelope se = (SOAPEnvelope)sob.getDocumentElement();
		SOAPHeader sh = se.getHeader();
		Iterator iter = sh.getChildElements();
		while(iter.hasNext())iter.next();

		StringWriter sw = new StringWriter();
		se.serialize(sw);

		checkBodyExists(sw.toString());
		
		se.close(false);
	}

	public void testBodyPreservedSerializeAndConsume() throws Exception{
		XMLStreamReader parser = StAXUtils.createXMLStreamReader(new StringReader(testMessage));
		StAXSOAPModelBuilder sob = new StAXSOAPModelBuilder(parser, null);
		SOAPEnvelope se = (SOAPEnvelope)sob.getDocumentElement();
		SOAPHeader sh = se.getHeader();
        sh.build();
		StringWriter sw = new StringWriter();
		se.serializeAndConsume(sw);

		checkBodyExists(sw.toString());
        
        se.close(false);
	}

    public void testBodyPreservedSerializeAndConsumeAsXML() throws Exception{
		XMLStreamReader parser = StAXUtils.createXMLStreamReader(new StringReader(testMessage));
		StAXOMBuilder sob = new StAXOMBuilder(parser);
		OMElement se = sob.getDocumentElement();
		OMElement sh = se.getFirstElement();
		Iterator iter = sh.getChildElements();
		while(iter.hasNext())iter.next();
		StringWriter sw = new StringWriter();
		se.serializeAndConsume(sw);

		checkBodyExists(sw.toString());
        
        se.close(false);
	}

    public void testBodyPreservedSerializeAndConsumeDoesntTouchHeaders() throws Exception{
		XMLStreamReader parser = StAXUtils.createXMLStreamReader(new StringReader(testMessage));
		StAXSOAPModelBuilder sob = new StAXSOAPModelBuilder(parser, null);
		SOAPEnvelope se = (SOAPEnvelope)sob.getDocumentElement();

		StringWriter sw = new StringWriter();
		se.serializeAndConsume(sw);

		checkBodyExists(sw.toString());
        
        se.close(false);
	}

	public void testBodyPreservedSerializeAndConsumeTouchesBody() throws Exception{
		XMLStreamReader parser = StAXUtils.createXMLStreamReader(new StringReader(testMessage));
		StAXSOAPModelBuilder sob = new StAXSOAPModelBuilder(parser, null);
		SOAPEnvelope se = (SOAPEnvelope)sob.getDocumentElement();
		SOAPHeader sh = se.getHeader();
		Iterator iter = sh.getChildElements();
		while(iter.hasNext())iter.next();
		se.getBody();
		StringWriter sw = new StringWriter();
		se.serializeAndConsume(sw);

		checkBodyExists(sw.toString());
        
        se.close(false);
	}

	private void checkBodyExists(String str) throws Exception{
		XMLStreamReader parser = StAXUtils.createXMLStreamReader(new StringReader(str));
		StAXSOAPModelBuilder sob = new StAXSOAPModelBuilder(parser, null);
		SOAPEnvelope se = (SOAPEnvelope)sob.getDocumentElement();
		SOAPBody sb = se.getBody();
		if(sb == null){
			fail("No SOAP Body");
		}
		Iterator children = sb.getChildElements();
		if(!children.hasNext()){
			fail("No children of the Body element");
		}
        
        se.close(false);
	}
        
        public void testTrace() throws Exception{
                XMLStreamReader parser = StAXUtils.createXMLStreamReader(new StringReader(testMessage));
                StAXSOAPModelBuilder sob = new StAXSOAPModelBuilder(parser, null);
                SOAPEnvelope se = (SOAPEnvelope)sob.getDocumentElement();
                SOAPHeader sh = se.getHeader();
                
                MyDebugLogger log = new MyDebugLogger();
                long length = CommonUtils.logDebug(se, log);
                assertTrue(length > 100);
                assertTrue(log.outputText.indexOf("x:Content") > 0);
                
                se.close(false);
        }
        
        class MyDebugLogger implements Log {

            public String outputText = "";
            public void debug(Object arg0) {
                outputText = outputText.concat(arg0.toString());   
            }
            public void debug(Object arg0, Throwable arg1) {}           
            public void error(Object arg0) {}
            public void error(Object arg0, Throwable arg1) {}
            public void fatal(Object arg0) {}
            public void fatal(Object arg0, Throwable arg1) {}
            public void info(Object arg0) {}
            public void info(Object arg0, Throwable arg1) {}

            public boolean isDebugEnabled() {
                return true;
            }

            public boolean isErrorEnabled() {
                return false;
            }

            public boolean isFatalEnabled() {
                return false;
            }

            public boolean isInfoEnabled() {
                return false;
            }

            public boolean isTraceEnabled() {
                return false;
            }

            public boolean isWarnEnabled() {
                return false;
            }

            public void trace(Object arg0) {}
            public void trace(Object arg0, Throwable arg1) {}
            public void warn(Object arg0) {}
            public void warn(Object arg0, Throwable arg1) {}
        }
}
