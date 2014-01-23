/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.wsdl.codegen;

import junit.framework.TestCase;

public class XML2JavaMappingTest extends TestCase {
    public void testVersion() throws Exception {
        Class iface = Class.forName("sample.axisversion.xsd.Version");
        assertNotNull(iface.getMethod("getVersion"));
        assertNotNull(iface.getMethod("fooBar"));
        assertNotNull(iface.getMethod("fooBaR2"));
        boolean caughtException = false;
        try {
            iface.getMethod("foobar2");
        } catch (NoSuchMethodException e) {
            caughtException = true;
        }
        assertTrue("Didn't catch expected Exception!", caughtException);
    }
}
