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
package interop;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;

import java.security.cert.X509Certificate;
import java.security.PrivateKey;

/**
 * Created by IntelliJ IDEA.
 * User: srida01
 * Date: Mar 15, 2004
 * Time: 10:47:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestInteropKeys extends TestCase {
    /**
     * TestScenario1 constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestInteropKeys(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestInteropKeys.class);
    }

    public void testInteropKeys1() throws Exception {
        Crypto c = CryptoFactory.getInstance("wsstest.properties");
        X509Certificate[] certs = c.getCertificates("alice");
        assertTrue(certs != null);
        assertTrue(certs[0] != null);
        PrivateKey privKey = c.getPrivateKey("alice","password");
        assertTrue(privKey != null);
    }

    public void testInteropKeys2() throws Exception {
        Crypto c = CryptoFactory.getInstance("wsstest.properties");
        X509Certificate[] certs = c.getCertificates("bob");
        assertTrue(certs != null);
        assertTrue(certs[0] != null);
        PrivateKey privKey = c.getPrivateKey("bob","password");
        assertTrue(privKey != null);
    }
}
