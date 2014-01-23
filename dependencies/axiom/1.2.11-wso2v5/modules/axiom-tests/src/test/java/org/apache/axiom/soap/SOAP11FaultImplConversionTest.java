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

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;


public class SOAP11FaultImplConversionTest extends AbstractTestCase {

    private String soap11FaulXmlPath = "soap/soap11/soapfault2.xml";

    public void testConversion() throws Exception {
        InputStream is = getTestResource(soap11FaulXmlPath);
        XMLStreamReader reader = StAXUtils.createXMLStreamReader(is);

        SOAPEnvelope env = new StAXSOAPModelBuilder(reader, null).getSOAPEnvelope();

        env.build();

        SOAPEnvelope env2 =
                new StAXSOAPModelBuilder(env.getXMLStreamReader(), null).getSOAPEnvelope();

        env2.build();

        env2.toString();

        //System.out.println(env2);
    }
}
