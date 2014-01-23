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

package org.apache.axis2.jaxws.rpclit.enumtype.tests;


import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.rpclit.enumtype.sei.PortType;
import org.apache.axis2.jaxws.rpclit.enumtype.sei.Service;
import org.test.rpclit.schema.ElementString;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;


public class RPCLitEnumTests extends AbstractTestCase {
    
	String axisEndpoint = "http://localhost:6060/axis2/services/RPCLitEnumService.PortTypeImplPort";

	public static Test suite() {
        return getTestSetup(new TestSuite(RPCLitEnumTests.class));
    }

	public void testEnumSimpleType(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
            Service service = new Service();
            PortType portType = service.getPort();

            BindingProvider p = (BindingProvider) portType;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

            Holder<ElementString> pString = new Holder<ElementString>(ElementString.A);
            portType.echoString(pString);
            ElementString es = pString.value;
            TestLogger.logger.debug("Response =" + es);
            
            // Try a second time
            pString = new Holder<ElementString>(ElementString.A);
            portType.echoString(pString);
            es = pString.value;
            TestLogger.logger.debug("Response =" + es);
            System.out.print("---------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }
}
