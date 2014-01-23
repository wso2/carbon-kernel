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

package org.apache.axis2.jaxws.sample.parallelasync.common;

import javax.xml.namespace.QName;

/**
 * This class holds constant strings such as endpoint addresses and common
 * conversion methods
 */
public class Constants {

    // server hostName and WC port
    private static final String SERVER = "localhost:6060";
    
    //public static final String WSDL_NAMESPACE = "http://common.wsfvt.async.jaxws";
    public static final String WSDL_NAMESPACE = "http://org/test/parallelasync";

    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope";

    public static final String SOAP12_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";

    public static final QName SERVICE_QNAME = new QName(WSDL_NAMESPACE,
            "AsyncService");

    public static final QName PORT_QNAME = new QName(WSDL_NAMESPACE,
            "AsyncPort");

    // Endpoint addresses
    public static final String BASE_ENDPOINT = "http://" + SERVER
            + "/axis2/services/";

    public static final String DOCLITWR_ASYNC_ENDPOINT = BASE_ENDPOINT
            + "AsyncService"; //+ "AsyncDocLitWrappedService";

    public static final String DOCLIT_ASYNC_ENDPOINT = BASE_ENDPOINT
            + "AsyncService"; //+ "AsyncDocLitService";

    public static final String RPCLIT_ASYNC_ENDPOINT = BASE_ENDPOINT
            + "AsyncService"; //+ "AsyncRpcLitService";

    public static final String THE_STRING = "This Is Just A Test";

    // how long the server should seep for before returning a response
    public static final long SERVER_SLEEP_SEC = 120;

    // maximum amount of time to wait for async operation to complete
    public static final int CLIENT_MAX_SLEEP_SEC = 120;

    // maximum amount of time to wait for async operation to complete
    public static final int CLIENT_SHORT_SLEEP_SEC = 15;

    // maximum number of times the client should check to see if
    // the server received sleep request
    public static final int MAX_ISASLEEP_CHECK = 10;

    // number of sec to sleep in between isAsleep checks
    public static final int SLEEP_ISASLEEP_SEC = 1;

}
