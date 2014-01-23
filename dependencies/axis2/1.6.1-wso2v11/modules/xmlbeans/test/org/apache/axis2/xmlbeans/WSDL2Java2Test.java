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

package org.apache.axis2.xmlbeans;

/**
 * Test for the WSDL that's missing a service Since we do not permit the clients to be generated
 * when service is missing, we should get an error
 */
public class WSDL2Java2Test extends WSDL2JavaFailureTestBase {

    protected void setUp() throws Exception {
        this.wsdlFileName = "no-service.wsdl";
        super.setUp();
    }


}
