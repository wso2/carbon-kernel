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

package org.apache.axiom.om;

import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class OMBodyTest extends OMTestCase implements OMConstants {
    SOAPBody soapBody;
    private static Log log = LogFactory.getLog(OMBodyTest.class);

    public OMBodyTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        soapBody = soapEnvelope.getBody();
    }

    /*
     * Class under test for SOAPFault addFault()
     */
    public void testAddFault() {
        log.debug("Adding SOAP fault to body ....");
        soapBody.addChild(
                soapFactory.createSOAPFault(soapBody,
                                            new Exception("Testing soap fault")));
        log.debug("\t checking for SOAP Fault ...");
        assertTrue("SOAP body has no SOAP fault", soapBody.hasFault());
        log.debug("\t checking for not-nullity ...");
        assertTrue("SOAP body has no SOAP fault", soapBody.getFault() != null);
    }
    
    /**
     * Ensure that invoking addChild twice on the same element only
     * adds the child one time.
     */
    public void testAddChildTwice() {
        log.debug("Add Child Twice");
        OMElement om1 = soapFactory.createOMElement("child1", "http://myChild", "pre");
        OMElement om2 = soapFactory.createOMElement("child2", "http://myChild", "pre");
        soapBody.addChild(om1);
        soapBody.addChild(om1);  // NOOP..Expected behavior: child removed and then added
        soapBody.addChild(om2);
        
        OMElement node = (OMElement) soapBody.
          getFirstChildWithName(new QName("http://myChild", "child1"));
        node = (OMElement) node.detach();
        
        assertTrue("Node is missing", node != null);
        assertTrue("Node has the wrong name " + node.getLocalName(), 
                   node.getLocalName().equals("child1"));
        
        node = (OMElement) soapBody.
          getFirstChildWithName(new QName("http://myChild", "child2"));
        assertTrue("Node is missing", node != null);
        assertTrue("Node has the wrong name " + node.getLocalName(), 
                   node.getLocalName().equals("child2"));
    }

}
