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

package org.apache.axis2.soap12testing.handlers;

public interface HeaderConstants {

    String REQUEST_HEADERBLOCK_NAME = "echoOk";
    String RESPONSE_HEADERBLOCK_NAME = "responseOk";
    String SAMPLE_ROLE = "http://example.org/ts-tests";
    String SOAP12_ROLE = "http://www.w3.org/2003/05/soap-envelope/role";
    String ULTIMATERECEIVER_ROLE = "ultimateReceiver";
    String NEXT_ROLE = "next";
    String NONE_ROLE = "none";
    String ROLE_BY_B = "B";
    String ROLE_BY_C = "C";
}
