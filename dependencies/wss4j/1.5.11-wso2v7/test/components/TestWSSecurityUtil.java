/**
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
package components;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.ws.security.util.WSSecurityUtil;

/**
 *
 */
public class TestWSSecurityUtil extends TestCase {

    public TestWSSecurityUtil(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestWSSecurityUtil.class);
    }
    
    
    public void
    testResolveSecureRandom() throws java.lang.Exception {
        //
        // Expect failure on bogus algorithm id
        //
        try {
            WSSecurityUtil.resolveSecureRandom("no-such-algorithm");
            fail("Expected failure on resolveSecureRandom");
        } catch (final NoSuchAlgorithmException e) {
            // complete
        }
        //
        // Test 
        //
        final SecureRandom r1 = WSSecurityUtil.resolveSecureRandom();
        assertNotNull(r1);
        final SecureRandom r2 = WSSecurityUtil.resolveSecureRandom();
        assertSame(r1, r2);
    }
}
