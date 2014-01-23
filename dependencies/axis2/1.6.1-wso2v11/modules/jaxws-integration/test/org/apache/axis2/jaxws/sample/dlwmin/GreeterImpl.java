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

package org.apache.axis2.jaxws.sample.dlwmin;

import org.apache.axis2.jaxws.sample.dlwmin.sei.Greeter;
import org.apache.axis2.jaxws.sample.dlwmin.sei.TestException;
import org.apache.axis2.jaxws.sample.dlwmin.sei.TestException2;
import org.apache.axis2.jaxws.sample.dlwmin.sei.TestException3;
import org.apache.axis2.jaxws.sample.dlwmin.types.ProcessFault3;
import org.apache.axis2.jaxws.sample.dlwmin.types.TestBean;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

@WebService(serviceName="GreeterService",
			endpointInterface = "org.apache.axis2.jaxws.sample.dlwmin.sei.Greeter",
			targetNamespace = "http://apache.org/axis2/jaxws/sample/dlwmin")
public class GreeterImpl implements Greeter {

    public String greetMe(String me) {
        if (me == null) {
            return null;
        } else {
            return "Hello " + me;
        }
    }

    public String testUnqualified(String in) {
        return in;
    }

    public TestBean process(int inAction, TestBean in) throws TestException, TestException2, TestException3 {
        if (inAction == 0) {
            // echo
            return in;
        } else if (inAction == 1) {
            // throw checked exception that does not have a fault bean
            throw new TestException("TestException thrown", 123);
        } else if (inAction == 2) {
            throw new WebServiceException("WebServiceException thrown");
        } else if (inAction == 3) {
            throw new NullPointerException("NPE thrown");
        } else if (inAction == 4) {
           // throw checked exception that does have a fault bean
            throw new TestException2("TestException2 thrown", 456);
        } else if (inAction == 5) {
           // throw checked exception that does have a fault bean
            ProcessFault3 faultInfo = new ProcessFault3();
            faultInfo.setFlag(789);
            throw new TestException3("TestException3 thrown", faultInfo);
        }
        return null;
    }

    public String simpleTest(String name, byte[] bytes) {
        return "name=" + name + " numbytes=" +bytes.length;
    }
}
