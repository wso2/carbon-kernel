/*
 * Copyright 2017 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bootstrap;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.security.AccessControlException;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNull;

/**
 * Tests functionality of CarbonSecurityManager class
 */
public class CarbonSecurityManagerTest {
    private CarbonSecurityManager carbonSecurityManager;

    /**
     * Test if creation of an instance of CarbonSecurityManager throws
     * IllegalArgumentException when denied.system.properties is not specified
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCarbonSecurityManager() {
        carbonSecurityManager = new CarbonSecurityManager();
    }

    /**
     * Test printDeniedSystemProperties method when denied.system.properties is specified
     * The method is private and accessed by the constructor
     */
    @Test
    public void testPrintDeniedSystemProperties() {
        System.setProperty("denied.system.properties", "mockDeniedProperty");
        carbonSecurityManager = new CarbonSecurityManager();
    }

    /**
     * Test checkPropertyAccess method for an allowed property using a mock carbonSecurityManager
     */
    @Test
    public void testCheckPropertyAccess() {
        carbonSecurityManager = mock(CarbonSecurityManager.class);
        carbonSecurityManager.checkPropertiesAccess();
        Mockito.verify(carbonSecurityManager).checkPropertiesAccess();
    }

    /**
     * Test testCheckPropertyAccess throws AccessControlException for a denied property
     */
    @Test(expectedExceptions = AccessControlException.class)
    public void testCheckPropertyAccessDeniedProperty() {
        String key = "mockDeniedProperty";
        System.setProperty("denied.system.properties", "mockDeniedProperty");
        carbonSecurityManager = new CarbonSecurityManager();
        carbonSecurityManager.checkPropertyAccess(key);
    }

    /**
     * Test if checkAccessThread method checks permission for a Thread
     */
    @Test
    public void testCheckAccessThread() {
        carbonSecurityManager = mock(CarbonSecurityManager.class);
        Thread thread = mock(Thread.class);
        carbonSecurityManager.checkAccess(thread);
    }

    /**
     * Test if checkAccessThread method checks permission for a ThreadGroup
     */
    @Test
    public void testCheckAccessThreadGroup() {
        carbonSecurityManager = mock(CarbonSecurityManager.class);
        ThreadGroup threadGroup = mock(ThreadGroup.class);
        carbonSecurityManager.checkAccess(threadGroup);
    }

    /**
     * Test if checkAccessThread handles a null Thread
     */
    @Test
    public void testCheckAccessNullThread() {
        carbonSecurityManager = mock(CarbonSecurityManager.class);
        Thread thread = null;
        assertNull(thread);
        carbonSecurityManager.checkAccess(thread);
    }

    /**
     * Test if checkAccessThread handles a null ThreadGroup
     */
    @Test
    public void testCheckAccessNullThreadGroup() {
        carbonSecurityManager = mock(CarbonSecurityManager.class);
        ThreadGroup threadGroup = null;
        assertNull(threadGroup);
        carbonSecurityManager.checkAccess(threadGroup);
    }
}
