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

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 *
 */
public class TransportHeadersTest extends TestCase {

    public void testServletRequest() {
        // This just validates that the HttpServletRequest test class below works as expected
        HttpServletRequest req = new TestServletRequest();
        assertEquals("h1Value", req.getHeader("header1"));
        assertEquals("h2Value", req.getHeader("header2"));
        assertEquals("h3Value", req.getHeader("header3"));
        assertNull(req.getHeader("newHeader1"));
        assertNull(req.getHeader("newHeader2"));

        Enumeration headers = req.getHeaderNames();
        assertNotNull(headers);
    }

    /* This test case is no need after changing the implementation of TransportHeader.
    * Previously TransportHeader is used two map to hold header information that put using
    * TransportHeader.put() method. Now use one map to hold these header detail. 
    * 
    public void testLocalMap() {
        HttpServletRequest req = new TestServletRequest();
        TransportHeaders headers = new TransportHeaders(req);
        String checkValue = null;
        //assertNull(headers.headerMap);
        assertNotNull(headers.localHeaderMap);
        assertTrue(headers.localHeaderMap.isEmpty());

        // Add a header that doesn't exist in the servlet request and make sure it doesn't
        // populate the headerMap
        headers.put("newHeader1", "newHeader1Value");
        checkValue = (String) headers.get("newHeader1");
        //assertNull(headers.headerMap);
        assertFalse(headers.localHeaderMap.isEmpty());
        assertEquals(1, headers.localHeaderMap.size());
        assertEquals("newHeader1Value", checkValue);

        // Add a header that does exist in the servlet request with a new value still shouldn't
        // populate
        headers.put("header3", "h3ValueNew");
        checkValue = (String) headers.get("header3");
        //assertNull(headers.headerMap);
        assertFalse(headers.localHeaderMap.isEmpty());
        assertEquals(2, headers.localHeaderMap.size());
        assertEquals("h3ValueNew", checkValue);

        // Now cause the map to be populated from the servlet request
        int size = headers.size();
        assertEquals(4, size);
        //assertNotNull(headers.headerMap);
        assertNull(headers.localHeaderMap);

        assertEquals("h1Value", (String) headers.get("header1"));
        assertEquals("h2Value", (String) headers.get("header2"));
        assertEquals("h3ValueNew", (String) headers.get("header3"));
        assertEquals("newHeader1Value", (String) headers.get("newHeader1"));

        // Now do another put of a new value and make sure it is put in the newly populated hashmap
        headers.put("newHeader2", "newHeader2Value");
        checkValue = (String) headers.get("newHeader2");
        //assertNotNull(headers.headerMap);
        assertNull(headers.localHeaderMap);
        size = headers.size();
        assertEquals(5, size);
        assertEquals("newHeader2Value", checkValue);

    }
    */

    public void testNoPopulateOnGet() {
        // Doing a get before a put shouldn't expand the headerMap.
        HttpServletRequest req = new TestServletRequest();
        TransportHeaders headers = new TransportHeaders(req);
        String checkValue = null;
        //assertNull(headers.headerMap);
        assertNotNull(headers.localHeaderMap);
        assertTrue(headers.localHeaderMap.isEmpty());

        checkValue = (String) headers.get("header1");
        assertNotNull(checkValue);
        //assertNull(headers.headerMap);
        assertNotNull(headers.localHeaderMap);
        assertEquals("h1Value", checkValue);
    }
}
