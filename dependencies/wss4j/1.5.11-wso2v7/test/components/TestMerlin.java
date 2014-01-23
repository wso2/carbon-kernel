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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.ws.security.components.crypto.AbstractCrypto;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;

/**
 * Created by IntelliJ IDEA.
 * User: srida01
 * Date: Apr 12, 2004
 * Time: 10:50:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestMerlin extends TestCase {
    /**
     * TestScenario1 constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestMerlin(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestMerlin.class);
    }

    public void testCrypto() {
        Crypto crypto = CryptoFactory.getInstance();
        assertTrue(crypto != null);
    }

    public void testAbstractCryptoWithNullProperties() 
        throws Exception {
        Crypto crypto = new NullPropertiesCrypto();
        assertTrue(crypto != null);
    }
    
    /**
     * WSS-102 -- ensure AbstractCrypto will null properties
     * can be instantiated
     */
    private static class NullPropertiesCrypto extends AbstractCrypto {
        public NullPropertiesCrypto() 
            throws Exception {
            super((java.util.Properties) null);
        }
    }
}
