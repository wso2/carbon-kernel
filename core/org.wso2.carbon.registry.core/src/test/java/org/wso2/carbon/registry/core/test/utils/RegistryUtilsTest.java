/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.test.utils;

import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class RegistryUtilsTest extends BaseTestCase {

    public void testGetResourceName() {

        assertEquals("Resource name incorrect.", "/", RegistryUtils.getResourceName("/"));

        assertEquals("Resource name incorrect.", "a", RegistryUtils.getResourceName("/a"));

        assertEquals("Resource name incorrect.", "b", RegistryUtils.getResourceName("/a/b"));

        assertEquals("Resource name incorrect.", "a.txt",
                RegistryUtils.getResourceName("/a/a.txt"));

        assertEquals("Resource name incorrect.", "a", RegistryUtils.getResourceName("/a/"));
        
        assertEquals("Resource name incorrect.", "b", RegistryUtils.getResourceName("/a/b/"));
    }
}
