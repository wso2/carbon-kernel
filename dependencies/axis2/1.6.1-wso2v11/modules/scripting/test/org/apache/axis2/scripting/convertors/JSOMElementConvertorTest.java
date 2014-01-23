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

package org.apache.axis2.scripting.convertors;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.scripting.ScriptReceiver;
import org.apache.axis2.scripting.TestUtils;

import java.util.Iterator;

public class JSOMElementConvertorTest extends TestCase {

    public static final String XML = "<a><b>petra</b></a>";

    public void testToAndFromScript() {
        JSOMElementConvertor convertor = new JSOMElementConvertor();
        Object o = convertor.toScript(TestUtils.createOMElement(XML));
        OMElement om = convertor.fromScript(o);
        assertEquals(XML, om.toString());
    }

    public void testFromScript() throws Exception {
        ScriptReceiver mediator = new ScriptReceiver();
        MessageContext inMC = TestUtils.createMockMessageContext(XML);
        AxisService axisServce = new AxisService();
        axisServce.setParent(new AxisServiceGroup(new AxisConfiguration()));
        axisServce.addParameter(new Parameter(ScriptReceiver.SCRIPT_ATTR, "foo.js"));
        axisServce.addParameter(new Parameter(ScriptReceiver.SCRIPT_SRC_PROP, "function invoke(inMC,outMC) { outMC.setPayloadXML(<a><b>petra</b></a>) }"));
        inMC.setAxisService(axisServce);
        mediator.invokeBusinessLogic(inMC, inMC);
        Iterator iterator = inMC.getEnvelope().getChildElements();
        iterator.next();
        assertEquals(XML, ((OMElement) iterator.next()).getFirstElement().toString());
    }

}
