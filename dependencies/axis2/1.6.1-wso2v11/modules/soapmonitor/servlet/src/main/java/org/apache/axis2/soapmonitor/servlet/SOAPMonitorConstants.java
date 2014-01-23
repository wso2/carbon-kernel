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

package org.apache.axis2.soapmonitor.servlet;

/**
 * SOAP Monitor Service constants
 */

public class SOAPMonitorConstants {

    /**
     * SOAP message types
     */
    public static final int SOAP_MONITOR_REQUEST = 0;
    public static final int SOAP_MONITOR_RESPONSE = 1;

    /**
     * Port Servlet initialization parameter
     */
    public static final String SOAP_MONITOR_PORT = "SOAPMonitorPort";

    /**
     * Host name Servlet initialization parameter
     */
    public static final String SOAP_MONITOR_HOST_NAME = "SOAPMonitorHostName";

    /**
     * Unique SOAP monitor id tag
     */
    public static final String SOAP_MONITOR_ID = "SOAPMonitorId";
}
