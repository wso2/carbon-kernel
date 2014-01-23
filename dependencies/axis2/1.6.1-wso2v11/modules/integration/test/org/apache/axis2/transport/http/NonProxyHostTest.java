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

package org.apache.axis2.transport.http;

import junit.framework.TestCase;
import org.apache.axis2.transport.http.util.HTTPProxyConfigurationUtil;

public class NonProxyHostTest extends TestCase {
    public void testForAxis2_3453() {
        String nonProxyHosts = "sealbook.ncl.ac.uk|*.sealbook.ncl.ac.uk|eskdale.ncl.ac.uk|*.eskdale.ncl.ac.uk|giga25.ncl.ac.uk|*.giga25.ncl.ac.uk";
        assertTrue(HTTPProxyConfigurationUtil.isHostInNonProxyList("sealbook.ncl.ac.uk", nonProxyHosts));
        assertFalse(HTTPProxyConfigurationUtil.isHostInNonProxyList("xsealbook.ncl.ac.uk", nonProxyHosts));
        assertTrue(HTTPProxyConfigurationUtil.isHostInNonProxyList("local","local|*.local|169.254/16|*.169.254/16"));
        assertFalse(HTTPProxyConfigurationUtil.isHostInNonProxyList("localhost","local|*.local|169.254/16|*.169.254/16"));
    }
}
