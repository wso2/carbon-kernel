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

package org.apache.axis2.jaxws.rpclit.stringarray;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService(name = "EchoNoSEI", serviceName="RPCLitStringArrayEchoNoSEIService", targetNamespace = "http://sei.stringarray.rpclit.jaxws.axis2.apache.org")
@SOAPBinding(style = Style.RPC)
public class EchoImplNoSEI {

    @WebMethod
    public String echoString(String arg0) {
    	return arg0;
    }

   @WebMethod
    public String[] echoStringArray(String [] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i] + "return";
        }
        return args;
    }

}
