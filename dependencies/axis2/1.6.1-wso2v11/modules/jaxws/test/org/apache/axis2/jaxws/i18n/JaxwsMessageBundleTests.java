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

package org.apache.axis2.jaxws.i18n;

import junit.framework.TestCase;

/**
 * Tests basic function of the Message Bundle
 *
 */
public class JaxwsMessageBundleTests extends TestCase {
    
    public JaxwsMessageBundleTests() { 
        super();
    }
   
    public JaxwsMessageBundleTests(String arg) {
        super(arg);   
    }
    
    /**
     * @testStrategy: Test that the resource bundle
     * is installed by obtaining a message
     */
    public void testMessages() throws Exception {
        final String str = "This string is a test string 01.";
        String tempStr = Messages.getMessage("test01");
        // Check the String for accuracy
        assertTrue(str.equals(tempStr));
    }

}
