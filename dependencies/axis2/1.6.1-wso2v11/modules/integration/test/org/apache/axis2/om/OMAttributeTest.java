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

package org.apache.axis2.om;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.StreamWrapper;
import za.co.eskom.nrs.xmlvend.base.x20.schema.AdviceReqDocument;
import za.co.eskom.nrs.xmlvend.base.x20.schema.ConfirmationAdviceReq;
import za.co.eskom.nrs.xmlvend.base.x20.schema.MsgID;

import javax.xml.namespace.QName;

/**
 * To run this test,maven build should have been run and the relevant type classes need to be
 * generated
 */
public class OMAttributeTest extends TestCase {

    public void testAttribNamespace() {

        //create a documentType
        AdviceReqDocument doc = AdviceReqDocument.Factory.newInstance();
        ConfirmationAdviceReq req = ConfirmationAdviceReq.Factory.newInstance();
        MsgID msgID = req.addNewAdviceReqMsgID();
        msgID.setUniqueNumber(11);
        req.addNewClientID();
        doc.setAdviceReq(req);

        //get the pull parser and construct the OMElement
        StAXOMBuilder builder = new StAXOMBuilder(
                OMAbstractFactory.getOMFactory(),
                new StreamWrapper(doc.newXMLStreamReader())
        );
        OMElement elt = builder.getDocumentElement();

        //traverse the element and look at the namespace of the attribute
        OMAttribute att =
                elt.getAttribute(new QName("http://www.w3.org/2001/XMLSchema-instance", "type"));
        assertNotNull(att);

        String prefix = att.getNamespace().getPrefix();
        assertNotNull(prefix);
        assertEquals(prefix, "xsi");

    }

}
